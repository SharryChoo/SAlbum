//
// Created by Sharry Choo on 2019-06-25.
//

#ifndef SMEDIA_PLAYER_MEDIASTATUS_H
#define SMEDIA_PLAYER_MEDIASTATUS_H


class MediaStatus {

public:
    /**
     * 是否处于播放状态
     */
    bool playing = false;

    /**
     * 是否处于暂停状态
     */
    bool pausing = false;

    /**
     * 是否正在 seeking
     */
    bool seeking = false;

    MediaStatus();
};


#endif //SMEDIA_PLAYER_MEDIASTATUS_H
