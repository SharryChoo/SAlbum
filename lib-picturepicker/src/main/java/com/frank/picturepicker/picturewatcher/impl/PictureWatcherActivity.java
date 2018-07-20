package com.frank.picturepicker.picturewatcher.impl;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.transition.Transition;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.frank.picturepicker.R;
import com.frank.picturepicker.picturewatcher.impl.mvp.PictureWatcherContract;
import com.frank.picturepicker.picturewatcher.impl.mvp.PictureWatcherPresenter;
import com.frank.picturepicker.picturewatcher.manager.WatcherConfig;
import com.frank.picturepicker.support.loader.PictureLoader;
import com.frank.picturepicker.widget.CheckedIndicatorView;
import com.frank.picturepicker.widget.DraggableViewPager;
import com.frank.picturepicker.widget.photoview.OnPhotoTapListener;
import com.frank.picturepicker.widget.photoview.PhotoView;
import com.frank.picturepicker.widget.toolbar.AppBarHelper;
import com.frank.picturepicker.widget.toolbar.GenericToolbar;
import com.frank.picturepicker.widget.toolbar.Style;

import java.util.ArrayList;

/**
 * Created by FrankChoo on 2017/12/28.
 * Email: frankchoochina@gmail.com
 * Version:  1.3
 * Description: 图片查看器的 Activity, 主题设置为背景透明效果更佳
 */
