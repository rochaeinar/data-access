package com.erc.dal;

/**
 * Created by einar on 8/31/2015.
 */
public class Log {
    public static void e(String message, Exception e) {
        if (e == null) {
            e = new Exception();
        }
        String fullMessagemessage = "ERROR: " + message +
                ", Message:" + e.getMessage() +
                ", LocalizedMessage:" + e.getLocalizedMessage() +
                ", ToString:" + e.toString();
        android.util.Log.e(Constant.TAG, fullMessagemessage);
        e.printStackTrace();
    }

    public static void w(String message) {
        android.util.Log.w(Constant.TAG, message);
    }

    public static void i(String message) {
        android.util.Log.i(Constant.TAG, message);
    }
}
