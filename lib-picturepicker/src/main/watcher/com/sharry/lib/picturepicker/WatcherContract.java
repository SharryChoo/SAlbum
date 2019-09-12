package com.sharry.lib.picturepicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import java.util.ArrayList;

/**
 * PicturePicture MVP 的约束
 *
 * @author Sharry <a href="xiaoyu.zhu@1hai.cn">Contact me.</a>
 * @version 1.0
 * @since 2018/6/13.
 */
interface WatcherContract {

    interface IView {

        void showSharedElementEnter(@NonNull MediaMeta mediaMeta, @NonNull SharedElementModel elementData);

        void showSharedElementExitAndFinish(@NonNull SharedElementModel elementData);

        void setLeftTitleText(@NonNull CharSequence content);

        void setIndicatorVisible(boolean isShowCheckedIndicator);

        void setIndicatorColors(int indicatorBorderCheckedColor, int indicatorBorderUncheckedColor,
                                int indicatorSolidColor, int indicatorTextColor);

        void setIndicatorChecked(boolean isChecked);

        void setIndicatorText(@NonNull CharSequence indicatorText);

        void setEnsureText(@NonNull CharSequence content);

        void setDisplayAdapter(@NonNull ArrayList<MediaMeta> mediaMetas);

        void setPickedAdapter(@NonNull ArrayList<MediaMeta> pickedSet);

        void setDisallowViewPagerDismissAnim(boolean isDisallowDismissAnim);

        void displayAt(int position);

        void smoothScrollToPosition(int position);

        void showPickedPanel();

        void dismissPickedPanel();

        void notifyItemRemoved(@NonNull MediaMeta removedMeta, int removedIndex);

        void notifyItemPicked(@NonNull MediaMeta addedMeta, int addedIndex);

        void finish();

        void setResult(@Nullable ArrayList<MediaMeta> pickedPaths, boolean isEnsurePressed);

        String getString(@StringRes int resId);

        void showMsg(@NonNull String msg);

    }

    interface IPresenter {

        void handlePagerChanged(int position);

        void handleEnsureClicked();

        void handleIndicatorClick(boolean isChecked);

        void handlePickedItemClicked(MediaMeta pickedUri);

        void handleBackPressed();

        void handleFinish();

    }

}