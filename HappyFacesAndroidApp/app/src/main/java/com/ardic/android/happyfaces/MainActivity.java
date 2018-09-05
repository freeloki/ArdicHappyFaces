package com.ardic.android.happyfaces;
import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ardic.android.happyfaces.camera.CameraSourcePreview;
import com.ardic.android.happyfaces.camera.GraphicOverlay;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends Activity implements ResultListener {
    private static final String TAG = "MainActivity";

    private CameraSource mCameraSource = null;

    private CameraSourcePreview mPreview;
    private GraphicOverlay mGraphicOverlay;
    private LinearLayout mAwesomeLayout;
    private TextView tfModelResultTextView;
    private ImageView mSamplePhotopreview,mProfilePhotopreview ;
    private TextView detectionResultTextView;
    // permission request codes need to be < 256
    private static final int RC_HANDLE_CAMERA_PERM = 2;
    private TFbridge tFbridge;
    private MyFaceDetector myFaceDetector;
    private Context mContext;
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
    private ArrayList<Integer> mFaceColorList, mFaceColorList2;
    private ResultListener listRes;
    private int threadCount=0;
    private int mCurrentFaceId=-1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_layout_activity);
        mAwesomeLayout = findViewById(R.id.cameraPreviewLinearLayout);

        mPreview = findViewById(R.id.preview);

        Log.i("humf", "preview size: "+mPreview.getHeight()+"|"+mPreview.getWidth());
        mGraphicOverlay = findViewById(R.id.faceOverlay);

        tfModelResultTextView =findViewById(R.id.nmf);
        detectionResultTextView=findViewById(R.id.detectResult);
        mSamplePhotopreview =findViewById(R.id.samplephoto);
        mProfilePhotopreview=findViewById(R.id.profilePhoto);
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

        mSamplePhotopreview.setImageDrawable(Drawable.createFromPath("//drawable-v24/"+ mSamplePhotopreview));

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
        mContext=context;
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


        if (!detector.isOperational()) {
            Log.w(TAG, "Face detector dependencies are not yet available.");
        }
       myFaceDetector = new MyFaceDetector(detector, context, this);
        // You can use your own processor
      //  myFaceDetector.
       myFaceDetector.setProcessor(
              new MultiProcessor.Builder<>(new GraphicFaceTrackerFactory(this))
                       .build());
        listRes=this;

     if (!myFaceDetector.isOperational()) {
           Log.w(TAG, "Face detector dependencies are not yet available.");
       }


        // You can use your own settings for CameraSource
        mCameraSource = new CameraSource.Builder(context, myFaceDetector)
                .setFacing(CameraSource.CAMERA_FACING_FRONT)
                .setRequestedPreviewSize(320, 240)
                .setAutoFocusEnabled(true)
                .setRequestedFps(10.0f)
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
    public void showResults(final String result,final int value) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

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

        /*runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mSamplePhotopreview.setImageBitmap(bmp);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        long temp = System.currentTimeMillis();
                   String tfresult=tFbridge.recognizeImagewithTf(bmp);
                        Log.i("Humfy", tfresult+ " threadId :" + threadCount +"     ms:"+(System.currentTimeMillis()-temp));
                        previewProfilePhoto(tfresult);
                        tfResult(tfresult);

                      threadCount++;

                    }
                }).start();


            }
        });*/
    }


    @Override
    public void previewProfilePhoto(final String str) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                    mProfilePhotopreview.setImageDrawable(getPhotoFromDrawable(str));

            }
        });

    }

    @Override
    public void onFaceFrame(final Frame newFrame, final SparseArray<Face> faceSparseArray) {


        new Thread(new Runnable() {
            @Override
            public void run() {
                    YuvImage yuvImage = new YuvImage(newFrame.getGrayscaleImageData().array(), ImageFormat.NV21, newFrame.getMetadata().getWidth(), newFrame.getMetadata().getHeight(), null);
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    yuvImage.compressToJpeg(new Rect(0, 0, newFrame.getMetadata().getWidth(), newFrame.getMetadata().getHeight()), 100, byteArrayOutputStream);
                    byte[] jpegArray = byteArrayOutputStream.toByteArray();
                    final Bitmap tempBitmap = BitmapFactory.decodeByteArray(jpegArray, 0, jpegArray.length);
                    for (int i = 0; i < faceSparseArray.size(); i++) {
                    Face thisFace = faceSparseArray.valueAt(i);
                    int x1 = (int) (thisFace.getPosition().x);
                    int y1 = (int) (thisFace.getPosition().y);
                    int width=(int)thisFace.getWidth();
                    int height=(int)thisFace.getHeight();

                    Log.i("FaceId", ""+thisFace.getId());
                    Log.i("size:", ""+x1+"X"+y1+"   "+width+"X"+height);
                    if(x1<0){
                        x1=0;
                    }

                    if(y1<0){
                        y1=0;
                    }
                    if(x1>=0 && y1>=0 && width+x1<=tempBitmap.getWidth() && height+y1<=tempBitmap.getHeight()) {
                        final Bitmap resizedbitmap1 = /*Bitmap.createScaledBitmap(*/Bitmap.createBitmap(tempBitmap, x1, y1, width, height)/*, 299, 299, false)*/;

                        if(mCurrentFaceId!=thisFace.getId())  //give to TF
                        {
                            Log.i("Control", "1");
                           previewImage(resizedbitmap1);

                            mCurrentFaceId=thisFace.getId();
                        }
                        String root = Environment.getExternalStorageDirectory().toString();
                        File myDir = new File(root + "/faces__");
                        myDir.mkdirs();

                        String fname = "Image_" + mCurrentFaceId+"_" +System.currentTimeMillis()+ ".jpg";
                        File file = new File(myDir, fname);
                        Log.i("salam", "" + file);
                        if (file.exists())
                            file.delete();
                        try {
                            FileOutputStream out = new FileOutputStream(file);
                            resizedbitmap1.compress(Bitmap.CompressFormat.JPEG, 90, out);
                            out.flush();
                            out.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }





                }



                }

        }).start();
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

    private Bitmap flipBitmap(Bitmap btmp){
        Matrix matrix = new Matrix();
        float cx=btmp.getWidth()/2.0f;
        float cy=btmp.getHeight()/2.0f;
        matrix.postScale(-1, 1, cx, cy);
        return  Bitmap.createBitmap(btmp, 0, 0, btmp.getWidth(), btmp.getHeight(), matrix, true);
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
        GraphicFaceTracker(GraphicOverlay overlay, ResultListener listener) {
            mOverlay = overlay;
            mFaceGraphic = new FaceGraphic(overlay);
            mlistenerColor=listener;
        }

        /**
         * Start tracking the detected face instance within the face overlay.
         */
        @Override
        public void onNewItem(int faceId, Face item) {
            mFaceGraphic.setId(faceId);

          Log.i("Face", "face:" + faceId + "Obj? "+ item.getPosition() + "  " + item.getLandmarks() );

          mlistenerColor.previewProfilePhoto("NONE");

        }

        /**
         * Update the position/characteristics of the face within the overlay.
         */
        @Override
        public void onUpdate(final FaceDetector.Detections<Face> detectionResults, Face face) {


            mOverlay.add(mFaceGraphic);
            int faceColorfromClass=mFaceGraphic.getFaceColor();
            mFaceGraphic.updateFace(face);

            if(faceColorfromClass!= mPrevfaceColor) {
                mFaceColorList.add(faceColorfromClass);
                mPrevfaceColor =faceColorfromClass;
                Log.i("color", "new>>" + mPrevfaceColor);
                Log.i("humf", "list"+ mFaceColorList);
                mlistenerColor.showResults("result", mPrevfaceColor);
                mlistenerColor.previewProfilePhoto("NONE");
                myFaceDetector.setFace(face);
                final Face currentface=face;

            }
            else{
               Log.i("color", "equal>>"+ mPrevfaceColor);
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
            Log.i("humf", "onMissing "+mPrevfaceColor);




        }

        /**
         * Called when the face is assumed to be gone for good. Remove the graphic annotation from
         * the overlay.
         */
        @Override
        public void onDone() {
            mOverlay.remove(mFaceGraphic);
            mlistenerColor.showResults("result", mPrevfaceColor);
            mlistenerColor.previewProfilePhoto("NONE");


        }
    }
    private  Drawable getPhotoFromDrawable(final String str){
        Drawable retDrawable=getResources().getDrawable(R.drawable.app_icon);
        switch (str){
            case "taalaialmasova":
                retDrawable=getResources().getDrawable(R.drawable.taalaialmasova);
                break;
            case "afsincelik":
                retDrawable=getResources().getDrawable(R.drawable.afsincelik);
                break;
            case "ahmetcakman":
                retDrawable=getResources().getDrawable(R.drawable.profile_iconmin);
                break;
            case "alpparkan":
                retDrawable=getResources().getDrawable(R.drawable.alpparkan);
                break;

            case "barisinanc":
                retDrawable=getResources().getDrawable(R.drawable.barisinanc);
                break;
            case "ceyhunerturk":
                retDrawable=getResources().getDrawable(R.drawable.ceyhunerturk);
                break;

            case "duygukalinyilmaz":
                retDrawable=getResources().getDrawable(R.drawable.duygukalinyilmaz);
                break;
            case "ecegercekkayurtar":
                retDrawable=getResources().getDrawable(R.drawable.profile_iconmin);
                break;

            case "elifcakmak":
                retDrawable=getResources().getDrawable(R.drawable.elifcakmak);
                break;
            case "farshaddelirabdinia":
                retDrawable=getResources().getDrawable(R.drawable.farshaddelirabdinia);
                break;
            case "haluktufekci":
                retDrawable=getResources().getDrawable(R.drawable.haluktufekci);
                break;
            case "huseyinbashan":
                retDrawable=getResources().getDrawable(R.drawable.huseyinbashan);
                break;
            case "ibrahimtezcan":
                retDrawable=getResources().getDrawable(R.drawable.ibrahimtezcan);
                break;
            case "leventbabacan":
                retDrawable=getResources().getDrawable(R.drawable.leventbabacan);
                break;

            case "mertacel":
                retDrawable=getResources().getDrawable(R.drawable.mertacel);
                break;
            case "metinpar":
                retDrawable=getResources().getDrawable(R.drawable.metinpar);
                break;

            case "oguzcakir":
                retDrawable=getResources().getDrawable(R.drawable.oguzcakir);
                break;
            case "ozgurozkok":
                retDrawable=getResources().getDrawable(R.drawable.ozgurozkok);
                break;

            case "pascalstreamax":
                retDrawable=getResources().getDrawable(R.drawable.profile_iconmin);
                break;
            case "perihanmirkelam":
                retDrawable=getResources().getDrawable(R.drawable.perihanmirkelam);
                break;
            case "samiozdil":
                retDrawable=getResources().getDrawable(R.drawable.profile_iconmin);
                break;
            case "sinanpayaslioglu":
                retDrawable=getResources().getDrawable(R.drawable.sinanpayaslioglu);
                break;

            case "sonerugraskan":
                retDrawable=getResources().getDrawable(R.drawable.sonerugraskan);
                break;
            case "sukrankomurcu":
                retDrawable=getResources().getDrawable(R.drawable.sukrankomurcu);
                break;

            case "tunckahveci":
                retDrawable=getResources().getDrawable(R.drawable.tunckahveci);
                break;
            case "suleymanfakir":
                retDrawable=getResources().getDrawable(R.drawable.suleymanfakir);
                break;
            case "yavuzerzurumlu":
                retDrawable=getResources().getDrawable(R.drawable.yavuzerzurumlu);
                break;
            case "ugurgelisken":
                retDrawable=getResources().getDrawable(R.drawable.ugurgelisken);
                break;
            case "NONE":
                retDrawable=getResources().getDrawable(R.drawable.profile_iconmin);
                break;

        }
        return retDrawable;
    }
}