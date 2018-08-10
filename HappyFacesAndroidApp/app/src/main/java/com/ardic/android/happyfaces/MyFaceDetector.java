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
import com.google.android.gms.vision.face.FaceDetector;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


public class MyFaceDetector extends Detector<Face> {
    private Detector<Face> mDelegate;
    private Context context=null;
    private static final int INPUT_SIZE = 299;
    private static final int IMAGE_MEAN = 0;
    private static final float IMAGE_STD = 255;
    private static final String INPUT_NAME = "Placeholder";
    private static final String OUTPUT_NAME = "final_result";

    private static final String MODEL_FILE = "file:///android_asset/output_graph.pb";
   private static final String LABEL_FILE =
            "file:///android_asset/labels.txt";
    private static final String testimg="/home/ardic/development/ArdicHappyFaces/FaceTrackerSAndroid/app/src/main/assets/test.jpg";

    private Classifier classifier;
    private Executor executor = Executors.newSingleThreadExecutor();


    MyFaceDetector(Detector<Face> delegate, Context con) {


        mDelegate = delegate;
        context=con;
        try {
            classifier = TensorFlowImageClassifier.create(
                    context.getAssets(),
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

    public SparseArray<Face> detect(Frame frame) {
        YuvImage yuvImage = new YuvImage(frame.getGrayscaleImageData().array(), ImageFormat.NV21, frame.getMetadata().getWidth(), frame.getMetadata().getHeight(), null);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        yuvImage.compressToJpeg(new Rect(0, 0, frame.getMetadata().getWidth(), frame.getMetadata().getHeight()), 100, byteArrayOutputStream);
        byte[] jpegArray = byteArrayOutputStream.toByteArray();
        Bitmap TempBitmap = BitmapFactory.decodeByteArray(jpegArray, 0, jpegArray.length);


        /**#########################################################################*/
        /* Bitmap faceBitmap = Bitmap.createBitmap(TempBitmap, (int)currentface.getPosition().x, (int)currentface.getPosition().y, (int)currentface.getWidth(),
                (int)currentface.getHeight());*/

        //Log.i(currentface.getPosition().x+"  ", "salam,mmmmmmmmmm");

        FaceDetector faceDetector = new FaceDetector.Builder(context).setTrackingEnabled(false).build();
        if(!faceDetector.isOperational()){


        }
        Frame frameface = new Frame.Builder().setBitmap(TempBitmap).build();
        SparseArray<Face> faces = faceDetector.detect(frame);
        for(int i=0; i<faces.size(); i++) {
            Face thisFace = faces.valueAt(i);
            float x = (thisFace.getPosition().x + thisFace.getWidth()/2);
            float y = (thisFace.getPosition().y + thisFace.getHeight()/2);
            float xOffset = (int)(thisFace.getWidth() / 2.0f);
            float yOffset = (int)(thisFace.getHeight() / 2.0f);
            int x1 = (int)thisFace.getPosition().x;
            int y1 = (int)thisFace.getPosition().y;
            int width = (int)thisFace.getWidth();
            int height = (int)thisFace.getHeight();
            int left = (int)(x - xOffset+85.0f);
            int top =(int)( y - yOffset+115.0f);
            int right =(int) (x + xOffset-70.0f);
            int bottom = (int)(y + yOffset-13.0f);
            System.out.println("fuile>>>>>>x="+x1+"   y="+y1+"     "+width+"      "+height);
            //boyle olmasi lazim  bir de yatay!!!!!!
            if(y1<0 || y1>height){
                y1=height;
            }
           /* FileInputStream in = null;
            Bitmap bMap=null;
            try {
                //in = new FileInputStream((FileInputStream).);

                BufferedInputStream buf = new BufferedInputStream(context.getAssets().open("test.jpg"));
                byte[] bMapArray = new byte[0];

                bMapArray = new byte[buf.available()];


                buf.read(bMapArray);
                bMap = BitmapFactory.decodeByteArray(bMapArray, 0, bMapArray.length);
            }catch (Exception e){
                Log.e("Exception ", String.valueOf(e));
            }*/

            if(y1<0){
                y1=Math.abs((int)thisFace.getPosition().y);
            }
            else if(y1>height){
                y1=height;
            }

            Bitmap resizedbitmap1= Bitmap.createBitmap(TempBitmap, (int)thisFace.getPosition().x, y1, width, height, null, false);
            // Bitmap ne=Bitmap.createBitmap()

            /**#***********************************************************************/
            Log.i("humf","1");
            Bitmap bitmaptf = Bitmap.createScaledBitmap(resizedbitmap1, INPUT_SIZE, INPUT_SIZE, false);

            Log.i("humf","2\n" + bitmaptf.getWidth() + "    ?    " + bitmaptf.getHeight() );

            // imageViewResult.setImageBitmap(bitmaptf);

            Log.i("humf","666\n" + classifier);

            List<Classifier.Recognition> results = classifier.recognizeImage(bitmaptf);
            Log.i("humf","3");
            //textViewResult=(TextView) textViewResult.findViewById(R.id.imgResult);
            //textViewResult.setText(results.toString());
            System.out.println("Result>> "+i+" >> "+results.toString());

            /*String root = Environment.getExternalStorageDirectory().toString();
            String folder_name=results.toString();//
            File myDir = new File(root + "/hhhh");
            myDir.mkdirs();
            Random generator = new Random();
            int n = 10000;
            n = generator.nextInt(n);
            String fname = "Image555-" + n + ".jpg";
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
            }*/
        }

        faceDetector.release();

       return mDelegate.detect(frame);
    }

    public boolean isOperational() {
        return mDelegate.isOperational();
    }

    public boolean setFocus(int id) {
        return mDelegate.setFocus(id);
    }
    public void setContext(Context context){this.context=context;}
    private void initTensorFlowAndLoadModel() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    classifier = TensorFlowImageClassifier.create(
                            context.getAssets(),
                            MODEL_FILE,
                            LABEL_FILE,
                            INPUT_SIZE,
                            IMAGE_MEAN,
                            IMAGE_STD,
                            INPUT_NAME,
                            OUTPUT_NAME);

                } catch (final Exception e) {
                    throw new RuntimeException("Error initializing TensorFlow!", e);
                }
            }
        });
    }


}
