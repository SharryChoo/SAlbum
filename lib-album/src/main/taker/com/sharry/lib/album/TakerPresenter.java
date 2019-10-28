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
            public void onComplete(@NonNull Uri uri) {
                performRecordComplete(uri);
            }

            @Override
            public void onFailed(int errorCode, @NonNull Throwable e) {
                performRecordFiled();
            }

        });
        this.mRecordOptions = new Options.Video.Builder()
                .setOutputDir(mConfig.getRelativePath())
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
        if (mVideoUri != null) {
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
            mFetchedBitmap = null;
            mCountTryAgain = 0;
            if (mVideoUri != null) {
                mView.stopVideoPlayer();
                // TODO 删除媒体文件
                mVideoUri = null;
            }
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
    private void performRecordComplete(Uri uri) {
        mVideoUri = uri;
        mView.setStatus(ITakerContract.IView.STATUS_VIDEO_PLAY);
        mView.startVideoPlayer(mVideoUri);
    }

    /**
     * 处理图像确认
     */
    private void performPictureEnsure() {
        try {
            Uri uri = FileUtil.createJpegUri((Context) mView, mConfig.getAuthority(), mConfig.getRelativePath());
            ParcelFileDescriptor fd = ((Context) mView).getContentResolver().openFileDescriptor(uri, "w");
            CompressUtil.doCompress(mFetchedBitmap, fd.getFileDescriptor(), mConfig.getPictureQuality(),
                    mFetchedBitmap.getWidth(), mFetchedBitmap.getHeight());
            // TODO 获取文件路径
            MediaMeta mediaMeta = MediaMeta.create(uri, "", true);
            mediaMeta.date = System.currentTimeMillis();
            mView.setResult(mediaMeta);
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
        // TODO 获取文件路径
        MediaMeta mediaMeta = MediaMeta.create(mVideoUri, "", false);
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
        if (mVideoUri != null) {
            mView.stopVideoPlayer();
            mContext.getContentResolver().delete(mVideoUri, null, null);
            mVideoUri = null;
        }
    }

}
