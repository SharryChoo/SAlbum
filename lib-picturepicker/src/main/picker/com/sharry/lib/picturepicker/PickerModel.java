package com.sharry.lib.picturepicker;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.sharry.lib.picturepicker.Constants.MIME_TYPE_3GP;
import static com.sharry.lib.picturepicker.Constants.MIME_TYPE_AIV;
import static com.sharry.lib.picturepicker.Constants.MIME_TYPE_FLV;
import static com.sharry.lib.picturepicker.Constants.MIME_TYPE_GIF;
import static com.sharry.lib.picturepicker.Constants.MIME_TYPE_JPEG;
import static com.sharry.lib.picturepicker.Constants.MIME_TYPE_MKV;
import static com.sharry.lib.picturepicker.Constants.MIME_TYPE_MOV;
import static com.sharry.lib.picturepicker.Constants.MIME_TYPE_MP4;
import static com.sharry.lib.picturepicker.Constants.MIME_TYPE_MPG;
import static com.sharry.lib.picturepicker.Constants.MIME_TYPE_PNG;
import static com.sharry.lib.picturepicker.Constants.MIME_TYPE_RMVB;
import static com.sharry.lib.picturepicker.Constants.MIME_TYPE_VOB;
import static com.sharry.lib.picturepicker.Constants.MIME_TYPE_WEBP;

/**
 * MVP frame model associated with PicturePicker.
 *
 * @author Sharry <a href="SharryChooCHN@Gmail.com">Contact me.</a>
 * @version 1.2
 * @since 2018/8/30 20:00
 */
class PickerModel implements PickerContract.IModel {

    private static final ThreadPoolExecutor PICKER_EXECUTOR = new ThreadPoolExecutor(
            1, 1,
            0, TimeUnit.SECONDS,
            new LinkedBlockingDeque<Runnable>()
    );

    PickerModel() {
    }

    @Override
    public void fetchData(Context context, boolean supportGif, boolean supportVideo, Callback callback) {
        PICKER_EXECUTOR.execute(new CursorRunnable(context, supportGif, supportVideo, callback));
    }

    /**
     * 遍历加载系统图片和视频
     */
    private static class CursorRunnable implements Runnable {

        private final boolean mSupportGif, mSupportVideo;
        private final Context mContext;
        private final Callback mCallback;

        CursorRunnable(Context context, boolean supportGif, boolean supportVideo, Callback callback) {
            this.mContext = context;
            this.mSupportGif = supportGif;
            this.mSupportVideo = supportVideo;
            this.mCallback = callback;
        }

        @Override
        public void run() {
            // 用于存储遍历到的所有图片文件夹集合
            ArrayList<FolderModel> folderModels = new ArrayList<>();
            // 创建一个图片文件夹, 用于保存所有图片
            FolderModel allFolderModel = new FolderModel(mContext.getString(R.string.picture_picker_picker_all_picture));
            folderModels.add(allFolderModel);
            // key 为图片的文件夹的绝对路径, values 为 FolderModel 的对象
            HashMap<String, FolderModel> caches = new HashMap<>();
            try {
                fetchPictures(allFolderModel, caches);
                if (mSupportVideo) {
                    fetchVideos(allFolderModel, caches);
                }
                folderModels.addAll(caches.values());
            } catch (Throwable e) {
                mCallback.onFailed(e);
            }
            mCallback.onCompleted(folderModels);
        }

