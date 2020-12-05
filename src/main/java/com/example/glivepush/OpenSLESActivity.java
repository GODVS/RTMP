package com.example.glivepush;

import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class OpenSLESActivity extends AppCompatActivity {

    static {
        System.loadLibrary("gpush");
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_sles);
    }

    public native void startRecord(String path);

    public native void stopRecord();

    public void startRecord(View view) {
        startRecord(Environment.getExternalStorageDirectory().getAbsolutePath() + "/g_opensl_record.pcm");
    }

    public void stopRecord(View view) {
        stopRecord();
    }
}
