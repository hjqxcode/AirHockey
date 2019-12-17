package com.hjqxcode.airhockey.util;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.util.Log;

public class Util {
    private static final String TAG = "Util";

    public static boolean supportES20(Context context) {
        final ActivityManager amg = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        final ConfigurationInfo cfgInfo = amg.getDeviceConfigurationInfo();
        Log.v(TAG, "ConfigurationInfo.reqGlEsVersion: " + cfgInfo.reqGlEsVersion);

        boolean supportES20 = cfgInfo.reqGlEsVersion >= 0x20000;
        return supportES20;
    }

    // Returns the input value x clamped to the range [min, max].
    public static float clamp(float x, float min, float max) {
        if (x > max) return max;
        if (x < min) return min;
        return x;
    }
}
