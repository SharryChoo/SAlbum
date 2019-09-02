//
// Created by Sharry Choo on 2019-06-17.
//
#ifndef SMEDIA_PLAYER_CONSTDEFINE_H
#define SMEDIA_PLAYER_CONSTDEFINE_H

#include <android/log.h>

/**
 * 日志相关
 */
#define TAG "SMedia-Player"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN, TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

/**
 * 用于和 Java 层回调的类
 */
#define SMEDIA_PLAYER_CLASS_NAME "com/sharry/lib/media/player/SMediaPlayer"

/**
 * 音频重采样的数据
 */
#define SAUDIO_RESAMPLE_SAMPLE_RATE 44100                                     // 每一秒的采样率
#define SAUDIO_RESAMPLE_CHANNEL_LAYOUT AV_CH_LAYOUT_STEREO                    // 声道布局
#define SAUDIO_RESAMPLE_PER_SAMPLE_FORMAT  AVSampleFormat::AV_SAMPLE_FMT_S16  // 每个采样点的深度 (2byte)

/**
 * 音频声道控制
 */
#define SAUDIO_CHANNEL_LAYOUT_RIGHT 0
#define SAUDIO_CHANNEL_LAYOUT_LEFT 1
#define SAUDIO_CHANNEL_LAYOUT_STEREO 2

/**
 * 队列的最大缓存数量
 */
#define MAX_QUEUE_CACHE_SIZE  100

#endif //SMEDIA_PLAYER_CONSTDEFINE_H
