package com.ardic.android.happyfaces;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class TFbridge {
    private static final int INPUT_SIZE = 299;
    private static final int IMAGE_MEAN = 0;
    private static final float IMAGE_STD = 255;
    private static final String INPUT_NAME = "Placeholder";
    private static final String OUTPUT_NAME = "final_result";

    private static final String MODEL_FILE = "file:///android_asset/output_graph.pb";
    private static final String LABEL_FILE =
            "file:///android_asset/labels.txt";
    private Classifier classifier;
    private Executor executor = Executors.newSingleThreadExecutor();
    private Context mContext;
    private String mTFresult;
    public TFbridge(Classifier classifier) {
        this.classifier = classifier;
    }
    public TFbridge(Bitmap imgTodetect, Context context){
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
        List<Classifier.Recognition>imgrecognize=classifier.recognizeImage(imgTodetect);
        mTFresult=imgrecognize.toString();

    }

    public TFbridge(Context context) {
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
    }

    public String getmTFresult() {
        return mTFresult;
    }

    public synchronized String recognizeImagewithTf(Bitmap bmp) {
        if(bmp!=null) {
            Bitmap bitmaptf = Bitmap.createScaledBitmap(bmp, INPUT_SIZE, INPUT_SIZE, false);
            List<Classifier.Recognition> imgrecognize = classifier.recognizeImage(bitmaptf);
            Log.i("Humfy", "result: "+imgrecognize.toString());
            Log.i("humf: ", "%>>" + imgrecognize.get(0).getConfidence() + "-");

            if(imgrecognize.get(0).getConfidence()>0.10f){
                mTFresult = imgrecognize.get(0).getTitle();
            }
            else{
                mTFresult="NONE";
            }

        }
        return mTFresult;
    }
}
