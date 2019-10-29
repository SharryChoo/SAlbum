package com.sharry.lib.media.recorder;

import android.Manifest;
import android.content.Context;
import android.media.AudioManager;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;

import com.sharry.lib.camera.SCameraView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Sharry <a href="xiaoyu.zhu@1hai.cn">Contact me.</a>
 * @version 1.0
 * @since 2019-07-15 17:24
 */
public final class SMediaRecorder implements IRecorderCallback {

    private static final String TAG = SMediaRecorder.class.getSimpleName();

    /**
     * Get instance from here.
     */
    public static SMediaRecorder with(@NonNull Context context) {
        return new SMediaRecorder(context);
    }

    /**
     * Constants.
     */
    private final List<IRecorderCallback> mCallbacks = new ArrayList<>();

    /**
     * Fields, init after method init() invoked.
     */
    private final Context mContext;
    private AudioManager mAudioManagerService;

    private SMediaRecorder(Context context) {
        mContext = context.getApplicationContext();
        mAudioManagerService = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
    }

    @Override
    public void onStart() {
        // 获取焦点
        mAudioManagerService.requestAudioFocus(null, AudioManager.STREAM_VOICE_CALL,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
        for (IRecorderCallback callback : mCallbacks) {
            callback.onStart();
        }
    }

    @Override
    public void onProgress(long time) {
        for (IRecorderCallback callback : mCallbacks) {
            callback.onProgress(time);
        }
    }

    @Override
    public void onPause() {
        for (IRecorderCallback callback : mCallbacks) {
            callback.onPause();
        }
    }

    @Override
    public void onResume() {
        for (IRecorderCallback callback : mCallbacks) {
            callback.onResume();
        }
    }

    @Override
    public void onCancel() {
        // 释放焦点
        mAudioManagerService.abandonAudioFocus(null);
        for (IRecorderCallback callback : mCallbacks) {
            callback.onCancel();
        }
    }

    @Override
    public void onComplete(@NonNull final Uri uri, File file) {
        // 释放焦点
        mAudioManagerService.abandonAudioFocus(null);
        for (IRecorderCallback callback : mCallbacks) {
            callback.onComplete(uri, file);
        }
    }

    @Override
    public void onFailed(@ErrorCode final int errorCode, @NonNull final Throwable e) {
        Log.w(TAG, "SMediaRecorder record failed", e);
        // 释放焦点
        mAudioManagerService.abandonAudioFocus(null);
        for (IRecorderCallback callback : mCallbacks) {
            callback.onFailed(errorCode, e);
        }
    }

    private IMediaRecorder mImpl;

    /**
     * 开始录制视频
     */
    @RequiresPermission(anyOf = {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    })
    public void start(SCameraView cameraView, @NonNull Options.Video options) {
        // 完善 Config
        completionOptions(options);
        // 取消之前的录制动作
        cancel();
        // 创建录制者
        mImpl = new VideoRecorder(mContext, options, cameraView, this);
        // 启动录制者
        mImpl.start();
    }

    /**
     * 开始采集音频
     * Subsequent calls to {@link #cancel} after start success.
     * Subsequent calls to {@link #complete} after start success.
     * <p>
     * If start success, it will be call method that {@link IRecorderCallback#onStart()}
     * If start failed, it will be call method that {@link IRecorderCallback#onFailed(int, Throwable)}
     * </p>
     *
     * @param options the options associated with this record.
     */
    @RequiresPermission(allOf = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    })
    public void start(@NonNull Options.Audio options) {
        // 完善 Config
        completionOptions(options);
        // 取消之前的录制动作
        cancel();
        // 创建录制者
        mImpl = new AudioRecorder(mContext, options, this);
        // 开始录制
        mImpl.start();
    }

    /**
     * 暂停录制
     */
    public void pause() {
        if (mImpl != null) {
            mImpl.pause();
        }
    }

    /**
     * 恢复录制
     */
    public void resume() {
        if (mImpl != null) {
            mImpl.resume();
        }
    }

    /**
     * 取消本次的音频采集
     * <p>
     * If cancel success, it will be call method that {@link IRecorderCallback#onCancel()}
     * If cancel failed, it will be call method that {@link IRecorderCallback#onFailed(int, Throwable)}
     * </p>
     */
    public void cancel() {
        if (mImpl != null) {
            mImpl.cancel();
            mImpl = null;
        }
    }

    /**
     * 完成音频采集
     * <p>
     * If complete success, it will be call method that {@link IRecorderCallback#onComplete(Uri, File)}
     * If complete failed, it will be call method that {@link IRecorderCallback#onFailed(int, Throwable)}
     * </p>
     */
    public void complete() {
        if (mImpl != null) {
            mImpl.complete();
            mImpl = null;
        }
    }

    /**
     * 注册音频录制回调
     * <p>
     * when use completed to avoid memory leak.
     */
    public void addRecordCallback(@NonNull IRecorderCallback callback) {
        int index = mCallbacks.indexOf(callback);
        if (index >= 0) {
            Log.i(TAG, "This callback already registered.");
        } else {
            mCallbacks.add(callback);
        }
    }

    /**
     * Verify the options legality.
     */
    private void completionOptions(@NonNull Options.Audio audioOptions) {
        // 若没有设置录音文件输出目录, 则指定为 App 的内部缓存目录
        if (TextUtils.isEmpty(audioOptions.getRelativePath())) {
            audioOptions.reBuilder().setRelativePath(mContext.getCacheDir().getAbsolutePath());
        }
    }

    /**
     * Verify the options legality.
     */
    private void completionOptions(@NonNull Options.Video videoOptions) {
        // 若没有设置录音文件输出目录, 则指定为 App 的内部缓存目录
        if (TextUtils.isEmpty(videoOptions.getRelativePath())) {
            videoOptions.reBuilder().setRelativePath(mContext.getCacheDir().getAbsolutePath());
        }
    }

}
