package com.sharry.app.salbum;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import com.sharry.lib.camera.PreviewerRendererImpl;
import com.sharry.lib.camera.PreviewerRendererWrapper;
import com.sharry.lib.opengles.util.FboHelper;
import com.sharry.lib.opengles.util.GlUtil;

import java.nio.FloatBuffer;

/**
 * 带水印效果的渲染器
 * <p>
 *
 * @author Sharry <a href="sharrychoochn@gmail.com">Contact me.</a>
 * @version 1.0
 * @since 2019-08-01 16:04
 */
public class WatermarkPreviewerRenderer extends PreviewerRendererWrapper {

    private static final String VERTEX_SHADER_STR = "attribute vec4 aVertexPosition;\n" +
            "    attribute vec2 aTexturePosition;\n" +
            "    varying vec2 vPosition;\n" +
            "    void main() {\n" +
            "        vPosition = aTexturePosition;\n" +
            "        gl_Position = aVertexPosition;\n" +
            "    }";


    private static final String FRAGMENT_SHADER_STR = "precision mediump float;\n" +
            "varying vec2 vPosition;\n" +
            "uniform sampler2D uTexture;\n" +
            "void main() {\n" +
            "    gl_FragColor=texture2D(uTexture, vPosition);\n" +
            "}";

    /**
     * 相机顶点坐标
     */
    private final float[] mCameraVertexCoords = new float[]{
            -1f, 1f,  // 左上
            -1f, -1f, // 左下
            1f, 1f,   // 右上
            1f, -1f,   // 右下
    };

    /**
     * 相机纹理映射坐标
     */
    private final float[] mCameraTextureCoords = new float[]{
            0f, 1f,   // 左上
            0f, 0f,   // 左下
            1f, 1f,   // 右上
            1f, 0f    // 右下
    };

    /**
     * 水印顶点坐标
     */
    private final float[] mWatermarkVertexCoords = new float[]{
            0f, 0f,  // 左上
            0f, 0f,  // 左下
            0f, 0f,  // 右上
            0f, 0f,  // 右下
    };

    /**
     * 水印纹理坐标, 水印从 Bitmap 中加载, 坐标系相反
     */
    private final float[] mWatermarkTextureCoords = new float[]{
            0f, 0f,   // 左下
            0f, 1f,   // 左上
            1f, 0f,   // 右下
            1f, 1f    // 右上
    };

    /**
     * 相机纹理顶点和纹理坐标
     */
    private final FloatBuffer mCameraTextureVertexBuffer = GlUtil.createFloatBuffer(mCameraVertexCoords);
    private final FloatBuffer mCameraTextureBuffer = GlUtil.createFloatBuffer(mCameraTextureCoords);

    /**
     * 水印纹理顶点和纹理坐标
     */
    private final FloatBuffer mWatermarkVertexBuffer = GlUtil.createFloatBuffer(mWatermarkVertexCoords);
    private final FloatBuffer mWatermarkTextureBuffer = GlUtil.createFloatBuffer(mWatermarkTextureCoords);

    private final Context mContext;
    private final FboHelper mFboHelper;
    private int mProgramId;
    private int aVertexPosition;
    private int aTexturePosition;
    private int mVboId;
    private int uTexture;
    private int mWatermarkTextureId = 0;
    private Bitmap mWatermarkBitmap;

    public WatermarkPreviewerRenderer(Context context) {
        super(new PreviewerRendererImpl(context));
        this.mContext = context;
        this.mFboHelper = new FboHelper();
    }

    @Override
    public void onAttach() {
        super.onAttach();
        mFboHelper.onAttach();
        // 初始化程序
        setupShaders();
        // 初始化顶点坐标
        setupCoordinates();
        // 初始化水印纹理
        setupWatermarkTexture();
    }

