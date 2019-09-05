package com.sharry.lib.picturepicker;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import com.sharry.lib.camera.AspectRatio;
import com.sharry.lib.camera.IPreviewer;
import com.sharry.lib.camera.SCameraView;
import com.sharry.lib.media.recorder.AudioOptions;
import com.sharry.lib.media.recorder.EncodeType;
import com.sharry.lib.media.recorder.IRecorderCallback;
import com.sharry.lib.media.recorder.MuxerType;
import com.sharry.lib.media.recorder.SMediaRecorder;
import com.sharry.lib.media.recorder.VideoOptions;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;

/**
 * @author Sharry <a href="sharrychoochn@gmail.com">Contact me.</a>
 * @version 1.0
 * @since 2019-09-02
 */
class TakerPresenter implements ITakerContract.IPresenter {

    private static final String TAG = TakerPresenter.class.getSimpleName();
    private static final int MAXIMUM_TRY_AGAIN_THRESHOLD = 3;

    private final ITakerContract.IView mView;
    private final TakerConfig mConfig;
    private final SMediaRecorder mRecorder;
    private final VideoOptions mRecordOptions;
    private Bitmap mFetchedBitmap;
    private File mVideoFile;
    private long mRecordDuration;
    private int mCountTryAgain = 0;

    TakerPresenter(TakerActivity view, TakerConfig config) {
        this.mView = view;
        this.mConfig = config;
        this.mRecorder = SMediaRecorder.with(view);
        this.mRecorder.addRecordCallback(new IRecorderCallback.Adapter() {

            @Override
            public void onProgress(long time) {
                performProgressChanged(time);
            }

            @Override
            public void onComplete(@NonNull File file) {
                performRecordComplete(file);
            }

            @Override
            public void onFailed(int errorCode, @NonNull Throwable e) {
                performRecordFiled();
            }

        });
        this.mRecordOptions = new VideoOptions.Builder()
                .setOutputDir(mConfig.getDirectoryPath())
                .setEncodeType(EncodeType.Video.H264)
                .setMuxerType(MuxerType.MP4)
                .setAudioOptions(AudioOptions.DEFAULT)
                .build();
        // 配置视图
        setupViews();
    }

    @Override
    public void handleGranted() {
        // 重置为预览, 防止销毁时文件的误删
        mView.setStatus(ITakerContract.IView.STATUS_CAMERA_PREVIEW);
        if (mVideoFile != null) {
            performVideoEnsure();
        } else {
            performPictureEnsure();
        }
    }

    @Override
    public void handleDenied() {
        mView.setStatus(ITakerContract.IView.STATUS_CAMERA_PREVIEW);
        recycle();
    }

    @Override
    public void handleVideoPlayFailed() {
        if (mCountTryAgain++ < MAXIMUM_TRY_AGAIN_THRESHOLD) {
            Log.w(TAG, "Occurred an error, try again " + mCountTryAgain + " time");
            mView.startVideoPlayer(mVideoFile.getAbsolutePath());
        } else {
            // 重新尝试了 3 次仍然没有播放成功, 说明录制的视频有问题, 当做录制失败处理
            performRecordFiled();
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
        mView.setStatus(ITakerContract.IView.STATUS_PICTURE_PREVIEW);
        mView.setPreviewSource(mFetchedBitmap);
    }

    @Override
    @SuppressLint("MissingPermission")
    public void handleRecordStart(SCameraView cameraView) {
        mRecorder.start(cameraView, mRecordOptions);
    }

    @Override
    public void handleRecordFinish(long duration) {
        if (duration < mConfig.getMinimumDuration()) {
            mRecorder.cancel();
            mView.toast("录制时间过短");
        } else {
            // Recorder 的 Complete 是异步操作, 这里先将录制按钮异常, 防止用户误触
            mView.setRecordButtonVisible(false);
            mRecorder.complete();
        }
    }

    @Override
    public void handleViewDestroy() {
        mRecorder.cancel();
        if (mView.getStatus() != ITakerContract.IView.STATUS_CAMERA_PREVIEW) {
            mFetchedBitmap = null;
            mCountTryAgain = 0;
            if (mVideoFile != null) {
                mView.stopVideoPlayer();
                if (mVideoFile.delete()) {
                    mView.notifyFileDeleted(mVideoFile.getAbsolutePath());
                }
                mVideoFile = null;
            }
        }
    }

    private void setupViews() {
        // 配置 CameraView
        mView.setPreviewAspect(mConfig.getPreviewAspect() == null ?
                AspectRatio.DEFAULT : mConfig.getPreviewAspect());
        mView.setPreviewFullScreen(mConfig.isFullScreen());
        if (!TextUtils.isEmpty(mConfig.getRendererClassName())) {
            mView.setPreviewRenderer(mConfig.getRendererClassName());
        }
        // 配置 RecorderView
        mView.setMaxRecordDuration(mConfig.getMaximumDuration());
        mView.setSupportVideoRecord(mConfig.isSupportVideoRecord());
        mView.setProgressColor(mConfig.getRecordProgressColor());
        // 设置 View 为预览状态
        mView.setStatus(ITakerContract.IView.STATUS_CAMERA_PREVIEW);
    }


    /**
     * 处理录制进度变更
     */
    private void performProgressChanged(long time) {
        mRecordDuration = time;
        mView.setRecordButtonProgress(time);
    }

    /**
     * 处理录制失败
     */
    private void performRecordFiled() {
        recycle();
        mView.toast("录制失败, 请稍后重试...");
        mView.setStatus(ITakerContract.IView.STATUS_CAMERA_PREVIEW);
    }

    /**
     * 处理录制成功
     */
    private void performRecordComplete(File file) {
        mVideoFile = file;
        mView.setStatus(ITakerContract.IView.STATUS_VIDEO_PLAY);
        mView.startVideoPlayer(mVideoFile.getAbsolutePath());
    }

    /**
     * 处理图像确认
     */
    private void performPictureEnsure() {
        File file = FileUtil.createCameraDestFile(mConfig.getDirectoryPath());
        try {
            CompressUtil.doCompress(mFetchedBitmap, file.getAbsolutePath(),
                    mConfig.getPictureQuality(), mFetchedBitmap.getWidth(), mFetchedBitmap.getHeight());
            MediaMeta mediaMeta = MediaMeta.create(file.getAbsolutePath(), true);
            mediaMeta.date = System.currentTimeMillis();
            mView.setResult(mediaMeta);
        } catch (IOException e) {
            mView.toast("图片保存失败");
        }
    }

    /**
     * 处理视频确认
     */
    private void performVideoEnsure() {
        long currentTime = System.currentTimeMillis();
        MediaMeta mediaMeta = MediaMeta.create(mVideoFile.getAbsolutePath(), false);
        mediaMeta.date = currentTime;
        mediaMeta.duration = mRecordDuration;
        mView.setResult(mediaMeta);
    }

    /**
     * 重置资源
     */
    private void recycle() {
        mFetchedBitmap = null;
        mCountTryAgain = 0;
        if (mVideoFile != null) {
            mView.stopVideoPlayer();
            if (mVideoFile.delete()) {
                mView.notifyFileDeleted(mVideoFile.getAbsolutePath());
            }
            mVideoFile = null;
        }
    }

}
