package com.sharry.lib.media.recorder;

import android.content.Context;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.util.Log;

import com.sharry.lib.camera.SCameraView;
import com.sharry.lib.camera.Size;

import java.io.IOException;
import java.nio.ByteBuffer;

import static com.sharry.lib.media.recorder.IRecorderCallback.ERROR_MUXER_FAILED;

/**
 * 视频信息录制者
 *
 * @author Sharry <a href="xiaoyu.zhu@1hai.cn">Contact me.</a>
 * @version 1.0
 * @since 2019-07-15 17:36
 */
final class VideoRecorder extends BaseMediaRecorder implements IAudioEncoder.Callback, IVideoEncoder.Callback {

    private static final String RECORD_PREFIX = "Video_";
    private final AudioRecorder mAudio;
    private final IVideoEncoder mEncoder;
    private final IVideoEncoder.Context mEncodeContext;
    private final IMuxer mMuxer;

    VideoRecorder(Context context, VideoOptions options, SCameraView cameraView, IRecorderCallback callback) {
        super(context, callback);
        // init audio record
        this.mAudio = new AudioRecorder(
                context,
                options.getAudioOptions().reBuilder().setIsJustEncode(true).build(),
                null
        );
        this.mAudio.setEncodeCallback(this);
        // inflate Context
        Size previewerSize = cameraView.getPreviewer().getSize();
        this.mEncodeContext = new IVideoEncoder.Context(
                previewerSize.getWidth(),
                previewerSize.getHeight(),
                options.getFrameRate(),
                cameraView.getPreviewer().getRenderer().getTextureId(),
                cameraView.getPreviewer().getEGLContext(),
                this
        );
        // Step1. Create an instance of video encoder.
        this.mEncoder = EncoderFactory.create(options.getVideoEncodeType());
        // Step2. Create an instance of video muxer and prepare.
        this.mMuxer = MuxerFactory.createEncoder(options.getMuxerType());
        try {
            this.mOutputFile = FileUtil.createFile(options.getOutputDir(), RECORD_PREFIX,
                    options.getMuxerType().getFileSuffix());
        } catch (IOException e) {
            throw new UnsupportedOperationException("Please ensure file can create correct.");
        }
    }

    // //////////////////////////////////// 音频的相关回调 ////////////////////////////////////

    @Override
    public void onAudioFormatChanged(MediaFormat outputFormat) {
        mMuxer.addAudioTrack(outputFormat);
    }

    @Override
    public void onAudioEncoded(ByteBuffer byteBuffer, MediaCodec.BufferInfo bufferInfo) {
        // 回调录制进度
        mCallback.onProgress(bufferInfo.presentationTimeUs / 1000);
        // 合并音视频
        try {
            mMuxer.execute(
                    IMuxer.Parcel.newInstance(IMuxer.Parcel.TRACK_AUDIO, byteBuffer, bufferInfo)
            );
        } catch (Throwable e) {
            handleRecordFailed(ERROR_MUXER_FAILED, e);
        }
    }

    // //////////////////////////////////// 视频的相关回调 ////////////////////////////////////

    @Override
    public void onVideoFormatChanged(MediaFormat outputFormat) {
        mMuxer.addVideoTrack(outputFormat);
    }

    @Override
    public void onVideoEncoded(ByteBuffer byteBuffer, MediaCodec.BufferInfo bufferInfo) {
        // 回调录制进度
        mCallback.onProgress(bufferInfo.presentationTimeUs / 1000);
        // 合并音视频
        try {
            mMuxer.execute(
                    IMuxer.Parcel.newInstance(IMuxer.Parcel.TRACK_VIDEO, byteBuffer, bufferInfo)
            );
        } catch (Throwable e) {
            handleRecordFailed(ERROR_MUXER_FAILED, e);
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
        // prepare encoder and muxer.
        try {
            mEncoder.prepare(mEncodeContext);
            mMuxer.prepare(mOutputFile);
        } catch (IOException e) {
            handleRecordFailed(IRecorderCallback.ERROR_ENCODER_PREPARE_FAILED, e);
            return;
        }
        // start everything.
        mEncoder.start();
        mAudio.start();
        mCallback.onStart();
    }

    @Override
    public void pause() {
        if (!isRecording) {
            Log.e(TAG, "Not recording.");
            return;
        }
        mEncoder.pause();
        mAudio.pause();
        mCallback.onPause();
    }

    @Override
    public void resume() {
        if (!isRecording) {
            Log.e(TAG, "Not recording.");
            return;
        }
        mEncoder.resume();
        mAudio.resume();
        mCallback.onResume();
    }

    @Override
    public void cancel() {
        if (!isRecording) {
            Log.e(TAG, "Not recording.");
            return;
        }
        // 回调音频取消
        mAudio.cancel();
        // 删除文件
        performRecordFileDelete();
        // 回调取消
        mCallback.onCancel();
        // 释放内存
        release();
    }

    @Override
    public void complete() {
        if (!isRecording) {
            Log.e(TAG, "Not recording.");
            return;
        }
        // 回调音频完成
        mAudio.complete();
        // 在文件管理器中刷新生成的文件
        FileUtil.notifyNewFileCreated(mContext, mOutputFile);
        mCallback.onComplete(mOutputFile);
        // 释放资源
        release();
    }

    private void release() {
        isRecording = false;
        try {
            mEncoder.stop();
            mMuxer.release();
        } catch (Throwable e) {
            // ignore;
        }
    }

}
