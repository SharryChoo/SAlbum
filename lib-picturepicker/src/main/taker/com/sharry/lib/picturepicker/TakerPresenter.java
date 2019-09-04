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

    private static final int COUNT_PLAY_TRY_AGAIN = 3;

    private final ITakerContract.IView mView;
    private final TakerConfig mConfig;
    private final SMediaRecorder mRecorder;
    private final VideoOptions mRecordOptions;
    private Bitmap mFetchedBitmap;
    private File mVideoFile;
    private long mRecordDuration;
    private int mTryAgainCount = 0;

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
        mView.setStatus(ITakerContract.IView.STATUS_CAMERA_PREVIEW);
        if (mVideoFile != null) {
            performVideoEnsure();
        } else {
            performPictureEnsure();
        }
    }

    @Override
    public void handleDenied() {
        recycleSource();
        // 置为相机预览
        mView.setStatus(ITakerContract.IView.STATUS_CAMERA_PREVIEW);
    }

    @Override
    public void handleVideoPlayFailed() {
        if (mTryAgainCount++ < COUNT_PLAY_TRY_AGAIN) {
            mView.startVideoPlayer(mConfig.getAuthority(), mVideoFile);
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
            mRecorder.complete();
        }
    }

    @Override
    public void handleViewDestroy() {
        mRecorder.cancel();
        if (mView.getStatus() != ITakerContract.IView.STATUS_CAMERA_PREVIEW) {
            mFetchedBitmap = null;
            mTryAgainCount = 0;
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
        recycleSource();
        mView.toast("录制失败, 请稍后重试...");
        mView.setStatus(ITakerContract.IView.STATUS_CAMERA_PREVIEW);
    }

    /**
     * 处理录制成功
     */
    private void performRecordComplete(File file) {
        mVideoFile = file;
        mView.setStatus(ITakerContract.IView.STATUS_VIDEO_PLAY);
        mView.startVideoPlayer(mConfig.getAuthority(), mVideoFile);
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
            // ignore.
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
    private void recycleSource() {
        mFetchedBitmap = null;
        mTryAgainCount = 0;
        if (mVideoFile != null) {
            mView.stopVideoPlayer();
            if (mVideoFile.delete()) {
                mView.notifyFileDeleted(mVideoFile.getAbsolutePath());
            }
            mVideoFile = null;
        }
    }

}
