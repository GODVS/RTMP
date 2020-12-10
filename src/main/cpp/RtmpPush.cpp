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

    /*************************************直播推流-video-start***********************************/
    rtmpPush->startPushing = false;
    /*************************************直播推流-video-end***********************************/

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

    rtmpPush->gCallJava->onConnectSuccess();
    /*************************************直播推流-video-start***********************************/
    //推流
    rtmpPush->startPushing = true;

    rtmpPush->startTime = RTMP_GetTime();

    while (true) {
        if (!rtmpPush->startPushing) {
            break;
        }
        RTMPPacket *packet = NULL;
        packet = rtmpPush->queue->getRtmpPacket();
        if (packet != NULL) {
            int result = RTMP_SendPacket(rtmpPush->rtmp, packet, 1);
//            LOGD("RTMP_SendPacket result is %d", result);
            RTMPPacket_Free(packet);
            packet = NULL;
        }
    }

    /*************************************直播推流-video-end***********************************/

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

//发送sps pps
void RtmpPush::pushSPSPPS(char *sps, int sps_len, char *pps, int pps_len) {
    //增加了额外的16字节的长度    sps/pps 按照关键帧处理
    int bodySize = sps_len + pps_len + 16;
    //初始化rtmpPacket
    RTMPPacket *packet = static_cast<RTMPPacket *>(malloc(sizeof(RTMPPacket)));
    RTMPPacket_Alloc(packet, bodySize);
    RTMPPacket_Reset(packet);

    char *body = packet->m_body;

    int i = 0;
    //0x17关键帧    frame type (4 bit)
    //              CodecID   (4 bit)
    body[i++] = 0x17;

    //fixed     (4 byte)
    body[i++] = 0x00;
    body[i++] = 0x00;
    body[i++] = 0x00;
    body[i++] = 0x00;

    //版本 Profile 兼容性 ProfileLevel
    body[i++] = 0x01;
    body[i++] = sps[1];
    body[i++] = sps[2];
    body[i++] = sps[3];

    //包长数据所使用的字节数
    body[i++] = 0xff;
    //sps个数
    body[i++] = 0xe1;
    //sps长度 (2 byte)
    body[i++] = (sps_len >> 8) & 0xff;
    body[i++] = sps_len & 0xff;
    //sps实际内容
    memcpy(&body[i], sps, sps_len);
    i += sps_len;

    //pps的个数
    body[i++] = 0x01;
    //pps长度 (2 byte)
    body[i++] = (pps_len >> 8) & 0xff;
    body[i++] = pps_len & 0xff;
    //pps实际内容
    memcpy(&body[i], pps, pps_len);

    packet->m_packetType = RTMP_PACKET_TYPE_VIDEO;
    packet->m_nBodySize = bodySize;
    //时间戳
    packet->m_nTimeStamp = 0;
    packet->m_hasAbsTimestamp = 0;

    packet->m_nChannel = 0x04; //音频或者视频
    packet->m_headerType = RTMP_PACKET_SIZE_MEDIUM;
    packet->m_nInfoField2 = rtmp->m_stream_id;

    //进队列
    queue->putRtmpPacket(packet);
}

void RtmpPush::pushVideoData(char *data, int data_len, bool keyFrame) {
    //增加了额外的9字节的长度
    int bodySize = data_len + 9;
    //初始化rtmpPacket
    RTMPPacket *packet = static_cast<RTMPPacket *>(malloc(sizeof(RTMPPacket)));
    RTMPPacket_Alloc(packet, bodySize);
    RTMPPacket_Reset(packet);

    char *body = packet->m_body;
    int i = 0;

    //0x17关键帧    frame type (4 bit)
    //0x27非关键帧  CodecID   (4 bit)
    if (keyFrame) {
        body[i++] = 0x17;
    } else {
        body[i++] = 0x27;
    }

    //fixed     (4 byte) NALU
    body[i++] = 0x01;
    body[i++] = 0x00;
    body[i++] = 0x00;
    body[i++] = 0x00;

    //dataLength : 长度信息（4 byte）
    body[i++] = (data_len >> 24) & 0xff;
    body[i++] = (data_len >> 16) & 0xff;
    body[i++] = (data_len >> 8) & 0xff;
    body[i++] = data_len & 0xff;
    //h264 裸数据
    memcpy(&body[i], data, data_len);

    packet->m_packetType = RTMP_PACKET_TYPE_VIDEO;
    packet->m_nBodySize = bodySize;
    //时间戳
    packet->m_nTimeStamp = RTMP_GetTime() - startTime;
    packet->m_hasAbsTimestamp = 0;

    packet->m_nChannel = 0x04; //音频或者视频
    packet->m_headerType = RTMP_PACKET_SIZE_LARGE;
    packet->m_nInfoField2 = rtmp->m_stream_id;

    //进队列
    queue->putRtmpPacket(packet);
}

/*************************************直播推流-audio-start***********************************/
void RtmpPush::pushAudioData(char *data, int data_len) {
    //增加了额外的2字节的长度
    int bodySize = data_len + 2;
    //初始化rtmpPacket
    RTMPPacket *packet = static_cast<RTMPPacket *>(malloc(sizeof(RTMPPacket)));
    RTMPPacket_Alloc(packet, bodySize);
    RTMPPacket_Reset(packet);
    char *body = packet->m_body;

    body[0] = 0xAF;
    body[1] = 0x01;
    memcpy(&body[2], data, data_len);

    packet->m_packetType = RTMP_PACKET_TYPE_AUDIO;
    packet->m_nBodySize = bodySize;
    //时间戳
    packet->m_nTimeStamp = RTMP_GetTime() - startTime;
    packet->m_hasAbsTimestamp = 0;

    packet->m_nChannel = 0x04; //音频或者视频
    packet->m_headerType = RTMP_PACKET_SIZE_LARGE;
    packet->m_nInfoField2 = rtmp->m_stream_id;

    queue->putRtmpPacket(packet);
}

void RtmpPush::pushStop() {
    startPushing = false;
    queue->notifyQueue();
    //
    pthread_join(push_thread, NULL);
}
/*************************************直播推流-audio-end***********************************/

