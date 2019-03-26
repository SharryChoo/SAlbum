package com.sharry.picturepicker.picker;

import android.content.Context;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.widget.ImageView;

import java.util.ArrayList;

/**
 * Created by Sharry on 2018/6/13.
 * Email: SharryChooCHN@Gmail.com
 * Version: 1.0
 * Description: PicturePicture MVP 的约束
 */
interface PicturePickerContract {

    interface IView {

        /**
         * 设置 Toolbar 是否为可滚动的类型
         */
        void setToolbarScrollable(boolean isScrollable);

        /**
         * 设置 Toolbar 背景色
         */
        void setToolbarBackgroundColor(int color);

        /**
         * 设置 Toolbar 背景图片
         */
        void setToolbarBackgroundDrawable(@DrawableRes int drawableId);

        /**
         * 设置悬浮按钮的颜色
         */
        void setFabColor(int color);

        /**
         * 设置悬浮按钮的可视性
         */
        void switchFabVisibility(boolean isVisible);

        /**
         * 设置整体的背景色
         */
        void setPicturesBackgroundColor(int color);

        /**
         * 设置 RecyclerView 的列数
         */
        void setPicturesSpanCount(int spanCount);

        /**
         * 设置图片的 Adapter
         */
        void setPicturesAdapter(@NonNull PickerConfig config, @NonNull ArrayList<String> displayPaths,
                                @NonNull ArrayList<String> userPickedPaths);

        /**
         * 设置图片文件夹的 Adapter
         */
        void setFolderAdapter(@NonNull ArrayList<PictureFolder> allFolders);

        /**
         * 显示选中的图片文件夹
         *
         * @param folderName 文件夹的名称
         */
        void setPictureFolderText(@NonNull String folderName);

        /**
         * 展示 Toolbar 确认文本
         */
        void setToolbarEnsureText(@NonNull CharSequence content);

        /**
         * 展示预览文本
         */
        void setPreviewText(@NonNull CharSequence content);

        /**
         * 通知选中的图片集合变更了
         */
        void notifyPickedPathsChanged();

        /**
         * 通知需要展示的集合变更了
         */
        void notifyDisplayPathsChanged();

        /**
         * 通知文件夹菜单数据变更了
         */
        void notifyFolderDataSetChanged();

        /**
         * 通过相机拍摄了一张照片
         */
        void notifyDisplayPathsInsertToFirst();

        /**
         * 展示消息通知
         */
        void showMsg(@NonNull String msg);

        /**
         * 从资源文件获取 String
         */
        String getString(@StringRes int resId);

        /**
         * 设置返回值
         */
        void setResult(@NonNull ArrayList<String> pickedPaths);
    }

    interface IPresenter {

        /**
         * 处理图片被选中了
         */
        boolean handlePictureChecked(@Nullable String imagePath);

        /**
         * 处理图片被移除了
         */
        void handlePictureRemoved(@Nullable String imagePath);

        /**
         * 处理图片被点击了
         */
        void handlePictureClicked(int position, @Nullable ImageView sharedElement);

        /**
         * 处理预览按钮被点击了
         */
        void handlePreviewClicked();

        /**
         * 处理图片文件夹被选中了
         */
        void handleFolderChecked(int position);

        /**
         * 确认按钮被点击了
         */
        void handleEnsureClicked();

        /**
         * 相机按钮被点击了
         */
        void handleCameraClicked();
    }

    interface IModel {

        interface Callback {

            void onComplete();

            void onFailed(Throwable throwable);


        }

        /**
         * 获取系统图片
         */
        void getSystemPictures(Context context, final Callback listener);

        /**
         * 获取当前需要显示的文件模型
         */
        PictureFolder getPictureFolderAt(int index);

        /**
         * 获取所有的图片文件夹
         */
        ArrayList<PictureFolder> getAllFolders();

        /**
         * 获取当前正在展示的图片集合
         */
        PictureFolder getCheckedFolder();

        /**
         * 获取用户选中的图片
         */
        ArrayList<String> getPickedPaths();

        /**
         * 设置当前选中的文件夹
         */
        void setCheckedFolder(PictureFolder curDisplayFolder);

        /**
         * 获取用户选中的图片
         */
        ArrayList<String> getDisplayPaths();

        /**
         * 添加用户选中的图片
         */
        void addPickedPicture(@NonNull String imagePath);

        /**
         * 移除用户选中的图片
         */
        void removePickedPicture(@NonNull String imagePath);
    }

}
