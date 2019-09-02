//
// Created by Sharry Choo on 2019-06-22.
//

#include "JNICall.h"
#include "ConstDefine.h"

JNICall::JNICall(JavaVM *java_vm, jobject jmedia_player) {
    this->java_vm = java_vm;
    this->jmedia_player = jmedia_player;
    JNIEnv *jniEnv = getCurrentEnv();
    if (jniEnv != NULL) {
        this->jmedia_player = jniEnv->NewGlobalRef(jmedia_player);
        jclass jPlayClass = jniEnv->GetObjectClass(jmedia_player);
        // 状态变更回调
        jmid_on_prepared = jniEnv->GetMethodID(jPlayClass, "onPrepared", "(I)V");
        jmid_on_loading = jniEnv->GetMethodID(jPlayClass, "onLoading", "(Z)V");
        jmid_on_progress = jniEnv->GetMethodID(jPlayClass, "onProgress", "(II)V");
        jmid_on_completed = jniEnv->GetMethodID(jPlayClass, "onCompleted", "()V");
        // 渲染 yuv 数据回调
        jmid_on_render_yuv = jniEnv->GetMethodID(jPlayClass, "onRenderYUV", "(II[B[B[B)V");
        // 分贝大小回调
        jmid_on_volume_db_changed = jniEnv->GetMethodID(jPlayClass, "onVolumeDBChanged", "(I)V");
        // 错误回调
        jmid_on_error = jniEnv->GetMethodID(jPlayClass, "onError", "(ILjava/lang/String;)V");
    }
}

