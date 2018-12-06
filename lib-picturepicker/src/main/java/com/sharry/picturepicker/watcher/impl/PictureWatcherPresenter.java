package com.sharry.picturepicker.watcher.impl;

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

import com.sharry.picturepicker.R;
import com.sharry.picturepicker.support.utils.VersionUtil;
import com.sharry.picturepicker.watcher.manager.WatcherConfig;

import java.text.MessageFormat;
import java.util.ArrayList;

/**
 * Created by Sharry on 2018/7/20.
 * Email: SharryChooCHN@Gmail.com
 * Version: 1.0
 * Description:
 */
class PictureWatcherPresenter implements PictureWatcherContract.IPresenter, WatcherPreviewAdapter.AdapterInteraction {

    private final PictureWatcherContract.IView mView;
    private final WatcherConfig mConfig;
    private final ArrayList<String> mDisplayPaths;
    private final ArrayList<String> mPickedPaths;
    private final boolean mIsSharedElement;                    // 是否支持共享元素

    private int mCurPosition;                                  // 当前展示的图片
    private String mCurDisplayPath;                            // 当前展示的图片地址
    private String mSharedKey;                                 // 共享元素的 Key
    private boolean mIsEnsurePressed = false;                  // 是否按压了确认

    PictureWatcherPresenter(PictureWatcherContract.IView view, WatcherConfig config, boolean isSharedElement) {
        this.mView = view;
        this.mConfig = config;
        this.mIsSharedElement = isSharedElement;
        // 获取需要展示图片的 URI 集合
        this.mDisplayPaths = config.getPictureUris();
        // 获取已经选中的图片
        this.mPickedPaths = config.getUserPickedSet();
        // 获取当前需要展示的 Position 和 URI
        this.mCurPosition = config.getPosition();
        this.mCurDisplayPath = mDisplayPaths.get(mCurPosition);
        // 判断是否开启共享动画
        if (VersionUtil.isLollipop()) {
            mView.setWindowEnterTransitions(new Slide().setDuration(300));
            mView.setWindowReturnTransitions(new Fade().setDuration(300));
            if (mIsSharedElement) {
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
        }
    }

    @Override
    public void start() {
        // 1. 设置 Toolbar 数据
        mView.displayToolbarLeftText(buildToolbarLeftText());
        mView.setToolbarCheckedIndicatorVisibility(mConfig.isPickerSupport());

        // 2. 设置 Pictures 数据
        mView.createPhotoViews(mDisplayPaths.size());
        if (mIsSharedElement) {
            // 绑定共享元素
            mView.bindSharedElementView(mDisplayPaths.indexOf(mSharedKey), mSharedKey);
            // 通知共享元素位置变更了
            mView.notifySharedElementChanged(mDisplayPaths.indexOf(mSharedKey), mSharedKey);
        }
        mView.displayPictureAt(mDisplayPaths, mCurPosition);

        // 3. 设置底部菜单和按钮选中的状态
        if (mConfig.isPickerSupport()) {
            mView.setToolbarCheckedIndicatorColors(
                    mConfig.getIndicatorBorderCheckedColor(),
                    mConfig.getIndicatorBorderUncheckedColor(),
                    mConfig.getIndicatorSolidColor(),
                    mConfig.getIndicatorTextColor()
            );
            mView.setToolbarIndicatorChecked(mPickedPaths.indexOf(mCurDisplayPath) != -1);
            mView.displayToolbarIndicatorText(buildToolbarCheckedIndicatorText());
            // 底部菜单
            mView.setPreviewAdapter(new WatcherPreviewAdapter(mPickedPaths, this));
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
        if (mConfig.isPickerSupport()) {
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
            if (mPickedPaths.size() < mConfig.getThreshold()) {
                mPickedPaths.add(mCurDisplayPath);
                int addedIndex = mPickedPaths.indexOf(mCurDisplayPath);
                // 通知 RecyclerView 数据变更
                mView.notifyBottomPictureAdded(mCurDisplayPath, addedIndex);
                mView.previewPicturesSmoothScrollToPosition(addedIndex);
            } else {
                mView.showMsg(
                        mView.getString(R.string.libpicturepicker_watcher_tips_over_threshold_prefix) +
                                mConfig.getThreshold() +
                                mView.getString(R.string.libpicturepicker_watcher_tips_over_threshold_suffix)
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
            mView.showMsg(mView.getString(R.string.libpicturepicker_watcher_tips_ensure_failed));
            return;
        }
        if (VersionUtil.isLollipop()) {
            mView.setWindowReturnTransitions(null);
            mView.setSharedElementReturnTransition(null);
        }
        mIsEnsurePressed = true;
        mView.finish();
    }

    @Override
    public void handleFinish() {
        mView.setResultBeforeFinish(mPickedPaths, mIsEnsurePressed);
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
                mView.getString(R.string.libpicturepicker_watcher_ensure), mPickedPaths.size(), mConfig.getThreshold());
    }

}
