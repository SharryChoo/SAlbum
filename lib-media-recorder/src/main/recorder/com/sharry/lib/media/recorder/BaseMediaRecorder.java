package com.sharry.lib.media.recorder;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

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
    protected Uri mOutputUri;

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

    /**
     * 处理录制失败的情况
     *
     * @param errorCode 错误码
     * @param e         异常
     */
    @WorkerThread
    @SuppressLint("WrongThread")
    void performRecordFailed(@IRecorderCallback.ErrorCode final int errorCode, @NonNull final Throwable e) {
        Log.i(TAG, "Record failed, errorCode is: " + errorCode, e);
        // 释放资源
        stop();
        // 删除文件
        deleteRecordFile();
        // 回调录制失败
        mCallback.onFailed(errorCode, e);
    }

    /**
     * 执行录制文件的删除
     */
    void deleteRecordFile() {
        if (mOutputUri != null) {
            mContext.getContentResolver().delete(mOutputUri, null, null);
        }
        mOutputUri = null;
    }

    @Override
    protected void finalize() throws Throwable {
        stop();
        super.finalize();
    }

    protected abstract void stop();

}
