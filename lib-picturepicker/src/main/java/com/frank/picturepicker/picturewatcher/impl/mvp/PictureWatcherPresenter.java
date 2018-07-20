package com.frank.picturepicker.picturewatcher.impl.mvp;

import android.app.Activity;
import android.content.Intent;
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
import com.frank.picturepicker.picturewatcher.impl.PictureWatcherPreviewAdapter;
import com.frank.picturepicker.picturewatcher.manager.PictureWatcherFragment;
import com.frank.picturepicker.picturewatcher.manager.WatcherConfig;
import com.frank.picturepicker.support.util.Utils;

import java.text.MessageFormat;
import java.util.ArrayList;

import static com.frank.picturepicker.picturewatcher.impl.PictureWatcherActivity.RESULT_EXTRA_IS_PICKED_ENSURE;
import static com.frank.picturepicker.picturewatcher.impl.PictureWatcherActivity.RESULT_EXTRA_PICKED_PICTURES;

/**
 * Created by Frank on 2018/7/20.
 * Email: frankchoochina@gmail.com
 * Version: 1.0
 * Description:
 */
public class PictureWatcherPresenter implements PictureWatcherContract.IPresenter, PictureWatcherPreviewAdapter.AdapterInteraction {

    private PictureWatcherContract.IView mView;
    private WatcherConfig mConfig;
    private ArrayList<String> mPictureUris;
    private ArrayList<String> mUserPickedSet;
    private PictureWatcherPreviewAdapter mBottomPreviewAdapter;

    private int mCurPosition = -1;
    private String mCurDisplayUri;
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
        mPictureUris = config.pictureUris == null ? new ArrayList<String>() : config.pictureUris;
        // 获取已经选中的图片
        mUserPickedSet = config.userPickedSet;
        mIsSupportPicked = mUserPickedSet != null;
        // 获取当前需要展示的 Position 和 URI
        mCurPosition = config.position;
        mCurDisplayUri = mPictureUris.get(mCurPosition);
        // 判断是否开启共享动画
        if (!Utils.isLollipop()) return;
        mView.setWindowEnterTransitions(new Slide().setDuration(300));
        mView.setWindowReturnTransitions(new Fade().setDuration(300));
        mIsSharedElement = isSharedElement;
        if (!mIsSharedElement) return;
        mSharedKey = mCurDisplayUri;
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
        mView.createPhotoViews(mPictureUris);
        if (mIsSharedElement) {
            // 绑定共享元素
            mView.bindSharedElementView(mPictureUris.indexOf(mSharedKey), mSharedKey);
            // 通知共享元素位置变更了
            mView.notifySharedElementChanged(mPictureUris.indexOf(mSharedKey), mSharedKey);
        }
        mView.displayPictureAt(mPictureUris, mCurPosition);

