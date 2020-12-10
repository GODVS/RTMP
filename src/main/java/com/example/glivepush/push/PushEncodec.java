package com.example.glivepush.push;

import android.content.Context;

import com.example.glivepush.encodec.GBaseMediaEncoder;

public class PushEncodec extends BasePushEncoder {

    private PushRender gEncodecRender;

    public PushEncodec(Context context, int textureid) {
        super(context);
        gEncodecRender = new PushRender(context, textureid);
        setRender(gEncodecRender);
        setRenderMode(BasePushEncoder.RENDERMODE_CONTINUOUSLY);//持续渲染
    }
}
