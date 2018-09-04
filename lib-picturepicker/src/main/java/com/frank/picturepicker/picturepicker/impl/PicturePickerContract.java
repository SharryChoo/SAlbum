package com.frank.picturepicker.picturepicker.impl;

import android.content.Context;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;
import android.widget.ImageView;

import com.frank.picturepicker.picturepicker.manager.PickerConfig;

import java.util.ArrayList;

/**
 * Created by Frank on 2018/6/13.
 * Email: frankchoochina@gmail.com
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
        void setBackgroundColor(int color);

        /**
         * 设置 RecyclerView 的列数
         */
        void setSpanCount(int spanCount);

        /**
         * 设置 RecyclerView 的 Adapter
         *
         * @param config
         * @param displayPaths
         * @param userPickedPaths
         */
        void setAdapter(PickerConfig config, ArrayList<String> displayPaths, ArrayList<String> userPickedPaths);

        /**
         * 显示选中的图片文件夹
         *
         * @param folderName 文件夹的名称
         */
        void setPictureFolderText(String folderName);

        /**
         * 展示 Toolbar 确认文本
         */
        void setToolbarEnsureText(CharSequence content);

        /**
         * 展示预览文本
         */
        void setPreviewText(CharSequence content);

        /**
         * 通知选中的图片集合变更了
         */
        void notifyPickedPathsChanged();

        /**
         * 通知需要展示的集合变更了
         */
        void notifyDisplayPathsChanged();

        /**
         * 通过相机拍摄了一张照片
         */
        void notifyDisplayPathsInsertToFirst();

        /**
         * 展示消息通知
         */
        void showMsg(String msg);

        /**
         * 展示底部文件夹选择的 Dialog
         */
        void showBottomMenuDialog(ArrayList<PictureFolder> allPictureFolders);

        /**
         * 从资源文件获取 String
         */
        String getString(@StringRes int resId);
    }

    interface IPresenter {

        /**
         * 初始化 Model 的数据
         */
        void start(Context context, PickerConfig config);

        /**
         * 处理图片被选中了
         */
        boolean handlePictureChecked(String imagePath);

        /**
         * 处理图片被移除了
         */
        void handlePictureRemoved(String imagePath);

        /**
         * 处理图片被点击了
         */
        void handlePictureClicked(int position, ImageView sharedElement);

        /**
         * 处理预览按钮被点击了
         */
        void handlePreviewClicked();

        /**
         * 处理底部菜单被点击了
         */
        void handleBottomMenuClicked();

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
         * 获取用户选中的图片
         */
        ArrayList<String> getDisplayPaths();

        /**
         * 添加用户选中的图片
         */
        void addPickedPicture(String imagePath);

        /**
         * 移除用户选中的图片
         */
        void removePickedPicture(String imagePath);
    }


}
