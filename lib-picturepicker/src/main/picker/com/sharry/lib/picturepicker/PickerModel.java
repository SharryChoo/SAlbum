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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadFactory;
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
import static com.sharry.lib.picturepicker.FileUtil.getLastFileName;
import static com.sharry.lib.picturepicker.FileUtil.getParentFolderPath;

/**
 * MVP frame model associated with PicturePicker.
 *
 * @author Sharry <a href="SharryChooCHN@Gmail.com">Contact me.</a>
 * @version 1.2
 * @since 2018/8/30 20:00
 */
class PickerModel implements PickerContract.IModel {

    private static final ThreadPoolExecutor PICKER_EXECUTOR = new ThreadPoolExecutor(
            // 最大支持 3 个线程并发
            3,
            3,
            30, TimeUnit.SECONDS,
            new LinkedBlockingDeque<Runnable>(),
            new ThreadFactory() {
                @Override
                public Thread newThread(Runnable r) {
                    Thread thread = new Thread(r, PickerModel.class.getSimpleName());
                    thread.setDaemon(false);
                    return thread;
                }
            }
    );

    PickerModel() {
    }

    @Override
    public void fetchData(final Context context, final boolean supportGif,
                          final boolean supportVideo, final Callback callback) {
        PICKER_EXECUTOR.execute(new Runnable() {

            @Override
            public void run() {
                // 用于存储遍历到的所有图片文件夹集合
                ArrayList<FolderModel> folderModels = new ArrayList<>();
                // 创建一个图片文件夹, 用于保存所有图片
                FolderModel folderAll = new FolderModel(
                        context.getString(R.string.picture_picker_picker_all_picture)
                );
                folderModels.add(folderAll);
                // key 为图片的文件夹的绝对路径, values 为 FolderModel 的对象
                ConcurrentHashMap<String, FolderModel> folders = new ConcurrentHashMap<>(16);
                // 创建计数器
                CountDownLatch latch = new CountDownLatch(supportVideo ? 2 : 1);
                // 获取图片数据
                PICKER_EXECUTOR.execute(new PictureFetchRunnable(context, supportGif, folders, folderAll, latch));
                // 获取视频数据
                if (supportVideo) {
                    PICKER_EXECUTOR.execute(new VideoFetchRunnable(context, folders, folderAll, latch));
                }
                // 等待执行结束
                try {
                    latch.await();
                } catch (InterruptedException e) {
                    // ignore.
                } finally {
                    // 注入数据
                    folderModels.addAll(folders.values());
                    // 回调完成
                    callback.onFetched(folderModels);
                }
            }

        });
    }

    /**
     * 获取 图片 资源
     */
    private static class PictureFetchRunnable implements Runnable {

        private final Context context;
        private final boolean supportGif;
        private final ConcurrentHashMap<String, FolderModel> folders;
        private final FolderModel folderAll;
        private final CountDownLatch latch;

        PictureFetchRunnable(Context context,
                             boolean supportGif,
                             ConcurrentHashMap<String, FolderModel> folders,
                             FolderModel folderAll,
                             CountDownLatch latch) {
            this.context = context;
            this.supportGif = supportGif;
            this.folderAll = folderAll;
            this.folders = folders;
            this.latch = latch;
        }

