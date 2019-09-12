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
        /**
         * 展示 Toolbar 左部文本
         */
        void displayToolbarLeftText(CharSequence content);

        /**
         * 设置顶部选择指示器的可见性
         */
        void setToolbarCheckedIndicatorVisibility(boolean isShowCheckedIndicator);

        /**
         * 设置顶部选择指示器的颜色
         *
         * @param indicatorBorderCheckedColor   边框选中颜色
         * @param indicatorBorderUncheckedColor 边框为选中颜色
         * @param indicatorSolidColor           填充颜色
         * @param indicatorTextColor            文本颜色
         */
        void setToolbarCheckedIndicatorColors(int indicatorBorderCheckedColor, int indicatorBorderUncheckedColor,
                                              int indicatorSolidColor, int indicatorTextColor);

        /**
         * 设置 Toolbar 上指示器是否被选中
         */
        void setToolbarIndicatorChecked(boolean isChecked);

        /**
         * 展示 Toolbar 指示器上的文本
         */
        void displayToolbarIndicatorText(CharSequence indicatorText);

        /**
         * 设置底部图片预览的 RecyclerView 的 Adapter
         */
        void setPreviewAdapter(ArrayList<MediaMeta> pickedSet);

        /**
         * 展示确认文本
         */
        void displayPreviewEnsureText(CharSequence content);

        /**
         * 设置展示的 Adapter
         */
        void setDisplayAdapter(ArrayList<MediaMeta> mediaMetas);

        /**
         * 展示指定位置的图片
         */
        void displayAt(int position);

        /**
         * 通知选中的图片被移除了
         */
        void notifyBottomPicturesRemoved(MediaMeta removedMeta, int removedIndex);

        /**
         * 通知图片插入了
         */
        void notifyBottomPictureAdded(MediaMeta addedMeta, int addedIndex);

        /**
         * 展示消息通知
         */
        void showMsg(String msg);

        /**
         * 从资源文件获取 String
         */
        String getString(@StringRes int resId);

        /**
         * 展示底部预览视图
         */
        void showBottomPreview();

        /**
         * 隐藏底部预览视图
         */
        void dismissBottomPreview();

        /**
         * 让图片预览滚动到指定位置
         *
         * @param position the position for scroll to.
         */
        void previewPicturesSmoothScrollToPosition(int position);

        /**
         * 处理 View 的 finish 操作
         */
        void finish();

        /**
         * 设置返回值在 finish 之前
         *
         * @param pickedPaths     用户选中的图片
         * @param isEnsurePressed 是否点击了确认按钮
         */
        void setResultBeforeFinish(@Nullable ArrayList<MediaMeta> pickedPaths, boolean isEnsurePressed);

        /**
         * 展示共享元素入场动画
         *
         * @param mediaMeta   共享元素位置的数据
         * @param elementData shared element data.
         */
        void showSharedElementEnter(MediaMeta mediaMeta, SharedElementModel elementData);

        void showSharedElementExitAndFinish(@NonNull SharedElementModel sharedElementModel);
    }


    interface IPresenter {

        /**
         * 处理页面的滑动
         */
        void handlePagerChanged(int position);

        /**
         * 处理确认按钮点击
         */
        void handleEnsureClick();

        /**
         * 处理 Toolbar 上索引的点击
         */
        void handleToolbarCheckedIndicatorClick(boolean checked);

        /**
         * 处理选中的条目被点击了
         */
        void handlePickedItemClicked(MediaMeta pickedUri);

        /**
         * 处理返回事件
         */
        void handleBackPressed();

        /**
         * 处理 View 的 finish
         */
        void handleSetResultBeforeFinish();

    }
}