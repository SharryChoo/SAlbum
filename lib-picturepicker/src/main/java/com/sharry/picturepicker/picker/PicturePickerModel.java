package com.sharry.picturepicker.picker;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.sharry.picturepicker.R;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * MVP frame model associated with PicturePicker.
 *
 * @author Sharry <a href="SharryChooCHN@Gmail.com">Contact me.</a>
 * @version 1.2
 * @since 2018/8/30 20:00
 */
class PicturePickerModel implements PicturePickerContract.IModel {

    private final ArrayList<String> mPickedPaths;                           // 用户已选中的图片地址集合(默认构造为空)
    private final ArrayList<String> mDisplayPaths = new ArrayList<>();      // 当前需要展示的集合
    private ArrayList<PictureFolder> mPictureFolders;                       // 所有包含图片数据的集合
    private PictureFolder mCheckedFolder;                                   // 当前正在展示的文件夹

    PicturePickerModel(ArrayList<String> pickedPaths, int threshold) {
        mPickedPaths = pickedPaths;
        // 验证一下阈值是否异常
        if (getPickedPaths().size() > threshold) {
            throw new RuntimeException("Your picked picture count is over your set threshold!");
        }
    }

    @Override
    public void getSystemPictures(Context context, final Callback callback) {
        new Thread(new CursorSystemPictureRunnable(context,
                new CursorSystemPictureRunnable.RunnableInteraction() {
                    @Override
                    public void onComplete(ArrayList<PictureFolder> pictureFolders) {
                        mPictureFolders = pictureFolders;
                        callback.onComplete();
                    }

                    @Override
                    public void onFailed(Throwable throwable) {
                        callback.onFailed(throwable);
                    }
                })
        ).start();
    }

    /**
     * 获取当前需要显示的文件模型
     */
    @Override
    public PictureFolder getPictureFolderAt(int index) {
        return mPictureFolders.get(index);
    }

    /**
     * 获取所有的图片文件夹
     */
    @Override
    public ArrayList<PictureFolder> getAllFolders() {
        return mPictureFolders;
    }

    /**
     * 设置当前选中的图片
     */
    @Override
    public PictureFolder getCheckedFolder() {
        return mCheckedFolder;
    }

    /**
     * 设置当前选中的文件夹
     */
    @Override
    public void setCheckedFolder(PictureFolder checkedFolder) {
        this.mCheckedFolder = checkedFolder;
        mDisplayPaths.clear();
        mDisplayPaths.addAll(checkedFolder.getPicturePaths());
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
    private static class CursorSystemPictureRunnable implements Runnable {

        interface RunnableInteraction {
            void onComplete(ArrayList<PictureFolder> pictureFolders);

            void onFailed(Throwable throwable);
        }

        private final Context mContext;
        private final RunnableInteraction mListener;

        CursorSystemPictureRunnable(Context context, RunnableInteraction listener) {
            mContext = context;
            mListener = listener;
        }

        @Override
        public void run() {
            // 用于存储遍历到的所有图片文件夹集合
            ArrayList<PictureFolder> pictureFolders = new ArrayList<>();
            // 创建一个图片文件夹, 用于保存所有图片
            PictureFolder allPictureFolder = new PictureFolder(mContext.getString(R.string.libpicturepicker_picker_all_picture));
            pictureFolders.add(allPictureFolder);
            // key 为图片的文件夹的绝对路径, values 为 PictureFolder 的对象
            HashMap<String, PictureFolder> caches = new HashMap<>();
            Cursor cursor = createImageCursor();
            try {
                if (cursor != null && cursor.getCount() > 0) {
                    // 是否是第一个遍历到的图片目录
                    while (cursor.moveToNext()) {
                        // 通过 cursor 获取图片路径
                        String picturePath = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                        if (TextUtils.isEmpty(picturePath)) {
                            continue;
                        }
                        // 添加到所有图片的目录下
                        allPictureFolder.addPath(picturePath);
                        // 获取图片父文件夹路径
                        String pictureFolderPath = getParentFolderPath(picturePath);
                        if (TextUtils.isEmpty(pictureFolderPath)) {
                            continue;
                        }
                        // 尝试从缓存中查找 pictureFolder 对象, 没有则创建新对象加入缓存
                        if (!caches.containsKey(pictureFolderPath)) {
                            String folderName = getLastFileName(pictureFolderPath);
                            caches.put(pictureFolderPath, new PictureFolder(folderName));
                        }
                        // 添加图片到缓存
                        caches.get(pictureFolderPath).addPath(picturePath);
                    }
                }
                // 添加所有文件夹数据
                pictureFolders.addAll(caches.values());
            } catch (Exception e) {
                mListener.onFailed(e);
            } finally {
                if (cursor != null) cursor.close();
            }
            mListener.onComplete(pictureFolders);
        }

        /**
         * Create cursor associated with this runnable.
         */
        private Cursor createImageCursor() {
            Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            String[] projection = new String[]{MediaStore.Images.Media.DATA};
            String selection = MediaStore.Images.Media.MIME_TYPE + "=? or " +
                    MediaStore.Images.Media.MIME_TYPE + "=? or " + MediaStore.Images.Media.MIME_TYPE + "=?";
            String[] selectionArgs = new String[]{"image/jpeg", "image/png", "image/jpg"};
            String sortOrder = MediaStore.Images.Media.DATE_ADDED + " DESC";
            return mContext.getContentResolver().query(uri, projection,
                    selection, selectionArgs, sortOrder);
        }

        /**
         * Get parent folder associated with this file.
         */
        private String getParentFolderPath(String filePath) {
            String parentFolderPath = new File(filePath).getParentFile().getAbsolutePath();
            if (TextUtils.isEmpty(parentFolderPath)) {
                int end = filePath.lastIndexOf(File.separator);
                if (end != -1) {
                    parentFolderPath = filePath.substring(0, end);
                }
            }
            return parentFolderPath;
        }

        /**
         * Get last file name associated with this filePath.
         */
        private String getLastFileName(String filePath) {
            String fileName = filePath.substring(filePath.lastIndexOf(File.separator) + 1);
            // 为空说明直接在 StorageCard 的根目录
            if (TextUtils.isEmpty(fileName)) {
                fileName = mContext.getString(R.string.libpicturepicker_picker_root_folder);
            }
            return fileName;
        }
    }
}
