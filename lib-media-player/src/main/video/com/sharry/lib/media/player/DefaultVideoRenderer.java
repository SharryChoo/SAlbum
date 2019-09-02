package com.sharry.lib.media.player;

import android.content.Context;
import android.opengl.GLES20;

import com.sharry.lib.opengles.GlUtil;

import java.nio.FloatBuffer;

/**
 * 默认的视频渲染器
 *
 * @author Sharry <a href="xiaoyu.zhu@1hai.cn">Contact me.</a>
 * @version 1.0
 * @since 2019-08-22 10:45
 */
public class DefaultVideoRenderer extends VideoRendererWrapper {

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
    private int mProgramId;
    private int aVertexPosition;
    private int aTexturePosition;
    private int mVboId;
    private int uTexture;

    public DefaultVideoRenderer(Context context) {
        super(new VideoRendererImpl(context));
    }

    @Override
    public void onEGLContextCreated() {
        super.onEGLContextCreated();
        // 上下文变更了, 重置数据
        reset();
        // 初始化程序
        setupShaders();
        // 初始化顶点坐标
        setupCoordinates();
    }

    @Override
    public void onDrawTexture(int yuvTextureId) {
        GLES20.glUseProgram(mProgramId);
        // 绑定纹理
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, yuvTextureId);
        // 写入顶点坐标
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mVboId);
        GLES20.glEnableVertexAttribArray(aVertexPosition);
        GLES20.glVertexAttribPointer(aVertexPosition, 2, GLES20.GL_FLOAT, false,
                8, 0);
        // 写入纹理坐标
        GLES20.glEnableVertexAttribArray(aTexturePosition);
        GLES20.glVertexAttribPointer(aTexturePosition, 2, GLES20.GL_FLOAT, false,
                8, mVertexCoordinate.length * 4);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        // 给 uTexture 赋值
        GLES20.glUniform1i(uTexture, 0);
        // 绘制到屏幕
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        // 解绑纹理
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
    }

    private void reset() {
        this.mProgramId = 0;
        this.mVboId = 0;
    }

    private void setupShaders() {
        if (mProgramId != 0) {
            return;
        }
        mProgramId = GlUtil.createProgram(VERTEX_SHADER_STR, FRAGMENT_SHADER_STR);
        aVertexPosition = GLES20.glGetAttribLocation(mProgramId, "aVertexPosition");
        aTexturePosition = GLES20.glGetAttribLocation(mProgramId, "aTexturePosition");
        uTexture = GLES20.glGetUniformLocation(mProgramId, "uTexture");
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

}
