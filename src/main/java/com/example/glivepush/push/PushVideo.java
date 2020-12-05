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

    private native void initPush(String pushUrl);
}
