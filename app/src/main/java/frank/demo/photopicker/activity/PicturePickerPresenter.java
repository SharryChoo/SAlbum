package frank.demo.photopicker.activity;

import java.lang.ref.WeakReference;
import java.util.List;

import frank.demo.photopicker.model.PictureFolderModel;

/**
 * Created by 99538 on 2017/8/3.
 */

public class PicturePickerPresenter {

    private PicturePickerViewInterface mView;
    private PicturePickerModel mModel = new PicturePickerModel();

    public PicturePickerPresenter(PicturePickerViewInterface view) {
        mView = new WeakReference<PicturePickerViewInterface>(view).get();
    }

    /**
     * 将用户选中的图片存入数据仓库
     */
    public void picturePicked(String url) {
        mModel.addPickedUriList(url);
        mView.pictureStateChanged();
    }

    /**
     * 将用户移除的图片从数据仓库移除
     */
    public void pictureRemove(String url) {
        mModel.removePickedUriList(url);
        mView.pictureStateChanged();
    }

    /**
     * 取出用户选中的图片的集合
     */
    public List<String> fetchPickedList() {
        return mModel.getPickedList();
    }

    /**
     * 取出所有文件模型的集合
     */
    public List<PictureFolderModel> fetchFolderModelList() {
        return mModel.getFolderModelList();
    }

    /**
     * 展示当前选中的文件夹
     */
    public void showCurrentFolder(int index) {
        mModel.setCurrentFolderModel(index);
        mView.showCurrentFolderPicture(mModel.getCurrentFolderModel().getImageUriList());
    }

    /**
     * 获取当前的文件名
     */
    public String getCurrentFolderName() {
        return mModel.getCurrentFolderModel().getFolderName();
    }

    /**
     * 初始化已选择的图片列表
     */
    public void initPickedList(List<String> pickedUriList) {
        mModel.setPickedList(pickedUriList);
    }

    public interface PicturePickerViewInterface {

        void showCurrentFolderPicture(List<String> currentPictureList);

        void pictureStateChanged();
    }

}
