//
// Created by Sharry Choo on 2019-06-24.
//

#include <pthread.h>
#include <unistd.h>
#include "SAudioPlayer.h"
#include "ConstDefine.h"

// //////////////////////////////// Open method /////////////////////////////////////////

SAudioPlayer::SAudioPlayer(JNICall *jniCall, int audioStreamIndex, MediaStatus *status) : AbsMedia(
        jniCall, audioStreamIndex, status) {
    st = new SoundTouch();
}

SAudioPlayer::~SAudioPlayer() {
    release();
}

bool SAudioPlayer::prepareInternal(AVFormatContext *) {
    /*
     * 构建重采样参数
     */
    // 音频原始数据
    int64_t in_ch_layout = codec_ctx->channel_layout;
    enum AVSampleFormat in_sample_fmt = codec_ctx->sample_fmt;
    int in_sample_rate = codec_ctx->sample_rate;
    LOGI("Audio SampleRate = %d, SampleRate format = %d, channel count = %d, total_duration = %lfs",
         in_sample_rate, in_sample_fmt, codec_ctx->channels, total_duration);

    // 目标重采样数据
    int64_t out_ch_layout = SAUDIO_RESAMPLE_CHANNEL_LAYOUT;
    enum AVSampleFormat out_sample_fmt = SAUDIO_RESAMPLE_PER_SAMPLE_FORMAT;
    int out_sample_rate = SAUDIO_RESAMPLE_SAMPLE_RATE;
    swr_context = swr_alloc_set_opts(
            NULL,
            out_ch_layout, out_sample_fmt, out_sample_rate,
            in_ch_layout, in_sample_fmt, in_sample_rate,
            0, NULL
    );
    if (swr_context == NULL) {
        callPlayerJniError(-1, "create swr_context failed.");
        return false;
    }

    /*
     * 初始化重采样
     */
    int swr_init_res = swr_init(swr_context);
    if (swr_init_res < 0) {
        callPlayerJniError(swr_init_res, "swr init error");
        return false;
    }

    /*
     * 创建缓冲数组
     */
    // 声音通道数量
    int nb_channels = av_get_channel_layout_nb_channels(
            static_cast<uint64_t>(out_ch_layout));
    // 声音通道数量
    int nb_samples = out_sample_rate / nb_channels;
    // 计算每一帧音频数据所需的缓冲区大小
    int buffer_size = av_samples_get_buffer_size(NULL, nb_channels, nb_samples, out_sample_fmt, 0);
    if (buffer_size < 0) {
        callPlayerJniError(-1, "bufferSize is illegal.");
        return false;
    }
    // 创建用于存储重采样数据的数组
    pcm_buffer = static_cast<uint8_t *>(malloc(static_cast<size_t>(buffer_size)));
    // 创建 SoundTouch 用于变速的数组
    st_buffer = static_cast<SAMPLETYPE *>(malloc(static_cast<size_t>(buffer_size)));
    // 设置 SoundTouch 的相关变量
    st->setSampleRate(static_cast<uint>(out_sample_rate));
    st->setChannels(static_cast<uint>(nb_channels));
    return true;
}

void playerCallback(SLAndroidSimpleBufferQueueItf caller, void *context) {
    // 从队列中取数据播放......
    try {
        SAudioPlayer *audio = static_cast<SAudioPlayer *>(context);
        int frame_bytes = audio->getSoundTouchData();
        if (frame_bytes != 0) {
            (*caller)->Enqueue(caller, audio->st_buffer,
                               static_cast<SLuint32>(frame_bytes));
        }
    } catch (...) {
        LOGE("OpenSLES occurred an exception. now tid is %d", gettid());
    }
}

void *openSLESPlay(void *context) {
    SAudioPlayer *impl = static_cast<SAudioPlayer *>(context);
    // 设置播放状态
    (*impl->sl_itf_pcm_player)->SetPlayState(impl->sl_itf_pcm_player, SL_PLAYSTATE_PLAYING);
    // 主动进行一次回调, 后续会在 OpenSL 开辟的新线程回调
    playerCallback(impl->sl_itf_pcm_buffer_queue, impl);
    return 0;
}

void SAudioPlayer::play() {
    // 初始化 OpenSL es
    initOpenSLES();
    // 创建消费者线程, 进行 opensl 的播放
    pthread_create(&thread_opensl_es_play, NULL, openSLESPlay, this);
}

void SAudioPlayer::pause() {
    if (sl_itf_pcm_player != NULL) {
        (*sl_itf_pcm_player)->SetPlayState(sl_itf_pcm_player, SL_PLAYSTATE_PAUSED);
    }
}

