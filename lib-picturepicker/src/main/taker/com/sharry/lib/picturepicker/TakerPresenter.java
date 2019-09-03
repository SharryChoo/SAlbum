package com.sharry.lib.picturepicker;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;

import androidx.annotation.NonNull;

import com.sharry.lib.camera.AspectRatio;
import com.sharry.lib.camera.SCameraView;
import com.sharry.lib.media.recorder.AudioOptions;
import com.sharry.lib.media.recorder.EncodeType;
import com.sharry.lib.media.recorder.IRecorderCallback;
import com.sharry.lib.media.recorder.MuxerType;
import com.sharry.lib.media.recorder.SMediaRecorder;
import com.sharry.lib.media.recorder.VideoOptions;

import java.io.File;
import java.io.IOException;

/**
 * @author Sharry <a href="sharrychoochn@gmail.com">Contact me.</a>
 * @version 1.0
 * @since 2019-09-02
 */
class TakerPresenter implements ITakerContract.IPresenter {

    private final ITakerContract.IView mView;
    private final TakerConfig mConfig;
    private final SMediaRecorder mRecorder;
    private final VideoOptions mRecordOptions;
    private final IRecorderCallback mRecorderCallback = new IRecorderCallback.Adapter() {

        @Override
        public void onProgress(long time) {
            mRecordDuration = time;
            mView.setRecordButtonProgress(time);
        }

        @Override
        public void onComplete(@NonNull File file) {
            performRecordComplete(file);
        }

        @Override
        public void onFailed(int errorCode, @NonNull Throwable e) {
            performRecordFiled();
        }

    };
    private Bitmap mFetchedBitmap;
    private File mVideoFile;
    private long mRecordDuration;
    private int mTryAgainCount = 0;

    TakerPresenter(TakerActivity view, TakerConfig config) {
        this.mView = view;
        this.mConfig = config;
        this.mRecorder = SMediaRecorder.with(view);
        this.mRecorder.addRecordCallback(mRecorderCallback);
        this.mRecordOptions = new VideoOptions.Builder()
                .setOutputDir(mConfig.getDirectoryPath())
                .setEncodeType(EncodeType.Video.H264)
                .setMuxerType(MuxerType.MP4)
                .setAudioOptions(AudioOptions.DEFAULT)
                .build();
        setupViews();
    }

    @Override
    public void handleGranted() {
        if (mVideoFile != null) {
            performVideoEnsure();
        } else {
            performPictureEnsure();
        }
    }

    @Override
    public void handleDenied() {
        mFetchedBitmap = null;
        if (mVideoFile != null) {
            mView.stopVideoPlayer();
            mVideoFile.delete();
            mView.notifyFileDeleted(mVideoFile.getAbsolutePath());
            mVideoFile = null;
        }
        // 置为预览状态
        mView.setToolbarVisible(true);
        mView.setCameraViewVisible(true);
        mView.setRecordButtonVisible(true);
        mView.setVideoPlayerVisible(false);
        mView.setGrantedButtonVisible(false);
        mView.setDeniedButtonVisible(false);
        mView.setPicturePreviewVisible(false);
        // 开始预览
        mView.startPreview();
    }

    @Override
    public void handleVideoPlayFailed() {
        if (mTryAgainCount++ < 3) {
            mView.startVideoPlayer(mVideoFile.getAbsolutePath());
        } else {
            performRecordFiled();
            mTryAgainCount = 0;
        }
    }

    @Override
    public void handleTakePicture(Bitmap bitmap) {
        if (bitmap == null) {
            mView.toast("获取照片失败");
            return;
        }
        // 保存 bitmap
        mFetchedBitmap = bitmap;
        // 展示拍摄的图片
        mView.setToolbarVisible(false);
        mView.setCameraViewVisible(false);
        mView.setRecordButtonVisible(false);
        mView.setVideoPlayerVisible(false);
        // 展示同意与否按钮
        mView.setGrantedButtonVisible(true);
        mView.setDeniedButtonVisible(true);
        // 展示拍摄的图片
        mView.setPicturePreviewVisible(true);
        mView.setPreviewSource(mFetchedBitmap);
    }

    @Override
    @SuppressLint("MissingPermission")
    public void handleRecordStart(SCameraView cameraView) {
        mRecorder.start(cameraView, mRecordOptions);
    }

    @Override
    public void handleRecordFinish(long duration) {
        if (duration < 1500) {
            mRecorder.cancel();
            mView.toast("录制时间过短");
        } else {
            mRecorder.complete();
        }
    }

    @Override
    public void release() {
        mRecorder.cancel();
    }

    private void setupViews() {
        // 配置 CameraView
        mView.setPreviewAspect(mConfig.getPreviewAspect() == null ?
                AspectRatio.DEFAULT : mConfig.getPreviewAspect());
        mView.setPreviewFullScreen(mConfig.isFullScreen());
        // 配置 RecorderView
        mView.setMaxRecordDuration(mConfig.getMaxRecordDuration());
        mView.isSupportVideoRecord(mConfig.isSupportVideoRecord());
        // 置为预览状态
        mView.setToolbarVisible(true);
        mView.setCameraViewVisible(true);
        mView.setRecordButtonVisible(true);
        mView.setVideoPlayerVisible(false);
        mView.setGrantedButtonVisible(false);
        mView.setDeniedButtonVisible(false);
        mView.setPicturePreviewVisible(false);
        // 开始预览
        mView.startPreview();
    }

    private void performRecordComplete(File file) {
        mVideoFile = file;
        mView.setToolbarVisible(false);
        mView.setCameraViewVisible(false);
        mView.setRecordButtonVisible(false);
        mView.setPicturePreviewVisible(false);
        // 展示同意与否按钮
        mView.setGrantedButtonVisible(true);
        mView.setDeniedButtonVisible(true);
        // 播放视频
        mView.setVideoPlayerVisible(true);
        mView.startVideoPlayer(mVideoFile.getAbsolutePath());
    }

    private void performPictureEnsure() {
        File file = FileUtil.createCameraDestFile(mConfig.getDirectoryPath());
        try {
            CompressUtil.doCompress(mFetchedBitmap, file.getAbsolutePath(),
                    mConfig.getPictureQuality(), mFetchedBitmap.getWidth(), mFetchedBitmap.getHeight());
            MediaMeta mediaMeta = MediaMeta.create(file.getAbsolutePath(), true);
            mediaMeta.date = System.currentTimeMillis();
            mView.setResult(mediaMeta);
        } catch (IOException e) {
            // ignore.
            mView.toast("图片保存失败");
        }
    }

    private void performVideoEnsure() {
        long currentTime = System.currentTimeMillis();
        MediaMeta mediaMeta = MediaMeta.create(mVideoFile.getAbsolutePath(), false);
        mediaMeta.date = currentTime;
        mediaMeta.duration = mRecordDuration;
        mView.setResult(mediaMeta);
    }

    private void performRecordFiled() {
        mView.toast("录制失败, 请稍后重试...");
    }

}