        @Override
        public void run() {
            Cursor cursor = supportGif ? createPictureCursorWithGif() : createPictureCursorWithoutGif();
            try {
                if (cursor == null) {
                    return;
                }
                while (cursor.moveToNext()) {
                    // 验证路径是否有效
                    String picturePath = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                    if (TextUtils.isEmpty(picturePath)) {
                        continue;
                    }
                    // 构建数据源
                    MediaMeta meta = MediaMeta.create(picturePath, true);
                    meta.date = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media.DATE_ADDED));
                    meta.mimeType = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.MIME_TYPE));
                    // 1. 添加到 <所有> 目录下
                    folderAll.addMeta(meta);
                    // 2. 添加到文件所在目录
                    String folderPath = getParentFolderPath(picturePath);
                    if (TextUtils.isEmpty(folderPath)) {
                        continue;
                    }
                    // 添加图片到缓存
                    FolderModel folder = folders.get(folderPath);
                    if (folder == null) {
                        String folderName = getLastFileName(folderPath);
                        if (TextUtils.isEmpty(folderName)) {
                            folderName = context.getString(R.string.picture_picker_picker_root_folder);
                        }
                        folder = new FolderModel(folderName);
                        folders.put(folderPath, folder);
                    }
                    folder.addMeta(meta);
                }
            } catch (Throwable throwable) {
                // ignore.
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
                latch.countDown();
            }
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
            return context.getContentResolver().query(uri, projection,
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
            return context.getContentResolver().query(uri, projection,
                    selection, selectionArgs, sortOrder);
        }

    }

    /**
     * 获取 Video 资源
     */
    private static class VideoFetchRunnable implements Runnable {

        private final Context context;
        private final ConcurrentHashMap<String, FolderModel> folders;
        private final FolderModel folderAll;
        private final CountDownLatch latch;

        VideoFetchRunnable(Context context,
                           ConcurrentHashMap<String, FolderModel> folders,
                           FolderModel folderAll,
                           CountDownLatch latch) {
            this.context = context;
            this.folderAll = folderAll;
            this.folders = folders;
            this.latch = latch;
        }

        @Override
        public void run() {
            Cursor cursor = createVideoCursor();
            try {
                if (cursor == null) {
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
                    // 添加到 <所有> 目录下
                    folderAll.addMeta(meta);
                    // 获取图片父文件夹路径
                    String folderPath = getParentFolderPath(path);
                    if (TextUtils.isEmpty(folderPath)) {
                        continue;
                    }
                    // 添加图片到缓存
                    FolderModel folder = folders.get(folderPath);
                    if (folder == null) {
                        String folderName = getLastFileName(folderPath);
                        if (TextUtils.isEmpty(folderName)) {
                            folderName = context.getString(R.string.picture_picker_picker_root_folder);
                        }
                        folder = new FolderModel(folderName);
                        folders.put(folderPath, folder);
                    }
                    folder.addMeta(meta);
                }
            } catch (Throwable throwable) {
                // ignore.
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
                latch.countDown();
            }
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
            return context.getContentResolver().query(uri, projection,
                    selection, selectionArgs, sortOrder);
        }

        /**
         * 获取视频缩略图地址
         */
        @Nullable
        private String fetchVideoThumbNail(long id, String path, long date) {
            String thumbNailPath = null;
            Cursor cursor = createThumbnailCursor(id);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    thumbNailPath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Thumbnails.DATA));
                }
                cursor.close();
            }
            // 若没有视频缩略图, 则获取视频第一帧
            if (TextUtils.isEmpty(thumbNailPath)) {
                try {
                    thumbNailPath = generateThumbnail(path, date);
                } catch (Exception e) {
                    // ignore.
                }
            }
            return thumbNailPath;
        }

        private Cursor createThumbnailCursor(long id) {
            Uri uri = MediaStore.Video.Thumbnails.EXTERNAL_CONTENT_URI;
            String[] projection = new String[]{
                    MediaStore.Video.Thumbnails.DATA,
                    MediaStore.Video.Thumbnails.VIDEO_ID
            };
            String selection = MediaStore.Video.Thumbnails.VIDEO_ID + "=?";
            String[] selectionArgs = new String[]{String.valueOf(id)};
            return context.getContentResolver().query(uri, projection, selection,
                    selectionArgs, null);
        }

        private String generateThumbnail(String videoPath, long videoCreateDate) throws Exception {
            // 将第一帧缓存到本地
            File videoThumbnailFile = FileUtil.createVideoThumbnailFile(context, videoCreateDate);
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

    }

}
