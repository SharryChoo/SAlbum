package frank.demo.photopicker.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.CheckableImageButton;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import frank.demo.photopicker.R;
import frank.demo.photopicker.adapter.ActivityPicturePickerAdapter;
import frank.demo.photopicker.adapter.DialogPickDirAdapter;
import frank.demo.photopicker.app_manager.BaseActivity;
import frank.demo.photopicker.app_manager.MyApp;
import frank.demo.photopicker.utils.AppBarsUtil;
import frank.demo.photopicker.utils.DialogUtil;

/**
 * Created by 99538 on 2017/7/25.
 */
public class PicturePickerActivity extends BaseActivity implements PicturePickerPresenter.PicturePickerViewInterface{

    public static final int MAX_PICKED_COUNT = 9;
    @MyApp.ViewResId(R.id.picked_text) private TextView mPickedText;
    @MyApp.ViewResId(R.id.ensure_picked) private TextView mEnsurePicked;
    @MyApp.ViewResId(R.id.recycler_view) private RecyclerView mRecyclerView;
    @MyApp.ViewResId(R.id.can_click_region) private LinearLayout mClickRegion;
    @MyApp.ViewResId(R.id.menu_button) private CheckableImageButton mMenuButton;
    @MyApp.ViewResId(R.id.folder_name) private TextView mFolderName;
    private ActivityPicturePickerAdapter mAdapter;
    private PicturePickerPresenter mPresenter;
    private List<String> mList = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_picker);
        MyApp.ViewResId(this, getWindow().getDecorView());
        String permissions[] = {Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE};
        requestRuntimePermission(permissions, new PermissionListener() {
            @Override
            public void onGranted() {
                initArgs();
                setViews();
                mPresenter.showCurrentFolder(0);
            }
        });
    }

    private void initArgs() {
        mPresenter = new PicturePickerPresenter(this);
        List<String> pickedUriList = getIntent().getStringArrayListExtra("picked_uri_list");
        if(pickedUriList != null) {
            mPresenter.initPickedList(pickedUriList);
        }
    }

    /**
     * 设置相关控件
     */
    private void setViews() {
        //设置状态栏透明
        AppBarsUtil.newInstance(this).setStatusBarStyle(AppBarsUtil.ALL_TRANSPARENT).commit();

        //设置图片被选中的状态
        mPickedText.setText("当前已选择图片" + mPresenter.fetchPickedList().size() + "/" + MAX_PICKED_COUNT);

        //RecyclerView
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        mAdapter = new ActivityPicturePickerAdapter(mList, mPresenter);
        mRecyclerView.setAdapter(mAdapter);

        //底部点击选择图片目录的区域
        mClickRegion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMenuButton.setChecked(true);
                View contentView = LayoutInflater.from(PicturePickerActivity.this).inflate(R.layout.dialog_picture_dir_list, null);
                RecyclerView recyclerView = (RecyclerView) contentView.findViewById(R.id.picture_dir_rv);
                recyclerView.setLayoutManager(new LinearLayoutManager(PicturePickerActivity.this));
                recyclerView.setAdapter(new DialogPickDirAdapter(mPresenter));
                DialogUtil.getInstance().displayDialogWindowWidth(PicturePickerActivity.this, contentView, Gravity.BOTTOM);
                DialogUtil.getInstance().getDialog().setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        mFolderName.setText(mPresenter.getCurrentFolderName());
                        mMenuButton.setChecked(false);
                        DialogUtil.getInstance().dismissDialog();
                    }
                });
            }
        });

        //确认按钮
        mEnsurePicked.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    @Override
    public void showCurrentFolderPicture(List<String> currentPictureList) {
        mList.clear();
        mList.addAll(currentPictureList);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void pictureStateChanged() {
        mPickedText.setText("当前已选择图片" + mPresenter.fetchPickedList().size() + "/" + MAX_PICKED_COUNT);
    }

}
