package com.erc.dataaccesslayer;

import android.content.Context;

import com.erc.dal.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * Created by einar on 9/5/2015.
 */
public class Util {

    public static String getAppPath(Context context) {
        return context.getFilesDir() + "/";
    }

    public static void copyRawFileToSdCard(int idFileRaw, String fullPath, Context context) {
        try {
            InputStream in = context.getResources().openRawResource(idFileRaw);
            File file = new File(fullPath);
            File folder = new File(file.getParent());
            if (!folder.exists()) {
                folder.mkdirs();
            }
            FileOutputStream out = new FileOutputStream(fullPath);
            byte[] buff = new byte[65536];
            int read = 0;
            try {
                while ((read = in.read(buff)) > 0) {
                    out.write(buff, 0, read);
                }
            } finally {

                in.close();
                out.close();
            }
        } catch (Exception e) {
            Log.e("copyFileRawToSdCard" + fullPath, e);
        }
    }
}
