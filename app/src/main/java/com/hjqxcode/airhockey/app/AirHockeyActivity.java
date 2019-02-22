package com.hjqxcode.airhockey.app;

import android.opengl.GLSurfaceView;
import android.os.Debug;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.hjqxcode.airhockey.renderer.AirHockeyRenderer;
import com.hjqxcode.airhockey.util.Util;

public class AirHockeyActivity extends AppCompatActivity {
    private GLSurfaceView mGLSurfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Debug.waitForDebugger();
        super.onCreate(savedInstanceState);

        if (Util.supportES20(this)) {
            mGLSurfaceView = new GLSurfaceView(this);
            mGLSurfaceView.setEGLContextClientVersion(2);
            mGLSurfaceView.setRenderer(new AirHockeyRenderer(this));

            setContentView(mGLSurfaceView);
        } else {
            Toast.makeText(this, "Exit app due to not support OpenGL ES 2.0", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mGLSurfaceView != null) {
            mGLSurfaceView.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mGLSurfaceView != null) {
            mGLSurfaceView.onPause();
        }
    }
}
