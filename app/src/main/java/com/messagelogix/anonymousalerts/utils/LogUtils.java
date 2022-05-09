package com.messagelogix.anonymousalerts.utils;

import android.util.Log;

import com.messagelogix.anonymousalerts.BuildConfig;

public class LogUtils {

    public static void debug(final String tag, String message) {
        if(BuildConfig.DEBUG) {
            Log.d(tag, message);
        }
    }
}