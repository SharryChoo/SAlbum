package frank.demo.photopicker.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowManager;

import frank.demo.photopicker.R;

/**
 * Created by 99538 on 2017/3/29.
 */
public class AppBarsUtil {

    //StatusBar的状态量
    public static final int ALL_TRANSPARENT = 1;
    public static final int HALF_TRANSPARENT = 2;
    public static final int HIDE = 3;
    public static final int DEFAULT = 4;

    private static AppBarsUtil mInstance;
    private Activity mActivity;
    private String decorViewOptions = "";

    /**构造方法私有化*/
    public static AppBarsUtil newInstance(Activity activity) {
        mInstance = new AppBarsUtil(activity);
        return mInstance;
    }

    private AppBarsUtil(Activity mActivity) {
        this.mActivity = mActivity;
    }

    /**设置StatusBar的风格*/
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public AppBarsUtil setStatusBarStyle(int style) {
        switch(style) {
            //设置状态栏为全透明
            case ALL_TRANSPARENT: {
                int option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
                decorViewOptions = decorViewOptions + String.valueOf(option);
                mActivity.getWindow().setStatusBarColor(Color.TRANSPARENT);
                break;
            }

            //设置状态栏为半透明
            case HALF_TRANSPARENT:{
                mActivity.getWindow().setFlags(
                        WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                        WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                break;
            }

            //隐藏状态栏
            case HIDE:{
                int option = View.SYSTEM_UI_FLAG_FULLSCREEN;
                decorViewOptions = decorViewOptions + String.valueOf(option);

                break;
            }

            //清除透明状态栏
            case DEFAULT: {
                int option = View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
                decorViewOptions = decorViewOptions + String.valueOf(option);

                //获取当前Activity主题中的color
                TypedValue typedValue = new TypedValue();
                mActivity.getTheme().resolveAttribute(R.attr.colorPrimaryDark, typedValue, true);
                int color = typedValue.data;

                mActivity.getWindow().setStatusBarColor(color);
            }
        }
        return mInstance;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public AppBarsUtil setNavigationBarStyle(int style) {
        switch(style) {
            //设置导航栏为全透明
            case ALL_TRANSPARENT: {
                int option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
                decorViewOptions = decorViewOptions + String.valueOf(option);

                mActivity.getWindow().setNavigationBarColor(Color.TRANSPARENT);
                break;
            }

            //设置状态导航栏为半透明
            case HALF_TRANSPARENT:{
                mActivity.getWindow().setFlags(
                        WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION,
                        WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
                break;
            }

            //隐藏导航栏
            case HIDE:{
                int option = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
                decorViewOptions = decorViewOptions + String.valueOf(option);
                break;
            }

            case DEFAULT: {
                //SYSTEM_UI_FLAG_LAYOUT_STABLE该Flag会让主体内容固定在状态栏下方
                int option = View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
                decorViewOptions = decorViewOptions + String.valueOf(option);

                //获取当前Activity主题中的color
                TypedValue typedValue = new TypedValue();
                mActivity.getTheme().resolveAttribute(R.attr.colorPrimaryDark, typedValue, true);
                int color = typedValue.data;

                mActivity.getWindow().setNavigationBarColor(color);
            }
        }

        return mInstance;
    }

    /**隐藏所有Bar(全屏模式)*/
    public AppBarsUtil setAllBarsHide() {
        int option = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

        decorViewOptions = String.valueOf(option);
        return mInstance;
    }

    public void commit() {
        if (decorViewOptions != "") {
            View decorView = mActivity.getWindow().getDecorView();
            int option = Integer.parseInt(decorViewOptions);
            decorView.setSystemUiVisibility(option);
        }
    }

}