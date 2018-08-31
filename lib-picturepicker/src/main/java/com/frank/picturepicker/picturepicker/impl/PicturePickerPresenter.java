package com.frank.picturepicker.picturepicker.impl;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ImageView;

import com.frank.picturepicker.R;
import com.frank.picturepicker.picturepicker.manager.PickerConfig;
import com.frank.picturepicker.picturepicker.manager.PicturePickerFragment;
import com.frank.picturepicker.picturetake.manager.PictureTakeManager;
import com.frank.picturepicker.picturetake.manager.TakeCallback;
import com.frank.picturepicker.picturewatcher.manager.PictureWatcherManager;
import com.frank.picturepicker.picturewatcher.manager.WatcherCallback;
import com.frank.picturepicker.pricturecrop.manager.CropCallback;
import com.frank.picturepicker.pricturecrop.manager.PictureCropManager;
import com.frank.picturepicker.support.loader.PictureLoader;

import java.text.MessageFormat;
import java.util.ArrayList;


/**
 * MVP frame presenter associated with PicturePicker.
 *
 * @author Frank <a href="frankchoochina@gmail.com">Contact me.</a>
 * @version 1.3
 * @since 2018/9/1 10:17
 */
class PicturePickerPresenter implements PicturePickerContract.IPresenter, TakeCallback, CropCallback, WatcherCallback {

    private static final String TAG = PicturePickerPresenter.class.getSimpleName();
    private final PicturePickerContract.IView mView;                          // View associated with this presenter.
    private PicturePickerModel mModel;                                        // Model associated with this presenter.
    private PickerConfig mConfig;                                             // Config associated with the PicturePicker.

    PicturePickerPresenter(PicturePickerContract.IView view) {
        this.mView = view;
    }

