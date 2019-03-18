package com.sharry.picturepicker.support.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.text.TextUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 处理图片相关的工具类
 *
 * @author Sharry <a href="SharryChooCHN@Gmail.com">Contact me.</a>
 * @version 1.0
 * @since 2018/9/18 16:23
 */
public class PictureUtil {

    /**
     * 图片压缩
     */
    public static void doCompress(String originPath, String destPath, int quality) throws IOException {
        if (TextUtils.isEmpty(originPath)) {
            throw new IllegalArgumentException("PictureUtil.doCompress -> parameter originFilePath must not be null!");
        }
        if (TextUtils.isEmpty(destPath)) {
            throw new IllegalArgumentException("PictureUtil.doCompress -> parameter destPath must not be null!");
        }
        // 1. 邻近采样压缩尺寸(Nearest Neighbour Resampling Compress)
        BitmapFactory.Options options = getBitmapOptions(originPath);
        Bitmap bitmap = BitmapFactory.decodeFile(originPath, options);
        if (bitmap == null) {
            return;
        }
        // 2. 旋转一下 Bitmap
        bitmap = rotateBitmap(bitmap, readPictureAngle(originPath));
        // 3. 质量压缩(Quality Compress)
        qualityCompress(bitmap, quality, destPath);
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
        if (file.exists()) {
            file.delete();
        }
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
        if (angle == 0) {
            return bitmap;
        }
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
            default:
                break;
        }
        return degree;
    }

}
