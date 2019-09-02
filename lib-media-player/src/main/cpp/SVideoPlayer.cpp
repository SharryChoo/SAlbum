//
// Created by Sharry Choo on 2019-06-24.
//

#include <string>
#include <android/native_window.h>
#include <android/native_window_jni.h>
#include "SVideoPlayer.h"
#include "ConstDefine.h"

extern "C" {
#include "libavutil/time.h"
};

void *doThreadRender(void *context);

// //////////////////////////////// Implementation/////////////////////////////////////////

SVideoPlayer::SVideoPlayer(JNICall *jni_call, int video_stream_index, MediaStatus *status,
                           SAudioPlayer *audio) : AbsMedia(jni_call, video_stream_index, status) {
    this->audio = audio;
    pthread_mutex_init(&mutex_pause, NULL);
    pthread_cond_init(&cond_pause, NULL);
}

SVideoPlayer::~SVideoPlayer() {
    release();
}

bool SVideoPlayer::prepareInternal(AVFormatContext *format_context) {
    // 查找解码器
    AVCodecParameters *params = format_context->streams[stream_index]->codecpar;
    AVCodec *codec = avcodec_find_decoder(params->codec_id);
    if (codec == NULL) {
        callPlayerJniError(-1, "find video codec error.");
        return false;
    }
    // 打开解码器
    codec_ctx = avcodec_alloc_context3(codec);
    if (codec_ctx == NULL) {
        callPlayerJniError(-1, "video codec alloc context error.");
        return false;
    }
    int params_to_cxt = avcodec_parameters_to_context(codec_ctx, params);
    if (params_to_cxt < 0) {
        callPlayerJniError(params_to_cxt, "param 2 context error");
        return false;
    }
    int codec_open_res = avcodec_open2(codec_ctx, codec, NULL);
    if (codec_open_res != 0) {
        callPlayerJniError(codec_open_res, "codec open error");
        return false;
    }
    // 计算默认的延时时长
    int num = format_context->streams[stream_index]->avg_frame_rate.num;// 分子
    int den = format_context->streams[stream_index]->avg_frame_rate.den;// 分母
    if (den != 0 && num != 0) {
        int fps = num / den;
        default_sync_delay_time = 1.0f / fps;
        LOGI("Video: fmt = %d, fps = %d, total_duration = %lfs", codec_ctx->pix_fmt, fps,
             total_duration);
    }
    return true;
}

void SVideoPlayer::play() {
    // 创建消费者线程, 读取视频队列中的数据
    pthread_create(&thread_video_play, NULL, doThreadRender, this);
}

void *doThreadRender(void *context) {
    try {
        SVideoPlayer *impl = static_cast<SVideoPlayer *>(context);
        impl->renderActual();
    } catch (...) {
        LOGE("Video doThreadRender occurred an exception.");
    }
    return 0;
}

