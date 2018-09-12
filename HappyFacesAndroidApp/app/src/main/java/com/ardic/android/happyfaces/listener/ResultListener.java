package com.ardic.android.happyfaces.listener;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.media.FaceDetector;
import android.util.SparseArray;

import com.ardic.android.happyfaces.model.ArdicFace;
import com.ardic.android.happyfaces.model.FaceResultViewHolder;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;

public interface ResultListener {
    void previewResult(FaceResultViewHolder resultView);

    void onFaceFrame(final Frame frame, final SparseArray<Face> faceSparseArray);
}
