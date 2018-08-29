package com.ardic.android.happyfaces;
import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ardic.android.happyfaces.camera.CameraSourcePreview;
import com.ardic.android.happyfaces.camera.GraphicOverlay;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends Activity implements ResultListener,  AllFacesResultListener {
    private static final String TAG = "MainActivity";

    private CameraSource mCameraSource = null;

    private CameraSourcePreview mPreview;
    private GraphicOverlay mGraphicOverlay;
    private LinearLayout mAwesomeLayout;
    private TextView tfModelResultTextView;
    private TextView mFaceColorPreview;
    private ImageView profilePhotoImageView;
    String PROFILEresult;
    private TextView detectionResultTextView;
    // permission request codes need to be < 256
    private static final int RC_HANDLE_CAMERA_PERM = 2;
    private TFbridge tFbridge;
    private MyFaceDetector myFaceDetector;
    private static final int COLOR_CHOICES[] = {
            Color.BLUE,
            Color.CYAN,
            Color.GREEN,
            Color.MAGENTA,
            Color.RED,
            Color.WHITE,
            Color.YELLOW,
            Color.BLACK
    };
    ArrayList<Integer> mFaceColorList, mFaceColorList2;
    ResultListener listRes;
    int threadCount=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_layout_activity);
        mAwesomeLayout = findViewById(R.id.cameraPreviewLinearLayout);

        mPreview = findViewById(R.id.preview);
        //mPreview.setLayoutParams(new LinearLayout.LayoutParams(640,480));
        Log.i("humf", "preview size: "+mPreview.getHeight()+"|"+mPreview.getWidth());
        mGraphicOverlay = findViewById(R.id.faceOverlay);

        tfModelResultTextView =findViewById(R.id.nmf);
        detectionResultTextView=findViewById(R.id.detectResult);
        profilePhotoImageView=findViewById(R.id.profilephoto);
        mFaceColorPreview=findViewById(R.id.FaceColortextView);

        // Check for the camera permission before accessing the camera.  If the
        // permission is not granted yet, request permission.
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA}, 50); Log.e("humf", "after create camera --TRUE");

            requestCameraPermission();
        }
        else{
            createCameraSource();
        }

    }

    /**
     * Handles the requesting of the camera permission.  This includes
     * showing a "Snackbar" message of why the permission is needed then
     * sending the request.
     */
    private void requestCameraPermission() {
        Log.w(TAG, "Camera permission is not granted. Requesting permission");

        final String[] permissions = new String[]{Manifest.permission.CAMERA};

        if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_CAMERA_PERM);
            return;
        }
        PROFILEresult="app_icon.png";
        profilePhotoImageView.setImageDrawable(Drawable.createFromPath("//drawable-v24/"+profilePhotoImageView));

        final Activity thisActivity = this;

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(thisActivity, permissions,
                        RC_HANDLE_CAMERA_PERM);
            }
        };

        Snackbar.make(mGraphicOverlay, R.string.permission_camera_rationale,
                Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.ok, listener)
                .show();
    }

    private void createCameraSource() {

        Context context = getApplicationContext();
        //set the tfbridge
        tFbridge=new TFbridge(context);
        mFaceColorList=new ArrayList<>();
        mFaceColorList.add(-1);
        mFaceColorList2=new ArrayList<>();

        // You can use your own settings for your detector
        FaceDetector detector = new FaceDetector.Builder(context)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .setProminentFaceOnly(false)
                .setTrackingEnabled(true)
                .build();

        // This is how you merge myFaceDetector and google.vision detector

       /* detector.setProcessor(
                new MultiProcessor.Builder<>(new GraphicFaceTrackerFactory(this)).setMaxGapFrames(5)
                        .build());*/

        if (!detector.isOperational()) {
            Log.w(TAG, "Face detector dependencies are not yet available.");
        }
       myFaceDetector = new MyFaceDetector(detector, context);
        // You can use your own processor
      //  myFaceDetector.
       myFaceDetector.setProcessor(
              new MultiProcessor.Builder<>(new GraphicFaceTrackerFactory(this))
                       .build());
        listRes=this;
        //myFaceDetector.setContext(context);
     if (!myFaceDetector.isOperational()) {
           Log.w(TAG, "Face detector dependencies are not yet available.");
       }

     /*   MultiDetector multiDetector = new MultiDetector.Builder()
                .add(detector)
                .add(myFaceDetector)
                .build();*/

        // You can use your own settings for CameraSource
        mCameraSource = new CameraSource.Builder(context, myFaceDetector)
                .setFacing(CameraSource.CAMERA_FACING_FRONT)
                .setRequestedPreviewSize(320, 240)
                .setAutoFocusEnabled(true)
                .setRequestedFps(5.0f)
                .build();


    }

    /**
     * Restarts the camera.
     */
    @Override
    protected void onResume() {
        super.onResume();

        startCameraSource();
    }

    /**
     * Stops the camera.
     */
    @Override
    protected void onPause() {
        super.onPause();
        mPreview.stop();
    }

    /**
     * Releases the resources associated with the camera source, the associated detector, and the
     * rest of the processing pipeline.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCameraSource != null) {
            mCameraSource.release();
        }
    }

    /**
     * Callback for the result from requesting permissions. This method
     * is invoked for every call on {@link #requestPermissions(String[], int)}.
     * <p>
     * <strong>Note:</strong> It is possible that the permissions request interaction
     * with the user is interrupted. In this case you will receive empty permissions
     * and results arrays which should be treated as a cancellation.
     * </p>
     *
     * @param requestCode  The request code passed in {@link #requestPermissions(String[], int)}.
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *                     which is either {@link PackageManager#PERMISSION_GRANTED}
     *                     or {@link PackageManager#PERMISSION_DENIED}. Never null.
     * @see #requestPermissions(String[], int)
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode != RC_HANDLE_CAMERA_PERM) {
            Log.d(TAG, "Got unexpected permission result: " + requestCode);
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Camera permission granted - initialize the camera source");
            // we have permission, so create the camerasource
            createCameraSource();
            return;
        }

        Log.e(TAG, "Permission not granted: results len = " + grantResults.length +
                " Result code = " + (grantResults.length > 0 ? grantResults[0] : "(empty)"));

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(" MainActivity")
                .setMessage(R.string.no_camera_permission)
                .setPositiveButton(R.string.ok, listener)
                .show();
    }

    //==============================================================================================
    // Camera Source Preview
    //==============================================================================================

    /**
     * Starts or restarts the camera source, if it exists.  If the camera source doesn't exist yet
     * (e.g., because onResume was called before the camera source was created), this will be called
     * again when the camera source is created.
     */
    private void startCameraSource() {


        // check that the device has play services available.
        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
                getApplicationContext());
        if (code != ConnectionResult.SUCCESS) {
            Dialog dlg =
                    GoogleApiAvailability.getInstance().getErrorDialog(this, code, 5000);
            dlg.show();
        }

        if (mCameraSource != null) {
            try {
                mPreview.start(mCameraSource, mGraphicOverlay);
            } catch (IOException e) {
                Log.e(TAG, "Unable to start camera source.", e);
                mCameraSource.release();
                mCameraSource = null;
            }
        }
    }

    @Override
    public void showResults(final String result,final int value,final boolean addORremove) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                /*f(addORremove==true){
                    mFaceColorList2.add(value);
                }
                else{
                    mFaceColorList2.remove(mFaceColorList.size()-1);

                }*/
                if(value==-1){
                   tfModelResultTextView.setTextColor(COLOR_CHOICES[7]);
                }
                tfModelResultTextView.setTextColor(COLOR_CHOICES[value]);


                tfModelResultTextView.setText(result);
            }
        });




    }

    @Override
    public void previewImage(final Bitmap bmp) {



        Log.i("Humfy", " threadzzzzzzz :" + threadCount);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "canvas:>>  "+bmp);

                Log.i(TAG,"" + bmp.getWidth() + " x " +  bmp.getHeight());
              //  Drawable d = new BitmapDrawable(getResources(), bmp);
                profilePhotoImageView.setImageBitmap(bmp);




                new Thread(new Runnable() {
                    @Override
                    public void run() {

                      Log.i("Humfy", tFbridge.recognizeImagewithTf(bmp) + " threadId :" + threadCount);
                      threadCount++;
                    }
                }).start();


            }
        });
    }

    @Override
    public void showAllFaces(String result, int value) {

    }

    @Override
    public void tfResult(final String str) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                tfModelResultTextView.setText(str);


            }
        });
    }

