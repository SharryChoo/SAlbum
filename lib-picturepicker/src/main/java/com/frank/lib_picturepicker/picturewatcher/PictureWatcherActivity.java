package com.frank.lib_picturepicker.picturewatcher;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.transition.Explode;
import android.transition.Fade;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.frank.lib_picturepicker.R;
import com.frank.lib_picturepicker.toolbar.AppBarHelper;
import com.frank.lib_picturepicker.toolbar.GenericToolbar;
import com.frank.lib_picturepicker.toolbar.Style;

import java.util.ArrayList;
import java.util.List;

import cn.bluemobi.dylan.photoview.library.PhotoView;
import cn.bluemobi.dylan.photoview.library.PhotoViewAttacher;

/**
 * Created by FrankChoo on 2017/12/28.
 * Email: frankchoochina@gmail.com
 * Version:  1.0
 * Description: 图片查看器的 Activity, 主题设置为背景透明效果更佳
 */
@SuppressLint("NewApi")
public class PictureWatcherActivity extends AppCompatActivity implements DraggableViewPager.OnPagerChangedListener {

    private static final String TAG = PictureWatcherActivity.class.getSimpleName();
    public static final String EXTRA_PICTURE_URIS = "extra_picture_uris";
    public static final String EXTRA_CUR_POSITION = "extra_cur_position";
    public static final String EXTRA_SHARED_ELEMENT = "extra_shared_element";

    /**
     * 调用图片查看器的方法
     *
     * @param context 上下文
     * @param uri     点击哪张图片进入图片查看器
     */
    public static void start(@NonNull Context context, @NonNull String uri) {
        start(context, uri, null);
        if (TextUtils.isEmpty(uri)) {
            throw new IllegalArgumentException("PictureWatcherActivity.start -> Parameter uri must not be null!");
        }
        ArrayList<String> uris = new ArrayList<>();
        uris.add(uri);
        start(context, uris, 0);
    }

    /**
     * 单张图片调用图片查看器的方法(共享元素)
     *
     * @param context 上下文
     * @param uri     点击哪张图片进入图片查看器
     */
    public static void start(@NonNull Context context, @NonNull String uri, @NonNull View transitionView) {
        if (TextUtils.isEmpty(uri)) {
            throw new IllegalArgumentException("PictureWatcherActivity.start -> Parameter uri must not be null!");
        }
        ArrayList<String> uris = new ArrayList<>();
        uris.add(uri);
        start(context, uris, 0, transitionView);
    }

    /**
     * 调用图片查看器的方法
     *
     * @param context     上下文
     * @param pictureUris 图片的Url集合
     * @param position    点击哪张图片进入图片查看器
     */
    public static void start(@NonNull Context context, @NonNull ArrayList<String> pictureUris, int position) {
        start(context, pictureUris, position, null);
    }

