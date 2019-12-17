package com.hjqxcode.airhockey.util;

import android.content.Context;
import android.content.res.Resources;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ShaderUtil {
    private static final String TAG = "ShaderUtil";

    public static String readShaderSourceCode(Context context, int resId) {
        StringBuilder content = new StringBuilder();

        Resources res = context.getResources();

        try {
            InputStream inputStream = res.openRawResource(resId);
            InputStreamReader inputReader = new InputStreamReader(inputStream);
            BufferedReader bufferdReader = new BufferedReader(inputReader);

            String line;
            while ((line = bufferdReader.readLine()) != null) {
                content.append(line).append("\n");
            }

            return content.toString();
        } catch (IOException e) {
            throw new RuntimeException("IOException when readLine resource id: " + resId, e);
        } catch (Resources.NotFoundException nfe) {
            throw new RuntimeException("Resources.NotFoundException when open resource id: " + resId, nfe);
        }
    }

    public static int compileShader(int type, String shaderCode) {
        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        if (type != GLES20.GL_VERTEX_SHADER && type != GLES20.GL_FRAGMENT_SHADER) {
            throw new IllegalArgumentException("create shader with illegal type: " + type);
        }
        int shader = GLES20.glCreateShader(type);

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        checkError();
        GLES20.glCompileShader(shader);
        checkError();

        return shader;
    }

    public static int linkProgram(int vertexShader, int fragmentShader) {
        int program = GLES20.glCreateProgram();
        checkError();
        if (program == 0) {
            throw new RuntimeException("glCreateProgram: " + GLES20.glGetError());
        }
        GLES20.glAttachShader(program, vertexShader);
        checkError();
        GLES20.glAttachShader(program, fragmentShader);
        checkError();
        GLES20.glLinkProgram(program);
        checkError();
        return program;
    }

    public static void checkError(){
        int error = GLES20.glGetError();
        if (error != 0) {
            Log.e(TAG, "GL error: " + error + ", msg: " + GLUtils.getEGLErrorString(error));
        }
    }

    public static void printVec2Vec(String tag, float[] orig, float[] dest) {
        if (orig == null || dest == null || orig.length != dest.length) return;

        StringBuilder builder = new StringBuilder();
        builder.append("[");
        for (int i = 0, size = orig.length; i < size; i++) {
            builder.append(orig[i]);
            if (i != size - 1) builder.append(", ");
        }
        builder.append("] --> [");
        for (int i = 0, size = dest.length; i < size; i++) {
            builder.append(dest[i]);
            if (i != size - 1) builder.append(", ");
        }
        builder.append("]");
        Log.d(TAG, "printVec2Vec: " + tag + "\n" + builder.toString());
    }

    public static void printMatrix(String tag, float[] matrix) {
        int length = matrix != null ? matrix.length : 0;
        if (length != 16) {
            Log.w(TAG, "printMatrix error matrix length: " + length);
            return;
        }

        StringBuilder builder = new StringBuilder();
        builder.append("[");
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (j == 0) {
                    if (i != 0) builder.append(" ");
                }
                builder.append(matrix[i + j * 4]);

                if (j != 3) {
                    builder.append(", ");
                } else if (i != 3) {
                    builder.append("\n");
                }
            }
        }
        builder.append("]");
        Log.d(TAG, "printMatrix: " + tag + "\n" + builder.toString());

    }

    abstract static class ShaderParameter {
        public int handle;
        protected final String mName;

        public ShaderParameter(String name) {
            mName = name;
        }

        public abstract void loadHandle(int program);

        void printHandle() {
            Log.v(TAG, mName + " handle: " + handle);
        }
    }

    public static class UniformShaderParameter extends ShaderParameter {
        public UniformShaderParameter(String name) {
            super(name);
        }

        @Override
        public void loadHandle(int program) {
            handle = GLES20.glGetUniformLocation(program, mName);
            printHandle();
            checkError();
        }
    }

    public static class AttributeShaderParameter extends ShaderParameter {
        public AttributeShaderParameter(String name) {
            super(name);
        }

        @Override
        public void loadHandle(int program) {
            handle = GLES20.glGetAttribLocation(program, mName);
            printHandle();
            checkError();
        }
    }
}
