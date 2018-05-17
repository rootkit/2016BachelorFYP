#include <jni.h>
#include <stdio.h>
#include <stdlib.h>
#include <iostream>
#include <android/log.h>

#include <opencv2/opencv.hpp>
#include "./stasm/stasm_lib.h"

using namespace cv;
using namespace std;

CascadeClassifier cascade;
bool init = false;
const String APP_DIR = "/data/data/com.weimanteam.weiman/app_data/";

class Histogram1D{
private:
	int histSize[1];
	float hranges[2];
	const float * ranges[1];
	int channels[1];
public:
	Histogram1D(){
		histSize[0] = 256;
		hranges[0] = 0.0;
		hranges[1] = 255.0;
		ranges[0] = hranges;
		channels[0] = 0;
	}
	MatND getHistogram(const Mat& image){
		MatND hist;
		calcHist(&image,1,channels,Mat(),hist,1,histSize,ranges);
		return hist;
	}
};

template <typename _Tp> static
void olbp_(InputArray _src, OutputArray _dst) {
    // get matrices
    Mat src = _src.getMat();
    // allocate memory for result
    _dst.create(src.rows-2, src.cols-2, CV_8UC1);
    Mat dst = _dst.getMat();
    // zero the result matrix
    dst.setTo(0);

	//cout<<"rows "<<src.rows<<" cols "<<src.cols<<endl;
	//cout<<"channels "<<src.channels();
	//getchar();

    // calculate patterns
    for(int i=1;i<src.rows-1;i++) {
		//cout<<endl;
        for(int j=1;j<src.cols-1;j++) {

            _Tp center = src.at<_Tp>(i,j);
			//cout<<"center"<<(int)center<<"  ";
            unsigned char code = 0;
            code |= (src.at<_Tp>(i-1,j-1) >= center) << 7;
            code |= (src.at<_Tp>(i-1,j  ) >= center) << 6;
            code |= (src.at<_Tp>(i-1,j+1) >= center) << 5;
            code |= (src.at<_Tp>(i  ,j+1) >= center) << 4;
            code |= (src.at<_Tp>(i+1,j+1) >= center) << 3;
            code |= (src.at<_Tp>(i+1,j  ) >= center) << 2;
            code |= (src.at<_Tp>(i+1,j-1) >= center) << 1;
            code |= (src.at<_Tp>(i  ,j-1) >= center) << 0;

            dst.at<unsigned char>(i-1,j-1) = code;
			//cout<<(int)code<<" ";
			//cout<<(int)code<<endl;
        }
    }
};