JNICall::~JNICall() {
    JNIEnv *jniEnv = getCurrentEnv();
    if (jniEnv != NULL) {
        jniEnv->DeleteGlobalRef(jmedia_player);
    } else {
        // 走到这里说明为 native 线程, 为其创建 JNIEnv
        if (java_vm->AttachCurrentThread(&jniEnv, 0) != JNI_OK) {
            return;
        }
        jniEnv->DeleteGlobalRef(jmedia_player);
        // 解除 native 线程与 java_vm 的关联
        java_vm->DetachCurrentThread();
    }
    LOGI("JNICall released");
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

/**
 * 回调到 java 层异步准备完成
 */
void JNICall::callOnPrepared(int total_duration) {
    JNIEnv *jniEnv = getCurrentEnv();
    if (jniEnv != NULL) {
        jniEnv->CallVoidMethod(jmedia_player, jmid_on_prepared, total_duration);
    } else {
        // 走到这里说明为 native 线程
        if (java_vm->AttachCurrentThread(&jniEnv, 0) != JNI_OK) {
            return;
        }
        jniEnv->CallVoidMethod(jmedia_player, jmid_on_prepared, total_duration);
        java_vm->DetachCurrentThread();
    }
}

void JNICall::callOnLoading(bool loading) {
    JNIEnv *jniEnv = getCurrentEnv();
    if (jniEnv != NULL) {
        jniEnv->CallVoidMethod(jmedia_player, jmid_on_loading, loading);
    } else {
        // 走到这里说明为 native 线程
        if (java_vm->AttachCurrentThread(&jniEnv, 0) != JNI_OK) {
            return;
        }
        jniEnv->CallVoidMethod(jmedia_player, jmid_on_loading, loading);
        java_vm->DetachCurrentThread();
    }
}

void JNICall::callOnProgress(int current, int duration) {
    JNIEnv *jniEnv = getCurrentEnv();
    if (jniEnv != NULL) {
        jniEnv->CallVoidMethod(jmedia_player, jmid_on_progress, current, duration);
    } else {
        // 走到这里说明为 native 线程
        if (java_vm->AttachCurrentThread(&jniEnv, 0) != JNI_OK) {
            return;
        }
        jniEnv->CallVoidMethod(jmedia_player, jmid_on_progress, current, duration);
        java_vm->DetachCurrentThread();
    }
}

void JNICall::callOnVolumeDBChanged(int db) {
    JNIEnv *jniEnv = getCurrentEnv();
    if (jniEnv != NULL) {
        jniEnv->CallVoidMethod(jmedia_player, jmid_on_volume_db_changed, db);
    } else {
        // 走到这里说明为 native 线程
        if (java_vm->AttachCurrentThread(&jniEnv, 0) != JNI_OK) {
            return;
        }
        jniEnv->CallVoidMethod(jmedia_player, jmid_on_volume_db_changed, db);
        java_vm->DetachCurrentThread();
    }
}

void JNICall::callOnCompleted() {
    JNIEnv *jniEnv = getCurrentEnv();
    if (jniEnv != NULL) {
        jniEnv->CallVoidMethod(jmedia_player, jmid_on_completed);
    } else {
        // 走到这里说明为 native 线程
        if (java_vm->AttachCurrentThread(&jniEnv, 0) != JNI_OK) {
            return;
        }
        jniEnv->CallVoidMethod(jmedia_player, jmid_on_completed);
        java_vm->DetachCurrentThread();
    }
}

void JNICall::callOnError(int code, const char *msg) {
    JNIEnv *jniEnv = getCurrentEnv();
    if (jniEnv != NULL) {
        jstring jMsg = jniEnv->NewStringUTF(msg);
        jniEnv->CallVoidMethod(jmedia_player, jmid_on_error, code, jMsg);
        jniEnv->DeleteLocalRef(jMsg);
    } else {
        // 走到这里说明为 native 线程, 为其创建 JNIEnv
        if (java_vm->AttachCurrentThread(&jniEnv, 0) != JNI_OK) {
            return;
        }
        jstring jMsg = jniEnv->NewStringUTF(msg);
        jniEnv->CallVoidMethod(jmedia_player, jmid_on_error, code, jMsg);
        // 移除 native 本地引用
        jniEnv->DeleteLocalRef(jMsg);
        // 解除 native 线程与 java_vm 的关联
        java_vm->DetachCurrentThread();
    }
}

void callOnRenderYUVInternal(JNIEnv *jniEnv, jobject obj, jmethodID method,
                             int width, int height, uint8_t *fy, uint8_t *fu, uint8_t *fv) {
    // 创建并填充 java 层的 y 数组
    int y_size = width * height;
    jbyteArray y = jniEnv->NewByteArray(y_size);
    jniEnv->SetByteArrayRegion(y, 0, y_size, reinterpret_cast<const jbyte *>(fy));

    // 创建并填充 java 层的 u 数组
    int uv_pre_size = y_size >> 2;
    jbyteArray u = jniEnv->NewByteArray(uv_pre_size);
    jniEnv->SetByteArrayRegion(u, 0, uv_pre_size, reinterpret_cast<const jbyte *>(fu));

    // 创建并填充 java 层的 v 数组
    jbyteArray v = jniEnv->NewByteArray(uv_pre_size);
    jniEnv->SetByteArrayRegion(v, 0, uv_pre_size, reinterpret_cast<const jbyte *>(fv));

    // 回调 Java 方法
    jniEnv->CallVoidMethod(obj, method, width, height, y, u, v);

    // 释放 native 层引用
    jniEnv->DeleteLocalRef(y);
    jniEnv->DeleteLocalRef(u);
    jniEnv->DeleteLocalRef(v);
}

void JNICall::callOnRenderYUV(int width, int height, uint8_t *fy, uint8_t *fu, uint8_t *fv) {
    JNIEnv *jniEnv = getCurrentEnv();
    if (jniEnv != NULL) {
        callOnRenderYUVInternal(jniEnv, jmedia_player, jmid_on_render_yuv, width, height, fy, fu,
                                fv);
    } else {
        // 走到这里说明为 native 线程
        if (java_vm->AttachCurrentThread(&jniEnv, 0) != JNI_OK) {
            return;
        }
        callOnRenderYUVInternal(jniEnv, jmedia_player, jmid_on_render_yuv, width, height, fy, fu,
                                fv);
        java_vm->DetachCurrentThread();
    }
}


