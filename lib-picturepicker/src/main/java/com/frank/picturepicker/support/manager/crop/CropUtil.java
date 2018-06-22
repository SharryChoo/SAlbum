package com.frank.picturepicker.support.manager.crop;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Frank on 2018/6/21.
 * Email: frankchoochina@gmail.com
 * Version: 1.0
 * Description: 图片裁剪的工具类
 */
class CropUtil {

    /**
     * Bitmap 质量压缩
     *
     * @param srcBitmap    原始 Bitmap
     * @param quality      压缩质量
     * @param destFilePath 压缩后的文件
     */
    public static void qualityCompress(Bitmap srcBitmap, int quality, String destFilePath) throws IOException {
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
     * 刷新文件管理器
     */
    static void freshMediaStore(Context context, File file) {
        MediaScanner.refresh(context, file);
    }

    /**
     * 获取 URI
     */
    static Uri getUriFromFile(Context context, String authority, File file) {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ?
                FileProvider.getUriForFile(context, authority, file) : Uri.fromFile(file);
    }

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
