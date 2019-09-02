package com.sharry.lib.media.recorder;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.util.Log;

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
    private final AudioOptions mOptions;
    private final IPCMProvider mProvider;
    private final IAudioEncoder mEncoder;
    private final IAudioEncoder.Context mEncodeContext;
    private final File mOutputFile;
    private IAudioEncoder.Callback mEncodeCallback;

    AudioRecorder(AudioOptions options, IRecorderCallback callback) {
        super(callback);
        this.mOptions = options;
        // 创建 PCM 数据提供者
        this.mProvider = options.getPcmProvider();
        mProvider.setOnPCMChangedListener(this);
        // 创建编码上下文
        try {
            if (!options.isJustEncode()) {
                mOutputFile = FileUtil.createFile(mOptions.getOutputDir(), FILE_PREFIX,
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
    public void OnPCMChanged(byte[] pcmData) {
        try {
            mEncoder.encode(pcmData);
        } catch (IOException e) {
            handleRecordFailed(IRecorderCallback.ERROR_ENCODE_FAILED, e);
        }
    }

    // //////////////////////////////////// 音频编码回调 ////////////////////////////////////

    @Override
    public void onAudioFormatChanged(MediaFormat outputFormat) {
        if (mEncodeCallback != null) {
            mEncodeCallback.onAudioFormatChanged(outputFormat);
        }
    }

    @Override
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
            Log.e(TAG, "Is already start.");
            return;
        }
        isRecording = true;
        // 准备编码器
        try {
            mEncoder.prepare(mEncodeContext);
        } catch (IOException e) {
            handleRecordFailed(IRecorderCallback.ERROR_ENCODER_PREPARE_FAILED, e);
        }
        // 2. 开始录制
        try {
            mProvider.start();
        } catch (Throwable throwable) {
            handleRecordFailed(IRecorderCallback.ERROR_START_FAILED, throwable);
        }
    }

    @Override
    public void pause() {
        if (!isRecording) {
            Log.e(TAG, "Not recording.");
            return;
        }
        mProvider.pause();
        mCallback.onPause();
    }

    @Override
    public void resume() {
        if (!isRecording) {
            Log.e(TAG, "Not recording.");
            return;
        }
        mProvider.resume();
        mCallback.onResume();
    }

    @Override
    public void cancel() {
        if (!isRecording) {
            Log.e(TAG, "Not recording.");
            return;
        }
        // 停止录制
        mProvider.stop();
        // 删除文件
        performRecordFileDelete();
        mCallback.onCancel();
        // 释放资源
        release();
    }

    @Override
    public void complete() {
        if (!isRecording) {
            Log.e(TAG, "Not recording.");
            return;
        }
        // 停止录制
        mProvider.stop();
        // 回调录制完成
        mCallback.onComplete(mOutputFile);
        // 释放资源
        release();
    }

    public void setEncodeCallback(IAudioEncoder.Callback callback) {
        this.mEncodeCallback = callback;
    }

    private void release() {
        try {
            // 释放资源
            mProvider.stop();
            mEncoder.release();
        } catch (IllegalStateException e) {
            // ignore.
        }
        isRecording = false;
    }

    /**
     * 执行录制文件的删除
     */
    private void performRecordFileDelete() {
        if (mOutputFile != null && mOutputFile.exists()) {
            if (mOutputFile.delete()) {
                Log.i(TAG, "Record file deleted.");
            } else {
                Log.i(TAG, "Record file delete failed.");
            }
        }
    }

}
