package com.frank.picturepicker.picturepicker.manager;

import android.Manifest;
import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;

import com.frank.picturepicker.picturepicker.impl.ui.PicturePickerActivity;
import com.frank.picturepicker.support.loader.IPictureLoader;
import com.frank.picturepicker.support.loader.PictureLoader;
import com.frank.picturepicker.support.permission.PermissionsCallback;
import com.frank.picturepicker.support.permission.PermissionsManager;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Frank on 2018/6/13.
 * Email: frankchoochina@gmail.com
 * Version: 1.1
 * Description: 图片选择器的管理类
 */
public class PicturePickerManager {

    public static final String TAG = PicturePickerManager.class.getSimpleName();

    public static PicturePickerManager with(@NonNull Context context) {
        if (context instanceof Activity) {
            Activity activity = (Activity) context;
            return new PicturePickerManager(activity);
        } else {
            throw new IllegalArgumentException("PicturePickerManager.with -> Context can not cast to Activity");
        }
    }

    private String[] mPermissions = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };
    private Activity mActivity;
    private PicturePickerFragment mPickerFragment;
    private PickerConfig mConfig;

    private PicturePickerManager(@NonNull Activity activity) {
        this.mActivity = activity;
        this.mPickerFragment = getCallbackFragment(mActivity);
        this.mConfig = new PickerConfig();
    }

    /**
     * 设置相册可选的最大数量
     *
     * @param threshold 阈值
     */
    public PicturePickerManager setThreshold(int threshold) {
        mConfig.threshold = threshold;
        return this;
    }

    /**
     * 设置用户已经选中的图片, 相册会根据 Path 比较, 在相册中打钩
     *
     * @param pickedPictures 已选中的图片
     */
    public PicturePickerManager setPickedPictures(@NonNull ArrayList<String> pickedPictures) {
        mConfig.userPickedSet.addAll(pickedPictures);
        return this;
    }

    public PicturePickerManager setSpanCount(int count) {
        mConfig.spanCount = count;
        return this;
    }

    /**
     * 设置 Toolbar 的背景色
     *
     * @param colorId color 资源 ID
     */
    public PicturePickerManager setToolbarBackgroundColorRes(@ColorRes int colorId) {
        return setToolbarBackgroundColor(ContextCompat.getColor(mActivity, colorId));
    }

    /**
     * 设置 Toolbar 的背景色
     */
    public PicturePickerManager setToolbarBackgroundColor(@ColorInt int color) {
        mConfig.toolbarBkgColor = color;
        return this;
    }

    /**
     * 设置 Toolbar 的背景图片
     *
     * @param drawableRes drawable 资源 ID
     */
    public PicturePickerManager setToolbarBackgroundDrawableRes(@DrawableRes int drawableRes) {
        mConfig.toolbarBkgDrawableResId = drawableRes;
        return this;
    }

    /**
     * 设置图片选择器的背景色
     *
     * @param colorId color 资源 ID
     */
    public PicturePickerManager setPickerBackgroundColorRes(@ColorRes int colorId) {
        return setPickerBackgroundColor(ContextCompat.getColor(mActivity, colorId));
    }

    /**
     * 设置图片选择器的背景色
     */
    public PicturePickerManager setPickerBackgroundColor(@ColorInt int color) {
        mConfig.pickerBackgroundColor = color;
        return this;
    }

    /**
     * 设置图片选择器的Item背景色
     *
     * @param colorId color 资源 ID
     */
    public PicturePickerManager setPickerItemBackgroundColorRes(@ColorRes int colorId) {
        return setPickerItemBackgroundColor(ContextCompat.getColor(mActivity, colorId));
    }

    /**
     * 设置图片选择器的背景色
     */
    public PicturePickerManager setPickerItemBackgroundColor(@ColorInt int color) {
        mConfig.pickerItemBackgroundColor = color;
        return this;
    }

    /**
     * 设置选择索引的边框颜色
     *
     * @param textColorId 边框的颜色 ID
     */
    public PicturePickerManager setIndicatorTextColorRes(@ColorRes int textColorId) {
        return setIndicatorTextColor(ContextCompat.getColor(mActivity, textColorId));
    }

    /**
     * 设置选择索引的边框颜色
     *
     * @param textColor 边框的颜色
     */
    public PicturePickerManager setIndicatorTextColor(@ColorInt int textColor) {
        mConfig.indicatorTextColor = textColor;
        return this;
    }

    /**
     * 设置选择索引的边框颜色
     *
     * @param solidColorId 边框的颜色 ID
     */
    public PicturePickerManager setIndicatorSolidColorRes(@ColorRes int solidColorId) {
        return setIndicatorSolidColor(ContextCompat.getColor(mActivity, solidColorId));
    }

    /**
     * 设置选择索引的边框颜色
     *
     * @param solidColor 边框的颜色
     */
    public PicturePickerManager setIndicatorSolidColor(@ColorInt int solidColor) {
        mConfig.indicatorSolidColor = solidColor;
        return this;
    }

    /**
     * 设置选择索引的边框颜色
     *
     * @param checkedColorId   选中的边框颜色
     * @param uncheckedColorId 未选中的边框颜色
     */
    public PicturePickerManager setIndicatorBorderColorRes(@ColorRes int checkedColorId, @ColorRes int uncheckedColorId) {
        return setIndicatorBorderColor(ContextCompat.getColor(mActivity, checkedColorId),
                ContextCompat.getColor(mActivity, uncheckedColorId));
    }

    /**
     * 设置选择索引的边框颜色
     *
     * @param checkedColor   选中的边框颜色的 Res Id
     * @param uncheckedColor 未选中的边框颜色的Res Id
     */
    public PicturePickerManager setIndicatorBorderColor(@ColorInt int checkedColor, @ColorInt int uncheckedColor) {
        mConfig.indicatorBorderCheckedColor = checkedColor;
        mConfig.indicatorBorderUncheckedColor = uncheckedColor;
        return this;
    }

    /**
     * 是否设置 Behavior 滚动动画
     */
    public PicturePickerManager isShowScrollBehavior(boolean isShowScrollBehavior) {
        mConfig.isShowScrollBehavior = isShowScrollBehavior;
        return this;
    }

    /**
     * 设置图片加载方案
     */
    public PicturePickerManager setPictureLoader(@NonNull IPictureLoader loader) {
        PictureLoader.setPictureLoader(loader);
        return this;
    }

    /**
     * FileProvider 的 authority
     */
    public PicturePickerManager setFileProviderAuthority(String authority) {
        this.mConfig.authority = authority;
        return this;
    }

    /**
     * 开启相机支持
     */
    public PicturePickerManager setCameraSupport(boolean isCameraSupport) {
        mConfig.isCameraSupport = isCameraSupport;
        return this;
    }

    /**
     * 相机的图标 ID
     */
    public PicturePickerManager setCameraIconDrawableRes(int cameraIconDrawableResId) {
        mConfig.cameraIconDrawableResId = cameraIconDrawableResId;
        return this;
    }

    /**
     * 设置相机拍摄存储的路径文件夹
     */
    public PicturePickerManager setCameraDestDirectory(@NonNull String directoryPath) {
        this.mConfig.cameraDirectoryPath = directoryPath;
        return this;
    }

    /**
     * 设置拍照后的压缩质量
     */
    public PicturePickerManager setCameraDestQuality(int quality) {
        mConfig.cameraDestQuality = quality;
        return this;
    }

    /**
     * 开启裁剪支持
     */
    public PicturePickerManager setCropSupport(boolean isCropSupport) {
        mConfig.isCropSupport = isCropSupport;
        return this;
    }

    /**
     * 设置是否为圆形裁剪区域
     */
    public PicturePickerManager setCropCircle(boolean isCropCircle) {
        mConfig.isCropCircle = isCropCircle;
        return this;
    }

    /**
     * 裁剪的宽高的设置
     */
    public PicturePickerManager setCropSize(int width, int height) {
        mConfig.cropWidth = width;
        mConfig.cropHeight = height;
        return this;
    }

    /**
     * 设置目的文件
     */
    public PicturePickerManager setCropDestFilePath(@NonNull String filePath) {
        this.mConfig.cropDestFilePath = filePath;
        return this;
    }

    /**
     * 设置裁剪后的压缩质量
     */
    public PicturePickerManager setCropDestQuality(int quality) {
        mConfig.cropDestQuality = quality;
        return this;
    }

    /**
     * 发起请求
     *
     * @param pickerCallback 图片选中的回调
     */
    public void start(@NonNull final PickerCallback pickerCallback) {
        // 1. 验证是否实现了图片加载器
        if (PictureLoader.getPictureLoader() == null) {
            throw new UnsupportedOperationException("PictureLoader.load -> please invoke setPictureLoader first");
        }
        // 2. 验证权限
        PermissionsManager.getManager(mActivity)
                .request(mPermissions)
                .execute(new PermissionsCallback() {
                    @Override
                    public void onResult(boolean granted) {
                        if (granted) startActual(pickerCallback);
                    }
                });
    }

    /**
     * 处理 PicturePickerActivity 的启动
     */
    private void startActual(PickerCallback pickerCallback) {
        // 若用户设置了相机拍摄存储的目录, 则尝试创建目录
        if (!TextUtils.isEmpty(mConfig.cameraDirectoryPath)) {
            File file = new File(mConfig.cameraDirectoryPath);
            if (!file.exists()) file.mkdirs();
        } else {
            mConfig.cameraDirectoryPath = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DCIM).getAbsolutePath();
        }
        // 若开启了裁剪, 则只能选中一张图片
        if (mConfig.isCropSupport) {
            mConfig.threshold = 1;
            mConfig.userPickedSet = null;
        }
        Intent intent = new Intent(mActivity, PicturePickerActivity.class);
        // 用户已经选中的图片数量
        intent.putExtra(PicturePickerActivity.START_INTENT_EXTRA_CONFIG, mConfig);
        mPickerFragment.setPickerCallback(pickerCallback);
        mPickerFragment.startActivityForResult(intent, PicturePickerFragment.REQUEST_CODE_PICKED);
    }

    /**
     * 获取用于回调的 Fragment
     */
    private PicturePickerFragment getCallbackFragment(Activity activity) {
        PicturePickerFragment pickerFragment = findCallbackFragment(activity);
        if (pickerFragment == null) {
            pickerFragment = PicturePickerFragment.newInstance();
            FragmentManager fragmentManager = activity.getFragmentManager();
            fragmentManager.beginTransaction().add(pickerFragment, TAG).commitAllowingStateLoss();
            fragmentManager.executePendingTransactions();
        }
        return pickerFragment;
    }

    /**
     * 在 Activity 中通过 TAG 去寻找我们添加的 Fragment
     */
    private PicturePickerFragment findCallbackFragment(Activity activity) {
        return (PicturePickerFragment) activity.getFragmentManager().findFragmentByTag(TAG);
    }
}
