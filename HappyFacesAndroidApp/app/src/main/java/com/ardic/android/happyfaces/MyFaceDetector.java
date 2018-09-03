package com.ardic.android.happyfaces;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.util.Log;
import android.util.SparseArray;

import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


public class MyFaceDetector extends Detector<Face> {
    private Detector<Face> mDelegate;
    private Context context;
    private Face mFace;
    private static final int INPUT_SIZE = 299;
    private boolean FaceStatus=false;
    private ResultListener resultListener;
    private Bitmap mBitmap;
    private Bitmap profilphoto;
    private ResultListener mListener;
    public MyFaceDetector(Detector<Face> delegate, Context con, ResultListener listener) {


        mDelegate = delegate;
        context = con;
        mFace=null;
        mListener=listener;

    }

    public SparseArray<Face> detect(Frame frame) {

        SparseArray<Face> awesomeFaces = mDelegate.detect(frame);



        if(mListener != null && awesomeFaces.size() > 0) {
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

    public Bitmap getmBitmap() {
        return mBitmap;
    }

    public void setOnResultListener(ResultListener listener) {
        resultListener = listener;
    }
    public Bitmap getProfilphoto(){
        return  profilphoto;
    }

    public void setFace(Face face){
        mFace=face;
        FaceStatus=true;
    }
    private Bitmap flipBitmap(Bitmap btmp){
        Matrix matrix = new Matrix();
        float cx=btmp.getWidth()/2.0f;
        float cy=btmp.getHeight()/2.0f;
        matrix.postScale(-1, 1, cx, cy);
        return  Bitmap.createBitmap(btmp, 0, 0, btmp.getWidth(), btmp.getHeight(), matrix, true);
    }

}
