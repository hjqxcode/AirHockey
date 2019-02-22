package com.hjqxcode.airhockey.renderer;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import com.hjqxcode.airhockey.R;
import com.hjqxcode.airhockey.util.ShaderUtil;
import com.hjqxcode.airhockey.util.ShaderUtil.AttributeShaderParameter;
import com.hjqxcode.airhockey.util.ShaderUtil.UniformShaderParameter;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


public class AirHockeyRenderer implements GLSurfaceView.Renderer {

    private static final int BYTES_PER_FLOAT = Float.SIZE / Byte.SIZE;
    private static final int POSITION_COMPONENT_COUNT = 2;

    private static final float[] AIRHOCKET_VERTICES = { // left bottom corner is original point
            // triangle 1(counter-clockwise order/winding order)
            -0.5f, -0.5f,
            0.5f, 0.5f,
            -0.5f, 0.5f,

            // triangle 2
            -0.5f, -0.5f,
            0.5f, -0.5f,
            0.5f, 0.5f,

            // middle seperate line
            -0.5f, 0f,
            0.5f, 0f,

            // mallets
            0f, -0.25f,
            0f, 0.25f,
    };

    private int mProgram;
    private Context mContext;
    private FloatBuffer mVerticesBuffer;
    private AttributeShaderParameter aPositon;
    private UniformShaderParameter uColor;

    public AirHockeyRenderer(Context context) {
        mContext = context.getApplicationContext();
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        mVerticesBuffer = ByteBuffer.allocateDirect(AIRHOCKET_VERTICES.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mVerticesBuffer.put(AIRHOCKET_VERTICES);

        String vertexSourceCode = ShaderUtil.readShaderSourceCode(mContext, R.raw.vertex_shader);
        int vertexId = ShaderUtil.compileShader(GLES20.GL_VERTEX_SHADER, vertexSourceCode);

        vertexSourceCode = ShaderUtil.readShaderSourceCode(mContext, R.raw.fragment_shader);
        int fragmentId = ShaderUtil.compileShader(GLES20.GL_FRAGMENT_SHADER, vertexSourceCode);

        mProgram = ShaderUtil.linkProgram(vertexId, fragmentId);

        aPositon = new AttributeShaderParameter("a_Position");
        uColor = new UniformShaderParameter("u_Color");
        aPositon.loadHandle(mProgram);
        uColor.loadHandle(mProgram);

        mVerticesBuffer.position(0);
        GLES20.glVertexAttribPointer(aPositon.handle, POSITION_COMPONENT_COUNT,
                GLES20.GL_FLOAT, false, 0, mVerticesBuffer);

        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glUseProgram(mProgram);
        ShaderUtil.checkError();

        GLES20.glEnableVertexAttribArray(aPositon.handle);

        // draw white table
        GLES20.glUniform4f(uColor.handle, 1.0f, 1.0f, 1.0f, 1.0f);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6);

        // draw red middle seperate line
        GLES20.glUniform4f(uColor.handle, 1.0f, 0.0f, 0.0f, 1.0f);
        GLES20.glDrawArrays(GLES20.GL_LINES, 6, 2);

        // draw the first mallet blue
        GLES20.glUniform4f(uColor.handle, 0.0f, 0.0f, 1.0f, 1.0f);
        GLES20.glDrawArrays(GLES20.GL_POINTS, 8, 1);

        // draw the second mallet red
        GLES20.glUniform4f(uColor.handle, 1.0f, 0.0f, 0.0f, 1.0f);
        GLES20.glDrawArrays(GLES20.GL_POINTS, 9, 1);

        GLES20.glDisableVertexAttribArray(aPositon.handle);
    }
}