        // 3. 设置底部菜单和按钮选中的状态
        if (mIsSupportPicked) {
            mView.setToolbarCheckedIndicatorColors(mConfig.indicatorBorderCheckedColor,
                    mConfig.indicatorBorderUncheckedColor, mConfig.indicatorSolidColor, mConfig.indicatorTextColor);
            mView.setToolbarIndicatorChecked(mUserPickedSet.indexOf(mCurDisplayUri) != -1);
            mView.displayToolbarIndicatorText(buildToolbarCheckedIndicatorText());
            // 底部菜单
            mBottomPreviewAdapter = new PictureWatcherPreviewAdapter(mUserPickedSet, this);
            mView.setPreviewAdapter(mBottomPreviewAdapter);
            mView.displayPreviewEnsureText(buildEnsureText());
            // 底部菜单延时弹出
            if (!mUserPickedSet.isEmpty()) {
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
    public void performPagerChanged(int position) {
        // 更新数据
        mCurPosition = position;
        mCurDisplayUri = mPictureUris.get(position);
        // 展示 Toolbar 左边的指示文本
        mView.displayToolbarLeftText(buildToolbarLeftText());
        // 展示图片
        mView.displayPictureAt(mPictureUris, mCurPosition);
        if (mIsSupportPicked) {
            mView.setToolbarIndicatorChecked(mUserPickedSet.indexOf(mCurDisplayUri) != -1);
            mView.displayToolbarIndicatorText(buildToolbarCheckedIndicatorText());
            mView.displayPreviewEnsureText(buildEnsureText());
        }
        if (mIsSharedElement) {
            int sharedPosition = mPictureUris.indexOf(mSharedKey);
            mView.notifySharedElementChanged(sharedPosition,
                    position == sharedPosition ? mSharedKey : "");
        }
    }

    @Override
    public void performToolbarCheckedIndicatorClick(boolean isChecked) {
        boolean nowVisible = !mUserPickedSet.isEmpty();
        if (isChecked) {
            // 移除选中数据与状态
            int removedIndex = mUserPickedSet.indexOf(mCurDisplayUri);
            mUserPickedSet.remove(removedIndex);
            // 通知 RecyclerView 数据变更
            mBottomPreviewAdapter.notifyItemRemoved(removedIndex);
        } else {
            // 判断是否达到选择上限
            if (mUserPickedSet.size() < mConfig.threshold) {
                mUserPickedSet.add(mCurDisplayUri);
                int addedIndex = mUserPickedSet.indexOf(mCurDisplayUri);
                // 通知 RecyclerView 数据变更
                mBottomPreviewAdapter.notifyItemInserted(addedIndex);
                mView.previewPicturesSmoothScrollToPosition(addedIndex);
            } else {
                mView.showMsg(
                        mView.getString(R.string.activity_picture_watcher_msg_over_threshold_prefix) +
                                mConfig.threshold +
                                mView.getString(R.string.activity_picture_watcher_msg_over_threshold_suffix)
                );
            }
        }
        mView.setToolbarIndicatorChecked(mUserPickedSet.indexOf(mCurDisplayUri) != -1);
        mView.displayToolbarIndicatorText(buildToolbarCheckedIndicatorText());
        mView.displayPreviewEnsureText(buildEnsureText());
        mView.setBottomPreviewVisibility(nowVisible, !mUserPickedSet.isEmpty());
    }

    @Override
    public void performEnsureClick() {
        if (mUserPickedSet.isEmpty()) {
            mView.showMsg(mView.getString(R.string.activity_picture_watcher_msg_ensure_failed));
            return;
        }
        if (Utils.isLollipop()) {
            mView.setWindowReturnTransitions(null);
            mView.setSharedElementReturnTransition(null);
        }
        mIsEnsurePressed = true;
        mView.onBackPressed();
    }

    @Override
    public void performBackPressed() {
        mView.onBackPressed();
    }

    @Override
    public ArrayList<String> fetchUserPicked() {
        return mUserPickedSet;
    }

    @Override
    public boolean isEnsurePressed() {
        return mIsEnsurePressed;
    }

    @Override
    public void onPreviewItemClicked(ImageView imageView, String uri, int position) {
        int indexOfPictureUris = mPictureUris.indexOf(uri);
        if (indexOfPictureUris != -1) {
            mView.displayPictureAt(mPictureUris, indexOfPictureUris);
        }
    }

    /**
     * 构建 Toolbar 左边的文本
     */
    private CharSequence buildToolbarLeftText() {
        return MessageFormat.format("{0}/{1}", mCurPosition + 1, mPictureUris.size());
    }

    /**
     * 构建 Toolbar checked Indicator 的文本
     */
    private CharSequence buildToolbarCheckedIndicatorText() {
        return String.valueOf(mUserPickedSet.indexOf(mCurDisplayUri) + 1);
    }

    /**
     * 构建确认按钮文本
     */
    private CharSequence buildEnsureText() {
        return MessageFormat.format("{0}({1}/{2})",
                mView.getString(R.string.activity_picture_watcher_btn_ensure), mUserPickedSet.size(), mConfig.threshold);
    }

    /**
     * 执行 View 的销毁
     */
    private void executeViewFinish(boolean isEnsureClick) {
        if (mIsSupportPicked) {
            Intent intent = new Intent();
            intent.putExtra(RESULT_EXTRA_PICKED_PICTURES, mUserPickedSet);
            intent.putExtra(RESULT_EXTRA_IS_PICKED_ENSURE, isEnsureClick);
            ((Activity) mView).setResult(PictureWatcherFragment.REQUEST_CODE_PICKED, intent);
        }
        ((Activity) mView).onBackPressed();
    }

}
