package com.sharry.lib.album;

import android.graphics.Bitmap;
import android.net.Uri;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import com.sharry.lib.camera.AspectRatio;
import com.sharry.lib.camera.SCameraView;

/**
 * @author Sharry <a href="sharrychoochn@gmail.com">Contact me.</a>
 * @version 1.0
 * @since 2019-09-02
 */
public interface ITakerContract {

    interface IView {

        void setPreviewAspect(@NonNull AspectRatio aspect);

        void setPreviewFullScreen(boolean fullScreen);

        void setPreviewRenderer(@NonNull String rendererClassName);

        void setRecordButtonVisible(boolean visible);

        void setSupportVideoRecord(boolean isVideoRecord);

        void setMaxRecordDuration(long maxDuration);

        void setRecordButtonProgress(long currentDuration);

        void setProgressColor(int recordProgressColor);

        void setPreviewSource(@NonNull Bitmap bitmap);

        void startVideoPlayer(@NonNull Uri uri);

        void stopVideoPlayer();

        int STATUS_CAMERA_PREVIEW = 1;
        int STATUS_PICTURE_PREVIEW = 2;
        int STATUS_VIDEO_PLAY = 3;
        int STATUS_PICKED = 4;

        Bitmap getCameraBitmap();

        @IntDef(value = {
                STATUS_CAMERA_PREVIEW,
                STATUS_PICTURE_PREVIEW,
                STATUS_VIDEO_PLAY,
                STATUS_PICKED
        })
        @interface Status {
        }

        void setStatus(@Status int status);

        @Status
        int getStatus();

        void toast(@StringRes int resId);

        void setResult(@NonNull MediaMeta mediaMeta);

    }

    interface IPresenter {

        void handleGranted();

        void handleDenied();

        void handleTakePicture();

        void handleRecordStart(SCameraView cameraView);

        void handleRecordFinish(long duration);

        void handleVideoPlayFailed();

        void handleViewDestroy();
    }

}
