package com.sharry.picturepicker.picker;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.ImageView;

import com.sharry.picturepicker.R;
import com.sharry.picturepicker.camera.CameraCallback;
import com.sharry.picturepicker.camera.CameraRequestManager;
import com.sharry.picturepicker.crop.CropCallback;
import com.sharry.picturepicker.crop.PictureCropManager;
import com.sharry.picturepicker.support.loader.PictureLoader;
import com.sharry.picturepicker.watcher.PictureWatcherManager;
import com.sharry.picturepicker.watcher.WatcherCallback;
import com.sharry.picturepicker.watcher.WatcherConfig;

import java.text.MessageFormat;
import java.util.ArrayList;


/**
 * MVP frame presenter associated with PicturePicker.
 *
 * @author Sharry <a href="SharryChooCHN@Gmail.com">Contact me.</a>
 * @version 1.3
 * @since 2018/9/1 10:17
 */
class PicturePickerPresenter implements PicturePickerContract.IPresenter, CameraCallback, CropCallback, WatcherCallback {

    private static final String TAG = PicturePickerPresenter.class.getSimpleName();
    private final PicturePickerContract.IView mView;                                                // View associated with this presenter.
    private final PicturePickerContract.IModel mModel;                                              // Model associated with this presenter.
    private final PickerConfig mPickerConfig;                                                       // Config associated with the PicturePicker.
    private final WatcherConfig mWatcherConfig;                                                     // Config associated with the PictureWatcher.

    PicturePickerPresenter(@NonNull PicturePickerContract.IView view,
                           @NonNull Context context, @NonNull PickerConfig config) {
        this.mView = view;
        this.mPickerConfig = config;
        this.mModel = new PicturePickerModel(mPickerConfig.getUserPickedSet(), mPickerConfig.getThreshold());
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
        initView();
        initModel(context);
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
    public void handleCameraClicked() {
        // 这里可以确保 CameraConfig 不为 null
        CameraRequestManager.with((Context) mView)
                .setConfig(mPickerConfig.getCameraConfig())
                .take(this);
    }

    @Override
    public void handlePictureClicked(int position, ImageView sharedElement) {
        PictureWatcherManager.with((Context) mView)
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
        PictureWatcherManager.with((Context) mView)
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
        PictureCropManager.with((Context) mView)
                .setConfig(
                        mPickerConfig.getCropConfig().rebuild()
                                .setOriginFile(mModel.getPickedPaths().get(0))
                                .build()
                )
                .crop(this);
    }

    @Override
    public void handleFolderChecked(int position) {
        performDisplayCheckedFolder(position);
    }

    @Override
    public void onWatcherPickedComplete(boolean isEnsure, ArrayList<String> pickedPictures) {
        // 刷新用户选中的集合
        mModel.getPickedPaths().clear();
        mModel.getPickedPaths().addAll(pickedPictures);
        if (mView == null) {
            return;
        }
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
    public void onCameraTakeComplete(String path) {
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
        mView.notifyFolderDataSetChanged();
    }

    @Override
    public void onCropComplete(String path) {
        mModel.getPickedPaths().clear();
        mModel.getPickedPaths().add(path);
        mView.setResult(mModel.getPickedPaths());
    }

    private void initView() {
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

    private void initModel(Context context) {
        // 获取图片数据
        mModel.getSystemPictures(context, new PicturePickerContract.IModel.Callback() {

            private final Handler handler = new Handler(Looper.getMainLooper());

            @Override
            public void onComplete() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        mView.setFolderAdapter(mModel.getAllFolders());
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
                        mView.showMsg(mView.getString(R.string.libpicturepicker_picker_tips_fetch_album_failed));
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
     * 是否可以继续选择图片
     *
     * @param isShowFailedMsg 是否提示失败原因
     * @return true is can picked, false is cannot picked.
     */
    private boolean isCanPickedPicture(boolean isShowFailedMsg) {
        if (mModel.getPickedPaths().size() == mPickerConfig.getThreshold() && mView != null) {
            if (isShowFailedMsg) {
                mView.showMsg(mView.getString(R.string.libpicturepicker_picker_tips_over_threshold_prefix)
                        + mPickerConfig.getThreshold()
                        + mView.getString(R.string.libpicturepicker_picker_tips_over_threshold_suffix)
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
            mView.showMsg(mView.getString(R.string.libpicturepicker_picker_tips_preview_failed));
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
            mView.showMsg(mView.getString(R.string.libpicturepicker_picker_tips_ensure_failed));
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
                mView.getString(R.string.libpicturepicker_picker_ensure),
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
                mView.getString(R.string.libpicturepicker_picker_preview),
                mModel.getPickedPaths().size()
        );
    }
}
