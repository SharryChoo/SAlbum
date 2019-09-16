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

import java.lang.ref.WeakReference;

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
public final class Previewer extends GLTextureView implements IPreviewer {

    private static final String TAG = Previewer.class.getSimpleName();

    private final SurfaceTexture.OnFrameAvailableListener mFrameAvailableListener;
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
        // create frame available listener
        this.mFrameAvailableListener = new FrameAvailableListenerImpl(this);
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
        mDataSource.setOnFrameAvailableListener(mFrameAvailableListener);
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
        Bitmap bitmap = super.getBitmap();
        Log.e("TAG", "bitmap width = " + bitmap.getWidth() + ", bitmap height = " + bitmap.getHeight());
        return bitmap;
    }

    @Override
    public EGLContext getEGLContext() {
        return getEglContext();
    }

    private static class FrameAvailableListenerImpl extends WeakReference<Previewer>
            implements SurfaceTexture.OnFrameAvailableListener {

        private FrameAvailableListenerImpl(Previewer referent) {
            super(referent);
        }

        @Override
        public void onFrameAvailable(SurfaceTexture surfaceTexture) {
            Previewer previewer = get();
            if (previewer != null) {
                previewer.requestRenderer();
            }
        }
    }

}
