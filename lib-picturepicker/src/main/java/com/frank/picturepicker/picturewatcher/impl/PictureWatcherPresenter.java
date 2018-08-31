package com.frank.picturepicker.picturewatcher.impl;

import android.os.Handler;
import android.os.Looper;
import android.transition.ChangeBounds;
import android.transition.ChangeImageTransform;
import android.transition.Fade;
import android.transition.Slide;
import android.transition.Transition;
import android.transition.TransitionSet;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;

import com.frank.picturepicker.R;
import com.frank.picturepicker.picturewatcher.manager.WatcherConfig;
import com.frank.picturepicker.support.util.Utils;

import java.text.MessageFormat;
import java.util.ArrayList;

/**
 * Created by Frank on 2018/7/20.
 * Email: frankchoochina@gmail.com
 * Version: 1.0
 * Description:
 */
class PictureWatcherPresenter implements PictureWatcherContract.IPresenter, PictureWatcherPreviewAdapter.AdapterInteraction {

    private PictureWatcherContract.IView mView;
    private WatcherConfig mConfig;
    private ArrayList<String> mDisplayPaths;
    private ArrayList<String> mPickedPaths;

    private int mCurPosition = -1;
    private String mCurDisplayPath;
    private String mSharedKey;// 共享元素的 Key
    // Flags
    private boolean mIsSharedElement = false;// 是否支持共享元素
    private boolean mIsSupportPicked = false;// 是否支持图片选择
    private boolean mIsEnsurePressed = false;// 是否支持图片选择

    @Override
    public void attach(PictureWatcherContract.IView view) {
        mView = view;
    }

    @Override
    public void init(WatcherConfig config, boolean isSharedElement) {
        this.mConfig = config;
        // 获取需要展示图片的 URI 集合
        mDisplayPaths = config.pictureUris == null ? new ArrayList<String>() : config.pictureUris;
        // 获取已经选中的图片
        mPickedPaths = config.userPickedSet;
        mIsSupportPicked = mPickedPaths != null;
        // 获取当前需要展示的 Position 和 URI
        mCurPosition = config.position;
        mCurDisplayPath = mDisplayPaths.get(mCurPosition);
        // 判断是否开启共享动画
        if (!Utils.isLollipop()) return;
        mView.setWindowEnterTransitions(new Slide().setDuration(300));
        mView.setWindowReturnTransitions(new Fade().setDuration(300));
        mIsSharedElement = isSharedElement;
        if (!mIsSharedElement) return;
        mSharedKey = mCurDisplayPath;
        // 设置共享元素场景切换动画
        mView.setWindowEnterTransitions(new Fade().setDuration(500));
        // 设置共享元素进入动画
        ChangeBounds elementEnterTransition = new ChangeBounds();
        elementEnterTransition.setDuration(500);
        elementEnterTransition.setInterpolator(new OvershootInterpolator(1f));
        mView.setSharedElementEnterTransition(elementEnterTransition);
        // 设置共享元素退出动画
        TransitionSet elementReturnTransition = new TransitionSet();
        Transition transition1 = new ChangeBounds();
        Transition transition2 = new ChangeImageTransform();
        transition1.setInterpolator(new OvershootInterpolator(0.5f));
        transition2.setInterpolator(new OvershootInterpolator(0.5f));
        elementReturnTransition.addTransition(transition1);
        elementReturnTransition.addTransition(transition2);
        elementReturnTransition.setDuration(400);
        mView.setSharedElementReturnTransition(elementReturnTransition);
    }

