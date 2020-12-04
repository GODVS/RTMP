package com.example.glivepush;

import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.glivepush.encodec.GMediaEncodec;
import com.example.glivepush.imgvideo.GImgVideoView;
import com.ywl5320.libmusic.WlMusic;
import com.ywl5320.listener.OnPreparedListener;
import com.ywl5320.listener.OnShowPcmDataListener;

public class ImgVideoActivity extends AppCompatActivity {

    private GImgVideoView gImgVideoView;
    private GMediaEncodec gMediaEncodec;
    private WlMusic wlMusic;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imgvideo);
        gImgVideoView = findViewById(R.id.imgVideoView);
        gImgVideoView.setCurrentImg(R.drawable.img_1);

        wlMusic = WlMusic.getInstance();
        wlMusic.setCallBackPcmData(true);
        wlMusic.setOnPreparedListener(new OnPreparedListener() {
            @Override
            public void onPrepared() {
                wlMusic.playCutAudio(39, 60);
            }
        });

        wlMusic.setOnShowPcmDataListener(new OnShowPcmDataListener() {
            @Override
            public void onPcmInfo(int samplerate, int bit, int channels) {
                gMediaEncodec = new GMediaEncodec(ImgVideoActivity.this, gImgVideoView.getFbotextureId());
                gMediaEncodec.initEncodec(gImgVideoView.getEglContext(),
                        Environment.getExternalStorageDirectory().getAbsolutePath() + "/g_img_video.mp4",
                        720, 500, samplerate, channels);
                gMediaEncodec.startRecord();
                startImgs();
            }

            @Override
            public void onPcmData(byte[] pcmdata, int size, long clock) {
                if (gMediaEncodec != null) {
                    gMediaEncodec.putPCMDate(pcmdata, size);
                }
            }
        });
    }

    public void start(View view) {
        wlMusic.setSource(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Music/周大侠-周杰伦.mp3");
        wlMusic.prePared();
    }

    //实现图片渲染
    private void startImgs() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 1; i <= 257; i++) {
                    int imgSrc = getResources().getIdentifier("img_" + i,
                            "drawable", getApplicationContext().getPackageName());
                    if (imgSrc == 0) {
                        Log.d("godv", "图片未找到");
                        break;
                    }
                    gImgVideoView.setCurrentImg(imgSrc);
                    try {
                        Thread.sleep(80);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if (gMediaEncodec != null) {
                    wlMusic.stop();
                    gMediaEncodec.stopRecord();
                    gMediaEncodec = null;
                }
            }
        }).start();
    }
}
