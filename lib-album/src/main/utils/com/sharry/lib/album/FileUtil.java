package com.sharry.lib.album;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
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
    static Uri createJpegUri(Context context, String relativePath) {
        // 创建拍照目标文件
        String fileName = "camera_" + DateFormat.format("yyyyMMdd_HH_mm_ss",
                Calendar.getInstance(Locale.CHINA)) + ".jpg";
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/" + relativePath);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpg");
        values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
        ContentResolver resolver = context.getContentResolver();
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            return resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        } else {
            return resolver.insert(MediaStore.Images.Media.INTERNAL_CONTENT_URI, values);
        }
    }

    static File createJpegFile(Context context, String relativePath) {
        // 创建拍照目标文件
        String fileName = "camera_" + DateFormat.format("yyyyMMdd_HH_mm_ss",
                Calendar.getInstance(Locale.CHINA)) + ".jpg";
        File dir = TextUtils.isEmpty(relativePath) ? context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                : new File(Environment.getExternalStorageDirectory(), relativePath);
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
     * 删除 Uri
     */
    @TargetApi(29)
    static void delete(Context context, Uri uri) {
        if (uri.toString().startsWith("content://")) {
            // content://开头的Uri
            context.getContentResolver().delete(uri, null, null);
        } else {
            File file = new File(getPath(context, uri));
            if (file.exists() && file.isFile()) {
                if (file.delete()) {
                    notifyMediaStore(context, file.getAbsolutePath());
                }
            }
        }
    }

    /**
     * 删除文件
     */
    static void delete(Context context, File file) {
        if (file != null && file.exists() && file.isFile()) {
            if (file.delete()) {
                notifyMediaStore(context, file.getAbsolutePath());
            }
        }
    }

    /**
     * Try to return the absolute file path from the given Uri
     */
    static String getPath(Context context, Uri uri) {
        String path = null;
        // 以 file:// 开头的
        if (ContentResolver.SCHEME_FILE.equals(uri.getScheme())) {
            path = uri.getPath();
            return path;
        } else if (ContentResolver.SCHEME_CONTENT.equals(uri.getScheme())) {
            if (DocumentsContract.isDocumentUri(context, uri)) {
                if (isExternalStorageDocument(uri)) {
                    // ExternalStorageProvider
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];
                    if ("primary".equalsIgnoreCase(type)) {
                        path = Environment.getExternalStorageDirectory() + "/" + split[1];
                        return path;
                    }
                } else if (isDownloadsDocument(uri)) {
                    // DownloadsProvider
                    final String id = DocumentsContract.getDocumentId(uri);
                    final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"),
                            Long.valueOf(id));
                    path = getDataColumn(context, contentUri, null, null);
                    return path;
                } else if (isMediaDocument(uri)) {
                    // MediaProvider
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];
                    Uri contentUri = null;
                    if ("image".equals(type)) {
                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                    } else if ("video".equals(type)) {
                        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                    } else if ("audio".equals(type)) {
                        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                    }
                    final String selection = "_id=?";
                    final String[] selectionArgs = new String[]{split[1]};
                    path = getDataColumn(context, contentUri, selection, selectionArgs);
                    return path;
                }
            }
        }
        return null;
    }

    private static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    private static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

}
