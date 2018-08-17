#include "opencv2/opencv.hpp"
#include "opencv2/objdetect/objdetect.hpp"
#include "opencv2/highgui/highgui.hpp"
#include "opencv2/imgproc/imgproc.hpp"

#include <iostream>
#include<string> // for string class
#include <unistd.h> //sleep
#include <chrono>
int imagecount=0;

using namespace std;
using namespace cv;
void VideoProcess (const String filename);
int faceDetection(const Mat& frame);
//const String  HAARCASCADES="/home/ardic/opencv-3.4.1/data/haarcascades/haarcascade_frontalface_alt.xml";
//const String IMAGEPATH="/home/ardic/Documents/faces/img4/ImageFromVideo_";
String  HAARCASCADES, IMAGEPATH;
double startTime;
int main(int argc, char const *argv[])
{
  if(argc !=4){
    cerr<<" Usage: ./exe haarcascadePATH IMAGEPATH #of_videos" << endl;
    return -1;
  }
  HAARCASCADES=argv[1];
  IMAGEPATH=argv[2];

  int total=atoi(argv[3]);
  cout<<"total ="<<total<<endl;
  
  for(int i=0; i<total; i++){
        
        String filename="video_"+to_string(i)+".h264";
        cout<<"the current video:  "<<filename<<endl;
        
       	
        VideoProcess(filename);
        double endTime=time(NULL)-startTime;
		SecondsToHourMinSec((int)(endTime));
		    
  }
  cout<<endl;      
  
  return 0;
}

void VideoProcess (const String filename){
	// register signal SIGINT and signal handler  
   signal(SIGINT, signalHandler); 
	startTime=time(NULL);
  VideoCapture videoCap(filename);
  if(!videoCap.isOpened()){
    cout << "Error opening video stream or file" << endl;
    cout << "Check: video_#number.h264" << endl;
      exit(1);
    }
    Mat frame;
    int i=1;

    while(true){
        videoCap >> frame;

        if(frame.empty()){
        	cout<<"empty...."<<endl;
          break;
        }
        
        int numOffaces=faceDetection(frame);
        if(numOffaces>-1){
          cout<<"face is found :)  "<<numOffaces<<endl;
           // Display the resulting frame
          String str="frame ";
          str+=i;         
        }
      i++;
      //sleep(15);
    }
    cout<<"release..."<<endl;
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
          int radius=faces[i].width*0.5;
          Point center( faces[i].x+faces[i].width*0.5 , faces[i].y+faces[i].width*0.5);
      
          Point point1(center.x-radius, center.y-radius);
            Point point4(center.x+radius, center.y+radius);
           Rect rect1(point1, point4);

          //cout<<faces[i].x <<"  "<<faces[i].width<<"   "<<faces[i].height<<"  "<<faces[i].y<<endl;
         
        cv::Mat roi = frame(rect1);
        std::time_t result = std::time(nullptr);
       
        
        cv::imwrite(IMAGEPATH+to_string(imagecount)+ std::to_string(result) + ".jpg", roi);
        imagecount++;
          
      }
    }
    

    return returnVal;
}
void signalHandler( int signum ) {
   cout << "Interrupt signal () received.\n";
   int handleTime=(int)(time(NULL)-startTime);

   cout<<"Interrupted at >>";
   SecondsToHourMinSec(handleTime);

   // cleanup and close up stuff here  
   // terminate program  

   exit(signum);  
}
void SecondsToHourMinSec(int seconds){
	int hours, minutes;
	minutes = seconds / 60;
	hours = minutes / 60;
	cout <<  int(hours) << ":" << int(minutes%60)<< ":" << int(seconds%60) << "  (H:M:S) \n";
    
}