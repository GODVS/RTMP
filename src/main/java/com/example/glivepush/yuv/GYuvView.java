package com.example.glivepush.yuv;

import android.content.Context;
import android.util.AttributeSet;

import com.example.glivepush.egl.GEGLSurfaceView;

public class GYuvView extends GEGLSurfaceView {
    private GYuvRender gYuvRender;

    public GYuvView(Context context) {
        this(context, null);
    }

    public GYuvView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GYuvView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        gYuvRender = new GYuvRender(context);
        setRender(gYuvRender);
        //手动
        setRenderMode(GEGLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    public void setFrameData(int w, int h, byte[] by, byte[] bu, byte[] bv) {
        if (gYuvRender != null) {
            gYuvRender.setFrameData(w, h, by, bu, bv);
            //手动渲染
            requestRender();
        }
    }
}
