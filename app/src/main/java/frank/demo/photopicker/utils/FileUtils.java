package frank.demo.photopicker.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v4.content.FileProvider;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.UUID;

import frank.demo.photopicker.app_manager.MyApp;

/**
 * Created by 99538035 on 2016/12/28.
 */
public class FileUtils {

    public static final int CACHE = 0;//App缓存目录
    public static final int SD = 1;//SD卡目录

    private static volatile FileUtils sInstance;

    private Context mContext;

    private File mCacheDirectory;//App专用缓存目录, 用户无法从文件管理器中浏览

    private File mStorageDirectory;//SD卡目录

    /**构造方法私有化*/
    public static FileUtils getInstance() {
        if(sInstance == null) {
            synchronized (FileUtils.class) {
                if (sInstance == null) {
                    sInstance = new FileUtils();
                }
            }
        }
        return sInstance;
    }

    private FileUtils() {
        this.mContext = MyApp.getContext();
        mStorageDirectory = getFileDir(SD);
        mCacheDirectory = getFileDir(CACHE);
    }

    private File getFileDir(int type) {
        File fileDirectory = null;
        switch (type) {
            case SD: {
                String rootDir = Environment.getExternalStorageDirectory().getAbsolutePath();
                String addDir = File.separator + getApplicationName(mContext) + File.separator;
                //创建目录用于存放用户保存内容
                fileDirectory = new File(rootDir, addDir);
                break;
            }
            case CACHE: {
                String cacheDir = mContext.getExternalCacheDir().toString();
                String addDir = File.separator + "appCache" + File.separator;
                //创建文件目录用于存放该App缓存
                fileDirectory = new File(cacheDir, addDir);
                break;
            }
        }
        //如果文件路径不存在则自动追加目录
        if (!fileDirectory.getParentFile().exists()) {
            fileDirectory.mkdirs();
        }

        return fileDirectory;
    }

    /**
     * 创建随机数命名文件
     */
    public File createRandomNameFile(int type) {
        String fileName = UUID.randomUUID().toString() + ".png";
        File randomNameFile = null;
        switch(type) {
            case CACHE: {
                randomNameFile = new File(mCacheDirectory, fileName);
                break;
            }
            case SD: {
                randomNameFile = new File(mStorageDirectory, fileName);
                break;
            }
        }

        return randomNameFile;
    }

    /**
     * 创建日期命名文件
     */
    public File createSimpleDateNameFile(int type) {
        //获取当前时间
        Calendar now = new GregorianCalendar();
        SimpleDateFormat simpleDate = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
        String fileName = simpleDate.format(now.getTime()) + ".png";
        File simpleDateNameFile = null;
        switch(type) {
            case CACHE: {
                simpleDateNameFile = new File(mCacheDirectory, fileName);
                break;
            }
            case SD: {
                simpleDateNameFile = new File(mStorageDirectory, fileName);
                break;
            }
        }
        
        return simpleDateNameFile;
    }

    /**
     * 创建指定名文件
     */
    public File createFile(String fileName, int type) {
        File file = null;
        switch(type) {
            case CACHE: {
                file = new File(mCacheDirectory, fileName);
                break;
            }
            case SD: {
                file = new File(mStorageDirectory, fileName);
                break;
            }
        }
        return file;
    }

    /**
     * 获取文件的Uri
     */
    public Uri getUri(File file) {
        Uri uri;
        if (Build.VERSION.SDK_INT >= 24) {
            //安卓7.0以上的版本需要用到FileProvider来获取Uri的真实地址, 需要与Manifest中的一致
            uri = FileProvider.getUriForFile(mContext, "frank.choo.demo.fileprovider", file);
        } else {
            uri = Uri.fromFile(file);//调用Uri的fromFile()方法将File对象转换成Uri对象,标识着outputImage的唯一地址
        }
        return uri;
    }

    /**
     * 删除指定文件或文件夹下所有文件
     */
    public boolean deleteFile(File file) {
        //指定文件是否存在
        if (file.exists()) {
            if (file.isFile()) {//判断file是否为一个标准文件
                file.delete();//删除该文件
            } else if (file.isDirectory()) {//判断file是否为文件路径
                File[] files = file.listFiles();
                for (File f : files) {
                    deleteFile(f);//递归删除该路径下的所有文件
                }
            } else {
                file.delete();
            }
        }
        return true;
    }
    
    /**
     * 删除所有缓存文件
     */
    public boolean deleteAllCacheFile() {
        File file = mCacheDirectory;
        //指定文件是否存在
        if (file.exists()) {
            if (file.isFile()) {//判断file是否为一个标准文件
                file.delete();//删除该文件
            } else if (file.isDirectory()) {//判断file是否为文件路径
                File[] files = file.listFiles();
                for (File f : files) {
                    deleteFile(f);//递归删除该路径下的所有文件
                }
            } else {
                file.delete();
            }
        }
        return true;
    }

    /**
     * 用于更新文件管理器,否则保存的图片不会更新
     */
    public void notifyFileChanged(String filePath) {
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        intent.setData(Uri.fromFile(new File(filePath)));
        mContext.sendBroadcast(intent);
    }

    /**
     * 获取AppName用于创建存储路径
     */
    public String getApplicationName(Context context) {
        PackageManager packageManager = null;
        ApplicationInfo applicationInfo = null;
        try {
            packageManager = context.getApplicationContext().getPackageManager();
            applicationInfo = packageManager.getApplicationInfo(context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            applicationInfo = null;
        }
        String applicationName =
                (String) packageManager.getApplicationLabel(applicationInfo);
        return applicationName;
    }
}
