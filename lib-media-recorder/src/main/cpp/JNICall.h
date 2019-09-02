//
// Created by Sharry Choo on 2019-06-22.
//

#ifndef SMEDIA_RECORDER_JNICALL_H
#define SMEDIA_RECORDER_JNICALL_H

#include <jni.h>

class JNICall {

public:
    JavaVM *java_vm;
    jobject jopensles_pcm_provider;
    jmethodID jmid_on_pcm_changed;

    JNICall(JavaVM *java_vm, jobject jopensles_pcm_provider);

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
    void callOnPCMChanged(uint8_t *pcm_data, int length);

};


#endif //SMEDIA_RECORDER_JNICALL_H
