package com.example.glivepush.push;

import android.text.TextUtils;

public class PushVideo {
    static {
        System.loadLibrary("gpush");
    }

    private GConnectListener gConnectListener;

    public void setgConnectListener(GConnectListener gConnectListener) {
        this.gConnectListener = gConnectListener;
    }

    private void onConnecting() {
        if (gConnectListener != null) {
            gConnectListener.onConnecting();
        }
    }

    private void onConnectSuccess() {
        if (gConnectListener != null) {
            gConnectListener.onConnectSuccess();
        }
    }

    private void onConnectFail(String msg) {
        if (gConnectListener != null) {
            gConnectListener.onConnectFail(msg);
        }
    }

    public void initLivePush(String url) {
        if (!TextUtils.isEmpty(url)) {
            initPush(url);
        }
    }


    public void pushSPSPPS(byte[] sps, byte[] pps) {
        if (sps != null && pps != null) {
            pushSPSPPS(sps, sps.length, pps, pps.length);
        }
    }

    public void pushVideoData(byte[] data, boolean ketFrame) {
        if (data != null) {
            pushVideoData(data, data.length, ketFrame);
        }
    }

    /*************************************直播推流-video-start***********************************/
    public void pushAudioData(byte[] data) {
        if (data != null) {
            pushAudioData(data, data.length);
        }
    }

    public void stopPush() {
        pushStop();
    }

    /*************************************直播推流-video-end***********************************/

    private native void initPush(String pushUrl);


    //发送帧数据
    //推流信息
    private native void pushVideoData(byte[] data, int data_len, boolean keyFrame);

    //发送sps pps
    private native void pushSPSPPS(byte[] sps, int sps_len, byte[] pps, int pps_len);

    /*************************************直播推流-audio-end***********************************/
    private native void pushAudioData(byte[] data, int data_len);

    private native void pushStop();

    /*************************************直播推流-audio-start***********************************/
}
