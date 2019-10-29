package com.sharry.lib.album;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import com.sharry.lib.camera.AspectRatio;
import com.sharry.lib.camera.SCameraView;
import com.sharry.lib.media.recorder.EncodeType;
import com.sharry.lib.media.recorder.IRecorderCallback;
import com.sharry.lib.media.recorder.MuxerType;
import com.sharry.lib.media.recorder.Options;
import com.sharry.lib.media.recorder.SMediaRecorder;

import java.io.File;

import static com.sharry.lib.album.TakerConfig.ASPECT_16_9;
import static com.sharry.lib.album.TakerConfig.ASPECT_1_1;
import static com.sharry.lib.album.TakerConfig.ASPECT_4_3;

/**
 * @author Sharry <a href="sharrychoochn@gmail.com">Contact me.</a>
 * @version 1.0
 * @since 2019-09-02
 */
class TakerPresenter implements ITakerContract.IPresenter {

    private static final String TAG = TakerPresenter.class.getSimpleName();
    private static final int MAXIMUM_TRY_AGAIN_THRESHOLD = 3;

    private final Context mContext;
    private final ITakerContract.IView mView;
    private final TakerConfig mConfig;
    private final SMediaRecorder mRecorder;
    private final Options.Video mRecordOptions;
    private Bitmap mFetchedBitmap;
    private long mRecordDuration;
    private int mCountTryAgain = 0;
    private Uri mVideoUri;
    private File mVideoFile;

    TakerPresenter(TakerActivity view, TakerConfig config) {
        this.mContext = view;
        this.mView = view;
        this.mConfig = config;
        this.mRecorder = SMediaRecorder.with(view);
        this.mRecorder.addRecordCallback(new IRecorderCallback.Adapter() {

            @Override
            public void onProgress(long time) {
                performProgressChanged(time);
            }

            @Override
            public void onComplete(@NonNull Uri uri, File file) {
                performRecordComplete(uri, file);
            }

            @Override
            public void onFailed(int errorCode, @NonNull Throwable e) {
                performRecordFiled();
            }

        });
        this.mRecordOptions = new Options.Video.Builder()
                .setRelativePath(mConfig.getRelativePath())
                .setAuthority(mConfig.getAuthority())
                .setEncodeType(EncodeType.Video.H264)
                .setMuxerType(MuxerType.MP4)
                .setResolution(Options.Video.RESOLUTION_720P)
                .setAudioOptions(Options.Audio.DEFAULT)
                .build();
        // 配置视图
        setupViews();
    }

    @Override
    public void handleGranted() {
        // 重置为预览, 防止销毁时文件的误删
        mView.setStatus(ITakerContract.IView.STATUS_CAMERA_PREVIEW);
        if (mVideoUri != null || mVideoFile != null) {
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
            mView.startVideoPlayer(mVideoUri);
        } else {
            // 重新尝试了 3 次仍然没有播放成功, 说明录制的视频有问题, 当做录制失败处理
            performRecordFiled();
        }
    }

    @Override
    public void handleTakePicture(Bitmap bitmap) {
        if (bitmap == null) {
            mView.toast(R.string.lib_album_taker_take_picture_failed);
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
            mView.toast(R.string.lib_album_taker_record_time_too_short);
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
            recycle();
        }
    }

    private void setupViews() {
        // 配置 CameraView
        switch (mConfig.getPreviewAspect()) {
            case ASPECT_1_1:
                mView.setPreviewAspect(AspectRatio.of(1, 1));
                break;
            case ASPECT_16_9:
                mView.setPreviewAspect(AspectRatio.of(16, 9));
                break;
            case ASPECT_4_3:
            default:
                mView.setPreviewAspect(AspectRatio.of(4, 3));
                break;
        }
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
        mView.toast(R.string.lib_album_taker_record_failed);
        mView.setStatus(ITakerContract.IView.STATUS_CAMERA_PREVIEW);
    }

    /**
     * 处理录制成功
     */
    private void performRecordComplete(Uri uri, File file) {
        mVideoUri = uri;
        mVideoFile = file;
        mView.setStatus(ITakerContract.IView.STATUS_VIDEO_PLAY);
        if (mVideoUri != null) {
            mView.startVideoPlayer(mVideoUri);
        } else if (mVideoFile != null) {
            mView.startVideoPlayer(
                    FileUtil.getUriFromFile(mContext, mConfig.getAuthority(), mVideoFile)
            );
        }
    }

    /**
     * 处理图像确认
     */
    private void performPictureEnsure() {
        try {
            if (VersionUtil.isQ()) {
                Uri uri = FileUtil.createJpegUri(mContext, mConfig.getRelativePath());
                ParcelFileDescriptor pfd = mContext.getContentResolver().openFileDescriptor(uri, "w");
                CompressUtil.doCompress(mFetchedBitmap, pfd.getFileDescriptor(), mConfig.getPictureQuality(),
                        mFetchedBitmap.getWidth(), mFetchedBitmap.getHeight());
                String path = FileUtil.getPath(mContext, uri);
                MediaMeta mediaMeta = MediaMeta.create(uri, path, true);
                mediaMeta.date = System.currentTimeMillis();
                mView.setResult(mediaMeta);
            } else {
                File file = FileUtil.createJpegFile(mContext, mConfig.getRelativePath());
                Uri uri = FileUtil.getUriFromFile(mContext, mConfig.getAuthority(), file);
                ParcelFileDescriptor pfd = mContext.getContentResolver().openFileDescriptor(uri, "w");
                CompressUtil.doCompress(mFetchedBitmap, pfd.getFileDescriptor(), mConfig.getPictureQuality(),
                        mFetchedBitmap.getWidth(), mFetchedBitmap.getHeight());
                MediaMeta mediaMeta = MediaMeta.create(uri, file.getAbsolutePath(), true);
                mediaMeta.date = System.currentTimeMillis();
                // Android 10 以下需要手动更新媒体库
                FileUtil.notifyMediaStore(mContext, file.getAbsolutePath());
                mView.setResult(mediaMeta);
            }
        } catch (Throwable e) {
            e.printStackTrace();
            mView.toast(R.string.lib_album_taker_picture_saved_failed);
        }
    }

    /**
     * 处理视频确认
     */
    private void performVideoEnsure() {
        long currentTime = System.currentTimeMillis();
        if (VersionUtil.isQ() && mVideoUri != null) {
            MediaMeta mediaMeta = MediaMeta.create(mVideoUri, FileUtil.getPath(mContext, mVideoUri), false);
            mediaMeta.date = currentTime;
            mediaMeta.duration = mRecordDuration;
            mView.setResult(mediaMeta);
        } else if (mVideoFile != null) {
            MediaMeta mediaMeta = MediaMeta.create(
                    FileUtil.getUriFromFile(mContext, mConfig.getAuthority(), mVideoFile),
                    mVideoFile.getAbsolutePath(),
                    false
            );
            mediaMeta.date = currentTime;
            mediaMeta.duration = mRecordDuration;
            // 通知媒体库的操作, 在 Recorder 中完成
            mView.setResult(mediaMeta);
        } else {
            mView.toast(R.string.lib_album_taker_video_saved_failed);
        }
    }

    /**
     * 重置资源
     */
    private void recycle() {
        mFetchedBitmap = null;
        mCountTryAgain = 0;
        if (mVideoUri != null) {
            FileUtil.delete(mContext, mVideoUri);
            mVideoUri = null;
        }
        if (mVideoFile != null) {
            FileUtil.delete(mContext, mVideoFile);
            mVideoFile = null;
        }
        mView.stopVideoPlayer();
    }

}
