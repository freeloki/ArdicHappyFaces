#include "opencv2/objdetect/objdetect.hpp"
#include "opencv2/highgui/highgui.hpp"
#include "opencv2/imgproc/imgproc.hpp"
#include "opencv2/opencv.hpp"
#include <iostream>
#include <stdio.h>
   
using namespace std;
using namespace cv;
   
int main( )
{
    Mat image;
    image = imread("2.png", CV_LOAD_IMAGE_COLOR);  
    imshow( "window1", image );
   
    // Load Face cascade (.xml file)
    CascadeClassifier face_cascade;
    face_cascade.load( "/home/ardic/opencv-3.4.1/data/haarcascades/haarcascade_frontalface_alt2.xml" );
 
     if(face_cascade.empty())
     {
      cerr<<"Error Loading XML file"<<endl;
      return 0;
     }
  
    // Detect faces
    std::vector<Rect> faces;
    face_cascade.detectMultiScale( image, faces, 1.1, 2, 0|CV_HAAR_SCALE_IMAGE, Size(30, 30) );
   
    // Draw circles on the detected faces
    for( int i = 0; i < faces.size(); i++ )
    {
        int radius=faces[i].width*0.5;
        Point center( faces[i].x+faces[i].width*0.5 , faces[i].y+faces[i].width*0.5);
        
        Point point1(center.x-radius, center.y-radius);
          Point point4(center.x+radius, center.y+radius);
          Rect rect1(point1, point4);

        cout<<faces[i].x <<"  "<<faces[i].width<<"   "<<faces[i].height<<"  "<<faces[i].y<<endl;
       //ellipse( image, center, Size( faces[i].width*0.5, faces[i].height*0.5), 0, 0, 360, Scalar( 200, 255, 255 ), 4, 8, 0 );
        //rectangle( image, center, Point( faces[i].width, faces[i].height),  Scalar( 200, 255, 255 ), 4, 8, 0 );
        //circle(image, center, 5r, Scalar(255, 0, 0), 4,22, 0);
        rectangle(image,point1, point4, Scalar(255, 0, 0), 4,8, 0);
    }
    cout<<"face.size";
       
    imshow( "Detected Face", image );
       
    waitKey(0);                   
    return 0;
}
