package com.sharry.lib.opengles.surface;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;

import static android.opengl.EGL14.EGL_CONTEXT_CLIENT_VERSION;

public class ContextSharedGLSurfaceView extends GLSurfaceView {

    private EGLContext mEGLContext;

    public ContextSharedGLSurfaceView(Context context) {
        this(context, null);
    }

    public ContextSharedGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setEGLContextClientVersion(2);
        // 利用 setEGLContextFactory 这种方式共享其他 GLSurfaceView 的 EGLContext
        setEGLContextFactory(new EGLContextFactory() {
            @Override
            public EGLContext createContext(EGL10 egl, EGLDisplay display, EGLConfig eglConfig) {
                int[] attrib_list = {EGL_CONTEXT_CLIENT_VERSION, 2, EGL10.EGL_NONE};
                if (mEGLContext != null) {
                    mEGLContext = egl.eglCreateContext(display, eglConfig, mEGLContext, attrib_list);
                } else {
                    mEGLContext = egl.eglCreateContext(display, eglConfig, EGL10.EGL_NO_CONTEXT, attrib_list);
                }
                return mEGLContext;
            }

            @Override
            public void destroyContext(EGL10 egl, EGLDisplay display, EGLContext context) {
                // ignore.
            }
        });
    }

    public void setEGLContext(EGLContext eglContext) {
        this.mEGLContext = eglContext;
    }

    public EGLContext getEGLContext() {
        return mEGLContext;
    }

}
