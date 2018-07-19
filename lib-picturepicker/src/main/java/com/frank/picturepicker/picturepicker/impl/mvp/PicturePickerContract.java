package com.frank.picturepicker.picturepicker.impl.mvp;

import android.content.Context;
import android.os.Parcelable;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.widget.ImageView;

import com.frank.picturepicker.picturepicker.impl.data.PictureFolder;
import com.frank.picturepicker.picturepicker.manager.PickerConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Frank on 2018/6/13.
 * Email: frankchoochina@gmail.com
 * Version: 1.0
 * Description: PicturePicture MVP 的约束
 */
public interface PicturePickerContract {

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
         * 显示选中的图片文件夹
         *
         * @param folderName 文件夹的名称
         * @param uris       文件夹中的数据
         */
        void displayPictures(String folderName, List<String> uris);

        /**
         * 展示 Toolbar 确认文本
         */
        void displayToolbarEnsureText(CharSequence content);

        /**
         * 展示预览文本
         */
        void displayPreviewText(CharSequence content);

        /**
         * 通知选中的图片集合变更了
         */
        void notifyUserPickedSetChanged();

        /**
         * 通过相机拍摄了一张照片
         *
         * @param path
         */
        void notifyCameraTakeOnePicture(String path);

        /**
         * 展示消息通知
         */
        void showMsg(String msg);

        /**
         * 从资源文件获取 String
         */
        String getString(@StringRes int resId);
    }

    interface IPresenter {

        /**
         * 绑定 View
         */
        void attach(IView view);

        /**
         * 初始化
         */
        void init(PickerConfig config);

        /**
         * 初始化 Model 的数据
         */
        void fetchData(Context context);

        /**
         * 获取需要展示的图片
         */
        void fetchDisplayPictures(int position);

        /**
         * 获取所有图片文件夹
         */
        ArrayList<PictureFolder> fetchAllPictureFolders();

        /**
         * 获取用户选中的所有图片
         */
        ArrayList<String> fetchUserPickedSet();

        /**
         * 处理图片被选中了
         */
        boolean performPictureChecked(String imagePath);

        /**
         * 处理图片被移除了
         */
        void performPictureUnchecked(String imagePath);

        /**
         * 处理图片被点击了
         */
        void performPictureClicked(ArrayList<String> curPaths, int position, ImageView sharedElement);

        /**
         * 处理预览按钮被点击了
         */
        void performPreviewClicked();

        /**
         * 处理底部菜单被点击了
         */
        void performBottomMenuClicked();

        /**
         * 确认按钮被点击了
         */
        void performEnsureClicked();

        /**
         * 相机按钮被点击了
         */
        void performCameraClicked();

        /**
         * 获取配置信息
         */
        PickerConfig getConfig();
    }

    interface IModel {

        /**
         * 设置用户已经选中的图片集合
         */
        void setUserPickedSet(ArrayList<String> userPicked);

        /**
         * 获取系统图片
         */
        void getSystemPictures(Context context, final ModelInitializeCallback listener);

        /**
         * 设置图片选择的阈值
         */
        void setThreshold(int threshold);

        /**
         * 获取图片选择的阈值
         */
        int getThreshold();

        /**
         * 获取当前需要显示的文件模型
         */
        PictureFolder getPictureFolderAt(int index);

        /**
         * 获取所有的图片文件夹
         */
        ArrayList<PictureFolder> getAllPictureFolders();

        /**
         * 获取当前正在展示的图片集合
         */
        PictureFolder getCurDisplayFolder();

        /**
         * 获取用户选中的图片
         */
        ArrayList<String> getUserPickedSet();

        /**
         * 添加用户选中的图片
         */
        void addPickedPicture(String imagePath);

        /**
         * 移除用户选中的图片
         */
        void removePickedPicture(String imagePath);
    }

    interface ModelInitializeCallback {
        void onComplete(List<PictureFolder> pictureFolders);

        void onFailed(Throwable throwable);
    }
}
