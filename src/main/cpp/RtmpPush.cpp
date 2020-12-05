#include "RtmpPush.h"

RtmpPush::RtmpPush(const char *url, GCallJava *gCallJava) {
    this->url = static_cast<char *>(malloc(512));
    strcpy(this->url, url);
    this->queue = new GQueue();

    this->gCallJava = gCallJava;
}

RtmpPush::~RtmpPush() {
    queue->notifyQueue();
    queue->clearQueue();
    free(url);
}

void *callBackPush(void *data) {
    RtmpPush *rtmpPush = static_cast<RtmpPush *>(data);

    rtmpPush->gCallJava->onConnecting(G_THREAD_CHILD);

    rtmpPush->rtmp = RTMP_Alloc();    //分配空间
    RTMP_Init(rtmpPush->rtmp);        //初始化
    rtmpPush->rtmp->Link.timeout = 10;   //设置超时时间
    rtmpPush->rtmp->Link.lFlags |= RTMP_LF_LIVE;  //追加直播
    RTMP_SetupURL(rtmpPush->rtmp, rtmpPush->url);    //设置推流URL
    RTMP_EnableWrite(rtmpPush->rtmp);    //设置可写状态
    if (!RTMP_Connect(rtmpPush->rtmp, NULL)) {    //链接服务器  0失败
//        LOGE("can not connect the url %s", rtmpPush->url);
        rtmpPush->gCallJava->onConnectFail("can not connect the url");

        goto end;
    }
    if (!RTMP_ConnectStream(rtmpPush->rtmp, 0)) {   //链接流  0失败
        rtmpPush->gCallJava->onConnectFail("can not connect the stream of the service");
        goto end;
    }
    //推流

    rtmpPush->gCallJava->onConnectSuccess();

    end:
    RTMP_Close(rtmpPush->rtmp);
    RTMP_Free(rtmpPush->rtmp);
    rtmpPush->rtmp = NULL;
    pthread_exit(&rtmpPush->push_thread);
}

void RtmpPush::init() {

    //gCallJava->onConnecting(G_THREAD_MAIN);
    pthread_create(&push_thread, NULL, callBackPush, this);
}

