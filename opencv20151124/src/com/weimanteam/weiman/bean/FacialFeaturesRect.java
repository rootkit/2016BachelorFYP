package com.weimanteam.weiman.bean;

import org.opencv.core.Point;
import org.opencv.core.Rect;

public class FacialFeaturesRect extends Rect{
	public FacialFeaturesRect(Point i1, Point i2) {
		super(i1, i2);
	}
	
	public FacialFeaturesRect(int x, int y, int width, int height) {
		super(x, y, width, height);
	}
	
	public Point getCenterPoint() {
		return new Point(x + width / 2, y + height / 2);
	}

	@Override
	public String toString() {
		return "FacialFeaturesRect [toString()=" + super.toString() 
				+ "\ncenter point is :" 
				+ getCenterPoint().toString()
				+ "]";
	}
	
}