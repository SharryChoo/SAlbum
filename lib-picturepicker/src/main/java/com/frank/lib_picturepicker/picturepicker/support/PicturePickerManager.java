package com.frank.lib_picturepicker.picturepicker.support;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;

import com.frank.lib_picturepicker.picturepicker.PicturePickerActivity;

import java.util.ArrayList;

/**
 * Created by Frank on 2018/6/13.
 * Email: frankchoochina@gmail.com
 * Version: 1.1
 * Description: 图片选择器
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

    private Activity mActivity;
    private PicturePickerFragment mPickerFragment;
    private PicturePickerConfig mConfig;

    private PicturePickerManager(@NonNull Activity activity) {
        this.mActivity = activity;
        this.mPickerFragment = getCallbackFragment(mActivity);
        this.mConfig = new PicturePickerConfig();
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
     *
     * @param color color 资源 ID
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
     * 发起请求
     *
     * @param pickerCallback 图片选中的回调
     */
    public void start(@NonNull final PicturePickerCallback pickerCallback) {
        mPickerFragment.verifyPermission(new PicturePickerFragment.PermissionsCallback() {
            @Override
            public void onResult(boolean granted) {
                if (granted) startActual(pickerCallback);
            }
        });
    }

    /**
     * 处理 PicturePickerActivity 的启动
     */
    private void startActual(PicturePickerCallback pickerCallback) {
        final Intent intent = new Intent(mActivity, PicturePickerActivity.class);
        // 用户已经选中的图片数量
        intent.putExtra(PicturePickerActivity.EXTRA_CONFIG, mConfig);
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
