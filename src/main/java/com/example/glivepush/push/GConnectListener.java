package com.example.glivepush.push;

public interface GConnectListener {
    //
    void onConnecting();

    void onConnectSuccess();

    void onConnectFail(String msg);
}
