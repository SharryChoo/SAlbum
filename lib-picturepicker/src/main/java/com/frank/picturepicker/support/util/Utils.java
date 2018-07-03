package com.frank.picturepicker.support.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Frank on 2018/6/21.
 * Email: frankchoochina@gmail.com
 * Version: 1.0
 * Description: 图片采集的工具类
 */
public class Utils {

    private static final String TAG = Utils.class.getSimpleName();

    /**
     * 图片压缩
     */
    public static void doCompress(String originPath, String destPath, int quality) throws IOException {
        if (TextUtils.isEmpty(originPath)) {
            throw new IllegalArgumentException("Utils.doCompress -> parameter originFilePath must not be null!");
        }
        if (TextUtils.isEmpty(destPath)) {
            throw new IllegalArgumentException("Utils.doCompress -> parameter destPath must not be null!");
        }
        // 1. 邻近采样压缩尺寸(Nearest Neighbour Resampling Compress)
        BitmapFactory.Options options = getBitmapOptions(originPath);
        Bitmap bitmap = BitmapFactory.decodeFile(originPath, options);
        if (bitmap == null) return;
        // 2. 旋转一下 Bitmap
        bitmap = rotateBitmap(bitmap, readPictureAngle(originPath));
        // 3. 质量压缩(Quality Compress)
        qualityCompress(bitmap, quality, destPath);
    }

    /**
     * 刷新文件管理器
     */
    public static void freshMediaStore(Context context, File file) {
        MediaScanner.refresh(context, file);
    }

    /**
     * 获取 URI
     */
    public static Uri getUriFromFile(Context context, String authority, File file) {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ?
                FileProvider.getUriForFile(context, authority, file) : Uri.fromFile(file);
    }

