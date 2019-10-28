package com.sharry.lib.album;

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
     * Get last file name associated with this filePath.
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
     * 创建默认文件目录(包名的最后一个字段/系统相册的目录)
     */
    static File createDefaultDirectory(Context context) {
        // 获取默认路径
        File defaultDir = TextUtils.isEmpty(getDefaultName(context)) ?
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) :
                new File(Environment.getExternalStorageDirectory(), getDefaultName(context));
        if (!defaultDir.exists()) {
            defaultDir.mkdirs();
        }
        return defaultDir;
    }

    static File createVideoThumbnailFile(Context context, long videoId) {
        return new File(context.getCacheDir(), "VideoThumbnail_" + videoId + ".jpg");
    }

    /**
     * 根据目的文件路径, 创建临时文件
     *
     * @param directoryPath 目标文件路径
     * @return 创建的文件
     */
    static File createTempFileByDestDirectory(String directoryPath) {
        // 获取临时文件目录
        File tempDirectory = new File(directoryPath);
        if (!tempDirectory.exists()) {
            tempDirectory.mkdirs();
        }
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
    static File createCameraDestFile(Context context, String directoryPath) {
        // 获取默认路径
        File dir = VersionUtil.isQ() || TextUtils.isEmpty(directoryPath) ?
                context.getExternalFilesDir(Environment.DIRECTORY_PICTURES) : new File(directoryPath);
        if (!dir.exists()) {
            boolean res = dir.mkdirs();
            if (!res) {
                Log.e(TAG, "create dir failed -> " + directoryPath);
            }
        }
        // 创建拍照目标文件
        String fileName = "camera_" + DateFormat.format("yyyyMMdd_HH_mm_ss",
                Calendar.getInstance(Locale.CHINA)) + ".jpg";
        File cameraFile = new File(dir, fileName);
        try {
            if (cameraFile.exists()) {
                cameraFile.delete();
            }
            cameraFile.createNewFile();
            Log.i(TAG, "create camera file success -> " + cameraFile.getAbsolutePath());
        } catch (IOException e) {
            Log.e(TAG, "create camera file failed -> " + cameraFile.getAbsolutePath(), e);
        }
        return cameraFile;
    }

    /**
     * 创建拍照文件
     *
     * @param directoryPath 文件目录路径
     */
    static File createCropDestFile(Context context, String directoryPath) {
        // 获取默认路径
        File dir = VersionUtil.isQ() || TextUtils.isEmpty(directoryPath) ?
                context.getExternalFilesDir(Environment.DIRECTORY_PICTURES) : new File(directoryPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        // 创建拍照目标文件
        String fileName = "crop_" + DateFormat.format("yyyyMMdd_HH_mm_ss",
                Calendar.getInstance(Locale.CHINA)) + ".jpg";
        File cropFile = new File(dir, fileName);
        try {
            if (cropFile.exists()) {
                cropFile.delete();
            }
            cropFile.createNewFile();
            Log.i(TAG, "create crop file success -> " + cropFile.getAbsolutePath());
        } catch (IOException e) {
            Log.e(TAG, "create crop file failed -> " + cropFile.getAbsolutePath(), e);
        }
        return cropFile;
    }

    /**
     * 获取默认文件名(包名的最后一个字段)
     */
    private static String getDefaultName(Context context) {
        String packageName = context.getPackageName();
        if (!TextUtils.isEmpty(packageName)) {
            int indexLastDot = packageName.lastIndexOf(".");
            if (indexLastDot != -1) {
                return packageName.substring(indexLastDot + 1);
            }
        }
        return null;
    }

    /**
     * 通知 MediaStore 文件删除了
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
     * 通过 MediaStore 保存，兼容AndroidQ，保存成功自动添加到相册数据库，无需再发送广告告诉系统插入相册
     *
     * @param context       context
     * @param sourceFile    源文件
     * @param destDirectory 目标目录
     * @return 拷贝成功之后的 URI
     */
    @Nullable
    static Uri copyPictureToPublic(Context context, File sourceFile, String destDirectory) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DESCRIPTION, "This is a picture");
        values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/" + getLastFileName(destDirectory));
        values.put(MediaStore.Images.Media.DISPLAY_NAME, sourceFile.getName());
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpg");
        values.put(MediaStore.Images.Media.TITLE, "image.jpg");
        ContentResolver resolver = context.getContentResolver();
        Uri destUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        if (writeToUri(sourceFile, destUri, resolver)) {
            return destUri;
        }
        return null;
    }

    /**
     * 通过 MediaStore 保存，兼容AndroidQ，保存成功自动添加到相册数据库，无需再发送广告告诉系统插入相册
     *
     * @param context       context
     * @param sourceFile    源文件
     * @param destDirectory 目标目录
     * @return 拷贝成功之后的 URI
     */
    @Nullable
    static Uri copyMp4ToPublic(Context context, File sourceFile, String destDirectory) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DESCRIPTION, "This is a video");
        values.put(MediaStore.Images.Media.RELATIVE_PATH, "Videos/" + getLastFileName(destDirectory));
        values.put(MediaStore.Images.Media.DISPLAY_NAME, sourceFile.getName());
        values.put(MediaStore.Images.Media.MIME_TYPE, "video/mp4");
        values.put(MediaStore.Images.Media.TITLE, "video.mp4");
        ContentResolver resolver = context.getContentResolver();
        Uri destUri = resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
        if (writeToUri(sourceFile, destUri, resolver)) {
            return destUri;
        }
        return null;
    }

    private static boolean writeToUri(File sourceFile, Uri destUri, ContentResolver resolver) {
        BufferedInputStream inputStream = null;
        OutputStream os = null;
        try {
            inputStream = new BufferedInputStream(new FileInputStream(sourceFile));
            if (destUri != null) {
                os = resolver.openOutputStream(destUri);
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
        } catch (IOException e) {
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
