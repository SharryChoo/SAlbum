//
// Created by Sharry Choo on 2019-06-22.
//

#include <pthread.h>
#include <malloc.h>
#include <string>
#include "SFFmpeg.h"
#include "ConstDefine.h"

// ///////////////// Method implementation ///////////////////

SFFmpeg::SFFmpeg(JNICall *jni_call, const char *uri) {
    this->jni_call = jni_call;
    this->uri = static_cast<char *>(malloc(strlen(uri + 1)));
    strcpy(this->uri, uri);
    this->status = new MediaStatus();
    pthread_mutex_init(&mutex_cond_pause, NULL);
    pthread_mutex_init(&mutex_format_ctx, NULL);
    pthread_cond_init(&cond_pause, NULL);
    LOGI("FFmpeg created.");
}

SFFmpeg::~SFFmpeg() {
    release();
}

void SFFmpeg::prepare() {
    // 注册音视频编解码
    av_register_all();
    // 初始化网络引擎
    avformat_network_init();

    /// 1. 打开资源
    int format_open_res = avformat_open_input(&format_ctx, uri, NULL, NULL);
    if (format_open_res < 0) {
        std::string str = "Open data source failed, the data source is: ";
        str += uri;
        jni_call->callOnError(format_open_res, str.c_str());
        return;
    }

    /// 2. 获取流信息
    int find_stream_info_res = avformat_find_stream_info(format_ctx, NULL);
    if (find_stream_info_res < 0) {
        jni_call->callOnError(find_stream_info_res, "find stream info failed.");
        return;
    }

    /// 3. 初始化音频播放器
    int audio_stream_index = av_find_best_stream(format_ctx, AVMediaType::AVMEDIA_TYPE_AUDIO,
                                                 -1, -1, NULL, 0);
    if (audio_stream_index < 0) {
        // 无音视频流, 说明数据有问题
        jni_call->callOnError(-1, "Cannot find or prepare audio");
        return;
    } else {
        audio = new SAudioPlayer(jni_call, audio_stream_index, status);
        if (!audio->prepare(format_ctx)) {
            delete (audio);
            audio = NULL;
            // 无音视频流, 说明数据有问题
            jni_call->callOnError(-1, "Cannot find or prepare audio");
            return;
        }
    }

    /// 4. 初始化视频播放器
    int video_stream_index = av_find_best_stream(format_ctx, AVMediaType::AVMEDIA_TYPE_VIDEO,
                                                 -1, -1, NULL, 0);
    if (video_stream_index < 0) {
        // 运行无视频流
        LOGE("Cannot find video index.");
    } else {
        // 初始化视频播放
        video = new SVideoPlayer(jni_call, video_stream_index, status, audio);
        if (!video->prepare(format_ctx)) {
            delete (video);
            video = NULL;
        }
    }

    /// 5. 初始化成功, 回调总时长
    jni_call->callOnPrepared(static_cast<int>(audio->total_duration));
    LOGI("FFmpeg prepared.");
}

void *doThreadPlay(void *context) {
    try {
        SFFmpeg *impl = static_cast<SFFmpeg *>(context);
        if (impl != NULL) {
            impl->decode();
        }
    } catch (...) {
        LOGE("FFmpeg play thread occurred an exception.");
    }
    return 0;
}

void SFFmpeg::play() {
    if (status->playing) {
        LOGI("FFmpeg is already playing.");
        return;
    }
    // 更新状态
    status->playing = true;
    // 创建一个生产者线程
    pthread_create(&thread_ffmpeg_decode, NULL, doThreadPlay, this);
    // 播放音频
    if (audio != NULL) {
        audio->play();
    }
    // 播放视频
    if (video != NULL) {
        video->play();
    }
    LOGI("FFmpeg played");
}

