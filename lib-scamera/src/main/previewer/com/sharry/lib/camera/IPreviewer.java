package com.sharry.lib.camera;

import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.opengl.EGLContext;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sharry.lib.opengles.texture.ITextureRenderer;

/**
 * 相机预览器的抽象描述
 *
 * @author Sharry <a href="xiaoyu.zhu@1hai.cn">Contact me.</a>
 * @version 1.0
 * @since 2019-08-08 10:49
 */
public interface IPreviewer {

    /**
     * 设置要预览的数据源
     */
    void setDataSource(@NonNull SurfaceTexture bufferTexture);

    /**
     * 设置 previewer 的渲染器
     */
    void setRenderer(@Nullable Renderer renderer);

    /**
     * 设置旋转角度
     */
    void setRotate(int degrees);

    /**
     * 设置缩放类型
     */
    void setScaleType(ScaleType type, boolean landscape, Size dataSourceSize);

    /**
     * 获取渲染器
     */
    @NonNull
    Renderer getRenderer();

    /**
     * 获取用于预览的 view
     */
    View getView();

    /**
     * 获取渲染器的尺寸
     */
    @NonNull
    Size getSize();

    /**
     * 获取当前帧的数据
     */
    Bitmap getBitmap();

    /**
     * 获取当前的渲染环境
     */
    EGLContext getEglContext();

    /**
     * 相机预览器的 Renderer
     * <p>
     * 对 ITextureRenderer 的增强, 拓展 matrix 功能
     *
     * @author Sharry <a href="sharrychoochn@gmail.com">Contact me.</a>
     * @version 1.0
     * @since 2019-07-27
     */
    interface Renderer extends ITextureRenderer {

        /**
         * 要渲染的数据源变更了
         *
         * @param dataSource 相机输出的外部纹理
         */
        void setDataSource(SurfaceTexture dataSource);

        /**
         * 获取预览器输出的纹理 ID
         */
        int getPreviewerTextureId();

        /**
         * 设置旋转角度
         */
        void setRotate(int degrees);

        /**
         * 设置缩放类型
         */
        void setScaleType(ScaleType type, boolean landscape, Size dataSourceSize, Size viewSize);

    }

}
