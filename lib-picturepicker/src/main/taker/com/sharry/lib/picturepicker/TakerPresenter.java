package com.sharry.lib.picturepicker;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;

import androidx.annotation.NonNull;

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

    };
    private Bitmap mFetchedBitmap;
    private File mVideoFile;
    private long mRecordDuration;

    TakerPresenter(TakerActivity view, TakerConfig config) {
        this.mView = view;
        this.mConfig = config;
        this.mRecorder = SMediaRecorder.with(view);
        this.mRecorder.addRecordCallback(mRecorderCallback);
        this.mRecordOptions = new VideoOptions.Builder()
                .setFrameRate(24)
                .setOutputDir(mConfig.getUseableDirectoryPath())
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
        // 回到预览状态
        setupViews();
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
    public void handleRecordFinish() {
        mRecorder.complete();
    }

    @Override
    public void release() {
        mRecorder.cancel();
    }

    private void setupViews() {
        // 置为预览状态
        mView.setToolbarVisible(true);
        mView.setCameraViewVisible(true);
        mView.setRecordButtonVisible(true);
        mView.setVideoPlayerVisible(false);
        mView.setGrantedButtonVisible(false);
        mView.setDeniedButtonVisible(false);
        mView.setPicturePreviewVisible(false);
        // 设置录制最大时长
        mView.setMaxRecordDuration(15 * 1000);
        mView.setRecordButtonProgress(0);
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
        File file = FileUtil.createCameraDestFile(mConfig.getUseableDirectoryPath());
        try {
            CompressUtil.doCompress(mFetchedBitmap, file.getAbsolutePath(),
                    mConfig.getCameraDestQuality(), mFetchedBitmap.getWidth(), mFetchedBitmap.getHeight());
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
        // TODO 获取图像缩略图
        mView.setResult(mediaMeta);
    }

}
