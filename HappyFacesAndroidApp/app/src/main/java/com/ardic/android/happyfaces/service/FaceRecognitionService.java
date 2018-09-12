package com.ardic.android.happyfaces.service;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.ardic.android.happyfaces.listener.ResultListener;
import com.ardic.android.happyfaces.model.ArdicFace;
import com.ardic.android.happyfaces.model.FaceResultViewHolder;
import com.ardic.android.happyfaces.tensorflow.TensorFlowBridge;
import com.ardic.android.happyfaces.utils.FileUtils;

import java.util.List;

public class FaceRecognitionService extends Service {
    private static final String TAG = FaceRecognitionService.class.getSimpleName();

    private TensorFlowBridge mTfTensorFlowBridge;
    private ResultListener mResultListener;

    private final IBinder mBinder = new FaceRecognitionServiceBinder();

    public class FaceRecognitionServiceBinder extends Binder {

        public FaceRecognitionService getService() {
            return FaceRecognitionService.this;
        }
    }

    public FaceRecognitionService() {
    }

    @Override
    public IBinder onBind(Intent intent) {

        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "FaceRecognitionService onCreate");
        mTfTensorFlowBridge = new TensorFlowBridge(getApplicationContext());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "FaceRecognitionService onStart");

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "FaceRecognitionService onDestroy");

        super.onDestroy();
    }

    public void setResultListener(ResultListener mResultListener) {
        this.mResultListener = mResultListener;
    }

    public List<ArdicFace> recognizeImage(Bitmap img, int faceId) {

        //  Log.i(TAG, "FACEDETECTOR CALLLEDDDDD!!!!! " + faceId);

        long temp = System.currentTimeMillis();
        List<ArdicFace> tfresult = mTfTensorFlowBridge.recognizeTensorFlowImage(img);
        Log.i("PreviewImage", tfresult.size() + " Possible Faces Detected in :" + (System.currentTimeMillis() - temp) + " ms.");

        if (!tfresult.isEmpty() && tfresult.size() == 1 && tfresult.get(0).getConfidence() > TensorFlowBridge.CONFIDENCE_PERCENTAGE) {

            ArdicFace face = tfresult.get(0);
            String resultMsg = "Welcome, " + face.getName() + " have a nice day.(%" + face.getPercentage() + ")";
            FaceResultViewHolder mFaceResultViewHolder = new FaceResultViewHolder(face, resultMsg, img);
            mResultListener.previewResult(mFaceResultViewHolder);
        } else {
            ArdicFace guestFace = new ArdicFace("NONE", "NONE", 0, getApplicationContext());

            String resultMsg = "Welcome Guest, We couldn't recognize you just for now :( But you look like:\n[";
            for (ArdicFace face : tfresult) {

                if (tfresult.get(tfresult.size() - 1).getName().equals(face.getName())) {
                    resultMsg += " " + face.getTitle() + " (%" + face.getPercentage() + ")].";
                } else {
                    resultMsg += " " + face.getTitle() + " (%" + face.getPercentage() + "), ";
                }
            }
            FaceResultViewHolder mFaceResultViewHolder = new FaceResultViewHolder(guestFace, resultMsg, img);
            mResultListener.previewResult(mFaceResultViewHolder);
        }
        return tfresult;
    }
}
