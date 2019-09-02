package com.sharry.lib.opengles;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.EGLContext;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.AttributeSet;
import android.view.TextureView;

import androidx.annotation.NonNull;

import java.lang.ref.WeakReference;

/**
 * 利用 TextureView 实现对外来 SurfaceTexture 的加工绘制
 *
 * @author Sharry <a href="sharrychoochn@gmail.com">Contact me.</a>
 * @version 1.0
 * @since 2019-07-27
 */
public class GLTextureView extends TextureView {

    protected ITextureRenderer mRenderer;
    protected RendererThread mRendererThread;

    public GLTextureView(Context context) {
        this(context, null);
    }

    public GLTextureView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GLTextureView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setSurfaceTextureListener(new SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                if (mRendererThread != null) {
                    mRendererThread.handleSurfaceTextureChanged();
                    return;
                }
                // do launch
                mRendererThread = new RendererThread(RendererThread.class.getSimpleName(),
                        new WeakReference<>(GLTextureView.this));
                mRendererThread.start();
                // invoke renderer lifecycle sequence.
                if (mRenderer != null) {
                    mRendererThread.handleRendererChanged();
                }
                mRendererThread.handleSurfaceSizeChanged();
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
                if (mRendererThread != null) {
                    mRendererThread.handleSurfaceSizeChanged();
                }
            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                if (mRendererThread != null) {
                    mRendererThread.quitSafely();
                    mRendererThread = null;
                }
                return true;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {
                // nothing.
            }

        });
    }

    /**
     * Set a renderer.
     */
    public void setRenderer(@NonNull ITextureRenderer renderer) {
        if (mRenderer == renderer) {
            return;
        }
        mRenderer = renderer;
        if (mRendererThread != null) {
            mRendererThread.handleRendererChanged();
        }
    }

    /**
     * request renderer.
     */
    public void requestRenderer() {
        if (mRendererThread != null) {
            mRendererThread.handleDrawFrame();
        }
    }

    /**
     * Gets a EGLContext
     *
     * @return return a instance of EGLContext. if mRendererThread not start, will be null.
     */
    public EGLContext getEglContext() {
        EGLContext res = null;
        if (mRendererThread != null) {
            res = mRendererThread.mEglCore.getContext();
        }
        return res;
    }

    static class RendererThread extends HandlerThread
            implements SurfaceTexture.OnFrameAvailableListener, Handler.Callback {

        private static final int MSG_SURFACE_TEXTURE_CHANGED = 0;
        private static final int MSG_RENDERER_CHANGED = 1;
        private static final int MSG_SURFACE_SIZE_CHANGED = 2;
        private static final int MSG_DRAW_FRAME = 3;

        private final WeakReference<GLTextureView> mWkRef;
        private final EglCore mEglCore = new EglCore();
        private Handler mRendererHandler;

        private RendererThread(String name, WeakReference<GLTextureView> view) {
            super(name);
            mWkRef = view;
        }

        @Override
        public synchronized void start() {
            super.start();
            mRendererHandler = new Handler(getLooper(), this);
            mRendererHandler.sendEmptyMessage(MSG_SURFACE_TEXTURE_CHANGED);
        }

        @Override
        public boolean quitSafely() {
            release();
            return super.quitSafely();
        }

        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                // 画布变更
                case MSG_SURFACE_TEXTURE_CHANGED:
                    preformSurfaceTextureChanged();
                    break;
                // 渲染器变更
                case MSG_RENDERER_CHANGED:
                    performRendererChanged();
                    break;
                // 画布尺寸变更
                case MSG_SURFACE_SIZE_CHANGED:
                    performSurfaceSizeChanged();
                    break;
                // 绘制数据帧
                case MSG_DRAW_FRAME:
                    performDrawFrame();
                    break;
                default:
                    break;
            }
            return false;
        }

        @Override
        public void onFrameAvailable(SurfaceTexture surfaceTexture) {
            if (mRendererHandler != null) {
                mRendererHandler.sendEmptyMessage(MSG_DRAW_FRAME);
            }
        }

        /**
         * {@link #preformSurfaceTextureChanged}
         */
        void handleSurfaceTextureChanged() {
            release();
            if (mRendererHandler != null) {
                mRendererHandler.sendEmptyMessage(MSG_SURFACE_TEXTURE_CHANGED);
            }
        }

        /**
         * {@link #performRendererChanged}
         */
        void handleRendererChanged() {
            if (mRendererHandler != null) {
                mRendererHandler.sendEmptyMessage(MSG_RENDERER_CHANGED);
            }
        }

        /**
         * {@link #performSurfaceSizeChanged}
         */
        void handleSurfaceSizeChanged() {
            if (mRendererHandler != null) {
                mRendererHandler.sendEmptyMessage(MSG_SURFACE_SIZE_CHANGED);
            }
        }

        /**
         * {@link #performSurfaceSizeChanged}
         */
        void handleDrawFrame() {
            if (mRendererHandler != null) {
                mRendererHandler.sendEmptyMessage(MSG_DRAW_FRAME);
            }
        }

        private void preformSurfaceTextureChanged() {
            GLTextureView view = mWkRef.get();
            if (view == null) {
                return;
            }
            // Create egl context
            mEglCore.initialize(view.getSurfaceTexture(), null);
            // invoke render lifecycle
            ITextureRenderer renderer = view.mRenderer;
            if (renderer != null) {
                renderer.onEGLContextCreated();
                renderer.onSurfaceChanged(view.getWidth(), view.getHeight());
            }
        }

        private void performRendererChanged() {
            GLTextureView view = mWkRef.get();
            if (view == null) {
                return;
            }
            mRendererHandler.removeMessages(MSG_DRAW_FRAME);
            ITextureRenderer renderer = view.mRenderer;
            if (renderer != null) {
                renderer.onEGLContextCreated();
                renderer.onSurfaceChanged(view.getWidth(), view.getHeight());
            }
        }

        private void performSurfaceSizeChanged() {
            GLTextureView view = mWkRef.get();
            if (view == null) {
                return;
            }
            ITextureRenderer renderer = view.mRenderer;
            if (renderer != null) {
                renderer.onSurfaceChanged(view.getWidth(), view.getHeight());
            }
        }

        private void performDrawFrame() {
            GLTextureView view = mWkRef.get();
            if (view == null) {
                return;
            }
            // 更新纹理数据
            ITextureRenderer renderer = view.mRenderer;
            // 执行渲染器的绘制
            if (renderer != null) {
                renderer.onDrawFrame();
            }
            // 将 EGL 绘制的数据, 输出到 View 的 preview 中
            mEglCore.swapBuffers();
        }

        private void release() {
            if (mRendererHandler != null) {
                mRendererHandler.removeMessages(MSG_SURFACE_TEXTURE_CHANGED);
                mRendererHandler.removeMessages(MSG_SURFACE_SIZE_CHANGED);
                mRendererHandler.removeMessages(MSG_DRAW_FRAME);
            }
            mEglCore.release();
        }
    }

}
