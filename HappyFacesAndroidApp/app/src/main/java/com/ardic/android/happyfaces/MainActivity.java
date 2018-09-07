package com.ardic.android.happyfaces;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
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
import com.ardic.android.happyfaces.model.ArdicFace;
import com.ardic.android.happyfaces.tracker.GraphicFaceTrackerFactory;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity implements ResultListener {
    private static final String TAG = "MainActivity";

    private CameraSource mCameraSource = null;

    private CameraSourcePreview mPreview;
    private GraphicOverlay mGraphicOverlay;
    private ImageView mSampleInputPhotoPreview, mSampleOutputPhotoPreview, mProfilePhotopreview;
    private TextView mResultTextView, mProfileName, mProfileSurname;
    // permission request codes need to be < 256
    private static final int RC_HANDLE_CAMERA_PERM = 2;
    private TensorFlowBridge mTfTensorFlowBridge;
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
    private ArrayList<Integer> mFaceColorList, mFaceColorList2;
    private int threadCount = 0;
    private int mCurrentFaceId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_layout_activity);

        mPreview = findViewById(R.id.preview);

        mTfTensorFlowBridge = new TensorFlowBridge(getApplicationContext());

        Log.i("humf", "preview size: " + mPreview.getHeight() + "|" + mPreview.getWidth());
        mGraphicOverlay = findViewById(R.id.faceOverlay);

        mSampleInputPhotoPreview = findViewById(R.id.input_photo);
        mSampleOutputPhotoPreview = findViewById(R.id.output_photo);
        mProfilePhotopreview = findViewById(R.id.beautiful_profile_photo);
        mResultTextView = findViewById(R.id.result_text_view);
        mProfileName = findViewById(R.id.profile_info_name);
        mProfileSurname = findViewById(R.id.profile_info_surname);
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

        //set the tfbridge
        //tFbridge=new TensorFlowBridge(context);

        mFaceColorList = new ArrayList<>();
        mFaceColorList.add(-1);
        mFaceColorList2 = new ArrayList<>();

        // You can use your own settings for your detector
        FaceDetector detector = new FaceDetector.Builder(getApplicationContext())
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .setProminentFaceOnly(false)
                .setTrackingEnabled(true)
                .build();

        // This is how you merge myFaceDetector and google.vision detector


        if (!detector.isOperational()) {
            Log.w(TAG, "Face detector dependencies are not yet available.");
        }
        myFaceDetector = new MyFaceDetector(detector, this);
        // You can use your own processor
        //  myFaceDetector.
        myFaceDetector.setProcessor(
                new MultiProcessor.Builder<>(new GraphicFaceTrackerFactory(mGraphicOverlay))
                        .build());

        if (!myFaceDetector.isOperational()) {
            Log.w(TAG, "Face detector dependencies are not yet available.");
        }


        // You can use your own settings for CameraSource
        mCameraSource = new CameraSource.Builder(getApplicationContext(), myFaceDetector)
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
    public void previewImage(final Bitmap bmp) {


        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mSampleInputPhotoPreview.setImageBitmap(bmp);
                long temp = System.currentTimeMillis();
                List<ArdicFace> tfresult = mTfTensorFlowBridge.recognizeTensorFlowImage(bmp);
                Log.i("Humfy", tfresult + " Detecting :" + tfresult + "     ms:" + (System.currentTimeMillis() - temp));

                if (!tfresult.isEmpty() && tfresult.size() == 1) {
                    previewProfilePhoto(tfresult.get(0));
                } else {
                    ArdicFace guest = new ArdicFace("NONE", "NONE", 0, getApplicationContext());
                    previewProfilePhoto(guest);
                    mResultTextView.setText("Welcome Guest, We couldn't recognize you just for now :( But you look like:\n[");
                    for (ArdicFace face : tfresult) {

                        if(tfresult.get(tfresult.size()-1).getName().equals(face.getName())) {
                            mResultTextView.append(" " + face.getTitle() + " (%" + face.getPercentage() + ")].");
                        } else {
                            mResultTextView.append(" " + face.getTitle() + " (%" + face.getPercentage() + "), ");
                        }
                    }

                }

            }
        });
    }


    @Override
    public void previewProfilePhoto(final ArdicFace face) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mProfilePhotopreview.setImageDrawable(face.getDrawable());
                mSampleOutputPhotoPreview.setImageDrawable(face.getDrawable());
                if (!"New".equals(face.getName())) {
                    mProfileName.setText(face.getName());
                    mProfileSurname.setText(face.getSurname());
                    mResultTextView.setText("Welcome, " + face.getName() + " have a nice day.");
                }
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
                    int width = (int) thisFace.getWidth();
                    int height = (int) thisFace.getHeight();

                    if (x1 >= 0 && y1 >= 0 && width + x1 <= tempBitmap.getWidth() && height + y1 <= tempBitmap.getHeight()) {
                        final Bitmap resizedbitmap1 = Bitmap.createBitmap(tempBitmap, x1, y1, width, height);

                        if (mCurrentFaceId != thisFace.getId())  //give to TF
                        {
                            //  Log.i("Control", "1");
                            previewImage(resizedbitmap1);

                            mCurrentFaceId = thisFace.getId();
                        }
                        String root = Environment.getExternalStorageDirectory().toString();
                        File myDir = new File(root + "/faces__");
                        myDir.mkdirs();

                        String fname = "Image_" + mCurrentFaceId + "_" + System.currentTimeMillis() + ".jpg";
                        File file = new File(myDir, fname);
                        //  Log.i("salam", "" + file);
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



}