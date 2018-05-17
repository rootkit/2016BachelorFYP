package com.weimanteam.weiman.util;

import org.opencv.core.Point;

import java.util.ArrayList;
@Deprecated
public class BezierUtil {

	private static final int STEPS = 30;

	//数组
	public static ArrayList<Point> bezierStart(Point[] controlPoints) {
		ArrayList<Point> drawPoints = new ArrayList<Point>();
		for (int i = 0; i <= STEPS; i++) {
			bezier(controlPoints, i, drawPoints);
		}
		return drawPoints;
	}
	
	//序列
	public static ArrayList<Point> bezierStart(ArrayList<Point> controlPoints) {
		Point[] holdPoints = (Point[]) controlPoints.toArray(new Point[controlPoints.size()]);
		
		ArrayList<Point> drawPoints = new ArrayList<Point>();
		for (int i = 0; i <= STEPS; i++) {
			bezier(holdPoints, i, drawPoints);
		}
		return drawPoints;
	}

	private static void bezier(Point[] arr, int t, ArrayList<Point> drawPoints) {
		if (arr.length == 1) {
			drawPoints.add(arr[0]);
			// System.out.println(t);
		} else {
			Point[] brr = new Point[arr.length - 1];
			for (int i = 0; i < brr.length; i++) {
				// System.out.println(i);
				brr[i] = new Point(arr[i].x + (arr[i + 1].x - arr[i].x) * t
						/ STEPS, arr[i].y + (arr[i + 1].y - arr[i].y) * t
						/ STEPS);
			}
			bezier(brr, t, drawPoints);
		}
	}
}