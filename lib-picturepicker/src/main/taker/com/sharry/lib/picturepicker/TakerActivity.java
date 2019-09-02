package com.sharry.lib.picturepicker;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.sharry.lib.camera.AspectRatio;
import com.sharry.lib.camera.SCameraView;
import com.sharry.lib.media.player.OnStatusChangedListener;
import com.sharry.lib.media.player.SVideoView;
import com.sharry.lib.picturepicker.toolbar.ImageViewOptions;
import com.sharry.lib.picturepicker.toolbar.SToolbar;

import java.io.File;

/**
 * @author Sharry <a href="sharrychoochn@gmail.com">Contact me.</a>
 * @version 1.0
 * @since 2019-09-02
 */
public class TakerActivity extends AppCompatActivity implements
        ITakerContract.IView,
        AspectRatioFragment.Listener,
        RecordProgressButton.RecordListener {

    public static final int REQUEST_CODE = 286;
    public static final String RESULT_EXTRA_MEDIA_META = "RESULT_EXTRA_MEDIA_META";
    private static final String EXTRA_TAKER_CONFIG = "extra_taker_config";

    public static void launchForResult(CallbackFragment fragment, TakerConfig config) {
        Intent intent = new Intent(fragment.getActivity(), TakerActivity.class);
        intent.putExtra(EXTRA_TAKER_CONFIG, config);
        fragment.startActivityForResult(intent, REQUEST_CODE);
    }

    private static final AspectRatio[] ASPECT_RATIOS = {
            AspectRatio.of(1, 1),   // 1:1
            AspectRatio.of(4, 3),   // 4:3
            AspectRatio.of(16, 9),  // 16:9
            AspectRatio.of(2, 1)
    };
    private ITakerContract.IPresenter mPresenter;
    private SCameraView mCameraView;
    private SToolbar mToolbar;
    private RecordProgressButton mBtnRecord;
    private ImageView mIvPicturePreview;
    private SVideoView mVideoView;
    private ImageView mIvDenied;
    private ImageView mIvGranted;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.picture_picker_activity_taker);
        initTitle();
        initViews();
        initPresenter();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mVideoView.resume();
        mCameraView.startPreview();
    }

    @Override
    protected void onPause() {
        mCameraView.stopPreview();
        mVideoView.pause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mVideoView.stop();
        mPresenter.release();
        super.onDestroy();
    }

    ////////////////////////////////////ITakerContract.IView///////////////////////////////////////

    @Override
    public void videoPlay() {
        mVideoView.play();
    }

    @Override
    public void videoPlayNext(@NonNull String nextUri) {
        mVideoView.next(nextUri);
    }

    @Override
    public void setPreviewSource(@NonNull Bitmap bitmap) {
        mIvPicturePreview.setImageBitmap(bitmap);
    }

    @Override
    public void setToolbarVisible(boolean visible) {
        mToolbar.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public void setCameraViewVisible(boolean visible) {
        mCameraView.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public void setRecordButtonVisible(boolean visible) {
        mBtnRecord.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public void setVideoPlayerVisible(boolean visible) {
        mVideoView.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public void setGrantedButtonVisible(boolean visible) {
        mIvGranted.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public void setDeniedButtonVisible(boolean visible) {
        mIvDenied.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public void setPicturePreviewVisible(boolean visible) {
        mIvPicturePreview.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public void toast(@NonNull String content) {
        Toast.makeText(this, content, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void setMaxRecordDuration(long maxDuration) {
        mBtnRecord.setMaxProgress(maxDuration);
    }

    @Override
    public void setRecordButtonProgress(long currentDuration) {
        mBtnRecord.setCurrentPercent(currentDuration);
    }

    @Override
    public void startPreview() {
        mCameraView.startPreview();
    }

    @Override
    public void notifyFileDeleted(String filePath) {
        FileUtil.notifyMediaStore(this, filePath);
    }

    @Override
    public void setResult(@NonNull MediaMeta mediaMeta) {
        Intent intent = new Intent();
        intent.putExtra(RESULT_EXTRA_MEDIA_META, mediaMeta);
        FileUtil.notifyMediaStore(this, mediaMeta.path);
        setResult(RESULT_OK, intent);
        finish();
    }

    ////////////////////////////////////AspectRatioFragment.Listener///////////////////////////////////////

    @Override
    public void onAspectRatioSelected(@NonNull AspectRatio ratio) {
        mCameraView.setAspectRatio(ratio);
    }

    ////////////////////////////////////RecordProgressButton.RecordListener///////////////////////////////////////

    @Override
    public void onTakePicture() {
        mCameraView.stopPreview();
        mPresenter.handleTakePicture(mCameraView.takePicture());
    }

    @Override
    public void onRecordStart() {
        mPresenter.handleRecordStart(mCameraView);
    }

    @Override
    public void onRecordFinish(long duration) {
        mPresenter.handleRecordFinish();
    }

    private void initTitle() {
        mToolbar = findViewById(R.id.toolbar);
        int paddingSize = (int) dp2Px(this, 20f);
        // 全面屏
        mToolbar.addLeftMenuImage(
                ImageViewOptions.Builder()
                        .setDrawableResId(R.drawable.ic_activity_video_record_full_screen)
                        .setPaddingLeft(paddingSize)
                        .setListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mCameraView.setAdjustViewBounds(!mCameraView.getAdjustViewBounds());
                            }
                        })
                        .build()
        );
        // aspect
        mToolbar.addLeftMenuImage(
                ImageViewOptions.Builder()
                        .setDrawableResId(R.drawable.ic_activity_video_record_aspect)
                        .setPaddingLeft(paddingSize)
                        .setListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                AspectRatioFragment.newInstance(ASPECT_RATIOS, mCameraView.getAspectRatio())
                                        .show(getSupportFragmentManager(), AspectRatioFragment.class.getSimpleName());
                            }
                        })
                        .build()
        );
        // switch
        mToolbar.addLeftMenuImage(
                ImageViewOptions.Builder()
                        .setDrawableResId(R.drawable.ic_activity_video_record_camera_switch)
                        .setPaddingLeft(paddingSize)
                        .setListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                int curFacing = mCameraView.getFacing();
                                mCameraView.setFacing(curFacing == SCameraView.FACING_FRONT ?
                                        SCameraView.FACING_BACK : SCameraView.FACING_FRONT);
                            }
                        })
                        .build()
        );
    }

    private void initViews() {
        // SCameraView
        mCameraView = findViewById(R.id.camera_view);
        mCameraView.setAutoFocus(true);
        // RecordProgressButton
        mBtnRecord = findViewById(R.id.btn_record);
        mBtnRecord.setOnRecordListener(this);

        // Taken picture preview
        mIvPicturePreview = findViewById(R.id.iv_picture_preview);

        // Video player
        mVideoView = findViewById(R.id.video_view);
        mVideoView.setOnStatusChangedListener(new OnStatusChangedListener.Adapter() {
            @Override
            public void onPrepared(int totalDuration) {
                mPresenter.handleVideoPlayPrepared();
            }

            @Override
            public void onCompleted() {
                mPresenter.handleVideoPlayCompleted();
            }
        });

        // Denied
        mIvDenied = findViewById(R.id.iv_denied);
        mIvDenied.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPresenter.handleDenied();
            }
        });

        // Granted
        mIvGranted = findViewById(R.id.iv_granted);
        mIvGranted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPresenter.handleGranted();
            }
        });
    }

    private void initPresenter() {
        mPresenter = new TakerPresenter(
                this,
                (TakerConfig) getIntent().getParcelableExtra(EXTRA_TAKER_CONFIG)
        );
    }

    public static float dp2Px(@NonNull Context context, float dpVal) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dpVal, context.getResources().getDisplayMetrics());
    }
}
