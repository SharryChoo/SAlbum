//
// Created by Sharry Choo on 2019-08-26.
//

#ifndef SMEDIA_RECORDBUFFER_H
#define SMEDIA_RECORDBUFFER_H


#include <stdint.h>

class RecordBuffer {

public:
    short **buffer;
    int index = -1;
public:
    RecordBuffer(int buffer_size);

    ~RecordBuffer();

public:
    short *getRecordBuffer();

    short *getNowBuffer();

};


#endif //SMEDIA_RECORDBUFFER_H
