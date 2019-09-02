//
// Created by Sharry Choo on 2019-07-08.
//
// 音视频抽象媒体类
//

#ifndef SMEDIA_PLAYER_ABSMEDIA_H
#define SMEDIA_PLAYER_ABSMEDIA_H

#include "JNICall.h"
#include "MediaPacketQueue.h"
#include "MediaStatus.h"

extern "C" {
#include "libavformat/avformat.h"
#include "libswscale/swscale.h"
#include "libswresample/swresample.h"
#include "libavutil/imgutils.h"
}

class AbsMedia {

public:
    int stream_index = -1;
    JNICall *jni_call = NULL;
    AVCodecContext *codec_ctx = NULL;
    MediaPacketQueue *queue = NULL;
    MediaStatus *status = NULL;
    pthread_mutex_t mutex_codec_ctx;

    AbsMedia(JNICall *jniCall, int streamIndex, MediaStatus *status);

    ~AbsMedia();

public:

    /**
     * 总共的时长
     *
     * Unit: second
     */
    double total_duration;

    /**
     * 记录当前播放时间
     *
     * Unit: second
     */
    double time_current = 0;

    /**
     * 上次回调 Java 层的时间
     *
     * Unit: second
     */
    int time_last_callback = 0;

    /**
     * 时间基
     */
    AVRational time_base;


public:
    bool prepare(AVFormatContext *format_ctx);

    virtual bool prepareInternal(AVFormatContext *format_ctx) = 0;

    virtual void play() = 0;

    void seek(AVFormatContext *format_ctx, int seconds);

    virtual void pause() = 0;

    virtual void resume() = 0;

    virtual void stop() = 0;

    virtual void release();

    void callPlayerJniError(int code, const char *msg);

};


#endif //SMEDIA_PLAYER_ABSMEDIA_H
