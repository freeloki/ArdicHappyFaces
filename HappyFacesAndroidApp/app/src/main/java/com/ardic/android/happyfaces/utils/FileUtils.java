package com.ardic.android.happyfaces.utils;

import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;

public class FileUtils {

    public static synchronized boolean writeImageToFile(final Bitmap bmp, final String faceId) {

        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/faces__");
        myDir.mkdirs();

        String fname = "Image_" + faceId + "_" + System.currentTimeMillis() + ".png";
        File file = new File(myDir, fname);
          Log.i("PreviewImage", "" + file);
        if (file.exists())
            file.delete();
        try {
            FileOutputStream out = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
