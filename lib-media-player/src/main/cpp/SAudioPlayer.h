//
// Created by Sharry Choo on 2019-06-24.
//

#ifndef SMEDIA_PLAYER_SAUDIOPLAYER_H
#define SMEDIA_PLAYER_SAUDIOPLAYER_H

#include <SLES/OpenSLES.h>
#include <SLES/OpenSLES_Android.h>
#include "AbsMedia.h"
#include "soundtouch/SoundTouch.h"

using namespace soundtouch;

class SAudioPlayer : public AbsMedia {
public:
    SAudioPlayer(JNICall *jniCall, int audioStreamIndex, MediaStatus *status);

    ~SAudioPlayer();

public:
    pthread_t thread_opensl_es_play;

    /**
     * 用于音频重采样
     */
    SwrContext *swr_context = NULL;
    uint8_t *pcm_buffer = NULL;   // 存储重采样之后的数据

    /**
     * 使用 SoundTouch 控制变速
     */
    SoundTouch *st = NULL;
    SAMPLETYPE *st_buffer = NULL;

    /**
     * OpenSL ES 控制音频播放
     */
    // opensl 对象
    SLObjectItf sl_obj_engine = NULL;
    SLObjectItf sl_obj_output_mix = NULL;
    SLObjectItf sl_obj_pcm_player = NULL;
    // opensl 接口
    SLEngineItf sl_itf_engine;
    SLEnvironmentalReverbItf sl_itf_output_mix = NULL;
    SLPlayItf sl_itf_pcm_player = NULL;
    SLVolumeItf sl_itf_pcm_volume = NULL;
    SLMuteSoloItf sl_itf_pcm_mute = NULL;
    SLAndroidSimpleBufferQueueItf sl_itf_pcm_buffer_queue;

public:

    bool prepareInternal(AVFormatContext *format_context);

    void play();

    void pause();

    void resume();

    void stop();

    void release();

public:
    /**
     * 音频独有, 使用 OpenSL ES 控制音量
     * @param percent  [0, 100]
     */
    void setVolume(int percent);

    /**
     * 设置声道
     *
     * @param channel_layout 0 is right, 1 is left, 2 is stereo
     */
    void setChannelLayout(int channel_layout);

    /**
     * 设置音调
     *
     * @param pitch base is 1.0f
     */
    void setPitch(float pitch);

    /**
     * 设置速度
     *
     * @param tempo base is 1.0f
     */
    void setTempo(float tempo);

public:
    // 初始化 OpenSL ES
    void initOpenSLES();

    // 获取 SoundTouch 处理后的数据
    int getSoundTouchData();

    // 重采样 PCM 数据帧, 返回每个通道采样点的数量
    int resampleFrame();

    // 根据 PCM 数据, 求当前帧的分贝值
    int getPCMDB(char *data, size_t data_bytes);

};

#endif //SMEDIA_PLAYER_SAUDIOPLAYER_H