void SAudioPlayer::resume() {
    if (sl_itf_pcm_player != NULL) {
        (*sl_itf_pcm_player)->SetPlayState(sl_itf_pcm_player, SL_PLAYSTATE_PLAYING);
    }
}

void SAudioPlayer::stop() {
    queue->notifyAll();
    if (sl_itf_pcm_player != NULL) {
        (*sl_itf_pcm_player)->SetPlayState(sl_itf_pcm_player, SL_PLAYSTATE_STOPPED);
    }
    int res = pthread_kill(thread_opensl_es_play, 0);
    if (res == ESRCH) {
        LOGI("thread_opensl_es_play already killed");
    } else {
        pthread_join(thread_opensl_es_play, NULL);
        LOGI("thread_opensl_es_play exit.");
    }
}

void SAudioPlayer::release() {
    AbsMedia::release();
    // 释放 jPcmByteArray 的内存
    if (pcm_buffer != NULL) {
        free(pcm_buffer);
        pcm_buffer = NULL;
    }
    if (st != NULL) {
        delete (st);
        st = NULL;
    }
    if (st_buffer != NULL) {
        free(st_buffer);
        st_buffer = NULL;
    }
    if (sl_obj_pcm_player != NULL) {
        (*sl_obj_pcm_player)->Destroy(sl_obj_pcm_player);
        sl_obj_pcm_player = NULL;
        sl_itf_pcm_player = NULL;
        sl_itf_pcm_buffer_queue = NULL;
        sl_itf_pcm_mute = NULL;
        sl_itf_pcm_volume = NULL;
    }
    if (sl_obj_output_mix != NULL) {
        (*sl_obj_output_mix)->Destroy(sl_obj_output_mix);
        sl_obj_output_mix = NULL;
        sl_itf_output_mix = NULL;
    }
    if (sl_obj_engine != NULL) {
        (*sl_obj_engine)->Destroy(sl_obj_engine);
        sl_obj_engine = NULL;
        sl_itf_engine = NULL;
    }
}

void SAudioPlayer::setChannelLayout(int channel_layout) {
    if (sl_itf_pcm_mute != NULL) {
        switch (channel_layout) {
            case SAUDIO_CHANNEL_LAYOUT_RIGHT:
                (*sl_itf_pcm_mute)->SetChannelMute(sl_itf_pcm_mute, 0, true);
                (*sl_itf_pcm_mute)->SetChannelMute(sl_itf_pcm_mute, 1, false);
                break;
            case SAUDIO_CHANNEL_LAYOUT_LEFT:
                (*sl_itf_pcm_mute)->SetChannelMute(sl_itf_pcm_mute, 0, false);
                (*sl_itf_pcm_mute)->SetChannelMute(sl_itf_pcm_mute, 1, true);
                break;
            case SAUDIO_CHANNEL_LAYOUT_STEREO:
            default:
                (*sl_itf_pcm_mute)->SetChannelMute(sl_itf_pcm_mute, 0, false);
                (*sl_itf_pcm_mute)->SetChannelMute(sl_itf_pcm_mute, 1, false);
                break;
        }
    }
}

void SAudioPlayer::setVolume(int percent) {
    if (sl_itf_pcm_volume != NULL) {
        // OpenSL es 的音量范围为 [-5000, 0], 与 [0, 100] 的转换公式为: (100 - percent) * (-50)
        // 以下为平滑调节音量的处理代码
        if (percent > 30) {
            (*sl_itf_pcm_volume)->SetVolumeLevel(sl_itf_pcm_volume, (100 - percent) * (-20));
        } else if (percent > 25) {
            (*sl_itf_pcm_volume)->SetVolumeLevel(sl_itf_pcm_volume, (100 - percent) * (-22));
        } else if (percent > 20) {
            (*sl_itf_pcm_volume)->SetVolumeLevel(sl_itf_pcm_volume, (100 - percent) * (-25));
        } else if (percent > 15) {
            (*sl_itf_pcm_volume)->SetVolumeLevel(sl_itf_pcm_volume, (100 - percent) * (-28));
        } else if (percent > 10) {
            (*sl_itf_pcm_volume)->SetVolumeLevel(sl_itf_pcm_volume, (100 - percent) * (-30));
        } else if (percent > 5) {
            (*sl_itf_pcm_volume)->SetVolumeLevel(sl_itf_pcm_volume, (100 - percent) * (-34));
        } else if (percent > 3) {
            (*sl_itf_pcm_volume)->SetVolumeLevel(sl_itf_pcm_volume, (100 - percent) * (-37));
        } else if (percent > 0) {
            (*sl_itf_pcm_volume)->SetVolumeLevel(sl_itf_pcm_volume, (100 - percent) * (-40));
        } else {
            (*sl_itf_pcm_volume)->SetVolumeLevel(sl_itf_pcm_volume, (100 - percent) * (-100));
        }
    }
}

