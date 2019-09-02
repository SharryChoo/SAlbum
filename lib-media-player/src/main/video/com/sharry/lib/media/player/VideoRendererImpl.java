package com.sharry.lib.media.player;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;

import com.sharry.lib.opengles.GlUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * 将 YUV 的数据输出到 Sampler 2D 的纹理, 并不会直接输出到屏幕
 *
 * @author Sharry <a href="xiaoyu.zhu@1hai.cn">Contact me.</a>
 * @version 1.0
 * @since 2019-07-18 16:58
 */
public class VideoRendererImpl implements IVideoRenderer {

    /**
     * 画布顶点坐标
     */
    private final float[] mVertexCoordinate = new float[]{
            -1f, 1f,  // 左上
            -1f, -1f, // 左下
            1f, 1f,   // 右上
            1f, -1f   // 右下
    };
    private final FloatBuffer mVertexBuffer;

    /**
     * YUV 纹理坐标, 映射时颠倒一下
     */
    private final float[] mTextureCoordinate = new float[]{
            0f, 0f,   // 左下
            0f, 1f,   // 左上
            1f, 0f,   // 右下
            1f, 1f    // 右上
    };
    private final FloatBuffer mTextureBuffer;

    /**
     * 着色器程序和相关变量
     */
    private final Context mContext;
    private final FboHelper mFbo;
    private int mProgram;
    private int aVertexPosition;
    private int aTexturePosition;
    private int uVertexMatrix;
    private int uSamplerY;
    private int uSamplerU;
    private int uSamplerV;
    private int mVboId;

    /**
     * 纹理 id
     */
    private final int[] mYUVTextureIds = new int[3];
    private int mSurfaceWidth, mSurfaceHeight;
    private int mFrameWidth, mFrameHeight;

    /**
     * 纹理数据
     */
    private ByteBuffer mFrameY, mFrameU, mFrameV;

    /**
     * 投影矩阵
     */
    private final float[] mProjectionMatrix = new float[16];

    /**
     * 图像缩放类型
     */
    private int mScaleType = SCALE_TYPE_FIT_CENTER;

