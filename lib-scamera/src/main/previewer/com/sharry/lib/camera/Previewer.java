package com.sharry.lib.camera;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.opengl.EGLContext;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import com.sharry.lib.opengles.GLTextureView;

/**
 * Camera 预览器
 * <p>
 * 使用 TextureView 渲染硬件相机输出的 SurfaceTexture
 *
 * @author Sharry <a href="sharrychoochn@gmail.com">Contact me.</a>
 * @version 1.0
 * @since 2019-04-24
 */
@SuppressLint("ViewConstructor")
public final class Previewer extends GLTextureView implements IPreviewer, SurfaceTexture.OnFrameAvailableListener {

    private static final String TAG = Previewer.class.getSimpleName();

    private SurfaceTexture mDataSource;
    private Renderer mRenderer;

    Previewer(Context context, FrameLayout parent) {
        super(context);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        params.gravity = Gravity.CENTER;
        parent.addView(this, params);
        // set default renderer
        setRenderer(new DefaultPreviewerRenderer(context));
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        requestRenderer();
    }

    @Override
    public void setDataSource(@NonNull SurfaceTexture dataSource) {
        if (mDataSource == dataSource) {
            Log.i(TAG, "Data source not changed.");
            return;
        }
        // update dataSource
        mDataSource = dataSource;
        // set callback
        mDataSource.setOnFrameAvailableListener(this);
        // notify renderer
        mRenderer.onDataSourceChanged(dataSource);
    }

    @Override
    public void setRenderer(@NonNull Renderer renderer) {
        Renderer before = mRenderer;
        // Copy transform matrix from before.
        if (before != null) {
            renderer.setMatrix(before.getMatrix());
        }
        // update renderer.
        mRenderer = renderer;
        mRenderer.onDataSourceChanged(mDataSource);
        super.setRenderer(mRenderer);
    }

    @Override
    public View getView() {
        return this;
    }

    @NonNull
    @Override
    public Renderer getRenderer() {
        return mRenderer;
    }

    @Override
    public Size getSize() {
        return new Size(getWidth(), getHeight());
    }

    @Override
    public Bitmap getBitmap() {
        return super.getBitmap();
    }

    @Override
    public EGLContext getEGLContext() {
        return getEglContext();
    }

}
