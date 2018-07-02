package com.frank.picturepicker.picturepicker.impl.mvp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.text.format.DateFormat;
import android.widget.ImageView;

import com.frank.picturepicker.R;
import com.frank.picturepicker.picturepicker.impl.data.PictureFolder;
import com.frank.picturepicker.picturepicker.impl.ui.PicturePickerActivity;
import com.frank.picturepicker.picturepicker.impl.ui.PicturePickerDialog;
import com.frank.picturepicker.picturepicker.manager.PickerConfig;
import com.frank.picturepicker.picturepicker.manager.PicturePickerFragment;
import com.frank.picturepicker.picturetake.manager.PictureTakeManager;
import com.frank.picturepicker.picturetake.manager.TakeCallback;
import com.frank.picturepicker.picturewatcher.manager.PictureWatcherManager;
import com.frank.picturepicker.picturewatcher.manager.WatcherCallback;
import com.frank.picturepicker.pricturecrop.manager.CropCallback;
import com.frank.picturepicker.pricturecrop.manager.PictureCropManager;
import com.frank.picturepicker.support.loader.PictureLoader;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;


/**
 * Created by think on 2018/5/26.
 * Email: frankchoochina@gmail.com
 * Version: 1.0
 * Description: 图片选择器的 Presenter
 */
public class PicturePickerPresenter implements PicturePickerContract.IPresenter, TakeCallback, CropCallback, WatcherCallback {

    private PicturePickerContract.IView mView;
    private PicturePickerModel mModel = new PicturePickerModel();
    private Handler mMainLooperHandler = new Handler(Looper.getMainLooper());
    private PickerConfig mConfig;

    @Override
    public void attach(PicturePickerContract.IView view) {
        this.mView = view;
    }

    @Override
    public void setupUserPickedSet(ArrayList<String> userPicked) {
        mModel.setUserPickedSet(userPicked == null ? new ArrayList<String>() : userPicked);
        if (mView == null) return;
        mView.updateEnsureAndPreviewTextContent(fetchUserPickedSet().size(), mModel.getThreshold());
    }

    @Override
    public void setupThreshold(int threshold) {
        mModel.setThreshold(threshold);
    }

    @Override
    public void initData(Context context, PickerConfig config) {
        this.mConfig = config;
        mModel.getSystemPictures(context, new PicturePickerContract.ModelInitializeCallback() {
            @Override
            public void onComplete(List<PictureFolder> pictureFolders) {
                mMainLooperHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        // 展示第一个图片文件夹
                        PictureFolder allPictureFolder = mModel.getPictureFolderAt(0);
                        mModel.setCurDisplayFolder(allPictureFolder);
                        if (mView == null) return;
                        mView.displayPictures(allPictureFolder.getFolderName(), allPictureFolder.getImagePaths());
                        mView.updateEnsureAndPreviewTextContent(fetchUserPickedSet().size(), mModel.getThreshold());
                    }
                });
            }

