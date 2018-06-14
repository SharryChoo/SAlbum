package com.frank.lib_picturepicker.picturepicker.mvp.view;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.frank.lib_picturepicker.R;
import com.frank.lib_picturepicker.picturepicker.mvp.PicturePickerContract;
import com.frank.lib_picturepicker.picturepicker.mvp.presenter.PicturePickerPresenter;
import com.frank.lib_picturepicker.picturewatcher.PictureWatcherActivity;
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
    public static final String EXTRA_USER_PICKED_PICTURES = "extra_user_picked_pictures";// 用户传入的已经选中的集合
    public static final String EXTRA_PICKED_THRESHOLD = "extra_picked_threshold";// 最大选择数量
    public static final String EXTRA_SPAN_COUNT = "extra_span_count";// 最大选择数量
    // Toolbar 背景
    public static final String EXTRA_TOOLBAR_BACKGROUND_COLOR = "extra_toolbar_background_color";// Toolbar 背景色
    public static final String EXTRA_TOOLBAR_BACKGROUND_DRAWABLE_RES = "extra_toolbar_background_drawable";// Toolbar 背景图片
    // Indicator 指示器的样式
    public static final String EXTRA_PICKED_INDICATOR_SOLID_COLOR = "extra_picked_indicator_solid_color";// 指示器内部填充的颜色
    public static final String EXTRA_PICKED_INDICATOR_BORDER_CHECKED_COLOR = "extra_picked_indicator_border_checked_color";// 指示器边框选中的颜色
    public static final String EXTRA_PICKED_INDICATOR_BORDER_UNCHECKED_COLOR = "extra_picked_indicator_border_unchecked_color";// 指示器边框未选中的颜色

    /**
     * Activity Result 相关
     */
    public static final int REQUEST_CODE = 0x00001234;// 请求码
    public static final String RESULT_EXTRA_PICKED_PICTURES = "extra_picked_pictures";// 返回的图片

    /**
     * Toolbar 上添加的控件的 Tag
     */
    private final int TAG_TOOLBAR_BACK = 0x00000001;
    private final int TAG_TOOLBAR_CHECKED_DETAIL = 0x00000002;
    private final int TAG_TOOLBAR_ENSURE = 0x00000003;
    private final int INVALIDATE_VALUE = -1;

    private PicturePickerPresenter mPresenter = new PicturePickerPresenter();

    // 通过 Intent 传递过来的数据
    private int mSpanCount = 3;
    private int mToolbarBkgColor = INVALIDATE_VALUE;
    private int mToolbarBkgDrawableRes = INVALIDATE_VALUE;
    private int mIndicatorSolidColor = INVALIDATE_VALUE;
    private int mIndicatorBorderCheckedColor = INVALIDATE_VALUE;
    private int mIndicatorBorderUncheckedColor = INVALIDATE_VALUE;

    // View 视图
    private GenericToolbar mToolbar;
    private RecyclerView mRecyclerView;
    private TextView mTvSelectedFolderName;
    private TextView mTvPreview;
    private TextView mTvToolbarEnsure;

    // 选择图片文件夹的弹窗
    private PicturePickerDialog mDialog;

    // 用于保存数据的相关集合
    private List<String> mCurDisplayPaths = new ArrayList<>();// 用户选中的文件夹下所有图片的集合
    private TextView mTvToolbarFolderName;

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
        // 获取外界传递过来, 用户已经获取到的图片的 URI 集合
        mPresenter.setupUserPicked(getIntent().getStringArrayListExtra(EXTRA_USER_PICKED_PICTURES));
        // 获取传入的阈值
        mPresenter.setupThreshold(getIntent().getIntExtra(EXTRA_PICKED_THRESHOLD, 9));
        // 获取用户设置的每行展示多少张图片
        mSpanCount = getIntent().getIntExtra(EXTRA_SPAN_COUNT, mSpanCount);
        // Toolbar 背景色
        mToolbarBkgColor = getIntent().getIntExtra(EXTRA_TOOLBAR_BACKGROUND_COLOR, mToolbarBkgColor);
        // Toolbar 背景图
        mToolbarBkgDrawableRes = getIntent().getIntExtra(EXTRA_TOOLBAR_BACKGROUND_DRAWABLE_RES, mToolbarBkgDrawableRes);
        // 获取指示器中间填充的颜色
        mIndicatorSolidColor = getIntent().getIntExtra(EXTRA_PICKED_INDICATOR_SOLID_COLOR, mIndicatorSolidColor);
        // 获取指示器边框的颜色
        mIndicatorBorderCheckedColor = getIntent().getIntExtra(EXTRA_PICKED_INDICATOR_BORDER_CHECKED_COLOR, mIndicatorBorderCheckedColor);
        mIndicatorBorderUncheckedColor = getIntent().getIntExtra(EXTRA_PICKED_INDICATOR_BORDER_UNCHECKED_COLOR, mIndicatorBorderUncheckedColor);
    }

    protected void initTitle() {
        // 设置沉浸式状态栏
        AppBarHelper.with(this).setStatusBarStyle(Style.TRANSPARENT).apply();
        // 初始化视图
        mToolbar = findViewById(R.id.toolbar);
        mToolbar.setAdjustToTransparentStatusBar(true);
        mToolbar.setTextColor(Color.WHITE);
        // 设置背景
        if (mToolbarBkgColor != INVALIDATE_VALUE)
            mToolbar.setBackgroundColor(mToolbarBkgColor);
        if (mToolbarBkgDrawableRes != INVALIDATE_VALUE)
            mToolbar.setBackgroundDrawableRes(mToolbarBkgDrawableRes);
        // 添加返回按钮
        mToolbar.addLeftIcon(TAG_TOOLBAR_BACK, R.drawable.icon_common_arrow_back_white, this);
        // 添加选中详情的文本
        mToolbar.addLeftText(TAG_TOOLBAR_CHECKED_DETAIL, "所有图片", 20, null);
        mTvToolbarFolderName = mToolbar.getViewByTag(TAG_TOOLBAR_CHECKED_DETAIL);
        // 添加图片预览按钮
        mToolbar.addRightText(TAG_TOOLBAR_ENSURE, "发送", 15, this);
        mTvToolbarEnsure = mToolbar.getViewByTag(TAG_TOOLBAR_ENSURE);
    }

    protected void initViews() {
        // RecyclerView
        mRecyclerView = findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, mSpanCount));
        mRecyclerView.setAdapter(new PicturePickerAdapter(this, mCurDisplayPaths, mSpanCount,
                mIndicatorSolidColor, mIndicatorBorderCheckedColor, mIndicatorBorderUncheckedColor));
        // 底部菜单控制区域
        findViewById(R.id.ll_bottom_menu).setOnClickListener(this);
        mTvSelectedFolderName = findViewById(R.id.tv_folder_name);
        mTvPreview = findViewById(R.id.tv_preview);
        mTvPreview.setOnClickListener(this);
    }

    protected void initData() {
        mPresenter.initData(this);
    }

    @Override
    public void displaySelectedFolder(String folderName, List<String> uris) {
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
        mTvPreview.setText("预览 (" + curPicked + ")");
        mTvToolbarEnsure.setText("发送 (" + curPicked + "/" + total + ")");
    }

    @Override
    public void updateTextViewVisibility(boolean isVisible) {
        int visible = isVisible ? View.VISIBLE : View.INVISIBLE;
        mToolbar.getViewByTag(TAG_TOOLBAR_ENSURE).setVisibility(visible);
        mTvPreview.setVisibility(visible);
    }

    @Override
    public void showMsg(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public List<String> getPickedPictures() {
        return mPresenter.getPickedPictures();
    }

    @Override
    public boolean onPicturePicked(String imagePath) {
        return mPresenter.performPicturePicked(imagePath);
    }

    @Override
    public void onPictureRemoved(String imagePath) {
        mPresenter.performPictureRemoved(imagePath);
    }

    @Override
    public void onClick(View v) {
        if (v == mToolbar.getViewByTag(TAG_TOOLBAR_BACK)) {// 返回按钮
            onBackPressed();
        } else if (v == mToolbar.getViewByTag(TAG_TOOLBAR_ENSURE)) {// 确认按钮
            Intent intent = new Intent();
            intent.putExtra(RESULT_EXTRA_PICKED_PICTURES, (ArrayList<String>) mPresenter.getPickedPictures());
            setResult(REQUEST_CODE, intent);
            finish();
        } else if (v.getId() == R.id.tv_preview) {// 预览按钮
            PictureWatcherActivity.start(this, 0,
                    (ArrayList<String>) mPresenter.getPickedPictures());
        } else if (v.getId() == R.id.ll_bottom_menu) {
            if (mDialog == null) {
                mDialog = new PicturePickerDialog(this, mPresenter.fetchAllPictureFolders())
                        .setOnItemClickedListener(new PicturePickerDialog.OnItemClickedListener() {
                            @Override
                            public void onDialogItemClicked(int position) {
                                mPresenter.fetchPicturePathsAt(position);
                            }
                        });
            }
            mDialog.show();
        }
    }
}
