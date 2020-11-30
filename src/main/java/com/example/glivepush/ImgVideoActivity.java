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
import com.example.glivepush.imgvideo.GImgVideoView;

public class ImgVideoActivity extends AppCompatActivity {
    private GImgVideoView gImgVideoView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imgvideo);
        gImgVideoView = findViewById(R.id.imgVideoView);

    }

    public void start(View view) {
        gImgVideoView.setCurrentImg();
    }
}
