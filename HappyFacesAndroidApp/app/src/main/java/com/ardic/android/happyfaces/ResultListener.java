package com.ardic.android.happyfaces;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;

public interface ResultListener {
    void showResults(String result, int value, boolean addORremove);
    void previewImage(Bitmap btp);
    void tfResult(String str);
}
