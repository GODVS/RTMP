package com.example.glivepush;

import android.content.res.Configuration;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.glivepush.camera.GCameraView;

public class CameraActivity extends AppCompatActivity {

    private GCameraView gCameraView;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        gCameraView = findViewById(R.id.cameraView);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        gCameraView.onDestory();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        gCameraView.previewAngle(this);
    }
}
