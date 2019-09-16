//
// Created by Sharry on 2019-08-26.
//

#include <cassert>
#include <pthread.h>
#include "OpenSLRecorder.h"
#include "ConstDefine.h"

OpenSLRecorder::OpenSLRecorder(JNICall *jni_call) {
    this->jni_call = jni_call;
    this->buffer = new RecordBuffer(RECORD_BUFFER_SIZE);
    pthread_mutex_init(&mutex_cond_request_stop, NULL);
    pthread_cond_init(&cond_request_stop, NULL);
}

OpenSLRecorder::~OpenSLRecorder() {
    if (buffer != NULL) {
        delete buffer;
        buffer = NULL;
        pthread_mutex_destroy(&mutex_cond_request_stop);
        pthread_cond_destroy(&cond_request_stop);
    }
    if (sl_obj_recorder != NULL) {
        (*sl_obj_recorder)->Destroy(sl_obj_recorder);
        sl_obj_recorder = NULL;
        sl_itf_recorder = NULL;
        sl_itf_record_buffer_queue = NULL;
    }
    if (sl_obj_engine != NULL) {
        (*sl_obj_engine)->Destroy(sl_obj_engine);
        sl_obj_engine = NULL;
        sl_itf_engine = NULL;
    }
}

void recordCallback(SLAndroidSimpleBufferQueueItf caller, void *context) {
    // 从队列中取数据播放......
    OpenSLRecorder *impl = static_cast<OpenSLRecorder *>(context);
    if (impl->requestStop) {
        // 停止 OpenSL ES 的录制
        (*impl->sl_itf_recorder)->SetRecordState(impl->sl_itf_recorder, SL_RECORDSTATE_STOPPED);
        // 当前已经停止, 此次请求完毕
        impl->requestStop = false;
        // 唤醒正在请求停止的线程
        pthread_cond_signal(&impl->cond_request_stop);
    } else {
        // 通过 JNI 将数据回调到 Java 层
        impl->jni_call->callOnPCMChanged(reinterpret_cast<uint8_t *>(impl->buffer->getNowBuffer()),
                                         RECORD_BUFFER_SIZE);
        // 将下一个 buffer 入队列
        (*caller)->Enqueue(caller, impl->buffer->getRecordBuffer(), RECORD_BUFFER_SIZE);
    }
}

void *openSLESRecord(void *context) {
    OpenSLRecorder *impl = static_cast<OpenSLRecorder *>(context);
    // 初始化
    impl->initOpenSLES();
    // 设置为开始录制状态
    (*impl->sl_itf_recorder)->SetRecordState(impl->sl_itf_recorder, SL_RECORDSTATE_RECORDING);
    return 0;
}

void OpenSLRecorder::start() {
    pthread_create(&thread_opensl_es_recode, NULL, openSLESRecord, this);
}

void OpenSLRecorder::pause() {
    if (sl_itf_recorder != NULL) {
        (*sl_itf_recorder)->SetRecordState(sl_itf_recorder, SL_RECORDSTATE_PAUSED);
    }
}

void OpenSLRecorder::resume() {
    if (sl_itf_recorder != NULL) {
        (*sl_itf_recorder)->SetRecordState(sl_itf_recorder, SL_RECORDSTATE_RECORDING);
    }
}

void OpenSLRecorder::stop() {
    // 若当前正在请求停止, 则无需再次请求
    if (requestStop) {
        return;
    }
    // 标记为正在请求停止
    requestStop = true;
    pthread_mutex_lock(&mutex_cond_request_stop);
    while (requestStop) {
        // looper waiting opensl es stopped
        pthread_cond_wait(&cond_request_stop, &mutex_cond_request_stop);
    }
    pthread_mutex_unlock(&mutex_cond_request_stop);
}

void OpenSLRecorder::initOpenSLES() {
    int result;
    /// 创建 OpenSL 引擎
    slCreateEngine(&sl_obj_engine, 0, NULL, 0, NULL, NULL);
    result = (*sl_obj_engine)->Realize(sl_obj_engine, SL_BOOLEAN_FALSE);
    assert(SL_RESULT_SUCCESS == result);
    result = (*sl_obj_engine)->GetInterface(sl_obj_engine, SL_IID_ENGINE, &sl_itf_engine);
    assert(SL_RESULT_SUCCESS == result);

    // 创建 pAudioSrc
    SLDataLocator_IODevice loc_dev = {SL_DATALOCATOR_IODEVICE, SL_IODEVICE_AUDIOINPUT,
                                      SL_DEFAULTDEVICEID_AUDIOINPUT, NULL};
    SLDataSource audio_src = {&loc_dev, NULL};
    // 创建 pAudioSnk
    SLDataLocator_AndroidSimpleBufferQueue loc_bq = {SL_DATALOCATOR_ANDROIDSIMPLEBUFFERQUEUE, 2};
    SLDataFormat_PCM format_pcm = {
            SL_DATAFORMAT_PCM,
            2,                                                          // 通道数
            SL_SAMPLINGRATE_44_1,                                       // 采样率
            SL_PCMSAMPLEFORMAT_FIXED_16, SL_PCMSAMPLEFORMAT_FIXED_16,
            SL_SPEAKER_FRONT_LEFT | SL_SPEAKER_FRONT_RIGHT,             // 通道布局
            SL_BYTEORDER_LITTLEENDIAN                                   // 对其方式
    };
    SLDataSink audio_snk = {&loc_bq, &format_pcm};
    // 创建 numInterfaces
    SLInterfaceID itf_ids[1] = {SL_IID_ANDROIDSIMPLEBUFFERQUEUE};
    SLboolean itf_req[1] = {SL_BOOLEAN_TRUE};
    /// 创建并实现 OpenSL 录制器
    result = (*sl_itf_engine)->CreateAudioRecorder(sl_itf_engine, &sl_obj_recorder, &audio_src,
                                                   &audio_snk, 1, itf_ids, itf_req);
    assert(SL_RESULT_SUCCESS == result);
    (*sl_obj_recorder)->Realize(sl_obj_recorder, SL_BOOLEAN_FALSE);

    /// 获取相关接口
    // 获取录制接口
    (*sl_obj_recorder)->GetInterface(sl_obj_recorder, SL_IID_RECORD, &sl_itf_recorder);
    // 获取缓冲队列接口
    (*sl_obj_recorder)->GetInterface(sl_obj_recorder, SL_IID_ANDROIDSIMPLEBUFFERQUEUE,
                                     &sl_itf_record_buffer_queue);
    /// 设置队列与回调
    (*sl_itf_record_buffer_queue)->RegisterCallback(sl_itf_record_buffer_queue, recordCallback,
                                                    this);
    // 主动回调一次, 后面会自动开启录制
    (*sl_itf_record_buffer_queue)->Enqueue(sl_itf_record_buffer_queue, buffer->getRecordBuffer(),
                                           RECORD_BUFFER_SIZE);
}