extern "C" {


//public static native double LBPmatching(long targetImg, long compareImg);
//targetImg : target img
//compareImg ： compare img(stored in library) after LBP
JNIEXPORT double JNICALL Java_com_example_asm_NativeCode_LBPmatching(JNIEnv* env,
		jobject obj, jlong targetImg, jlong compareImg) {

	const char * PATH = APP_DIR.c_str();
	double result = 0.0;
	Mat * targetIMG = (Mat *) targetImg;
	Mat * compareIMG = (Mat *) compareImg;

	Mat lbp_temp = Mat::zeros((*targetIMG).size(),(*targetIMG).type()) ;
	olbp_<uchar>((*targetIMG),lbp_temp);

	return cv::compareHist(lbp_temp,(*compareIMG),CV_COMP_BHATTACHARYYA);

}


/*
 * do Canny edge detect
 */
JNIEXPORT void JNICALL Java_com_example_asm_NativeCode_DoCanny(JNIEnv* env,
		jobject obj, jlong matSrc, jlong matDst, jdouble threshold1 = 50,
		jdouble threshold2 = 150, jint aperatureSize = 3) {

	Mat * img = (Mat *) matSrc;
	Mat * dst = (Mat *) matDst;
	cvtColor(*img, *dst, COLOR_BGR2GRAY);
	Canny(*img, *dst, threshold1, threshold2, aperatureSize);
}

/*
 * face detection
 * matDst: face region
 * scaleFactor = 1.1
 * minNeighbors = 2
 * minSize = 30 * 30
 */
JNIEXPORT void JNICALL Java_com_example_asm_NativeCode_FaceDetect(JNIEnv* env,
		jobject obj, jlong matSrc, jlong matDst, jdouble scaleFactor, jint minNeighbors, jint minSize) {

	Mat * src = (Mat *) matSrc;
	Mat * dst = (Mat *) matDst;

	float factor = 0.3;
	Mat img;
	resize(*src, img, Size((*src).cols * factor, (*src).rows * factor));

	String cascadeFile = APP_DIR + "haarcascade_frontalface_alt2.xml";

	if (!init) {
		cascade.load(cascadeFile);
		init = true;
	}

	if (cascade.empty() != true) {
		vector<Rect> faces;
		cascade.detectMultiScale(img, faces, scaleFactor, minNeighbors, 0
				| CV_HAAR_FIND_BIGGEST_OBJECT
				| CV_HAAR_DO_ROUGH_SEARCH
				| CV_HAAR_SCALE_IMAGE, Size(minSize, minSize));

		for (int i = 0; i < faces.size(); i++) {
			Rect rect = faces[i];
			rect.x /= factor;
			rect.y /= factor;
			rect.width /= factor;
			rect.height /= factor;

			if (i == 0) {
				(*src)(rect).copyTo(*dst);
			}

			rectangle(*src, rect.tl(), rect.br(), Scalar(0, 255, 0, 255), 3);
		}
	}
}

/*
 *  do ASM
 *  error code:
 *  -1: illegal input Mat
 *  -2: ASM initialize error
 *  -3: no face detected
 */
JNIEXPORT jintArray JNICALL Java_com_weimanteam_weiman_util_NativeCode_FindFaceLandmarks(
		JNIEnv* env, jobject, jlong matAddr, jfloat ratioW, jfloat ratioH) {
	const char * PATH = APP_DIR.c_str();

	clock_t StartTime = clock();
	jintArray arr = env->NewIntArray(2 * stasm_NLANDMARKS);
	jint *out = env->GetIntArrayElements(arr, 0);

	Mat img = *(Mat *) matAddr;
	cvtColor(img, img, COLOR_BGR2GRAY);

	if (!img.data) {
		out[0] = -1; // error code: -1(illegal input Mat)
		out[1] = -1;
		img.release();
		env->ReleaseIntArrayElements(arr, out, 0);
		return arr;
	}

	int foundface;
	float landmarks[2 * stasm_NLANDMARKS]; // x,y coords

	if (!stasm_search_single(&foundface, landmarks, (const char*) img.data,
			img.cols, img.rows, " ", PATH)) {
		out[0] = -2; // error code: -2(ASM initialize failed)
		out[1] = -2;
		img.release();
		env->ReleaseIntArrayElements(arr, out, 0);
		return arr;
	}

	if (!foundface) {
		out[0] = -3; // error code: -3(no face found)
		out[1] = -3;
		img.release();
		env->ReleaseIntArrayElements(arr, out, 0);
		return arr;
	} else {
		for (int i = 0; i < stasm_NLANDMARKS; i++) {
			out[2 * i] = cvRound(landmarks[2 * i] * ratioW);
			out[2 * i + 1] = cvRound(landmarks[2 * i + 1] * ratioH);
		}
	}
	double TotalAsmTime = double(clock() - StartTime) / CLOCKS_PER_SEC;
	__android_log_print(ANDROID_LOG_INFO, "com.example.asm.native",
			"running in native code, \nStasm Ver:%s Img:%dx%d ---> Time:%.3f secs.", stasm_VERSION,
			img.cols, img.rows, TotalAsmTime);

	img.release();
	env->ReleaseIntArrayElements(arr, out, 0);
	return arr;
}





}

