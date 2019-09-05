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
class PickerPresenter implements PickerContract.IPresenter,
        WatcherCallback,
        TakerCallback,
        CropperCallback {

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

    /**
     * Data Source.
     */
    private ArrayList<FolderModel> mFolderModels;
    private final ArrayList<MediaMeta> mPickedSet;

    /**
     * Current checked set.
     */
    private final ArrayList<MediaMeta> mDisplaySet = new ArrayList<>();
    private FolderModel mCheckedFolder;

    PickerPresenter(@NonNull PickerContract.IView view,
                    @NonNull Context context, @NonNull PickerConfig config) {
        this.mView = view;
        this.mPickerConfig = config;
        this.mPickedSet = mPickerConfig.getUserPickedSet();
        this.mWatcherConfig = WatcherConfig.Builder()
                .setThreshold(mPickerConfig.getThreshold())
                .setIndicatorTextColor(mPickerConfig.getIndicatorTextColor())
                .setIndicatorSolidColor(mPickerConfig.getIndicatorSolidColor())
                .setIndicatorBorderColor(
                        mPickerConfig.getIndicatorBorderCheckedColor(),
                        mPickerConfig.getIndicatorBorderUncheckedColor()
                )
                .setUserPickedSet(mPickedSet)
                .build();
        this.mModel = new PickerModel();
        setupView();
        fetchData(context);
    }

    //////////////////////////////////////////////PickerContract.IPresenter/////////////////////////////////////////////////

    @Override
    public void handleCameraClicked() {
        if (mPickerConfig.getTakerConfig() != null) {
            TakerManager.with((Context) mView)
                    .setConfig(
                            mPickerConfig.getTakerConfig().rebuild()
                                    .setCropConfig(mPickerConfig.getCropperConfig())
                                    .setVideoRecord(mPickerConfig.isPickVideo())
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
                .setConfig(mWatcherConfig.rebuild().setDisplayDataSet(mDisplaySet, position).build())
                .startForResult(this);
    }

    @Override
    public boolean handlePictureChecked(MediaMeta checkedMeta) {
        boolean result = isCanPickedPicture(true);
        if (result && mPickedSet.add(checkedMeta)) {
            mView.setToolbarEnsureText(buildEnsureText());
            mView.setPreviewText(buildPreviewText());
        }
        return result;
    }

    @Override
    public void handlePictureRemoved(MediaMeta removedMeta) {
        if (mPickedSet.remove(removedMeta)) {
            mView.setToolbarEnsureText(buildEnsureText());
            mView.setPreviewText(buildPreviewText());
        }
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
                                .setDisplayDataSet(mPickedSet, 0)
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
        if (mPickerConfig.isCropSupport()) {
            // 启动裁剪
            assert mPickerConfig.getCropperConfig() != null;
            CropperManager.with((Context) mView)
                    .setConfig(
                            mPickerConfig.getCropperConfig().rebuild()
                                    .setOriginFile(mPickedSet.get(0).path)
                                    .build()
                    )
                    .crop(this);
        } else {
            mView.setResult(mPickedSet);
        }
    }

    @Override
    public void handleFolderChecked(int position) {
        performDisplayCheckedFolder(position);
    }

    //////////////////////////////////////////////WatcherCallback/////////////////////////////////////////////////

    @Override
    public void onWatcherPickedComplete(boolean isEnsure, ArrayList<MediaMeta> pickedMetas) {
        // 刷新用户选中的集合
        mPickedSet.clear();
        mPickedSet.addAll(pickedMetas);
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

    //////////////////////////////////////////////TakerCallback/////////////////////////////////////////////////

    @Override
    public void onCameraTakeComplete(@NonNull MediaMeta newMeta) {
        // 1. 添加到 <当前展示> 的文件夹下
        mCheckedFolder.addMeta(newMeta);
        // 2. 添加到 <所有文件> 的文件夹下
        FolderModel folderAll = mFolderModels.get(0);
        if (folderAll != mCheckedFolder) {
            folderAll.addMeta(newMeta);
        }
        // 3. 更新展示的集合
        mDisplaySet.add(0, newMeta);
        // 3.1 判断是否可以继续选择
        if (isCanPickedPicture(false)) {
            mPickedSet.add(0, newMeta);
            mView.setToolbarEnsureText(buildEnsureText());
            mView.setPreviewText(buildPreviewText());
        }
        // 3.2 通知 UI 更新视图
        mView.notifyNewMetaInsertToFirst();
        mView.notifyFolderDataSetChanged();
    }

    //////////////////////////////////////////////CropperCallback/////////////////////////////////////////////////

    @Override
    public void onCropComplete(@NonNull String path) {
        mPickedSet.clear();
        mPickedSet.add(MediaMeta.create(path, true));
        mView.setResult(mPickedSet);
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
            mView.setBackgroundColor(mPickerConfig.getPickerBackgroundColor());
        }
        // 设置图片的列数
        mView.setSpanCount(mPickerConfig.getSpanCount());
        // 设置 RecyclerView 的 Adapter
        mView.setPickerAdapter(mPickerConfig, mDisplaySet, mPickedSet);
    }

    private void fetchData(Context context) {
        mView.setProgressBarVisible(true);
        mModel.fetchData(
                context,
                mPickerConfig.isPickGif(),
                mPickerConfig.isPickVideo(),
                new PickerContract.IModel.Callback() {

                    private final Handler mainHandler = new Handler(Looper.getMainLooper());

                    @Override
                    public void onCompleted(@NonNull final ArrayList<FolderModel> folderModels) {
                        mFolderModels = folderModels;
                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mView.setProgressBarVisible(false);
                                mView.setFolderAdapter(mFolderModels);
                                performDisplayCheckedFolder(0);
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
                }
        );
    }

    /**
     * 执行展示文件夹的操作
     */
    private void performDisplayCheckedFolder(int position) {
        // Upgrade checked folder.
        mCheckedFolder = mFolderModels.get(position);
        mDisplaySet.clear();
        mDisplaySet.addAll(mCheckedFolder.getMetas());
        // Set folder text associated with view.
        mView.setPictureFolderText(mCheckedFolder.getName());
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
        if (mPickedSet.size() == mPickerConfig.getThreshold()) {
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
        if (mPickedSet.isEmpty()) {
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
        if (mPickedSet.isEmpty()) {
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
                mPickedSet.size(),
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
                mPickedSet.size()
        );
    }
}
