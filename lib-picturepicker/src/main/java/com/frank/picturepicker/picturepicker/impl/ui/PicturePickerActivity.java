package com.frank.picturepicker.picturepicker.impl.ui;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.frank.picturepicker.R;
import com.frank.picturepicker.picturepicker.impl.mvp.PicturePickerContract;
import com.frank.picturepicker.picturepicker.impl.mvp.PicturePickerPresenter;
import com.frank.picturepicker.picturepicker.manager.PickerConfig;
import com.frank.picturepicker.widget.PicturePickerFabBehavior;
import com.frank.picturepicker.widget.toolbar.AppBarHelper;
import com.frank.picturepicker.widget.toolbar.GenericToolbar;
import com.frank.picturepicker.widget.toolbar.Style;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by think on 2018/5/26.
 * Email: frankchoochina@gmail.com
 * Version: 1.2
 * Description: 图片选择器的 Activity
 */
public class PicturePickerActivity extends AppCompatActivity implements PicturePickerContract.IView,
        PicturePickerAdapter.AdapterInteraction,
        View.OnClickListener {

    /**
     * Intent 常量
     */
    public static final String START_INTENT_EXTRA_CONFIG = "start_intent_extra_config";// 用户配置的属性
    public static final String RESULT_INTENT_EXTRA_PICKED_PICTURES = "result_intent_extra_picked_pictures";// 返回的图片

    /**
     * Toolbar 上添加的控件的 Tag
     */
    private final int TAG_TOOLBAR_BACK = 0x00000001;
    private final int TAG_TOOLBAR_CHECKED_DETAIL = 0x00000002;
    private final int TAG_TOOLBAR_ENSURE = 0x00000003;

    private PicturePickerContract.IPresenter mPresenter = new PicturePickerPresenter();

    // View 视图
    private GenericToolbar mToolbar;
    private RecyclerView mRecyclerView;
    private TextView mTvToolbarFolderName;
    private TextView mTvSelectedFolderName;
    private TextView mTvPreview;
    private TextView mTvToolbarEnsure;

    // 用于保存数据的相关集合
    private ArrayList<String> mCurDisplayPaths = new ArrayList<>();// 用户选中的文件夹下所有图片的集合
    private FloatingActionButton mFab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        parseIntent();
        setContentView(R.layout.activity_picture_picker);
        mPresenter.attach(this);
        initTitle();
        initViews();
        initData();
    }

    protected void parseIntent() {
        mPresenter.init((PickerConfig) getIntent().getParcelableExtra(START_INTENT_EXTRA_CONFIG));
    }

    protected void initTitle() {
        // 设置沉浸式状态栏
        AppBarHelper.with(this).setStatusBarStyle(Style.TRANSPARENT).apply();
        // 初始化视图
        mToolbar = findViewById(R.id.toolbar);
        mToolbar.setAdjustToTransparentStatusBar(true);
        // 添加返回按钮
        mToolbar.addLeftIcon(TAG_TOOLBAR_BACK, R.drawable.icon_common_arrow_back_white, this);
        // 添加选中详情的文本
        mToolbar.addLeftText(TAG_TOOLBAR_CHECKED_DETAIL, getString(R.string.activity_picture_picker_all_picture), 20, null);
        mTvToolbarFolderName = mToolbar.getViewByTag(TAG_TOOLBAR_CHECKED_DETAIL);
        // 添加图片确认按钮
        mToolbar.addRightText(TAG_TOOLBAR_ENSURE, getString(R.string.activity_picture_picker_btn_toolbar_ensure), 15, this);
        mTvToolbarEnsure = mToolbar.getViewByTag(TAG_TOOLBAR_ENSURE);
    }

    protected void initViews() {
        // RecyclerView
        mRecyclerView = findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this,
                mPresenter.getConfig().spanCount));
        mRecyclerView.setAdapter(new PicturePickerAdapter(this, mCurDisplayPaths,
                mPresenter.getConfig()));
        // 底部菜单控制区域
        findViewById(R.id.ll_bottom_menu).setOnClickListener(this);
        mTvSelectedFolderName = findViewById(R.id.tv_folder_name);
        mTvPreview = findViewById(R.id.tv_preview);
        mTvPreview.setOnClickListener(this);
        // 悬浮按钮
        mFab = findViewById(R.id.fab);
        mFab.setVisibility(View.GONE);
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) mFab.getLayoutParams();
        params.setBehavior(new PicturePickerFabBehavior());
        mFab.setLayoutParams(params);
        mFab.setOnClickListener(this);
    }

    protected void initData() {
        mPresenter.fetchData(this);
    }

    @Override
    public void setToolbarBackgroundColor(int color) {
        mToolbar.setBackgroundColor(color);
    }

    @Override
    public void setToolbarBackgroundDrawable(int drawableId) {
        mToolbar.setBackgroundDrawableRes(drawableId);
    }

    @Override
    public void setToolbarScrollable(boolean isScrollable) {
        if (isScrollable) {
            AppBarLayout.LayoutParams params = (AppBarLayout.LayoutParams) mToolbar.getLayoutParams();
            params.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL
                    | AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS
                    | AppBarLayout.LayoutParams.SCROLL_FLAG_SNAP);
            mToolbar.setLayoutParams(params);
        }
    }

    @Override
    public void setBackgroundColor(int color) {
        mRecyclerView.setBackgroundColor(color);
    }

    @Override
    public void setFabColor(int color) {
        mFab.setBackgroundTintList(ColorStateList.valueOf(color));
    }

    @Override
    public void switchFabVisibility(boolean isVisible) {
        mFab.setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }

    @Override
    public void displayPictures(String folderName, List<String> uris) {
        // 更新文件夹名称
        mTvSelectedFolderName.setText(folderName);
        mTvToolbarFolderName.setText(folderName);
        // 刷新数据
        mCurDisplayPaths.clear();
        mCurDisplayPaths.addAll(uris);
        mRecyclerView.getAdapter().notifyDataSetChanged();
    }

    @Override
    public void displayToolbarEnsureText(CharSequence content) {
        mTvToolbarEnsure.setText(content);
    }

    @Override
    public void displayPreviewText(CharSequence content) {
        mTvPreview.setText(content);
    }

    @Override
    public void notifyUserPickedSetChanged() {
        mRecyclerView.getAdapter().notifyDataSetChanged();
    }

    @Override
    public void notifyCameraTakeOnePicture(String path) {
        mCurDisplayPaths.add(0, path);
        mRecyclerView.getAdapter().notifyItemInserted(1);
    }

    @Override
    public void showMsg(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public List<String> onUserPickedSet() {
        return mPresenter.fetchUserPickedSet();
    }

    @Override
    public boolean onPictureChecked(String uri) {
        return mPresenter.performPictureChecked(uri);
    }

    @Override
    public void onPictureUnchecked(String uri) {
        mPresenter.performPictureUnchecked(uri);
    }

    @Override
    public void onPictureClicked(ImageView imageView, String uri, int position) {
        mPresenter.performPictureClicked(mCurDisplayPaths, position, imageView);
    }

    @Override
    public void onCameraClicked() {
        mPresenter.performCameraClicked();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.ll_bottom_menu) {// 底部菜单按钮
            mPresenter.performBottomMenuClicked();
        } else if (v.getId() == R.id.tv_preview) {// 预览按钮
            mPresenter.performPreviewClicked();
        } else if (v == mToolbar.getViewByTag(TAG_TOOLBAR_BACK)) {// 返回按钮
            onBackPressed();
        } else if (v == mToolbar.getViewByTag(TAG_TOOLBAR_ENSURE) || v.getId() == R.id.fab) {// 确认按钮
            mPresenter.performEnsureClicked();
        }
    }

}
