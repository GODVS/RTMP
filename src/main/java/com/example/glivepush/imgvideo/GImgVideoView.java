package com.example.glivepush.imgvideo;

import android.content.Context;
import android.util.AttributeSet;

import com.example.glivepush.egl.GEGLSurfaceView;

public class GImgVideoView extends GEGLSurfaceView {

    //使用Render
    private GImgVideoRender gImgVideoRender;
    //得到的id
    private int fbotextureId;

    public GImgVideoView(Context context) {
        this(context, null);
    }

    public GImgVideoView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GImgVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        //初始化
        gImgVideoRender = new GImgVideoRender(context);
        setRender(gImgVideoRender);
        //设置模式  手动
        setRenderMode(GEGLSurfaceView.RENDERMODE_WHEN_DIRTY);
        //设置监听
        gImgVideoRender.setOnRenderCreateListener(new GImgVideoRender.OnRenderCreateListener() {
            @Override
            public void onCreate(int textureId) {
                fbotextureId = textureId;
            }
        });
    }

    public void setCurrentImg(int img) {
        if (gImgVideoRender != null) {
            gImgVideoRender.setCurrentImgSrc(img);
            //手动刷新调用
            requestRender();
        }
    }

    public int getFbotextureId() {
        return fbotextureId;
    }
}
