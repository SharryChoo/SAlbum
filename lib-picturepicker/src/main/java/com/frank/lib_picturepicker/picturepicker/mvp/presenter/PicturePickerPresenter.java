package com.frank.lib_picturepicker.picturepicker.mvp.presenter;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import com.frank.lib_picturepicker.R;
import com.frank.lib_picturepicker.picturepicker.data.PictureFolder;
import com.frank.lib_picturepicker.picturepicker.mvp.PicturePickerContract;
import com.frank.lib_picturepicker.picturepicker.mvp.model.PicturePickerModel;
import com.frank.lib_picturepicker.picturepicker.mvp.view.dialog.PicturePickerDialog;
import com.frank.lib_picturepicker.picturepicker.support.PicturePickerConfig;
import com.frank.lib_picturepicker.picturewatcher.support.PictureWatcherCallback;
import com.frank.lib_picturepicker.picturewatcher.support.PictureWatcherManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by think on 2018/5/26.
 * Email: frankchoochina@gmail.com
 * Version: 1.0
 * Description: 图片选择器的 Presenter
 */
public class PicturePickerPresenter implements PicturePickerContract.IPresenter {

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
        mView.updateTextContent(mModel.getUserPickedSet().size(), mModel.getThreshold());
        mView.updateTextViewVisibility(mModel.getUserPickedSet().size() > 0);
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
                        if (mView == null) return;
                        mView.displayPictures(allPictureFolder.getFolderName(), allPictureFolder.getImagePaths());
                        mView.updateTextContent(mModel.getUserPickedSet().size(), mModel.getThreshold());
                        mView.updateTextViewVisibility(mModel.getUserPickedSet().size() > 0);
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
        PictureFolder target = mModel.getPictureFolderAt(position);
        if (mView == null) return;
        mView.displayPictures(target.getFolderName(), target.getImagePaths());
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
        if (fetchUserPickedSet().size() == mModel.getThreshold()) {
            mView.showMsg(mView.getString(R.string.activity_picture_picker_msg_over_threshold_prefix)
                    + mModel.getThreshold()
                    + mView.getString(R.string.activity_picture_picker_msg_over_threshold_suffix)
            );
            return false;
        }
        mModel.addPickedPicture(uri);
        mView.updateTextContent(mModel.getUserPickedSet().size(), mModel.getThreshold());
        mView.updateTextViewVisibility(mModel.getUserPickedSet().size() > 0);
        return true;
    }

    @Override
    public void performPictureUnchecked(String imagePath) {
        mModel.removePickedPicture(imagePath);
        mView.updateTextContent(mModel.getUserPickedSet().size(), mModel.getThreshold());
        mView.updateTextViewVisibility(mModel.getUserPickedSet().size() > 0);
    }

    @Override
    public void performPictureClicked(View sharedElement, String uri, int position, PicturePickerConfig config, ArrayList<String> pictureUris) {
        PictureWatcherManager.with(sharedElement.getContext())
                .setThreshold(mModel.getThreshold())
                .setIndicatorTextColor(config.indicatorTextColor)
                .setIndicatorSolidColor(config.indicatorSolidColor)
                .setIndicatorBorderColor(config.indicatorBorderCheckedColor, config.indicatorBorderUncheckedColor)
                .setPictureUris(pictureUris, position)
                .setUserPickedSet(fetchUserPickedSet())
                .setSharedElement(sharedElement)
                .start(new PictureWatcherCallback() {
                    @Override
                    public void onResult(ArrayList<String> userPickedSet) {
                        if (mView == null) return;
                        setupUserPickedSet(userPickedSet);
                        mView.notifyUserPickedSetChanged();
                    }
                });
    }

    @Override
    public void performPreviewClicked(Context context, PicturePickerConfig config) {
        PictureWatcherManager.with(context)
                .setThreshold(mModel.getThreshold())
                .setIndicatorTextColor(config.indicatorTextColor)
                .setIndicatorSolidColor(config.indicatorSolidColor)
                .setIndicatorBorderColor(config.indicatorBorderCheckedColor, config.indicatorBorderUncheckedColor)
                .setPictureUris(fetchUserPickedSet(), 0)
                .setUserPickedSet(fetchUserPickedSet())
                .start(new PictureWatcherCallback() {
                    @Override
                    public void onResult(ArrayList<String> userPickedSet) {
                        if (mView == null) return;
                        setupUserPickedSet(userPickedSet);
                        mView.notifyUserPickedSetChanged();
                    }
                });
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

}
