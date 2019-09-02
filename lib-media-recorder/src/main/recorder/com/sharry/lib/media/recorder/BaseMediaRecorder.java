package com.sharry.lib.media.recorder;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @author Sharry <a href="sharrychoochn@gmail.com">Contact me.</a>
 * @version 1.0
 * @since 2019-07-15
 */
abstract class BaseMediaRecorder implements IMediaRecorder {

    static final String TAG = IMediaRecorder.class.getSimpleName();
    final IRecorderCallback mCallback;
    volatile boolean isRecording = false;

    BaseMediaRecorder(final IRecorderCallback callback) {
        this.mCallback = (IRecorderCallback) Proxy.newProxyInstance(
                this.getClass().getClassLoader(),
                new Class[]{IRecorderCallback.class},
                new InvocationHandler() {

                    final Handler mainThreadHandler = new Handler(Looper.getMainLooper());

                    @Override
                    public Object invoke(Object proxy, final Method method, final Object[] args) {
                        // Hook 方法让其在主线程回调
                        if (callback == null) {
                            return null;
                        }
                        mainThreadHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    method.invoke(callback, args);
                                } catch (Throwable e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                        return null;
                    }
                }
        );
    }

    void handleRecordFailed(@IRecorderCallback.ErrorCode final int errorCode, @NonNull final Throwable e) {
        Log.e(TAG, "Record failed, errorCode is: " + errorCode, e);
        // 释放资源
        cancel();
        // 回调录制失败
        mCallback.onFailed(errorCode, e);
    }

    @Override
    protected void finalize() throws Throwable {
        cancel();
        super.finalize();
    }

}
