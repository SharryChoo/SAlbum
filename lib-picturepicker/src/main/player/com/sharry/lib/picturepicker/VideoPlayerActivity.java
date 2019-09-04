package com.sharry.lib.picturepicker;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSeekBar;
import androidx.constraintlayout.widget.ConstraintLayout;

/**
 * 视频播放的 Activity
 *
 * @author Sharry <a href="xiaoyu.zhu@1hai.cn">Contact me.</a>
 * @version 1.0
 * @since 2019-09-04 16:33
 */
public class VideoPlayerActivity extends AppCompatActivity implements MediaPlayer.OnPreparedListener,
        MediaPlayer.OnCompletionListener,
        MediaPlayer.OnErrorListener,
        View.OnClickListener {

    private static final String EXTRA_MEDIA_META = "extra_media_meta";
    private ObjectAnimator mPanelHindAnimator;
    private ObjectAnimator mPanelShowAnimator;

    public static void launch(Context context, MediaMeta mediaMeta) {
        Intent intent = new Intent(context, VideoPlayerActivity.class);
        intent.putExtra(EXTRA_MEDIA_META, mediaMeta);
        context.startActivity(intent);
        if (context instanceof Activity) {
            ((Activity) context).overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }
    }

    private static final int MSG_WHAT_UPDATE_PROGRESS = 0;

    private final Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what != MSG_WHAT_UPDATE_PROGRESS) {
                return;
            }
            // 更新进度
            updateProgress();
        }
    };

    private MediaMeta mDataSource;

    /**
     * Widget.
     */
    private FrameLayout mFlContainer;
    private VideoView mVideoView;
    private TextView mTvCurrent;
    private AppCompatSeekBar mSeekBar;
    private TextView mTvTotal;
    private ConstraintLayout mClControl;
    private ImageView mIvControl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.picture_picker_widget_video_player);
        parseIntent();
        initViews();
        initData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mVideoView.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mVideoView.stopPlayback();
        mHandler.removeMessages(MSG_WHAT_UPDATE_PROGRESS);
    }

    @Override
    public void onBackPressed() {
        mVideoView.stopPlayback();
        super.onBackPressed();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    ////////////////////////////////////MediaPlayer.OnCompletionListener///////////////////////////////////////

    @Override
    public void onPrepared(MediaPlayer mp) {
        // 为 View 注入数据
        mIvControl.setImageResource(R.drawable.ic_picture_picker_player_video_pasue);
        mTvCurrent.setText(DateUtil.format(0));
        mTvTotal.setText(DateUtil.format(mp.getDuration()));
        mSeekBar.setMax(mp.getDuration());
        // 播放
        mVideoView.start();
        // 更新进度
        updateProgress();
        // 隐藏控制面板
        hindControlPanel();
    }

    ////////////////////////////////////MediaPlayer.OnErrorListener///////////////////////////////////////

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        resetViews();
        Toast.makeText(this, "该视频无法播放", Toast.LENGTH_SHORT).show();
        return true;
    }

    ////////////////////////////////////MediaPlayer.OnCompletionListener///////////////////////////////////////

    @Override
    public void onCompletion(MediaPlayer mp) {
        resetViews();
    }

    ////////////////////////////////////View.OnClickListener///////////////////////////////////////

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.video_view || v.getId() == R.id.fl_container) {
            showControlPanel();
        } else if (v.getId() == R.id.iv_control) {
            // 切换为暂停态
            if (mVideoView.isPlaying()) {
                mIvControl.setImageResource(R.drawable.ic_picture_picker_player_video_play);
                mVideoView.pause();
            }
            // 切换为播放态
            else {
                mIvControl.setImageResource(R.drawable.ic_picture_picker_player_video_pasue);
                mVideoView.start();
                updateProgress();
            }
        } else if (v.getId() == R.id.cl_control) {
            hindControlPanel();
        } else {
            // ignore.
        }
    }

    private void parseIntent() {
        mDataSource = getIntent().getParcelableExtra(EXTRA_MEDIA_META);
    }

    private void initViews() {
        // 设置外层窗体, 让其可响应事件
        mFlContainer = findViewById(R.id.fl_container);
        mFlContainer.setOnClickListener(this);
        // 配置 Video View
        mVideoView = findViewById(R.id.video_view);
        mVideoView.setOnPreparedListener(this);
        mVideoView.setOnCompletionListener(this);
        mVideoView.setOnErrorListener(this);
        mVideoView.setOnClickListener(this);
        // 配置 SeekBar
        mSeekBar = findViewById(R.id.seek_bar);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            int currentDuration;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                currentDuration = progress;
                mTvCurrent.setText(DateUtil.format(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mVideoView.seekTo(currentDuration);
            }
        });

        // 控制中心
        mClControl = findViewById(R.id.cl_control);
        mClControl.setOnClickListener(this);
        // 控制按钮
        mIvControl = findViewById(R.id.iv_control);
        mIvControl.setOnClickListener(this);
        // 播放进度
        mTvCurrent = findViewById(R.id.tv_current);
        mTvTotal = findViewById(R.id.tv_total);
    }

    private void initData() {
        mVideoView.setVideoPath(mDataSource.path);
        resetViews();
    }

    private void resetViews() {
        mHandler.removeMessages(MSG_WHAT_UPDATE_PROGRESS);
        mIvControl.setImageResource(R.drawable.ic_picture_picker_player_video_play);
        mTvCurrent.setText(DateUtil.format(0));
        mSeekBar.setProgress(0);
        showControlPanel();
    }

    private void updateProgress() {
        // 更新进度
        mTvCurrent.setText(DateUtil.format(mVideoView.getCurrentPosition()));
        mSeekBar.setProgress(mVideoView.getCurrentPosition());
        // 进行下一次更新
        if (mVideoView != null && mVideoView.isPlaying()) {
            mHandler.sendEmptyMessageDelayed(MSG_WHAT_UPDATE_PROGRESS, 1000);
        }
    }

    private void showControlPanel() {
        if (mPanelShowAnimator == null) {
            mPanelShowAnimator = ObjectAnimator.ofFloat(mClControl,
                    "alpha", 0f, 1f);
            mPanelShowAnimator.setDuration(200);
            mPanelShowAnimator.addListener(new AnimatorListenerAdapter() {

                @Override
                public void onAnimationStart(Animator animation) {
                    mClControl.setVisibility(View.VISIBLE);
                }

            });
        }
        if (mPanelShowAnimator.isRunning()) {
            return;
        }
        mPanelShowAnimator.start();
    }

    private void hindControlPanel() {
        if (mPanelHindAnimator == null) {
            mPanelHindAnimator = ObjectAnimator.ofFloat(mClControl,
                    "alpha", 1f, 0f);
            mPanelHindAnimator.setDuration(200);
            mPanelHindAnimator.addListener(new AnimatorListenerAdapter() {

                @Override
                public void onAnimationStart(Animator animation) {
                    mClControl.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    mClControl.setVisibility(View.INVISIBLE);
                }
            });
        }
        if (mPanelHindAnimator.isRunning()) {
            return;
        }
        mPanelHindAnimator.start();
    }

}
