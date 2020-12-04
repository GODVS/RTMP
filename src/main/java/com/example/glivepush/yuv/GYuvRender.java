package com.example.glivepush.yuv;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import com.example.glivepush.R;
import com.example.glivepush.egl.GEGLSurfaceView;
import com.example.glivepush.egl.GShaderUtil;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class GYuvRender implements GEGLSurfaceView.GGLRender {

    private Context context;
    //顶点坐标  主要用来在本地开辟内存
    private float[] vertexData = {
            1f, 1f,
            -1f, 1f,
            1f, -1f,
            -1f, -1f
    };
    //生成的本地内存
    private FloatBuffer vertexBuffer;

    //纹理坐标
    private float[] fragmentData = {
            1f, 0f,
            0f, 0f,
            1f, 1f,
            0f, 1f
    };
    //生成的本地内存
    private FloatBuffer fragmentBuffer;

    private int program;
    private int vPosition;
    private int fPosition;

    private int sampler_y;
    private int sampler_u;
    private int sampler_v;

    //纹理数组
    private int[] texture_yuv;
    private int textureid;

    private int fboId;

    private int w;
    private int h;

    private Buffer y;
    private Buffer u;
    private Buffer v;

    private GYuvFboRender gYuvFboRender;

    private int umatrix;
    private float[] matrix = new float[16];

    public GYuvRender(Context context) {
        this.context = context;

        gYuvFboRender = new GYuvFboRender(context);

        //生成的本地内存
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

        Matrix.setIdentityM(matrix, 0);
    }

    @Override
    public void onSurfaceCreated() {
        gYuvFboRender.onCreate();
        //工具方法加载代码
        String vertexSource = GShaderUtil.getRawResource(context, R.raw.vertex_shader_yuv);
        String fragmentSource = GShaderUtil.getRawResource(context, R.raw.fragment_shader_yuv);

        //工具方法创建program
        program = GShaderUtil.createProgram(vertexSource, fragmentSource);
        vPosition = GLES20.glGetAttribLocation(program, "v_Position");
        //取出纹理坐标
        fPosition = GLES20.glGetAttribLocation(program, "f_Position");

        sampler_y = GLES20.glGetUniformLocation(program, "sampler_y");
        sampler_u = GLES20.glGetUniformLocation(program, "sampler_u");
        sampler_v = GLES20.glGetUniformLocation(program, "sampler_v");

        umatrix = GLES20.glGetUniformLocation(program, "u_Matrix");

        //生成纹理
        texture_yuv = new int[3];
        GLES20.glGenTextures(3, texture_yuv, 0);

        for (int i = 0; i < 3; i++) {
            //绑定纹理
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture_yuv[i]);
            //设置环绕过滤方法
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            //解绑
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        }

        int[] fbos = new int[1];
        GLES20.glGenBuffers(1, fbos, 0);
        fboId = fbos[0];
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fboId);

        int[] textureIds = new int[1];
        GLES20.glGenTextures(1, textureIds, 0);
        textureid = textureIds[0];
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureid);

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, 720, 500, 0,
                GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D,
                textureid, 0);
        if (GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER) != GLES20.GL_FRAMEBUFFER_COMPLETE) {
            Log.e("godv", "fbo wrong");
        } else {
            Log.e("godv", "fbo success");
        }

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        Matrix.rotateM(matrix, 0, 180, 1, 0, 0);
        GLES20.glViewport(0, 0, width, height);
        gYuvFboRender.onChange(width, height);
    }

    @Override
    public void onDrawFrame() {
        //绑定FBO  离屏渲染
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fboId);
        //清屏
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        //使用颜色清屏
        GLES20.glClearColor(1.0f, 0.0f, 0.0f, 1.0f);

        if (w > 0 && h > 0 && y != null && u != null && v != null) {
            GLES20.glUseProgram(program);

            GLES20.glUniformMatrix4fv(umatrix, 1, false, matrix, 0);

            GLES20.glEnableVertexAttribArray(vPosition);
            GLES20.glVertexAttribPointer(vPosition, 2, GLES20.GL_FLOAT, false, 8,
                    vertexBuffer);
            GLES20.glEnableVertexAttribArray(fPosition);
            GLES20.glVertexAttribPointer(fPosition, 2, GLES20.GL_FLOAT, false, 8,
                    fragmentBuffer);

            //激活第0个纹理 默认纹理
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            //绑定
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture_yuv[0]);
            //附值
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_LUMINANCE, w, h, 0,
                    GLES20.GL_LUMINANCE, GLES20.GL_UNSIGNED_BYTE, y);
            //链接
            GLES20.glUniform1i(sampler_y, 0);

            GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture_yuv[1]);
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_LUMINANCE, w / 2,
                    h / 2, 0, GLES20.GL_LUMINANCE, GLES20.GL_UNSIGNED_BYTE, u);
            GLES20.glUniform1i(sampler_u, 1);

            GLES20.glActiveTexture(GLES20.GL_TEXTURE2);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture_yuv[2]);
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_LUMINANCE, w / 2,
                    h / 2, 0, GLES20.GL_LUMINANCE, GLES20.GL_UNSIGNED_BYTE, v);
            GLES20.glUniform1i(sampler_v, 2);

            y.clear();
            u.clear();
            v.clear();

            y = null;
            u = null;
            v = null;
        }
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

        gYuvFboRender.onDraw(textureid);

    }

    public void setFrameData(int w, int h, byte[] by, byte[] bu, byte[] bv) {
        this.w = w;
        this.h = h;
        this.y = ByteBuffer.wrap(by);
        this.u = ByteBuffer.wrap(bu);
        this.v = ByteBuffer.wrap(bv);
    }
}
