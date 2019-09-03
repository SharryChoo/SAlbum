package com.sharry.lib.picturepicker;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import java.text.MessageFormat;
import java.util.ArrayList;


/**
 * MVP frame presenter associated with PicturePicker.
 *
 * @author Sharry <a href="SharryChooCHN@Gmail.com">Contact me.</a>
 * @version 1.3
 * @since 2018/9/1 10:17
 */
class PickerPresenter implements PickerContract.IPresenter, TakerCallback, CropperCallback, WatcherCallback {

    private static final String TAG = PickerPresenter.class.getSimpleName();

    /**
     * View associated with this presenter.
     */
    private final PickerContract.IView mView;
    /**
     * Model associated with this presenter.
     */
    private final PickerContract.IModel mModel;
    /**
     * Config associated with the PicturePicker.
     */
    private final PickerConfig mPickerConfig;
    /**
     * Config associated with the PictureWatcher.
     */
    private final WatcherConfig mWatcherConfig;

    PickerPresenter(@NonNull PickerContract.IView view,
                    @NonNull Context context, @NonNull PickerConfig config) {
        this.mView = view;
        this.mPickerConfig = config;
        this.mModel = new PickerModel(mPickerConfig.getUserPickedSet(), mPickerConfig.getThreshold());
        this.mWatcherConfig = WatcherConfig.Builder()
                .setThreshold(mPickerConfig.getThreshold())
                .setIndicatorTextColor(mPickerConfig.getIndicatorTextColor())
                .setIndicatorSolidColor(mPickerConfig.getIndicatorSolidColor())
                .setIndicatorBorderColor(
                        mPickerConfig.getIndicatorBorderCheckedColor(),
                        mPickerConfig.getIndicatorBorderUncheckedColor()
                )
                .setUserPickedSet(mModel.getPickedPaths())
                .build();
        setupView();
        fetchData(context);
    }

    @Override
    public boolean handlePictureChecked(MediaMeta checkedMeta) {
        boolean result = isCanPickedPicture(true);
        if (result) {
            mModel.addPicked(checkedMeta);
            mView.setToolbarEnsureText(buildEnsureText());
            mView.setPreviewText(buildPreviewText());
        }
        return result;
    }

    @Override
    public void handlePictureRemoved(MediaMeta removedMeta) {
        mModel.removePicked(removedMeta);
        mView.setToolbarEnsureText(buildEnsureText());
        mView.setPreviewText(buildPreviewText());
    }

    @Override
    public void handleCameraClicked() {
        // 这里可以确保 TakerConfig 不为 null
        if (mPickerConfig.getTakerConfig() != null) {
            TakerManager.with((Context) mView)
                    .setConfig(
                            mPickerConfig.getTakerConfig().rebuild()
                                    .setCropConfig(mPickerConfig.getCropperConfig())
                                    .build()
                    )
                    .take(this);
        }
    }

    @Override
    public void handlePictureClicked(int position, ImageView sharedElement) {
        WatcherManager.with((Context) mView)
                .setSharedElement(sharedElement)
                .setPictureLoader(PictureLoader.getPictureLoader())
                .setConfig(
                        mWatcherConfig.rebuild()
                                .setPictureUris(mModel.getDisplayPaths(), position)
                                .build()
                )
                .startForResult(this);
    }

    @Override
    public void handlePreviewClicked() {
        if (!isCanPreview()) {
            return;
        }
        WatcherManager.with((Context) mView)
                .setPictureLoader(PictureLoader.getPictureLoader())
                .setConfig(
                        mWatcherConfig.rebuild()
                                .setPictureUris(mModel.getPickedPaths(), 0)
                                .build()
                )
                .startForResult(this);
    }

    @Override
    public void handleEnsureClicked() {
        if (!isCanEnsure()) {
            return;
        }
        // 不需要裁剪, 直接返回
        if (!mPickerConfig.isCropSupport()) {
            mView.setResult(mModel.getPickedPaths());
            return;
        }
        // 需要裁剪, 则启动裁剪
        CropperManager.with((Context) mView)
                .setConfig(
                        mPickerConfig.getCropperConfig().rebuild()
                                .setOriginFile(mModel.getPickedPaths().get(0).path)
                                .build()
                )
                .crop(this);
    }

    @Override
    public void handleFolderChecked(int position) {
        performDisplayCheckedFolder(position);
    }

    @Override
    public void onWatcherPickedComplete(boolean isEnsure, ArrayList<MediaMeta> pickedMetas) {
        // 刷新用户选中的集合
        mModel.getPickedPaths().clear();
        mModel.getPickedPaths().addAll(pickedMetas);
        if (mView == null) {
            return;
        }
        // 展示标题和预览文本
        mView.setToolbarEnsureText(buildEnsureText());
        mView.setPreviewText(buildPreviewText());
        if (isEnsure) {
            // 执行确认事件
            handleEnsureClicked();
        } else {
            // 通知更新
            mView.notifyPickedPathsChanged();
        }
    }

