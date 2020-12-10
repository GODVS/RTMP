#ifndef RTMPSUC_RTMPPUSH_H
#define RTMPSUC_RTMPPUSH_H

#include <malloc.h>
#include <cstring>
#include "GQueue.h"
#include "pthread.h"
#include "GCallJava.h"

extern "C" {
#include "librtmp/rtmp.h"
};

class RtmpPush {
public:
    RTMP *rtmp = NULL;
    char *url = NULL;
    GQueue *queue = NULL;

    pthread_t push_thread;

    GCallJava *gCallJava = NULL;

    bool startPushing = false;

    long startTime = 0;

public:

    RtmpPush(const char *url, GCallJava *gCallJava);

    ~RtmpPush();

    void init();


    //发送sps pps
    void pushSPSPPS(char *sps, int sps_len, char *pps, int pps_len);

    //发送帧数据
    void pushVideoData(char *data, int data_len, bool keyFrame);

    /*************************************直播推流-video-start***********************************/
    void pushAudioData(char *data, int data_len);

    void pushStop();
    /*************************************直播推流-video-end***********************************/
};

#endif //RTMPSUC_RTMPPUSH_H
