package com.frank.lib_picturepicker.picturepicker.mvp.presenter;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.frank.lib_picturepicker.picturepicker.data.PictureFolder;
import com.frank.lib_picturepicker.picturepicker.mvp.PicturePickerContract;
import com.frank.lib_picturepicker.picturepicker.mvp.model.PicturePickerModel;

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

    /**
     * 配置用户选中的图片 URI
     *
     * @param userPicked 用户已经选中的图片
     */
    @Override
    public void setupUserPickedSet(ArrayList<String> userPicked) {
        mModel.setUserPickedSet(userPicked == null ? new ArrayList<String>() : userPicked);
        mView.updateTextContent(mModel.getUserPickedSet().size(), mModel.getThreshold());
        mView.updateTextViewVisibility(mModel.getUserPickedSet().size() > 0);
    }

    /**
     * 配置阈值
     */
    @Override
    public void setupThreshold(int threshold) {
        mModel.setThreshold(threshold);
    }

    /**
     * 初始化 Model 的数据
     */
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
                        mView.showMsg("获取相册数据失败");
                    }
                });
            }
        });
    }

    /**
     * 获取 <<指定索引处的>> 图片文件夹
     *
     * @param position
     */
    @Override
    public void fetchDisplayPictures(int position) {
        PictureFolder target = mModel.getPictureFolderAt(position);
        if (mView == null) return;
        mView.displayPictures(target.getFolderName(), target.getImagePaths());
    }

    /**
     * 获取所有图片文件夹
     */
    @Override
    public ArrayList<PictureFolder> fetchAllPictureFolders() {
        return mModel.getAllPictureFolders();
    }

    /**
     * 获取用户选中的所有图片
     *
     * @return
     */
    @Override
    public ArrayList<String> fetchUserPickedSet() {
        return mModel.getUserPickedSet();
    }

    /**
     * 处理图片被选中了
     */
    @Override
    public boolean performPicturePicked(String uri) {
        if (fetchUserPickedSet().size() == mModel.getThreshold()) {
            mView.showMsg("最多只可选择 " + mModel.getThreshold() + " 张图片");
            return false;
        }
        mModel.addPickedPicture(uri);
        mView.updateTextContent(mModel.getUserPickedSet().size(), mModel.getThreshold());
        mView.updateTextViewVisibility(mModel.getUserPickedSet().size() > 0);
        return true;
    }

    /**
     * 处理图片被移除了
     */
    @Override
    public void performPictureRemoved(String uri) {
        mModel.removePickedPicture(uri);
        mView.updateTextContent(mModel.getUserPickedSet().size(), mModel.getThreshold());
        mView.updateTextViewVisibility(mModel.getUserPickedSet().size() > 0);
    }


}
