package com.sharry.picturepicker.watcher.impl;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sharry.picturepicker.R;
import com.sharry.picturepicker.support.loader.PictureLoader;
import com.sharry.picturepicker.support.utils.SharedElementUtils;
import com.sharry.picturepicker.watcher.manager.WatcherConfig;
import com.sharry.picturepicker.widget.CheckedIndicatorView;
import com.sharry.picturepicker.widget.DraggableViewPager;
import com.sharry.picturepicker.widget.photoview.OnPhotoTapListener;
import com.sharry.picturepicker.widget.photoview.PhotoView;
import com.sharry.picturepicker.widget.toolbar.SToolbar;
import com.sharry.picturepicker.widget.toolbar.ViewOptions;

import java.util.ArrayList;

/**
 * 图片查看器的 Activity, 主题设置为背景透明效果更佳
 *
 * @author Sharry <a href="SharryChooCHN@Gmail.com">Contact me.</a>
 * @version 1.3
 * @since 2018/9/22 23:24
 */
public class PictureWatcherActivity extends AppCompatActivity implements
        PictureWatcherContract.IView,
        DraggableViewPager.OnPagerChangedListener {

    public static final int REQUEST_CODE = 508;
    private static final String EXTRA_CONFIG = "start_intent_extra_config";
    private static final String EXTRA_SHARED_ELEMENT = "start_intent_extra_shared_element";
    public static final String RESULT_EXTRA_PICKED_PICTURES = "result_extra_picked_pictures";
    public static final String RESULT_EXTRA_IS_PICKED_ENSURE = "result_extra_is_picked_ensure";


    /**
     * U can launch this activity from here.
     *
     * @param request       请求的 Activity
     * @param resultTo      PictureWatcherActivity 返回值的去向
     * @param config        PictureWatcherActivity 的配置
     * @param sharedElement 共享元素
     */
    public static void startActivityForResult(@NonNull Activity request, @NonNull Fragment resultTo,
                                              @NonNull WatcherConfig config, @Nullable View sharedElement) {
        Intent intent = new Intent(request, PictureWatcherActivity.class);
        intent.putExtra(PictureWatcherActivity.EXTRA_CONFIG, config);
        if (sharedElement != null) {
            intent.putExtra(
                    PictureWatcherActivity.EXTRA_SHARED_ELEMENT,
                    SharedElementData.parseFrom(sharedElement, config.getPosition())
            );
        }
        // 非共享元素的启动
        resultTo.startActivityForResult(intent, REQUEST_CODE);
        // 使用淡入淡出的效果
        if (sharedElement != null) {
            request.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }
    }

    /**
     * The presenter for the view.
     */
    private PictureWatcherContract.IPresenter mPresenter;

    /**
     * Widgets for this Activity.
     */
    private TextView mTvTitle;
    private CheckedIndicatorView mCheckIndicator;
    private DraggableViewPager mViewPager;
    private LinearLayout mLlBottomPreviewContainer;
    private RecyclerView mBottomPreviewPictures;
    private TextView mTvEnsure;
    private ArrayList<PhotoView> mPhotoViews = new ArrayList<>();

    /**
     * The animator for bottom preview.
     */
    private ObjectAnimator mBottomPreviewShowAnimator;
    private ObjectAnimator mBottomPreviewDismissAnimator;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initPresenter();
        setContentView(R.layout.libpicturepicker_activity_picture_watcher);
        initTitle();
        initViews();
        mPresenter.setup();
    }

    @Override
    public void onBackPressed() {
        mPresenter.handleBackPressed();
    }

    @Override
    public void finish() {
        mPresenter.handleSetResultBeforeFinish();
        super.finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
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
    public void setPreviewAdapter(WatcherPreviewAdapter adapter) {
        mBottomPreviewPictures.setAdapter(adapter);
    }

    @Override
    public void createPhotoViews(int photoViewCount) {
        for (int i = 0; i < photoViewCount; i++) {
            PhotoView photoView = new PhotoView(this);
            photoView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            photoView.setOnPhotoTapListener(new OnPhotoTapListener() {
                @Override
                public void onPhotoTap(ImageView view, float x, float y) {
                    onBackPressed();
                }
            });
            mPhotoViews.add(photoView);
        }
        mViewPager.setAdapter(new WatcherPagerAdapter(mPhotoViews));
    }

    @Override
    public void showSharedElementEnter(final SharedElementData data) {
        mViewPager.setSharedElementPosition(data.sharedPosition);
        final PhotoView target = mPhotoViews.get(data.sharedPosition);
        target.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                target.getViewTreeObserver().removeOnPreDrawListener(this);
                // Execute enter animator.
                SharedElementUtils.createSharedElementEnterAnimator(target, data).start();
                return true;
            }
        });
    }

    @Override
    public void showSharedElementExitAndFinish(SharedElementData data) {
        final PhotoView target = mPhotoViews.get(data.sharedPosition);
        Animator exitAnim = SharedElementUtils.createSharedElementExitAnimator(target, data);
        exitAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                mViewPager.setBackgroundColor(Color.TRANSPARENT);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                finish();
            }
        });
        exitAnim.start();
    }

    @Override
    public void notifyBottomPicturesRemoved(String removedPath, int removedIndex) {
        mBottomPreviewPictures.getAdapter().notifyItemRemoved(removedIndex);
    }

    @Override
    public void notifyBottomPictureAdded(String insertPath, int addedIndex) {
        mBottomPreviewPictures.getAdapter().notifyItemInserted(addedIndex);
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
        mPresenter.handlePagerChanged(position);
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
    public void showBottomPreview() {
        if (mLlBottomPreviewContainer.getVisibility() == View.VISIBLE) {
            return;
        }
        if (mBottomPreviewShowAnimator == null) {
            mBottomPreviewShowAnimator = ObjectAnimator.ofFloat(mLlBottomPreviewContainer,
                    "translationY", mLlBottomPreviewContainer.getHeight(), 0);
            mBottomPreviewShowAnimator.setDuration(200);
            mBottomPreviewShowAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    mLlBottomPreviewContainer.setVisibility(View.VISIBLE);
                }
            });
        }
        mBottomPreviewShowAnimator.start();
    }

    @Override
    public void dismissBottomPreview() {
        if (mLlBottomPreviewContainer.getVisibility() == View.INVISIBLE) {
            return;
        }
        if (mBottomPreviewDismissAnimator == null) {
            mBottomPreviewDismissAnimator = ObjectAnimator.ofFloat(mLlBottomPreviewContainer,
                    "translationY", 0, mLlBottomPreviewContainer.getHeight());
            mBottomPreviewDismissAnimator.setDuration(200);
            mBottomPreviewDismissAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLlBottomPreviewContainer.setVisibility(View.INVISIBLE);
                }
            });
        }
        mBottomPreviewDismissAnimator.start();
    }

    @Override
    public void previewPicturesSmoothScrollToPosition(int position) {
        mBottomPreviewPictures.smoothScrollToPosition(position);
    }

    @Override
    public void showMsg(String msg) {
        Snackbar.make(mBottomPreviewPictures, msg, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void setResultBeforeFinish(@Nullable ArrayList<String> pickedPaths, boolean isEnsurePressed) {
        Intent intent = new Intent();
        intent.putExtra(RESULT_EXTRA_PICKED_PICTURES, pickedPaths);
        intent.putExtra(RESULT_EXTRA_IS_PICKED_ENSURE, isEnsurePressed);
        setResult(RESULT_OK, intent);
    }

    private void initPresenter() {
        mPresenter = new PictureWatcherPresenter(
                this,
                (WatcherConfig) getIntent().getParcelableExtra(EXTRA_CONFIG),
                ((SharedElementData) getIntent().getParcelableExtra(EXTRA_SHARED_ELEMENT))
        );
    }

    private void initTitle() {
        SToolbar toolbar = findViewById(R.id.toolbar);
        mTvTitle = toolbar.getTitleText();
        // 添加右部的索引
        mCheckIndicator = new CheckedIndicatorView(this);
        toolbar.addRightMenuView(mCheckIndicator, new ViewOptions.Builder()
                .setVisibility(View.INVISIBLE)
                .setWidthExcludePadding(dp2px(this, 25))
                .setHeightExcludePadding(dp2px(this, 25))
                .setPaddingRight(dp2px(this, 10))
                .setListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mPresenter.handleToolbarCheckedIndicatorClick(mCheckIndicator.isChecked());
                    }
                })
                .build());
    }

    private void initViews() {
        // 1. 初始化 ViewPager
        mViewPager = findViewById(R.id.view_pager);
        mViewPager.setOnPagerChangedListener(this);
        // 2. 初始化底部菜单
        mLlBottomPreviewContainer = findViewById(R.id.ll_bottom_container);
        mBottomPreviewPictures = findViewById(R.id.recycle_pictures);
        mBottomPreviewPictures.setLayoutManager(new LinearLayoutManager(this,
                LinearLayoutManager.HORIZONTAL, false));
        mTvEnsure = findViewById(R.id.tv_ensure);
        mTvEnsure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPresenter.handleEnsureClick();
            }
        });
    }

    private int dp2px(Context context, float dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                context.getResources().getDisplayMetrics());
    }

}