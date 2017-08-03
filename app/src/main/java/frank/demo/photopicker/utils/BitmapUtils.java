package frank.demo.photopicker.utils;

import android.graphics.Bitmap;
import android.graphics.Matrix;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by 99538 on 2017/7/22.
 */

public class BitmapUtils {

    /**
     * 旋转Bitmap
     */
    public static Bitmap rotateBitmap(Bitmap bitmap, int rotate) {
        Bitmap newBitmap;
        //判断是否需要旋转
        if(bitmap.getWidth() < bitmap.getHeight()) {
            return bitmap;
        }else {
            Matrix matrix = new Matrix();
            matrix.reset();
            matrix.postRotate(rotate);//顺时针旋转
            newBitmap = Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),matrix,true);
        }
        //释放资源
        if (!bitmap.isRecycled()) {
            bitmap.recycle();
        }
        return newBitmap;
    }

    /**
     * 将Bitmap写入用户自定义文件
     */
    public static void writeBitmapToFile(File file, Bitmap bitmap, int quality)  {
        try {
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, quality, out);
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
