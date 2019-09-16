//
// Created by Sharry Choo on 2019-06-17.
//
#ifndef SMEDIA_RECORDER_CONSTDEFINE_H
#define SMEDIA_RECORDER_CONSTDEFINE_H

#include <android/log.h>

/**
 * 日志相关
 */
#define TAG "SMedia-Recorder"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN, TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)


/**
 * 用于和 Java 层回调的类
 */
#define OPENSLES_PCM_PROVIDER_CLASS_NAME "com/sharry/lib/media/recorder/OpenSLESPCMProvider"

#define RECORD_BUFFER_SIZE 4096

#endif //SMEDIA_RECORDER_CONSTDEFINE_H
