package com.sharry.lib.album;

import android.content.Context;
import android.view.View;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

/**
 * PicturePicture MVP 的约束
 *
 * @author Sharry <a href="SharryChooCHN@Gmail.com">Contact me.</a>
 * @version 1.0
 * @since 2018/6/13.
 */
interface PickerContract {

    interface IView {

        void setToolbarScrollable(boolean isScrollable);

        void setToolbarBackgroundColor(int color);

        void setToolbarBackgroundDrawable(@DrawableRes int drawableId);

        void setFabColor(int color);

        void setFabVisible(boolean isVisible);

        void setBackgroundColor(int color);

        void setSpanCount(int spanCount);

        void setPickerAdapter(@NonNull PickerConfig config, @NonNull ArrayList<MediaMeta> metas,
                              @NonNull ArrayList<MediaMeta> userPickedMetas);

        void setFolderAdapter(@NonNull ArrayList<FolderModel> allFolders);

        void setPictureFolderText(@NonNull String folderName);

        void setToolbarEnsureText(@NonNull CharSequence content);

        void setPreviewText(@NonNull CharSequence content);

        void notifyDisplaySetItemChanged(int changedIndex);

        void notifyDisplaySetChanged();

        void notifyFolderDataSetChanged();

        void notifyNewMetaInsertToFirst();

        void showMsg(@NonNull String msg);

        String getString(@StringRes int resId);

        void setProgressBarVisible(boolean visible);

        void setResultAndFinish(@NonNull ArrayList<MediaMeta> pickedPaths);
    }

    interface IPresenter {

        boolean handlePictureChecked(@Nullable MediaMeta checkedMeta);

        void handlePictureUnchecked(@Nullable MediaMeta removedMeta);

        void handlePickedSetChanged(MediaMeta mediaMeta);

        void handleCameraClicked();

        void handlePictureClicked(int position, @Nullable View sharedElement);

        void handleFolderChecked(int position);

        void handlePreviewClicked();

        void handleEnsureClicked();

        void handleRecycleViewDraw(RecyclerView parent);

        void handleViewDestroy();
    }

    interface IModel {

        interface Callback {

            void onFetched(@NonNull ArrayList<FolderModel> folderModels);

        }

        void fetchData(Context context, boolean pickPicture, boolean supportGif, boolean supportVideo, final Callback listener);

        void stopIfFetching();

    }

}
