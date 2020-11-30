package com.example.glivepush.egl;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.lang.ref.WeakReference;

import javax.microedition.khronos.egl.EGLContext;

public abstract class GEGLSurfaceView extends SurfaceView implements SurfaceHolder.Callback{


    private Surface surface;
    private EGLContext eglContext;

    private GEGLThread gEGLThread;
    private GGLRender gGLRender;

    public final static int RENDERMODE_WHEN_DIRTY = 0;
    public final static int RENDERMODE_CONTINUOUSLY = 1;

    private int mRenderMode = RENDERMODE_CONTINUOUSLY;


    public GEGLSurfaceView(Context context) {
        this(context, null);
    }

    public GEGLSurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GEGLSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        getHolder().addCallback(this);
    }

    public void setRender(GGLRender gGLRender) {
        this.gGLRender = gGLRender;
    }

    public void setRenderMode(int mRenderMode) {

        if(gGLRender == null)
        {
            throw  new RuntimeException("must set render before");
        }
        this.mRenderMode = mRenderMode;
    }

    public void setSurfaceAndEglContext(Surface surface, EGLContext eglContext)
    {
        this.surface = surface;
        this.eglContext = eglContext;
    }

    public EGLContext getEglContext()
    {
        if(gEGLThread != null)
        {
            return gEGLThread.getEglContext();
        }
        return null;
    }

    public void requestRender()
    {
        if(gEGLThread != null)
        {
            gEGLThread.requestRender();
        }
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if(surface == null)
        {
            surface = holder.getSurface();
        }
        gEGLThread = new GEGLThread(new WeakReference<GEGLSurfaceView>(this));
        gEGLThread.isCreate = true;
        gEGLThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        gEGLThread.width = width;
        gEGLThread.height = height;
        gEGLThread.isChange = true;

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        gEGLThread.onDestory();
        gEGLThread = null;
        surface = null;
        eglContext = null;
    }

    public interface GGLRender
    {
        void onSurfaceCreated();
        void onSurfaceChanged(int width, int height);
        void onDrawFrame();
    }


    static class GEGLThread extends Thread {

        private WeakReference<GEGLSurfaceView> geglSurfaceViewWeakReference;
        private EglHelper eglHelper = null;
        private Object object = null;

        private boolean isExit = false;
        private boolean isCreate = false;
        private boolean isChange = false;
        private boolean isStart = false;

        private int width;
        private int height;

        public GEGLThread(WeakReference<GEGLSurfaceView> geglSurfaceViewWeakReference) {
            this.geglSurfaceViewWeakReference = geglSurfaceViewWeakReference;
        }

        @Override
        public void run() {
            super.run();
            isExit = false;
            isStart = false;
            object = new Object();
            eglHelper = new EglHelper();
            eglHelper.initEgl(geglSurfaceViewWeakReference.get().surface, geglSurfaceViewWeakReference.get().eglContext);

            while (true)
            {
                if(isExit)
                {
                    //释放资源
                    release();
                    break;
                }

                if(isStart)
                {
                    if(geglSurfaceViewWeakReference.get().mRenderMode == RENDERMODE_WHEN_DIRTY)
                    {
                        synchronized (object)
                        {
                            try {
                                object.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    else if(geglSurfaceViewWeakReference.get().mRenderMode == RENDERMODE_CONTINUOUSLY)
                    {
                        try {
                            Thread.sleep(1000 / 60);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    else
                    {
                        throw  new RuntimeException("mRenderMode is wrong value");
                    }
                }

                onCreate();
                onChange(width, height);
                onDraw();

                isStart = true;
            }
        }

        private void onCreate() {
            if(isCreate && geglSurfaceViewWeakReference.get().gGLRender != null)
            {
                isCreate = false;
               geglSurfaceViewWeakReference.get().gGLRender.onSurfaceCreated();
            }
        }

        private void onChange(int width, int height) {
            if(isChange && geglSurfaceViewWeakReference.get().gGLRender != null)
            {
                isChange = false;
                geglSurfaceViewWeakReference.get().gGLRender.onSurfaceChanged(width, height);
            }
        }

        private void onDraw() {
            if(geglSurfaceViewWeakReference.get().gGLRender != null && eglHelper != null)
            {
                geglSurfaceViewWeakReference.get().gGLRender.onDrawFrame();
                if(!isStart)
                {
                    geglSurfaceViewWeakReference.get().gGLRender.onDrawFrame();
                }
                eglHelper.swapBuffers();

            }
        }

        private void requestRender()
        {
            if(object != null)
            {
                synchronized (object)
                {
                    object.notifyAll();
                }
            }
        }

        public void onDestory()
        {
            isExit = true;
            requestRender();
        }


        public void release()
        {
            if(eglHelper != null)
            {
                eglHelper.destoryEgl();
                eglHelper = null;
                object = null;
                geglSurfaceViewWeakReference = null;
            }
        }

        public EGLContext getEglContext()
        {
            if(eglHelper != null)
            {
                return eglHelper.getmEglContext();
            }
            return null;
        }
    }
}
