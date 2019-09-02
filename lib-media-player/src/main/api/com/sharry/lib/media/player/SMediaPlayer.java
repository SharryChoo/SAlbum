package com.sharry.lib.media.player;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 基于 FFmpeg 的音视频编解码
 *
 * @author Sharry <a href="xiaoyu.zhu@1hai.cn">Contact me.</a>
 * @version 1.0
 * @since 2019-07-11 14:31
 */
public final class SMediaPlayer implements IMediaPlayer,
        OnStatusChangedListener,
        OnRenderYUVListener,
        OnVolumeDBListener,
        OnErrorListener {

    static {
        System.loadLibrary("smedia-player");
    }

    private static final String TAG = SMediaPlayer.class.getSimpleName();
    private static final int DEFAULT_VOLUME_PERCENT = 50;

    /**
     * 开一个单独的线程, 用于处理可能会耗时的操作
     */
    private static final ThreadPoolExecutor sMediaControlExecutor = new ThreadPoolExecutor(
            1, 1, 30,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>()
    );

    /**
     * 全局可用播放器当前音量大小
     */
    private static int sVolumePercent = DEFAULT_VOLUME_PERCENT;

    /**
     * 全局可用, 控制声道数
     */
    private static int sChannelLayout = CHANNEL_LAYOUT_STEREO;

    /**
     * 方便在主线程回调接口
     */
    private final Handler mMainThreadHandler = new Handler(Looper.getMainLooper());

    /**
     * AbsMedia player 的监听器
     */
    private OnStatusChangedListener mStatusChangedListener;
    private OnErrorListener mErrorListener;
    private OnVolumeDBListener mVolumeDBListener;
    private OnRenderYUVListener mRenderYUVListener;

    /**
     * 数据源
     */
    private String mDataSource;

    /**
     * 当前媒体播放的时长
     */
    private int mCurDuration = 0;

    /**
     * 当前媒体的总长度
     */
    private int mTotalDuration = 0;

    /**
     * 播放声调
     */
    private float mPitch = 1.0f;

    /**
     * 播放速度
     */
    private float mTempo = 1.0f;

    /**
     * 判断是否正在切换下一曲
     */
    private volatile boolean isOnPlayingNext = false;


    @Override
    public void setDataSource(@NonNull String dataSource) {
        this.mDataSource = dataSource;
    }

    @Override
    public void prepare() {
        if (TextUtils.isEmpty(mDataSource)) {
            throw new NullPointerException("Please ensure u set data source first.");
        }
        sMediaControlExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativePrepare(mDataSource);
            }
        });
    }

    @Override
    public void play() {
        nativePlay();
        nativeSetVolume(sVolumePercent);
        nativeChannelLayout(sChannelLayout);
        nativeSetPitch(mPitch);
        nativeSetTempo(mTempo);
    }

    @Override
    public void seek(final int second) {
        sMediaControlExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeSeek(second);
            }
        });
    }

    @Override
    public void pause() {
        nativePause();
        onPaused();
    }

    @Override
    public void resume() {
        nativeResume();
        onResumed();
    }

    @Override
    public void stop() {
        mCurDuration = 0;
        mTotalDuration = 0;
        sMediaControlExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeStop();
                onStopped();
                if (isOnPlayingNext) {
                    nativePrepare(mDataSource);
                }
            }
        });
    }

    @Override
    public void next(@NonNull String nextDataSource) {
        // 更新下一个数据源
        setDataSource(nextDataSource);
        if (!isOnPlayingNext) {
            isOnPlayingNext = true;
            stop();
        }
    }

    @Override
    public void setVolume(@IntRange(from = 0, to = 100) int percent) {
        if (percent >= 0 && percent <= 100) {
            sVolumePercent = percent;
            nativeSetVolume(percent);
        }
    }

    @Override
    public void setChannelLayout(@ChannelLayout int channelLayout) {
        sChannelLayout = channelLayout;
        nativeChannelLayout(channelLayout);
    }

    @Override
    public void setPitch(float pitch) {
        mPitch = pitch;
        nativeSetPitch(pitch);
    }

    @Override
    public void setTempo(float tempo) {
        mTempo = tempo;
        nativeSetTempo(tempo);
    }

    @Override
    public void setOnStatusChangedListener(@Nullable OnStatusChangedListener statusChangedListener) {
        this.mStatusChangedListener = statusChangedListener;
    }

    @Override
    public void setOnVolumeDBListener(@Nullable OnVolumeDBListener volumeDBListener) {
        this.mVolumeDBListener = volumeDBListener;
    }

    @Override
    public void setOnErrorListener(@Nullable OnErrorListener errorListener) {
        this.mErrorListener = errorListener;
    }

    @Override
    public void setOnRenderYUVListener(@Nullable OnRenderYUVListener renderYUVListener) {
        this.mRenderYUVListener = renderYUVListener;
    }

    @Override
    public int getVolumePercent() {
        return sVolumePercent;
    }

    @Override
    public int getChannelLayout() {
        return sChannelLayout;
    }

    @Override
    public int getCurDuration() {
        return mCurDuration;
    }

    @Override
    public int getTotalDuration() {
        return mTotalDuration;
    }

    ///////////////////////////// Invoke from native /////////////////////////////

    /**
     * Invoke from JNI
     */
    @Override
    public void onPrepared(final int totalDuration) {
        mTotalDuration = totalDuration;
        // 播放下一首状态结束
        isOnPlayingNext = false;
        if (mStatusChangedListener != null) {
            mMainThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    mStatusChangedListener.onPrepared(mTotalDuration);
                }
            });
        }
    }

    /**
     * Invoke from JNI
     */
    @Override
    public void onProgress(final int current, final int total) {
        this.mCurDuration = current;
        this.mTotalDuration = total;
        if (mStatusChangedListener != null) {
            mMainThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    mStatusChangedListener.onProgress(current, total);
                }
            });
        }
    }

    /**
     * Invoke from JNI
     */
    @Override
    public void onLoading(final boolean isLoading) {
        if (mStatusChangedListener != null) {
            mMainThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    mStatusChangedListener.onLoading(isLoading);
                }
            });
        }
    }

    @Override
    public void onPaused() {
        if (mStatusChangedListener != null) {
            mStatusChangedListener.onPaused();
        }
    }

    @Override
    public void onResumed() {
        if (mStatusChangedListener != null) {
            mStatusChangedListener.onResumed();
        }
    }

    /**
     * Invoke from JNI
     */
    @Override
    public void onCompleted() {
        nativeStop();
        if (mStatusChangedListener != null) {
            mMainThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    mStatusChangedListener.onCompleted();
                }
            });
        }
    }

    @Override
    public void onStopped() {
        if (mStatusChangedListener != null) {
            mMainThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    mStatusChangedListener.onStopped();
                }
            });
        }
    }

    /**
     * Invoke from JNI
     */
    @Override
    public void onVolumeDBChanged(final int db) {
        if (mVolumeDBListener != null) {
            mMainThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    mVolumeDBListener.onVolumeDBChanged(db);
                }
            });
        }
    }

    /**
     * Invoke from JNI
     */
    @Override
    public void onRenderYUV(int width, int height, byte[] y, byte[] u, byte[] v) {
        if (mRenderYUVListener != null) {
            mRenderYUVListener.onRenderYUV(width, height, y, u, v);
        }
    }

    /**
     * Invoke from JNI
     */
    @Override
    public void onError(final int errorCode, final String errorMsg) {
        Log.e(TAG, "Occurred a error, error code is: " + errorCode + ", error msg is: " + errorMsg);
        stop();
        // 播放下一首状态结束
        isOnPlayingNext = false;
        if (mErrorListener != null) {
            mMainThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    mErrorListener.onError(errorCode, errorMsg);
                }
            });
        }
    }

    ///////////////////////////// Native method /////////////////////////////

    private native void nativePrepare(String uri);

    private native void nativePlay();

    private native void nativeSeek(int duration);

    private native void nativePause();

    private native void nativeResume();

    private native void nativeStop();

    private native void nativeSetVolume(int percent);

    private native void nativeChannelLayout(int channelLayout);

    private native void nativeSetPitch(float channelLayout);

    private native void nativeSetTempo(float channelLayout);

}
