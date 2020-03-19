package com.sharry.lib.opengles.util;

import android.content.Context;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class GlUtil {

    /**
     * 获取 glsl 资源
     */
    public static String getGLResource(Context context, int rawId) {
        InputStream inputStream = context.getResources().openRawResource(rawId);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder builder = new StringBuilder();
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                builder.append(line).append("\n");
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return builder.toString();
    }

    /**
     * 创建顶点 buffer
     */
    public static FloatBuffer createFloatBuffer(float[] coords) {
        FloatBuffer buffer = ByteBuffer.allocateDirect(coords.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        buffer.put(coords, 0, coords.length)
                .position(0);
        return buffer;
    }

    /**
     * 创建一个 OpenGL 程序
     *
     * @param vertexSource   顶点着色器源码
     * @param fragmentSource 片元着色器源码
     */
    public static int createProgram(String vertexSource, String fragmentSource) {
        // 分别加载创建着色器
        int vertexShaderId = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource);
        int fragmentShaderId = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource);
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

    /**
     * 编译着色器
     *
     * @param shaderType 着色器的类型
     * @param source     资源源代码
     */
    private static int loadShader(int shaderType, String source) {
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

    ////////////////////////////////////////////////////////////////////////////
    // Texture 创建相关
    ////////////////////////////////////////////////////////////////////////////

    /**
     * 创建纹理 type {@link GLES20#GL_TEXTURE_2D}
     * @return textureId
     */
    public static int createTexture2D() {
        // 生成绑定纹理
        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);
        int textureId = textures[0];
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        // 设置环绕方向
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);
        // 设置纹理过滤方式
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        // 解绑
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        return textureId;
    }

    /**
     * 创建纹理 type {@link GLES11Ext#GL_TEXTURE_EXTERNAL_OES}
     * @return textureId
     */
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

    ////////////////////////////////////////////////////////////////////////////
    // Framebuffer 相关
    ////////////////////////////////////////////////////////////////////////////

    /**
     * 绑定纹理附件类型的 FBO
     *
     * @param frameBufferId FBO ID
     * @param texture2DId   2D 纹理 ID
     */
    public static void bindFrameTexture(int frameBufferId, int texture2DId) {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBufferId);
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,
                GLES20.GL_TEXTURE_2D, texture2DId, 0);
    }

    /**
     * 解绑 FBO
     */
    public static void unBindFrameBuffer() {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
    }

    public static void checkGlError(String operation) {
        int error = GLES20.glGetError();
        if (error != GLES20.GL_NO_ERROR) {
            String msg = operation + ", glError " + error;
        }
    }

}
