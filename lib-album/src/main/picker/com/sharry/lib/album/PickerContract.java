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

        void setPickerItemDecoration(@NonNull RecyclerView.ItemDecoration itemDecoration);

        void setFolderAdapter(@NonNull ArrayList<FolderModel> allFolders);

        void setPictureFolderText(@NonNull String folderName);

        void setToolbarEnsureText(@NonNull CharSequence content);

        void setPreviewText(@NonNull CharSequence content);

        void notifyPickedSetChanged();

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

        void handlePictureRemoved(@Nullable MediaMeta removedMeta);

        void handlePictureClicked(int position, @Nullable View sharedElement);

        void handlePreviewClicked();

        void handleFolderChecked(int position);

        void handleEnsureClicked();

        void handleCameraClicked();

        void handleViewDestroy();

    }

    interface IModel {

        interface Callback {

            void onFetched(@NonNull ArrayList<FolderModel> folderModels);

        }

        void fetchData(Context context, boolean supportGif, boolean supportVideo, final Callback listener);

        void stopIfFetching();

    }

}
