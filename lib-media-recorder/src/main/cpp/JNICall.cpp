//
// Created by Sharry Choo on 2019-06-22.
//

#include "JNICall.h"
#include "ConstDefine.h"

JNICall::JNICall(JavaVM *java_vm, jobject jopensles_pcm_provider) {
    this->java_vm = java_vm;
    this->jopensles_pcm_provider = jopensles_pcm_provider;
    JNIEnv *jniEnv = getCurrentEnv();
    if (jniEnv != NULL) {
        this->jopensles_pcm_provider = jniEnv->NewGlobalRef(jopensles_pcm_provider);
        jclass jPlayClass = jniEnv->GetObjectClass(jopensles_pcm_provider);
        // 状态变更回调
        jmid_on_pcm_changed = jniEnv->GetMethodID(jPlayClass, "OnPCMChanged", "([B)V");
    }
}

JNICall::~JNICall() {
    JNIEnv *jniEnv = getCurrentEnv();
    if (jniEnv != NULL) {
        jniEnv->DeleteGlobalRef(jopensles_pcm_provider);
    } else {
        // 走到这里说明为 native 线程, 为其创建 JNIEnv
        if (java_vm->AttachCurrentThread(&jniEnv, 0) != JNI_OK) {
            return;
        }
        jniEnv->DeleteGlobalRef(jopensles_pcm_provider);
        // 解除 native 线程与 java_vm 的关联
        java_vm->DetachCurrentThread();
    }
}

JNIEnv *JNICall::getCurrentEnv() {
    JNIEnv *env = NULL;
    int ret = java_vm->GetEnv((void **) &env, JNI_VERSION_1_6);
    if (ret != JNI_OK) {
        // This is may be native thread and not attach to java vm.
        return NULL;
    }
    return env;
}

void callOnPCMChangedInternal(JNIEnv *jniEnv, jobject obj, jmethodID method, uint8_t *pcm_data,
                              int length) {
    // 创建并填充 java 层的 y 数组
    jbyteArray jpcm_bytes = jniEnv->NewByteArray(length);
    jniEnv->SetByteArrayRegion(jpcm_bytes, 0, length, reinterpret_cast<const jbyte *>(pcm_data));

    // 回调 Java 方法
    jniEnv->CallVoidMethod(obj, method, jpcm_bytes, length);

    // 释放 native 层引用
    jniEnv->DeleteLocalRef(jpcm_bytes);
}

void JNICall::callOnPCMChanged(uint8_t *pcm_data, int length) {
    JNIEnv *jniEnv = getCurrentEnv();
    if (jniEnv != NULL) {
        callOnPCMChangedInternal(jniEnv, jopensles_pcm_provider, jmid_on_pcm_changed, pcm_data,
                                 length);
    } else {
        // 走到这里说明为 native 线程
        if (java_vm->AttachCurrentThread(&jniEnv, 0) != JNI_OK) {
            return;
        }
        callOnPCMChangedInternal(jniEnv, jopensles_pcm_provider, jmid_on_pcm_changed, pcm_data,
                                 length);
        java_vm->DetachCurrentThread();
    }
}