//==============================================================================================
    // Graphic Face Tracker
    //==============================================================================================

    /**
     * Factory for creating a face tracker to be associated with a new face.  The multiprocessor
     * uses this factory to create face trackers as needed -- one for each individual.
     */
    private class GraphicFaceTrackerFactory implements MultiProcessor.Factory<Face>{
        ResultListener Rlistener;
        GraphicFaceTrackerFactory(ResultListener listener){
            super();
            String facelistStr="";
            for (int i = 1; i <mFaceColorList2.size() ; i++) {
                facelistStr+=i+". >>face color is ";
                switch (mFaceColorList2.get(i)){
                    case 0: facelistStr+="BLUE";
                        break;
                    case 1: facelistStr+="CYAN";
                        break;
                    case 2: facelistStr+="GREEN";
                        break;
                    case 3: facelistStr+="MAGENTA";
                        break;
                    case 4: facelistStr+="RED";
                        break;
                    case 5: facelistStr+="WHITE";
                        break;
                    case 6: facelistStr+="YELLOW";
                        break;
                    case 7: facelistStr+="BLACK";
                        break;

                }
                facelistStr+="\n";
            }

            Log.i("humf", "list2"+ mFaceColorList2);
            mFaceColorPreview.setText(facelistStr);
            Rlistener=listener;
        }
        @Override
        public Tracker<Face> create(Face face) {
            return new GraphicFaceTracker(mGraphicOverlay, Rlistener);
        }
    }

    /**
     * Face tracker for each detected individual. This maintains a face graphic within the app's
     * associated face overlay.
     */
    private class GraphicFaceTracker extends Tracker<Face>{
        private GraphicOverlay mOverlay;
        private FaceGraphic mFaceGraphic;
        private int mPrevfaceColor =-1;
        ResultListener mlistenerColor;
        private String TFresultStr;
        private final int FrameStatus=10;
        private  int frameCount=0;
        GraphicFaceTracker(GraphicOverlay overlay, ResultListener listener) {
            mOverlay = overlay;
            mFaceGraphic = new FaceGraphic(overlay);
            mlistenerColor=listener;
           // mFaceGraphic.setmRlistener(mlistenerColor);
        }

        /**
         * Start tracking the detected face instance within the face overlay.
         */
        @Override
        public void onNewItem(int faceId, Face item) {
            mFaceGraphic.setId(faceId);

          Log.i("Face", "face:" + faceId + "Obj? "+ item.getPosition() + "  " + item.getLandmarks() );


        }

        /**
         * Update the position/characteristics of the face within the overlay.
         */
        @Override
        public void onUpdate(FaceDetector.Detections<Face> detectionResults, Face face) {
            ;


          //  detectionResults.getFrameMetadata().

            mOverlay.add(mFaceGraphic);
            String strface="face color";
            int faceColorfromClass=mFaceGraphic.getFaceColor();
            mFaceGraphic.updateFace(face);

            if(faceColorfromClass!= mPrevfaceColor) {
                mFaceColorList.add(faceColorfromClass);
                mPrevfaceColor =faceColorfromClass;
                Log.i("color", "new>>" + mPrevfaceColor);
                Log.i("humf", "list"+ mFaceColorList);
                mlistenerColor.showResults("result", mPrevfaceColor, true);
                myFaceDetector.setFace(face);
                int x1 = (int) face.getPosition().x;
                int y1 = (int) face.getPosition().y;
                int width = (int) face.getWidth();
                int height = (int) face.getHeight();

                if (y1 < 0) {
                    y1 = Math.abs((int) face.getPosition().y);
                } else if (y1 > height) {
                    y1 = height - 1;
                }
                if (x1 < 0) {
                    x1 = Math.abs((int) face.getPosition().x);
                } else if (x1 + width > width) {
                    x1 = width - 1;
                }
                //boyle olmasi lazim  bir de yatay!!!!!!
                final Bitmap resizedbitmap1 = Bitmap.createBitmap((myFaceDetector.getmBitmap()), x1, y1, width, height);
                mlistenerColor.previewImage(resizedbitmap1);
                final Runnable r = new Runnable() {
                    public void run() {
                        TFresultStr=tFbridge.recognizeImagewithTf(resizedbitmap1);
                        Log.i(TAG, "RESULT>> "+TFresultStr);
                    }
                };



            }
            else{
               Log.i("color", "equal>>"+ mPrevfaceColor);
                Log.i("humf", "list"+ mFaceColorList);
/*                if(FrameStatus==frameCount){
                    int x1 = (int) face.getPosition().x;
                    int y1 = (int) face.getPosition().y;
                    int width = (int) face.getWidth();
                    int height = (int) face.getHeight();

                    if (y1 < 0) {
                        y1 = Math.abs((int) face.getPosition().y);
                    } else if (y1 > height) {
                        y1 = height - 1;
                    }
                    if (x1 < 0) {
                        x1 = Math.abs((int) face.getPosition().x);
                    } else if (x1 + width > width) {
                        x1 = width - 1;
                    }
                    //boyle olmasi lazim  bir de yatay!!!!!!
                    Log.i(TAG, "FACE: "+face.getWidth()+" X "+face.getHeight()+"  "+face.getPosition().x+" X "+face.getPosition().y+"   "+face.getEulerY()+" X "+face.getEulerZ());
                    final Bitmap resizedbitmap1 = Bitmap.createBitmap(myFaceDetector.getmBitmap(), x1, y1, width, height);
                    mlistenerColor.previewImage(resizedbitmap1);

                    frameCount=0;
                }*/
                frameCount++;

            }
            Log.i(TAG,"!!!!         " +  mOverlay.getCanvas());





        }

        /**
         * Hide the graphic when the corresponding face was not detected.  This can happen for
         * intermediate frames temporarily (e.g., if the face was momentarily blocked from
         * view).
         */
        @Override
        public void onMissing(FaceDetector.Detections<Face> detectionResults) {
            mOverlay.remove(mFaceGraphic);
            Log.i("humf", "onMissing "+mPrevfaceColor);




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
}