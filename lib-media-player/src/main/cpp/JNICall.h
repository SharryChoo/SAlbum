//
// Created by Sharry Choo on 2019-06-22.
//

#ifndef SMEDIA_PLAYER_JNICALL_H
#define SMEDIA_PLAYER_JNICALL_H

#include <jni.h>

class JNICall {

public:
    JavaVM *java_vm;
    jobject jmedia_player;
    jmethodID jmid_on_prepared;
    jmethodID jmid_on_loading;
    jmethodID jmid_on_progress;
    jmethodID jmid_on_volume_db_changed;
    jmethodID jmid_on_render_yuv;
    jmethodID jmid_on_completed;
    jmethodID jmid_on_error;

    JNICall(JavaVM *java_vm, jobject jmedia_player);

    ~JNICall();

    /**
     * 创建当前线程的 JNI env
     *
     * @return 当前线程的 JNIEnv, 若是在 native 层创建的线程, 并且没有 javaVM->AttachCurrentThread() 则返回 NULL
     */
    JNIEnv *getCurrentEnv();

    /**
     * 回调异步准备完毕
     */
    void callOnPrepared(int total_duration);

    /**
     * 回调正在加载中
     */
    void callOnLoading(bool loading);

    /**
     * 回调进度
     */
    void callOnProgress(int current, int duration);

    /**
     * 回调进度
     */
    void callOnVolumeDBChanged(int db);

    /**
    * 回调播放完成
    */
    void callOnCompleted();

    /**
     * 回调错误异常
     */
    void callOnError(int code, const char *msg);

    /**
     * 回调视频数据的渲染
     */
    void callOnRenderYUV(int width, int height, uint8_t *fy, uint8_t *fu, uint8_t *fv);

};


#endif //SMEDIA_PLAYER_JNICALL_H
