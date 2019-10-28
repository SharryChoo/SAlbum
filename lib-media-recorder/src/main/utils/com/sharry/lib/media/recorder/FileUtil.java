package com.sharry.lib.media.recorder;

import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
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
    static File createFile(Context context, String dirPath, String prefix, String suffix) throws IOException {
        // 1. 生成指定目录
        File dir = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q || TextUtils.isEmpty(dirPath) ?
                context.getExternalFilesDir(Environment.DIRECTORY_MOVIES) : new File(dirPath);
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
