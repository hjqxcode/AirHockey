package com.hjqxcode.airhockey.renderer;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

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
    private static final int COLOR_COMPONENT_COUNT = 4;
    private static final int STRIDE = (POSITION_COMPONENT_COUNT + COLOR_COMPONENT_COUNT) * BYTES_PER_FLOAT;

    private static final float[] AIRHOCKET_VERTICES = { // left bottom corner is original point
            // triangle fan(counter-clockwise order/winding order)
            // X, Y, R, G, B, A
            0f,     0f,   1.0f, 1.0f, 1.0f, 1.0f,
            -0.5f, -0.8f, 0.7f, 0.7f, 0.7f, 1.0f,
            0.5f,  -0.8f, 0.7f, 0.7f, 0.7f, 1.0f,
            0.5f,   0.8f, 0.7f, 0.7f, 0.7f, 1.0f,
            -0.5f,  0.8f, 0.7f, 0.7f, 0.7f, 1.0f,
            -0.5f, -0.8f, 0.7f, 0.7f, 0.7f, 1.0f,

            // middle seperate line
            -0.5f,    0f, 1.0f,    0f,  0f, 1.0f,
            0.5f,     0f, 1.0f,    0f,  0f, 1.0f,

            // mallets
            0f, -0.25f,    0f, 0f, 1.0f, 1.0f,
            0f,  0.25f, 1.0f, 0f, 0f, 1.0f
    };

    private static boolean DEBUG = true;

    private int mProgram;
    private Context mContext;
    private FloatBuffer mVerticesBuffer;
    private AttributeShaderParameter aPositon;
    private AttributeShaderParameter aColor;
    private UniformShaderParameter uMatrix;

    private final float[] projectMatric = new float[16];

    public AirHockeyRenderer(Context context) {
        mContext = context.getApplicationContext();
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        String vertexSourceCode = ShaderUtil.readShaderSourceCode(mContext, R.raw.vertex_shader);
        int vertexId = ShaderUtil.compileShader(GLES20.GL_VERTEX_SHADER, vertexSourceCode);

        vertexSourceCode = ShaderUtil.readShaderSourceCode(mContext, R.raw.fragment_shader);
        int fragmentId = ShaderUtil.compileShader(GLES20.GL_FRAGMENT_SHADER, vertexSourceCode);

        mProgram = ShaderUtil.linkProgram(vertexId, fragmentId);

        aPositon = new AttributeShaderParameter("a_Position");
        aColor = new AttributeShaderParameter("a_Color");
        uMatrix = new UniformShaderParameter("u_Matrix");
        aPositon.loadHandle(mProgram);
        aColor.loadHandle(mProgram);
        uMatrix.loadHandle(mProgram);

        mVerticesBuffer = ByteBuffer.allocateDirect(AIRHOCKET_VERTICES.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mVerticesBuffer.put(AIRHOCKET_VERTICES);
        mVerticesBuffer.position(0);
        GLES20.glVertexAttribPointer(aPositon.handle,  // handle index
                POSITION_COMPONENT_COUNT,              // Specifies the number of components per generic vertex attribute.Must be 1, 2, 3, or 4. The initial value is 4.
                GLES20.GL_FLOAT,                       // Specifies the data type of each component in the array
                false,                       // Specifies whether fixed-point data values should be normalized (GL_TRUE) or converted directly as fixed-point values (GL_FALSE) when they are accessed.
                STRIDE,                                // Specifies the byte offset between consecutive generic vertex attributes. If stride is 0, the generic vertex attributes are understood to be tightly packed in the array. The initial value is 0.
                mVerticesBuffer);                      // Specifies a pointer to the first component of the first generic vertex attribute in the array. The initial value is 0.

        mVerticesBuffer.position(POSITION_COMPONENT_COUNT);
        GLES20.glVertexAttribPointer(aColor.handle, COLOR_COMPONENT_COUNT,
                GLES20.GL_FLOAT, false, STRIDE, mVerticesBuffer);

        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);

        if (DEBUG) {
            final float[] identity = new float[16];
            Matrix.setIdentityM(identity, 0);
            ShaderUtil.printMatrix("identity", identity);
            Matrix.translateM(identity, 0, 0.1f, 0.2f, 0.3f);
            ShaderUtil.printMatrix("translate(0.1, 0.2, 0.3)", identity);
        }

        // Computes an orthographic（正交） projection matrix
        // orthoM(float[] m, int mOffset, float left, float right, float bottom, float top, float near, float far)
        float aspectRatio;
        String orthoMInfo;
        if (width < height) {
            aspectRatio = (float) height / width;
            Matrix.orthoM(projectMatric, 0, -1f, 1f, -aspectRatio, aspectRatio, -1f, 1f);
            orthoMInfo = String.format("orthoM(projectMatric, 0, -1f, 1f, -%f, %f, -1f, 1f): ", aspectRatio, aspectRatio);

            if (DEBUG) { // demo project
                float[] result = new float[4];
                float[] vecL = new float[]{-1, -aspectRatio, 0, 1};
                Matrix.multiplyMV(result, 0, projectMatric, 0, vecL, 0);
                ShaderUtil.printVec2Vec("L", vecL, result);

                float[] vecM = new float[]{0, 0, 0, 1};
                Matrix.multiplyMV(result, 0, projectMatric, 0, vecM, 0);
                ShaderUtil.printVec2Vec("M", vecM, result);

                float[] vecR = new float[]{1, aspectRatio, 0, 1};
                Matrix.multiplyMV(result, 0, projectMatric, 0, vecR, 0);
                ShaderUtil.printVec2Vec("R", vecR, result);
            }
        } else {
            aspectRatio = (float) width / height;
            Matrix.orthoM(projectMatric, 0, -aspectRatio, aspectRatio, -1f, 1f, -1f, 1f);
            orthoMInfo = String.format("orthoM(projectMatric, 0, -%f, %f, -1f, 1f, -1f, 1f): ", aspectRatio, aspectRatio);
        }
        ShaderUtil.printMatrix(orthoMInfo, projectMatric);

        // glUniformMatrix4fv(int location, int count, boolean transpose, float[] value, int offset)
        // GLES20.glUniformMatrix4fv(uMatrix.handle, 1, false, projectMatric, 0);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        // glUniformMatrix4fv(int location, int count, boolean transpose, float[] value, int offset)
        GLES20.glUniformMatrix4fv(uMatrix.handle, 1, false, projectMatric, 0);

        GLES20.glUseProgram(mProgram);
        ShaderUtil.checkError();

        GLES20.glEnableVertexAttribArray(aPositon.handle);
        GLES20.glEnableVertexAttribArray(aColor.handle);

        // draw gradient table
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, 6);

        // draw red middle seperate line
        GLES20.glDrawArrays(GLES20.GL_LINES, 6, 2);

        // draw two mallets blue
        GLES20.glDrawArrays(GLES20.GL_POINTS, 8, 1);
        GLES20.glDrawArrays(GLES20.GL_POINTS, 9, 1);

        GLES20.glDisableVertexAttribArray(aPositon.handle);
        GLES20.glDisableVertexAttribArray(aColor.handle);
    }
}
