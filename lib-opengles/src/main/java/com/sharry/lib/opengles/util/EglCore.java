package com.sharry.lib.opengles.util;

import android.graphics.SurfaceTexture;
import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLExt;
import android.opengl.EGLSurface;
import android.util.Log;
import android.view.Surface;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import static android.opengl.EGL14.EGL_CONTEXT_CLIENT_VERSION;

/**
 * An EGL helper class.
 * <p>
 * The EGLContext must only be attached to one thread at a time.  This class is not thread-safe.
 * <p>
 * Get more details<href>https://github.com/google/grafika/blob/master/app/src/main/java/com/android/grafika/gles/EglCore.java</href>
 *
 * @author Sharry <a href="sharrychoochn@gmail.com">Contact me.</a>
 * @version 1.0
 * @since 2019-07-29
 */
public class EglCore {

    public static final int EGL_VERSION_2 = 2;
    public static final int EGL_VERSION_3 = 3;

    @IntDef(value = {
            EGL_VERSION_2,
            EGL_VERSION_3
    })
    private @interface EGLVersion {

    }

    private static final String TAG = EglCore.class.getSimpleName();

    private final int mEGLVersion;
    private EGLDisplay mEGLDisplay = EGL14.EGL_NO_DISPLAY;
    private EGLContext mEGLContext = EGL14.EGL_NO_CONTEXT;
    private EGLSurface mEGLSurface = EGL14.EGL_NO_SURFACE;

    public EglCore() {
        this(EGL_VERSION_2);
    }

    public EglCore(@EGLVersion int eglVersion) {
        mEGLVersion = eglVersion;
    }

    /**
     * Initialize EGL for a given configuration spec.
     *
     * @param surface    native window
     * @param eglContext if null will create new context, false will use shared context
     */
    public void initialize(@NonNull Surface surface, @Nullable EGLContext eglContext) {
        initializeInternal(surface, eglContext == null ? EGL14.EGL_NO_CONTEXT : eglContext);
    }

    /**
     * Initialize EGL for a given configuration spec.
     *
     * @param surfaceTexture native window
     * @param eglContext     if null will create new context, false will use shared context
     */
    public void initialize(@NonNull SurfaceTexture surfaceTexture, @Nullable EGLContext eglContext) {
        initializeInternal(surfaceTexture, eglContext == null ? EGL14.EGL_NO_CONTEXT : eglContext);
    }

    /**
     * Makes our EGL context current, using the supplied "draw" and "read" surfaces.
     */
    public void makeCurrent() {
        if (!EGL14.eglMakeCurrent(mEGLDisplay, mEGLSurface, mEGLSurface, mEGLContext)) {
            throw new RuntimeException("eglMakeCurrent failed");
        }
    }

    /**
     * Sends the presentation time stamp to EGL.  Time is expressed in nanoseconds.
     */
    public void setPresentationTime(long nsecs) {
        EGLExt.eglPresentationTimeANDROID(mEGLDisplay, mEGLSurface, nsecs);
    }

    /**
     * Calls eglSwapBuffers.  Use this to "publish" the current frame.
     *
     * @return false on failure
     */
    public boolean swapBuffers() {
        return EGL14.eglSwapBuffers(mEGLDisplay, mEGLSurface);
    }

    /**
     * Discards all resources held by this class, notably the EGL context.  This must be
     * called from the thread where the context was created.
     * <p>
     * On completion, no context will be current.
     */
    public void release() {
        if (mEGLDisplay != EGL14.EGL_NO_DISPLAY) {
            EGL14.eglMakeCurrent(mEGLDisplay, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE,
                    EGL14.EGL_NO_CONTEXT);
            EGL14.eglDestroyContext(mEGLDisplay, mEGLContext);
            EGL14.eglDestroySurface(mEGLDisplay, mEGLSurface);
            EGL14.eglTerminate(mEGLDisplay);
        }
        mEGLContext = EGL14.EGL_NO_CONTEXT;
        mEGLDisplay = EGL14.EGL_NO_DISPLAY;
        mEGLSurface = EGL14.EGL_NO_SURFACE;
    }

