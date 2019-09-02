package com.sharry.lib.media.recorder;

import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.IOException;
import java.util.Date;

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
    static File createFile(String dirPath, String prefix, String suffix) throws IOException {
        // 1. 生成指定目录
        File dir = new File(dirPath);
        if (!dir.exists() || !dir.isDirectory()) {
            dir.mkdirs();
        }
        // 2. 根据时间创建文件
        File file = new File(dir, prefix + new Date().getTime()
                + suffix);
        if (file.exists()) {
            file.delete();
        }
        file.createNewFile();
        return file;
    }

    static void notifyNewFileCreated(Context context, File file) {
        FileScanner.refresh(context, file);
    }

    static void notifyFileDeleted(Context context, String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return;
        }
        try {
            String where = MediaStore.Audio.Media.DATA + " like \"" + filePath;
            context.getContentResolver().delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, where, null);
        } catch (Throwable throwable) {
            // ignore.
        }
    }

    static class FileScanner implements MediaScannerConnection.MediaScannerConnectionClient {

        private static final String TAG = FileScanner.class.getSimpleName();

        static void refresh(Context context, File file) {
            // 4.0 以上的系统使用 FileScanner 更新
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                new FileScanner(context, file);
            } else {
                Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                intent.setData(Uri.fromFile(file));
                context.sendBroadcast(intent);
            }
        }

        private File mFile;
        private MediaScannerConnection mMsc;

        private FileScanner(Context context, File file) {
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
            if (mFile == null || !mFile.exists()) {
                Log.e(TAG, "Verify failed, scanner target file not exist!");
                return false;
            }
            return true;
        }
    }

}
