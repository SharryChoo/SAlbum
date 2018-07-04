package com.frank.picturepicker.picturewatcher.impl;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.transition.ChangeBounds;
import android.transition.ChangeImageTransform;
import android.transition.Explode;
import android.transition.Fade;
import android.transition.Slide;
import android.transition.Transition;
import android.transition.TransitionSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.frank.picturepicker.R;
import com.frank.picturepicker.picturewatcher.manager.PictureWatcherFragment;
import com.frank.picturepicker.picturewatcher.manager.WatcherConfig;
import com.frank.picturepicker.support.loader.PictureLoader;
import com.frank.picturepicker.support.util.Utils;
import com.frank.picturepicker.widget.CheckedIndicatorView;
import com.frank.picturepicker.widget.DraggableViewPager;
import com.frank.picturepicker.widget.photoview.OnPhotoTapListener;
import com.frank.picturepicker.widget.photoview.PhotoView;
import com.frank.picturepicker.widget.toolbar.AppBarHelper;
import com.frank.picturepicker.widget.toolbar.GenericToolbar;
import com.frank.picturepicker.widget.toolbar.Style;

import java.text.MessageFormat;
import java.util.ArrayList;

/**
 * Created by FrankChoo on 2017/12/28.
 * Email: frankchoochina@gmail.com
 * Version:  1.3
 * Description: 图片查看器的 Activity, 主题设置为背景透明效果更佳
 */
