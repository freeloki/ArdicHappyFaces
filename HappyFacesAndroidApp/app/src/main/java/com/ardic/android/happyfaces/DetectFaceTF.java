package com.ardic.android.happyfaces;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.google.android.gms.vision.face.Face;

import java.io.IOException;
import java.util.List;

public class DetectFaceTF {
    Face mFace;
    private Context mContext;
    private static final int INPUT_SIZE = 299;
    private static final int IMAGE_MEAN = 0;
    private static final float IMAGE_STD = 255;
    private static final String INPUT_NAME = "Placeholder";
    private static final String OUTPUT_NAME = "final_result";

    private static final String MODEL_FILE = "file:///android_asset/output_graph.pb";
    private static final String LABEL_FILE =
            "file:///android_asset/labels.txt";
    private Classifier classifier;
    private ResultListener resultListener;
    public DetectFaceTF(Face face, Context context){
        mContext=context;
        try {
            classifier = TensorFlowImageClassifier.create(
                    mContext.getAssets(),
                    MODEL_FILE,
                    LABEL_FILE,
                    INPUT_SIZE,
                    IMAGE_MEAN,
                    IMAGE_STD,
                    INPUT_NAME,
                    OUTPUT_NAME);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mFace=face;
    }
    private void WhoseFACEisDetected(Face face, Bitmap TempBitmap){
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
            y1 = height-2;
        }
        if (x1 < 0) {
            x1 = Math.abs((int) thisFace.getPosition().x);
        } else if (x1+width > width) {
            x1 = width-2;
        }

       Bitmap resizedbitmap1= Bitmap.createBitmap(TempBitmap, (int)thisFace.getPosition().x, y1, INPUT_SIZE, INPUT_SIZE, null, false);
        // Bitmap ne=Bitmap.createBitmap()

        /**#***********************************************************************/
       Log.i("humf","1");
        Bitmap bitmaptf = Bitmap.createScaledBitmap(resizedbitmap1, INPUT_SIZE, INPUT_SIZE, false);

        Log.i("humf","2\n" + bitmaptf.getWidth() + "    ?    " + bitmaptf.getHeight() );

        // imageViewResult.setImageBitmap(bitmaptf);

        Log.i("humf","666\n" + classifier);

        List<Classifier.Recognition> tfmodel = classifier.recognizeImage(bitmaptf);

        System.out.println("Result>> "  + " >> " + tfmodel.toString());

        if(resultListener != null) {
            resultListener.showResults(tfmodel.toString());
        }
    }
    public void setOnResultListener(ResultListener listener) {
        resultListener = listener;
    }
}