    /**
     * 创建默认文件目录(包名的最后一个字段/系统相册的目录)
     */
    public static File createDefaultDirectory(Context context) {
        // 获取默认路径
        File defaultDir = TextUtils.isEmpty(getDefaultName(context)) ?
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) :
                new File(Environment.getExternalStorageDirectory(), getDefaultName(context));
        if (!defaultDir.exists()) defaultDir.mkdirs();
        return defaultDir;
    }

    /**
     * 根据目的文件路径, 创建临时文件
     *
     * @param directoryPath 目标文件路径
     * @return 创建的文件
     */
    public static File createTempFileByDestDirectory(String directoryPath) {
        // 获取临时文件目录
        File tempDirectory = new File(directoryPath);
        if (!tempDirectory.exists()) tempDirectory.mkdirs();
        // 创建临时文件
        String tempFileName = "temp_file_" + DateFormat.format("yyyyMMdd_HHmmss", Calendar.getInstance(Locale.CHINA)) + ".jpg";
        File tempFile = new File(tempDirectory, tempFileName);
        try {
            if (tempFile.exists()) tempFile.delete();
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
    public static File createCameraDestFile(String directoryPath) {
        // 获取默认路径
        File dir = new File(directoryPath);
        if (!dir.exists()) dir.mkdirs();
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
    public static File createCropDestFile(String directoryPath) {
        // 获取默认路径
        File dir = new File(directoryPath);
        if (!dir.exists()) dir.mkdirs();
        // 创建拍照目标文件
        String fileName = "crop_" + DateFormat.format("yyyyMMdd_HHmmss",
                Calendar.getInstance(Locale.CHINA)) + ".jpg";
        File cropFile = new File(dir, fileName);
        try {
            if (cropFile.exists()) cropFile.delete();
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
    public static String getDefaultFileProviderAuthority(Context context) {
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
     * 解析图片文件的宽高与目标宽高, 获取 Bitmap.Options
     *
     * @param filePath 文件路径
     * @return 获取 Bitmap.Options
     */
    private static BitmapFactory.Options getBitmapOptions(String filePath) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);
        options.inSampleSize = calculateSampleSize(options.outWidth, options.outHeight);
        options.inJustDecodeBounds = false;
        return options;
    }

    /**
     * 根据主流屏幕自适应计算采样率
     *
     * @param srcWidth  原始宽度
     * @param srcHeight 原始高度
     * @return 采样率
     */
    private static int calculateSampleSize(int srcWidth, int srcHeight) {
        //将 srcWidth 和 srcHeight 设置为偶数，方便除法计算
        srcWidth = srcWidth % 2 == 1 ? srcWidth + 1 : srcWidth;
        srcHeight = srcHeight % 2 == 1 ? srcHeight + 1 : srcHeight;

        int longSide = Math.max(srcWidth, srcHeight);
        int shortSide = Math.min(srcWidth, srcHeight);

        float scale = ((float) shortSide / longSide);
        if (scale <= 1 && scale > 0.5625) {
            if (longSide < 1664) {
                return 1;
            } else if (longSide >= 1664 && longSide < 4990) {
                return 2;
            } else if (longSide > 4990 && longSide < 10240) {
                return 4;
            } else {
                return longSide / 1280 == 0 ? 1 : longSide / 1280;
            }
        } else if (scale <= 0.5625 && scale > 0.5) {
            return longSide / 1280 == 0 ? 1 : longSide / 1280;
        } else {
            return (int) Math.ceil(longSide / (1280.0 / scale));
        }
    }

    /**
     * Bitmap 质量压缩
     *
     * @param srcBitmap    原始 Bitmap
     * @param quality      压缩质量
     * @param destFilePath 压缩后的文件
     */
    private static void qualityCompress(Bitmap srcBitmap, int quality, String destFilePath) throws IOException {
        File file = new File(destFilePath);
        if (file.exists()) file.delete();
        file.createNewFile();
        // 进行质量压缩
        FileOutputStream out = new FileOutputStream(file);
        // 采用有损的 jpeg 图片压缩
        srcBitmap.compress(Bitmap.CompressFormat.JPEG, quality, out);
        out.flush();
        out.close();
    }

    /**
     * 旋转 Bitmap
     *
     * @param bitmap 原始 bitmap
     * @param angle  旋转的角度
     */
    private static Bitmap rotateBitmap(Bitmap bitmap, int angle) {
        if (angle == 0) return bitmap;
        //旋转图片 动作
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        // 旋转后的 Bitmap
        return Bitmap.createBitmap(bitmap, 0, 0,
                bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    /**
     * 读取图片文件旋转的角度
     *
     * @param imagePath 文件路径
     */
    private static int readPictureAngle(String imagePath) throws IOException {
        int degree = 0;
        ExifInterface exifInterface = new ExifInterface(imagePath);
        int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                degree = 90;
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                degree = 180;
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                degree = 270;
                break;
        }
        return degree;
    }

    /**
     * 用于通知文件管理器变更
     */
    private static class MediaScanner implements MediaScannerConnection.MediaScannerConnectionClient {

        private static final String TAG = MediaScanner.class.getSimpleName();

        private static void refresh(Context context, File file) {
            // 4.0 以上的系统使用 MediaScanner 更新
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                new MediaScanner(context, file);
            } else {
                Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                intent.setData(Uri.fromFile(file));
                context.sendBroadcast(intent);
            }
        }

        private File mFile;
        private MediaScannerConnection mMsc;

        private MediaScanner(Context context, File file) {
            this.mFile = file;
            if (verify()) {
                this.mMsc = new MediaScannerConnection(context, this);
                mMsc.connect();
            }
        }

        @Override
        public void onMediaScannerConnected() {
            mMsc.scanFile(mFile.getAbsolutePath(), null);
        }

        @Override
        public void onScanCompleted(String path, Uri uri) {
            mMsc.disconnect();
        }

        /**
         * 验证文件的合法性
         */
        private boolean verify() {
            if (!mFile.exists()) {
                Log.e(TAG, "Verify failed, scanner target file not exist!");
                return false;
            }
            return true;
        }

    }
}
