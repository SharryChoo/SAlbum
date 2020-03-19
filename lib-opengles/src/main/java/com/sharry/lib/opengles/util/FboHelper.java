package com.sharry.lib.opengles.util;

import android.opengl.GLES20;

import com.sharry.lib.opengles.texture.ITextureRenderer;

public class FboHelper implements ITextureRenderer {

    private int mTextureId;
    private int mFramebufferId;

    public FboHelper() {
    }

    @Override
    public void onAttach() {
        createFbo();
        createTexture();
    }

    private void createFbo() {
        // 创建 fbo
        int[] fBoIds = new int[1];
        GLES20.glGenFramebuffers(1, fBoIds, 0);
        mFramebufferId = fBoIds[0];
    }

    private void createTexture() {
        int[] textureIds = new int[1];
        GLES20.glGenTextures(1, textureIds, 0);
        mTextureId = textureIds[0];
        // 绑定纹理
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureId);
        // 设置纹理环绕方式
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);
        // 设置纹理过滤方式
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        // 解绑纹理
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
    }

    @Override
    public void onSizeChanged(int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        // 更新纹理的大小
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureId);
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
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        // 将纹理绑定到 FBO 上, 作为颜色附件
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFramebufferId);
        GLES20.glFramebufferTexture2D(
                GLES20.GL_FRAMEBUFFER,
                GLES20.GL_COLOR_ATTACHMENT0,  // 描述为颜色附件
                GLES20.GL_TEXTURE_2D,
                mTextureId,
                0
        );
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
    }

    @Override
    public void onDraw() {
        // nothing.
    }

    @Override
    public void onDetach() {
        // 删除 FBO
        if (mFramebufferId != 0) {
            int[] fBoIds = new int[1];
            fBoIds[0] = mFramebufferId;
            GLES20.glDeleteFramebuffers(1, fBoIds, 0);
        }
        // 删除 2D 纹理
        if (mTextureId != 0) {
            int size = 1;
            int[] textures = new int[size];
            textures[0] = mTextureId;
            GLES20.glDeleteTextures(1, textures, 0);
        }
    }

    public void bindFramebuffer() {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFramebufferId);
    }

    public void unbindFramebuffer() {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
    }

    public int getTexture2DId() {
        return mTextureId;
    }

}
