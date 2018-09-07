package com.ardic.android.happyfaces;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.media.FaceDetector;
import android.util.SparseArray;

import com.ardic.android.happyfaces.model.ArdicFace;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;

public interface ResultListener {
    void showResults(String result, int value);
    void previewImage(Bitmap btp);
    void tfResult(String str);
    void previewProfilePhoto(ArdicFace face);
    void onFaceFrame(final Frame frame, final SparseArray<Face> faceSparseArray);
}
