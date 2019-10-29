package com.sharry.lib.media.recorder;

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
     * 创建音频文件
     */
    @NonNull
    static Uri createAudioUri(Context context, String authority, String relativePath, String suffix) {
        String fileName = "audio_" + DateFormat.format("yyyyMMdd_HH_mm_ss",
                Calendar.getInstance(Locale.CHINA)) + suffix;
        // 1. 生成指定目录
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q || TextUtils.isEmpty(relativePath)) {
            File dir = TextUtils.isEmpty(relativePath) ? context.getExternalFilesDir(Environment.DIRECTORY_PICTURES) : new File(relativePath);
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
                return getUriFromFile(context, authority, file);
            } catch (Throwable e) {
                throw new UnsupportedOperationException("Cannot create file at:  " + dir);
            }
        } else {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Audio.Media.RELATIVE_PATH, Environment.DIRECTORY_MUSIC + "/" + relativePath);
            values.put(MediaStore.Audio.Media.MIME_TYPE, "audio/aac");
            values.put(MediaStore.Audio.Media.DISPLAY_NAME, fileName);
            ContentResolver resolver = context.getContentResolver();
            return resolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values);
        }
    }

    /**
     * 创建音频文件
     */
    @NonNull
    static Uri createVideoUri(Context context, String authority, String relativePath, String suffix) {
        String fileName = "video_" + DateFormat.format("yyyyMMdd_HH_mm_ss",
                Calendar.getInstance(Locale.CHINA)) + suffix;
        // 1. 生成指定目录
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q || TextUtils.isEmpty(relativePath)) {
            File dir = TextUtils.isEmpty(relativePath) ? context.getExternalFilesDir(Environment.DIRECTORY_PICTURES) : new File(relativePath);
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
                return getUriFromFile(context, authority, file);
            } catch (Throwable e) {
                throw new UnsupportedOperationException("Cannot create file at:  " + dir);
            }
        } else {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Video.Media.RELATIVE_PATH, Environment.DIRECTORY_MOVIES + "/" + relativePath);
            values.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4");
            values.put(MediaStore.Video.Media.DISPLAY_NAME, fileName);
            ContentResolver resolver = context.getContentResolver();
            return resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
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
     * 删除文件
     */
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
     * Try to return the absolute file path from the given Uri
     *
     * @return the file path or null
     */
    static String getPath(final Context context, final Uri uri) {
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
            Cursor cursor = context.getContentResolver().query(
                    uri, new String[]{MediaStore.Images.ImageColumns.DATA},
                    null, null, null);
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

}
