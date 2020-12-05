#include <jni.h>
#include <string>
#include "RtmpPush.h"
#include "GCallJava.h"

GCallJava *gCallJava = NULL;
JavaVM *javaVm = NULL;

RtmpPush *rtmpPush = NULL;
extern "C"
JNIEXPORT void JNICALL
Java_com_example_glivepush_push_PushVideo_initPush(JNIEnv *env, jobject thiz, jstring pushUrl_) {
    // TODO: implement initPush()
    const char *pushUrl = env->GetStringUTFChars(pushUrl_, 0);

    gCallJava = new GCallJava(javaVm, env, &thiz);
    rtmpPush = new RtmpPush(pushUrl, gCallJava);

    rtmpPush->init();

    env->ReleaseStringUTFChars(pushUrl_, pushUrl);
}

extern "C"
JNIEXPORT jint JNICALL
JNI_OnLoad(JavaVM *vm, void *reserved) {
    JNIEnv *env;
    javaVm = vm;
    if (vm->GetEnv((void **) &env, JNI_VERSION_1_6) != JNI_OK) {
        return -1;
    }
    return JNI_VERSION_1_6;
}

extern "C"
JNIEXPORT void JNICALL
JNI_OnUnload(JavaVM *vm, void *reserved) {
    javaVm = NULL;
}