    /**
     * Gets the current EGLContext
     *
     * @return the current EGLContext
     */
    public EGLContext getContext() {
        return mEGLContext;
    }

    /**
     * Copy from {@link android.opengl.GLSurfaceView#EglHelper}
     */
    private void initializeInternal(Object nativeWindow, EGLContext sharedEglContext) {
        /*
         * Create a connection for system native window
         */
        mEGLDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);
        if (mEGLDisplay == EGL14.EGL_NO_DISPLAY) {
            throw new RuntimeException("eglGetDisplay failed");
        }

        /*
         * We can now initialize EGL for that display
         */
        int[] version = new int[2];
        if (!EGL14.eglInitialize(mEGLDisplay, version, 0, version, 1)) {
            mEGLDisplay = null;
            throw new RuntimeException("eglInitialize failed");
        }

        /*
         * Create EGLConfig
         */
        EGLConfig eglConfig = chooseConfig();
        if (eglConfig == null) {
            throw new RuntimeException("Cannot find suitable config.");
        }

        /*
         * Create EGLContext
         */
        int[] attrib_list = {EGL_CONTEXT_CLIENT_VERSION, mEGLVersion, EGL14.EGL_NONE};
        EGLContext eglContext = EGL14.eglCreateContext(mEGLDisplay, eglConfig, sharedEglContext,
                attrib_list, 0);
        if (EGL14.eglGetError() == EGL14.EGL_SUCCESS) {
            mEGLContext = eglContext;
        } else {
            throw new RuntimeException("Create EGLContext failed.");
        }

        /*
         * Create EGLSurface
         */
        int[] surfaceAttribs = {EGL14.EGL_NONE};
        mEGLSurface = EGL14.eglCreateWindowSurface(mEGLDisplay, eglConfig, nativeWindow,
                surfaceAttribs, 0);
        if (mEGLSurface == null || mEGLSurface == EGL14.EGL_NO_SURFACE) {
            throw new RuntimeException("createWindowSurface returned EGL_BAD_NATIVE_WINDOW.");
        }

        /*
         * Bind context
         */
        makeCurrent();
    }

    /**
     * Finds a suitable EGLConfig.
     */
    private EGLConfig chooseConfig() {
        int renderableType = EGL14.EGL_OPENGL_ES2_BIT;
        if (mEGLVersion >= 3) {
            renderableType |= EGLExt.EGL_OPENGL_ES3_BIT_KHR;
        }
        // The actual surface is generally RGBA or RGBX, so situationally omitting alpha
        // doesn't really help.  It can also lead to a huge performance hit on glReadPixels()
        // when reading into a GL_RGBA buffer.
        int[] attribList = {
                EGL14.EGL_RED_SIZE, 8,
                EGL14.EGL_GREEN_SIZE, 8,
                EGL14.EGL_BLUE_SIZE, 8,
                EGL14.EGL_ALPHA_SIZE, 8,
                //EGL14.EGL_DEPTH_SIZE, 16,
                //EGL14.EGL_STENCIL_SIZE, 8,
                EGL14.EGL_RENDERABLE_TYPE, renderableType,
                EGL14.EGL_NONE, 0,      // placeholder for recordable [@-3]
                EGL14.EGL_NONE
        };
        EGLConfig[] configs = new EGLConfig[1];
        int[] numConfigs = new int[1];
        if (!EGL14.eglChooseConfig(mEGLDisplay, attribList, 0, configs,
                0, configs.length,
                numConfigs, 0)) {
            Log.w(TAG, "unable to find RGB8888 / " + mEGLVersion + " EGLConfig");
            return null;
        }
        return configs[0];
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            if (mEGLDisplay != EGL14.EGL_NO_DISPLAY) {
                // We're limited here -- finalizers don't run on the thread that holds
                // the EGL state, so if a surface or context is still current on another
                // thread we can't fully release it here.  Exceptions thrown from here
                // are quietly discarded.  Complain in the log file.
                Log.w(TAG, "WARNING: EglCore was not explicitly released -- state may be leaked");
                release();
            }
        } finally {
            super.finalize();
        }
    }

}
