package com.weimanteam.weiman.util;

import java.util.ArrayList;

import org.opencv.core.Point;

import com.weimanteam.weiman.bean.FacialFeaturesRect;


public class FacialFeaturesPointUtil {
	private static final int MouthPointStartIndex = 59;
	private static final int MouthPointEndIndex = 76;

	private static final int NosePointStartIndex = 48;
	private static final int NosePointEndIndex = 58;
	
	private static final int EyePointStartIndex = 30;//排除28, 29，增加18, 25
	private static final int EyePointEndIndex = 47;
	
	private static final int LeftEyePointStartIndex = 30;//增加18 ,21的宽度，不要取18和21的高度
	private static final int LeftEyePointEndIndex = 38;
	
	private static final int EyebrowPointStartIndex = 16;
	private static final int EyebrowPointEndIndex = 27;
	
	private static final int LeftbrowPointStartIndex = 16;
	private static final int LeftbrowPointEndIndex = 21;
	
	private static final int FacePointStartIndex = 0;
	private static final int FacePointEndIndex = 15;
	
	public static FacialFeaturesRect getMouthRect(int[] points) {
		FacialFeaturesRect mouthRect = calFacialFeaturesRect(points, MouthPointStartIndex, MouthPointEndIndex);
		return mouthRect;
	}
	
	public static FacialFeaturesRect getNoseRect(int[] points) {
		FacialFeaturesRect noseRect = calFacialFeaturesRect(points, NosePointStartIndex, NosePointEndIndex);
		return noseRect;
	}
	
	public static FacialFeaturesRect getEyeRect(int[] points) {
		FacialFeaturesRect eyeRect = calFacialFeaturesRectForEye(points, EyePointStartIndex, EyePointEndIndex);
		return eyeRect;
	}
	
	public static FacialFeaturesRect getEyebrowRect(int[] points) {
		FacialFeaturesRect eyebrowRect = calFacialFeaturesRect(points, EyebrowPointStartIndex, EyebrowPointEndIndex);
		return eyebrowRect;
	}
	
	public static FacialFeaturesRect getFaceRect(int[] points) {
		FacialFeaturesRect faceRect = calFacialFeaturesRect(points, FacePointStartIndex, FacePointEndIndex);
		return faceRect;
	}
	
	public static FacialFeaturesRect getLeftEyeRect(int[] points) {
		FacialFeaturesRect faceRect = calFacialFeaturesRectForLeftEye(points, LeftEyePointStartIndex, LeftEyePointEndIndex);
		return faceRect;
	}
	
	public static FacialFeaturesRect getLeftEyebrowRect(int[] points) {
		FacialFeaturesRect eyebrowRect = calFacialFeaturesRect(points, LeftbrowPointStartIndex, LeftbrowPointEndIndex);
		return eyebrowRect;
	}
	
	
	public static FacialFeaturesRect getRect(ArrayList<Point> points) {
		FacialFeaturesRect faceRect = calFacialFeaturesRect(points);
		return faceRect;
	}
	
	//计算points数组里的点围成的范围
	private static FacialFeaturesRect calFacialFeaturesRect(int[] points, int start, int end) {
		int minX = points[start * 2];
		int maxX = points[start * 2];
		int minY = points[start * 2 + 1];
		int maxY = points[start * 2 + 1];
		
		for (int i = start; i <= end; i++) {
			if (points[i * 2] < minX) minX = points[i * 2];
			if (points[i * 2] > maxX) maxX = points[i * 2];
			if (points[i * 2 + 1] < minY) minY = points[i * 2 + 1];
			if (points[i * 2 + 1] > maxY) maxY = points[i * 2 + 1];
		}
		return new FacialFeaturesRect(new Point(minX, minY), new Point(maxX, maxY));
	}
	
	//计算points数组里的点围成的范围,eye专用，增加的特殊点
		private static FacialFeaturesRect calFacialFeaturesRectForEye(int[] points, int start, int end) {
			//增加控制点 18 和 25的宽度
			int minX = points[18 * 2];
			int maxX = points[25 * 2];
			int minY = points[start * 2 + 1];
			int maxY = points[start * 2 + 1];
			
			for (int i = start; i <= end; i++) {
				if (points[i * 2] < minX) minX = points[i * 2];
				if (points[i * 2] > maxX) maxX = points[i * 2];
				if (points[i * 2 + 1] < minY) minY = points[i * 2 + 1];
				if (points[i * 2 + 1] > maxY) maxY = points[i * 2 + 1];
			}
			return new FacialFeaturesRect(new Point(minX, minY), new Point(maxX, maxY));
		}
		//计算points数组里的点围成的范围,leftEye专用，增加的特殊点
		private static FacialFeaturesRect calFacialFeaturesRectForLeftEye(int[] points, int start, int end) {
			//增加控制点 18 21的宽度
			int minX = points[18 * 2];
			int maxX = points[21 * 2];
			int minY = points[start * 2 + 1];
			int maxY = points[start * 2 + 1];
			
			for (int i = start; i <= end; i++) {
				if (points[i * 2] < minX) minX = points[i * 2];
				if (points[i * 2] > maxX) maxX = points[i * 2];
				if (points[i * 2 + 1] < minY) minY = points[i * 2 + 1];
				if (points[i * 2 + 1] > maxY) maxY = points[i * 2 + 1];
			}
			return new FacialFeaturesRect(new Point(minX, minY), new Point(maxX, maxY));
		}
	
	//计算points数组里的点围成的范围
		private static FacialFeaturesRect calFacialFeaturesRect(ArrayList<Point> points) {
			assert(points != null);
			int minX = (int) points.get(0).x;
			int maxX = (int) points.get(0).x;
			int minY = (int) points.get(0).y;
			int maxY = (int) points.get(0).y;
			
			for (int i = 0; i < points.size(); i++) {
				if (points.get(i).x < minX) minX = (int) points.get(i).x;
				if (points.get(i).x > maxX) maxX = (int) points.get(i).x;
				if (points.get(i).y < minY) minY = (int) points.get(i).y;
				if (points.get(i).y > maxY) maxY = (int) points.get(i).y;
			}
			return new FacialFeaturesRect(new Point(minX, minY), new Point(maxX, maxY));
		}
	
}