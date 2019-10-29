package com.sharry.lib.media.recorder;

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

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;

import java.io.File;
import java.util.Calendar;
import java.util.Locale;

/**
 * @author Sharry <a href="xiaoyu.zhu@1hai.cn">Contact me.</a>
 * @version 1.0
 * @since 1/22/2019 4:31 PM
 */
class FileUtil {

    /**
     * 创建音频 URI
     */
    @NonNull
    @TargetApi(29)
    static Uri createAudioUri(Context context, String relativePath, String mime, String suffix) {
        String fileName = "audio_" + DateFormat.format("yyyyMMdd_HH_mm_ss",
                Calendar.getInstance(Locale.CHINA)) + suffix;
        ContentValues values = new ContentValues();
        values.put(MediaStore.Audio.Media.RELATIVE_PATH, Environment.DIRECTORY_MUSIC + "/" + relativePath);
        values.put(MediaStore.Audio.Media.MIME_TYPE, mime);
        values.put(MediaStore.Audio.Media.DISPLAY_NAME, fileName);
        ContentResolver resolver = context.getContentResolver();
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            return resolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values);
        } else {
            return resolver.insert(MediaStore.Audio.Media.INTERNAL_CONTENT_URI, values);
        }
    }

    /**
     * 创建音频文件
     */
    static File createAudioFile(Context context, String relativePath, String suffix) {
        String fileName = "audio_" + DateFormat.format("yyyyMMdd_HH_mm_ss",
                Calendar.getInstance(Locale.CHINA)) + suffix;
        File dir = TextUtils.isEmpty(relativePath) ?
                context.getExternalFilesDir(Environment.DIRECTORY_PICTURES) : new File(relativePath);
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
            return file;
        } catch (Throwable e) {
            throw new UnsupportedOperationException("Cannot create file at:  " + dir);
        }
    }

    /**
     * 创建视频 URI
     */
    @NonNull
    @TargetApi(29)
    static Uri createVideoUri(Context context, String relativePath, String mime, String suffix) {
        String fileName = "video_" + DateFormat.format("yyyyMMdd_HH_mm_ss",
                Calendar.getInstance(Locale.CHINA)) + suffix;
        ContentValues values = new ContentValues();
        values.put(MediaStore.Video.Media.RELATIVE_PATH, Environment.DIRECTORY_MOVIES + "/" + relativePath);
        values.put(MediaStore.Video.Media.MIME_TYPE, mime);
        values.put(MediaStore.Video.Media.DISPLAY_NAME, fileName);
        ContentResolver resolver = context.getContentResolver();
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            return resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
        } else {
            return resolver.insert(MediaStore.Video.Media.INTERNAL_CONTENT_URI, values);
        }
    }

    /**
     * 创建视频 File
     */
    @NonNull
    static File createVideoFile(Context context, String relativePath, String suffix) {
        String fileName = "video_" + DateFormat.format("yyyyMMdd_HH_mm_ss",
                Calendar.getInstance(Locale.CHINA)) + suffix;
        File dir = TextUtils.isEmpty(relativePath) ? context.getExternalFilesDir(Environment.DIRECTORY_MOVIES)
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
            return file;
        } catch (Throwable e) {
            throw new UnsupportedOperationException("Cannot create file at:  " + dir);
        }
    }

    /**
     * 获取 URI
     */
    static Uri getUriFromFile(Context context, String authority, File file) {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ?
                FileProvider.getUriForFile(context, authority, file) : Uri.fromFile(file);
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
     * Try to return the absolute file path from the given Uri
     */
    static String getPath(Context context, Uri uri) {
        String path;
        // 以 file:// 开头的
        if (ContentResolver.SCHEME_FILE.equals(uri.getScheme())) {
            return uri.getPath();
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
