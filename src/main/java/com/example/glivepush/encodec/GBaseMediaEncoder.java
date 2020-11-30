package com.example.glivepush.encodec;

import android.content.Context;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.util.Log;
import android.view.Surface;

import com.example.glivepush.egl.EglHelper;
import com.example.glivepush.egl.GEGLSurfaceView;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;

import javax.microedition.khronos.egl.EGLContext;

public abstract class GBaseMediaEncoder {

    private Surface surface;
    private EGLContext eglContext;

    private int width;
    private int height;

    //视频的编码器
    private MediaCodec videoEncodec;
    private MediaFormat videoFormat;
    private MediaCodec.BufferInfo videoBufferinfo;

    //音频的编码器
    private MediaCodec audioEncodec;
    private MediaFormat audioFormat;
    private MediaCodec.BufferInfo audioBufferinfo;
    private long audioPts = 0;
    private int sampleRate = 0;

    //用来复用
    private MediaMuxer mediaMuxer;
    private boolean encodecStart;
    private boolean audioExit;
    private boolean videoExit;


    //渲染视频的线程
    private GEGLMediaThread geglMediaThread;
    //编码视频的线程
    private VideoEncodecThread videoEncodecThread;
    //编码音频的线程
    private AudioEncodecThread audioEncodecThread;

    private GEGLSurfaceView.GGLRender gGLRender;

    public final static int RENDERMODE_WHEN_DIRTY = 0;
    public final static int RENDERMODE_CONTINUOUSLY = 1;

    private int mRenderMode = RENDERMODE_CONTINUOUSLY;

    private OnMediaInfoListener onMediaInfoListener;

    public void setOnMediaInfoListener(OnMediaInfoListener onMediaInfoListener) {
        this.onMediaInfoListener = onMediaInfoListener;
    }

    public GBaseMediaEncoder(Context context) {
    }

    public void setRender(GEGLSurfaceView.GGLRender gGLRender) {
        this.gGLRender = gGLRender;
    }

    public void setRenderMode(int mRenderMode) {
        if (gGLRender == null) {
            throw new RuntimeException("must set render before");
        }
        this.mRenderMode = mRenderMode;
    }

    //初始化方法
    public void initEncodec(EGLContext eglContext, String savePath, int width, int height, int sampleRate, int channelCount) {
        this.width = width;
        this.height = height;
        this.eglContext = eglContext;
        initMediaEncodc(savePath, width, height, sampleRate, channelCount);
    }

    //开始编码
    public void startRecord() {
        if (surface != null && eglContext != null) {
            audioPts = 0;
            //初始化
            audioExit = false;
            videoExit = false;
            encodecStart = false;

            geglMediaThread = new GEGLMediaThread(new WeakReference<GBaseMediaEncoder>(this));
            videoEncodecThread = new VideoEncodecThread(new WeakReference<GBaseMediaEncoder>(this));
            audioEncodecThread = new AudioEncodecThread(new WeakReference<GBaseMediaEncoder>(this));
            geglMediaThread.isCreate = true;
            geglMediaThread.isChange = true;
            geglMediaThread.start();
            videoEncodecThread.start();
            audioEncodecThread.start();
        }
    }

    //结束编码
    public void stopRecord() {
        if (geglMediaThread != null && videoEncodecThread != null && audioEncodecThread != null) {
            videoEncodecThread.exit();
            audioEncodecThread.exit();
            geglMediaThread.onDestory();
            videoEncodecThread = null;
            geglMediaThread = null;
            audioEncodecThread = null;
        }
    }

