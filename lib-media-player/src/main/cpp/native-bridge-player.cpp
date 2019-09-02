#include <jni.h>
#include <string>
#include "JNICall.h"
#include "SFFmpeg.h"
#include "ConstDefine.h"

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
    jclass jclsMediaPlayer = env->FindClass(SMEDIA_PLAYER_CLASS_NAME);
    jclsMediaPlayer = reinterpret_cast<jclass>(env->NewGlobalRef(jclsMediaPlayer));
    if (!jclsMediaPlayer) {
        LOGE("Fail to create global reference for %s", SMEDIA_PLAYER_CLASS_NAME);
    }
    int res = registerNativeMethods(env, jclsMediaPlayer);
    if (res != 0) {
        LOGE("Failed to register native methods for class %s ", SMEDIA_PLAYER_CLASS_NAME);
    }
    env->DeleteGlobalRef(jclsMediaPlayer);
    return JNI_VERSION_1_6;
}

namespace smediaplayer {

    SFFmpeg *gFFmpeg = NULL;
    JNICall *gJniCall = NULL;

    void nativePrepare(JNIEnv *env, jobject instance, jstring uri_) {
        if (gFFmpeg != NULL) {
            LOGE("Please stop it first.");
            return;
        }
        // 播放音频
        const char *uri = env->GetStringUTFChars(uri_, 0);
        gJniCall = new JNICall(gJavaVM, instance);
        gFFmpeg = new SFFmpeg(gJniCall, uri);
        gFFmpeg->prepare();
        env->ReleaseStringUTFChars(uri_, uri);
    }

    void nativePlay(JNIEnv *, jobject) {
        // 播放音频
        if (gFFmpeg != NULL) {
            gFFmpeg->play();
        }
    }

    void nativeSeek(JNIEnv *, jobject, jint duration) {
        // 播放音频
        if (gFFmpeg != NULL) {
            gFFmpeg->seek(duration);
        }
    }

    void nativePause(JNIEnv *, jobject) {
        if (gFFmpeg != NULL) {
            gFFmpeg->pause();
        }
    }

    void nativeResume(JNIEnv *, jobject) {
        if (gFFmpeg != NULL) {
            gFFmpeg->resume();
        }
    }

    void nativeSetVolume(JNIEnv *, jobject, jint percent) {
        if (gFFmpeg != NULL) {
            gFFmpeg->setVolume(percent);
        }
    }

    void nativeChannelLayout(JNIEnv *, jobject, jint channelLayout) {
        if (gFFmpeg != NULL) {
            gFFmpeg->setChannelLayout(channelLayout);
        }
    }

    void nativeSetPitch(JNIEnv *, jobject, jfloat pitch) {
        if (gFFmpeg != NULL) {
            gFFmpeg->setPitch(pitch);
        }
    }

    void nativeSetTempo(JNIEnv *, jobject, jfloat tempo) {
        if (gFFmpeg != NULL) {
            gFFmpeg->setTempo(tempo);
        }
    }

    void nativeStop(JNIEnv *, jobject) {
        if (gFFmpeg != NULL) {
            gFFmpeg->stop();
            delete (gFFmpeg);
            gFFmpeg = NULL;
        }
        if (gJniCall != NULL) {
            delete (gJniCall);
            gJniCall = NULL;
        }
    }

}

JNINativeMethod gBridgeMethods[] = {
        {"nativePrepare",  "(Ljava/lang/String;)V", (void *) smediaplayer::nativePrepare},
        {"nativePlay",          "()V",                   (void *) smediaplayer::nativePlay},
        {"nativeSeek",          "(I)V",                  (void *) smediaplayer::nativeSeek},
        {"nativePause",         "()V",                   (void *) smediaplayer::nativePause},
        {"nativeResume",        "()V",                   (void *) smediaplayer::nativeResume},
        {"nativeStop",          "()V",                   (void *) smediaplayer::nativeStop},
        {"nativeSetVolume",     "(I)V",                  (void *) smediaplayer::nativeSetVolume},
        {"nativeChannelLayout", "(I)V",                  (void *) smediaplayer::nativeChannelLayout},
        {"nativeSetPitch",      "(F)V",                  (void *) smediaplayer::nativeSetPitch},
        {"nativeSetTempo",      "(F)V",                  (void *) smediaplayer::nativeSetTempo},
};

int registerNativeMethods(JNIEnv *env, jclass cls) {
    return env->RegisterNatives(cls, gBridgeMethods,
                                sizeof(gBridgeMethods) / sizeof(gBridgeMethods[0]));
}


