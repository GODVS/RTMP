#include <jni.h>
#include <string>

#include <SLES/OpenSLES.h>
#include <SLES/OpenSLES_Android.h>

#include "RecordBuffer.h"
#include "AndroidLog.h"

SLObjectItf slObjectEngine = NULL;
SLEngineItf engineItf = NULL;

//录音器
SLObjectItf recordObj = NULL;
SLRecordItf recordItf = NULL;

SLAndroidSimpleBufferQueueItf recorderBufferQueue = NULL;

//声明队列
RecordBuffer *recordBuffer;

//文件
FILE *pcmFile = NULL;
bool finish = false;

//回调方法
void bqRecorderCallback(SLAndroidSimpleBufferQueueItf bq, void *context) {
    fwrite(recordBuffer->getNowBuffer(), 1, 4096, pcmFile);
    if (finish) {
        LOGE("录制完成");
        //改变状态
        (*recordItf)->SetRecordState(recordItf, SL_RECORDSTATE_STOPPED);
        (*recordObj)->Destroy(recordObj);
        recordObj = NULL;
        recordItf = NULL;
        (*slObjectEngine)->Destroy(slObjectEngine);
        slObjectEngine = NULL;
        engineItf = NULL;
        delete (recordBuffer);
    } else {
        LOGE("正在录制");
        (*recorderBufferQueue)->Enqueue(recorderBufferQueue, recordBuffer->getRecordBuffer(), 4096);
    }
}


extern "C"
JNIEXPORT void JNICALL
Java_com_example_glivepush_OpenSLESActivity_startRecord(JNIEnv *env, jobject thiz, jstring path_) {
    const char *path = env->GetStringUTFChars(path_, 0);

    recordBuffer = new RecordBuffer(4096);
    pcmFile = fopen(path, "w");
    finish = false;

    //创建引擎
    slCreateEngine(&slObjectEngine, 0, NULL, 0, NULL, NULL);
    //初始化
    (*slObjectEngine)->Realize(slObjectEngine, SL_BOOLEAN_FALSE);
    //得到
    (*slObjectEngine)->GetInterface(slObjectEngine, SL_IID_ENGINE, &engineItf);


    //设备
    SLDataLocator_IODevice loc_dev = {
            SL_DATALOCATOR_IODEVICE,
            SL_IODEVICE_AUDIOINPUT,
            SL_DEFAULTDEVICEID_AUDIOINPUT,
            NULL
    };
    SLDataSource audioSrc = {&loc_dev, NULL};

    //队列
    SLDataLocator_AndroidSimpleBufferQueue loc_bq = {
            SL_DATALOCATOR_ANDROIDSIMPLEBUFFERQUEUE,
            2
    };

    //1 格式 2 声道数 3 采样率 4 位数 5 位数 6 通道布局（立体） 7 对齐
    SLDataFormat_PCM format_pcm = {
            SL_DATAFORMAT_PCM, 2, SL_SAMPLINGRATE_44_1,
            SL_PCMSAMPLEFORMAT_FIXED_16, SL_PCMSAMPLEFORMAT_FIXED_16,
            SL_SPEAKER_FRONT_LEFT | SL_SPEAKER_FRONT_RIGHT, SL_BYTEORDER_LITTLEENDIAN
    };

    SLDataSink audioSink = {
            &loc_bq,
            &format_pcm
    };

    const SLInterfaceID id[1] = {SL_IID_ANDROIDSIMPLEBUFFERQUEUE};
    const SLboolean req[1] = {SL_BOOLEAN_TRUE};

    //创建录音器
    (*engineItf)->CreateAudioRecorder(engineItf, &recordObj, &audioSrc, &audioSink, 1, id, req);
    //初始化
    (*recordObj)->Realize(recordObj, SL_BOOLEAN_FALSE);
    //实例化
    (*recordObj)->GetInterface(recordObj, SL_IID_RECORD, &recordItf);

    (*recordObj)->GetInterface(recordObj, SL_IID_ANDROIDSIMPLEBUFFERQUEUE, &recorderBufferQueue);

    //入队
    (*recorderBufferQueue)->Enqueue(recorderBufferQueue, recordBuffer->getRecordBuffer(), 4096);
    //回调
    (*recorderBufferQueue)->RegisterCallback(recorderBufferQueue, bqRecorderCallback, NULL);

    //声明当前录制状态 开始录制
    (*recordItf)->SetRecordState(recordItf, SL_RECORDSTATE_RECORDING);
}
extern "C"
JNIEXPORT void JNICALL
Java_com_example_glivepush_OpenSLESActivity_stopRecord(JNIEnv *env, jobject thiz) {
    // TODO: implement stopRecord()
    finish = true;
}