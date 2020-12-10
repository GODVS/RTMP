package com.example.glivepush.push;

public interface GConnectListener {
    // 回调方法
    void onConnecting();

    void onConnectSuccess();

    void onConnectFail(String msg);
}
