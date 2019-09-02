package com.sharry.lib.picturepicker;

import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
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
        String tempFileName = "temp_file_" + DateFormat.format("yyyyMMdd_HHmmss", Calendar.getInstance(Locale.CHINA)) + ".jpg";
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
    static File createCameraDestFile(String directoryPath) {
        // 获取默认路径
        File dir = new File(directoryPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        // 创建拍照目标文件
        String fileName = "camera_" + DateFormat.format("yyyyMMdd_HHmmss",
                Calendar.getInstance(Locale.CHINA)) + ".jpg";
        File cameraFile = new File(dir, fileName);
        try {
            if (cameraFile.exists()) cameraFile.delete();
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
    static File createCropDestFile(String directoryPath) {
        // 获取默认路径
        File dir = new File(directoryPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        // 创建拍照目标文件
        String fileName = "crop_" + DateFormat.format("yyyyMMdd_HHmmss",
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
     * 创建默认的 FileProvider 的 Authority
     */
    static String getDefaultFileProviderAuthority(Context context) {
        return context.getPackageName() + ".FileProvider";
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
        if (TextUtils.isEmpty(filePath)) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            MediaScannerConnection.scanFile(context, new String[]{filePath}, null, null);
        } else {
            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + Environment.getExternalStorageDirectory())));
        }
    }

}