void SAudioPlayer::setPitch(float pitch) {
    if (st != NULL) {
        st->setPitch(pitch);
    }
}

void SAudioPlayer::setTempo(float tempo) {
    if (st != NULL) {
        st->setTempo(tempo);
    }
}

// ////////////////////////////////// Inner methods ////////////////////////////////////////

void SAudioPlayer::initOpenSLES() {
    /// 1. 创建引擎接口对象
    slCreateEngine(&sl_obj_engine, 0, NULL, 0, NULL, NULL);
    // realize the engine
    (*sl_obj_engine)->Realize(sl_obj_engine, SL_BOOLEAN_FALSE);
    // get the engine interface, which is needed in order to create other objects
    (*sl_obj_engine)->GetInterface(sl_obj_engine, SL_IID_ENGINE, &sl_itf_engine);

    /// 2. 设置混音器
    const SLInterfaceID ids[1] = {SL_IID_ENVIRONMENTALREVERB};
    const SLboolean req[1] = {SL_BOOLEAN_FALSE};
    (*sl_itf_engine)->CreateOutputMix(sl_itf_engine, &sl_obj_output_mix, 1, ids, req);
    (*sl_obj_output_mix)->Realize(sl_obj_output_mix, SL_BOOLEAN_FALSE);
    (*sl_obj_output_mix)->GetInterface(sl_obj_output_mix, SL_IID_ENVIRONMENTALREVERB,
                                       &sl_itf_output_mix);
    SLEnvironmentalReverbSettings reverb_settings = SL_I3DL2_ENVIRONMENT_PRESET_STONECORRIDOR;
    (*sl_itf_output_mix)->SetEnvironmentalReverbProperties(sl_itf_output_mix, &reverb_settings);

    /// 3. 创建播放器
    // 配置 PCM 格式信息
    SLDataLocator_AndroidSimpleBufferQueue simple_buffer_queue = {
            SL_DATALOCATOR_ANDROIDSIMPLEBUFFERQUEUE, 2};
    SLDataFormat_PCM format_pcm = {
            SL_DATAFORMAT_PCM,                                    // 播放 PCM 数据
            2,                                                    // 两声道
            SL_SAMPLINGRATE_44_1,                                 // 采样率 44100
            SL_PCMSAMPLEFORMAT_FIXED_16,                          // 位数 16 位
            SL_PCMSAMPLEFORMAT_FIXED_16,                          // 和位数一致
            SL_SPEAKER_FRONT_LEFT | SL_SPEAKER_FRONT_RIGHT,       // 立体声
            SL_BYTEORDER_LITTLEENDIAN                             // 配置信息结束标识
    };
    SLDataSource data_source = {&simple_buffer_queue, &format_pcm};
    SLDataLocator_OutputMix output_mix = {SL_DATALOCATOR_OUTPUTMIX, sl_obj_output_mix};
    SLDataSink data_sink = {&output_mix, NULL};
    // 填充请求的方法描述
    SLInterfaceID interface_ids[3] = {SL_IID_BUFFERQUEUE, SL_IID_VOLUME, SL_IID_MUTESOLO};
    SLboolean interface_required[3] = {SL_BOOLEAN_TRUE, SL_BOOLEAN_TRUE, SL_BOOLEAN_TRUE};
    (*sl_itf_engine)->CreateAudioPlayer(sl_itf_engine, &sl_obj_pcm_player, &data_source, &data_sink,
                                        3, interface_ids, interface_required);
    /// 4. 实现播放器
    (*sl_obj_pcm_player)->Realize(sl_obj_pcm_player, SL_BOOLEAN_FALSE);

    /// 5. 获取接口
    // 获取 player 接口
    (*sl_obj_pcm_player)->GetInterface(sl_obj_pcm_player, SL_IID_PLAY, &sl_itf_pcm_player);
    // 获取声音控制接口
    (*sl_obj_pcm_player)->GetInterface(sl_obj_pcm_player, SL_IID_VOLUME, &sl_itf_pcm_volume);
    // 获取声道控制接口
    (*sl_obj_pcm_player)->GetInterface(sl_obj_pcm_player, SL_IID_MUTESOLO, &sl_itf_pcm_mute);
    // 获取缓冲队列接口 pcm_buffer_queue
    (*sl_obj_pcm_player)->GetInterface(sl_obj_pcm_player, SL_IID_BUFFERQUEUE,
                                       &sl_itf_pcm_buffer_queue);

    /// 注册缓冲接口回调
    (*sl_itf_pcm_buffer_queue)->RegisterCallback(sl_itf_pcm_buffer_queue, playerCallback, this);
}

