package com.ardic.android.happyfaces;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
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

    Bitmap profilphoto;
    MyFaceDetector(Detector<Face> delegate, Context con) {


        mDelegate = delegate;
        context = con;
        mFace=null;

    }
    MyFaceDetector(Context cont){
        context = cont;
        mDelegate=this;
        mFace=null;
    }

    public SparseArray<Face> detect(Frame frame) {
        YuvImage yuvImage = new YuvImage(frame.getGrayscaleImageData().array(), ImageFormat.NV21, frame.getMetadata().getWidth(), frame.getMetadata().getHeight(), null);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        yuvImage.compressToJpeg(new Rect(0, 0, frame.getMetadata().getWidth(), frame.getMetadata().getHeight()), 100, byteArrayOutputStream);
        byte[] jpegArray = byteArrayOutputStream.toByteArray();
        Bitmap TempBitmap = BitmapFactory.decodeByteArray(jpegArray, 0, jpegArray.length);


        /*FaceDetector faceDetector = new FaceDetector.Builder(context).setTrackingEnabled(false).build();
        if (!faceDetector.isOperational()) {


        }*/
        Frame frameface = new Frame.Builder().setBitmap(TempBitmap).build();
        //get faceee
        if(mFace!=null){
            Log.i("myfacdetector:"," myfaceeeeeeeeeeeee");
        }
        Log.i("assets: ", context.getAssets()+"  *");
       //SparseArray<Face> faces = mDelegate.detect(frameface);
        //for (int i = 0; i < faces.size(); i++) {
        if(FaceStatus==true) {
            Face thisFace = mFace;
            float x = (thisFace.getPosition().x + thisFace.getWidth() / 2);
            float y = (thisFace.getPosition().y + thisFace.getHeight() / 2);
            float xOffset = (int) (thisFace.getWidth() / 2.0f);
            float yOffset = (int) (thisFace.getHeight() / 2.0f);
            int x1 = (int) thisFace.getPosition().x;
            int y1 = (int) thisFace.getPosition().y;
            int width = (int) thisFace.getWidth();
            int height = (int) thisFace.getHeight();
            int left = (int) (x - xOffset + 85.0f);
            int top = (int) (y - yOffset + 115.0f);
            int right = (int) (x + xOffset - 70.0f);
            int bottom = (int) (y + yOffset - 13.0f);
            System.out.println("fuile>>>>>>x=" + x1 + "   y=" + y1 + "     " + width + "      " + height);
            //boyle olmasi lazim  bir de yatay!!!!!!

            if (y1 < 0) {
                y1 = Math.abs((int) thisFace.getPosition().y);
            } else if (y1 > height) {
                y1 = height - 2;
            }
            if (x1 < 0) {
                x1 = Math.abs((int) thisFace.getPosition().x);
            } else if (x1 + width > width) {
                x1 = width - 2;
            }

            System.out.println("fuile>22>>>>>x=" + x1 + "   y=" + y1 + "     " + width + "      " + height);
            //boyle olmasi lazim  bir de yatay!!!!!!
            Bitmap resizedbitmap1 = Bitmap.createBitmap(TempBitmap, x1, y1, 299, 299, null, false);
            // Bitmap ne=Bitmap.createBitmap()

            /**#***********************************************************************/



            Bitmap bitmaptf = Bitmap.createScaledBitmap(resizedbitmap1, INPUT_SIZE, INPUT_SIZE, false);




            profilphoto=resizedbitmap1;

            if (resultListener != null) {
                //resultListener.showResults(tfmodel.get(0).getTitle().toString());
                //resultListener.showProfilePhoto(resizedbitmap1);
            }


            FaceStatus=false;
        }



        //faceDetector.release();

        return mDelegate.detect(frame);
    }

    public boolean isOperational() {
        return mDelegate.isOperational();
    }

    public boolean setFocus(int id) {
        return mDelegate.setFocus(id);
    }


    public void setOnResultListener(ResultListener listener) {
        resultListener = listener;
    }
    public Bitmap getProfilphoto(){
        return  profilphoto;
    }

    public void setmFace(Face face){
        mFace=face;
        FaceStatus=true;
    }


}
