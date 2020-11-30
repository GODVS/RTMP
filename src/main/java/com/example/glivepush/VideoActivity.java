package com.example.glivepush;

import android.media.MediaFormat;
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

public class VideoActivity extends AppCompatActivity {

    private GCameraView gCameraView;
    private Button btnRecord;

    private GMediaEncodec gMediaEncodec;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        gCameraView = findViewById(R.id.cameraView);
        btnRecord = findViewById(R.id.btn_record);
    }

    public void record(View view) {
        if(gMediaEncodec == null){
//            MediaFormat.MIMETYPE_VIDEO_AVC      h264
            gMediaEncodec = new GMediaEncodec(this, gCameraView.getTextureId());
            gMediaEncodec.initEncodec(gCameraView.getEglContext(),
                    Environment.getExternalStorageDirectory().getAbsolutePath() + "/g_live_pusher.mp4",
                    MediaFormat.MIMETYPE_VIDEO_AVC, 720, 1280);
            gMediaEncodec.setOnMediaInfoListener(new GBaseMediaEncoder.OnMediaInfoListener() {
                @Override
                public void onMediaTime(int times) {
                    Log.i("godv", times + "");
                }
            });
            gMediaEncodec.startRecord();
            btnRecord.setText("正在录制");
        } else {
            gMediaEncodec.stopRecord();
            btnRecord.setText("开始录制");
            gMediaEncodec = null;
        }
    }
}