int SAudioPlayer::getSoundTouchData() {
    // 获取重采样之后每个通道的采样点数
    int nb_per_channel_samples = 0;
    int nb_channels = av_get_channel_layout_nb_channels(SAUDIO_RESAMPLE_CHANNEL_LAYOUT);
    int byte_per_sample = av_get_bytes_per_sample(SAUDIO_RESAMPLE_PER_SAMPLE_FORMAT);
    // 获取 SoundTouch 处理好的一帧音频数据
    int nb_received_per_channel_samples = 0;
    while (nb_received_per_channel_samples == 0 && status->playing && !status->pausing) {
        if ((nb_per_channel_samples = resampleFrame()) == 0) {
            continue;
        }
        // 计算采样点总数
        int nb_samples = nb_per_channel_samples * nb_channels;
        // 将采样点注入 sound_touch_buffer 数组
        // FFmpeg 重采样的 buffer 为 8 bit
        // SoundTouch 的 buffer 为 16 bit, 因此需要合并拷贝到 sound_touch_buffer 中
        for (int i = 0; i < nb_samples + 1; i++) {
            st_buffer[i] = (
                    pcm_buffer[i * 2] | (pcm_buffer[i * 2 + 1] << 8)
            );
        }
        // 将数据提交给 SoundTouch 处理
        st->putSamples(st_buffer, static_cast<uint>(nb_per_channel_samples));
        // 尝试获取 SoundTouch 处理好的 nb_per_channel_samples 个采样点的数据
        nb_received_per_channel_samples = st->receiveSamples(st_buffer,
                                                             static_cast<uint>(nb_per_channel_samples));
    };
    // 计算数据的字节数
    int st_frame_bytes = nb_received_per_channel_samples * byte_per_sample * nb_channels;
    // 回调分贝数数量到 java 层
    jni_call->callOnVolumeDBChanged(
            getPCMDB(
                    reinterpret_cast<char *>(st_buffer),
                    st_frame_bytes
            )
    );
    return st_frame_bytes;
}

int SAudioPlayer::resampleFrame() {
    int per_channel_nb_samples = 0;
    // 这里进行了 while 循环, 防止队列中读到无效数据影响播放
    // 回调是否正在加载
    jni_call->callOnLoading(queue->size() == 0);
    // 执行重采样操作
    AVPacket *packet = queue->pop();
    if (packet == NULL) {
        LOGI("Audio resample packet is NULL, is mean queue has been cleared.");
        return per_channel_nb_samples;
    }
    pthread_mutex_lock(&mutex_codec_ctx);
    int res;
    AVFrame *frame = av_frame_alloc();
    if ((res = avcodec_send_packet(codec_ctx, packet)) == 0) {
        if ((res = avcodec_receive_frame(codec_ctx, frame)) == 0) {
            // 返回一个通道采样点的个数
            per_channel_nb_samples = swr_convert(
                    swr_context,
                    &pcm_buffer,
                    frame->nb_samples,
                    reinterpret_cast<const uint8_t **>(&frame->data),
                    frame->nb_samples
            );
            // 获取当前帧的时间
            time_current = av_frame_get_best_effort_timestamp(frame) * av_q2d(time_base);// 换算为秒
            // 回调到 Java 层
            if (abs(static_cast<int>(time_current - time_last_callback)) > 0) {
                time_last_callback = static_cast<int>(time_current);
                jni_call->callOnProgress(time_last_callback, static_cast<int>(total_duration));
            }
        } else {
            LOGW("Audio avcodec_receive_frame occurred an error, error code is %d", res);
        }
    } else {
        LOGW("Audio avcodec_send_packet occurred an error, error code is %d", res);
    }
    pthread_mutex_unlock(&mutex_codec_ctx);
    av_packet_unref(packet);
    av_packet_free(&packet);
    av_frame_unref(frame);
    av_frame_free(&frame);
    // 返回一个通道采样点的个数
    return per_channel_nb_samples;
}

int SAudioPlayer::getPCMDB(char *data, size_t data_bytes) {
    int db = 0;
    short int perval = 0;
    // 计算所有采样点的综合
    double sum = 0;
    for (int i = 0; i < data_bytes; i += 2) {
        // 每次拷贝两字节, 即 16 位与采样点位一致
        memcpy(&perval, data + i, 2);
        sum += abs(perval);
    }
    // 获取采样点的平均值
    int average = static_cast<int>(sum / (data_bytes / 2));
    if (sum > 0) {
        db = static_cast<int>((int) 20.0 * log10(average));
    }
    return db;
}
