package com.ardic.android.happyfaces.tracker;

import android.util.Log;

import com.ardic.android.happyfaces.camera.GraphicOverlay;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

/**
 * Face tracker for each detected individual. This maintains a face graphic within the app's
 * associated face overlay.
 */
public class GraphicFaceTracker extends Tracker<Face> {
    private GraphicOverlay mOverlay;
    private FaceGraphic mFaceGraphic;
    private int mPrevfaceColor = -1;

    public GraphicFaceTracker(GraphicOverlay overlay) {
        mOverlay = overlay;
        mFaceGraphic = new FaceGraphic(overlay);
    }

    /**
     * Start tracking the detected face instance within the face overlay.
     */
    @Override
    public void onNewItem(int faceId, Face item) {
        mFaceGraphic.setId(faceId);
        Log.i("Face", "face:" + faceId + "Obj? " + item.getPosition() + "  " + item.getLandmarks());
    }

    /**
     * Update the position/characteristics of the face within the overlay.
     */
    @Override
    public void onUpdate(final FaceDetector.Detections<Face> detectionResults, Face face) {


        mOverlay.add(mFaceGraphic);
        int faceColorfromClass = mFaceGraphic.getFaceColor();
        mFaceGraphic.updateFace(face);

        if (faceColorfromClass != mPrevfaceColor) {
            mPrevfaceColor = faceColorfromClass;
        }
    }


    /**
     * Hide the graphic when the corresponding face was not detected.  This can happen for
     * intermediate frames temporarily (e.g., if the face was momentarily blocked from
     * view).
     */
    @Override
    public void onMissing(FaceDetector.Detections<Face> detectionResults) {
        mOverlay.remove(mFaceGraphic);
        Log.i("humf", "onMissing " + mPrevfaceColor);


    }

    /**
     * Called when the face is assumed to be gone for good. Remove the graphic annotation from
     * the overlay.
     */
    @Override
    public void onDone() {
        mOverlay.remove(mFaceGraphic);
    }
}
