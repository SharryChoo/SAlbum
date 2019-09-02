//
// Created by Sharry Choo on 2019-06-24.
//

#ifndef SMEDIA_PLAYER_SVIDEOPLAYER_H
#define SMEDIA_PLAYER_SVIDEOPLAYER_H


#include "AbsMedia.h"
#include "SAudioPlayer.h"

class SVideoPlayer : public AbsMedia {
public:
    SAudioPlayer *audio;
    pthread_mutex_t mutex_pause;
    pthread_cond_t cond_pause;

    SVideoPlayer(JNICall *jni_call, int video_stream_index, MediaStatus *status, SAudioPlayer *audio);

    ~SVideoPlayer();

public:
    /**
     * 渲染视频的线程
     */
    pthread_t thread_video_play = NULL;

    /**
     * 音视频同步, 默认情况下最合适的延时, 根据帧率获取
     */
    double default_sync_delay_time = 0.04;

    /**
     * 音视频同步延时时长
     */
    double sync_delay_time;

    bool prepareInternal(AVFormatContext *format_context);

    void play();

    void pause();

    void resume();

    void stop();

    void release();

    /**
     * 获取视频同步音频休眠的时间
     *
     * @param video_frame 当前视频帧
     * @return 休眠时间(秒)
     */
    double getSyncDelayTime(AVFrame *video_frame);

    void renderActual();

};


#endif //SMEDIA_PLAYER_SVIDEOPLAYER_H
