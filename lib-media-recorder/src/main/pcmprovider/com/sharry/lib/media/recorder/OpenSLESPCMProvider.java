package com.sharry.lib.media.recorder;

/**
 * 使用 OpenSL ES 实现的音频录制引擎
 *
 * @author Sharry <a href="xiaoyu.zhu@1hai.cn">Contact me.</a>
 * @version 1.0
 * @since 2019-08-26 15:34
 */
public class OpenSLESPCMProvider implements IPCMProvider, IPCMProvider.OnPCMChangedListener {

    static {
        System.loadLibrary("smedia-recorder");
    }

    private OnPCMChangedListener listener;

    @Override
    public void start() {
        nativeStart();
    }

    @Override
    public void pause() {
        nativePause();
    }

    @Override
    public void resume() {
        nativeResume();
    }

    @Override
    public void stop() {
        nativeStop();
        // 防止内存泄漏
        listener = null;
    }

    @Override
    public void OnPCMChanged(byte[] pcmData) {
        if (listener != null) {
            listener.OnPCMChanged(pcmData);
        }
    }

    @Override
    public void setOnPCMChangedListener(OnPCMChangedListener listener) {
        this.listener = listener;
    }

    // native method.
    private native void nativeStart();

    private native void nativePause();

    private native void nativeResume();

    private native void nativeStop();

}
