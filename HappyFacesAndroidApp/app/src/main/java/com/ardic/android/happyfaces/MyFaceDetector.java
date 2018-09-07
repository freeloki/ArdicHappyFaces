package com.ardic.android.happyfaces;


import android.util.SparseArray;

import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;

public class MyFaceDetector extends Detector<Face> {
    private Detector<Face> mDelegate;
    private ResultListener mListener;
    private Face mFace;
    private boolean FaceStatus;

    public MyFaceDetector(Detector<Face> delegate, ResultListener listener) {

        mDelegate = delegate;
        mListener = listener;

    }

    public SparseArray<Face> detect(Frame frame) {

        SparseArray<Face> awesomeFaces = mDelegate.detect(frame);


        if (mListener != null && awesomeFaces.size() > 0) {
            mListener.onFaceFrame(frame, awesomeFaces);
        }
        return awesomeFaces;
    }


    public boolean isOperational() {
        return mDelegate.isOperational();
    }

    public boolean setFocus(int id) {

        return mDelegate.setFocus(id);
    }

}
