package com.weimanteam.weiman.model;

import com.weimanteam.weiman.bean.FacialFeaturesRect;

import org.opencv.core.Point;
import org.opencv.core.Rect;

public class ScaleAndMoveModel{
	private final int WebViewFaceCenterPointX = (int) (640 * 0.5);
	private final int WebViewFaceCenterPointY = (int) (640 * 0.5);
	private final float webViewHeight = 640 * 2 / 3;//webView的既定高度
	private Point targetPoint;
	private FacialFeaturesRect srcRect;
	public int deltaX = 0;
	public int deltaY = 0;
	public double ratio = 1; //target / src 的缩放比例
	private MatchPosition matchPosition;
	public enum MatchPosition{
	       Topmiddle , Centermiddle, Bottommiddle;
	}
	/*
	 * @param ratio 缩放比例
	 * @param targetPoint 需要将Rect移动到的目标点位置
	 * @param srcRect 此时的Rect
	 * @param srcRect和targetRect对准时，按照他们的哪个点进行对准
	 */
	public ScaleAndMoveModel(double ratio, Point targetPoint, FacialFeaturesRect srcRect, MatchPosition matchPosition) {
		this.ratio = ratio;
		this.targetPoint = targetPoint;
		this.srcRect = srcRect;
		this.matchPosition = matchPosition;
		scaleAndMoveRect();
	}
	
	public ScaleAndMoveModel(FacialFeaturesRect targetRect, FacialFeaturesRect srcRect, MatchPosition matchPosition) {
		this.srcRect = srcRect;
		this.matchPosition = matchPosition;
		targetPoint = getTargetPoint(targetRect, matchPosition);
		ratio = calRatio(targetRect.width, srcRect.width);
		scaleAndMoveRect();
	}
	
	private Point getTargetPoint(FacialFeaturesRect tRect, MatchPosition matchPosition) {
		switch (matchPosition) {
		case Topmiddle:
			return tRect.getTopCenterPoint();
		case Centermiddle:
			return tRect.getCenterPoint();
		case Bottommiddle:
			return tRect.getBottomCenterPoint();
		}
		return null;
	}
	
	private double calRatio(int target, int src) {
		double t = (double)(target);
		return t / src;
	}
	
	private void caldeltaXAnddeltaY(Point scaledPoint) {
		deltaX = (int) (targetPoint.x - scaledPoint.x);
		deltaY = (int) (targetPoint.y - scaledPoint.y);
	}
	
	//模板方法
	private void scaleAndMoveRect() {
		FacialFeaturesRect scaledRect = scaleRect(srcRect);
		caldeltaXAnddeltaY(getTargetPoint(scaledRect, matchPosition));
	}
	
	
	public FacialFeaturesRect scaleRect(Rect srcRect) {
		return new FacialFeaturesRect((int)(srcRect.x * ratio), 
							(int)(srcRect.y * ratio), 
							(int)(srcRect.width * ratio), 
							(int)(srcRect.height * ratio));
	}
	
	public Rect moveRect(Rect srcRect) {
		return new FacialFeaturesRect((int)(srcRect.x + deltaX), 
				(int)(srcRect.y + deltaY), 
				(int)(srcRect.width), 
				(int)(srcRect.height));
}
	
}