package com.example.glivepush.push;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

public class AudioRecordUtil {
    private AudioRecord audioRecord;
    private int bufferSizeInBytes;
    private boolean start = false;
    private int readSize = 0;

    private OnRecordListener onRecordListener;

    public void setOnRecordListener(OnRecordListener onRecordListener) {
        this.onRecordListener = onRecordListener;
    }

    public AudioRecordUtil() {
        bufferSizeInBytes = AudioRecord.getMinBufferSize(
                44100,
                AudioFormat.CHANNEL_IN_STEREO,
                AudioFormat.ENCODING_PCM_16BIT);
        //HZ 声道数 16bitPCM
        audioRecord = new AudioRecord(
                MediaRecorder.AudioSource.MIC,
                44100,
                AudioFormat.CHANNEL_IN_STEREO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSizeInBytes);
        //MediaRecorder.AudioSource.MIC 麦克风
    }

    public void startRecord() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                start = true;
                //开始录音
                audioRecord.startRecording();
                byte[] audioData = new byte[bufferSizeInBytes];
                while (start) {
                    readSize = audioRecord.read(audioData, 0, bufferSizeInBytes);
                    if (onRecordListener != null) {
                        onRecordListener.recordByte(audioData, readSize);
                    }
                }
                if (audioRecord != null) {
                    audioRecord.stop();
                    audioRecord.release();
                    audioRecord = null;
                }
            }
        }).start();
    }

    public void stopRecord() {
        start = false;
    }

    public interface OnRecordListener {
        void recordByte(byte[] audioData, int readSize);
    }

    public boolean isStart() {
        return start;
    }
}
