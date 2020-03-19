package com.sharry.lib.camera;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import com.sharry.lib.opengles.util.FboHelper;
import com.sharry.lib.opengles.util.GlUtil;

import java.nio.FloatBuffer;

import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.glGetUniformLocation;

/**
 * 处理相机输出的 OES 输出到 2D Texture 中, 2D 纹理 ID  通过 {@link #getPreviewerTextureId()} 获取
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
    private int mProgram = 0;
    private int aVertexCoordinate;
    private int aTextureCoordinate;
    private int uTextureMatrix;
    private int uVertexMatrix;
    private int uTexture;

    /**
     * Vertex buffer object 相关
     */
    private int mVboId = 0;

    /**
     * 用于和数据源绑定的外部纹理 ID
     */
    private int mOesTextureId = 0;

    /**
     * 数据源相关变量
     */
    private volatile SurfaceTexture mDataSource;
    private final float[] mDataSourceMatrix = new float[16];
    private boolean mIsAttached = false;

    /**
     * Matrix
     */
    private final float[] mProjectionMatrix = new float[16];      // 投影矩阵
    private final float[] mRotationMatrix = new float[16];        // 裁剪矩阵
    private final float[] mFinalMatrix = new float[16];           // 裁剪矩阵

    public PreviewerRendererImpl(Context context) {
        mContext = context;
        mFboHelper = new FboHelper();
        // 初始化矩阵
        Matrix.setIdentityM(mProjectionMatrix, 0);
        Matrix.setIdentityM(mRotationMatrix, 0);
        Matrix.setIdentityM(mFinalMatrix, 0);
    }

    ////////////////////////////////////////////////////////////////////////////
    // 渲染器的生命周期
    ////////////////////////////////////////////////////////////////////////////

    @Override
    public void onAttach() {
        mFboHelper.onAttach();
        // 配置着色器
        setupShaders();
        // 配置坐标
        setupCoordinates();
        // 创建一个 OES 的纹理 ID, 用于后续绑定 DataSource.
        mOesTextureId = createOESTexture();
    }

    private void setupShaders() {
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

    private int createOESTexture() {
        // 生成绑定纹理
        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);
        int textureId = textures[0];
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);
        // 设置环绕方向
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);
        // 设置纹理过滤方式
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        // 解绑
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);
        return textureId;
    }

    @Override
    public void onSizeChanged(int width, int height) {
        mFboHelper.onSizeChanged(width, height);
        // 设置画布尺寸
        GLES20.glViewport(0, 0, width, height);
        // 清屏
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glClearColor(0f, 0f, 0f, 0f);
    }

    @Override
    public void onDraw() {
        // 清屏
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glClearColor(0f, 0f, 0f, 0f);
        // 获取 OES Texture 中的数据帧
        if (mDataSource != null) {
            try {
                // 为数据源绑定纹理 ID
                if (!mIsAttached) {
                    attachDataSource();
                }
                // 从获取数据源中获取数据
                mDataSource.updateTexImage();
                // 获取数据源的 transform 矩阵
                mDataSource.getTransformMatrix(mDataSourceMatrix);
            } catch (Throwable e) {
                // ignore.
            }
        }
        // 绑定 FBO
        mFboHelper.bindFramebuffer();
        // 将外部纹理绘制到 FBO
        draw();
        // 解绑 FBO
        mFboHelper.unbindFramebuffer();
    }

    private void attachDataSource() {
        try {
            mDataSource.detachFromGLContext();
        } catch (Throwable e) {
            // ignore.
        }
        try {
            mDataSource.attachToGLContext(mOesTextureId);
            mIsAttached = true;
        } catch (Throwable e) {
            // ignore.
        }
    }

    private void draw() {
        // 激活着色器
        GLES20.glUseProgram(mProgram);
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
        // 使裁剪矩阵合并旋转矩阵
        Matrix.multiplyMM(mFinalMatrix, 0, mProjectionMatrix, 0,
                mRotationMatrix, 0);
        // 顶点变换矩阵赋值
        GLES20.glUniformMatrix4fv(uVertexMatrix, 1, false, mFinalMatrix, 0);

        // 纹理变换矩阵赋值
        GLES20.glUniformMatrix4fv(uTextureMatrix, 1, false, mDataSourceMatrix, 0);

        /*
         片元着色器, 为 uTexture 赋值
         */
        GLES20.glUniform1i(uTexture, 0);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mOesTextureId);

        // 绘制矩形区域
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        // 解绑纹理
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);
    }

    @Override
    public void onDetach() {
        mFboHelper.onDetach();
        // 释放着色器程序
        if (mProgram != 0) {
            GLES20.glDeleteProgram(mProgram);
        }
        // 释放 VBO
        if (mVboId != 0) {
            int size = 1;
            int[] vboIds = new int[size];
            vboIds[0] = mVboId;
            GLES20.glDeleteBuffers(1, vboIds, 0);
        }
        // 释放纹理
        if (mOesTextureId != 0) {
            int size = 1;
            int[] textures = new int[size];
            textures[0] = mOesTextureId;
            GLES20.glDeleteTextures(1, textures, 0);
        }
        mIsAttached = false;
    }

    ////////////////////////////////////////////////////////////////////////////
    // 其他
    ////////////////////////////////////////////////////////////////////////////

    @Override
    public void setDataSource(SurfaceTexture dataSource) {
        if (mDataSource != dataSource) {
            mDataSource = dataSource;
            mIsAttached = false;
        }
    }

    @Override
    public void setRotate(int degrees) {
        Matrix.rotateM(mRotationMatrix, 0, degrees, 0, 0, 1);
    }

    @Override
    public void setScaleType(ScaleType type, boolean isLandscape, Size dataSourceSize, Size viewSize) {
        if (type != ScaleType.CENTER_CROP) {
            return;
        }
        // 设置正交投影
        float aspectPlane = viewSize.getWidth() / (float) viewSize.getHeight();
        float aspectTexture = isLandscape ? dataSourceSize.getWidth() / (float) dataSourceSize.getHeight()
                : dataSourceSize.getHeight() / (float) dataSourceSize.getWidth();
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
        Log.e(TAG, "view size = " + viewSize + ", data source size = " + dataSourceSize);
    }

    @Override
    public int getPreviewerTextureId() {
        return mFboHelper.getTexture2DId();
    }

}
