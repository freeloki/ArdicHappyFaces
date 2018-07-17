#include "opencv2/opencv.hpp"
#include "opencv2/objdetect/objdetect.hpp"
#include "opencv2/highgui/highgui.hpp"
#include "opencv2/imgproc/imgproc.hpp"

#include <iostream>
#include<string> // for string class


using namespace std;
using namespace cv;
void VideoProcess (const String filename);
int faceDetection(const Mat& frame);
const String  HAARCASCADES="/home/ardic/opencv-3.4.1/data/haarcascades/haarcascade_frontalface_alt.xml";


int main(int argc, char const *argv[])
{
	if(argc !=2){
		cerr<<" Usage: ./exe Video_name.mp4" << endl;
		return -1;
	}
	VideoProcess(argv[1]);
	return 0;
}

void VideoProcess (const String filename){
	VideoCapture videoCap(filename);
	if(!videoCap.isOpened()){
		cout << "Error opening video stream or file" << endl;
    	exit(1);
  	}
  	Mat frame;
  	int i=0;
  	while(true){
  		videoCap >> frame;

  		if(frame.empty()){
  			break;
  		}
  		cout<<"s11111     "<<i<<endl;
  		int numOffaces=faceDetection(frame);
    	if(numOffaces>-1){
    		cout<<"face is found :)  "<<numOffaces<<endl;
    		 // Display the resulting frame
    		String str="frame ";
    		str+=i;

    		imwrite("str.png",frame);
    		imshow( str, frame );
    		waitKey(0);


    		
    	}
    	i++;
    	cout<<"searching a face... "<<i<<endl;
    	imshow("real video", frame);
    	cout<<"***********************"<<i<<endl;
    	//if(waitKey(5) >= 0) break;
    	
  	}
  	videoCap.release();

}
int faceDetection(const Mat& frame){
	// Load Face cascade (.xml file)
    CascadeClassifier face_cascade;
    face_cascade.load(HAARCASCADES);
    // Detect faces
    vector<Rect> faces;
    int returnVal=-1;
    face_cascade.detectMultiScale( frame, faces, 1.1, 2, 0|CV_HAAR_SCALE_IMAGE, Size(30, 30) );
    if(!faces.empty()){
    	//cout<<"face is found :)"<<endl;
    	returnVal=faces.size();
    	// Draw circles on the detected faces
	    for( int i = 0; i < faces.size(); i++ )
	    {
	        Point center( faces[i].x + faces[i].width*0.5, faces[i].y + faces[i].height*0.5 );
	        rectangle( frame, center, Point( faces[i].width*0.5, faces[i].height*0.5),  Scalar( 200, 255, 255 ), 4, 8, 0 );
	    }
    }

    return returnVal;
}