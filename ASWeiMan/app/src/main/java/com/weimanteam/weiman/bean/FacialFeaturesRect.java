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
	
	public Point getTopCenterPoint() {
		return new Point(x + width / 2, y);
	}
	
	public Point getBottomCenterPoint() {
		return new Point(x + width / 2, y + height);
	}

	@Override
	public String toString() {
		return "FacialFeaturesRect [toString()=" + super.toString() 
				+ "\ncenter point is :" 
				+ getCenterPoint().toString()
				+ "]";
	}
	
	//只增高头部
	public FacialFeaturesRect getHigherRect(double r) {
		return new FacialFeaturesRect(x, (int)(y - height * r), width, (int)(height + height * r));
	}
	
	//左右同时变宽
	public FacialFeaturesRect getFatterRect(double r){
		return new FacialFeaturesRect((int)(x - width * r), y, (int)(width + width * r * 2), height);
	}
	
	
	//两侧同时缩放
	public FacialFeaturesRect getThinnerRect(double r) {
		return new FacialFeaturesRect((int)(x + width * r / 2), y, (int)(width * (1 - r)), height);
	}
	
}