//
// Created by Sharry on 2019-08-26.
//


#include <jni.h>
#include "ConstDefine.h"
#include "OpenSLRecorder.h"
#include "JNICall.h"

JavaVM *gJavaVM = NULL;

int registerNativeMethods(JNIEnv *env, jclass cls);

extern "C"
JNIEXPORT jint JNICALL
JNI_OnLoad(JavaVM *javaVM, void *reserverd) {
    // 通过初始化方法获取 JavaVM
    gJavaVM = javaVM;
    JNIEnv *env;
    if (javaVM->GetEnv(reinterpret_cast<void **>(&env), JNI_VERSION_1_6) != JNI_OK) {
        return -1;
    }
    // 注册 IMediaPlayer 函数
    jclass jclsMediaPlayer = env->FindClass(OPENSLES_PCM_PROVIDER_CLASS_NAME);
    jclsMediaPlayer = reinterpret_cast<jclass>(env->NewGlobalRef(jclsMediaPlayer));
    if (!jclsMediaPlayer) {
        LOGE("Fail to create global reference for %s", OPENSLES_PCM_PROVIDER_CLASS_NAME);
    }
    int res = registerNativeMethods(env, jclsMediaPlayer);
    if (res != 0) {
        LOGE("Failed to register native methods for class %s ", OPENSLES_PCM_PROVIDER_CLASS_NAME);
    }
    env->DeleteGlobalRef(jclsMediaPlayer);
    return JNI_VERSION_1_6;
}

namespace openslesprovider {

    OpenSLRecorder *pRecorder = NULL;
    JNICall *pJniCall = NULL;

    void nativeStart(JNIEnv *, jobject jobj) {
        if (pRecorder == NULL) {
            pJniCall = new JNICall(gJavaVM, jobj);
            pRecorder = new OpenSLRecorder(pJniCall);
            pRecorder->start();
        } else {
            LOGI("Please stop first");
        }
    }

    void nativePause(JNIEnv *, jobject) {
        if (pRecorder != NULL) {
            pRecorder->pause();
        }
    }

    void nativeResume(JNIEnv *, jobject) {
        if (pRecorder != NULL) {
            pRecorder->resume();
        }
    }

    void nativeStop(JNIEnv *, jobject) {
        if (pRecorder != NULL) {
            pRecorder->stop();
        }
        if (pJniCall != NULL) {
            delete pJniCall;
            pJniCall = NULL;
        }
        if (pRecorder != NULL) {
            delete pRecorder;
            pRecorder = NULL;
        }
        LOGI("OpenSL ES recorder stopped.");
    }

}

JNINativeMethod gBridgeMethods[] = {
        {"nativeStart",  "()V", (void *) openslesprovider::nativeStart},
        {"nativePause",  "()V", (void *) openslesprovider::nativePause},
        {"nativeResume", "()V", (void *) openslesprovider::nativeResume},
        {"nativeStop",   "()V", (void *) openslesprovider::nativeStop}
};

int registerNativeMethods(JNIEnv *env, jclass cls) {
    return env->RegisterNatives(cls, gBridgeMethods,
                                sizeof(gBridgeMethods) / sizeof(gBridgeMethods[0]));
}
