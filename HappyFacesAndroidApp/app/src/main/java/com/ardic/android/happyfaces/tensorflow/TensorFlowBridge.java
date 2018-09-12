package com.ardic.android.happyfaces.tensorflow;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.ardic.android.happyfaces.model.ArdicFace;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TensorFlowBridge {
    private static final int INPUT_SIZE = 160;
    private static final int IMAGE_MEAN = 0;
    private static final float IMAGE_STD = 255;
    private static final String INPUT_NAME = "Placeholder";
    private static final String OUTPUT_NAME = "final_result";
    public static final float CONFIDENCE_PERCENTAGE = 0.85f;
    //private static final String MODEL_FILE = "file:///android_asset/100_192_mobile_net_2.pb";
    //private static final String MODEL_FILE = "file:///android_asset/mobilenet_v2_100_160_output_graph.pb";
    //mobilenet_v2_100_128_output_graph
    private static final String MODEL_FILE = "file:///android_asset/mobilenet_v2_100_160_optimized_graph_3.pb";
    //private static final String MODEL_FILE = "file:///android_asset/mobilenet_v2_100_128_output_graph.pb";


    private static final String LABEL_FILE =
            "file:///android_asset/labels.txt";
    private Classifier classifier;
    private Context mContext;

    public TensorFlowBridge(Context context) {
        mContext = context.getApplicationContext();
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
            classifier.enableStatLogging(true);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public synchronized List<ArdicFace> recognizeTensorFlowImage(Bitmap bmp) {

        List<ArdicFace> faces = new ArrayList<>();
        if (bmp != null) {
            Bitmap bitmaptf = Bitmap.createScaledBitmap(bmp, INPUT_SIZE, INPUT_SIZE, false);
            List<Classifier.Recognition> imgrecognize = classifier.recognizeImage(bitmaptf);
           // Log.i("Humfy", "result: " + imgrecognize.toString());
           // Log.i("humf: ", "%>>" + imgrecognize.get(0).getConfidence() + "-");

            if (!imgrecognize.isEmpty()) {
                Classifier.Recognition mRecognition = imgrecognize.get(0);
                if (mRecognition.getConfidence() >= CONFIDENCE_PERCENTAGE) {
                    faces.add(new ArdicFace(mRecognition.getTitle(), mRecognition.getId(), mRecognition.getConfidence(), mContext));
                    return faces;

                } else {
                    for (Classifier.Recognition recognition : imgrecognize) {
                        faces.add(new ArdicFace(recognition.getTitle(), recognition.getId(), recognition.getConfidence(), mContext));
                    }
                    return faces;
                }
            }

        }
        return faces;
    }
}
