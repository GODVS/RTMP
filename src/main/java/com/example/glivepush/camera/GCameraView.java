package com.example.glivepush.camera;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.WindowManager;

import com.example.glivepush.egl.GEGLSurfaceView;

public class GCameraView extends GEGLSurfaceView {

    private GCameraRender gCameraRender;
    private GCamera gCamera;

    private int cameraId = Camera.CameraInfo.CAMERA_FACING_BACK;

    private int textureId = -1;

    public GCameraView(Context context) {
        this(context,null);
    }

    public GCameraView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public GCameraView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        gCameraRender = new GCameraRender(context);
        gCamera = new GCamera(context);
        setRender(gCameraRender);

        //调用获取旋转角度
        previewAngle(context);

        gCameraRender.setOnSurfaceCreateListener(new GCameraRender.OnSurfaceCreateListener() {
            @Override
            public void onSurfaceCreate(SurfaceTexture surfaceTexture, int tid) {
                gCamera.initCamera(surfaceTexture, cameraId);
                textureId = tid;
            }
        });
    }

    public void onDestory(){
        if(gCamera != null){
            gCamera.stopPreview();
        }
    }

    public void previewAngle(Context context){
//        获取activity旋转角度
        int angle = ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();
        gCameraRender.resetMatrix();
        switch (angle){
            case Surface.ROTATION_0:
                if(cameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    gCameraRender.setAngle(90, 0, 0, 1);
                    gCameraRender.setAngle(180, 1, 0, 0);
                } else {
                    gCameraRender.setAngle(90f, 0f, 0f, 1f);
                }

                break;
            case Surface.ROTATION_90:
                if(cameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    gCameraRender.setAngle(180, 0, 0, 1);
                    gCameraRender.setAngle(180, 0, 1, 0);
                } else {
                    gCameraRender.setAngle(90f, 0f, 0f, 1f);
                }
                break;
            case Surface.ROTATION_180:
                if(cameraId == Camera.CameraInfo.CAMERA_FACING_BACK)
                {
                    gCameraRender.setAngle(90f, 0.0f, 0f, 1f);
                    gCameraRender.setAngle(180f, 0.0f, 1f, 0f);
                } else {
                    gCameraRender.setAngle(-90, 0f, 0f, 1f);
                }
                break;
            case Surface.ROTATION_270:
                if(cameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    gCameraRender.setAngle(180f, 0.0f, 1f, 0f);
                } else {
                    gCameraRender.setAngle(0f, 0f, 0f, 1f);
                }

                break;
        }
    }

    public int getTextureId(){
        return textureId;
    }
}
