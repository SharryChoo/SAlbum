package com.sharry.lib.album;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;
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
     * 创建图片路径的 URI
     *
     * @param relativePath 文件目录路径
     */
    @TargetApi(29)
    static Uri createJpegPendingItem(Context context, String relativePath) {
        // 创建拍照目标文件
        String fileName = "camera_" + DateFormat.format("yyyyMMdd_HH_mm_ss",
                Calendar.getInstance(Locale.CHINA)) + ".jpg";
        ContentValues values = new ContentValues();
        // 创建相对路径
        if (TextUtils.isEmpty(relativePath)) {
            values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);
        } else {
            values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/" + relativePath);
        }
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpg");
        values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
        values.put(MediaStore.Images.Media.IS_PENDING, 1);
        ContentResolver resolver = context.getContentResolver();
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            return resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        } else {
            return resolver.insert(MediaStore.Images.Media.INTERNAL_CONTENT_URI, values);
        }
    }

    /**
     * 通知 MediaStore 文件发布
     */
    @TargetApi(29)
    static void publishPendingItem(Context context, Uri item) {
        if (context == null || item == null) {
            return;
        }
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.IS_PENDING, 0);
        context.getContentResolver().update(item, values, null, null);
    }

    /**
     * 删除 Uri
     */
    @TargetApi(29)
    static void delete(Context context, Uri uri) {
        if (context != null && uri != null) {
            context.getContentResolver().delete(uri, null, null);
        }
    }

    /**
     * 创建 JPEG 图片文件
     */
    static File createJpegFile(Context context, String relativePath) {
        // 创建拍照目标文件
        String fileName = "camera_" + DateFormat.format("yyyyMMdd_HH_mm_ss",
                Calendar.getInstance(Locale.CHINA)) + ".jpg";
        File dir = TextUtils.isEmpty(relativePath) ? Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                : new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), relativePath);
        try {
            // 获取默认路径
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File file = new File(dir, fileName);
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();
            Log.i(TAG, "create jpeg file success -> " + file.getAbsolutePath());
            return file;
        } catch (Throwable e) {
            throw new UnsupportedOperationException("Cannot create file at:  " + dir);
        }
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
     * 删除文件
     */
    static void delete(Context context, File file) {
        if (context != null && file != null && file.exists() && file.isFile()) {
            if (file.delete()) {
                notifyMediaStore(context, file.getAbsolutePath());
            }
        }
    }

    /**
     * 根据图片 URI 获取图片路径
     */
    static String getImagePath(final Context context, final Uri uri) {
        if (null == uri) {
            return null;
        }
        final String scheme = uri.getScheme();
        String data = null;

        if (scheme == null) {
            data = uri.getPath();
        } else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            data = uri.getPath();
        } else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
            Cursor cursor = context.getContentResolver().query(uri,
                    new String[]{MediaStore.Images.ImageColumns.DATA}, null,
                    null, null
            );
            if (null != cursor) {
                if (cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                    if (index > -1) {
                        data = cursor.getString(index);
                    }
                }
                cursor.close();
            }
        }
        return data;
    }

    /**
     * 根据视频 URI 获取视频路径
     */
    static String getVideoPath(final Context context, final Uri uri) {
        if (null == uri) {
            return null;
        }
        final String scheme = uri.getScheme();
        String data = null;

        if (scheme == null) {
            data = uri.getPath();
        } else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            data = uri.getPath();
        } else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
            Cursor cursor = context.getContentResolver().query(uri,
                    new String[]{MediaStore.Video.VideoColumns.DATA}, null,
                    null, null
            );
            if (null != cursor) {
                if (cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(MediaStore.Video.VideoColumns.DATA);
                    if (index > -1) {
                        data = cursor.getString(index);
                    }
                }
                cursor.close();
            }
        }
        return data;
    }

}
