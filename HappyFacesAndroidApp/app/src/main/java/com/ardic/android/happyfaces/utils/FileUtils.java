package com.ardic.android.happyfaces.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
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
    public static synchronized boolean writeImageToFile(final Bitmap bmp, final String faceId, String filename) {

        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/aaaaa/" + filename);
        myDir.mkdirs();

        String fname = "Image_" + faceId + "_" + System.currentTimeMillis() + ".jpg";
        File file = new File(myDir, fname);
        Log.i("PreviewImage", "" + file);
        if (file.exists())
            file.delete();
        try {
            FileOutputStream out = new FileOutputStream(file);
            getCroppedBitmap(bmp).compress(Bitmap.CompressFormat.JPEG, 100, out);
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

        final int color = 0XFF000000;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        // canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        // canvas.drawOval();
        //Bitmap _bmp = Bitmap.createScaledBitmap(output, 60, 60, false);
        //return _bmp;
        float x = bitmap.getWidth() / 2.0f;
        float y = bitmap.getHeight() / 2.0f;

        float mLeftOffset = x * 0.82f;
        float mTopOffset = y*0.80f;
        float mRightOffset = x*0.82f;
        float mBottomOffset = y*0.95f;


        float mLeft = x - mLeftOffset;
        float mTop = y - mTopOffset;
        float mRight = x + mRightOffset;
        float mBottom = y + mBottomOffset;


        canvas.drawOval(mLeft, mTop, mRight, mBottom, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);


        return output;
    }

    /**
     * Adjusts a horizontal value of the supplied value from the preview scale to the view
     * scale.
     */
    public static float scaleX(float horizontal) {
        return horizontal * 0.80f;
    }

    /**
     * Adjusts a vertical value of the supplied value from the preview scale to the view scale.
     */
    public static float scaleY(float vertical) {
        return vertical * 0.82f;
    }
}
