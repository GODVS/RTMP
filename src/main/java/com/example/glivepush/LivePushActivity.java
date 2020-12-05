package com.example.glivepush;

import android.os.Bundle;
import android.os.Environment;
import android.se.omapi.SEService;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.glivepush.camera.GCameraView;
import com.example.glivepush.push.GConnectListener;
import com.example.glivepush.push.PushVideo;

public class LivePushActivity extends AppCompatActivity {

    private PushVideo pushVideo;
    private GCameraView gCameraView;
    private boolean start = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_push);
        pushVideo = new PushVideo();
        gCameraView = findViewById(R.id.cameraView);

        pushVideo.setgConnectListener(new GConnectListener() {
            @Override
            public void onConnecting() {
                Log.d("godv", "链接服务器中");
            }

            @Override
            public void onConnectSuccess() {
                Log.d("godv", "链接服务器成功");
            }

            @Override
            public void onConnectFail(String msg) {
                Log.d("godv", msg);
            }
        });
    }

    public void startPush(View view) {
        start = !start;
        if (start) {
            pushVideo.initLivePush("rtmp://192.168.0.14/myapp/mystream");
        } else {

        }
    }
}
