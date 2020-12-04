package com.example.glivepush;

import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.glivepush.yuv.GYuvRender;
import com.example.glivepush.yuv.GYuvView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class YuvActivity extends AppCompatActivity {

    private GYuvView gYuvView;
    private FileInputStream fis;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_yuv);
        gYuvView = findViewById(R.id.yuvView);
    }

    public void readYuv(View view) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    int w = 640;
                    int h = 360;
                    fis = new FileInputStream(new File(Environment.getExternalStorageDirectory()
                            .getAbsolutePath() + "/Music/sintel_640_360.yuv"));
                    //创建y byte
                    byte[] y = new byte[w * h];
                    byte[] u = new byte[w * h / 4];
                    byte[] v = new byte[w * h / 4];

                    while (true) {
                        int ry = fis.read(y);
                        int ru = fis.read(u);
                        int rv = fis.read(v);
                        if (ry > 0 && ru > 0 && rv > 0) {
                            gYuvView.setFrameData(w, h, y, u, v);
                            Thread.sleep(40);
                        } else {
                            Log.d("godv", "完成");
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
