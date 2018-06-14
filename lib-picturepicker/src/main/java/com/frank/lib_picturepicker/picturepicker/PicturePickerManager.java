package com.frank.lib_picturepicker.picturepicker;

import android.Manifest;
import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;

import com.frank.lib_picturepicker.permission.PermissionsCallback;
import com.frank.lib_picturepicker.permission.PermissionsManager;
import com.frank.lib_picturepicker.picturepicker.support.PicturePickerCallback;
import com.frank.lib_picturepicker.picturepicker.support.PicturePickerCallbackFragment;
import com.frank.lib_picturepicker.picturepicker.mvp.view.PicturePickerActivity;

import java.util.ArrayList;

import static com.frank.lib_picturepicker.picturepicker.mvp.view.PicturePickerActivity.EXTRA_PICKED_INDICATOR_BORDER_CHECKED_COLOR;
import static com.frank.lib_picturepicker.picturepicker.mvp.view.PicturePickerActivity.EXTRA_PICKED_INDICATOR_BORDER_UNCHECKED_COLOR;
import static com.frank.lib_picturepicker.picturepicker.mvp.view.PicturePickerActivity.EXTRA_PICKED_INDICATOR_SOLID_COLOR;
import static com.frank.lib_picturepicker.picturepicker.mvp.view.PicturePickerActivity.EXTRA_PICKED_THRESHOLD;
import static com.frank.lib_picturepicker.picturepicker.mvp.view.PicturePickerActivity.EXTRA_SPAN_COUNT;
import static com.frank.lib_picturepicker.picturepicker.mvp.view.PicturePickerActivity.EXTRA_TOOLBAR_BACKGROUND_COLOR;
import static com.frank.lib_picturepicker.picturepicker.mvp.view.PicturePickerActivity.EXTRA_TOOLBAR_BACKGROUND_DRAWABLE_RES;
import static com.frank.lib_picturepicker.picturepicker.mvp.view.PicturePickerActivity.EXTRA_USER_PICKED_PICTURES;

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
    private PicturePickerCallbackFragment mCallbackFragment;
    private ArrayList<String> mPickedPictures = new ArrayList<>();
    private int mThreshold = 9;
    private int mSpanCount = 3;

    // Toolbar 背景色
    private final int INVALIDATE_VALUE = -1;
    private int mToolbarBkgColor = INVALIDATE_VALUE;
    private int mToolbarBkgDrawableResId = INVALIDATE_VALUE;
    private int mIndicatorSolidColor = INVALIDATE_VALUE;
    private int mIndicatorBorderCheckedColor = INVALIDATE_VALUE;
    private int mIndicatorBorderUncheckedColor = INVALIDATE_VALUE;

    private PicturePickerManager(Activity activity) {
        this.mActivity = activity;
        this.mCallbackFragment = getCallbackFragment(mActivity);
    }

    /**
     * 设置相册可选的最大数量
     *
     * @param threshold 阈值
     */
    public PicturePickerManager setThreshold(int threshold) {
        this.mThreshold = threshold;
        return this;
    }

    /**
     * 设置用户已经选中的图片, 相册会根据 Path 比较, 在相册中打钩
     *
     * @param pickedPictures 已选中的图片
     */
    public PicturePickerManager setPickedPictures(@NonNull ArrayList<String> pickedPictures) {
        this.mPickedPictures.addAll(pickedPictures);
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
        this.mToolbarBkgColor = color;
        return this;
    }

    /**
     * 设置 Toolbar 的背景图片
     *
     * @param drawableRes drawable 资源 ID
     */
    public PicturePickerManager setToolbarBackgroundDrawableRes(@DrawableRes int drawableRes) {
        this.mToolbarBkgDrawableResId = drawableRes;
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
        mIndicatorSolidColor = solidColor;
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
        mIndicatorBorderCheckedColor = checkedColor;
        mIndicatorBorderUncheckedColor = uncheckedColor;
        return this;
    }


    public PicturePickerManager setSpanCount(int count) {
        mSpanCount = count;
        return this;
    }

    /**
     * 发起请求
     *
     * @param callback 图片选中的回调
     */
    public void start(@NonNull final PicturePickerCallback callback) {
        // 权限检测
        PermissionsManager.getManager(mActivity)
                .request(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .execute(new PermissionsCallback() {
                    @Override
                    public void onResult(boolean granted) {
                        if (granted) performActivityStart(callback);
                    }
                });
    }

    /**
     * 处理 PicturePickerActivity 的启动
     *
     * @param callback
     */
    private void performActivityStart(PicturePickerCallback callback) {
        final Intent intent = new Intent(mActivity, PicturePickerActivity.class);
        // 用户已经选中的图片数量
        intent.putExtra(EXTRA_USER_PICKED_PICTURES, mPickedPictures);
        intent.putExtra(EXTRA_PICKED_THRESHOLD, mThreshold);
        intent.putExtra(EXTRA_SPAN_COUNT, mSpanCount);
        // Toolbar
        intent.putExtra(EXTRA_TOOLBAR_BACKGROUND_COLOR, mToolbarBkgColor);
        intent.putExtra(EXTRA_TOOLBAR_BACKGROUND_DRAWABLE_RES, mToolbarBkgDrawableResId);
        // 指示器
        intent.putExtra(EXTRA_PICKED_INDICATOR_SOLID_COLOR, mIndicatorSolidColor);
        intent.putExtra(EXTRA_PICKED_INDICATOR_BORDER_CHECKED_COLOR, mIndicatorBorderCheckedColor);
        intent.putExtra(EXTRA_PICKED_INDICATOR_BORDER_UNCHECKED_COLOR, mIndicatorBorderUncheckedColor);
        mCallbackFragment.setCallback(callback);
        mCallbackFragment.startActivityForResult(intent, PicturePickerActivity.REQUEST_CODE);
    }

    /**
     * 获取用于回调的 Fragment
     */
    private PicturePickerCallbackFragment getCallbackFragment(Activity activity) {
        PicturePickerCallbackFragment callbackFragment = findCallbackFragment(activity);
        if (callbackFragment == null) {
            callbackFragment = PicturePickerCallbackFragment.newInstance();
            FragmentManager fragmentManager = activity.getFragmentManager();
            fragmentManager.beginTransaction().add(callbackFragment, TAG).commitAllowingStateLoss();
            fragmentManager.executePendingTransactions();
        }
        return callbackFragment;
    }

    /**
     * 在 Activity 中通过 TAG 去寻找我们添加的 Fragment
     */
    private PicturePickerCallbackFragment findCallbackFragment(Activity activity) {
        return (PicturePickerCallbackFragment) activity.getFragmentManager().findFragmentByTag(TAG);
    }

}
