package com.sharry.lib.album;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Locale;

/**
 * 处理文件相关的工具类
 *
 * @author Sharry <a href="SharryChooCHN@Gmail.com">Contact me.</a>
 * @version 1.0
 * @since 2018/9/22 17:39
 */
class FileUtil {

    private static final String TAG = FileUtil.class.getSimpleName();

    /**
     * Get parent folder associated with this file.
     */
    static String getParentFolderPath(String filePath) {
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
     * Get last file name associated with this path.
     */
    static String getLastFileName(String filePath) {
        return filePath.substring(filePath.lastIndexOf(File.separator) + 1);
    }

    /**
     * 获取 URI
     */
    static Uri getUriFromFile(Context context, String authority, File file) {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ?
                FileProvider.getUriForFile(context, authority, file) : Uri.fromFile(file);
    }

    static File createVideoThumbnailFile(Context context, long videoId) {
        return new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                "VideoThumbnail_" + videoId + ".jpg");
    }

    /**
     * 创建临时文件
     *
     * @return 创建的文件
     */
    static File createTempJpegFile(Context context) {
        // 获取临时文件目录
        File tempDirectory = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        // 创建临时文件
        String tempFileName = "temp_file_" + DateFormat.format("yyyyMMdd_HH_mm_ss",
                Calendar.getInstance(Locale.CHINA)) + ".jpg";
        File tempFile = new File(tempDirectory, tempFileName);
        try {
            if (tempFile.exists()) {
                tempFile.delete();
            }
            tempFile.createNewFile();
            Log.i(TAG, "create temp file directory success -> " + tempFile.getAbsolutePath());
        } catch (IOException e) {
            Log.e(TAG, "create temp file directory failed ->" + tempFile.getAbsolutePath(), e);
        }
        return tempFile;
    }

    /**
     * 创建拍照文件
     *
     * @param directoryPath 文件目录路径
     */
    static File createJpegFile(Context context, String directoryPath) {
        // 获取默认路径
        File dir = VersionUtil.isQ() || TextUtils.isEmpty(directoryPath) ?
                context.getExternalFilesDir(Environment.DIRECTORY_PICTURES) : new File(directoryPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        // 创建拍照目标文件
        String fileName = "camera_" + DateFormat.format("yyyyMMdd_HH_mm_ss",
                Calendar.getInstance(Locale.CHINA)) + ".jpg";
        File jpegFile = new File(dir, fileName);
        try {
            if (jpegFile.exists()) {
                jpegFile.delete();
            }
            jpegFile.createNewFile();
            Log.i(TAG, "create jpeg file success -> " + jpegFile.getAbsolutePath());
        } catch (IOException e) {
            Log.e(TAG, "create jpeg file failed -> " + jpegFile.getAbsolutePath(), e);
        }
        return jpegFile;
    }

    /**
     * 通知 MediaStore 文件更替
     */
    static void notifyMediaStore(Context context, String filePath) {
        if (context == null || TextUtils.isEmpty(filePath)) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            MediaScannerConnection.scanFile(context.getApplicationContext(), new String[]{filePath}, null, null);
        } else {
            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + Environment.getExternalStorageDirectory())));
        }
    }

    /**
     * 通过 MediaStore 保存到 Pictures 中
     *
     * @param context context
     * @param source  源文件
     * @return 拷贝成功之后的 URI
     */
    @Nullable
    @TargetApi(29)
    static Uri copyToPictures(Context context, File source) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpg");
        values.put(MediaStore.Images.Media.DISPLAY_NAME, source.getName());
        ContentResolver resolver = context.getContentResolver();
        Uri destUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        if (writeToUri(resolver, source, destUri)) {
            return destUri;
        }
        return null;
    }

    /**
     * 通过 MediaStore 保存到 Movies 中
     *
     * @param context context
     * @param src     源文件
     * @return 拷贝成功之后的 URI
     */
    @Nullable
    @TargetApi(29)
    static Uri copyToMovies(Context context, File src) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_MOVIES);
        values.put(MediaStore.Images.Media.MIME_TYPE, "video/mp4");
        values.put(MediaStore.Images.Media.DISPLAY_NAME, src.getName());
        ContentResolver resolver = context.getContentResolver();
        Uri dstUri = resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
        if (writeToUri(resolver, src, dstUri)) {
            return dstUri;
        }
        return null;
    }

    private static boolean writeToUri(ContentResolver resolver, File src, Uri dstUri) {
        BufferedInputStream inputStream = null;
        OutputStream os = null;
        try {
            inputStream = new BufferedInputStream(new FileInputStream(src));
            if (dstUri != null) {
                os = resolver.openOutputStream(dstUri);
            }
            if (os != null) {
                byte[] buffer = new byte[1024 * 4];
                int len;
                while ((len = inputStream.read(buffer)) != -1) {
                    os.write(buffer, 0, len);
                }
                os.flush();
            }
            return true;
        } catch (Throwable e) {
            e.printStackTrace();
            return false;
        } finally {
            close(os, inputStream);
        }
    }

    private static void close(Closeable... closeables) {
        if (closeables == null) {
            return;
        }
        for (Closeable closeable : closeables) {
            try {
                closeable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
