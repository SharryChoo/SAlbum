package frank.demo.photopicker.app_manager;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Process;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import frank.demo.photopicker.utils.FileUtils;

/**
 * Created by 99538 on 2017/5/23.
 */

public class CrashHandler implements Thread.UncaughtExceptionHandler {

    private static final String TAG = "CrashHandler";
    private static final boolean DEBUG = true;
    private Context mContext;
    private static CrashHandler mInstance;
    private Thread.UncaughtExceptionHandler mDefaultCrashHandler;

    private CrashHandler() {
    }

    public static CrashHandler getInstance() {
        if (mInstance == null) {
            mInstance = new CrashHandler();
        }
        return mInstance;
    }

    public void init(Context context) {
        mContext = context;
        mDefaultCrashHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    /**
     * 这是最关键的函数, 当程序中有异常未被捕获时, 系统将激动调用#uncaughtException方法
     * @param thread 为未出现异常的线程
     * @param ex 为未捕获异常, 有了这个exThread我们就可以得到异常的信息
     */
    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        try {
            recordExceptionToCache(ex);
            uploadExceptionToServer();
        } catch (IOException e) {
            e.printStackTrace();
        }
        ex.printStackTrace();

        //如果系统提供了默认的异常处理器, 则交给系统去结束程序, 否则就由自己结束自己
        if(mDefaultCrashHandler != null) {
            mDefaultCrashHandler.uncaughtException(thread, ex);
        } else {
            Process.killProcess(Process.myPid());
        }
    }

    /**记录程序Crash信息*/
    private void recordExceptionToCache(Throwable ex) throws IOException {
        String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(System.currentTimeMillis()));
        String fileName = "crash" + time + ".txt";
        File file = FileUtils.getInstance().createFile(fileName, FileUtils.SD);

        try {
            PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));
            pw.println(time);
            recordPhoneInfo(pw);
            pw.println();
            ex.printStackTrace(pw);
            pw.close();
        } catch (Exception e) {
             Log.e(TAG, "dump crash info failed");
        }
    }

    /**记录手机信息*/
    private void recordPhoneInfo(PrintWriter pw) throws Exception{
        PackageManager pm = mContext.getPackageManager();
        PackageInfo pi = pm.getPackageInfo(mContext.getPackageName(), PackageManager.GET_ACTIVITIES);
        pw.print("App Version: ");
        pw.print(pi.versionName);
        pw.print("_");
        pw.println(pi.versionCode);

        //Android版本号
        pw.print("OS Version: ");
        pw.print(Build.VERSION.RELEASE);
        pw.print("_");
        pw.println(Build.VERSION.SDK_INT);

        //手机制造厂商
        pw.print("Vendor: ");
        pw.println(Build.MANUFACTURER);

        //手机型号
        pw.print("Model: ");
        pw.println(Build.MODEL);

        //CPU架构
        pw.print("CPU ABI: ");
        pw.println(Build.CPU_ABI);
    }

    /**
     * 将错误信息上传到服务器
     */
    private void uploadExceptionToServer() {
        //TODO Uplaod Excepiton Message To Your Web Server
    }

}
