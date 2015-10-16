package com.erc.dal;

/**
 * Created by einar on 8/31/2015.
 */
public class Log {
    public static void e(String message, Exception e) {
        String fullMessagemessage = "ERROR: " + message +
                ", Message:" + e.getMessage() +
                ", LocalizedMessage:" + e.getLocalizedMessage() +
                ", ToString:" + e.toString();
        android.util.Log.e(Ctt.TAG, fullMessagemessage);
        e.printStackTrace();
    }

    public static void w(String message) {
        android.util.Log.w(Ctt.TAG, message);
    }

    public static void i(String message) {
        android.util.Log.i(Ctt.TAG, message);
    }
}