            @Override
            public void onFailed(Throwable throwable) {
                mMainLooperHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mView == null) return;
                        mView.showMsg(mView.getString(R.string.activity_picture_picker_msg_fetch_album_failed));
                    }
                });
            }
        });
    }

    @Override
    public void fetchDisplayPictures(int position) {
        PictureFolder curDisplayFolder = mModel.getPictureFolderAt(position);
        mModel.setCurDisplayFolder(curDisplayFolder);
        if (mView == null) return;
        mView.displayPictures(curDisplayFolder.getFolderName(), curDisplayFolder.getImagePaths());
    }

    @Override
    public ArrayList<PictureFolder> fetchAllPictureFolders() {
        return mModel.getAllPictureFolders();
    }

    @Override
    public ArrayList<String> fetchUserPickedSet() {
        return mModel.getUserPickedSet();
    }

    @Override
    public boolean performPictureChecked(String uri) {
        boolean result = isCanPickedPicture(true);
        if (result) {
            mModel.addPickedPicture(uri);
            mView.updateEnsureAndPreviewTextContent(fetchUserPickedSet().size(), mModel.getThreshold());
        }
        return result;
    }

    @Override
    public void performPictureUnchecked(String imagePath) {
        mModel.removePickedPicture(imagePath);
        mView.updateEnsureAndPreviewTextContent(fetchUserPickedSet().size(), mModel.getThreshold());
    }

    @Override
    public void performCameraClicked() {
        PictureTakeManager.with((Context) mView)
                .setFileProviderAuthority(mConfig.authority)
                .setCameraDestFilePath(
                        new File(mConfig.cameraDirectoryPath,
                                "camera_" + DateFormat.format("yyyyMMdd_HHmmss",
                                        Calendar.getInstance(Locale.CHINA)) + ".jpg").getAbsolutePath()
                )
                .setCameraDestQuality(mConfig.cameraDestQuality)
                .take(this);
    }

    @Override
    public void performPictureClicked(ArrayList<String> pictureUris, int position,
                                      ImageView sharedElement) {
        PictureWatcherManager.with((Context) mView)
                .setThreshold(mModel.getThreshold())
                .setIndicatorTextColor(mConfig.indicatorTextColor)
                .setIndicatorSolidColor(mConfig.indicatorSolidColor)
                .setIndicatorBorderColor(mConfig.indicatorBorderCheckedColor, mConfig.indicatorBorderUncheckedColor)
                .setPictureUris(pictureUris, position)
                .setUserPickedSet(fetchUserPickedSet())
                .setSharedElement(sharedElement)
                .setPictureLoader(PictureLoader.getPictureLoader())
                .start(this);
    }

    @Override
    public void performPreviewClicked() {
        if (!isCanLaunchPreview()) return;
        PictureWatcherManager.with((Context) mView)
                .setThreshold(mModel.getThreshold())
                .setIndicatorTextColor(mConfig.indicatorTextColor)
                .setIndicatorSolidColor(mConfig.indicatorSolidColor)
                .setIndicatorBorderColor(mConfig.indicatorBorderCheckedColor, mConfig.indicatorBorderUncheckedColor)
                .setPictureUris(fetchUserPickedSet(), 0)
                .setUserPickedSet(fetchUserPickedSet())
                .setPictureLoader(PictureLoader.getPictureLoader())
                .start(this);
    }

    @Override
    public void performEnsureClicked() {
        if (!isCanEnsure()) return;
        // 不需要裁剪, 直接返回
        if (!mConfig.isCropSupport) {
            performUserPickedSetResult();
            return;
        }
        // 需要裁剪, 则启动裁剪
        PictureCropManager.with((Context) mView)
                .setFileProviderAuthority(mConfig.authority)
                .setDesireSize(mConfig.cropWidth, mConfig.cropHeight)
                .setCropCircle(mConfig.isCropCircle)
                // 启动裁剪了只能选择一张图片
                .setOriginFilePath(fetchUserPickedSet().get(0))
                // 裁剪后存储的文件路径
                .setDestFilePath(mConfig.cropDestFilePath)
                // 裁剪后压缩的质量
                .setQuality(mConfig.cropDestQuality)
                .crop(this);
    }

    @Override
    public void performBottomMenuClicked() {
        new PicturePickerDialog((Context) mView, fetchAllPictureFolders())
                .setOnItemClickedListener(new PicturePickerDialog.OnItemClickedListener() {
                    @Override
                    public void onDialogItemClicked(int position) {
                        fetchDisplayPictures(position);
                    }
                })
                .show();
    }

    @Override
    public void onWatcherPickedComplete(boolean isEnsure, ArrayList<String> userPickedSet) {
        if (mView == null) return;
        setupUserPickedSet(userPickedSet);
        if (isEnsure) performEnsureClicked();
        else mView.notifyUserPickedSetChanged();
    }

    @Override
    public void onTakeComplete(String path) {
        // 添加到当前拍照的文件夹下
        PictureFolder curFolder = mModel.getCurDisplayFolder();
        curFolder.getImagePaths().add(0, path);
        // 添加到所有文件的文件夹下
        PictureFolder allPictureFolder = mModel.getPictureFolderAt(0);
        if (allPictureFolder != curFolder) {
            allPictureFolder.getImagePaths().add(0, path);
        }
        // 判断是否可以继续选择
        if (isCanPickedPicture(false)) {
            mModel.addPickedPicture(path);// 添加到选中的集合中
            mView.updateEnsureAndPreviewTextContent(fetchUserPickedSet().size(), mModel.getThreshold());// 更新文本
        }
        mView.notifyCameraTakeOnePicture(path);// 通知拍摄了一张照片
    }

    @Override
    public void onCropComplete(String path) {
        fetchUserPickedSet().clear();
        fetchUserPickedSet().add(path);
        performUserPickedSetResult();
    }

    /**
     * 是否可以继续选择图片
     *
     * @param isShowFailedMsg 是否提示失败原因
     * @return true is can picked, false is cannot picked.
     */
    private boolean isCanPickedPicture(boolean isShowFailedMsg) {
        if (fetchUserPickedSet().size() == mModel.getThreshold() && mView != null) {
            if (isShowFailedMsg) {
                mView.showMsg(mView.getString(R.string.activity_picture_picker_msg_over_threshold_prefix)
                        + mModel.getThreshold()
                        + mView.getString(R.string.activity_picture_picker_msg_over_threshold_suffix)
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
    private boolean isCanLaunchPreview() {
        if (fetchUserPickedSet().size() == 0 && mView != null) {
            mView.showMsg(mView.getString(R.string.activity_picture_picker_msg_preview_failed));
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
        if (fetchUserPickedSet().size() == 0 && mView != null) {
            mView.showMsg(mView.getString(R.string.activity_picture_picker_msg_ensure_failed));
            return false;
        }
        return true;
    }

    /**
     * 处理图片选择完成了
     */
    private void performUserPickedSetResult() {
        if (mView != null && mView instanceof Activity) {
            Activity bind = (Activity) mView;
            Intent intent = new Intent();
            intent.putExtra(PicturePickerActivity.RESULT_INTENT_EXTRA_PICKED_PICTURES, fetchUserPickedSet());
            bind.setResult(PicturePickerFragment.REQUEST_CODE_PICKED, intent);
            bind.finish();
        }
    }
}
