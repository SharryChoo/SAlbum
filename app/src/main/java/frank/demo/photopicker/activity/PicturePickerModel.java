package frank.demo.photopicker.activity;

import android.database.Cursor;
import android.provider.MediaStore;
import android.text.TextUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import frank.demo.photopicker.app_manager.MyApp;
import frank.demo.photopicker.model.PictureFolderModel;

/**
 * Created by 99538 on 2017/8/3.
 */

public class PicturePickerModel {

    private List<PictureFolderModel> mFolderModelList;// 所有包含图片文件夹Model的集合

    private List<String> mPickedUriList;// 当前用户选中的URI集合

    private PictureFolderModel mCurrentFolderModel;// 当前正在显示的图片文件Model

    public PicturePickerModel() {
        mFolderModelList = getPictureFolderList();
        mCurrentFolderModel = mFolderModelList.get(0);
        mPickedUriList = new ArrayList<>();
    }

    /**
     * 初始化pickedList列表
     */
    public void setPickedList(List<String> pickedUriList) {
        mPickedUriList = pickedUriList;
    }

    /**
     * 获取文件模型的集合
     */
    public List<PictureFolderModel> getFolderModelList() {
        return mFolderModelList;
    }


    /**
     * 设置当前需要显示的文件模型目录
     */
    public void setCurrentFolderModel(int index) {
        mCurrentFolderModel = mFolderModelList.get(index);
    }

    /**
     * 获取当前需要显示的文件模型
     */
    public PictureFolderModel getCurrentFolderModel() {
        return mCurrentFolderModel;
    }

    /**
     * 获取被选中图片的URI集合
     */
    public List<String> getPickedList() {
        return mPickedUriList;
    }

    /**
     * 向集合中添加被选中的图片
     */
    public void addPickedUriList(String uri) {
        mPickedUriList.add(uri);
    }

    /**
     * 从集合中移除取消选中的集合
     */
    public void removePickedUriList(String uri) {
        mPickedUriList.remove(mPickedUriList.indexOf(uri));
    }

    /**遍历StorageCard找寻所有图片所在的文件夹以及自身对的*/
    private List<PictureFolderModel> getPictureFolderList() {
        List<PictureFolderModel> pictureFolderModels = new ArrayList<>();
        PictureFolderModel allPictureFolderModel = new PictureFolderModel("所有图片");
        pictureFolderModels.add(allPictureFolderModel);
        //key为存放图片的文件夹路径, values为PictureFolderModel的对象
        HashMap<String, PictureFolderModel> hashMap = new HashMap<>();
        Cursor cursor = null;
        try {
            cursor = MyApp.getContext().getContentResolver().query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    new String[]{MediaStore.Images.Media.DATA},
                    MediaStore.Images.Media.MIME_TYPE + "=? or " + MediaStore.Images.Media.MIME_TYPE + "=? or " + MediaStore.Images.Media.MIME_TYPE + "=?",
                    new String[]{"image/jpeg", "image/png", "image/jpg"},
                    MediaStore.Images.Media.DATE_ADDED + " DESC"
            );
            PictureFolderModel otherFolderModel;
            if (cursor != null && cursor.getCount() > 0) {
                //是否是第一个遍历到的图片目录
                while (cursor.moveToNext()) {
                    String imagePath = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                    if (TextUtils.isEmpty(imagePath)) {
                        continue;
                    }
                    allPictureFolderModel.addImageUriList(imagePath);

                    // 其他图片目录
                    String folderPath = null;
                    File folder = new File(imagePath).getParentFile();
                    if (folder != null) {
                        folderPath = folder.getAbsolutePath();
                    }
                    if (TextUtils.isEmpty(folderPath)) {
                        // 找寻最后一个分隔符
                        int end = imagePath.lastIndexOf(File.separator);
                        if (end != -1) {
                            folderPath = imagePath.substring(0, end);
                        }
                    }
                    if (!TextUtils.isEmpty(folderPath)) {
                        // 通过folderPath在hashMap中寻找是否存在相应的PictureFolderModel对象
                        if (hashMap.containsKey(folderPath)) {
                            otherFolderModel = hashMap.get(folderPath);
                        } else {
                            // 通过文件路径截取文件名
                            String folderName = folderPath.substring(folderPath.lastIndexOf(File.separator) + 1);
                            // 为空说明直接在StorageCard的根目录
                            if (TextUtils.isEmpty(folderName)) {
                                folderName = "/";
                            }
                            otherFolderModel = new PictureFolderModel(folderName);
                            hashMap.put(folderPath, otherFolderModel);
                        }
                        otherFolderModel.addImageUriList(imagePath);
                    }
                }
                // 添加其他图片目录
                pictureFolderModels.addAll(hashMap.values());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return pictureFolderModels;
    }

}