    private void initMediaEncodc(String savePath, int width, int height, int sampleRate, int channelCount) {
        //参数二录制类型
        try {
            mediaMuxer = new MediaMuxer(savePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            initVideoEncodec(MediaFormat.MIMETYPE_VIDEO_AVC, width, height);
            initAudioEncodec(MediaFormat.MIMETYPE_AUDIO_AAC, sampleRate, channelCount);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //初始化video的编码器
    private void initVideoEncodec(String mimeType, int width, int height) {
        try {
            videoBufferinfo = new MediaCodec.BufferInfo();

            videoFormat = MediaFormat.createVideoFormat(mimeType, width, height);
            videoFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT,
                    MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
            //码率
            videoFormat.setInteger(MediaFormat.KEY_BIT_RATE, width * height * 4);
            //帧率
            videoFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 30);
            //关键帧间隔
            videoFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);

            videoEncodec = MediaCodec.createEncoderByType(mimeType);
            //录制没有Surface
            //最后一个参数传的是编码
            videoEncodec.configure(videoFormat, null, null,
                    MediaCodec.CONFIGURE_FLAG_ENCODE);
            //得到Surface
            surface = videoEncodec.createInputSurface();
        } catch (IOException e) {
            e.printStackTrace();
            videoEncodec = null;
            videoFormat = null;
            videoBufferinfo = null;
        }
    }

    //初始化音频编码器
    private void initAudioEncodec(String mimeType, int simpleRate, int channelCount) {
        try {
            this.sampleRate = simpleRate;
            audioBufferinfo = new MediaCodec.BufferInfo();
            audioFormat = MediaFormat.createAudioFormat(mimeType, simpleRate, channelCount);
            //设置比特率
            audioFormat.setInteger(MediaFormat.KEY_BIT_RATE, 96000);
            //设置aac格式等级
            audioFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
            //设置最大输入缓存
            audioFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 4096);
            //生成encodec
            audioEncodec = MediaCodec.createEncoderByType(mimeType);

            audioEncodec.configure(audioFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        } catch (IOException e) {
            e.printStackTrace();
            audioEncodec = null;
            audioFormat = null;
            audioBufferinfo = null;
        }
    }

    //传递PCM的方法
    public void putPCMDate(byte[] buffer, int size) {
        if (audioEncodecThread != null && !audioEncodecThread.isExit && buffer != null && size > 0) {
            int inputBufferindex = audioEncodec.dequeueInputBuffer(0);
            if (inputBufferindex >= 0) {
                ByteBuffer byteBuffer = audioEncodec.getInputBuffers()[inputBufferindex];
                byteBuffer.clear();
                byteBuffer.put(buffer);
                long pts = getAudioPts(size, sampleRate);
                audioEncodec.queueInputBuffer(inputBufferindex, 0, size, pts, 0);
            }
        }
    }

    private long getAudioPts(int size, int sampleRate) {
        audioPts += (long) (1.0 * size / (sampleRate * 2 * 2) * 1000000.0);
        return audioPts;
    }

    //渲染视频
    static class GEGLMediaThread extends Thread {
        private WeakReference<GBaseMediaEncoder> encoder;
        private EglHelper eglHelper;
        private Object object;

        private boolean isExit = false;
        private boolean isCreate = false;
        private boolean isChange = false;
        private boolean isStart = false;

        public GEGLMediaThread(WeakReference<GBaseMediaEncoder> encoder) {
            this.encoder = encoder;
        }

        @Override
        public void run() {
            super.run();
            isExit = false;
            isStart = false;
            object = new Object();
            eglHelper = new EglHelper();
            eglHelper.initEgl(encoder.get().surface, encoder.get().eglContext);
            while (true) {
                if (isExit) {
                    release();
                    break;
                }
                //刷新模式
                if (isStart) {
                    if (encoder.get().mRenderMode == RENDERMODE_WHEN_DIRTY) {
                        synchronized (object) {
                            try {
                                object.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    } else if (encoder.get().mRenderMode == RENDERMODE_CONTINUOUSLY) {
                        try {
                            Thread.sleep(1000 / 60);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } else {
                        throw new RuntimeException("mRenderMode is wrong value");
                    }
                }

                onCreate();
                onChange(encoder.get().width, encoder.get().height);
                onDraw();

                isStart = true;
            }
        }

        public void release() {
            if (eglHelper != null) {
                eglHelper.destoryEgl();
                eglHelper = null;
                object = null;
                encoder = null;
            }
        }

        private void onCreate() {
            if (isCreate && encoder.get().gGLRender != null) {
                isCreate = false;
                encoder.get().gGLRender.onSurfaceCreated();
            }
        }

        private void onChange(int width, int height) {
            if (isChange && encoder.get().gGLRender != null) {
                isChange = false;
                encoder.get().gGLRender.onSurfaceChanged(width, height);
            }
        }

        private void onDraw() {
            if (encoder.get().gGLRender != null && eglHelper != null) {
                encoder.get().gGLRender.onDrawFrame();
                if (!isStart) {
                    encoder.get().gGLRender.onDrawFrame();
                }
                eglHelper.swapBuffers();
            }
        }

        private void requestRender() {
            if (object != null) {
                synchronized (object) {
                    object.notifyAll();
                }
            }
        }

        public void onDestory() {
            isExit = true;
            requestRender();
        }
    }

    //编码视频
    static class VideoEncodecThread extends Thread {

        private WeakReference<GBaseMediaEncoder> encoder;

        private boolean isExit;

        private MediaCodec videoEncodec;
        private MediaFormat videoFormat;
        private MediaCodec.BufferInfo videoBufferinfo;

        private MediaMuxer mediaMuxer;

        //视频轨道
        private int videoTrackIndex = -1;
        //pts
        private long pts;

        public VideoEncodecThread(WeakReference<GBaseMediaEncoder> encoder) {
            this.encoder = encoder;
            videoEncodec = encoder.get().videoEncodec;
            videoFormat = encoder.get().videoFormat;
            videoBufferinfo = encoder.get().videoBufferinfo;
            mediaMuxer = encoder.get().mediaMuxer;
            videoTrackIndex = -1;
        }

        @Override
        public void run() {
            super.run();
            pts = 0;
            videoTrackIndex = -1;
            isExit = false;
            videoEncodec.start();

            while (true) {
                if (isExit) {
                    videoEncodec.stop();
                    videoEncodec.release();
                    videoEncodec = null;

                    //写入头信息
                    encoder.get().videoExit = true;
                    if (encoder.get().audioExit) {
                        mediaMuxer.stop();
                        mediaMuxer.release();
                        mediaMuxer = null;
                    }
                    Log.d("godv", "录制完成");
                    break;
                }
                //视频编码开始
                //输出队列索引
                int outputBufferIndex = videoEncodec.dequeueOutputBuffer(videoBufferinfo, 0);
                if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    //获取视频轨道
                    videoTrackIndex = mediaMuxer.addTrack(videoEncodec.getOutputFormat());
                    if (encoder.get().audioEncodecThread.audioTrackIndex != -1) {
                        mediaMuxer.start();
                        encoder.get().encodecStart = true;
                    }
                } else {
                    while (outputBufferIndex >= 0) {

                        if (encoder.get().encodecStart) {
                            ByteBuffer outputBuffer = videoEncodec.getOutputBuffers()[outputBufferIndex];
                            outputBuffer.position(videoBufferinfo.offset);
                            outputBuffer.limit(videoBufferinfo.offset + videoBufferinfo.size);

                            if (pts == 0) {
                                pts = videoBufferinfo.presentationTimeUs;
                            }
                            videoBufferinfo.presentationTimeUs = videoBufferinfo.presentationTimeUs - pts;

                            //写入
                            mediaMuxer.writeSampleData(videoTrackIndex, outputBuffer,
                                    videoBufferinfo);
                            //回调
                            if (encoder.get().onMediaInfoListener != null) {
                                encoder.get().onMediaInfoListener.onMediaTime(
                                        (int) videoBufferinfo.presentationTimeUs / 1000000);
                            }
                        }

                        videoEncodec.releaseOutputBuffer(outputBufferIndex, false);
                        outputBufferIndex = videoEncodec.dequeueOutputBuffer(videoBufferinfo, 0);
                    }
                }
            }
        }

        public void exit() {
            isExit = true;
        }
    }

    //编码音频
    static class AudioEncodecThread extends Thread {
        //外层类的引用
        private WeakReference<GBaseMediaEncoder> encoder;
        //是否退出
        private boolean isExit;
        //编码
        private MediaCodec audioEncodec;
        private MediaCodec.BufferInfo bufferInfo;
        private MediaMuxer mediaMuxer;
        //视频轨道
        private int audioTrackIndex = -1;
        //pts
        private long pts;

        public AudioEncodecThread(WeakReference<GBaseMediaEncoder> encoder) {
            this.encoder = encoder;
            audioEncodec = encoder.get().audioEncodec;
            bufferInfo = encoder.get().audioBufferinfo;
            mediaMuxer = encoder.get().mediaMuxer;
        }

        @Override
        public void run() {
            super.run();
            //初始化
            pts = 0;
            audioTrackIndex = -1;
            isExit = false;

            //编码器开始编码
            audioEncodec.start();

            while (true) {
                if (isExit) {
                    //回收资源
                    audioEncodec.stop();
                    audioEncodec.release();
                    audioEncodec = null;

                    encoder.get().audioExit = true;

                    if (encoder.get().videoExit) {
                        mediaMuxer.stop();
                        mediaMuxer.release();
                        mediaMuxer = null;
                    }
                    break;
                }

                int outputBufferIndex = audioEncodec.dequeueOutputBuffer(bufferInfo, 0);
                //格式改变
                if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    //获取音频轨道
                    if (mediaMuxer != null) {
                        audioTrackIndex = mediaMuxer.addTrack(audioEncodec.getOutputFormat());
                        if (encoder.get().videoEncodecThread.videoTrackIndex != -1) {
                            mediaMuxer.start();
                            encoder.get().encodecStart = true;
                        }
                    }
                } else {
                    while (outputBufferIndex >= 0) {
                        if (encoder.get().encodecStart) {

                            ByteBuffer outputBuffer = audioEncodec.getOutputBuffers()[outputBufferIndex];
                            outputBuffer.position(bufferInfo.offset);
                            outputBuffer.limit(bufferInfo.offset + bufferInfo.size);

                            if (pts == 0) {
                                pts = bufferInfo.presentationTimeUs;
                            }
                            bufferInfo.presentationTimeUs = bufferInfo.presentationTimeUs - pts;

                            //写入mp4
                            mediaMuxer.writeSampleData(audioTrackIndex, outputBuffer, bufferInfo);
                        }
                        audioEncodec.releaseOutputBuffer(outputBufferIndex, false);
                        outputBufferIndex = audioEncodec.dequeueOutputBuffer(bufferInfo, 0);
                    }
                }
            }
        }

        public void exit() {
            isExit = true;
        }
    }

    public interface OnMediaInfoListener {
        void onMediaTime(int times);
    }
}
