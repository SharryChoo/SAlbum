//
// Created by Sharry Choo on 2019-06-22.
//

#include "JNICall.h"
#include "ConstDefine.h"

////////////////////////////////////////////////////////////////////////////
// Construct and Destruct
////////////////////////////////////////////////////////////////////////////

JNICall::JNICall(JavaVM *java_vm, jobject jopensles_pcm_provider) {
    this->java_vm = java_vm;
    this->jopensles_pcm_provider = jopensles_pcm_provider;
    JNIEnv *jniEnv = NULL;
    EnvResult result = getJniEnv(&jniEnv);
    if (jniEnv) {
        this->jopensles_pcm_provider = jniEnv->NewGlobalRef(jopensles_pcm_provider);
        jclass jPlayClass = jniEnv->GetObjectClass(jopensles_pcm_provider);
        // 状态变更回调
        jmid_on_pcm_changed = jniEnv->GetMethodID(jPlayClass, "OnPCMChanged", "([B)V");
    }
    if (result == THREAD_ATTACH_TO_JVM) {
        detach();
    }
}

JNICall::~JNICall() {
    JNIEnv *jniEnv = NULL;
    EnvResult result = getJniEnv(&jniEnv);
    if (jniEnv != NULL) {
        jniEnv->DeleteGlobalRef(jopensles_pcm_provider);
    }
    if (result == THREAD_ATTACH_TO_JVM) {
        detach();
    }
}

////////////////////////////////////////////////////////////////////////////
// Open Method
////////////////////////////////////////////////////////////////////////////

void JNICall::callOnPCMChanged(uint8_t *pcm_data, int length) {
    JNIEnv *jniEnv = NULL;
    EnvResult result = getJniEnv(&jniEnv);
    if (jniEnv) {
        // 创建并填充 java 层的 y 数组
        jbyteArray jpcm_bytes = jniEnv->NewByteArray(length);
        jniEnv->SetByteArrayRegion(jpcm_bytes, 0, length,
                                   reinterpret_cast<const jbyte *>(pcm_data));

        // 回调 Java 方法
        jniEnv->CallVoidMethod(jopensles_pcm_provider, jmid_on_pcm_changed, jpcm_bytes, length);
        // 释放 native 层引用
        jniEnv->DeleteLocalRef(jpcm_bytes);
    }
    if (result == THREAD_ATTACH_TO_JVM) {
        detach();
    }
}

////////////////////////////////////////////////////////////////////////////
// Private Method
////////////////////////////////////////////////////////////////////////////

EnvResult JNICall::getJniEnv(JNIEnv **env) {
    // 尝试直接获取当前线程的 JNIEnv 指针
    if (JNI_OK != java_vm->GetEnv(reinterpret_cast<void **>(env), JNI_VERSION_1_6)) {
        // 将当前线程 Attach 到 JVM, 获取 JNIEnv 指针
        if (JNI_OK != java_vm->AttachCurrentThread(env, NULL)) {
            return ENV_RESULT_ERROR;
        }
        return THREAD_ATTACH_TO_JVM;
    }
    return ENV_RESULT_OK;
}

void JNICall::detach() {
    java_vm->DetachCurrentThread();
}


