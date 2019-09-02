//
// Created by Sharry on 2019-08-26.
//

#ifndef SMEDIA_OPENSLRECORDER_H
#define SMEDIA_OPENSLRECORDER_H

#include <SLES/OpenSLES.h>
#include <SLES/OpenSLES_Android.h>
#include <sys/types.h>
#include "RecordBuffer.h"
#include "JNICall.h"

class OpenSLRecorder {

public:
    JNICall *jni_call;
    RecordBuffer *buffer;
    pthread_t thread_opensl_es_recode;

    /**
     * 用于安全 Stop 的锁与条件变量
     */
    bool requestStop = false;
    pthread_mutex_t mutex_cond_request_stop;
    pthread_cond_t cond_request_stop;


    /**
     * OpenSL ES 相关变量
     */
    SLObjectItf sl_obj_engine = NULL;
    SLEngineItf sl_itf_engine = NULL;

    SLObjectItf sl_obj_recorder = NULL;
    SLRecordItf sl_itf_recorder = NULL;
    SLAndroidSimpleBufferQueueItf sl_itf_record_buffer_queue = NULL;

    OpenSLRecorder(JNICall *jni_call);

    ~OpenSLRecorder();

    void start();

    void pause();

    void resume();

    void stop();

    void initOpenSLES();
};


#endif //SMEDIA_OPENSLRECORDER_H