        /**
         * 获取所有图片资源信息
         */
        private void fetchPictures(FolderModel allFolderModel, HashMap<String, FolderModel> caches) {
            Cursor cursor = mSupportGif ? createPictureCursorWithGif() : createPictureCursorWithoutGif();
            if (cursor == null || cursor.getCount() == 0) {
                return;
            }
            // 是否是第一个遍历到的图片目录
            while (cursor.moveToNext()) {
                // 验证路径是否有效
                String picturePath = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                if (TextUtils.isEmpty(picturePath)) {
                    continue;
                }
                MediaMeta meta = MediaMeta.create(picturePath, true);
                meta.date = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media.DATE_ADDED));
                meta.mimeType = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.MIME_TYPE));
                // 添加到所有图片的目录下
                allFolderModel.addMeta(meta);
                // 获取图片父文件夹路径
                String folderPath = getParentFolderPath(picturePath);
                if (TextUtils.isEmpty(folderPath)) {
                    continue;
                }
                // 添加图片到缓存
                FolderModel folder = caches.get(folderPath);
                if (folder == null) {
                    String folderName = getLastFileName(folderPath);
                    folder = new FolderModel(folderName);
                    caches.put(folderPath, folder);
                }
                folder.addMeta(meta);
            }
            cursor.close();
        }

        /**
         * 获取所有视频资源信息
         */
        private void fetchVideos(FolderModel allFolderModel, HashMap<String, FolderModel> caches) throws Exception {
            Cursor cursor = createVideoCursor();
            if (cursor == null || cursor.getCount() == 0) {
                return;
            }
            while (cursor.moveToNext()) {
                // 验证路径是否有效
                String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA));
                if (TextUtils.isEmpty(path)) {
                    continue;
                }
                MediaMeta meta = MediaMeta.create(path, false);
                meta.duration = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION));
                meta.date = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED));
                meta.size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE));
                meta.mimeType = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.MIME_TYPE));
                // 获取缩略图
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID));
                meta.thumbnailPath = fetchVideoThumbNail(id, path, meta.date);
                // 添加到所有图片的目录下
                allFolderModel.addMeta(meta);
                // 获取图片父文件夹路径
                String folderPath = getParentFolderPath(path);
                if (TextUtils.isEmpty(folderPath)) {
                    continue;
                }
                // 添加图片到缓存
                FolderModel folder = caches.get(folderPath);
                if (folder == null) {
                    String folderName = getLastFileName(folderPath);
                    folder = new FolderModel(folderName);
                    caches.put(folderPath, folder);
                }
                folder.addMeta(meta);
            }
            cursor.close();
        }

        /**
         * 获取视频缩略图地址
         */
        @Nullable
        private String fetchVideoThumbNail(long id, String path, long date) throws Exception {
            String thumbNailPath = null;
            Cursor cursor = createThumbnailCursor(id);
            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    thumbNailPath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Thumbnails.DATA));
                }
                cursor.close();
            }
            // 若没有视频缩略图, 则获取视频第一帧
            if (TextUtils.isEmpty(thumbNailPath)) {
                thumbNailPath = generateThumbnail(path, date);
            }
            return thumbNailPath;
        }

        /**
         * Create image cursor associated with this runnable.
         */
        private Cursor createPictureCursorWithGif() {
            Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            String[] projection = new String[]{
                    MediaStore.Images.Media.DATA,
                    MediaStore.Images.Media.DATE_ADDED,
                    MediaStore.Video.Media.MIME_TYPE
            };
            String selection = MediaStore.Images.Media.MIME_TYPE + "=? or " +
                    MediaStore.Images.Media.MIME_TYPE + "=? or " +
                    MediaStore.Images.Media.MIME_TYPE + "=? or " +
                    MediaStore.Images.Media.MIME_TYPE + "=?";
            String[] selectionArgs = new String[]{
                    MIME_TYPE_JPEG,
                    MIME_TYPE_PNG,
                    MIME_TYPE_WEBP,
                    MIME_TYPE_GIF
            };
            String sortOrder = MediaStore.Images.Media.DATE_ADDED + " DESC";
            return mContext.getContentResolver().query(uri, projection,
                    selection, selectionArgs, sortOrder);
        }

        /**
         * Create image cursor associated with this runnable.
         */
        private Cursor createPictureCursorWithoutGif() {
            Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            String[] projection = new String[]{
                    MediaStore.Images.Media.DATA,
                    MediaStore.Images.Media.DATE_ADDED,
                    MediaStore.Video.Media.MIME_TYPE
            };
            String selection = MediaStore.Images.Media.MIME_TYPE + "=? or " +
                    MediaStore.Images.Media.MIME_TYPE + "=? or " +
                    MediaStore.Images.Media.MIME_TYPE + "=?";
            String[] selectionArgs = new String[]{
                    MIME_TYPE_JPEG,
                    MIME_TYPE_PNG,
                    MIME_TYPE_WEBP
            };
            String sortOrder = MediaStore.Images.Media.DATE_ADDED + " DESC";
            return mContext.getContentResolver().query(uri, projection,
                    selection, selectionArgs, sortOrder);
        }

        /**
         * Create video cursor associated with this runnable.
         */
        private Cursor createVideoCursor() {
            Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
            String[] projection = new String[]{
                    MediaStore.Video.Media._ID,
                    MediaStore.Video.Media.DATA,
                    MediaStore.Video.Media.DURATION,
                    MediaStore.Video.Media.DATE_ADDED,
                    MediaStore.Video.Media.SIZE,
                    MediaStore.Video.Media.MIME_TYPE
            };
            String selection = MediaStore.Images.Media.MIME_TYPE + "=? or " +
                    MediaStore.Video.Media.MIME_TYPE + "=? or " +
                    MediaStore.Video.Media.MIME_TYPE + "=? or " +
                    MediaStore.Video.Media.MIME_TYPE + "=? or " +
                    MediaStore.Video.Media.MIME_TYPE + "=? or " +
                    MediaStore.Video.Media.MIME_TYPE + "=? or " +
                    MediaStore.Video.Media.MIME_TYPE + "=? or " +
                    MediaStore.Video.Media.MIME_TYPE + "=? or " +
                    MediaStore.Video.Media.MIME_TYPE + "=?";
            String[] selectionArgs = new String[]{
                    MIME_TYPE_MP4,
                    MIME_TYPE_3GP,
                    MIME_TYPE_AIV,
                    MIME_TYPE_RMVB,
                    MIME_TYPE_VOB,
                    MIME_TYPE_FLV,
                    MIME_TYPE_MKV,
                    MIME_TYPE_MOV,
                    MIME_TYPE_MPG,
            };
            String sortOrder = MediaStore.Images.Media.DATE_ADDED + " DESC";
            return mContext.getContentResolver().query(uri, projection,
                    selection, selectionArgs, sortOrder);
        }

        private Cursor createThumbnailCursor(long id) {
            Uri uri = MediaStore.Video.Thumbnails.EXTERNAL_CONTENT_URI;
            String[] projection = new String[]{
                    MediaStore.Video.Thumbnails.DATA,
                    MediaStore.Video.Thumbnails.VIDEO_ID
            };
            String selection = MediaStore.Video.Thumbnails.VIDEO_ID + "=?";
            String[] selectionArgs = new String[]{String.valueOf(id)};
            return mContext.getContentResolver().query(uri, projection, selection,
                    selectionArgs, null);
        }

        private String generateThumbnail(String videoPath, long videoCreateDate) throws Exception {
            // 将第一帧缓存到本地
            File videoThumbnailFile = FileUtil.createVideoThumbnailFile(mContext, videoCreateDate);
            if (videoThumbnailFile.exists()) {
                return videoThumbnailFile.getAbsolutePath();
            } else {
                videoThumbnailFile.createNewFile();
            }
            // 获取 video 第一帧
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            try {
                retriever.setDataSource(videoPath);
                Bitmap bitmap = retriever.getFrameAtTime(0);
                // 将图像压缩并写入 dstPath
                if (bitmap != null) {
                    CompressUtil.doCompress(bitmap, videoThumbnailFile.getAbsolutePath(),
                            50, 512, 512);
                }
            } catch (Throwable e) {
                // ignore.
            } finally {
                retriever.release();
            }
            // 返回路径
            return videoThumbnailFile.getAbsolutePath();
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
                fileName = mContext.getString(R.string.picture_picker_picker_root_folder);
            }
            return fileName;
        }
    }

}
