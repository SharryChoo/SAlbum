package com.sharry.lib.media.recorder;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

/**
 * 系统的音频录制引擎
 *
 * @author Sharry <a href="xiaoyu.zhu@1hai.cn">Contact me.</a>
 * @version 1.0
 * @since 2019-08-26 15:34
 */
public class DefaultPCMProvider implements IPCMProvider, Runnable {

    private final AudioRecord mImpl;
    private final int mMinBufferSize;
    private final Object mLockPause = new Object();

    private OnPCMChangedListener mListener;
    private Thread mRecordThread;
    private volatile boolean isStart;
    private volatile boolean isPause;

    DefaultPCMProvider() {
        // 获取最小录音缓冲区大小
        this.mMinBufferSize = AudioRecord.getMinBufferSize(
                44100,
                AudioFormat.CHANNEL_IN_STEREO,
                AudioFormat.ENCODING_PCM_16BIT
        );
        // 构建实体对象
        this.mImpl = new AudioRecord(
                MediaRecorder.AudioSource.MIC,    // 采样的输入设备
                44100,             // 采样率
                AudioFormat.CHANNEL_IN_STEREO,    // 渠道设置(双单声道录制)
                AudioFormat.ENCODING_PCM_16BIT,   // 输出的格式, 输出的源数据均为 PCM
                mMinBufferSize                    // 数据缓冲的大小
        );
    }

    @Override
    public void start() {
        isStart = true;
        mRecordThread = new Thread(this);
        mRecordThread.start();
    }

    @Override
    public void pause() {
        isPause = true;
    }

    @Override
    public void resume() {
        isPause = false;
        synchronized (mLockPause) {
            mLockPause.notify();
        }
    }

    @Override
    public void stop() {
        isStart = false;
        isPause = false;
        synchronized (mLockPause) {
            mLockPause.notify();
        }
        if (mRecordThread != null) {
            try {
                mRecordThread.join(1000);
            } catch (InterruptedException e) {
                // ignore.
            }
        }
        mImpl.stop();
        mImpl.release();
    }

    @Override
    public void setOnPCMChangedListener(OnPCMChangedListener listener) {
        this.mListener = listener;
    }

    @Override
    public void run() {
        mImpl.startRecording();
        byte[] pcmData = new byte[mMinBufferSize];
        while (isStart) {
            if (isPause) {
                synchronized (mLockPause) {
                    try {
                        mLockPause.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                continue;
            }
            // 获取录制的音频流
            mImpl.read(pcmData, 0, mMinBufferSize);
            if (mListener != null) {
                mListener.OnPCMChanged(pcmData);
            }
        }
    }

}
