//
// Created by Sharry Choo on 2019-06-25.
//

#include <pthread.h>
#include "MediaPacketQueue.h"
#include "ConstDefine.h"

MediaPacketQueue::MediaPacketQueue(MediaStatus *status) {
    this->queue = new std::queue<AVPacket *>();
    this->status = status;
    pthread_mutex_init(&mutex_queue, NULL);
    pthread_cond_init(&cond_not_empty, NULL);
    pthread_cond_init(&cond_not_full, NULL);
}

MediaPacketQueue::~MediaPacketQueue() {
    // 清理数据
    clear();
    // 销毁锁和条件变量
    pthread_mutex_destroy(&mutex_queue);
    pthread_cond_destroy(&cond_not_empty);
    pthread_cond_destroy(&cond_not_full);
    // 删除队列
    delete (queue);
    queue = NULL;
}

// 在 Decode 线程调用
void MediaPacketQueue::push(AVPacket *newPacket) {
    pthread_mutex_lock(&mutex_queue);
    while (status->playing && queue->size() >= MAX_QUEUE_CACHE_SIZE) {
        pthread_cond_wait(&cond_not_full, &mutex_queue);
    }
    queue->push(newPacket);
    pthread_cond_signal(&cond_not_empty);
    pthread_mutex_unlock(&mutex_queue);
}

// 在音视频 play 线程调用
AVPacket *MediaPacketQueue::pop() {
    AVPacket *res = NULL;
    pthread_mutex_lock(&mutex_queue);
    // MESA 管程规范
    while (status->playing && queue->empty()) {
        pthread_cond_wait(&cond_not_empty, &mutex_queue);
    }
    if (!queue->empty()) {
        res = queue->front();
        queue->pop();
    }
    pthread_cond_signal(&cond_not_full);
    pthread_mutex_unlock(&mutex_queue);
    return res;
}

void MediaPacketQueue::clear() {
    // 清除缓存队列
    pthread_mutex_lock(&mutex_queue);
    while (!queue->empty()) {
        AVPacket *front = queue->front();
        queue->pop();
        // 解引用
        av_packet_unref(front);
        // 释放内存
        av_packet_free(&front);
    }
    pthread_cond_signal(&cond_not_full);
    pthread_mutex_unlock(&mutex_queue);
}

int MediaPacketQueue::size() {
    int res = 0;
    pthread_mutex_lock(&mutex_queue);
    res = queue->size();
    pthread_mutex_unlock(&mutex_queue);
    return res;
}

void MediaPacketQueue::notifyAll() {
    pthread_cond_signal(&cond_not_empty);
    pthread_cond_signal(&cond_not_full);
}