// on work thread.
void SVideoPlayer::renderActual() {
    // 初始化转换上下文
    SwsContext *sws_ctx = sws_getContext(
            codec_ctx->width,
            codec_ctx->height,
            codec_ctx->pix_fmt,
            codec_ctx->width,
            codec_ctx->height,
            AV_PIX_FMT_YUV420P,
            SWS_BICUBIC, NULL, NULL, NULL
    );
    // 创建用于存储原始数据帧
    AVFrame *origin_frame = av_frame_alloc();
    AVFrame *yuv420p_frame = av_frame_alloc();
    int buffer_size = av_image_get_buffer_size(
            AV_PIX_FMT_YUV420P,
            codec_ctx->width,
            codec_ctx->height,
            1
    );
    // 填充数组数据
    uint8_t *buffer = static_cast<uint8_t *>(av_malloc(
            buffer_size * sizeof(uint8_t)));
    av_image_fill_arrays(
            yuv420p_frame->data, yuv420p_frame->linesize, buffer,
            AV_PIX_FMT_YUV420P, codec_ctx->width,
            codec_ctx->height, 1
    );
    while (status->playing) {
        // 若是暂停了则阻塞等待
        pthread_mutex_lock(&mutex_pause);
        while (status->pausing) {
            pthread_cond_wait(&cond_pause, &mutex_pause);
        }
        pthread_mutex_unlock(&mutex_pause);
        // 若正在 seeking, 则稍后重试
        if (status->seeking) {
            av_usleep(100 * 1000);
            continue;
        }
        AVPacket *packet = queue->pop();
        if (packet == NULL) {
            LOGI("Video packet is NULL, is mean queue has been cleared.");
            continue;
        }
        pthread_mutex_lock(&mutex_codec_ctx);
        int res;
        if ((res = avcodec_send_packet(codec_ctx, packet)) == 0) {
            if ((res = avcodec_receive_frame(codec_ctx, origin_frame)) == 0) {
                // 执行转码, 转为标准的 YUV420P(YU12/YV12)
                // 防止 YUV420SP(NV12/NV21) 被误识别成为 YUV420P 导致的花屏
                sws_scale(
                        sws_ctx,
                        (const uint8_t *const *) origin_frame->data,
                        origin_frame->linesize,
                        0,
                        codec_ctx->height,
                        yuv420p_frame->data,
                        yuv420p_frame->linesize
                );
                // 回调到 Java 层
                jni_call->callOnRenderYUV(
                        codec_ctx->width, codec_ctx->height,
                        yuv420p_frame->data[0],
                        yuv420p_frame->data[1],
                        yuv420p_frame->data[2]
                );
                // 若正在 seeking, 则停止同步操作
                if (!status->seeking) {
                    // 在播放之前判断需要休眠的时长, 用于和音频保持同步
                    av_usleep(getSyncDelayTime(origin_frame) * 1000 * 1000);
                } else {
                    LOGW("Invalid sync, because is seeking.");
                }
            } else {
                LOGW("Video avcodec_receive_frame occurred an error, error code is %d", res);
            }
        } else {
            LOGW("Video avcodec_send_packet occurred an error, error code is %d", res);
        }
        pthread_mutex_unlock(&mutex_codec_ctx);
        av_packet_unref(packet);
        av_packet_free(&packet);
    }
    av_frame_unref(origin_frame);
    av_frame_free(&origin_frame);
    av_frame_unref(yuv420p_frame);
    av_frame_free(&yuv420p_frame);
    sws_freeContext(sws_ctx);
    free(buffer);
}

void SVideoPlayer::pause() {
    // do nothing.
}

void SVideoPlayer::resume() {
    // 唤醒暂停时的阻塞
    pthread_cond_signal(&cond_pause);
}

void SVideoPlayer::stop() {
    // 清空队列
    queue->notifyAll();
    // 唤醒暂停时的阻塞
    pthread_cond_signal(&cond_pause);
    int res = pthread_kill(thread_video_play, 0);
    if (res == ESRCH) {
        LOGI("thread_video_play already killed");
    } else {
        pthread_join(thread_video_play, NULL);
        LOGI("thread_video_play exit.");
    }
}

double SVideoPlayer::getSyncDelayTime(AVFrame *video_frame) {
    time_current = av_frame_get_best_effort_timestamp(video_frame) * av_q2d(time_base);// 换算为秒
    // 获取视频帧与音频帧相差的绝对时长
    double time_diff = audio->time_current - time_current;
    // 视频快了慢一些, 视频慢了就快一些
    // 尽量将时间控制在范围之内, fps: 24, 0.04; fps: 30, 0.033
    // 第一次控制，0.016s ～ -0.016s
    if (time_diff > 0.016 || time_diff < -0.016) {
        if (time_diff > 0.016) {
            sync_delay_time = sync_delay_time * 2 / 3;
        } else if (time_diff < -0.016) {
            sync_delay_time = sync_delay_time * 3 / 2;
        }
        // 第二次控制，defaultDelayTime * 2 / 3 ～ defaultDelayTime * 3 / 2
        if (sync_delay_time < default_sync_delay_time / 2) {
            sync_delay_time = default_sync_delay_time * 2 / 3;
        } else if (sync_delay_time > default_sync_delay_time * 2) {
            sync_delay_time = default_sync_delay_time * 3 / 2;
        }
    }
    // 偏差较大
    if (time_diff >= 0.25) {
        sync_delay_time = 0;
    } else if (time_diff <= -0.25) {
        sync_delay_time = default_sync_delay_time * 2;
    } else {
        // nothing
    }
    return sync_delay_time;
}

void SVideoPlayer::release() {
    AbsMedia::release();
    // 移除暂停锁
    pthread_mutex_destroy(&mutex_pause);
    pthread_cond_destroy(&cond_pause);
}
