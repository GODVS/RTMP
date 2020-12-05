#ifndef RTMPSUC_GCALLJAVA_H
#define RTMPSUC_GCALLJAVA_H

#include "jni.h"

#define G_THREAD_MAIN 1
#define G_THREAD_CHILD 2

class GCallJava {

public:
    JNIEnv *jniEnv = NULL;
    JavaVM *javaVm = NULL;

    jobject jobj;
    jmethodID jmid_connecting;
    jmethodID jmid_connectsuccess;
    jmethodID jmid_connectfail;

public:
    GCallJava(JavaVM *javaVm, JNIEnv *jniEnv, jobject *jobj);

    ~GCallJava();

    void onConnecting(int type);

    void onConnectSuccess();

    void onConnectFail(char *msg);
};

#endif //RTMPSUC_GCALLJAVA_H
