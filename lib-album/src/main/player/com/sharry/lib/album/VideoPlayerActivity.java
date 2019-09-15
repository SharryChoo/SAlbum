package com.sharry.lib.album;

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
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSeekBar;
import androidx.constraintlayout.widget.ConstraintLayout;

/**
 * 视频播放的 Activity
 *
 * @author Sharry <a href="sharrychoochn@gmail.com">Contact me.</a>
 * @version 1.0
 * @since 2019-09-04 16:33
 */
public class VideoPlayerActivity extends AppCompatActivity implements MediaPlayer.OnPreparedListener,
        MediaPlayer.OnCompletionListener,
        MediaPlayer.OnErrorListener,
        View.OnClickListener {

    private static final String EXTRA_MEDIA_META = "extra_media_meta";

    public static void launch(Context context, MediaMeta mediaMeta) {
        Intent intent = new Intent(context, VideoPlayerActivity.class);
        intent.putExtra(EXTRA_MEDIA_META, mediaMeta);
        context.startActivity(intent);
        if (context instanceof Activity) {
            ((Activity) context).overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }
    }

    private static final String TAG = VideoPlayerActivity.class.getSimpleName();
    private static final int MSG_WHAT_UPDATE_PROGRESS = 0;
    private static final int MAXIMUM_TRY_AGAIN_THRESHOLD = 3;

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

    /**
     * 播放数据源
     */
    private MediaMeta mDataSource;

    /**
     * Widgets.
     */
    private VideoView mVideoView;
    private TextView mTvCurrent;
    private AppCompatSeekBar mSeekBar;
    private TextView mTvTotal;
    private ConstraintLayout mClControl;
    private ImageView mIvControl;
    private ObjectAnimator mPanelHindAnimator;
    private ObjectAnimator mPanelShowAnimator;

    /**
     * 条件控制变量
     */
    private boolean mIsPrepared = false;
    private boolean mIsPaused = false;
    private int mCountTryAgain = 0;
    private int mCurrentDuration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lib_album_activity_video_player);
        parseIntent();
        initViews();
        prepare();
    }

    @Override
    protected void onResume() {
        super.onResume();
        showControlPanel();
    }

    @Override
    protected void onPause() {
        super.onPause();
        pause();
    }

    @Override
    public void onBackPressed() {
        stop();
        super.onBackPressed();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    ////////////////////////////////////MediaPlayer.OnCompletionListener///////////////////////////////////////

    /**
     * Callback when SetVideoPath / onResume
     */
    @Override
    public void onPrepared(MediaPlayer mp) {
        // 标记为准备完成
        mIsPrepared = true;
        // 清空重试次数
        mCountTryAgain = 0;
        // 为 View 注入数据
        mTvTotal.setText(DateUtil.format(mp.getDuration()));
        mSeekBar.setMax(mp.getDuration());
        // 若之前是暂停, 则显示控制面板
        if (mIsPaused) {
            showControlPanel();
        }
        // 若之前非暂停, 则直接播放
        else {
            play();
        }
    }

    ////////////////////////////////////MediaPlayer.OnErrorListener///////////////////////////////////////

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        if (mCountTryAgain++ < MAXIMUM_TRY_AGAIN_THRESHOLD) {
            Log.w(TAG, "Occurred an error, try again " + mCountTryAgain + " time");
            prepare();
            return true;
        } else {
            // 重置视图
            reset();
            // 标记为准备失败
            mIsPrepared = false;
            return false;
        }
    }

    ////////////////////////////////////MediaPlayer.OnCompletionListener///////////////////////////////////////

    @Override
    public void onCompletion(MediaPlayer mp) {
        reset();
        mIsPrepared = false;
    }

    ////////////////////////////////////View.OnClickListener///////////////////////////////////////

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.video_view || v.getId() == R.id.fl_container) {
            showControlPanel();
        } else if (v.getId() == R.id.cl_control) {
            hindControlPanel();
        } else if (v.getId() == R.id.iv_control) {
            if (mIsPrepared) {
                if (mVideoView.isPlaying()) {
                    pause();
                } else {
                    play();
                }
            } else {
                prepare();
            }
        } else {
            // ignore.
        }
    }

    //////////////////////////////////// Private methods ///////////////////////////////////////

    private void parseIntent() {
        mDataSource = getIntent().getParcelableExtra(EXTRA_MEDIA_META);
    }

    private void initViews() {
        // 设置外层窗体, 让其可响应事件
        findViewById(R.id.fl_container).setOnClickListener(this);
        // 配置 Video View
        mVideoView = findViewById(R.id.video_view);
        mVideoView.setOnPreparedListener(this);
        mVideoView.setOnCompletionListener(this);
        mVideoView.setOnErrorListener(this);
        mVideoView.setOnClickListener(this);
        // 配置 SeekBar
        mSeekBar = findViewById(R.id.seek_bar);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mCurrentDuration = progress;
                mTvCurrent.setText(DateUtil.format(mCurrentDuration));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mVideoView.seekTo(mCurrentDuration);
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

    //////////////////////////////////// VideoPlay Control ///////////////////////////////////////

    private void prepare() {
        mVideoView.setVideoPath(mDataSource.path);
        reset();
    }

    private void play() {
        mIvControl.setImageResource(R.drawable.ic_album_player_video_pasue);
        mVideoView.start();
        mVideoView.seekTo(mCurrentDuration);
        updateProgress();
        mIsPaused = false;
    }

    private void pause() {
        mIvControl.setImageResource(R.drawable.ic_album_player_video_play);
        mVideoView.pause();
        mHandler.removeMessages(MSG_WHAT_UPDATE_PROGRESS);
        mIsPaused = true;
    }

    private void stop() {
        mVideoView.stopPlayback();
    }

    private void reset() {
        mHandler.removeMessages(MSG_WHAT_UPDATE_PROGRESS);
        mIvControl.setImageResource(R.drawable.ic_album_player_video_play);
        mTvCurrent.setText(DateUtil.format(0));
        mSeekBar.setProgress(0);
        showControlPanel();
    }

}
