package com.sharry.lib.camera;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sharry.lib.opengles.texture.GLTextureView;

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
    private Renderer mPreviewerRenderer;

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

    /**
     * 暂存 Renderer 的状态值, 方便 Renderer 切换时快速还原
     */
    private SurfaceTexture mDataSource;
    private Size mDataSourceSize = getSize();
    private int mDegree = 0;
    private ScaleType mScaleType = ScaleType.CENTER_CROP;
    private boolean mLandscape = false;

    @Override
    public void setDataSource(@NonNull SurfaceTexture dataSource) {
        if (mDataSource == dataSource) {
            Log.i(TAG, "Data source not changed.");
            return;
        }
        // set callback
        dataSource.setOnFrameAvailableListener(mFrameAvailableListener);
        // notify renderer
        Renderer renderer = mPreviewerRenderer;
        if (renderer != null) {
            renderer.setDataSource(dataSource);
        }
        // update dataSource
        mDataSource = dataSource;
    }

    @Override
    public void setRenderer(@Nullable Renderer newRenderer) {
        if (mPreviewerRenderer == newRenderer) {
            return;
        }
        if (newRenderer != null) {
            newRenderer.setDataSource(mDataSource);
            newRenderer.setRotate(mDegree);
            newRenderer.setScaleType(mScaleType, mLandscape, mDataSourceSize, getSize());
        }
        mPreviewerRenderer = newRenderer;
        super.setRenderer(newRenderer);
    }

    @Override
    public void setRotate(int degrees) {
        Renderer renderer = mPreviewerRenderer;
        if (renderer != null) {
            renderer.setRotate(degrees);
        }
        mDegree = degrees;
    }

    @Override
    public void setScaleType(ScaleType type, boolean landscape, Size dataSourceSize) {
        Renderer renderer = mPreviewerRenderer;
        if (renderer != null) {
            renderer.setScaleType(type, landscape, dataSourceSize, getSize());
        }
        mScaleType = type;
        mLandscape = landscape;
        mDataSourceSize = dataSourceSize;
    }

    @Override
    public View getView() {
        return this;
    }

    @NonNull
    @Override
    public Renderer getRenderer() {
        return mPreviewerRenderer;
    }

    @Override
    public Size getSize() {
        return new Size(getWidth(), getHeight());
    }

    @Override
    public Bitmap getBitmap() {
        Bitmap bitmap = super.getBitmap();
        Log.e(TAG, "bitmap width = " + bitmap.getWidth() + ", bitmap height = " + bitmap.getHeight());
        return bitmap;
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
