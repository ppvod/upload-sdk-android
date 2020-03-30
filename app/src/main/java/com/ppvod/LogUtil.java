package com.ppvod;


import android.util.Log;

public class LogUtil {

    public static void info(String s) {
        Log.i("uploader",s);
    }

    public static void error(String s) {
        Log.e("uploader",s);
    }

    public static void debug(String s) {
        Log.d("uploader",s);
    }
}