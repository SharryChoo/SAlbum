package com.frank.lib_picturepicker.picturepicker;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.frank.lib_picturepicker.R;
import com.frank.lib_picturepicker.picturepicker.mvp.PicturePickerContract;
import com.frank.lib_picturepicker.picturepicker.mvp.presenter.PicturePickerPresenter;
import com.frank.lib_picturepicker.picturepicker.support.PicturePickerConfig;
import com.frank.lib_picturepicker.picturepicker.support.PicturePickerFragment;
import com.frank.lib_picturepicker.picturewatcher.support.PictureWatcherCallback;
import com.frank.lib_picturepicker.picturewatcher.support.PictureWatcherManager;
import com.frank.lib_picturepicker.toolbar.AppBarHelper;
import com.frank.lib_picturepicker.toolbar.GenericToolbar;
import com.frank.lib_picturepicker.toolbar.Style;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by think on 2018/5/26.
 * Email: frankchoochina@gmail.com
 * Version: 1.0
 * Description: 图片选择器的 Activity
 */
public class PicturePickerActivity extends AppCompatActivity implements PicturePickerContract.IView,
        PicturePickerAdapter.AdapterInteraction,
        View.OnClickListener {

    /**
     * Intent 常量
     */
    public static final String EXTRA_CONFIG = "extra_config";// 用户配置的属性

    /**
     * Toolbar 上添加的控件的 Tag
     */
    private final int TAG_TOOLBAR_BACK = 0x00000001;
    private final int TAG_TOOLBAR_CHECKED_DETAIL = 0x00000002;
    private final int TAG_TOOLBAR_ENSURE = 0x00000003;

    private PicturePickerPresenter mPresenter = new PicturePickerPresenter();

    // 通过 Intent 传递过来的数据
    private PicturePickerConfig mConfig;

    // View 视图
    private GenericToolbar mToolbar;
    private RecyclerView mRecyclerView;
    private TextView mTvToolbarFolderName;
    private TextView mTvSelectedFolderName;
    private TextView mTvPreview;
    private TextView mTvToolbarEnsure;
    private FloatingActionButton mFab;

    // 选择图片文件夹的弹窗
    private PicturePickerDialog mDialog;

    // 用于保存数据的相关集合
    private ArrayList<String> mCurDisplayPaths = new ArrayList<>();// 用户选中的文件夹下所有图片的集合

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_picker);
        mPresenter.attach(this);
        parseIntent();
        initTitle();
        initViews();
        initData();
    }

    protected void parseIntent() {
        mConfig = getIntent().getParcelableExtra(EXTRA_CONFIG);
        // 获取外界传递过来, 用户已经获取到的图片的 URI 集合
        mPresenter.setupUserPickedSet(mConfig.userPickedSet);
        // 获取传入的阈值
        mPresenter.setupThreshold(mConfig.threshold);
    }

    protected void initTitle() {
        // 设置沉浸式状态栏
        AppBarHelper.with(this).setStatusBarStyle(Style.TRANSPARENT).apply();
        // 初始化视图
        mToolbar = findViewById(R.id.toolbar);
        mToolbar.setAdjustToTransparentStatusBar(true);
        // 设置背景
        if (mConfig.toolbarBkgColor != PicturePickerConfig.INVALIDATE_VALUE)
            mToolbar.setBackgroundColor(mConfig.toolbarBkgColor);
        if (mConfig.toolbarBkgDrawableResId != PicturePickerConfig.INVALIDATE_VALUE)
            mToolbar.setBackgroundDrawableRes(mConfig.toolbarBkgDrawableResId);
        // 添加返回按钮
        mToolbar.addLeftIcon(TAG_TOOLBAR_BACK, R.drawable.icon_common_arrow_back_white, this);
        // 添加选中详情的文本
        mToolbar.addLeftText(TAG_TOOLBAR_CHECKED_DETAIL, "所有图片", 20, null);
        mTvToolbarFolderName = mToolbar.getViewByTag(TAG_TOOLBAR_CHECKED_DETAIL);
        // 添加图片预览按钮
        mToolbar.addRightText(TAG_TOOLBAR_ENSURE, "确认", 15, this);
        mTvToolbarEnsure = mToolbar.getViewByTag(TAG_TOOLBAR_ENSURE);
    }

    protected void initViews() {
        // RecyclerView
        mRecyclerView = findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, mConfig.spanCount));
        mRecyclerView.setAdapter(new PicturePickerAdapter(this, mCurDisplayPaths, mConfig));
        // 底部菜单控制区域
        findViewById(R.id.ll_bottom_menu).setOnClickListener(this);
        mTvSelectedFolderName = findViewById(R.id.tv_folder_name);
        mTvPreview = findViewById(R.id.tv_preview);
        mTvPreview.setOnClickListener(this);
        // 悬浮菜单
        mFab = findViewById(R.id.fab);
        mFab.setBackgroundColor(mConfig.toolbarBkgColor);
        mFab.setOnClickListener(this);
    }

    protected void initData() {
        mPresenter.initData(this);
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
    public void updateTextContent(int curPicked, int total) {
        if (mTvToolbarEnsure == null || mTvPreview == null) return;
        mTvToolbarEnsure.setText("确认 (" + curPicked + "/" + total + ")");
        mTvPreview.setText("预览 (" + curPicked + ")");
    }

    @Override
    public void updateTextViewVisibility(boolean isVisible) {
        if (mTvToolbarEnsure == null || mTvPreview == null) return;
        int visible = isVisible ? View.VISIBLE : View.INVISIBLE;
        mTvToolbarEnsure.setVisibility(visible);
        mTvPreview.setVisibility(visible);
    }

    @Override
    public void showMsg(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public List<String> getPickedPictures() {
        return mPresenter.fetchUserPickedSet();
    }

    @Override
    public boolean onIndicatorSelected(String uri) {
        return mPresenter.performPicturePicked(uri);
    }

    @Override
    public void onIndicatorDeselect(String uri) {
        mPresenter.performPictureRemoved(uri);
    }

    @Override
    public void onPictureClick(ImageView imageView, String uri, int position) {
        PictureWatcherManager.with(this)
                .setThreshold(mConfig.threshold)
                .setIndicatorTextColor(mConfig.indicatorTextColor)
                .setIndicatorSolidColor(mConfig.indicatorSolidColor)
                .setIndicatorBorderColor(mConfig.indicatorBorderCheckedColor, mConfig.indicatorBorderUncheckedColor)
                .setPictureUris(mCurDisplayPaths, position)
                .setUserPickedSet(mPresenter.fetchUserPickedSet())
                .setSharedElement(imageView)
                .start(new PictureWatcherCallback() {
                    @Override
                    public void onResult(ArrayList<String> userPickedSet) {
                        mPresenter.setupUserPickedSet(userPickedSet);
                        mRecyclerView.getAdapter().notifyDataSetChanged();
                    }
                });
    }

    @Override
    public void onClick(View v) {
        if (v == mToolbar.getViewByTag(TAG_TOOLBAR_BACK)) {// 返回按钮
            onBackPressed();
        } else if (v == mToolbar.getViewByTag(TAG_TOOLBAR_ENSURE) || v.getId() == R.id.fab) {// 确认按钮
            Intent intent = new Intent();
            intent.putExtra(PicturePickerFragment.RESULT_EXTRA_PICKED_PICTURES, mPresenter.fetchUserPickedSet());
            setResult(PicturePickerFragment.REQUEST_CODE_PICKED, intent);
            finish();
        } else if (v.getId() == R.id.ll_bottom_menu) {
            if (mDialog == null) {
                mDialog = new PicturePickerDialog(this, mPresenter.fetchAllPictureFolders())
                        .setOnItemClickedListener(new PicturePickerDialog.OnItemClickedListener() {
                            @Override
                            public void onDialogItemClicked(int position) {
                                mPresenter.fetchDisplayPictures(position);
                            }
                        });
            }
            mDialog.show();
        } else if (v.getId() == R.id.tv_preview) {// 预览按钮
            PictureWatcherManager.with(this)
                    .setThreshold(mConfig.threshold)
                    .setIndicatorTextColor(mConfig.indicatorTextColor)
                    .setIndicatorSolidColor(mConfig.indicatorSolidColor)
                    .setIndicatorBorderColor(mConfig.indicatorBorderCheckedColor, mConfig.indicatorBorderUncheckedColor)
                    .setPictureUris(mPresenter.fetchUserPickedSet(), 0)
                    .setUserPickedSet(mPresenter.fetchUserPickedSet())
                    .start(new PictureWatcherCallback() {
                        @Override
                        public void onResult(ArrayList<String> userPickedSet) {
                            mPresenter.setupUserPickedSet(userPickedSet);
                            mRecyclerView.getAdapter().notifyDataSetChanged();
                        }
                    });
        }
    }

}
