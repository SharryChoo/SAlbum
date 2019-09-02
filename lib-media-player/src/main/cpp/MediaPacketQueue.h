//
// Created by Sharry Choo on 2019-06-25.
//

#ifndef SMEDIA_PLAYER_MEDIAPACKETQUEUE_H
#define SMEDIA_PLAYER_MEDIAPACKETQUEUE_H


#include <queue>
#include "MediaStatus.h"

extern "C" {
#include "libavformat/avformat.h"
#include "libswscale/swscale.h"
#include "libswresample/swresample.h"
#include "libavutil/imgutils.h"
}

class MediaPacketQueue {

public:

    std::queue<AVPacket *> *queue;
    pthread_mutex_t mutex_queue;
    pthread_cond_t cond_not_empty;
    pthread_cond_t cond_not_full;
    MediaStatus *status;

    MediaPacketQueue(MediaStatus *status);

    ~MediaPacketQueue();

public:
    /**
     * 压栈
     */
    void push(AVPacket *newPacket);

    /**
     * 弹出栈顶元素, 若无栈顶元素, 会阻塞当前线程
     *
     * 解除阻塞时机:
     * 1. 栈中 push 了一条数据数据
     * 2. notify 调用, 会解除阻塞, 数据可能为 NULL
     *
     */
    AVPacket *pop();

    /**
     * 清除队列
     */
    void clear();

    /**
     * 获取队列中的数据量
     */
    int size();

    /**
     * 通知解除阻塞
     */
    void notifyAll();
};


#endif //SMEDIA_PLAYER_MEDIAPACKETQUEUE_H
