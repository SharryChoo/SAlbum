package com.frank.picturepicker.picturepicker.impl.mvp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;

import com.frank.picturepicker.R;
import com.frank.picturepicker.picturepicker.impl.data.PictureFolder;
import com.frank.picturepicker.picturepicker.impl.ui.PicturePickerActivity;
import com.frank.picturepicker.picturepicker.impl.ui.PicturePickerDialog;
import com.frank.picturepicker.pricturecrop.manager.CropCallback;
import com.frank.picturepicker.picturetake.manager.TakeCallback;
import com.frank.picturepicker.picturewatcher.manager.WatcherCallback;
import com.frank.picturepicker.picturepicker.manager.PickerConfig;
import com.frank.picturepicker.support.loader.PictureLoader;
import com.frank.picturepicker.pricturecrop.manager.PictureCropManager;
import com.frank.picturepicker.picturepicker.manager.PicturePickerFragment;
import com.frank.picturepicker.picturetake.manager.PictureTakeManager;
import com.frank.picturepicker.picturewatcher.manager.PictureWatcherManager;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Created by think on 2018/5/26.
 * Email: frankchoochina@gmail.com
 * Version: 1.0
 * Description: 图片选择器的 Presenter
 */
public class PicturePickerPresenter implements PicturePickerContract.IPresenter, TakeCallback, WatcherCallback, CropCallback {

    private PicturePickerContract.IView mView;
    private PicturePickerModel mModel = new PicturePickerModel();
    private Handler mMainLooperHandler = new Handler(Looper.getMainLooper());

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
    public void initData(Context context) {
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
    public void performCameraClicked(final Context context, final PickerConfig config) {
        PictureTakeManager.with(context)
                .setFileProviderAuthority(config.authority)
                .setCameraDestFilePath(new File(config.cameraDirectoryPath,
                        new Date().getTime() + ".jpg").getAbsolutePath())
                .setCameraDestQuality(config.cameraDestQuality)
                .take(this);
    }

    @Override
    public void performPictureClicked(Context context, ArrayList<String> pictureUris, int position,
                                      PickerConfig config, ImageView sharedElement) {
        PictureWatcherManager.with(context)
                .setThreshold(mModel.getThreshold())
                .setIndicatorTextColor(config.indicatorTextColor)
                .setIndicatorSolidColor(config.indicatorSolidColor)
                .setIndicatorBorderColor(config.indicatorBorderCheckedColor, config.indicatorBorderUncheckedColor)
                .setPictureUris(pictureUris, position)
                .setUserPickedSet(fetchUserPickedSet())
                .setSharedElement(sharedElement)
                .setPictureLoader(PictureLoader.getPictureLoader())
                .start(this);
    }

    @Override
    public void performPreviewClicked(Context context, PickerConfig config) {
        if (!isCanLaunchPreview()) return;
        PictureWatcherManager.with(context)
                .setThreshold(mModel.getThreshold())
                .setIndicatorTextColor(config.indicatorTextColor)
                .setIndicatorSolidColor(config.indicatorSolidColor)
                .setIndicatorBorderColor(config.indicatorBorderCheckedColor, config.indicatorBorderUncheckedColor)
                .setPictureUris(fetchUserPickedSet(), 0)
                .setUserPickedSet(fetchUserPickedSet())
                .setPictureLoader(PictureLoader.getPictureLoader())
                .start(this);
    }

    @Override
    public void performEnsureClicked(final PicturePickerActivity bind, PickerConfig config) {
        if (!isCanEnsure()) return;
        // 不需要裁剪, 直接返回
        if (!config.isCropSupport) {
            performUserPickedSetResult();
            return;
        }
        // 需要裁剪, 则启动裁剪
        PictureCropManager.with(bind)
                .setFileProviderAuthority(config.authority)
                .setDesireSize(config.cropWidth, config.cropHeight)
                .setCropCircle(config.isCropCircle)
                // 启动裁剪了只能选择一张图片
                .setOriginFilePath(fetchUserPickedSet().get(0))
                // 裁剪后存储的文件路径
                .setDestFilePath(config.cropDestFilePath)
                // 裁剪后压缩的质量
                .setQuality(config.cropDestQuality)
                .crop(this);
    }

    @Override
    public void performBottomMenuClicked(Context context) {
        new PicturePickerDialog(context, fetchAllPictureFolders())
                .setOnItemClickedListener(new PicturePickerDialog.OnItemClickedListener() {
                    @Override
                    public void onDialogItemClicked(int position) {
                        fetchDisplayPictures(position);
                    }
                })
                .show();
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
    public void onWatcherPickedComplete(ArrayList<String> userPickedSet) {
        if (mView == null) return;
        setupUserPickedSet(userPickedSet);
        mView.notifyUserPickedSetChanged();
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
        if (fetchUserPickedSet().size() == mModel.getThreshold()) {
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
        if (fetchUserPickedSet().size() == 0) {
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
        if (fetchUserPickedSet().size() == 0) {
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
            intent.putExtra(PicturePickerFragment.RESULT_EXTRA_PICKED_PICTURES, fetchUserPickedSet());
            bind.setResult(PicturePickerFragment.REQUEST_CODE_PICKED, intent);
            bind.finish();
        }
    }
}
