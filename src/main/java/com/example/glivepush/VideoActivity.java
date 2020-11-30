package com.example.glivepush;

import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.glivepush.camera.GCameraView;
import com.example.glivepush.encodec.GBaseMediaEncoder;
import com.example.glivepush.encodec.GMediaEncodec;
import com.ywl5320.libmusic.WlMusic;
import com.ywl5320.listener.OnCompleteListener;
import com.ywl5320.listener.OnPreparedListener;
import com.ywl5320.listener.OnShowPcmDataListener;

public class VideoActivity extends AppCompatActivity {

    private GCameraView gCameraView;
    private Button btnRecord;

    private GMediaEncodec gMediaEncodec;

    private WlMusic wlMusic;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        gCameraView = findViewById(R.id.cameraView);
        btnRecord = findViewById(R.id.btn_record);

        wlMusic = WlMusic.getInstance();
        wlMusic.setCallBackPcmData(true);

        wlMusic.setOnPreparedListener(new OnPreparedListener() {
            @Override
            public void onPrepared() {
                wlMusic.playCutAudio(39, 60);
            }
        });
        wlMusic.setOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete() {
                if (gMediaEncodec != null) {
                    gMediaEncodec.stopRecord();
                    gMediaEncodec = null;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            btnRecord.setText("开始录制");
                        }
                    });
                }
            }
        });

        wlMusic.setOnShowPcmDataListener(new OnShowPcmDataListener() {
            @Override
            public void onPcmInfo(int samplerate, int bit, int channels) {
                //初始化GMediaEncodec
                //MediaFormat.MIMETYPE_VIDEO_AVC      h264
                gMediaEncodec = new GMediaEncodec(VideoActivity.this, gCameraView.getTextureId());
                gMediaEncodec.initEncodec(gCameraView.getEglContext(),
                        Environment.getExternalStorageDirectory().getAbsolutePath() + "/g_live_pusher.mp4",
                        720, 1280, samplerate, channels);
                gMediaEncodec.setOnMediaInfoListener(new GBaseMediaEncoder.OnMediaInfoListener() {
                    @Override
                    public void onMediaTime(int times) {
                        Log.i("godv", "time is " + times);
                    }
                });

                gMediaEncodec.startRecord();
            }

            @Override
            public void onPcmData(byte[] pcmdata, int size, long clock) {
                if (gMediaEncodec != null) {
                    gMediaEncodec.putPCMDate(pcmdata, size);
                }
            }
        });
    }

    public void record(View view) {
        if (gMediaEncodec == null) {
            wlMusic.setSource(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Music/周大侠-周杰伦.mp3");
            wlMusic.prePared();
            btnRecord.setText("正在录制");
        } else {
            gMediaEncodec.stopRecord();
            btnRecord.setText("开始录制");
            gMediaEncodec = null;
            wlMusic.stop();
        }
    }
}
