package com.example.glivepush;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.glivepush.audioRcord.AudioRecordUtil;

public class AudioRecordActivity extends AppCompatActivity {

    private AudioRecordUtil audioRecordUtil;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_record);
    }

    public void record(View view) {
        if (audioRecordUtil == null) {
            audioRecordUtil = new AudioRecordUtil();
            audioRecordUtil.setOnRecordListener(new AudioRecordUtil.OnRecordListener() {
                @Override
                public void recordByte(byte[] audioData, int readSize) {
                    Log.d("godv", "readSize is : " + readSize);
                }
            });
            audioRecordUtil.startRecord();
        } else {
            audioRecordUtil.stopRecord();
            audioRecordUtil = null;
        }
    }
}
