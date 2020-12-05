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

public:

    RtmpPush(const char *url, GCallJava *gCallJava);

    ~RtmpPush();

    void init();

};

#endif //RTMPSUC_RTMPPUSH_H