    @Override
    public void start(Context context, PickerConfig config) {
        this.mConfig = config;
        // 初始化 Model
        this.mModel = new PicturePickerModel(mConfig.userPickedSet == null ? new ArrayList<String>()
                : mConfig.userPickedSet, mConfig.threshold);
        // 配置 UI 视图
        mView.setToolbarScrollable(mConfig.isShowScrollBehavior);
        mView.switchFabVisibility(mConfig.isShowScrollBehavior);
        if (mConfig.toolbarBkgColor != PickerConfig.INVALIDATE_VALUE) {
            mView.setToolbarBackgroundColor(mConfig.toolbarBkgColor);
            mView.setFabColor(mConfig.toolbarBkgColor);
        }
        if (mConfig.toolbarBkgDrawableResId != PickerConfig.INVALIDATE_VALUE) {
            mView.setToolbarBackgroundDrawable(mConfig.toolbarBkgDrawableResId);
        }
        if (mConfig.pickerBackgroundColor != PickerConfig.INVALIDATE_VALUE) {
            mView.setBackgroundColor(mConfig.pickerBackgroundColor);
        }
        // 设置 RecyclerView 的列数
        mView.setSpanCount(mConfig.spanCount);
        // 设置 RecyclerView 的Adapter
        mView.setAdapter(mConfig, mModel.getDisplayPaths(), mModel.getPickedPaths());
        // 获取图片数据
        mModel.getSystemPictures(context, new PicturePickerContract.IModel.Callback() {

            private final Handler handler = new Handler(Looper.getMainLooper());

            @Override
            public void onComplete() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        handleFolderChecked(0);
                    }
                });
            }

            @Override
            public void onFailed(Throwable throwable) {
                Log.e(TAG, throwable.getMessage(), throwable);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        mView.showMsg(mView.getString(R.string.libpicturepicker_picturepicker_tips_fetch_album_failed));
                    }
                });
            }

        });
    }

    @Override
    public boolean handlePictureChecked(String path) {
        boolean result = isCanPickedPicture(true);
        if (result) {
            mModel.addPickedPicture(path);
            mView.setToolbarEnsureText(buildEnsureText());
            mView.setPreviewText(buildPreviewText());
        }
        return result;
    }

    @Override
    public void handlePictureRemoved(String path) {
        mModel.removePickedPicture(path);
        mView.setToolbarEnsureText(buildEnsureText());
        mView.setPreviewText(buildPreviewText());
    }

    @Override
    public void handleBottomMenuClicked() {
        mView.showBottomMenuDialog(mModel.getAllFolders());
    }

    @Override
    public void handleCameraClicked() {
        PictureTakeManager.with((Context) mView)
                .setFileProviderAuthority(mConfig.authority)
                .setCameraDirectory(mConfig.cameraDirectoryPath)
                .setCameraQuality(mConfig.cameraQuality)
                .take(this);
    }

    @Override
    public void handlePictureClicked(int position, ImageView sharedElement) {
        PictureWatcherManager.with((Context) mView)
                .setThreshold(mConfig.threshold)
                .setIndicatorTextColor(mConfig.indicatorTextColor)
                .setIndicatorSolidColor(mConfig.indicatorSolidColor)
                .setIndicatorBorderColor(mConfig.indicatorBorderCheckedColor,
                        mConfig.indicatorBorderUncheckedColor)
                .setPictureUris(mModel.getDisplayPaths(), position)
                .setUserPickedSet(mModel.getPickedPaths())
                .setSharedElement(sharedElement)
                .setPictureLoader(PictureLoader.getPictureLoader())
                .start(this);
    }

    @Override
    public void handlePreviewClicked() {
        if (!isCanPreview()) return;
        PictureWatcherManager.with((Context) mView)
                .setThreshold(mConfig.threshold)
                .setIndicatorTextColor(mConfig.indicatorTextColor)
                .setIndicatorSolidColor(mConfig.indicatorSolidColor)
                .setIndicatorBorderColor(mConfig.indicatorBorderCheckedColor, mConfig.indicatorBorderUncheckedColor)
                .setPictureUris(mModel.getPickedPaths(), 0)
                .setUserPickedSet(mModel.getPickedPaths())
                .setPictureLoader(PictureLoader.getPictureLoader())
                .start(this);
    }

    @Override
    public void handleEnsureClicked() {
        if (!isCanEnsure()) return;
        // 不需要裁剪, 直接返回
        if (!mConfig.isCropSupport) {
            performUserPickedSetResult();
            return;
        }
        // 需要裁剪, 则启动裁剪
        PictureCropManager.with((Context) mView)
                .setFileProviderAuthority(mConfig.authority)
                .setCropSize(mConfig.cropWidth, mConfig.cropHeight)
                .setCropCircle(mConfig.isCropCircle)
                // 启动裁剪了只能选择一张图片
                .setOriginFile(mModel.getPickedPaths().get(0))
                // 裁剪后存储的文件路径
                .setCropDirectory(mConfig.cropDirectoryPath)
                // 裁剪后压缩的质量
                .setCropQuality(mConfig.cropQuality)
                .crop(this);
    }

    @Override
    public void handleFolderChecked(int position) {
        performDisplayCheckedFolder(position);
    }

    @Override
    public void onWatcherPickedComplete(boolean isEnsure, ArrayList<String> userPickedSet) {
        // 刷新用户选中的集合
        mModel.getPickedPaths().clear();
        mModel.getPickedPaths().addAll(userPickedSet);
        if (mView == null) return;
        // 展示标题和预览文本
        mView.setToolbarEnsureText(buildEnsureText());
        mView.setPreviewText(buildPreviewText());
        if (isEnsure) {
            handleEnsureClicked();// 执行确认事件
        } else {
            mView.notifyPickedPathsChanged();// 通知更新
        }
    }

    @Override
    public void onTakeComplete(String path) {
        // 1. 添加到 <当前展示> 的文件夹下
        PictureFolder checkedFolder = mModel.getCheckedFolder();
        checkedFolder.getPicturePaths().add(0, path);
        // 2. 添加到 <所有文件> 的文件夹下
        PictureFolder allPictureFolder = mModel.getPictureFolderAt(0);
        if (allPictureFolder != checkedFolder) {
            allPictureFolder.getPicturePaths().add(0, path);
        }
        // 3. 更新展示的图片集合
        mModel.getDisplayPaths().add(0, path);
        // 3.1 判断是否可以继续选择
        if (isCanPickedPicture(false)) {
            mModel.addPickedPicture(path);// 添加到选中的集合中
            mView.setToolbarEnsureText(buildEnsureText());
            mView.setPreviewText(buildPreviewText());
        }
        // 3.2 通知 UI 更新视图
        mView.notifyDisplayPathsInsertToFirst();
    }

    @Override
    public void onCropComplete(String path) {
        mModel.getPickedPaths().clear();
        mModel.getPickedPaths().add(path);
        performUserPickedSetResult();
    }

    /**
     * 执行展示文件夹的操作
     */
    private void performDisplayCheckedFolder(int position) {
        // Get display folder at position.
        PictureFolder curDisplayFolder = mModel.getPictureFolderAt(position);
        mModel.setCheckedFolder(curDisplayFolder);
        // Set folder text associated with view.
        mView.setPictureFolderText(curDisplayFolder.getFolderName());
        // Set ensure text associated with view toolbar.
        mView.setToolbarEnsureText(buildEnsureText());
        // Set preview text associated with view.
        mView.setPreviewText(buildPreviewText());
        // Notify view displays paths changed.
        mView.notifyDisplayPathsChanged();
    }

    /**
     * 处理图片选择完成了
     */
    private void performUserPickedSetResult() {
        if (mView != null && mView instanceof Activity) {
            Activity bind = (Activity) mView;
            Intent intent = new Intent();
            intent.putExtra(PicturePickerActivity.RESULT_INTENT_EXTRA_PICKED_PICTURES, mModel.getPickedPaths());
            bind.setResult(PicturePickerFragment.REQUEST_CODE_PICKED, intent);
            bind.finish();
        }
    }

    /**
     * 是否可以继续选择图片
     *
     * @param isShowFailedMsg 是否提示失败原因
     * @return true is can picked, false is cannot picked.
     */
    private boolean isCanPickedPicture(boolean isShowFailedMsg) {
        if (mModel.getPickedPaths().size() == mConfig.threshold && mView != null) {
            if (isShowFailedMsg) {
                mView.showMsg(mView.getString(R.string.libpicturepicker_picturepicker_tips_over_threshold_prefix)
                        + mConfig.threshold
                        + mView.getString(R.string.libpicturepicker_picturepicker_tips_over_threshold_suffix)
                );
            }
            return false;
        }
        return true;
    }

    /**
     * 是否可以启动图片预览
     *
     * @return true is can launch, false is cannot launch.
     */
    private boolean isCanPreview() {
        if (mModel.getPickedPaths().size() == 0 && mView != null) {
            mView.showMsg(mView.getString(R.string.libpicturepicker_picturepicker_tips_preview_failed));
            return false;
        }
        return true;
    }

    /**
     * 是否可以发起确认请求
     *
     * @return true is can ensure, false is cannot ensure.
     */
    private boolean isCanEnsure() {
        if (mModel.getPickedPaths().size() == 0 && mView != null) {
            mView.showMsg(mView.getString(R.string.libpicturepicker_picturepicker_tips_ensure_failed));
            return false;
        }
        return true;
    }

    /**
     * 构建标题确认文本
     */
    private CharSequence buildEnsureText() {
        return MessageFormat.format(
                "{0} ({1}/{2})",
                mView.getString(R.string.libpicturepicker_picturepicker_ensure),
                mModel.getPickedPaths().size(),
                mConfig.threshold
        );
    }

    /**
     * 构建预览文本
     */
    private CharSequence buildPreviewText() {
        return MessageFormat.format(
                "{0} ({1})",
                mView.getString(R.string.libpicturepicker_picturepicker_preview),
                mModel.getPickedPaths().size()
        );
    }
}
