package com.ardic.android.happyfaces.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.RequiresApi;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;

public class FileUtils {

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static synchronized boolean writeImageToFile(final Bitmap bmp, final String faceId) {

        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/faces/" + faceId);
        myDir.mkdirs();

        String fname = "Image_" + faceId + "_" + System.currentTimeMillis() + ".jpg";
        File file = new File(myDir, fname);
        //Log.i("PreviewImage", "" + file);
        if (file.exists())
            file.delete();
        try {
            FileOutputStream out = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static Bitmap getCroppedBitmap(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = Color.BLACK;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);

        float x = bitmap.getWidth() / 2.0f;
        float y = bitmap.getHeight() / 2.0f;

        float mLeftOffset = x * 0.83f;
        float mTopOffset = y * 0.85f;
        float mRightOffset = x * 0.83f;
        float mBottomOffset = y;


        float mLeft = x - mLeftOffset;
        float mTop = y - mTopOffset;
        float mRight = x + mRightOffset;
        float mBottom = y + mBottomOffset;


        canvas.drawOval(mLeft, mTop, mRight, mBottom, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);


        Bitmap output2 = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas2 = new Canvas(output2);
        canvas2.drawARGB(255, 0, 0, 0);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));
        canvas2.drawBitmap(output, rect, rect, paint);
        return output2;
    }
}
