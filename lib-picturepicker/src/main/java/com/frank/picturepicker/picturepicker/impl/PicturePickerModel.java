package com.frank.picturepicker.picturepicker.impl;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.frank.picturepicker.R;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by think on 2018/5/26.
 * Email: frankchoochina@gmail.com
 * Version: 1.0
 * Description:
 */
class PicturePickerModel implements PicturePickerContract.IModel {

    private final ArrayList<String> mPickedPaths;                     // 用户已选中的图片地址集合(默认构造为空)
    private final ArrayList<String> mDisplayPaths = new ArrayList<>();      // 当前需要展示的集合
    private ArrayList<PictureFolder> mFolderModels;                   // 所有包含图片文件夹Model的集合
    private PictureFolder mCheckedFolder;                            // 当前正在展示的文件夹

    PicturePickerModel(ArrayList<String> pickedPaths, int threshold) {
        mPickedPaths = pickedPaths;
        // 验证一下阈值是否异常
        if (getPickedPaths().size() > threshold) {
            throw new RuntimeException("Your picked picture count is over your set threshold!");
        }
    }

    @Override
    public void getSystemPictures(Context context, final PicturePickerContract.ModelInitializeCallback listener) {
        new Thread(new CursorSystemPictureRunnable(context, listener)).start();
    }

    /**
     * 获取当前需要显示的文件模型
     */
    @Override
    public PictureFolder getPictureFolderAt(int index) {
        return mFolderModels.get(index);
    }

    /**
     * 获取所有的图片文件夹
     */
    @Override
    public ArrayList<PictureFolder> getAllFolders() {
        return mFolderModels;
    }

    /**
     * 设置当前选中的图片
     */
    @Override
    public PictureFolder getCheckedFolder() {
        return mCheckedFolder;
    }

    public void setCheckedFolder(PictureFolder checkedFolder) {
        this.mCheckedFolder = checkedFolder;
        mDisplayPaths.clear();
        mDisplayPaths.addAll(checkedFolder.getImagePaths());
    }

    /**
     * 获取用户选中的图片
     */
    @Override
    public ArrayList<String> getPickedPaths() {
        return mPickedPaths;
    }

    /**
     * 添加用户选中的图片
     */
    @Override
    public void addPickedPicture(String path) {
        if (mPickedPaths.indexOf(path) == -1) {
            mPickedPaths.add(path);
        }
    }

    /**
     * 移除用户选中的图片
     */
    @Override
    public void removePickedPicture(String path) {
        if (mPickedPaths.indexOf(path) == -1) return;
        mPickedPaths.remove(path);
    }

    @Override
    public ArrayList<String> getDisplayPaths() {
        return mDisplayPaths;
    }

    /**
     * 遍历加载系统图片的线程
     */
    private class CursorSystemPictureRunnable implements Runnable {

        private Context mContext;
        private PicturePickerContract.ModelInitializeCallback mListener;

        CursorSystemPictureRunnable(Context context, PicturePickerContract.ModelInitializeCallback listener) {
            mContext = context;
            mListener = listener;
        }

        @Override
        public void run() {
            ArrayList<PictureFolder> pictureFolders = new ArrayList<>();
            PictureFolder allPictureFolder = new PictureFolder(mContext.getString(R.string.libpicturepicker_picturepicker_all_picture));
            pictureFolders.add(allPictureFolder);
            //key为存放图片的文件夹路径, values为PictureFolderModel的对象
            HashMap<String, PictureFolder> hashMap = new HashMap<>();
            Cursor cursor = createImageCursor();
            try {
                PictureFolder otherFolderModel;
                if (cursor != null && cursor.getCount() > 0) {
                    //是否是第一个遍历到的图片目录
                    while (cursor.moveToNext()) {
                        String imagePath = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                        if (TextUtils.isEmpty(imagePath)) continue;
                        // 1. 添加到所有图片的目录下
                        allPictureFolder.addImagePath(imagePath);
                        // 2. 添加到图片所在的目录下
                        String folderPath = null;
                        File folder = new File(imagePath).getParentFile();
                        if (folder != null) folderPath = folder.getAbsolutePath();
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
                                if (TextUtils.isEmpty(folderName)) folderName = "/";
                                otherFolderModel = new PictureFolder(folderName);
                                hashMap.put(folderPath, otherFolderModel);
                            }
                            otherFolderModel.addImagePath(imagePath);
                        }
                    }
                }
                // 添加所有文件夹数据
                pictureFolders.addAll(hashMap.values());
            } catch (Exception e) {
                mListener.onFailed(e);
            } finally {
                if (cursor != null) cursor.close();
            }
            mFolderModels = pictureFolders;
            mListener.onComplete(pictureFolders);
        }

        private Cursor createImageCursor() {
            Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            String[] projection = new String[]{MediaStore.Images.Media.DATA};
            String selection = MediaStore.Images.Media.MIME_TYPE + "=? or " +
                    MediaStore.Images.Media.MIME_TYPE + "=? or " + MediaStore.Images.Media.MIME_TYPE + "=?";
            String[] selectionArgs = new String[]{"image/jpeg", "image/png", "image/jpg"};
            String sortOrder = MediaStore.Images.Media.DATE_ADDED + " DESC";
            Cursor imageCursor = mContext.getContentResolver().query(uri, projection,
                    selection, selectionArgs, sortOrder);
            return imageCursor;
        }
    }
}
