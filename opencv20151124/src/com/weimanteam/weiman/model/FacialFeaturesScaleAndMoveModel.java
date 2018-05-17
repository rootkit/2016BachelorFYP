package com.weimanteam.weiman.model;

import org.opencv.core.Point;
import org.opencv.core.Rect;

import android.R.integer;

import com.weimanteam.weiman.bean.FacialFeaturesRect;

public class FacialFeaturesScaleAndMoveModel {
	private final int WebViewFaceCenterPointX = (int) (640 * 0.5);
	private final int WebViewFaceCenterPointY = (int) (640 * 0.5);
	private Point targetCenterPoint;
	
	private Point faceCenterPoint;
	public int deltaX = 0;
	public int deltaY = 0;
	
	private final float webViewHeight = 640 * 2 / 3;//webView的既定高度
	public double ratio = 1; //target / src 的缩放比例
	
//	public static final int horizontalScale = 0;
//	public static final int verticalScale = 1;
	
	//此处的ratio暂时根据高度或者高度比例实现
	public FacialFeaturesScaleAndMoveModel(int targetHeight, int wholeHeight, Point targetCenterPoint) {
		ratio = (double)targetHeight / wholeHeight;
//		ratio = 0.028;
		this.targetCenterPoint = targetCenterPoint;
	}
	
	//此处的ratio为直接传入参数
	public FacialFeaturesScaleAndMoveModel(double ratio, Point targetCenterPoint) {
		this.ratio = ratio;
		this.targetCenterPoint = targetCenterPoint;
	}
	
	//给从真人脸到webView专用的，向后兼容
	public FacialFeaturesScaleAndMoveModel(int wholeHeight) {
		ratio = webViewHeight / wholeHeight;
	}
	
	//给从svg变换到webView上（依据真人脸确定的位置用）
	public Rect scaleAndMoveRect2(Rect srcRect) {
		Rect scaleRect = scaleRect(srcRect);
		setFaceCenterPoint2(((FacialFeaturesRect)scaleRect).getCenterPoint());
		return moveRect(scaleRect);
	}
	
	//公用
	public Rect scaleRect(Rect srcRect) {
		return new FacialFeaturesRect((int)(srcRect.x * ratio), 
							(int)(srcRect.y * ratio), 
							(int)(srcRect.width * ratio), 
							(int)(srcRect.height * ratio));
	}
	
	//给从真人脸到webView专用的，向后兼容
	public void setFaceCenterPoint(Point faceCenterPoint) {
		this.faceCenterPoint = faceCenterPoint;
		calDelataXAndY();
	}
	//给从真人脸到webView专用的，向后兼容
	private void calDelataXAndY() {
		assert(faceCenterPoint != null);
		deltaX = (int) (WebViewFaceCenterPointX - faceCenterPoint.x);
		deltaY = (int) (WebViewFaceCenterPointY - faceCenterPoint.y);
	}
	
	public void setFaceCenterPoint2(Point faceCenterPoint) {
		this.faceCenterPoint = faceCenterPoint;
		calDelataXAndY2();
	}
	
	private void calDelataXAndY2() {
		assert(faceCenterPoint != null);
		assert(targetCenterPoint != null);
		deltaX = (int) (targetCenterPoint.x - faceCenterPoint.x);
		deltaY = (int) (targetCenterPoint.y - faceCenterPoint.y);
	}
	
	
	//公用
	public Rect moveRect(Rect srcRect) {
		assert(faceCenterPoint != null);
		return new FacialFeaturesRect((int)(srcRect.x + deltaX), 
				(int)(srcRect.y + deltaY), 
				(int)(srcRect.width), 
				(int)(srcRect.height));
}
	
}