void SFFmpeg::decode() {
    while (status->playing) {
        // 暂停, 阻塞等待
        pthread_mutex_lock(&mutex_cond_pause);
        while (status->pausing) {
            pthread_cond_wait(&cond_pause, &mutex_cond_pause);
        }
        pthread_mutex_unlock(&mutex_cond_pause);
        // seek, 稍后重试
        if (status->seeking) {
            av_usleep(100 * 1000);// 睡眠 100 ms
            continue;
        }
        // 解码数据帧
        AVPacket *pkt = av_packet_alloc();
        pthread_mutex_lock(&mutex_format_ctx);
        int res = av_read_frame(format_ctx, pkt);
        pthread_mutex_unlock(&mutex_format_ctx);
        if (res >= 0) { // 数据写入队列
            if (audio != NULL && pkt->stream_index == audio->stream_index) {
                audio->queue->push(pkt);
            } else if (video != NULL && pkt->stream_index == video->stream_index) {
                video->queue->push(pkt);
            } else {
                av_packet_unref(pkt);
                av_packet_free(&pkt);
            }
        } else if (res == AVERROR_EOF) {// 解码完毕
            av_packet_unref(pkt);
            av_packet_free(&pkt);
            // 等待音频播放完毕
            while (status->playing) {
                // 队列中存在数据, 说明仍在继续播放
                if (audio != NULL && audio->queue->size() > 0) {
                    av_usleep(100 * 1000);// 睡眠 100 ms
                    continue;
                } else {
                    jni_call->callOnCompleted();
                    break;
                }
            }
            break;
        } else {
            // 解码过程中的非致命异常, 无需回调 JNI 的 onError
            av_packet_unref(pkt);
            av_packet_free(&pkt);
            // 睡眠 100 ms, 降低 CPU 占用
            av_usleep(100 * 1000);
            LOGW("av_read_frame occurred an error, code is %d", res);
        }
    }
    LOGI("decode quit.");
}

void SFFmpeg::seek(int seconds) {
    status->seeking = true;
    pthread_mutex_lock(&mutex_format_ctx);
    if (audio != NULL) {
        audio->seek(format_ctx, seconds);
    }
    if (video != NULL) {
        video->seek(format_ctx, seconds);
    }
    pthread_mutex_unlock(&mutex_format_ctx);
    status->seeking = false;
    LOGI("FFmpeg seeked.");
}

void SFFmpeg::pause() {
    if (!status->playing) {
        LOGI("Invalid operation, current status is just prepared, not playing.");
        return;
    }
    if (status->pausing) {
        LOGI("FFmpeg is already pausing.");
        return;
    }
    // 置为暂停状态
    status->pausing = true;
    // 暂停音频的消费者
    if (audio != NULL) {
        audio->pause();
    }
    // 暂停视频的消费者
    if (video != NULL) {
        video->pause();
    }
    LOGI("FFmpeg paused.");
}

void SFFmpeg::resume() {
    if (!status->playing) {
        LOGI("Invalid operation, current status is just prepared, not playing.");
        return;
    }
    if (!status->pausing) {
        LOGI("FFmpeg is already resume.");
        return;
    }
    // 解除暂停状态
    status->pausing = false;
    // 唤醒解码线程
    pthread_cond_signal(&cond_pause);
    // 恢复音频
    if (audio != NULL) {
        audio->resume();
    }
    // 播放视频
    if (video != NULL) {
        video->resume();
    }
    LOGI("FFmpeg resumed.");
}

void SFFmpeg::stop() {
    status->playing = false;
    status->pausing = false;
    // 唤醒解码线程的暂停
    pthread_cond_signal(&cond_pause);
    // 结束视频播放
    if (video != NULL) {
        video->stop();
    }
    // 结束音频播放
    if (audio != NULL) {
        audio->stop();
    }
    // 若当前正在播放, 则等待播放线程结束
    int res = pthread_kill(thread_ffmpeg_decode, 0);
    if (res == ESRCH) {
        LOGI("thread_ffmpeg_decode already killed");
    } else {
        pthread_join(thread_ffmpeg_decode, NULL);
        LOGI("thread_ffmpeg_decode exit.");
    }
    LOGI("FFmpeg stoped.");
}

void SFFmpeg::setVolume(int percent) {
    if (audio != NULL) {
        audio->setVolume(percent);
    }
}

void SFFmpeg::setChannelLayout(int channel_layout) {
    if (audio != NULL) {
        audio->setChannelLayout(channel_layout);
    }
}

void SFFmpeg::setPitch(float pitch) {
    if (audio != NULL) {
        audio->setPitch(pitch);
    }
}

void SFFmpeg::setTempo(float tempo) {
    if (audio != NULL) {
        audio->setTempo(tempo);
    }
}

void SFFmpeg::release() {
    // 释放视频资源
    if (video != NULL) {
        delete (video);
        video = NULL;
    }
    // 释放音视频播放资源
    if (audio != NULL) {
        delete (audio);
        audio = NULL;
    }
    // 释放创建的对象
    if (status != NULL) {
        delete (status);
        status = NULL;
    }
    // 释放 FFmpeg 资源
    if (format_ctx != NULL) {
        avformat_close_input(&format_ctx);
        avformat_free_context(format_ctx);
        avformat_network_deinit();
        format_ctx = NULL;
        // 销毁暂停锁
        pthread_mutex_destroy(&mutex_cond_pause);
        // 销毁 Seek 锁
        pthread_mutex_destroy(&mutex_format_ctx);
        // 销毁暂停条件变量
        pthread_cond_destroy(&cond_pause);
    }
    // 标记为已释放
    LOGI("FFmpeg released");
}

