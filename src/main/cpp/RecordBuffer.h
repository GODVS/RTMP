#ifndef RTMPSUC_RECORDBUFFER_H
#define RTMPSUC_RECORDBUFFER_H


class RecordBuffer {
public:
    //二级指针    一维数组
    short **buffer;
    int index = -1;
public:
    RecordBuffer(int bufferSize);

    ~RecordBuffer();

    short *getRecordBuffer();

    short *getNowBuffer();
};


#endif //RTMPSUC_RECORDBUFFER_H
