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
        if (!TextUtils.isEmpty(relativePath)) {
            values.put(MediaStore.Audio.Media.RELATIVE_PATH, Environment.DIRECTORY_MUSIC + "/" + relativePath);
        }
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
        if (!TextUtils.isEmpty(relativePath)) {
            values.put(MediaStore.Video.Media.RELATIVE_PATH, Environment.DIRECTORY_MOVIES + "/" + relativePath);
        }
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
        context.getContentResolver().delete(uri, null, null);
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

    /**
     * 根据音频 URI 获取音频路径
     */
    static String getAudioPath(final Context context, final Uri uri) {
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
                    new String[]{MediaStore.Audio.AudioColumns.DATA}, null,
                    null, null
            );
            if (null != cursor) {
                if (cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(MediaStore.Audio.AudioColumns.DATA);
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
