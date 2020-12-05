#include "GCallJava.h"

GCallJava::GCallJava(JavaVM *javaVm, JNIEnv *jniEnv, jobject *jobj) {
    this->javaVm = javaVm;
    this->jniEnv = jniEnv;
    this->jobj = jniEnv->NewGlobalRef(*jobj);

    jclass jlz = jniEnv->GetObjectClass(this->jobj);

    jmid_connecting = jniEnv->GetMethodID(jlz, "onConnecting", "()V");
    jmid_connectsuccess = jniEnv->GetMethodID(jlz, "onConnectSuccess", "()V");
    jmid_connectfail = jniEnv->GetMethodID(jlz, "onConnectFail", "(Ljava/lang/String;)V");
}

GCallJava::~GCallJava() {
    jniEnv->DeleteGlobalRef(jobj);
    javaVm = NULL;
    jniEnv = NULL;
}

void GCallJava::onConnecting(int type) {
    if (type == G_THREAD_CHILD) {
        JNIEnv *jniEnv;
        if (javaVm->AttachCurrentThread(&jniEnv, 0) != JNI_OK) {
            return;
        }
        jniEnv->CallVoidMethod(jobj, jmid_connecting);
        javaVm->DetachCurrentThread();

    } else {
        jniEnv->CallVoidMethod(jobj, jmid_connecting);
    }
}

void GCallJava::onConnectSuccess() {
    JNIEnv *jniEnv;
    if (javaVm->AttachCurrentThread(&jniEnv, 0) != JNI_OK) {
        return;
    }
    jniEnv->CallVoidMethod(jobj, jmid_connectsuccess);
    javaVm->DetachCurrentThread();
}

void GCallJava::onConnectFail(char *msg) {
    JNIEnv *jniEnv;
    if (javaVm->AttachCurrentThread(&jniEnv, 0) != JNI_OK) {
        return;
    }
    jstring jmsg = jniEnv->NewStringUTF(msg);
    jniEnv->CallVoidMethod(jobj, jmid_connectfail, jmsg);
    jniEnv->DeleteLocalRef(jmsg);
    javaVm->DetachCurrentThread();

}


