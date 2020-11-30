package com.example.glivepush.camera;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import com.example.glivepush.R;
import com.example.glivepush.egl.GEGLSurfaceView;
import com.example.glivepush.egl.GShaderUtil;
import com.example.glivepush.util.DisplayUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class GCameraRender implements GEGLSurfaceView.GGLRender, SurfaceTexture.OnFrameAvailableListener {

    private Context context;

    private FloatBuffer vertexBuffer;
    private FloatBuffer fragmentBuffer;

    private int program;

    private int vPosition;
    private int fPosition;

    private int vboId;
    private int fboId;

    private int fboTextureid;
    private int cameraTextureid;

    private int screenWidth;
    private int screenHeight;

    private int width;
    private int height;

    private GCameraFboRender gCameraFboRender;

    private SurfaceTexture surfaceTexture;
    private OnSurfaceCreateListener onSurfaceCreateListener;

    public void setOnSurfaceCreateListener(OnSurfaceCreateListener onSurfaceCreateListener) {
        this.onSurfaceCreateListener = onSurfaceCreateListener;
    }

    private int umatrix;
    private float[] matrix = new float[16];


    private final float[] vertexData = {
            -1f, -1f,
            1f, -1f,
            -1f, 1f,
            1f, 1f
    };

    private final float[] fragmentData = {
            0f, 1f,
            1f, 1f,
            0f, 0f,
            1f, 0f
    };

    public GCameraRender(Context context) {
        this.context = context;

        screenWidth = DisplayUtil.getScreenWidth(context);
        screenHeight = DisplayUtil.getScreenHeight(context);

        gCameraFboRender = new GCameraFboRender(context);

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

    @Override
    public void onSurfaceCreated() {

        gCameraFboRender.onCreate();

        //获取着色器属性
        String vertexSource = GShaderUtil.getRawResource(context, R.raw.vertex_shader);
        String fragmentSource = GShaderUtil.getRawResource(context, R.raw.fragment_shader);
        program = GShaderUtil.createProgram(vertexSource, fragmentSource);
        //顶点坐标
        vPosition = GLES20.glGetAttribLocation(program, "v_Position");
        //纹理坐标
        fPosition = GLES20.glGetAttribLocation(program, "f_Position");

        //加载矩阵
        umatrix = GLES20.glGetUniformLocation(program, "u_Matrix");

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

        //fbo
        int[] fbos = new int[1];
        GLES20.glGenBuffers(1, fbos, 0);
        fboId = fbos[0];
        //绑定
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fboId);

        //生成纹理
        int[] textureIds = new int[1];
        GLES20.glGenTextures(1, textureIds, 0);
        fboTextureid = textureIds[0];
        //绑定
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, fboTextureid);

        //设置环绕过滤方法
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

        //设置FBO分配内存大小
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, screenWidth, screenHeight, 0,
                GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);

        //把纹理绑定到FBO
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,
                GLES20.GL_TEXTURE_2D, fboTextureid, 0);
        //检查FBO绑定是否成功
        if (GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER) != GLES20.GL_FRAMEBUFFER_COMPLETE) {
            Log.e("godv", "fbo error");
        } else {
            Log.e("godv", "fbo success");
        }

        //解绑纹理
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        //解绑
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

        //生成摄像头纹理
        int[] textureIdsoes = new int[1];
        GLES20.glGenTextures(1, textureIdsoes, 0);
        cameraTextureid = textureIdsoes[0];
        //绑定扩展纹理
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, cameraTextureid);

        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);

        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

        //摄像头id
        surfaceTexture = new SurfaceTexture(cameraTextureid);
        //监听
        surfaceTexture.setOnFrameAvailableListener(this);

        if (onSurfaceCreateListener != null) {
            onSurfaceCreateListener.onSurfaceCreate(surfaceTexture, fboTextureid);
        }

        //解绑纹理
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);
    }

    //重置矩阵
    public void resetMatrix() {
        Matrix.setIdentityM(matrix, 0);
    }

    //设置角度
    public void setAngle(float angle, float x, float y, float z) {
        Matrix.rotateM(matrix, 0, angle, x, y, z);

    }

    @Override
    public void onSurfaceChanged(int width, int height) {
//        gCameraFboRender.onChange(width, height);
//        GLES20.glViewport(0,0,width,height);

        this.width = width;
        this.height = height;
    }

    @Override
    public void onDrawFrame() {
        surfaceTexture.updateTexImage();

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glClearColor(1f, 0f, 0f, 1f);

        GLES20.glUseProgram(program);

        //设置屏幕大小
        GLES20.glViewport(0, 0, screenWidth, screenHeight);
        //使用
        GLES20.glUniformMatrix4fv(umatrix, 1, false, matrix, 0);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fboId);
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

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

        gCameraFboRender.onChange(width, height);
        gCameraFboRender.onDraw(fboTextureid);
    }

    //有数据回调
    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {

    }

    public interface OnSurfaceCreateListener {

        void onSurfaceCreate(SurfaceTexture surfaceTexture, int textureId);
    }

    public int getFboTextureid() {
        return fboTextureid;
    }
}
