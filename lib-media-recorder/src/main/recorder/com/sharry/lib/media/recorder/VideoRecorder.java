package com.sharry.lib.media.recorder;

import android.content.Context;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.util.Log;

import androidx.annotation.WorkerThread;

import com.sharry.lib.camera.SCameraView;
import com.sharry.lib.camera.Size;

import java.nio.ByteBuffer;

import static com.sharry.lib.media.recorder.IRecorderCallback.ERROR_MUXER_FAILED;
import static com.sharry.lib.media.recorder.Options.Video.RESOLUTION_1080P;
import static com.sharry.lib.media.recorder.Options.Video.RESOLUTION_480P;
import static com.sharry.lib.media.recorder.Options.Video.RESOLUTION_720P;

/**
 * 视频信息录制者
 *
 * @author Sharry <a href="xiaoyu.zhu@1hai.cn">Contact me.</a>
 * @version 1.0
 * @since 2019-07-15 17:36
 */
final class VideoRecorder extends BaseMediaRecorder implements IAudioEncoder.Callback, IVideoEncoder.Callback {

    private final AudioRecorder mAudio;
    private final IVideoEncoder mEncoder;
    private final IVideoEncoder.Context mEncodeContext;
    private final IMuxer mMuxer;

    VideoRecorder(Context context, Options.Video options, SCameraView cameraView, IRecorderCallback callback) {
        super(context, callback);
        // init audio record
        this.mAudio = new AudioRecorder(
                context,
                options.getAudioOptions().reBuilder().setIsJustEncode(true).build(),
                null
        );
        this.mAudio.setEncodeCallback(this);
        // inflate Context
        int[] frameSize = new int[2];
        calculateRecordFrameSize(options.getResolution(), frameSize, cameraView.getPreviewer().getSize(), cameraView.isLandscape());
        this.mEncodeContext = new IVideoEncoder.Context(
                frameSize[0], frameSize[1],
                options.getFrameRate(),
                cameraView.getPreviewer().getRenderer().getTextureId(),
                cameraView.getPreviewer().getEGLContext(),
                this
        );
        // Step1. Create an instance of video encoder.
        this.mEncoder = EncoderFactory.create(options.getVideoEncodeType());
        // Step2. Create an instance of video muxer and prepare.
        this.mMuxer = MuxerFactory.createEncoder(options.getMuxerType());
        if (VersionUtil.isQ()) {
            this.mOutputUri = FileUtil.createVideoUri(context, options.getRelativePath(),
                    options.getMuxerType().getMIME(), options.getMuxerType().getFileSuffix());
        } else {
            this.mOutputFile = FileUtil.createVideoFile(context, options.getRelativePath(),
                    options.getMuxerType().getFileSuffix());
        }
    }

    // //////////////////////////////////// IAudioEncoder.Callback ////////////////////////////////////

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
            performRecordFailed(ERROR_MUXER_FAILED, e);
        }
    }

    // //////////////////////////////////// IVideoEncoder.Callback  ////////////////////////////////////

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
            performRecordFailed(ERROR_MUXER_FAILED, e);
        }
    }

    // //////////////////////////////////// IMediaRecorder ////////////////////////////////////

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
                // prepare encoder.
                try {
                    mEncoder.prepare(mEncodeContext);
                } catch (Throwable e) {
                    performRecordFailed(IRecorderCallback.ERROR_ENCODER_PREPARE_FAILED, e);
                    return;
                }
                // prepare muxer.
                try {
                    if (VersionUtil.isQ()) {
                        mMuxer.prepare(mContext, mOutputUri);
                    } else {
                        mMuxer.prepare(mContext, mOutputFile);
                    }
                } catch (Throwable e) {
                    performRecordFailed(IRecorderCallback.ERROR_MUXER_PREPARE_FAILED, e);
                    return;
                }
                // start everything.
                mEncoder.start();
                mAudio.start();
                mCallback.onStart();
            }
        });
    }

    @Override
    public void pause() {
        if (!isRecording) {
            Log.i(TAG, "Not recording.");
            return;
        }
        mEncoder.pause();
        mAudio.pause();
        mCallback.onPause();
    }

    @Override
    public void resume() {
        if (!isRecording) {
            Log.i(TAG, "Not recording.");
            return;
        }
        mEncoder.resume();
        mAudio.resume();
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
                // 回调取消
                mCallback.onCancel();
            }
        });
    }

    @Override
    public void complete() {
        if (!isRecording) {
            Log.i(TAG, "Not recording.");
            return;
        }
        AVPoolExecutor.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                // 释放资源
                stop();
                // 处理成功回调
                if (!VersionUtil.isQ()) {
                    // 在文件管理器中刷新生成的文件
                    FileUtil.notifyMediaStore(mContext, mOutputFile.getAbsolutePath());
                }
                // 回调完成
                mCallback.onComplete(mOutputUri, mOutputFile);
            }
        });
    }

    // //////////////////////////////////// BaseMediaRecorder ////////////////////////////////////

    @Override
    @WorkerThread
    protected void stop() {
        if (isRecording) {
            // 音频停止
            mAudio.stop();
            // 停止编码器
            mEncoder.stop();
            // 停止音视频合并
            mMuxer.stop();
            // 变更标记位
            isRecording = false;
        }
    }

    /**
     * 计算录制时视频帧的尺寸
     *
     * @param resolution    需要录制的分辨率
     * @param frameSize     传出参数, 用于保存计算后的数据
     * @param previewerSize 相机数据源的尺寸
     * @param isLandscape   手机是否为横屏
     */
    private void calculateRecordFrameSize(int resolution, int[] frameSize, Size previewerSize, boolean isLandscape) {
        float resolutionWidth, resolutionHeight;
        switch (resolution) {
            case RESOLUTION_1080P:
                resolutionWidth = 1080f;
                resolutionHeight = 1920f;
                break;
            case RESOLUTION_480P:
                resolutionWidth = 480f;
                resolutionHeight = 720f;
                break;
            case RESOLUTION_720P:
            default:
                resolutionWidth = 720f;
                resolutionHeight = 1280f;
                break;
        }
        // 若为横屏, 则翻转宽高
        if (isLandscape) {
            float temp = resolutionWidth;
            resolutionWidth = resolutionHeight;
            resolutionHeight = temp;
        }
        float scale = Math.min(resolutionWidth / previewerSize.getWidth(),
                resolutionHeight / previewerSize.getHeight());
        // 预览数据的尺寸比目标分辨率小, 则直接使用预览尺寸
        if (scale >= 1.0f) {
            frameSize[0] = previewerSize.getWidth();
            frameSize[1] = previewerSize.getHeight();
        }
        // 缩放预览尺寸到符合分辨率的标准之后再进行采集
        else {
            frameSize[0] = (int) (previewerSize.getWidth() * scale);
            frameSize[1] = (int) (previewerSize.getHeight() * scale);
        }
    }

}
