package com.sharry.lib.media.recorder;

import android.opengl.GLES20;

import com.sharry.lib.opengles.texture.ITextureRenderer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class H264Render implements ITextureRenderer {

    /**
     * 顶点坐标
     */
    private float[] mVertexCoordinate = new float[]{
            -1f, 1f,
            -1f, -1f,
            1f, 1f,
            1f, -1f
    };

    /**
     * 纹理坐标
     */
    private float[] mFragmentCoordinate = new float[]{
            0f, 1f,
            0f, 0f,
            1f, 1f,
            1f, 0f
    };

    private static final String vertexSource = "attribute vec4 v_Position;\n" +
            "attribute vec2 f_Position;\n" +
            "varying vec2 ft_Position;\n" +
            "void main() {\n" +
            "    ft_Position = f_Position;\n" +
            "    gl_Position = v_Position;\n" +
            "}\n";

    private static final String fragmentSource = "precision mediump float;\n" +
            "varying vec2 ft_Position;\n" +
            "uniform sampler2D sTexture;\n" +
            "void main() {\n" +
            "    gl_FragColor=texture2D(sTexture, ft_Position);\n" +
            "}\n";

    private FloatBuffer mVertexBuffer;
    private FloatBuffer mFragmentBuffer;
    private int mVboId;
    private int mProgram;
    private int vPosition;
    private int fPosition;
    private int mTextureId;

    H264Render(int textureId) {
        mTextureId = textureId;
        mVertexBuffer = createBuffer(mVertexCoordinate);
        mFragmentBuffer = createBuffer(mFragmentCoordinate);
    }

    @Override
    public void onAttach() {
        mProgram = createProgram(vertexSource, fragmentSource);
        // 获取坐标
        vPosition = GLES20.glGetAttribLocation(mProgram, "v_Position");
        fPosition = GLES20.glGetAttribLocation(mProgram, "f_Position");
        // 创建 vbos
        int[] vBos = new int[1];
        GLES20.glGenBuffers(1, vBos, 0);
        // 绑定 vbos
        mVboId = vBos[0];
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mVboId);
        // 开辟 vbos
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, (mVertexCoordinate.length + mFragmentCoordinate.length) * 4,
                null, GLES20.GL_STATIC_DRAW);
        // 赋值 vbos
        GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, 0, mVertexCoordinate.length * 4, mVertexBuffer);
        GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, mVertexCoordinate.length * 4,
                mFragmentCoordinate.length * 4, mFragmentBuffer);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
    }

    @Override
    public void onSizeChanged(int width, int height) {
        GLES20.glViewport(0, 0, width, height);
    }

    @Override
    public void onDraw() {
        // 激活 program
        GLES20.glUseProgram(mProgram);
        // 绑定纹理
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureId);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mVboId);
        // 给顶点坐标赋值
        GLES20.glEnableVertexAttribArray(vPosition);
        GLES20.glVertexAttribPointer(vPosition, 2, GLES20.GL_FLOAT,
                false, 8, 0);
        // 给纹理坐标赋值
        GLES20.glEnableVertexAttribArray(fPosition);
        GLES20.glVertexAttribPointer(fPosition, 2, GLES20.GL_FLOAT, false,
                8, mVertexCoordinate.length * 4);
        // 绘制到屏幕
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        // 解绑
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
    }

    @Override
    public void onDetach() {
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
    }

    private FloatBuffer createBuffer(float[] vertexData) {
        FloatBuffer buffer = ByteBuffer.allocateDirect(vertexData.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        buffer.put(vertexData, 0, vertexData.length)
                .position(0);
        return buffer;
    }

    private int createProgram(String vertexSource, String fragmentSource) {
        // 分别加载创建着色器
        int vertexShaderId = compileShader(GLES20.GL_VERTEX_SHADER, vertexSource);
        int fragmentShaderId = compileShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource);
        if (vertexShaderId != 0 && fragmentShaderId != 0) {
            // 创建 OpenGL 程序 ID
            int programId = GLES20.glCreateProgram();
            if (programId == 0) {
                return 0;
            }
            // 链接上 顶点着色器
            GLES20.glAttachShader(programId, vertexShaderId);
            // 链接上 片段着色器
            GLES20.glAttachShader(programId, fragmentShaderId);
            // 链接 OpenGL 程序
            GLES20.glLinkProgram(programId);
            // 验证链接结果是否失败
            int[] status = new int[1];
            GLES20.glGetProgramiv(programId, GLES20.GL_LINK_STATUS, status, 0);
            if (status[0] != GLES20.GL_TRUE) {
                // 失败后删除这个 OpenGL 程序
                GLES20.glDeleteProgram(programId);
                return 0;
            }
            return programId;
        }
        return 0;
    }

    private int compileShader(int shaderType, String source) {
        // 创建着色器 ID
        int shaderId = GLES20.glCreateShader(shaderType);
        if (shaderId != 0) {
            // 1. 将着色器 ID 和着色器程序内容关联
            GLES20.glShaderSource(shaderId, source);
            // 2. 编译着色器
            GLES20.glCompileShader(shaderId);
            // 3. 验证编译结果
            int[] status = new int[1];
            GLES20.glGetShaderiv(shaderId, GLES20.GL_COMPILE_STATUS, status, 0);
            if (status[0] != GLES20.GL_TRUE) {
                // 编译失败删除这个着色器 id
                GLES20.glDeleteShader(shaderId);
                return 0;
            }
        }
        return shaderId;
    }

}
