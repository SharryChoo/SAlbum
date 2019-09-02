
//
// Created by Sharry Choo on 2019-07-08.
//

#include <pthread.h>
#include "AbsMedia.h"

AbsMedia::AbsMedia(JNICall *jniCall, int streamIndex, MediaStatus *status) {
    this->jni_call = jniCall;
    this->stream_index = streamIndex;
    this->status = status;
    this->queue = new MediaPacketQueue(status);
    pthread_mutex_init(&mutex_codec_ctx, NULL);
}

AbsMedia::~AbsMedia() {
    release();
}

// //////////////////////////////// implementation /////////////////////////////////////////

bool AbsMedia::prepare(AVFormatContext *format_ctx) {
    // 查找解码器
    AVCodecParameters *params = format_ctx->streams[stream_index]->codecpar;
    AVCodec *codec = avcodec_find_decoder(params->codec_id);
    if (codec == NULL) {
        callPlayerJniError(-1, "find audio codec error.");
        return false;
    }
    // 打开解码器
    codec_ctx = avcodec_alloc_context3(codec);
    if (codec_ctx == NULL) {
        callPlayerJniError(-1, "open audio context error.");
        return false;
    }
    int paramsToCxt = avcodec_parameters_to_context(codec_ctx, params);
    if (paramsToCxt < 0) {
        callPlayerJniError(paramsToCxt, "param 2 context error");
        return false;
    }
    int codecOpenRes = avcodec_open2(codec_ctx, codec, NULL);
    if (codecOpenRes != 0) {
        callPlayerJniError(codecOpenRes, "codec open error");
        return false;
    }
    total_duration = format_ctx->duration / AV_TIME_BASE;
    time_base = format_ctx->streams[stream_index]->time_base;
    // 初始化实现类
    return prepareInternal(format_ctx);
}

void AbsMedia::seek(AVFormatContext *format_ctx, int seconds) {
    if (total_duration <= 0) {
        return;
    }
    if (seconds >= 0 && seconds < total_duration) {
        // 重置队列和相关变量
        queue->clear();
        time_last_callback = 0;
        time_current = 0;
        // seek 到指定位置
        int64_t timestamp = seconds * AV_TIME_BASE;
        av_seek_frame(format_ctx, -1, timestamp, 0);
//        avformat_seek_file(format_ctx, -1, INT64_MIN, timestamp, INT64_MAX, 0);// 弃用
        // flush 数据流
        pthread_mutex_lock(&mutex_codec_ctx);
        avcodec_flush_buffers(codec_ctx);
        pthread_mutex_unlock(&mutex_codec_ctx);
    }
}

void AbsMedia::release() {
    // 移除缓存队列
    if (queue != NULL) {
        delete (queue);
        queue = NULL;
    }
    if (codec_ctx != NULL) {
        avcodec_close(codec_ctx);
        avcodec_free_context(&codec_ctx);
        codec_ctx = NULL;
        // 移除暂停锁, 不放到外面是防止被 destroy 两次
        pthread_mutex_destroy(&mutex_codec_ctx);
    }
}

void AbsMedia::callPlayerJniError(int code, const char *msg) {
    jni_call->callOnError(code, msg);
}