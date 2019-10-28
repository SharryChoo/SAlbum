package com.sharry.lib.album;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

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
    public boolean handlePictureChecked(MediaMeta checkedMeta) {
        boolean result = isCanPickedPicture(true);
        if (result && mPickedSet.add(checkedMeta)) {
            mView.setToolbarEnsureText(buildEnsureText());
            mView.setPreviewText(buildPreviewText());
        }
        return result;
    }

    @Override
    public void handlePictureUnchecked(MediaMeta removedMeta) {
        if (mPickedSet.remove(removedMeta)) {
            mView.setToolbarEnsureText(buildEnsureText());
            mView.setPreviewText(buildPreviewText());
        }
    }

    @Override
    public void handleCameraClicked() {
        if (mPickerConfig.getTakerConfig() != null) {
            TakerManager.with((Context) mView)
                    .setConfig(
                            mPickerConfig.getTakerConfig().rebuild()
                                    // 取消相机拍摄后的裁剪动作, 由 Picker ensure 时触发
                                    .setVideoRecord(mPickerConfig.isPickVideo())
                                    .build()
                    )
                    .take(this);
        }
    }

    @Override
    public void handlePictureClicked(int position, View sharedElement) {
        WatcherManager.with((Context) mView)
                .setSharedElement(sharedElement)
                .setLoaderEngine(Loader.getPictureLoader())
                .setConfig(
                        mWatcherConfig.rebuild()
                                .setDisplayDataSet(mDisplaySet, position)
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
                .setLoaderEngine(Loader.getPictureLoader())
                .setConfig(
                        mWatcherConfig.rebuild()
                                .setDisplayDataSet(mPickedSet, 0)
                                .build()
                )
                .startForResult(this);
    }

    @Override
    public void handleFolderChecked(int position) {
        performFolderChecked(position);
    }

    @Override
    public void handleEnsureClicked() {
        if (!isCanEnsure()) {
            return;
        }
        // 不需要裁剪, 直接返回
        if (mPickerConfig.isCropSupport() && mPickedSet.get(0).isPicture()) {
            // 启动裁剪
            assert mPickerConfig.getCropperConfig() != null;
            CropperManager.with((Context) mView)
                    .setConfig(
                            mPickerConfig.getCropperConfig().rebuild()
                                    .setOriginFile(mPickedSet.get(0).contentUri)
                                    .build()
                    )
                    .crop(this);
        } else {
            mView.setResultAndFinish(mPickedSet);
        }
    }

    @Override
    public void handleRecycleViewDraw(RecyclerView parent) {
        // Cache view bounds.
        SharedElementHelper.CACHES.clear();
        for (int i = 0; i < parent.getChildCount(); i++) {
            View child = parent.getChildAt(i);
            int adapterPosition = parent.getChildAdapterPosition(child) +
                    (mPickerConfig.isCameraSupport() ? -1 : 0);
            SharedElementHelper.CACHES.put(adapterPosition, SharedElementHelper.Bounds.parseFrom(
                    child, adapterPosition));
        }
    }

    @Override
    public void handleViewDestroy() {
        // 终止 mModel 获取数据
        mModel.stopIfFetching();
        // 清空共享元素缓存的数据
        SharedElementHelper.CACHES.clear();
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
            mView.notifyPickedSetChanged();
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
            mPickedSet.add(newMeta);
            mView.setToolbarEnsureText(buildEnsureText());
            mView.setPreviewText(buildPreviewText());
        }
        // 3.2 通知 UI 更新视图
        mView.notifyNewMetaInsertToFirst();
        mView.notifyFolderDataSetChanged();
    }

    //////////////////////////////////////////////CropperCallback/////////////////////////////////////////////////

    @Override
    public void onCropComplete(@NonNull MediaMeta mediaMeta) {
        mPickedSet.clear();
        mPickedSet.add(mediaMeta);
        mView.setResultAndFinish(mPickedSet);
    }

    private void setupView() {
        // 配置 UI 视图
        mView.setToolbarScrollable(mPickerConfig.isToolbarBehavior());
        mView.setFabVisible(mPickerConfig.isFabBehavior());
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
                context.getApplicationContext(),
                mPickerConfig.isPickGif(),
                mPickerConfig.isPickVideo(),
                new PickerContract.IModel.Callback() {

                    private final Handler mainHandler = new Handler(Looper.getMainLooper());

                    @Override
                    public void onFetched(@NonNull final ArrayList<FolderModel> folderModels) {
                        mFolderModels = folderModels;
                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mView.setProgressBarVisible(false);
                                mView.setFolderAdapter(mFolderModels);
                                performFolderChecked(0);
                            }
                        });
                    }

                }
        );
    }

    /**
     * 执行展示文件夹的操作
     */
    private void performFolderChecked(int position) {
        // Upgrade checked folder.
        mCheckedFolder = mFolderModels.get(position);
        mDisplaySet.clear();
        mDisplaySet.addAll(mCheckedFolder.getMetas());
        // Notify view displays paths changed.
        mView.notifyDisplaySetChanged();
        // Set folder text associated with view.
        mView.setPictureFolderText(mCheckedFolder.getName());
        // Set ensure text associated with view toolbar.
        mView.setToolbarEnsureText(buildEnsureText());
        // Set preview text associated with view.
        mView.setPreviewText(buildPreviewText());
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
                mView.showMsg(mView.getString(R.string.lib_album_picker_tips_over_threshold_prefix)
                        + mPickerConfig.getThreshold()
                        + mView.getString(R.string.lib_album_picker_tips_over_threshold_suffix)
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
            mView.showMsg(mView.getString(R.string.lib_album_picker_tips_preview_failed));
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
            mView.showMsg(mView.getString(R.string.lib_album_picker_tips_ensure_failed));
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
                mView.getString(R.string.lib_album_picker_ensure),
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
                mView.getString(R.string.lib_album_picker_preview),
                mPickedSet.size()
        );
    }
}
