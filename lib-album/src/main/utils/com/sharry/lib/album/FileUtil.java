package com.sharry.lib.album;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;

import androidx.core.content.FileProvider;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
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
     * 创建图片地址uri,用于保存拍照后的照片 Android 10以后使用这种方法
     */
    private Uri createImageUri(Context context) {
        String status = Environment.getExternalStorageState();
        // 判断是否有SD卡, 优先使用SD卡存储, 当没有SD卡时使用手机存储
        if (status.equals(Environment.MEDIA_MOUNTED)) {
            return context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new ContentValues());
        } else {
            return context.getContentResolver().insert(MediaStore.Images.Media.INTERNAL_CONTENT_URI, new ContentValues());
        }
    }

    /**
     * 创建拍照文件
     *
     * @param directoryPath 文件目录路径
     */
    static File createCameraDestFile(String directoryPath) {
        Log.e(TAG, "isExternalStorageWritable = " + isExternalStorageWritable());
        // 获取默认路径
        File dir = new File(directoryPath);
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
        if (context == null || TextUtils.isEmpty(filePath)) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            MediaScannerConnection.scanFile(context.getApplicationContext(), new String[]{filePath}, null, null);
        } else {
            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + Environment.getExternalStorageDirectory())));
        }
    }

    static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    static boolean isAndroidQFileExists(Context context, String path) {
        if (context == null) {
            return false;
        }
        AssetFileDescriptor afd = null;
        ContentResolver cr = context.getContentResolver();
        try {
            Uri uri = Uri.parse(path);
            afd = cr.openAssetFileDescriptor(Uri.parse(path), "r");
            if (afd == null) {
                return false;
            } else {
                close(afd);
            }
        } catch (FileNotFoundException e) {
            return false;
        } finally {
            close(afd);
        }
        return true;
    }

    static void close(Closeable... closeables) {
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
