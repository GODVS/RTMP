package com.example.glivepush.encodec;

import android.content.Context;

public class GMediaEncodec extends GBaseMediaEncoder{

    private GEncodecRender gEncodecRender;

    public GMediaEncodec(Context context, int textureid) {
        super(context);
        gEncodecRender = new GEncodecRender(context, textureid);
        setRender(gEncodecRender);
        setRenderMode(GBaseMediaEncoder.RENDERMODE_CONTINUOUSLY);//持续渲染
    }
}
