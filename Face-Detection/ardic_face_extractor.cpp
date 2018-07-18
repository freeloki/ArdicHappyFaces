#include "opencv2/opencv.hpp"
#include "opencv2/objdetect/objdetect.hpp"
#include "opencv2/objdetect/objdetect_c.h"
#include "opencv2/highgui/highgui.hpp"
#include "opencv2/imgproc/imgproc.hpp"

#include <iostream>
#include<string> // for string class
#include <unistd.h> //sleep
int imagecount=0;

using namespace std;
using namespace cv;
void VideoProcess (const String filename);
int faceDetection(const Mat& frame);
const String  HAARCASCADES="/mnt/Linux-Extended/Dev-Programs/opencv-src/opencv/data/haarcascades/haarcascade_frontalface_alt.xml";

char keyboard; 




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
  		
  	int i=1;

keyboard = 0;



namedWindow( "frame",cv::WindowFlags::WINDOW_FULLSCREEN);
cv::resizeWindow("frame", 640, 480);


  	while( keyboard != 'q' && keyboard != 27 ) {

  			videoCap >> frame;
  		
	  		if(frame.empty()){
	  			break;
	  		}
	  		int numOffaces=faceDetection(frame);
	    	if(numOffaces>-1){
	    		cout<<"face is found :)  "<<numOffaces<<endl;
	    		 // Display the resulting frame
    imshow( "frame", frame );
 
    // Press  ESC on keyboard to exit
    char c=(char)waitKey(25);
    if(c==27)
      break;
	    		
	    		

	    		 // Display the resulting frame
	    		String str="frame ";
	    		str+=i;	    		
	    	}	
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
    face_cascade.detectMultiScale( frame, faces, 1.1, 2, 0| CV_HAAR_SCALE_IMAGE, Size(30, 30) );
    if(!faces.empty()){
    	//cout<<"face is found :)"<<endl;
    	returnVal=faces.size();
    	// Draw circles on the detected faces
	    for( int i = 0; i < faces.size(); i++ )
	    {
	        int radius=faces[i].width*0.5;
        	Point center( faces[i].x+faces[i].width*0.5 , faces[i].y+faces[i].width*0.5);
        
        	Point point1(center.x-radius, center.y-radius);
            Point point4(center.x+radius, center.y+radius);
			Rect rect1(point1, point4);

	        cout<<faces[i].x <<"  "<<faces[i].width<<"   "<<faces[i].height<<"  "<<faces[i].y<<endl;
			cv::Mat roi = frame(rect1);
			cv::imwrite("Raw-Faces/ImageFromVideo_" + std::to_string(imagecount) + ".jpg", roi);
			imagecount++;
	        
	    }
    }
    else
    {
    	cout<<"face is empty"<<endl;
    }

    return returnVal;
}
