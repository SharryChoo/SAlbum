package com.sharry.lib.camera;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import androidx.annotation.NonNull;

import com.sharry.lib.opengles.GlUtil;

import java.nio.FloatBuffer;

import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.glGetUniformLocation;

/**
 * 处理相机输出的 OES 输出到 2D Texture 中, 2D 纹理 ID  通过 {@link #getTextureId()} 获取
 *
 * @author Sharry <a href="sharrychoochn@gmail.com">Contact me.</a>
 * @version 1.0
 * @since 2019-07-28
 */
public class PreviewerRendererImpl implements IPreviewer.Renderer {

    private static final String TAG = PreviewerRendererImpl.class.getSimpleName();

    private final float[] mVertexCoordinate = new float[]{
            -1f, 1f,  // 左上
            -1f, -1f, // 左下
            1f, 1f,   // 右上
            1f, -1f   // 右下
    };
    private final float[] mTextureCoordinate = new float[]{
            0f, 1f,   // 左上
            0f, 0f,   // 左下
            1f, 1f,   // 右上
            1f, 0f    // 右下
    };
    private final FloatBuffer mVertexBuffer = GlUtil.createFloatBuffer(mVertexCoordinate);
    private final FloatBuffer mTextureBuffer = GlUtil.createFloatBuffer(mTextureCoordinate);

    private final Context mContext;
    private final FboHelper mFboHelper;

    /**
     * 着色器相关
     */
    private int mProgram;
    private int aVertexCoordinate;
    private int aTextureCoordinate;
    private int uTextureMatrix;
    private int uVertexMatrix;
    private int uTexture;

    /**
     * Vertex buffer object 相关
     */
    private int mVboId;

    /**
     * 外部纹理
     */
    private final static int MAGIC_OES_TEXTURE_ID = 0;
    private volatile SurfaceTexture mOESTexture;
    private final float[] mOESTextureMatrix = new float[16];

    /**
     * Matrix
     */
    private final float[] mProjectionMatrix = new float[16];      // 投影矩阵
    private final float[] mRotationMatrix = new float[16];        // 裁剪矩阵
    private final float[] mFinalMatrix = new float[16];           // 裁剪矩阵

    public PreviewerRendererImpl(Context context) {
        mContext = context;
        mFboHelper = new FboHelper();
    }

    @Override
    public void onEGLContextCreated() {
        // 上下文变更了, 重置数据
        reset();
        // 配置着色器
        setupShaders();
        // 配置坐标
        setupCoordinates();
    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        mFboHelper.onSurfaceSizeChanged(width, height);
        GLES20.glViewport(0, 0, width, height);
        // 清屏
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glClearColor(0f, 0f, 0f, 0f);
    }

    @Override
    public void onDrawFrame() {
        // 清屏
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glClearColor(0f, 0f, 0f, 0f);
        // 获取 OES Texture 中的数据帧
        if (mOESTexture != null) {
            try {
                mOESTexture.updateTexImage();
                mOESTexture.getTransformMatrix(mOESTextureMatrix);
            } catch (Throwable e) {
                setupOESTexture();
                return;
            }
        }
        // 绑定 FBO
        mFboHelper.bindFramebuffer();
        // 将外部纹理绘制到 FBO
        drawOES2FBO();
        // 解绑 FBO
        mFboHelper.unbindFramebuffer();
    }

    @Override
    public void onDataSourceChanged(SurfaceTexture oesTexture) {
        mOESTexture = oesTexture;
    }

    @Override
    public void resetMatrix() {
        Matrix.setIdentityM(mProjectionMatrix, 0);
        Matrix.setIdentityM(mRotationMatrix, 0);
        Matrix.setIdentityM(mFinalMatrix, 0);
    }

    @Override
    public void rotate(int degrees) {
        Matrix.rotateM(mRotationMatrix, 0, degrees, 0, 0, 1);
    }

    @Override
    public void centerCrop(boolean isLandscape, Size surfaceSize, Size textureSize) {
        // 设置正交投影
        float aspectPlane = surfaceSize.getWidth() / (float) surfaceSize.getHeight();
        float aspectTexture = isLandscape ? textureSize.getWidth() / (float) textureSize.getHeight()
                : textureSize.getHeight() / (float) textureSize.getWidth();
        float left, top, right, bottom;
        // 1. 纹理比例 > 投影平面比例
        if (aspectTexture > aspectPlane) {
            left = -aspectPlane / aspectTexture;
            right = -left;
            top = 1;
            bottom = -1;
        }
        // 2. 纹理比例 < 投影平面比例
        else {
            left = -1;
            right = 1;
            top = 1 / aspectPlane * aspectTexture;
            bottom = -top;
        }
        Matrix.orthoM(
                mProjectionMatrix, 0,
                left, right, bottom, top,
                1, -1
        );
        Log.e(TAG, "preview size = " + surfaceSize + ", camera size = " + textureSize);
    }

    @Override
    public void applyMatrix() {
        // 使裁剪矩阵合并旋转矩阵
        Matrix.multiplyMM(mFinalMatrix, 0, mProjectionMatrix, 0,
                mRotationMatrix, 0);
    }

    @NonNull
    @Override
    public float[] getMatrix() {
        return mFinalMatrix;
    }

    @Override
    public void setMatrix(@NonNull float[] matrix) {
        System.arraycopy(matrix, 0, mFinalMatrix,
                0, matrix.length);
    }

    @Override
    public int getTextureId() {
        return mFboHelper.getTexture2DId();
    }

    private void reset() {
        this.mProgram = 0;
        this.mVboId = 0;
    }

