#ifndef RTMPSUC_GQUEUE_H
#define RTMPSUC_GQUEUE_H

#include "queue"
#include "pthread.h"
#include "AndroidLog.h"

//导入RTMPPacket
extern "C" {
#include "librtmp/rtmp.h"
};

class GQueue {
public:
    std::queue<RTMPPacket *> queuePacket;
    //线程锁
    pthread_mutex_t mutexPacket;
    //条件变量
    pthread_cond_t condPacket;

public:
    GQueue();

    ~GQueue();

    int putRtmpPacket(RTMPPacket *packet);

    RTMPPacket *getRtmpPacket();

    void clearQueue();

    void notifyQueue();
};


#endif //RTMPSUC_GQUEUE_H
