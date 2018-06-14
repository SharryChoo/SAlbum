package com.frank.lib_picturepicker.picturepicker.mvp;

import android.content.Context;

import com.frank.lib_picturepicker.picturepicker.data.PictureFolder;

import java.util.List;

/**
 * Created by Frank on 2018/6/13.
 * Email: frankchoochina@gmail.com
 * Version: 1.0
 * Description:
 */
public interface PicturePickerContract {

    interface IView {

        /**
         * 显示选中的图片文件夹
         *
         * @param folderName
         * @param uris
         */
        void displaySelectedFolder(String folderName, List<String> uris);

        /**
         * 更新标题文本
         */
        void updateTextContent(int curPicked, int total);

        /**
         * 展示消息通知
         *
         * @param msg
         */
        void showMsg(String msg);

        /**
         * 跟新预览文本的可见性(选中图片大于 0 时, 即为可见)
         *
         * @param isVisible
         */
        void updateTextViewVisibility(boolean isVisible);
    }

    interface IPresenter {

        void attach(IView view);

        /**
         * 添加用户跳转到 View 时携带的图片地址集合
         *
         * @param userPicked
         */
        void setupUserPicked(List<String> userPicked);

        /**
         * 配置阈值
         *
         * @param threshold
         */
        void setupThreshold(int threshold);

        /**
         * 初始化 Model 的数据
         *
         * @param context
         */
        void initData(Context context);

        /**
         * 获取 <<指定索引处的>> 图片文件夹
         *
         * @param position
         */
        void fetchPicturePathsAt(int position);

        /**
         * 获取所有图片文件夹
         */
        List<PictureFolder> fetchAllPictureFolders();

        /**
         * 获取用户选中的所有图片
         *
         * @return
         */
        List<String> getPickedPictures();

        /**
         * 处理图片被选中了
         *
         * @param imagePath
         */
        boolean performPicturePicked(String imagePath);

        /**
         * 处理图片被移除了
         *
         * @param imagePath
         */
        void performPictureRemoved(String imagePath);

    }

    interface IModel {

        void init(Context context, final ModelInitializeCallback listener);

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
        List<PictureFolder> getAllPictureFolders();

        /**
         * 获取用户选中的图片
         *
         * @return
         */
        List<String> getPickedPictures();

        /**
         * 添加用户选中的图片
         *
         * @param imagePath
         */
        void addPickedPicture(String imagePath);

        /**
         * 移除用户选中的图片
         *
         * @param imagePath
         */
        void removePickedPicture(String imagePath);
    }

    interface ModelInitializeCallback {
        void onComplete(List<PictureFolder> pictureFolders);

        void onFailed(Throwable throwable);
    }
}