    @Override
    public void fetchData() {
        // 1. 设置 Toolbar 数据
        mView.displayToolbarLeftText(buildToolbarLeftText());
        mView.setToolbarCheckedIndicatorVisibility(mIsSupportPicked);

        // 2. 设置 Pictures 数据
        mView.createPhotoViews(mDisplayPaths);
        if (mIsSharedElement) {
            // 绑定共享元素
            mView.bindSharedElementView(mDisplayPaths.indexOf(mSharedKey), mSharedKey);
            // 通知共享元素位置变更了
            mView.notifySharedElementChanged(mDisplayPaths.indexOf(mSharedKey), mSharedKey);
        }
        mView.displayPictureAt(mDisplayPaths, mCurPosition);

        // 3. 设置底部菜单和按钮选中的状态
        if (mIsSupportPicked) {
            mView.setToolbarCheckedIndicatorColors(mConfig.indicatorBorderCheckedColor,
                    mConfig.indicatorBorderUncheckedColor, mConfig.indicatorSolidColor, mConfig.indicatorTextColor);
            mView.setToolbarIndicatorChecked(mPickedPaths.indexOf(mCurDisplayPath) != -1);
            mView.displayToolbarIndicatorText(buildToolbarCheckedIndicatorText());
            // 底部菜单
            mView.setPreviewAdapter(new PictureWatcherPreviewAdapter(mPickedPaths, this));
            mView.displayPreviewEnsureText(buildEnsureText());
            // 底部菜单延时弹出
            if (!mPickedPaths.isEmpty()) {
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mView.setBottomPreviewVisibility(false, true);
                    }
                }, mIsSharedElement ? 500 : 0);
            }
        }
    }

    @Override
    public void handlePagerChanged(int position) {
        // 更新数据
        mCurPosition = position;
        mCurDisplayPath = mDisplayPaths.get(position);
        // 展示 Toolbar 左边的指示文本
        mView.displayToolbarLeftText(buildToolbarLeftText());
        // 展示图片
        mView.displayPictureAt(mDisplayPaths, mCurPosition);
        if (mIsSupportPicked) {
            mView.setToolbarIndicatorChecked(mPickedPaths.indexOf(mCurDisplayPath) != -1);
            mView.displayToolbarIndicatorText(buildToolbarCheckedIndicatorText());
            mView.displayPreviewEnsureText(buildEnsureText());
        }
        if (mIsSharedElement) {
            int sharedPosition = mDisplayPaths.indexOf(mSharedKey);
            mView.notifySharedElementChanged(sharedPosition,
                    position == sharedPosition ? mSharedKey : "");
        }
    }

    @Override
    public void handleToolbarCheckedIndicatorClick(boolean isChecked) {
        boolean nowVisible = !mPickedPaths.isEmpty();
        if (isChecked) {
            // 移除选中数据与状态
            int removedIndex = mPickedPaths.indexOf(mCurDisplayPath);
            mPickedPaths.remove(removedIndex);
            // 通知 RecyclerView 数据变更
            mView.notifyBottomPicturesRemoved(mCurDisplayPath, removedIndex);
        } else {
            // 判断是否达到选择上限
            if (mPickedPaths.size() < mConfig.threshold) {
                mPickedPaths.add(mCurDisplayPath);
                int addedIndex = mPickedPaths.indexOf(mCurDisplayPath);
                // 通知 RecyclerView 数据变更
                mView.notifyBottomPictureAdded(mCurDisplayPath, addedIndex);
                mView.previewPicturesSmoothScrollToPosition(addedIndex);
            } else {
                mView.showMsg(
                        mView.getString(R.string.libpicturepicker_picturewatcher_tips_over_threshold_prefix) +
                                mConfig.threshold +
                                mView.getString(R.string.libpicturepicker_picturewatcher_tips_over_threshold_suffix)
                );
            }
        }
        mView.setToolbarIndicatorChecked(mPickedPaths.indexOf(mCurDisplayPath) != -1);
        mView.displayToolbarIndicatorText(buildToolbarCheckedIndicatorText());
        mView.displayPreviewEnsureText(buildEnsureText());
        mView.setBottomPreviewVisibility(nowVisible, !mPickedPaths.isEmpty());
    }

    @Override
    public void handleEnsureClick() {
        if (mPickedPaths.isEmpty()) {
            mView.showMsg(mView.getString(R.string.libpicturepicker_picturewatcher_tips_ensure_failed));
            return;
        }
        if (Utils.isLollipop()) {
            mView.setWindowReturnTransitions(null);
            mView.setSharedElementReturnTransition(null);
        }
        mIsEnsurePressed = true;
        mView.finish();
    }

    @Override
    public void handleBackPressed() {
        mView.finish();
    }

    @Override
    public ArrayList<String> getUserPicked() {
        return mPickedPaths;
    }

    @Override
    public boolean isEnsurePressed() {
        return mIsEnsurePressed;
    }

    @Override
    public void onPreviewItemClicked(ImageView imageView, String uri, int position) {
        int indexOfPictureUris = mDisplayPaths.indexOf(uri);
        if (indexOfPictureUris != -1) {
            mView.displayPictureAt(mDisplayPaths, indexOfPictureUris);
        }
    }

    /**
     * 构建 Toolbar 左边的文本
     */
    private CharSequence buildToolbarLeftText() {
        return MessageFormat.format("{0}/{1}", mCurPosition + 1, mDisplayPaths.size());
    }

    /**
     * 构建 Toolbar checked Indicator 的文本
     */
    private CharSequence buildToolbarCheckedIndicatorText() {
        return String.valueOf(mPickedPaths.indexOf(mCurDisplayPath) + 1);
    }

    /**
     * 构建确认按钮文本
     */
    private CharSequence buildEnsureText() {
        return MessageFormat.format("{0}({1}/{2})",
                mView.getString(R.string.libpicturepicker_picturewatcher_ensure), mPickedPaths.size(), mConfig.threshold);
    }

}
