package com.ardic.android.happyfaces.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.ardic.android.happyfaces.R;
import com.ardic.android.happyfaces.listener.ResultListener;
import com.ardic.android.happyfaces.camera.CameraSourcePreview;
import com.ardic.android.happyfaces.camera.GraphicOverlay;
import com.ardic.android.happyfaces.detector.MyFaceDetector;
import com.ardic.android.happyfaces.model.ArdicFace;
import com.ardic.android.happyfaces.model.FaceResultViewHolder;
import com.ardic.android.happyfaces.service.FaceRecognitionService;
import com.ardic.android.happyfaces.tracker.GraphicFaceTrackerFactory;
import com.ardic.android.happyfaces.utils.FileUtils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class MainActivity extends Activity implements ResultListener {
    private static final String TAG = "MainActivity";

    private CameraSource mCameraSource = null;

    private CameraSourcePreview mPreview;
    private GraphicOverlay mGraphicOverlay;
    private ImageView mSampleInputPhotoPreview, mSampleOutputPhotoPreview, mProfilePhotopreview;
    private TextView mResultTextView, mProfileName, mProfileSurname;
    // permission request codes need to be < 256
    private static final int RC_HANDLE_CAMERA_PERM = 2;
    private MyFaceDetector myFaceDetector;
    private FaceRecognitionService mService;
    private boolean mBound = false;

    private List<Integer> trackingIds = new CopyOnWriteArrayList<>();
    private ImageButton mButtonOptions;
    private SharedPreferences mPreferences;
    private SharedPreferences.Editor mEditorPrefernces ;
    private boolean isFileSettingsEnabled;
    private boolean isTensorFlowEnabled;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_layout_activity);

        //Shared Preferences
        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mEditorPrefernces = mPreferences.edit();
        Log.i("Preferences", mPreferences.getString("FileEnableSettings", ""));
        //#######################################################################
        Intent intent = new Intent(this, FaceRecognitionService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        mPreview = findViewById(R.id.preview);


        Log.i("humf", "preview size: " + mPreview.getHeight() + "|" + mPreview.getWidth());
        mGraphicOverlay = findViewById(R.id.faceOverlay);

        mSampleInputPhotoPreview = findViewById(R.id.input_photo);
        mSampleOutputPhotoPreview = findViewById(R.id.output_photo);
        mProfilePhotopreview = findViewById(R.id.beautiful_profile_photo);
        mResultTextView = findViewById(R.id.result_text_view);
        mProfileName = findViewById(R.id.profile_info_name);
        mProfileSurname = findViewById(R.id.profile_info_surname);
        mButtonOptions=findViewById(R.id.button3);
        mButtonOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectActivity();
            }
        });
        // Check for the camera permission before accessing the camera.  If the
        // permission is not granted yet, request permission.
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA}, 50);
            Log.e("humf", "after create camera --TRUE");

            requestCameraPermission();
        } else {
            createCameraSource();
        }

    }

    private void selectActivity() {
        Intent intent = new Intent(this, SelectActivity.class);
        //startActivity(intent);
        MainActivity.this.startActivity(intent);
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

        mSampleInputPhotoPreview.setImageDrawable(Drawable.createFromPath("//drawable-v24/" + mSampleInputPhotoPreview));

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


        // You can use your own settings for your detector
        FaceDetector detector = new FaceDetector.Builder(getApplicationContext())
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .setProminentFaceOnly(false)
                .setTrackingEnabled(true)
                .setMode(FaceDetector.ACCURATE_MODE)
                .build();

        // This is how you merge myFaceDetector and google.vision detector


        if (!detector.isOperational()) {
            Log.w(TAG, "Face detector dependencies are not yet available.");
        }
        myFaceDetector = new MyFaceDetector(detector, this);
        // You can use your own processor
        //  myFaceDetector.

        MultiProcessor multiProcessor = new MultiProcessor.Builder(new GraphicFaceTrackerFactory(mGraphicOverlay)).build();
        // multiProcessor.receiveDetections();

        myFaceDetector.setProcessor(multiProcessor);

        if (!myFaceDetector.isOperational()) {
            Log.w(TAG, "Face detector dependencies are not yet available.");
        }


        // You can use your own settings for CameraSource
        mCameraSource = new CameraSource.Builder(getApplicationContext(), myFaceDetector)
                .setFacing(CameraSource.CAMERA_FACING_FRONT)
                .setRequestedPreviewSize(960, 720)
                //.setRequestedPreviewSize(640, 480)
                .setAutoFocusEnabled(false)
                .setRequestedFps(15.0f)
                .build();


    }

    /**
     * Restarts the camera.
     */
    @Override
    protected void onResume() {
        super.onResume();

        startCameraSource();
        //Shared Preferences
        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);


        //#######################################################################
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

        unbindService(mConnection);
        mBound = false;
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


    public void previewResult(final FaceResultViewHolder result) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mSampleInputPhotoPreview.setImageBitmap(result.getInputPreviewBmp());
                mProfilePhotopreview.setImageDrawable(result.getFace().getDrawable());
                mSampleOutputPhotoPreview.setImageDrawable(result.getFace().getDrawable());

                mProfileName.setText(result.getFace().getName());
                mProfileSurname.setText(result.getFace().getSurname());
                mResultTextView.setText(result.getResultMsg());
            }
        });

    }

    @Override
    public void onFaceFrame(final Frame newFrame, final SparseArray<Face> faceSparseArray) {


        //    new Thread(new Runnable() {
        //     @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        //     @Override
        //   public void run() {
        YuvImage yuvImage = new YuvImage(newFrame.getGrayscaleImageData().array(), ImageFormat.NV21, newFrame.getMetadata().getWidth(), newFrame.getMetadata().getHeight(), null);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        yuvImage.compressToJpeg(new Rect(0, 0, newFrame.getMetadata().getWidth(), newFrame.getMetadata().getHeight()), 100, byteArrayOutputStream);
        byte[] jpegArray = byteArrayOutputStream.toByteArray();
        final Bitmap tempBitmap = BitmapFactory.decodeByteArray(jpegArray, 0, jpegArray.length);
        for (int i = 0; i < faceSparseArray.size(); i++) {
            Face thisFace = faceSparseArray.valueAt(i);
            int x1 = (int) (thisFace.getPosition().x);
            int y1 = (int) (thisFace.getPosition().y);
            int width = (int) thisFace.getWidth();
            int height = (int) thisFace.getHeight();


            if (x1 >= 0 && y1 >= 0 && width + x1 <= tempBitmap.getWidth() && height + y1 <= tempBitmap.getHeight()) {
                Bitmap resizedbitmap1 = Bitmap.createBitmap(tempBitmap, x1, y1, width, height);

                resizedbitmap1 = FileUtils.getCroppedBitmap(resizedbitmap1);

                if (!trackingIds.contains(thisFace.getId())) {

                    //  Log.i("Control", "1");
                    if (myFaceDetector.isFace(newFrame) ) {
                        if (mBound) {
                            if(mPreferences.getString("TensorFlowEnableSettings", "")=="true") {
                                mService.recognizeImage(resizedbitmap1, thisFace.getId());
                                trackingIds.add(thisFace.getId());
                            }
                        }
                        // mTotalPersonBitmap.clear();
                        // Log.i("PreviewImage", "TF Detector FrameID: " + newFrame.getMetadata().getId() + "\nFrameTimeStamp: " + newFrame.getMetadata().getTimestampMillis());
                        // Log.i("PreviewImage", "TF Detector Size:   " + width + " x " + height);
                    }
                }

                //TODO: Write face to file here.
                if(mPreferences.getString("FileEnableSettings", "")=="true") {
                    if (/*(newFrame.getMetadata().getId() % 5 == 0) && */myFaceDetector.isFace(newFrame) &&
                            FileUtils.writeImageToFile(resizedbitmap1, String.valueOf(thisFace.getId()))) {
                        // Log.i("PreviewImage", "FrameID: " + newFrame.getMetadata().getId() + "\nFrameTimeStamp: " + newFrame.getMetadata().getTimestampMillis());
                        // Log.i("PreviewImage", "Size:   " + width + " x " + height);
                        // Log.i("PreviewImage", "File Write Success !!! ");
                    }
                }


            }


        }
        //  }

        //   }).start();
    }


    /**
     * Defines callbacks for service binding, passed to bindService()
     */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            FaceRecognitionService.FaceRecognitionServiceBinder binder = (FaceRecognitionService.FaceRecognitionServiceBinder) service;
            mService = binder.getService();
            mBound = true;
            mService.setResultListener(MainActivity.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    public void readPrefs(){
       // mPreferences.getString(SelectActivity.SETTINGS_TENSORFLOW"")

        //mPreferences.get
    }
}