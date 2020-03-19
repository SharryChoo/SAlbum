package com.sharry.lib.opengles.texture;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.EGLContext;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.AttributeSet;
import android.view.TextureView;

import androidx.annotation.Nullable;

import com.sharry.lib.opengles.util.EglCore;

import java.lang.ref.WeakReference;

/**
 * 利用 TextureView 实现对外来 SurfaceTexture 的加工绘制
 *
 * @author Sharry <a href="sharrychoochn@gmail.com">Contact me.</a>
 * @version 2.0
 * @since 2019-07-27
 */
public class GLTextureView extends TextureView {

    private volatile ITextureRenderer mRenderer;
    private volatile RenderWorker mRenderWorker;

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
                RenderWorker renderWorker = mRenderWorker;
                if (renderWorker == null) {
                    // do launch
                    renderWorker = new RenderWorker(GLTextureView.this);
                    renderWorker.start();
                    mRenderWorker = renderWorker;
                }
                renderWorker.handleSurfaceTextureChanged(surface);
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
                RenderWorker renderWorker = mRenderWorker;
                if (renderWorker != null) {
                    renderWorker.handleSurfaceTextureSizeChanged();
                }
            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                RenderWorker renderWorker = mRenderWorker;
                if (renderWorker != null) {
                    renderWorker.quitSafely();
                }
                mRenderWorker = null;
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
    public void setRenderer(@Nullable ITextureRenderer renderer) {
        if (mRenderer == renderer) {
            return;
        }
        ITextureRenderer oldRenderer = mRenderer;
        mRenderer = renderer;
        RenderWorker renderWorker = mRenderWorker;
        if (renderWorker != null) {
            renderWorker.handleRendererChanged(oldRenderer);
        }
    }

    /**
     * request renderer.
     */
    public void requestRenderer() {
        RenderWorker renderWorker = mRenderWorker;
        if (renderWorker != null) {
            renderWorker.handleDrawFrame();
        }
    }

    /**
     * Gets a EGLContext
     *
     * @return return a instance of EGLContext. if mRendererThread not start, will be null.
     */
    public EGLContext getEglContext() {
        EGLContext res = null;
        RenderWorker renderWorker = mRenderWorker;
        if (renderWorker != null) {
            res = renderWorker.mEglCore.getContext();
        }
        return res;
    }

