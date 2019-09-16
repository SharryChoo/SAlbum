//
// Created by Sharry Choo on 2019-08-26.
//

#include "RecordBuffer.h"

RecordBuffer::RecordBuffer(int buffer_size) {
    buffer = new short *[2];
    for (int i = 0; i < 2; ++i) {
        buffer[i] = new short[buffer_size];
    }
}

RecordBuffer::~RecordBuffer() {
    for (int i = 0; i < 2; ++i) {
        delete buffer[i];
    }
    delete buffer;
}

short *RecordBuffer::getRecordBuffer() {
    index++;
    if (index > 1) {
        index = 0;
    }
    return buffer[index];
}

short *RecordBuffer::getNowBuffer() {
    return buffer[index];
}


