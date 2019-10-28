package com.sharry.lib.media.recorder;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.util.Log;

import androidx.annotation.WorkerThread;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * 音频信息录制者
 *
 * @author Sharry <a href="xiaoyu.zhu@1hai.cn">Contact me.</a>
 * @version 1.0
 * @since 2019-07-15 17:36
 */
final class AudioRecorder extends BaseMediaRecorder implements IAudioEncoder.Callback, IPCMProvider.OnPCMChangedListener {

    /**
     * Inner Constants.
     */
    private static final String FILE_PREFIX = "audio_";

    /**
     * Fields.
     */
    private final Options.Audio mOptions;
    private final IPCMProvider mProvider;
    private final IAudioEncoder mEncoder;
    private final IAudioEncoder.Context mEncodeContext;
    private final File mOutputFile;
    private IAudioEncoder.Callback mEncodeCallback;

    AudioRecorder(Context context, Options.Audio options, IRecorderCallback callback) {
        super(context, callback);
        this.mOptions = options;
        // 创建 PCM 数据提供者
        this.mProvider = options.getPcmProvider() == null ? new OpenSLESPCMProvider()
                : options.getPcmProvider();
        mProvider.setOnPCMChangedListener(this);
        // 创建编码上下文
        try {
            if (!options.isJustEncode()) {
                mOutputFile = FileUtil.createFile(context, mOptions.getOutputDir(), FILE_PREFIX,
                        mOptions.getAudioEncodeType().getFileSuffix());
            } else {
                mOutputFile = null;
            }
            mEncodeContext = new IAudioEncoder.Context(
                    options.getSampleRate(),
                    options.getChannelLayout(),
                    options.getPerSampleSize(),
                    options.isJustEncode(),
                    mOutputFile == null ? null : mOutputFile.getAbsolutePath(),
                    this
            );
        } catch (IOException e) {
            throw new UnsupportedOperationException("Please ensure file can create correct.");
        }
        // 创建编码实现者
        this.mEncoder = EncoderFactory.create(mOptions.getAudioEncodeType());
    }

    // //////////////////////////////////// PCM 数据源回调 ////////////////////////////////////

    @Override
    @WorkerThread
    public void OnPCMChanged(byte[] pcmData) {
        try {
            mEncoder.encode(pcmData);
        } catch (Throwable e) {
            performRecordFailed(IRecorderCallback.ERROR_ENCODE_FAILED, e);
        }
    }

    // //////////////////////////////////// 音频编码回调 ////////////////////////////////////

    @Override
    @WorkerThread
    public void onAudioFormatChanged(MediaFormat outputFormat) {
        if (mEncodeCallback != null) {
            mEncodeCallback.onAudioFormatChanged(outputFormat);
        }
    }

    @Override
    @WorkerThread
    @SuppressLint("WrongThread")
    public void onAudioEncoded(ByteBuffer byteBuffer, MediaCodec.BufferInfo bufferInfo) {
        // 回调进度
        long recordTime = bufferInfo.presentationTimeUs / 1000;
        mCallback.onProgress(recordTime);
        // 回调数据
        if (mEncodeCallback != null) {
            mEncodeCallback.onAudioEncoded(byteBuffer, bufferInfo);
        }
        // 超过了录制时长, 自动完成
        if (recordTime >= mOptions.getDuration()) {
            complete();
        }
    }

    // //////////////////////////////////// Recorder 生命周期 ////////////////////////////////////

    @Override
    public void start() {
        if (isRecording) {
            Log.i(TAG, "Is already start.");
            return;
        }
        isRecording = true;
        AVPoolExecutor.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                // 准备编码器
                try {
                    mEncoder.prepare(mEncodeContext);
                } catch (Throwable e) {
                    performRecordFailed(IRecorderCallback.ERROR_ENCODER_PREPARE_FAILED, e);
                }
                // 开始录制
                try {
                    mProvider.start();
                } catch (Throwable throwable) {
                    performRecordFailed(IRecorderCallback.ERROR_START_FAILED, throwable);
                }
            }
        });
    }

    @Override
    public void pause() {
        if (!isRecording) {
            Log.i(TAG, "Not recording.");
            return;
        }
        mProvider.pause();
        mCallback.onPause();
    }

    @Override
    public void resume() {
        if (!isRecording) {
            Log.i(TAG, "Not recording.");
            return;
        }
        mProvider.resume();
        mCallback.onResume();
    }

    @Override
    public void cancel() {
        if (!isRecording) {
            Log.i(TAG, "Not recording.");
            return;
        }
        AVPoolExecutor.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                // 停止录制
                stop();
                // 删除文件
                deleteRecordFile();
                // 回调录制取消
                mCallback.onCancel();
            }
        });
    }

    @Override
    public void complete() {
        if (!isRecording) {
            return;
        }
        AVPoolExecutor.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                // 停止录制
                stop();
                // 回调录制完成
                if (mOutputFile != null) {
                    // 在文件管理器中刷新生成的文件
                    FileUtil.notifyMediaStore(mContext, mOutputFile.getAbsolutePath());
                    // 回调录制完成
                    mCallback.onComplete(mOutputFile);
                }
            }
        });
    }

    @Override
    protected void stop() {
        if (isRecording) {
            mProvider.stop();
            mEncoder.stop();
            isRecording = false;
        }
    }

    void setEncodeCallback(IAudioEncoder.Callback callback) {
        this.mEncodeCallback = callback;
    }

}
