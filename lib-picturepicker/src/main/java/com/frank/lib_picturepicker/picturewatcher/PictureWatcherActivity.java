package com.frank.lib_picturepicker.picturewatcher;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.frank.lib_picturepicker.R;
import com.frank.lib_picturepicker.widget.DraggableViewPager;
import com.frank.lib_picturepicker.widget.toolbar.AppBarHelper;
import com.frank.lib_picturepicker.widget.toolbar.GenericToolbar;
import com.frank.lib_picturepicker.widget.toolbar.Style;

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
public class PictureWatcherActivity extends AppCompatActivity implements DraggableViewPager.OnPagerChangedListener {

    public static final String EXTRA_PICTURE_URIS = "extra_picture_uris";
    public static final String EXTRA_CUR_POSITION = "extra_cur_position";

    /**
     * 调用图片查看器的方法
     *
     * @param context     上下文
     * @param position    点击哪张图片进入图片查看器
     * @param pictureUris 图片的Url集合
     */
    public static void start(Context context, int position, ArrayList<String> pictureUris) {
        if (pictureUris == null || pictureUris.isEmpty()) {
            throw new IllegalArgumentException("PictureWatcherActivity.start -> pictureUris must not be null!");
        }
        Intent intent = new Intent(context, PictureWatcherActivity.class);
        intent.putStringArrayListExtra(EXTRA_PICTURE_URIS, pictureUris);
        intent.putExtra(EXTRA_CUR_POSITION, position);
        context.startActivity(intent);
    }

    private GenericToolbar mToolbar;
    private TextView mTvBottomIndicator;
    private List<PhotoView> mPhotoViews = new ArrayList<>();
    private List<String> mUris = new ArrayList<>();
    private int mCurPosition;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_watcher);
        parseIntent();
        initTitle();
        initViews();
    }

    protected void parseIntent() {
        // 获取需要展示图片的 URI 集合
        List<String> uris = getIntent().getStringArrayListExtra(EXTRA_PICTURE_URIS);
        for (String uri : uris) {
            mUris.add(uri);
            mPhotoViews.add(createPhotoView());
        }
        // 获取当前需要展示的 Position
        mCurPosition = getIntent().getIntExtra(EXTRA_CUR_POSITION, 0);
    }

    protected void initTitle() {
        AppBarHelper.with(this).setStatusBarStyle(Style.TRANSPARENT).apply();
        mToolbar = findViewById(R.id.toolbar);
        mToolbar.setAdjustToTransparentStatusBar(true);
        mToolbar.addLeftIcon(0, R.drawable.ic_arrow_back_white, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    protected void initViews() {
        //加载图片
        Glide.with(this).load(mUris.get(mCurPosition)).into(mPhotoViews.get(mCurPosition));
        // 初始化底部的索引指示文本
        mTvBottomIndicator = findViewById(R.id.tv_pager_indicator);
        updateBottomIndicator(mCurPosition);
        // 初始化 ViewPager
        DraggableViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(new PictureWatcherAdapter(mPhotoViews));
        viewPager.setCurrentItem(mCurPosition);
        viewPager.setOnPagerChangedListener(this);
    }

    @Override
    public View onPagerChanged(int position) {
        updateBottomIndicator(position);
        final PhotoView photoView = mPhotoViews.get(position);
        // 若 PhotoView 中的图片没有内容, 则加载
        if (photoView.getDrawable() == null) {
            Glide.with(this).load(mUris.get(position)).into(photoView);
        }
        return photoView;
    }

    /**
     * 获取底部索引指示的文本
     */
    private void updateBottomIndicator(int position) {
        int nowPager = position + 1;
        int AllPager = mUris.size();
        mTvBottomIndicator.setText(nowPager + "/" + AllPager);
    }

    private PhotoView createPhotoView() {
        PhotoView photoView = new PhotoView(this);
        photoView.setOnPhotoTapListener(new PhotoViewAttacher.OnPhotoTapListener() {
            @Override
            public void onPhotoTap(View view, float x, float y) {
                finish();
            }
        });
        photoView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                // showDialog();
                return false;
            }
        });
        return photoView;
    }

    private void showDialog() {
      /*  Dialog dialog = new Dialog.Builder(this)
                .setLayoutRes(R.layout.dialog_save_photo)
                .setGravity(Gravity.BOTTOM)
                .show();
        view.findViewById(R.id.save_image_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveImage();
                DialogUtil.getManager().dismissDialog();
            }
        });*/
    }

    private void saveImage() {
        /*String permissions[] = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
        requestRuntimePermission(permissions, new PermissionListener() {
            @Override
            public void onGranted() {
                Glide.with(PictureWatcherActivity.this).load(mUris.get(mCurPosition)).asBitmap().into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                        //将文件保存到SD卡中,并且刷新文件管理器刷新显示
                        FileUtils fileUtils = FileUtils.getManager();
                        File file = fileUtils.createSimpleDateNameFile(FileUtils.SD);
                        BitmapUtils.writeBitmapToFile(file, resource, 100);
                        fileUtils.notifyFileChanged(file.getPath());
                        String path = file.getPath();
                        Toast.makeText(MyApp.getContext(), "保存完毕" + path, Toast.LENGTH_LONG).show();
                    }
                });
            }
        });*/
    }

}

