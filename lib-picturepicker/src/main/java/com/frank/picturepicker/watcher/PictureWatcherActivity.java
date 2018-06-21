package com.frank.picturepicker.watcher;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.transition.Explode;
import android.transition.Fade;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.frank.picturepicker.R;
import com.frank.picturepicker.support.config.WatcherConfig;
import com.frank.picturepicker.support.loader.PictureLoader;
import com.frank.picturepicker.support.manager.watcher.PictureWatcherFragment;
import com.frank.picturepicker.watcher.widget.CheckedIndicatorView;
import com.frank.picturepicker.watcher.widget.DraggableViewPager;
import com.frank.picturepicker.watcher.widget.photoview.OnPhotoTapListener;
import com.frank.picturepicker.watcher.widget.photoview.PhotoView;
import com.frank.picturepicker.watcher.widget.toolbar.AppBarHelper;
import com.frank.picturepicker.watcher.widget.toolbar.GenericToolbar;
import com.frank.picturepicker.watcher.widget.toolbar.Style;

import java.util.ArrayList;

/**
 * Created by FrankChoo on 2017/12/28.
 * Email: frankchoochina@gmail.com
 * Version:  1.2
 * Description: 图片查看器的 Activity, 主题设置为背景透明效果更佳
 */
@SuppressLint("NewApi")
public class PictureWatcherActivity extends AppCompatActivity implements
        DraggableViewPager.OnPagerChangedListener,
        View.OnClickListener,
        PictureWatcherRecyclerAdapter.AdapterInteraction {

    private static final String TAG = PictureWatcherActivity.class.getSimpleName();
    public static final String EXTRA_CONFIG = "extra_config";
    public static final String EXTRA_SHARED_ELEMENT = "extra_shared_element";

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

    // 当前展示的 位置 和 URI
    private int mCurPosition;
    private String mCurUri;
    private PhotoView mCurView;

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
        mConfig = getIntent().getParcelableExtra(EXTRA_CONFIG);
        // 获取需要展示图片的 URI 集合
        mPictureUris = mConfig.pictureUris == null ?
                new ArrayList<String>() : mConfig.pictureUris;
        // 获取已经选中的图片
        mUserPickedSet = mConfig.userPickedSet;
        // 获取当前需要展示的 Position 和 URI
        mCurPosition = mConfig.position;
        mCurUri = mPictureUris.get(mCurPosition);
        // 判断是否开启共享动画
        mIsSharedElement = getIntent().getBooleanExtra(EXTRA_SHARED_ELEMENT, false);
        if (mIsSharedElement) {
            mSharedPosition = mCurPosition;
            mSharedKey = mPictureUris.get(mSharedPosition);
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
        // 设置标题文本
        toolbar.setTitleGravity(Gravity.LEFT);
        mTvTitle = toolbar.getTitleText();
        mTvTitle.setTextSize(20);
        // 图片是否选中的指示器
        if (mUserPickedSet == null) return;
        mCheckIndicator = new CheckedIndicatorView(this);
        mCheckIndicator.setChecked(false);
        mCheckIndicator.setBorderColor(mConfig.indicatorBorderCheckedColor, mConfig.indicatorBorderUncheckedColor);
        mCheckIndicator.setSolidColor(mConfig.indicatorSolidColor);
        mCheckIndicator.setTextColor(mConfig.indicatorTextColor);
        toolbar.addRightView(1, mCheckIndicator, 25, 25, this);
    }

    protected void initViews() {
        // 初始化 ViewPager
        mViewPager = findViewById(R.id.view_pager);
        if (mIsSharedElement) mViewPager.setSharedElementPosition(mSharedPosition);
        mAdapter = new PictureWatcherAdapter(mPhotoViews);
        mViewPager.setAdapter(mAdapter);
        mViewPager.setOnPagerChangedListener(this);
        // 初始化 RecyclerView
        if (mUserPickedSet != null) {
            mRecyclerView = findViewById(R.id.recycler_view);
            mRecyclerView.setVisibility(mUserPickedSet.isEmpty() ? View.INVISIBLE : View.VISIBLE);
            mRecyclerView.setAdapter(new PictureWatcherRecyclerAdapter(mUserPickedSet, this));
            mRecyclerView.setLayoutManager(new LinearLayoutManager(this,
                    LinearLayoutManager.HORIZONTAL, false));
        }
        if (!mIsSharedElement) {
            initData();
            return;
        }
        // 初始化共享元素展位图
        final ImageView ivSharedElementHolder = findViewById(R.id.iv_share_element_holder);
        PictureLoader.load(this, mSharedKey, ivSharedElementHolder);
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
                }, 600);
                return true;
            }
        });
    }

    private void initData() {
        // 填充数据
        for (String uri : mPictureUris) {
            PhotoView photoView = createPhotoView();
            mPhotoViews.add(photoView);
            if (mIsSharedElement && uri.equals(mSharedKey)) {
                ViewCompat.setTransitionName(photoView, mSharedKey);
                // 防止在共享元素动画完成之前展示出来, 造成重影
                photoView.setVisibility(View.INVISIBLE);
            }
        }
        mAdapter.notifyDataSetChanged();
        // 手动加载第一张需要展示的图片
        onPagerChanged(mCurPosition);
        mViewPager.bindCaptureView(mCurView);
        mViewPager.setCurrentItem(mCurPosition, false);
    }

    @Override
    public View onPagerChanged(int position) {
        // 更新当前页面的 URI
        mCurPosition = position;
        mCurUri = mPictureUris.get(position);
        mCurView = mPhotoViews.get(position);
        // 更新当前页面共享元素的 key
        ViewCompat.setTransitionName(mPhotoViews.get(mSharedPosition),
                position == mSharedPosition ? mSharedKey : "");
        updateToolbarIndicatorCheckedStatus();// 更新 Title 选中标识
        updateToolbarIndicatorTextContent(); // 更新标题角标索引
        displayCurrentPhotoView();// 加载图片
        return mCurView;
    }

    @Override
    public void finish() {
        if (mUserPickedSet != null) {
            Intent intent = new Intent();
            intent.putExtra(PictureWatcherFragment.RESULT_EXTRA_PICKED_PICTURES, mUserPickedSet);
            setResult(PictureWatcherFragment.REQUEST_CODE_PICKED, intent);
        }
        super.finish();
        // 当前 Activity 关闭时, 使用淡入淡出的动画
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    public void onClick(View v) {
        if (mCheckIndicator.isChecked()) {// Checked-> Unchecked
            // 移除选中数据与状态
            int removedIndex = mUserPickedSet.indexOf(mCurUri);
            mUserPickedSet.remove(removedIndex);
            mCheckIndicator.setChecked(false);
            // 通知 RecyclerView 数据变更
            mRecyclerView.getAdapter().notifyItemRemoved(removedIndex);
            if (mUserPickedSet.size() == 0) mRecyclerView.setVisibility(View.INVISIBLE);
        } else {// Unchecked -> Checked
            // 判断是否达到选择上限
            if (mUserPickedSet.size() < mConfig.threshold) {
                mUserPickedSet.add(mCurUri);
                int addedIndex = mUserPickedSet.indexOf(mCurUri);
                mCheckIndicator.setText(String.valueOf(addedIndex + 1));
                mCheckIndicator.setChecked(true);
                // 通知 RecyclerView 数据变更
                mRecyclerView.setVisibility(View.VISIBLE);
                mRecyclerView.getAdapter().notifyItemInserted(addedIndex);
                mRecyclerView.smoothScrollToPosition(addedIndex);
            } else {
                Toast.makeText(this, getString(R.string.activity_picture_watcher_msg_over_threshold_prefix)
                        + mConfig.threshold + getString(R.string.activity_picture_watcher_msg_over_threshold_suffix), Toast.LENGTH_SHORT).show();
                mCheckIndicator.setChecked(false);
            }
        }
    }

    @Override
    public void onItemClicked(ImageView imageView, String uri, int position) {
        int indexOfPictureUris = mPictureUris.indexOf(uri);
        if (indexOfPictureUris != -1) {
            mViewPager.setCurrentItem(indexOfPictureUris);
        }
    }

    /**
     * 更新 Toolbar 上标题文本的索引
     */
    private void updateToolbarIndicatorTextContent() {
        int nowPager = mCurPosition + 1;
        int AllPager = mPictureUris.size();
        mTvTitle.setText(nowPager + "/" + AllPager);
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