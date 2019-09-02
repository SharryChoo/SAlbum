//
// Created by Sharry Choo on 2019-06-22.
//

#ifndef SMEDIA_PLAYER_SFFMPEG_H
#define SMEDIA_PLAYER_SFFMPEG_H

#include "JNICall.h"
#include "SAudioPlayer.h"
#include "SVideoPlayer.h"

extern "C" {
#include "libavformat/avformat.h"
#include "libswscale/swscale.h"
#include "libswresample/swresample.h"
#include "libavutil/imgutils.h"
#include "libavutil/time.h"
}

class SFFmpeg {

public:
    // 构造参数
    JNICall *jni_call = NULL;
    char *uri = NULL;
    MediaStatus *status = NULL;

    SFFmpeg(JNICall *jni_call, const char *uri);

    ~SFFmpeg();

public:
    /**
     * FFmpeg 格式上下文
     */
    AVFormatContext *format_ctx = NULL;
    pthread_mutex_t mutex_format_ctx;

    /**
     * 音视频播放的实现者
     */
    SAudioPlayer *audio = NULL;
    SVideoPlayer *video = NULL;

    /**
     * 解码线程
     */
    pthread_t thread_ffmpeg_decode = NULL;

    /**
     * 控制暂停的条件
     */
    pthread_cond_t cond_pause;

    /**
     * 暂停条件控制的锁
     */
    pthread_mutex_t mutex_cond_pause;

    void prepare();

    void play();

    void seek(int seconds);

    void pause();

    void resume();

    void stop();

    void setVolume(int percent);

    void setChannelLayout(int channel_layout);

    void setPitch(float pitch);

    void setTempo(float tempo);

    void release();

    void decode();

};

#endif //SMEDIA_PLAYER_SFFMPEG_H
