package com.example.glivepush;

import android.os.Bundle;
import android.os.Environment;
import android.se.omapi.SEService;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.glivepush.camera.GCameraView;
import com.example.glivepush.push.BasePushEncoder;
import com.example.glivepush.push.GConnectListener;
import com.example.glivepush.push.PushEncodec;
import com.example.glivepush.push.PushVideo;
import com.example.glivepush.util.DisplayUtil;

public class LivePushActivity extends AppCompatActivity {

    private PushVideo pushVideo;

    private GCameraView gCameraView;
    private boolean start = false;
    private PushEncodec pushEncodec;

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

                pushEncodec = new PushEncodec(LivePushActivity.this, gCameraView.getTextureId());
                pushEncodec.initEncodec(
                        gCameraView.getEglContext(),
                        DisplayUtil.getScreenWidth(LivePushActivity.this),
                        DisplayUtil.getScreenHeight(LivePushActivity.this),
                        44100,
                        2
                );
                pushEncodec.startRecord();

                /*************************************直播推流-audio-start***********************************/
                pushEncodec.setOnMediaInfoListener(new BasePushEncoder.OnMediaInfoListener() {
                    @Override
                    public void onMediaTime(int times) {

                    }

                    @Override
                    public void onSPSPPSInfo(byte[] sps, byte[] pps) {
                        pushVideo.pushSPSPPS(sps, pps);
                    }

                    @Override
                    public void onVideoInfo(byte[] data, boolean keyFrame) {
                        pushVideo.pushVideoData(data, keyFrame);
                    }

                    @Override
                    public void onAudioInfo(byte[] data) {
                        pushVideo.pushAudioData(data);
                    }
                });
                /*************************************直播推流-audio-end***********************************/

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
            if (pushEncodec != null) {
                pushVideo.stopPush();
                pushEncodec.stopRecord();
                pushEncodec = null;
            }
        }
    }
}
