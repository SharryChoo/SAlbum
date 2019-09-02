package com.sharry.lib.media.recorder;

import androidx.annotation.WorkerThread;

/**
 * 音频 PCM 数据源的提供者
 *
 * @author Sharry <a href="xiaoyu.zhu@1hai.cn">Contact me.</a>
 * @version 1.0
 * @since 2019-08-26 16:19
 */
public interface IPCMProvider {

    void start();

    void pause();

    void resume();

    void stop();

    void setOnPCMChangedListener(OnPCMChangedListener listener);

    interface OnPCMChangedListener {

        @WorkerThread
        void OnPCMChanged(byte[] pcmData);

    }

}