    @Override
    public void onCameraTakeComplete(@NonNull MediaMeta newMeta) {
        // 1. 添加到 <当前展示> 的文件夹下
        FolderModel checkedFolder = mModel.getCheckedFolder();
        checkedFolder.getMetas().add(0, newMeta);
        // 2. 添加到 <所有文件> 的文件夹下
        FolderModel allFolderModel = mModel.getPictureFolderAt(0);
        if (allFolderModel != checkedFolder) {
            allFolderModel.getMetas().add(0, newMeta);
        }
        // 3. 更新展示的图片集合
        mModel.getDisplayPaths().add(0, newMeta);
        // 3.1 判断是否可以继续选择
        if (isCanPickedPicture(false)) {
            mModel.addPicked(newMeta);
            mView.setToolbarEnsureText(buildEnsureText());
            mView.setPreviewText(buildPreviewText());
        }
        // 3.2 通知 UI 更新视图
        mView.notifyDisplayPathsInsertToFirst();
        mView.notifyFolderDataSetChanged();
    }

    @Override
    public void onCropComplete(@NonNull String path) {
        mModel.getPickedPaths().clear();
        mModel.getPickedPaths().add(MediaMeta.create(path, true));
        mView.setResult(mModel.getPickedPaths());
    }

    private void setupView() {
        // 配置 UI 视图
        mView.setToolbarScrollable(mPickerConfig.isToolbarBehavior());
        mView.switchFabVisibility(mPickerConfig.isFabBehavior());
        if (mPickerConfig.getToolbarBkgColor() != PickerConfig.INVALIDATE_VALUE) {
            mView.setToolbarBackgroundColor(mPickerConfig.getToolbarBkgColor());
            mView.setFabColor(mPickerConfig.getToolbarBkgColor());
        }
        if (mPickerConfig.getToolbarBkgDrawableResId() != PickerConfig.INVALIDATE_VALUE) {
            mView.setToolbarBackgroundDrawable(mPickerConfig.getToolbarBkgDrawableResId());
        }
        if (mPickerConfig.getPickerBackgroundColor() != PickerConfig.INVALIDATE_VALUE) {
            mView.setPicturesBackgroundColor(mPickerConfig.getPickerBackgroundColor());
        }
        // 设置图片的列数
        mView.setPicturesSpanCount(mPickerConfig.getSpanCount());
        // 设置 RecyclerView 的 Adapter
        mView.setPicturesAdapter(mPickerConfig, mModel.getDisplayPaths(), mModel.getPickedPaths());
    }

    private void fetchData(Context context) {
        mView.setProgressBarVisible(true);
        mModel.getSystemPictures(context, new PickerContract.IModel.Callback() {

            private final Handler mainHandler = new Handler(Looper.getMainLooper());

            @Override
            public void onComplete() {
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mView.setProgressBarVisible(false);
                        mView.setFolderAdapter(mModel.getAllFolders());
                        handleFolderChecked(0);
                    }
                });
            }

            @Override
            public void onFailed(Throwable throwable) {
                Log.e(TAG, throwable.getMessage(), throwable);
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mView.setProgressBarVisible(false);
                        mView.showMsg(mView.getString(R.string.picture_picker_picker_tips_fetch_album_failed));
                    }
                });
            }
        });
    }

    /**
     * 执行展示文件夹的操作
     */
    private void performDisplayCheckedFolder(int position) {
        // Get display folder at position.
        FolderModel curDisplayFolder = mModel.getPictureFolderAt(position);
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
     * 是否可以继续选择图片
     *
     * @param isShowFailedMsg 是否提示失败原因
     * @return true is can picked, false is cannot picked.
     */
    private boolean isCanPickedPicture(boolean isShowFailedMsg) {
        if (mModel.getPickedPaths().size() == mPickerConfig.getThreshold() && mView != null) {
            if (isShowFailedMsg) {
                mView.showMsg(mView.getString(R.string.picture_picker_picker_tips_over_threshold_prefix)
                        + mPickerConfig.getThreshold()
                        + mView.getString(R.string.picture_picker_picker_tips_over_threshold_suffix)
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
            mView.showMsg(mView.getString(R.string.picture_picker_picker_tips_preview_failed));
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
            mView.showMsg(mView.getString(R.string.picture_picker_picker_tips_ensure_failed));
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
                mView.getString(R.string.picture_picker_picker_ensure),
                mModel.getPickedPaths().size(),
                mPickerConfig.getThreshold()
        );
    }

    /**
     * 构建预览文本
     */
    private CharSequence buildPreviewText() {
        return MessageFormat.format(
                "{0} ({1})",
                mView.getString(R.string.picture_picker_picker_preview),
                mModel.getPickedPaths().size()
        );
    }
}