@SuppressLint("NewApi")
public class PictureWatcherActivity extends AppCompatActivity implements
        PictureWatcherContract.IView,
        DraggableViewPager.OnPagerChangedListener {

    // 启动时的 Extra
    public static final String START_INTENT_EXTRA_CONFIG = "start_intent_extra_config";
    public static final String START_INTENT_EXTRA_SHARED_ELEMENT = "start_intent_extra_shared_element";
    // 返回时的 Extra
    public static final String RESULT_EXTRA_PICKED_PICTURES = "result_extra_picked_pictures";// 返回的图片
    public static final String RESULT_EXTRA_IS_PICKED_ENSURE = "result_extra_is_picked_ensure";// 是否是确认选择

    // Presenter
    private PictureWatcherContract.IPresenter mPresenter = new PictureWatcherPresenter();

    private TextView mTvTitle;
    private CheckedIndicatorView mCheckIndicator;
    private DraggableViewPager mViewPager;
    // 底部选中容器
    private LinearLayout mLlBottomPreviewContainer;
    private RecyclerView mBottomPreviewPictures;
    private TextView mTvEnsure;

    private ArrayList<PhotoView> mPhotoViews = new ArrayList<>();// 图片对象
    private PictureWatcherAdapter mAdapter;// ViewPager 的 Adapter

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPresenter.attach(this);
        parseIntent();
        setContentView(R.layout.activity_picture_watcher);
        initTitle();
        initViews();
        initData();
    }

    protected void parseIntent() {
        mPresenter.init(
                (WatcherConfig) getIntent().getParcelableExtra(START_INTENT_EXTRA_CONFIG),
                getIntent().getBooleanExtra(START_INTENT_EXTRA_SHARED_ELEMENT, false)
        );
    }

    protected void initTitle() {
        AppBarHelper.with(this).setStatusBarStyle(Style.TRANSPARENT).apply();
        GenericToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setAdjustToTransparentStatusBar(true);
        toolbar.addLeftIcon(0, R.drawable.icon_common_arrow_back_white, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPresenter.performBackPressed();
            }
        });
        toolbar.setTitleGravity(Gravity.LEFT);
        mTvTitle = toolbar.getTitleText();
        mTvTitle.setTextSize(20);
        // 添加右部的索引
        mCheckIndicator = new CheckedIndicatorView(this);
        mCheckIndicator.setVisibility(View.INVISIBLE);
        toolbar.addRightView(1, mCheckIndicator, 25, 25, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPresenter.performToolbarCheckedIndicatorClick(mCheckIndicator.isChecked());
            }
        });
    }

    protected void initViews() {
        // 1. 初始化 ViewPager
        mViewPager = findViewById(R.id.view_pager);
        mAdapter = new PictureWatcherAdapter(mPhotoViews);
        mViewPager.setAdapter(mAdapter);
        mViewPager.setOnPagerChangedListener(this);
        // 2. 初始化底部菜单
        mLlBottomPreviewContainer = findViewById(R.id.ll_bottom_container);
        mBottomPreviewPictures = findViewById(R.id.recycler_view);
        mBottomPreviewPictures.setLayoutManager(new LinearLayoutManager(this,
                LinearLayoutManager.HORIZONTAL, false));
        mTvEnsure = findViewById(R.id.tv_ensure);
        mTvEnsure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPresenter.performEnsureClick();
            }
        });
    }

    protected void initData() {
        mPresenter.fetchData();
    }

    @Override
    public void setWindowEnterTransitions(Transition enterTransition) {
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        getWindow().setEnterTransition(enterTransition);
    }

    @Override
    public void setWindowReturnTransitions(Transition returnTransition) {
        getWindow().setReturnTransition(returnTransition);
    }

    @Override
    public void setSharedElementEnterTransition(Transition enterTransition) {
        getWindow().setSharedElementEnterTransition(enterTransition);
    }

    @Override
    public void setSharedElementReturnTransition(Transition returnTransition) {
        getWindow().setSharedElementReturnTransition(returnTransition);
    }

    @Override
    public void setToolbarCheckedIndicatorVisibility(boolean isShowCheckedIndicator) {
        mCheckIndicator.setVisibility(isShowCheckedIndicator ? View.VISIBLE : View.GONE);
    }

    @Override
    public void setToolbarCheckedIndicatorColors(int indicatorBorderCheckedColor, int indicatorBorderUncheckedColor,
                                                 int indicatorSolidColor, int indicatorTextColor) {
        mCheckIndicator.setBorderColor(indicatorBorderCheckedColor, indicatorBorderUncheckedColor);
        mCheckIndicator.setSolidColor(indicatorSolidColor);
        mCheckIndicator.setTextColor(indicatorTextColor);
    }

    @Override
    public void setPreviewAdapter(PictureWatcherPreviewAdapter adapter) {
        mBottomPreviewPictures.setAdapter(adapter);
    }

    @Override
    public void createPhotoViews(ArrayList<String> pictureUris) {
        for (String uri : pictureUris) {
            PhotoView photoView = new PhotoView(this);
            photoView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            photoView.setOnPhotoTapListener(new OnPhotoTapListener() {
                @Override
                public void onPhotoTap(ImageView view, float x, float y) {
                    onBackPressed();
                }
            });
            mPhotoViews.add(photoView);
        }
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void bindSharedElementView(int position, String sharedKey) {
        // Postpone the shared element enter transition.
        postponeEnterTransition();
        // 绑定共享元素
        final PhotoView sharedElement = mPhotoViews.get(position);
        sharedElement.setTransitionName(sharedKey);
        sharedElement.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                sharedElement.getViewTreeObserver().removeOnPreDrawListener(this);
                startPostponedEnterTransition();
                return true;
            }
        });
    }

    @Override
    public void notifySharedElementChanged(int sharedPosition, String sharedKey) {
        mViewPager.setSharedElementPosition(sharedPosition);
        mPhotoViews.get(sharedPosition).setTransitionName(sharedKey);
    }

    @Override
    public void displayPictureAt(ArrayList<String> pictureUris, int curPosition) {
        mViewPager.setCurrentItem(curPosition);
        // 加载当前的位置的图片
        PhotoView curView = mPhotoViews.get(curPosition);
        if (curView != null && curView.getDrawable() == null) {
            PictureLoader.load(this, pictureUris.get(curPosition), curView);
        }
        // 加载前一个
        int beforeIndex = curPosition - 1;
        PhotoView beforeView = beforeIndex >= 0 ? mPhotoViews.get(beforeIndex) : null;
        if (beforeView != null && beforeView.getDrawable() == null) {
            PictureLoader.load(this, pictureUris.get(beforeIndex), beforeView);
        }
        // 加载后一个
        int afterIndex = curPosition + 1;
        PhotoView afterView = afterIndex < pictureUris.size() ? mPhotoViews.get(afterIndex) : null;
        if (afterView != null && afterView.getDrawable() == null) {
            PictureLoader.load(this, pictureUris.get(afterIndex), afterView);
        }
    }

    @Override
    public void setToolbarIndicatorChecked(boolean isChecked) {
        mCheckIndicator.setChecked(isChecked);
    }

    @Override
    public void displayToolbarIndicatorText(CharSequence indicatorText) {
        mCheckIndicator.setText(indicatorText);
    }

    @Override
    public void onPagerChanged(int position) {
        mPresenter.performPagerChanged(position);
    }

    @Override
    public void displayPreviewEnsureText(CharSequence content) {
        mTvEnsure.setText(content);
    }

    @Override
    public void displayToolbarLeftText(CharSequence content) {
        mTvTitle.setText(content);
    }

    @Override
    public void setBottomPreviewVisibility(boolean nowVisible, final boolean destVisible) {
        if (nowVisible == destVisible) return;
        int startY = nowVisible ? 0 : mLlBottomPreviewContainer.getHeight();
        int endY = nowVisible ? mLlBottomPreviewContainer.getHeight() : 0;
        ObjectAnimator animator = ObjectAnimator.ofFloat(mLlBottomPreviewContainer,
                "translationY", startY, endY);
        animator.setDuration(200);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                mLlBottomPreviewContainer.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mLlBottomPreviewContainer.setVisibility(destVisible ? View.VISIBLE : View.INVISIBLE);
            }
        });
        animator.start();
    }

    @Override
    public void previewPicturesSmoothScrollToPosition(int position) {
        mBottomPreviewPictures.smoothScrollToPosition(position);
    }

    @Override
    public void showMsg(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void finish() {
        super.finish();
        // 当前 Activity 关闭时, 使用淡入淡出的动画
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

}