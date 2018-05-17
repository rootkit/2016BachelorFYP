package com.weimanteam.weiman.model;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import com.weimanteam.weiman.config.Params;
import com.weimanteam.weiman.util.ImageUtils;

import android.graphics.Bitmap;

public class OpenCVModel {
	public static Bitmap canny_bitmap(Bitmap bitmap) {
		Mat src = new Mat();
		Utils.bitmapToMat(bitmap, src);
		
		Mat canny_mat = new Mat();
		Imgproc.Canny(src, canny_mat, Params.CannyParams.THRESHOLD1,
				Params.CannyParams.THRESHOLD2);
		Bitmap canny_bitmap = ImageUtils.mat2Bitmap(canny_mat);
		
		return canny_bitmap;
	}
	
	
}