@SuppressLint("NewApi")
public class PictureWatcherActivity extends AppCompatActivity implements
        DraggableViewPager.OnPagerChangedListener,
        View.OnClickListener,
        PictureWatcherRecyclerAdapter.AdapterInteraction {

    private static final String TAG = PictureWatcherActivity.class.getSimpleName();

    // 启动时的 Extra
    public static final String START_INTENT_EXTRA_CONFIG = "start_intent_extra_config";
    public static final String START_INTENT_EXTRA_SHARED_ELEMENT = "start_intent_extra_shared_element";
    // 返回时的 Extra
    public static final String RESULT_EXTRA_PICKED_PICTURES = "result_extra_picked_pictures";// 返回的图片
    public static final String RESULT_EXTRA_IS_PICKED_ENSURE = "result_extra_is_picked_ensure";// 是否是确认选择

    // 数据的集合
    private WatcherConfig mConfig;
    private ArrayList<String> mPictureUris;// 展示的图片集合
    private ArrayList<String> mUserPickedSet;// 用户选中的图片集合, 配合相册使用
    private ArrayList<PhotoView> mPhotoViews = new ArrayList<>();// 图片对象
    private PictureWatcherAdapter mAdapter;// ViewPager 的 Adapter

    // SharedElement
    private boolean mIsSharedElement = false;// 开启共享元素
    private int mSharedPosition;// 共享元素的位置
    private String mSharedKey;// 共享元素的 key

    // 视图
    private DraggableViewPager mViewPager;
    private CheckedIndicatorView mCheckIndicator;
    private TextView mTvTitle;
    private RecyclerView mRecyclerView;
    private LinearLayout mllBottomContainer;
    private TextView mTvEnsure;

    // 当前展示的 位置 和 URI
    private int mCurPosition;
    private String mCurUri;
    private PhotoView mCurView;
    private boolean mIsEnsure = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        parseIntent();
        setupWindowTransitions();
        setContentView(R.layout.activity_picture_watcher);
        // Postpone the shared element enter transition.
        if (mIsSharedElement) postponeEnterTransition();
        initTitle();
        initViews();
        initData();
    }

    protected void parseIntent() {
        mConfig = getIntent().getParcelableExtra(START_INTENT_EXTRA_CONFIG);
        // 获取需要展示图片的 URI 集合
        mPictureUris = mConfig.pictureUris == null ?
                new ArrayList<String>() : mConfig.pictureUris;
        // 获取已经选中的图片
        mUserPickedSet = mConfig.userPickedSet;
        // 获取当前需要展示的 Position 和 URI
        mCurPosition = mConfig.position;
        mCurUri = mPictureUris.get(mCurPosition);
        // 判断是否开启共享动画
        mIsSharedElement = getIntent().getBooleanExtra(START_INTENT_EXTRA_SHARED_ELEMENT, false);
        if (mIsSharedElement) {
            mSharedPosition = mCurPosition;
            mSharedKey = mPictureUris.get(mSharedPosition);
        }
    }

    protected void setupWindowTransitions() {
        // 5.0 以上的系统使用 Transition 过渡动画
        if (!Utils.isLollipop()) return;
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        // 进出动画
        getWindow().setEnterTransition(mIsSharedElement ?
                new Fade().setDuration(500) : new Slide().setDuration(300));
        getWindow().setReturnTransition(new Fade().setDuration(300));
        // 共享元素动画设置
        ChangeBounds elementEnterTransition = new ChangeBounds();
        elementEnterTransition.setDuration(500);
        elementEnterTransition.setInterpolator(new OvershootInterpolator(1f));
        getWindow().setSharedElementEnterTransition(elementEnterTransition);
        // 退出动画
        TransitionSet elementReturnTransition = new TransitionSet();
        Transition transition1 = new ChangeBounds();
        Transition transition2 = new ChangeImageTransform();
        transition1.setInterpolator(new OvershootInterpolator(0.5f));
        transition2.setInterpolator(new OvershootInterpolator(0.5f));
        elementReturnTransition.addTransition(transition1);
        elementReturnTransition.addTransition(transition2);
        elementReturnTransition.setDuration(400);
        getWindow().setSharedElementReturnTransition(elementReturnTransition);
    }

    protected void initTitle() {
        AppBarHelper.with(this).setStatusBarStyle(Style.TRANSPARENT).apply();
        GenericToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setAdjustToTransparentStatusBar(true);
        toolbar.addLeftIcon(0, R.drawable.icon_common_arrow_back_white, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        // 1. 设置标题文本
        toolbar.setTitleGravity(Gravity.LEFT);
        mTvTitle = toolbar.getTitleText();
        mTvTitle.setTextSize(20);
        // 2. 配置图片选中指示器
        if (mUserPickedSet == null) return;
        mCheckIndicator = new CheckedIndicatorView(this);
        mCheckIndicator.setChecked(false);
        mCheckIndicator.setBorderColor(mConfig.indicatorBorderCheckedColor, mConfig.indicatorBorderUncheckedColor);
        mCheckIndicator.setSolidColor(mConfig.indicatorSolidColor);
        mCheckIndicator.setTextColor(mConfig.indicatorTextColor);
        toolbar.addRightView(1, mCheckIndicator, 25, 25, this);
    }

    protected void initViews() {
        // 1. 初始化 ViewPager
        mViewPager = findViewById(R.id.view_pager);
        if (mIsSharedElement) mViewPager.setSharedElementPosition(mSharedPosition);
        mAdapter = new PictureWatcherAdapter(mPhotoViews);
        mViewPager.setAdapter(mAdapter);
        mViewPager.setOnPagerChangedListener(this);
        // 2. 初始化底部菜单
        if (mUserPickedSet == null) return;
        mllBottomContainer = findViewById(R.id.ll_bottom_container);
        mRecyclerView = findViewById(R.id.recycler_view);
        mRecyclerView.setAdapter(new PictureWatcherRecyclerAdapter(mUserPickedSet, this));
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this,
                LinearLayoutManager.HORIZONTAL, false));
        mTvEnsure = findViewById(R.id.tv_ensure);
        mTvEnsure.setOnClickListener(this);
    }

    protected void initData() {
        // 1. 填充 ViewPager 数据
        for (String uri : mPictureUris) {
            PhotoView photoView = createPhotoView();
            mPhotoViews.add(photoView);
            // 配置共享元素
            if (mIsSharedElement && uri.equals(mSharedKey)) {
                ViewCompat.setTransitionName(photoView, mSharedKey);
                photoView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        mPhotoViews.get(mSharedPosition).getViewTreeObserver().removeOnPreDrawListener(this);
                        startPostponedEnterTransition();
                        return true;
                    }
                });
            }
        }
        mAdapter.notifyDataSetChanged();
        onPagerChanged(mCurPosition); // 手动加载第一张需要展示的图片
        mViewPager.bindCaptureView(mCurView);// 绑定第一张捕获的照片
        mViewPager.setCurrentItem(mCurPosition, false);// 设置当前选中的图片
        // 2. 更新底部菜单状态
        if (mUserPickedSet == null) return;
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                updateBottomMenuStatus();
            }
        }, mIsSharedElement ? 500 : 0);
    }

    @Override
    public View onPagerChanged(int position) {
        // 更新当前页面的 URI
        mCurPosition = position;
        mCurUri = mPictureUris.get(position);
        mCurView = mPhotoViews.get(position);
        // 更新当前页面共享元素的 key
        if (mIsSharedElement) {
            ViewCompat.setTransitionName(mPhotoViews.get(mSharedPosition),
                    position == mSharedPosition ? mSharedKey : "");
        }
        updateToolbarIndicatorCheckedStatus();// 更新 Title 选中标识
        updateToolbarIndicatorTextContent(); // 更新标题角标索引
        displayCurrentPhotoView();// 加载图片
        return mCurView;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.tv_ensure) {// 确认按钮
            if (Utils.isLollipop()) getWindow().setReturnTransition(null);
            if (mIsSharedElement) getWindow().setSharedElementReturnTransition(null);
            // 更新标记位为确认按钮的返回
            mIsEnsure = true;
            onBackPressed();
        } else {
            if (mCheckIndicator.isChecked()) {// Checked-> Unchecked
                // 移除选中数据与状态
                int removedIndex = mUserPickedSet.indexOf(mCurUri);
                mUserPickedSet.remove(removedIndex);
                mCheckIndicator.setChecked(false);
                // 通知 RecyclerView 数据变更
                mRecyclerView.getAdapter().notifyItemRemoved(removedIndex);
            } else {// Unchecked -> Checked
                // 判断是否达到选择上限
                if (mUserPickedSet.size() < mConfig.threshold) {
                    mUserPickedSet.add(mCurUri);
                    int addedIndex = mUserPickedSet.indexOf(mCurUri);
                    mCheckIndicator.setText(String.valueOf(addedIndex + 1));
                    mCheckIndicator.setChecked(true);
                    // 通知 RecyclerView 数据变更
                    mRecyclerView.getAdapter().notifyItemInserted(addedIndex);
                    mRecyclerView.smoothScrollToPosition(addedIndex);
                } else {
                    Toast.makeText(this, getString(R.string.activity_picture_watcher_msg_over_threshold_prefix)
                                    + mConfig.threshold + getString(R.string.activity_picture_watcher_msg_over_threshold_suffix),
                            Toast.LENGTH_SHORT).show();
                    mCheckIndicator.setChecked(false);
                }
            }
            updateBottomMenuStatus();
        }
    }

    @Override
    public void onItemClicked(ImageView imageView, String uri, int position) {
        int indexOfPictureUris = mPictureUris.indexOf(uri);
        if (indexOfPictureUris != -1) {
            mViewPager.setCurrentItem(indexOfPictureUris);
        }
    }

    @Override
    public void onBackPressed() {
        // 若为共享元素则在开始动画之前将底部选中菜单移除
        if (mIsSharedElement) {
            mllBottomContainer.setVisibility(View.INVISIBLE);
        }
        super.onBackPressed();
    }

    @Override
    public void finish() {
        // 回调数据
        if (mUserPickedSet != null) {
            Intent intent = new Intent();
            intent.putExtra(RESULT_EXTRA_PICKED_PICTURES, mUserPickedSet);
            intent.putExtra(RESULT_EXTRA_IS_PICKED_ENSURE, mIsEnsure);
            setResult(PictureWatcherFragment.REQUEST_CODE_PICKED, intent);
        }
        super.finish();
        // 当前 Activity 关闭时, 使用淡入淡出的动画
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    /**
     * 更新 Toolbar 上标题文本的索引
     */
    private void updateToolbarIndicatorTextContent() {
        int nowPager = mCurPosition + 1;
        int AllPager = mPictureUris.size();
        mTvTitle.setText(MessageFormat.format("{0}/{1}", nowPager, AllPager));
    }

    /**
     * 更新 Toolbar 上的选中指示器的状态
     */
    private void updateToolbarIndicatorCheckedStatus() {
        if (mUserPickedSet == null) return;
        // 更新 Title 选中标识
        if (mUserPickedSet.indexOf(mCurUri) != -1) {
            mCheckIndicator.setChecked(true);
            mCheckIndicator.setText(String.valueOf(mUserPickedSet.indexOf(mCurUri) + 1));
        } else {
            mCheckIndicator.setChecked(false);
        }
    }

    /**
     * 更新底部菜单的状态
     */
    private void updateBottomMenuStatus() {
        // 更新底部文本
        mTvEnsure.setText(MessageFormat.format("{0}({1}/{2})",
                getString(R.string.activity_picture_watcher_btn_ensure), mUserPickedSet.size(), mConfig.threshold));
        // 更新底部视图
        boolean beforeVisible = mllBottomContainer.getVisibility() == View.VISIBLE;
        final boolean nowVisible = !mUserPickedSet.isEmpty();
        if (beforeVisible != nowVisible) {
            int startY = beforeVisible ? 0 : mllBottomContainer.getHeight();
            int endY = beforeVisible ? mllBottomContainer.getHeight() : 0;
            ObjectAnimator animator = ObjectAnimator.ofFloat(mllBottomContainer,
                    "translationY", startY, endY);
            animator.setDuration(200);
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    mllBottomContainer.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    mllBottomContainer.setVisibility(nowVisible ? View.VISIBLE : View.INVISIBLE);
                }
            });
            animator.start();
        }
    }

    /**
     * 显示图片当前位置的图片
     */
    private void displayCurrentPhotoView() {
        // 加载当前的位置的图片
        PhotoView curView = mPhotoViews.get(mCurPosition);
        if (curView != null && curView.getDrawable() == null) {
            PictureLoader.load(this, mPictureUris.get(mCurPosition), curView);
        }
        // 加载前一个
        int beforeIndex = mCurPosition - 1;
        PhotoView beforeView = beforeIndex >= 0 ? mPhotoViews.get(beforeIndex) : null;
        if (beforeView != null && beforeView.getDrawable() == null) {
            PictureLoader.load(this, mPictureUris.get(beforeIndex), beforeView);
        }
        // 加载后一个
        int afterIndex = mCurPosition + 1;
        PhotoView afterView = afterIndex < mPictureUris.size() ? mPhotoViews.get(afterIndex) : null;
        if (afterView != null && afterView.getDrawable() == null) {
            PictureLoader.load(this, mPictureUris.get(afterIndex), afterView);
        }
    }

    /**
     * 创建 PhotoView
     */
    private PhotoView createPhotoView() {
        PhotoView photoView = new PhotoView(this);
        photoView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        photoView.setOnPhotoTapListener(new OnPhotoTapListener() {
            @Override
            public void onPhotoTap(ImageView view, float x, float y) {
                onBackPressed();
            }
        });
        return photoView;
    }

}