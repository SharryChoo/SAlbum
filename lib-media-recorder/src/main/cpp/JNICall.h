//
// Created by Sharry Choo on 2019-06-22.
//

#ifndef SMEDIA_RECORDER_JNICALL_H
#define SMEDIA_RECORDER_JNICALL_H

#include <jni.h>

typedef int EnvResult;
#define ENV_RESULT_OK 0
#define ENV_RESULT_ERROR -106
#define THREAD_ATTACH_TO_JVM 1

class JNICall {

public:
    JavaVM *java_vm;
    jobject jopensles_pcm_provider;
    jmethodID jmid_on_pcm_changed;

    JNICall(JavaVM *java_vm, jobject jopensles_pcm_provider);

    ~JNICall();

    /**
     * 回调异步准备完毕
     */
    void callOnPCMChanged(uint8_t *pcm_data, int length);

private:

    /**
     * 获取当前线程的 JNIEnv 对象
     *
     * @param env 传出参数, 内部会进行赋值操作
     * @return if is AV_ATTACH_TO_VM, need invoke detach.
     */
    EnvResult getJniEnv(JNIEnv **env);

    /**
     * 解绑当前线程与 JVM 的关联
     */
    void detach();

};


#endif //SMEDIA_RECORDER_JNICALL_H