    private void setupShaders() {
        if (mProgram != 0) {
            return;
        }
        // 加载着色器
        String vertexSource = GlUtil.getGLResource(mContext, R.raw.camera_vertex_shader);
        String fragmentSource = GlUtil.getGLResource(mContext, R.raw.camera_fragment_shader);
        mProgram = GlUtil.createProgram(vertexSource, fragmentSource);
        // 加载 Program 中的变量
        aVertexCoordinate = GLES20.glGetAttribLocation(mProgram, "aVertexCoordinate");
        aTextureCoordinate = GLES20.glGetAttribLocation(mProgram, "aTextureCoordinate");
        uVertexMatrix = glGetUniformLocation(mProgram, "uVertexMatrix");
        uTextureMatrix = glGetUniformLocation(mProgram, "uTextureMatrix");
        uTexture = glGetUniformLocation(mProgram, "uTexture");
    }

    private void setupCoordinates() {
        if (mVboId != 0) {
            return;
        }
        // 创建 vbo
        int vboSize = 1;
        int[] vboIds = new int[vboSize];
        GLES20.glGenBuffers(vboSize, vboIds, 0);
        // 将顶点坐标写入 vbo
        mVboId = vboIds[0];
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mVboId);
        // 开辟 VBO 空间
        GLES20.glBufferData(
                GLES20.GL_ARRAY_BUFFER,
                (mVertexCoordinate.length + mTextureCoordinate.length) * 4,
                null,
                GLES20.GL_STATIC_DRAW
        );
        // 写入顶点坐标
        GLES20.glBufferSubData(
                GLES20.GL_ARRAY_BUFFER,
                0,
                (mVertexCoordinate.length) * 4,
                mVertexBuffer
        );
        // 写入纹理坐标
        GLES20.glBufferSubData(
                GLES20.GL_ARRAY_BUFFER,
                (mVertexCoordinate.length) * 4,
                (mTextureCoordinate.length) * 4,
                mTextureBuffer
        );
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
    }

    private void setupOESTexture() {
        if (mOESTexture == null) {
            return;
        }
        try {
            mOESTexture.detachFromGLContext();
        } catch (Throwable e) {
            // ignore.
        }
        try {
            // 绑定纹理
            mOESTexture.attachToGLContext(MAGIC_OES_TEXTURE_ID);
        } catch (Throwable e) {
            // ignore.
        }
    }

    private void drawOES2FBO() {
        // 激活着色器
        GLES20.glUseProgram(mProgram);
        // 绑定纹理
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, MAGIC_OES_TEXTURE_ID);

        /*
         顶点着色器
         */
        // 顶点坐标赋值
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mVboId);
        GLES20.glEnableVertexAttribArray(aVertexCoordinate);
        GLES20.glVertexAttribPointer(aVertexCoordinate, 2, GL_FLOAT, false,
                8, 0);
        // 纹理坐标赋值
        GLES20.glEnableVertexAttribArray(aTextureCoordinate);
        GLES20.glVertexAttribPointer(aTextureCoordinate, 2, GL_FLOAT, false,
                8, mVertexCoordinate.length * 4);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        // 顶点变换矩阵赋值
        GLES20.glUniformMatrix4fv(uVertexMatrix, 1, false, mFinalMatrix, 0);
        // 纹理变换矩阵赋值
        GLES20.glUniformMatrix4fv(uTextureMatrix, 1, false, mOESTextureMatrix, 0);

        /*
         片元着色器, 为 uTexture 赋值
         */
        GLES20.glUniform1i(uTexture, 0);

        // 执行渲染管线
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        // 解绑纹理
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);
    }

    /**
     * 用于离屏渲染
     */
    private static class FboHelper {

        private int mFramebufferId;
        private int mTextureId;

        FboHelper() {
        }

        void onSurfaceSizeChanged(int width, int height) {
            GLES20.glViewport(0, 0, width, height);
            // 配置纹理
            setupTexture(width, height);
            // 配置 fbo
            setupFbo();
        }

        void bindFramebuffer() {
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFramebufferId);
        }

        void unbindFramebuffer() {
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        }

        int getTexture2DId() {
            return mTextureId;
        }

        private void setupTexture(int width, int height) {
            if (mTextureId == 0) {
                int[] textureIds = new int[1];
                GLES20.glGenTextures(1, textureIds, 0);
                mTextureId = textureIds[0];
            }
            // 绑定纹理
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureId);
            // 设置纹理环绕方式
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);
            // 设置纹理过滤方式
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            // 创建一个空的纹理画布
            GLES20.glTexImage2D(
                    GLES20.GL_TEXTURE_2D,
                    0,
                    GLES20.GL_RGBA,
                    width, height,
                    0,
                    GLES20.GL_RGBA,
                    GLES20.GL_UNSIGNED_BYTE,
                    null
            );
            // 解绑纹理
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        }

        private void setupFbo() {
            if (mFramebufferId != 0) {
                return;
            }
            // 创建 fbo
            int[] fBoIds = new int[1];
            GLES20.glGenBuffers(1, fBoIds, 0);
            mFramebufferId = fBoIds[0];
            // 绑定 fbo
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFramebufferId);
            // 将纹理绑定到 FBO 上, 作为颜色附件
            GLES20.glFramebufferTexture2D(
                    GLES20.GL_FRAMEBUFFER,
                    GLES20.GL_COLOR_ATTACHMENT0,  // 描述为颜色附件
                    GLES20.GL_TEXTURE_2D,
                    mTextureId,
                    0
            );
            // 解绑 fbo
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        }

    }

}
