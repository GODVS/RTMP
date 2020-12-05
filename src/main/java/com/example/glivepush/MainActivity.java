package com.example.glivepush;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.AudioRecord;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void cameraPreview(View view) {
        Intent intent = new Intent(this, CameraActivity.class);
        startActivity(intent);
    }

    public void videoRecord(View view) {
        Intent intent = new Intent(this, VideoActivity.class);
        startActivity(intent);
    }

    //图片生成视频
    public void imgVideo(View view) {
        Intent intent = new Intent(this, ImgVideoActivity.class);
        startActivity(intent);
    }

    public void yuvPlayer(View view) {
        Intent intent = new Intent(this, YuvActivity.class);
        startActivity(intent);
    }

    public void audioRecord(View view) {
        Intent intent = new Intent(this, AudioRecordActivity.class);
        startActivity(intent);
    }

    public void SLESRecord(View view) {
        Intent intent = new Intent(this, OpenSLESActivity.class);
        startActivity(intent);
    }

    public void livePush(View view) {
        Intent intent = new Intent(this, LivePushActivity.class);
        startActivity(intent);
    }
}
