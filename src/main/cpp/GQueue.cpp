#include "GQueue.h"

GQueue::GQueue() {
    //初始化
    pthread_mutex_init(&mutexPacket, NULL);
    pthread_cond_init(&condPacket, NULL);
}

GQueue::~GQueue() {
    clearQueue();
    //销毁线程锁
    pthread_mutex_destroy(&mutexPacket);
    //销毁条件变量
    pthread_cond_destroy(&condPacket);
}

int GQueue::putRtmpPacket(RTMPPacket *packet) {
    pthread_mutex_lock(&mutexPacket);
    queuePacket.push(packet);
    //通知
    pthread_cond_signal(&condPacket);
    pthread_mutex_unlock(&mutexPacket);
    return 0;
}

RTMPPacket *GQueue::getRtmpPacket() {
    pthread_mutex_lock(&mutexPacket);
    RTMPPacket *p = NULL;
    if (!queuePacket.empty()) {
        p = queuePacket.front();
        queuePacket.pop();
    } else {
        pthread_cond_wait(&condPacket, &mutexPacket);
    }
    pthread_mutex_unlock(&mutexPacket);
    return p;
}

void GQueue::clearQueue() {
    pthread_mutex_lock(&mutexPacket);
    while (true) {
        if (queuePacket.empty()) {
            break;
        }
        RTMPPacket *p = queuePacket.front();
        queuePacket.pop();
        RTMPPacket_Free(p);
        p = NULL;
    }
    pthread_mutex_unlock(&mutexPacket);
}

void GQueue::notifyQueue() {
    pthread_mutex_lock(&mutexPacket);
    pthread_cond_signal(&condPacket);
    pthread_mutex_unlock(&mutexPacket);
}