    public VideoRendererImpl(Context context) {
        this.mContext = context;
        this.mFbo = new FboHelper();
        // 初始化顶点坐标
        mVertexBuffer = ByteBuffer.allocateDirect(mVertexCoordinate.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(mVertexCoordinate);
        mVertexBuffer.position(0);
        // 初始化纹理顶点坐标
        mTextureBuffer = ByteBuffer.allocateDirect(mTextureCoordinate.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(mTextureCoordinate);
        mTextureBuffer.position(0);
    }

    @Override
    public void onEGLContextCreated() {
        // 配置着色器
        setupShaders();
        // 配置 vertex buffer
        setupVbo();
        // 创建 YUV 的纹理 ID
        createTextures();
    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        this.mFbo.onSurfaceSizeChanged(width, height);
        this.mSurfaceWidth = width;
        this.mSurfaceHeight = height;
        GLES20.glViewport(0, 0, width, height);
        // 清屏并绘制黑色
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glClearColor(0f, 0f, 0f, 1f);
    }

    @Override
    public void onDrawFrame() {
        mFbo.bindFramebuffer();

        // 清屏并绘制黑色
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glClearColor(0f, 0f, 0f, 1f);
        if (mFrameWidth == 0 || mFrameHeight == 0 || mFrameY == null || mFrameU == null || mFrameV == null) {
            return;
        }
        // 激活 Program
        GLES20.glUseProgram(mProgram);
        // 写入裁剪矩阵数据
        GLES20.glUniformMatrix4fv(uVertexMatrix, 1, false, mProjectionMatrix, 0);
        // 写入顶点坐标数据
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mVboId);
        GLES20.glEnableVertexAttribArray(aVertexPosition);
        GLES20.glVertexAttribPointer(aVertexPosition, 2, GLES20.GL_FLOAT, false, 8, 0);
        // 写入纹理坐标数据
        GLES20.glEnableVertexAttribArray(aTexturePosition);
        GLES20.glVertexAttribPointer(aTexturePosition, 2, GLES20.GL_FLOAT, false, 8,
                mVertexCoordinate.length * 4);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

        // 绘制 Y 纹理
        GLES20.glUniform1i(uSamplerY, 0);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mYUVTextureIds[0]);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_LUMINANCE, mFrameWidth,
                mFrameHeight, 0, GLES20.GL_LUMINANCE, GLES20.GL_UNSIGNED_BYTE, mFrameY);
        // 绘制 U 纹理
        GLES20.glUniform1i(uSamplerU, 1);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mYUVTextureIds[1]);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_LUMINANCE, mFrameWidth >> 1,
                mFrameHeight >> 1, 0, GLES20.GL_LUMINANCE, GLES20.GL_UNSIGNED_BYTE, mFrameU);
        // 绘制 V 纹理
        GLES20.glUniform1i(uSamplerV, 2);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE2);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mYUVTextureIds[2]);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_LUMINANCE, mFrameWidth >> 1,
                mFrameHeight >> 1, 0, GLES20.GL_LUMINANCE, GLES20.GL_UNSIGNED_BYTE, mFrameV);

        // 执行绘制
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);

        mFbo.unbindFramebuffer();
    }

    @Override
    public void onRenderYUV(int width, int height, byte[] y, byte[] u, byte[] v) {
        this.mFrameY = ByteBuffer.wrap(y);
        this.mFrameU = ByteBuffer.wrap(u);
        this.mFrameV = ByteBuffer.wrap(v);
        if (width != mFrameWidth && height != mFrameHeight) {
            this.mFrameWidth = width;
            this.mFrameHeight = height;
            float aspectPlane = mSurfaceWidth / (float) mSurfaceHeight;
            float aspectTexture = mFrameWidth / (float) mFrameHeight;
            switch (mScaleType) {
                case SCALE_TYPE_FIT_XY:
                    // 初始化正交投影
                    Matrix.orthoM(mProjectionMatrix, 0, -1, 1, -1, 1, 1, -1);
                    break;
                case SCALE_TYPE_CENTER_CROP:
                    centerCrop(aspectPlane, aspectTexture);
                    break;
                case SCALE_TYPE_FIT_CENTER:
                default:
                    fitCenter(aspectPlane, aspectTexture);
                    break;
            }
        }
    }


    @Override
    public void setScaleMode(@ScaleType int scaleType) {
        this.mScaleType = scaleType;
    }

    @Override
    public int getTextureId() {
        return mFbo.getTexture2DId();
    }

    private void setupShaders() {
        // 获取 GL 代码
        String vertexSource = GlUtil.getGLResource(mContext, R.raw.video_vertex_shader);
        String fragmentSource = GlUtil.getGLResource(mContext, R.raw.video_fragment_shader);
        mProgram = GlUtil.createProgram(vertexSource, fragmentSource);

        // 获取坐标
        aVertexPosition = GLES20.glGetAttribLocation(mProgram, "aVertexPosition");
        aTexturePosition = GLES20.glGetAttribLocation(mProgram, "aTexturePosition");
        uVertexMatrix = GLES20.glGetUniformLocation(mProgram, "uVertexMatrix");
        uSamplerY = GLES20.glGetUniformLocation(mProgram, "uSamplerY");
        uSamplerU = GLES20.glGetUniformLocation(mProgram, "uSamplerU");
        uSamplerV = GLES20.glGetUniformLocation(mProgram, "uSamplerV");
    }

    private void setupVbo() {
        // 创建 vbo(Vertex Buffer Object)
        int vboSize = 1;
        int[] vboIds = new int[vboSize];
        // 1. 创建缓冲对象
        GLES20.glGenBuffers(
                vboSize,      // n: 缓冲区数量
                vboIds,       // buffers: 传出参数, 用于保存创建好的 vbo id
                0     // offset: 描述 buffers 的偏移量
        );
        mVboId = vboIds[0];
        // 2. 绑定缓冲对象
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mVboId);
        // 3. 为缓冲对象开辟缓冲区
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER,
                (mVertexCoordinate.length + mTextureCoordinate.length) * 4,
                null, GLES20.GL_STATIC_DRAW);
        // 4.1 将顶点坐标
        GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, 0,
                mVertexCoordinate.length * 4, mVertexBuffer);
        // 4.2 将纹理坐标写入
        GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, mVertexCoordinate.length * 4,
                mTextureCoordinate.length * 4, mTextureBuffer);
        // 5. 解绑缓冲对象
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
    }

    private void fitCenter(float aspectPlane, float aspectTexture) {
        float left, top, right, bottom;
        // 1. 纹理比例 > 投影平面比例
        if (aspectTexture > aspectPlane) {
            left = -1;
            right = 1;
            top = 1 / aspectPlane * aspectTexture;
            bottom = -top;
        }
        // 2. 纹理比例 < 投影平面比例
        else {
            left = -aspectPlane / aspectTexture;
            right = -left;
            top = 1;
            bottom = -1;
        }
        Matrix.orthoM(
                mProjectionMatrix, 0,
                left, right, bottom, top,
                1, -1
        );
    }

    private void centerCrop(float aspectPlane, float aspectTexture) {
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
    }

    /**
     * 创建纹理
     */
    private void createTextures() {
        // 生成绑定纹理
        GLES20.glGenTextures(3, mYUVTextureIds, 0);
        for (int textureId : mYUVTextureIds) {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
            // 设置环绕方向
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);
            // 设置纹理过滤方式
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            // 解绑
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        }
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
