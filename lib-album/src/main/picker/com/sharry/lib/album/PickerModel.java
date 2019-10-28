package com.sharry.lib.album;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.sharry.lib.album.Constants.MIME_TYPE_3GP;
import static com.sharry.lib.album.Constants.MIME_TYPE_AIV;
import static com.sharry.lib.album.Constants.MIME_TYPE_FLV;
import static com.sharry.lib.album.Constants.MIME_TYPE_GIF;
import static com.sharry.lib.album.Constants.MIME_TYPE_JPEG;
import static com.sharry.lib.album.Constants.MIME_TYPE_MKV;
import static com.sharry.lib.album.Constants.MIME_TYPE_MOV;
import static com.sharry.lib.album.Constants.MIME_TYPE_MP4;
import static com.sharry.lib.album.Constants.MIME_TYPE_MPG;
import static com.sharry.lib.album.Constants.MIME_TYPE_PNG;
import static com.sharry.lib.album.Constants.MIME_TYPE_RMVB;
import static com.sharry.lib.album.Constants.MIME_TYPE_VOB;
import static com.sharry.lib.album.Constants.MIME_TYPE_WEBP;
import static com.sharry.lib.album.FileUtil.getLastFileName;
import static com.sharry.lib.album.FileUtil.getParentFolderPath;

/**
 * MVP frame model associated with PicturePicker.
 *
 * @author Sharry <a href="SharryChooCHN@Gmail.com">Contact me.</a>
 * @version 1.2
 * @since 2018/8/30 20:00
 */
class PickerModel implements PickerContract.IModel {

    private static final String TAG = PickerModel.class.getSimpleName();
    private static final ThreadPoolExecutor FETCH_EXECUTOR;

    static {
        FETCH_EXECUTOR = new ThreadPoolExecutor(
                // 3 个线程并发即可满足需求
                // 使用 4 个, 是为了预防因其中一个线程意外阻塞而导致任务无法正常执行的问题
                4, 4,
                30, TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(),
                new ThreadFactory() {
                    @Override
                    public Thread newThread(Runnable r) {
                        Thread thread = new Thread(r, PickerModel.class.getSimpleName());
                        thread.setDaemon(false);
                        return thread;
                    }
                }
        );
        // 允许核心线程销毁, 相册为低频组件, 无需持有核心线程, 防止占用过多系统资源
        FETCH_EXECUTOR.allowCoreThreadTimeOut(true);
    }

    private Future mFetchDataFuture;
    private Future mFetchPictureFuture;
    private Future mFetchVideoFuture;

    PickerModel() {
    }

    @Override
    public void fetchData(final Context context, final boolean supportGif,
                          final boolean supportVideo, final Callback callback) {
        mFetchDataFuture = FETCH_EXECUTOR.submit(new Runnable() {

            @Override
            public void run() {
                // 用于存储遍历到的所有图片文件夹集合
                ArrayList<FolderModel> folderModels = new ArrayList<>();
                // 创建一个图片文件夹, 用于保存所有图片
                FolderModel folderAll = new FolderModel(
                        context.getString(R.string.lib_album_picker_all_picture)
                );
                folderModels.add(folderAll);
                /*
                   key 为图片所在文件夹的绝对路径
                   values 为 FolderModel 的对象
                 */
                ConcurrentHashMap<String, FolderModel> folders = new ConcurrentHashMap<>(16);
                // 等待执行结束
                try {
                    // 创建计数器
                    CountDownLatch latch = new CountDownLatch(supportVideo ? 2 : 1);
                    // 获取图片数据
                    mFetchPictureFuture = FETCH_EXECUTOR.submit(new PictureFetchRunnable(context, supportGif, folders, folderAll, latch));
                    // 获取视频数据
                    if (supportVideo) {
                        mFetchVideoFuture = FETCH_EXECUTOR.submit(new VideoFetchRunnable(context, folders, folderAll, latch));
                    }
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

    @Override
    public void stopIfFetching() {
        if (mFetchDataFuture != null) {
            mFetchDataFuture.cancel(true);
        }
        if (mFetchPictureFuture != null) {
            mFetchPictureFuture.cancel(true);
        }
        if (mFetchVideoFuture != null) {
            mFetchVideoFuture.cancel(true);
        }
    }

    /**
     * The runnable for fetch picture resources.
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
                while (cursor.moveToNext()) {
                    // 验证路径是否有效
                    String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                    if (TextUtils.isEmpty(path)) {
                        continue;
                    }
                    // 构建数据源
                    long id = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media._ID));
                    MediaMeta meta = MediaMeta.create(
                            Uri.withAppendedPath(
                                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                    String.valueOf(id)),
                            path,
                            true
                    );
                    meta.date = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media.DATE_ADDED));
                    meta.mimeType = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.MIME_TYPE));

                    // 1. 添加到 <所有> 目录下
                    folderAll.addMeta(meta);
                    // 2. 添加到文件所在目录
                    String folderPath = getParentFolderPath(path);
                    if (TextUtils.isEmpty(folderPath)) {
                        continue;
                    }
                    // 添加资源到缓存
                    FolderModel folder = folders.get(folderPath);
                    if (folder == null) {
                        String folderName = getLastFileName(folderPath);
                        if (TextUtils.isEmpty(folderName)) {
                            folderName = context.getString(R.string.lib_album_picker_root_folder);
                        }
                        folder = new FolderModel(folderName);
                        folders.put(folderPath, folder);
                    }
                    folder.addMeta(meta);
                }
                Log.i(TAG, "Fetch picture resource completed.");
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
                    MediaStore.Video.Media._ID,
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
                    MediaStore.Video.Media._ID,
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
     * The runnable for fetch video resources.
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
                while (cursor.moveToNext()) {
                    // 验证路径是否有效
                    String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA));
                    if (TextUtils.isEmpty(path)) {
                        continue;
                    }
                    long id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID));
                    MediaMeta meta = MediaMeta.create(
                            Uri.withAppendedPath(
                                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                                    String.valueOf(id)
                            ),
                            path,
                            false
                    );
                    meta.duration = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION));
                    meta.date = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED));
                    meta.size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE));
                    meta.mimeType = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.MIME_TYPE));
                    // 获取缩略图
                    meta.thumbnailPath = fetchVideoThumbNail(id, path, meta.date);
                    // 添加到 <所有> 目录下
                    folderAll.addMeta(meta);
                    // 获取资源所在文件夹
                    String folderPath = getParentFolderPath(path);
                    if (TextUtils.isEmpty(folderPath)) {
                        continue;
                    }
                    // 添加资源到缓存
                    FolderModel folder = folders.get(folderPath);
                    if (folder == null) {
                        String folderName = getLastFileName(folderPath);
                        if (TextUtils.isEmpty(folderName)) {
                            folderName = context.getString(R.string.lib_album_picker_root_folder);
                        }
                        folder = new FolderModel(folderName);
                        folders.put(folderPath, folder);
                    }
                    folder.addMeta(meta);
                }
                Log.i(TAG, "Fetch video resource completed.");
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
    }

}