    private void setupShaders() {
        mProgramId = GlUtil.createProgram(VERTEX_SHADER_STR, FRAGMENT_SHADER_STR);
        aVertexPosition = GLES20.glGetAttribLocation(mProgramId, "aVertexPosition");
        aTexturePosition = GLES20.glGetAttribLocation(mProgramId, "aTexturePosition");
        uTexture = GLES20.glGetUniformLocation(mProgramId, "uTexture");
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
                mCameraVertexCoords.length * 4 +
                        mCameraTextureCoords.length * 4
                        + mWatermarkVertexCoords.length * 4
                        + mWatermarkTextureCoords.length * 4,
                null,
                GLES20.GL_STATIC_DRAW
        );
        // 写入相机顶点坐标
        GLES20.glBufferSubData(
                GLES20.GL_ARRAY_BUFFER,
                0,
                mCameraVertexCoords.length * 4,
                mCameraTextureVertexBuffer
        );
        // 写入相机纹理坐标
        GLES20.glBufferSubData(
                GLES20.GL_ARRAY_BUFFER,
                mCameraVertexCoords.length * 4,
                mCameraTextureCoords.length * 4,
                mCameraTextureBuffer
        );
        // 写入水印顶点坐标
        GLES20.glBufferSubData(
                GLES20.GL_ARRAY_BUFFER,
                mCameraVertexCoords.length * 4 +
                        mCameraTextureCoords.length * 4,
                mWatermarkVertexCoords.length * 4,
                mWatermarkVertexBuffer
        );
        // 写入水印纹理坐标
        GLES20.glBufferSubData(
                GLES20.GL_ARRAY_BUFFER,
                mCameraVertexCoords.length * 4 +
                        mCameraTextureCoords.length * 4 +
                        mWatermarkVertexCoords.length * 4,
                mWatermarkTextureCoords.length * 4,
                mWatermarkTextureBuffer
        );
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
    }

    private void setupWatermarkTexture() {
        int[] textureIds = new int[1];
        GLES20.glGenTextures(1, textureIds, 0);
        mWatermarkTextureId = textureIds[0];
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mWatermarkTextureId);
        // 设置纹理环绕方式
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);
        // 设置纹理过滤方式
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        // 创建 Bitmap, 将其写入纹理
        mWatermarkBitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_demo_watermark);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, mWatermarkBitmap, 0);
        // 解绑
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
    }

    @Override
    public void onSizeChanged(int width, int height) {
        super.onSizeChanged(width, height);
        mFboHelper.onSizeChanged(width, height);
        // 启用透明
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        GLES20.glViewport(0, 0, width, height);
        // 更新水印坐标
        updateWatermarkCoors(width, height);
    }

    private void updateWatermarkCoors(int surfaceWidth, int surfaceHeight) {
        float height = mWatermarkBitmap.getHeight();
        float width = mWatermarkBitmap.getWidth();
        height = height * (1 / (float) surfaceHeight);
        width = width * (1 / (float) surfaceWidth);
        float left = -0.9f;
        float bottom = -0.9f;
        // 设置水印的位置
        // 左上
        mWatermarkVertexCoords[0] = left;
        mWatermarkVertexCoords[1] = bottom + height;
        // 左下
        mWatermarkVertexCoords[2] = left;
        mWatermarkVertexCoords[3] = bottom;
        // 右上
        mWatermarkVertexCoords[4] = left + width;
        mWatermarkVertexCoords[5] = bottom + height;
        // 右下
        mWatermarkVertexCoords[6] = left + width;
        mWatermarkVertexCoords[7] = bottom;
        // 更新 Buffer
        mWatermarkVertexBuffer.put(mWatermarkVertexCoords, 0, mWatermarkVertexCoords.length)
                .position(0);
        // 更新 VBO
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mVboId);
        // 写入水印顶点坐标
        GLES20.glBufferSubData(
                GLES20.GL_ARRAY_BUFFER,
                mCameraVertexCoords.length * 4 +
                        mCameraTextureCoords.length * 4,
                mWatermarkVertexCoords.length * 4,
                mWatermarkVertexBuffer
        );
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
    }

    @Override
    protected void onDrawTexture(int textureId) {
        mFboHelper.bindFramebuffer();
        // 绘制纹理
        drawOriginTexture(textureId);
        // 绘制水印
        drawWatermark();
        // 解绑
        mFboHelper.unbindFramebuffer();
        // 绘制到系统自带的缓冲上
        drawToEGLSurface();
    }

    private void drawOriginTexture(int textureId) {
        GLES20.glUseProgram(mProgramId);
        // 绑定相机的纹理
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        // 写入顶点坐标
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mVboId);
        GLES20.glEnableVertexAttribArray(aVertexPosition);
        GLES20.glVertexAttribPointer(aVertexPosition, 2, GLES20.GL_FLOAT, false, 8, 0);
        // 写入纹理坐标
        GLES20.glEnableVertexAttribArray(aTexturePosition);
        GLES20.glVertexAttribPointer(aTexturePosition, 2, GLES20.GL_FLOAT, false,
                8, mCameraVertexCoords.length * 4);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        // 给 uTexture 赋值
        GLES20.glUniform1i(uTexture, 0);
        // 绘制到屏幕
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        // 解绑纹理
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
    }

    private void drawWatermark() {
        GLES20.glUseProgram(mProgramId);
        // 绑定纹理
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mWatermarkTextureId);
        // 写入水印顶点坐标
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mVboId);
        GLES20.glEnableVertexAttribArray(aVertexPosition);
        GLES20.glVertexAttribPointer(aVertexPosition, 2, GLES20.GL_FLOAT, false,
                8, (mCameraVertexCoords.length + mCameraTextureCoords.length) * 4);
        // 写入水印纹理坐标
        GLES20.glEnableVertexAttribArray(aTexturePosition);
        GLES20.glVertexAttribPointer(
                aTexturePosition,
                2,
                GLES20.GL_FLOAT,
                false,
                8,
                (mCameraVertexCoords.length + mCameraTextureCoords.length + mWatermarkVertexCoords.length) * 4
        );
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        // 给 uTexture 赋值
        GLES20.glUniform1i(uTexture, 0);
        // 绘制到屏幕
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        // 解绑纹理
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
    }

    private void drawToEGLSurface() {
        GLES20.glUseProgram(mProgramId);
        // 绑定纹理
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, getPreviewerTextureId());
        // 写入顶点坐标
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mVboId);
        GLES20.glEnableVertexAttribArray(aVertexPosition);
        GLES20.glVertexAttribPointer(aVertexPosition, 2, GLES20.GL_FLOAT, false,
                8, 0);
        // 写入纹理坐标
        GLES20.glEnableVertexAttribArray(aTexturePosition);
        GLES20.glVertexAttribPointer(aTexturePosition, 2, GLES20.GL_FLOAT, false,
                8, mCameraVertexCoords.length * 4);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        // 给 uTexture 赋值
        GLES20.glUniform1i(uTexture, 0);
        // 绘制到屏幕
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        // 解绑纹理
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
    }

    @Override
    public int getPreviewerTextureId() {
        return mFboHelper.getTexture2DId();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mFboHelper.onDetach();
        // 释放着色器程序
        if (mProgramId != 0) {
            GLES20.glDeleteProgram(mProgramId);
        }
        // 释放 VBO
        if (mVboId != 0) {
            int size = 1;
            int[] vboIds = new int[size];
            vboIds[0] = mVboId;
            GLES20.glDeleteBuffers(1, vboIds, 0);
        }
        // 释放纹理
        if (mWatermarkTextureId != 0) {
            int size = 1;
            int[] textures = new int[size];
            textures[0] = mWatermarkTextureId;
            GLES20.glDeleteTextures(1, textures, 0);
        }
    }
}