    /**
     * 调用图片查看器的方法(共享元素)
     *
     * @param context     上下文
     * @param pictureUris 图片的Url集合
     * @param position    点击哪张图片进入图片查看器
     */
    public static void start(@NonNull Context context, @NonNull ArrayList<String> pictureUris,
                             int position, View transitionView) {
        if (!(context instanceof Activity)) {
            throw new IllegalArgumentException("PictureWatcherActivity.start -> Parameter context cannot cast to be Activity!");
        }
        if (pictureUris.isEmpty()) {
            throw new IllegalArgumentException("PictureWatcherActivity.start -> Parameter pictureUris must not be null or empty!");
        }
        Activity activity = (Activity) context;
        Intent intent = new Intent(activity, PictureWatcherActivity.class);
        intent.putStringArrayListExtra(EXTRA_PICTURE_URIS, pictureUris);
        intent.putExtra(EXTRA_CUR_POSITION, position);
        // 5.0 以上的系统使用 Transition 跳转
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (transitionView != null) {// 共享元素
                intent.putExtra(EXTRA_SHARED_ELEMENT, true);
                activity.startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(
                        activity, transitionView, pictureUris.get(position)).toBundle());
            } else {
                activity.startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(activity).toBundle());
            }
        } else {
            activity.startActivity(intent);
        }
    }

    // 数据的集合
    private List<String> mUris = new ArrayList<>();
    private List<PhotoView> mPhotoViews = new ArrayList<>();
    private PictureWatcherAdapter mAdapter;

    // Flag
    private boolean mIsSharedElement = false;// 开启共享元素
    private String mSharedKey;
    private int mSharedPosition;// 共享元素的 key

    // 视图
    private DraggableViewPager mViewPager;
    private TextView mTvBottomIndicator;
    private int mCurPosition;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        parseIntent();
        // 5.0 以上的系统使用 Transition 过渡动画
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
            getWindow().setEnterTransition(new Explode().setDuration(300));
            getWindow().setReturnTransition(new Fade().setDuration(200));
        }
        setContentView(R.layout.activity_picture_watcher);
        // Postpone the shared element enter transition.
        if (mIsSharedElement) postponeEnterTransition();
        initTitle();
        initViews();
    }

    protected void parseIntent() {
        // 获取需要展示图片的 URI 集合
        mUris = getIntent().getStringArrayListExtra(EXTRA_PICTURE_URIS);
        // 获取当前需要展示的 Position
        mCurPosition = getIntent().getIntExtra(EXTRA_CUR_POSITION, 0);
        // 判断是否开启共享动画
        mIsSharedElement = getIntent().getBooleanExtra(EXTRA_SHARED_ELEMENT, false);
        if (mIsSharedElement) {
            mSharedPosition = mCurPosition;
            mSharedKey = mUris.get(mSharedPosition);
        }
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
    }

    protected void initViews() {
        // 初始化 ViewPager
        mViewPager = findViewById(R.id.view_pager);
        if (mIsSharedElement) mViewPager.setSharedElementPosition(mSharedPosition);
        mAdapter = new PictureWatcherAdapter(mPhotoViews);
        mViewPager.setAdapter(mAdapter);
        mViewPager.setOnPagerChangedListener(this);
        // 初始化底部的索引指示文本
        mTvBottomIndicator = findViewById(R.id.tv_pager_indicator);
        updateBottomIndicator(mCurPosition);
        if (!mIsSharedElement) {
            initData();
            return;
        }
        // 初始化共享元素展位图
        final ImageView ivSharedElementHolder = findViewById(R.id.iv_share_element_holder);
        Glide.with(this).asBitmap().load(mSharedKey).into(ivSharedElementHolder);
        ViewCompat.setTransitionName(ivSharedElementHolder, mSharedKey);
        ivSharedElementHolder.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                ivSharedElementHolder.getViewTreeObserver().removeOnPreDrawListener(this);
                startPostponedEnterTransition();
                initData();
                // 等待真正的 ViewPager 图片加载完成后再将共享元素占位图隐藏
                ivSharedElementHolder.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mPhotoViews.get(mSharedPosition).setVisibility(View.VISIBLE);
                        ivSharedElementHolder.setVisibility(View.GONE);
                    }
                }, 500);
                return true;
            }
        });
    }

    private void initData() {
        for (String uri : mUris) {
            PhotoView photoView = createPhotoView();
            mPhotoViews.add(photoView);
            if (mIsSharedElement && uri.equals(mSharedKey)) {
                ViewCompat.setTransitionName(photoView, mSharedKey);
                // 防止在共享元素动画完成之前展示出来
                photoView.setVisibility(View.INVISIBLE);
            }
        }
        mAdapter.notifyDataSetChanged();
        mViewPager.setCurrentItem(mCurPosition, false);
    }

    @Override
    public View onPagerChanged(int position) {
        // 更新当前页面共享元素的 key
        ViewCompat.setTransitionName(mPhotoViews.get(mSharedPosition),
                position == mSharedPosition ? mSharedKey : "");
        // 更新底部角标索引
        updateBottomIndicator(position);
        // 加载图片
        displayCurrentPhotoView(position);
        return mPhotoViews.get(position);
    }

    @Override
    public void finish() {
        super.finish();
        // 当前 Activity 关闭时, 使用淡入淡出的动画
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    /**
     * 获取底部索引指示的文本
     */
    private void updateBottomIndicator(int position) {
        int nowPager = position + 1;
        int AllPager = mUris.size();
        mTvBottomIndicator.setText(nowPager + "/" + AllPager);
    }

    /**
     * 显示图片当前位置的图片
     */
    private void displayCurrentPhotoView(int position) {
        // 加载当前的位置的图片
        PhotoView curView = mPhotoViews.get(position);
        if (curView != null && curView.getDrawable() == null) {
            Glide.with(this).load(mUris.get(position)).into(curView);
        }
        // 加载前一个
        int beforeIndex = position - 1;
        PhotoView beforeView = beforeIndex >= 0 ? mPhotoViews.get(beforeIndex) : null;
        if (beforeView != null && beforeView.getDrawable() == null) {
            Glide.with(this).load(mUris.get(beforeIndex)).into(beforeView);
        }
        // 加载后一个
        int afterIndex = position + 1;
        PhotoView afterView = afterIndex < mUris.size() ? mPhotoViews.get(afterIndex) : null;
        if (afterView != null && afterView.getDrawable() == null) {
            Glide.with(this).load(mUris.get(afterIndex)).into(afterView);
        }
    }

    /**
     * 创建 PhotoView
     */
    private PhotoView createPhotoView() {
        PhotoView photoView = new PhotoView(this);
        photoView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        photoView.setOnPhotoTapListener(new PhotoViewAttacher.OnPhotoTapListener() {
            @Override
            public void onPhotoTap(View view, float x, float y) {
                onBackPressed();
            }
        });
        photoView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                return false;
            }
        });
        return photoView;
    }

}