    static class RenderWorker extends HandlerThread implements Handler.Callback,
            SurfaceTexture.OnFrameAvailableListener {

        private static final int MSG_SURFACE_TEXTURE_CHANGED = 0;
        private static final int MSG_RENDERER_CHANGED = 1;
        private static final int MSG_SURFACE_SIZE_CHANGED = 2;
        private static final int MSG_DRAW_FRAME = 3;
        private static final int MSG_DESTROY = 4;

        private final WeakReference<GLTextureView> mWkRef;
        private final EglCore mEglCore = new EglCore();
        private Handler mHandler = null;

        private RenderWorker(GLTextureView view) {
            super(RenderWorker.class.getSimpleName());
            mWkRef = new WeakReference<>(view);
        }

        ////////////////////////////////////////////////////////////////////////////
        // Lifecycle
        ////////////////////////////////////////////////////////////////////////////

        @Override
        public synchronized void start() {
            super.start();
            // 实例化 Handler
            mHandler = new Handler(getLooper(), this);
        }

        @Override
        public boolean quit() {
            return quitSafely();
        }

        @Override
        public boolean quitSafely() {
            handleDestroy();
            boolean res = super.quitSafely();
            mHandler = null;
            return res;
        }

        ////////////////////////////////////////////////////////////////////////////
        // Handler.Callback
        ////////////////////////////////////////////////////////////////////////////

        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                // 画布变更
                case MSG_SURFACE_TEXTURE_CHANGED:
                    if (msg.obj instanceof SurfaceTexture) {
                        preformSurfaceTextureChanged((SurfaceTexture) msg.obj);
                    }
                    break;
                // 渲染器变更
                case MSG_RENDERER_CHANGED:
                    if (msg.obj instanceof ITextureRenderer) {
                        performRendererChanged((ITextureRenderer) msg.obj);
                    }
                    break;
                // 画布尺寸变更
                case MSG_SURFACE_SIZE_CHANGED:
                    performSurfaceSizeChanged();
                    break;
                // 绘制数据帧
                case MSG_DRAW_FRAME:
                    performDrawFrame();
                    break;
                // 处理线程退出
                case MSG_DESTROY:
                    performDestroy();
                    break;
                default:
                    break;
            }
            return false;
        }

        ////////////////////////////////////////////////////////////////////////////
        // SurfaceTexture.OnFrameAvailableListener
        ////////////////////////////////////////////////////////////////////////////

        @Override
        public void onFrameAvailable(SurfaceTexture surfaceTexture) {
            Handler handler = mHandler;
            if (handler != null) {
                handler.sendEmptyMessage(MSG_DRAW_FRAME);
            }
        }

        ////////////////////////////////////////////////////////////////////////////
        // Open method invoke at UI Thread
        ////////////////////////////////////////////////////////////////////////////

        /**
         * {@link #preformSurfaceTextureChanged}
         */
        void handleSurfaceTextureChanged(SurfaceTexture surface) {
            Handler handler = mHandler;
            if (handler != null) {
                Message msg = Message.obtain();
                msg.what = MSG_SURFACE_TEXTURE_CHANGED;
                msg.obj = surface;
                handler.sendMessage(msg);
            }
        }

        /**
         * {@link #performRendererChanged}
         */
        void handleRendererChanged(ITextureRenderer oldRenderer) {
            Handler handler = mHandler;
            if (handler != null) {
                Message msg = Message.obtain();
                msg.what = MSG_RENDERER_CHANGED;
                msg.obj = oldRenderer;
                handler.sendMessage(msg);
            }
        }

        /**
         * {@link #performSurfaceSizeChanged}
         */
        void handleSurfaceTextureSizeChanged() {
            Handler handler = mHandler;
            if (handler != null) {
                handler.sendEmptyMessage(MSG_SURFACE_SIZE_CHANGED);
            }
        }

        /**
         * {@link #performSurfaceSizeChanged}
         */
        void handleDrawFrame() {
            Handler handler = mHandler;
            if (handler != null) {
                handler.sendEmptyMessage(MSG_DRAW_FRAME);
            }
        }

        /**
         * {@link #performDestroy()}
         */
        void handleDestroy() {
            Handler handler = mHandler;
            if (handler != null) {
                handler.sendEmptyMessage(MSG_DESTROY);
            }
        }

        ////////////////////////////////////////////////////////////////////////////
        // Private method invoke at Handler Thread
        ////////////////////////////////////////////////////////////////////////////

        private void preformSurfaceTextureChanged(SurfaceTexture surfaceTexture) {
            // 释放之前的 EGL 环境
            performDestroy();
            // 重新初始化 EGL 环境
            GLTextureView view = mWkRef.get();
            if (view == null) {
                return;
            }
            // Recreate egl context
            mEglCore.initialize(surfaceTexture, null);
            // invoke render lifecycle
            ITextureRenderer renderer = view.mRenderer;
            if (renderer != null) {
                renderer.onAttach();
                renderer.onSizeChanged(view.getWidth(), view.getHeight());
            }
        }

        private void performRendererChanged(ITextureRenderer oldRenderer) {
            // 移除所有的绘制动作
            Handler handler = mHandler;
            if (handler != null) {
                handler.removeMessages(MSG_DRAW_FRAME);
            }
            GLTextureView view = mWkRef.get();
            if (view == null) {
                return;
            }
            // 回调之前 Renderer 的解绑方法
            if (oldRenderer != null) {
                oldRenderer.onDetach();
            }
            // 重新回调生命周期
            ITextureRenderer renderer = view.mRenderer;
            if (renderer != null) {
                renderer.onAttach();
                renderer.onSizeChanged(view.getWidth(), view.getHeight());
            }
        }

        private void performSurfaceSizeChanged() {
            GLTextureView view = mWkRef.get();
            if (view == null) {
                return;
            }
            ITextureRenderer renderer = view.mRenderer;
            if (renderer != null) {
                renderer.onSizeChanged(view.getWidth(), view.getHeight());
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
                renderer.onDraw();
            }
            // 将 EGL 绘制的数据, 输出到 View 的 preview 中
            mEglCore.swapBuffers();
        }

        private void performDestroy() {
            // 移除所有待执行的消息
            Handler handler = mHandler;
            if (handler != null) {
                handler.removeMessages(MSG_SURFACE_TEXTURE_CHANGED);
                handler.removeMessages(MSG_SURFACE_SIZE_CHANGED);
                handler.removeMessages(MSG_DRAW_FRAME);
            }
            // 回调 Renderer 的解绑方法
            GLTextureView view = mWkRef.get();
            if (view != null) {
                ITextureRenderer renderer = view.mRenderer;
                if (renderer != null) {
                    renderer.onDetach();
                }
            }
            // 释放该线程的 EGL 环境
            mEglCore.release();
        }
    }

}
