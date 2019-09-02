package com.sharry.lib.media.recorder;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.File;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @author Sharry <a href="sharrychoochn@gmail.com">Contact me.</a>
 * @version 1.0
 * @since 2019-07-15
 */
abstract class BaseMediaRecorder implements IMediaRecorder {

    protected static final String TAG = IMediaRecorder.class.getSimpleName();
    protected final Context mContext;
    protected final IRecorderCallback mCallback;
    protected volatile boolean isRecording = false;
    protected File mOutputFile;

    BaseMediaRecorder(Context context, final IRecorderCallback callback) {
        this.mContext = context;
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

    /**
     * 执行录制文件的删除
     */
    void performRecordFileDelete() {
        if (mOutputFile != null && mOutputFile.exists()) {
            if (mOutputFile.delete()) {
                Log.i(TAG, "Record file deleted.");
            } else {
                Log.i(TAG, "Record file delete failed.");
            }
            FileUtil.notifyFileDeleted(mContext, mOutputFile.getAbsolutePath());
        }
        mOutputFile = null;
    }

    @Override
    protected void finalize() throws Throwable {
        cancel();
        super.finalize();
    }

}
