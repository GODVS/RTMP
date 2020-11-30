package com.example.glivepush.imgvideo;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.util.Log;

import com.example.glivepush.R;
import com.example.glivepush.egl.GEGLSurfaceView;
import com.example.glivepush.egl.GShaderUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class GImgVideoRender implements GEGLSurfaceView.GGLRender {

    private Context context;

    private float[] vertexData = {
            -1f, -1f,
            1f, -1f,
            -1f, 1f,
            1f, 1f,

            0f, 0f,
            0f, 0f,
            0f, 0f,
            0f, 0f
    };

    private FloatBuffer vertexBuffer;

    private float[] fragmentData = {
            0f, 1f,
            1f, 1f,
            0f, 0f,
            1f, 0f
    };
    private FloatBuffer fragmentBuffer;

    private int program;
    private int vPosition;
    private int fPosition;
    private int textureid;

    private int vboId;
    private int fboId;

    private int srcImg = 0;

    //改变的
    private int imgTextureId;

    private GImgVideoFboRender gImgVideoFboRender;

    private OnRenderCreateListener onRenderCreateListener;

    //初始化
    public GImgVideoRender(Context context) {
        this.context = context;

        //初始化顶点坐标
        gImgVideoFboRender = new GImgVideoFboRender(context);

        vertexBuffer = ByteBuffer.allocateDirect(vertexData.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(vertexData);
        vertexBuffer.position(0);

        fragmentBuffer = ByteBuffer.allocateDirect(fragmentData.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(fragmentData);
        fragmentBuffer.position(0);

    }

    public void setOnRenderCreateListener(OnRenderCreateListener onRenderCreateListener) {
        this.onRenderCreateListener = onRenderCreateListener;
    }

    public interface OnRenderCreateListener {
        void onCreate(int textureId);
    }


    @Override
    public void onSurfaceCreated() {
        gImgVideoFboRender.onCreate();

        //更换shader
        String vertexSource = GShaderUtil.getRawResource(context, R.raw.vertex_shader_screen);

        String fragmentSource = GShaderUtil.getRawResource(context, R.raw.fragment_shader_screen);

        program = GShaderUtil.createProgram(vertexSource, fragmentSource);

        if (program > 0) {
            //顶点坐标
            vPosition = GLES20.glGetAttribLocation(program, "v_Position");
            //纹理坐标
            fPosition = GLES20.glGetAttribLocation(program, "f_Position");
        }
    }

    @Override
    public void onSurfaceChanged(int width, int height) {

        int[] vbos = new int[1];
        GLES20.glGenBuffers(1, vbos, 0);
        vboId = vbos[0];
        //绑定
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboId);
        //分配内存
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, vertexData.length * 4 + fragmentData.length * 4,
                null, GLES20.GL_STATIC_DRAW);
        //缓存到显存
        GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, 0, vertexData.length * 4, vertexBuffer);
        GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, vertexData.length * 4, fragmentData.length * 4,
                fragmentBuffer);
        //解绑
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);


        int[] fbos = new int[1];
        GLES20.glGenBuffers(1, fbos, 0);
        fboId = fbos[0];
        //绑定
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fboId);

        //生成离屏渲染纹理
        int[] textureIds = new int[1];
        GLES20.glGenTextures(1, textureIds, 0);
        textureid = textureIds[0];

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureid);

        //设置环绕过滤方法
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

        //设置FBO分配内存大小
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, 900, 1600, 0,
                GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
        //把纹理绑定到FBO
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,
                GLES20.GL_TEXTURE_2D, textureid, 0);
        //检查FBO绑定是否成功
        if (GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER) != GLES20.GL_FRAMEBUFFER_COMPLETE) {
            Log.d("godv", "fbo error");
        } else {
            Log.d("godv", "fbo success");
        }

        //解绑纹理
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);

        //解绑
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

//        setCurrentImgSrc(srcImg);


        if (onRenderCreateListener != null) {
            onRenderCreateListener.onCreate(textureid);
        }

        GLES20.glViewport(0, 0, width, height);
        gImgVideoFboRender.onChange(width, height);
    }

    @Override
    public void onDrawFrame() {
        imgTextureId = GShaderUtil.loadTexture(srcImg, context);

        Log.d("godv", "id is : " + imgTextureId);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fboId);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glClearColor(1f, 0f, 0f, 1f);

        GLES20.glUseProgram(program);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, imgTextureId);


        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboId);

        GLES20.glEnableVertexAttribArray(vPosition);
        GLES20.glVertexAttribPointer(vPosition, 2, GLES20.GL_FLOAT, false, 8,
                0);

        GLES20.glEnableVertexAttribArray(fPosition);
        GLES20.glVertexAttribPointer(fPosition, 2, GLES20.GL_FLOAT, false, 8,
                vertexData.length * 4);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

        //绘制一张清除一张
        int[] ids = new int[]{imgTextureId};
        GLES20.glDeleteTextures(1, ids, 0);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        gImgVideoFboRender.onDraw(textureid);
    }

    public void setCurrentImgSrc(int src) {
        srcImg = src;
//        imgTextureId = WlShaderUtil.loadTexrute(src, context);
    }
}
