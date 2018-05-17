package com.weimanteam.weiman.config;

import java.util.ArrayList;
import java.util.HashMap;

import org.opencv.core.Point;

import com.weimanteam.weiman.bean.FacialFeaturesRect;
import com.weimanteam.weiman.util.FileUtil;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * 全局类
 * 
 * @author HHX
 * @version 创建时间:2015年11月04日13:59:41
 * 
 */
public class Global
{
	public static final String APP_NAME = "WeiMan";
	final public static ArrayList<ArrayList<Integer>> mFeatrueArrayList = new ArrayList<ArrayList<Integer>>();
	//真人脸变形和平移后确定在webView中的位置
	public static volatile FacialFeaturesRect scaledAndMovedFace;
	public static volatile FacialFeaturesRect scaledAndMovedEye;
	public static volatile FacialFeaturesRect scaledAndMovedNose;
	public static volatile FacialFeaturesRect scaledAndMovedEyebrow;
	public static volatile FacialFeaturesRect scaledAndMovedMouth;
	//从Data.js中提取出来的各个控制点，子List中存放的是某个五官，整个List表示一列对应于同一个五官的数据。
	public static ArrayList<ArrayList<Point>> facePointsList;
	public static ArrayList<ArrayList<Point>> eyePointsList;
	public static ArrayList<ArrayList<Point>> nosePointsList;
	public static ArrayList<ArrayList<Point>> eyebrowPointsList;
	public static ArrayList<ArrayList<Point>> mouthPointsList;
	
	public static ArrayList<ArrayList<Integer>> getFeatureImageData(Context context) {
		mFeatrueArrayList.add(new ArrayList<Integer>());

		// 简单根据文件名读取drawable的发型数据
		for (int i = 0; i < 75; i++) {
			int drawable = context.getResources().getIdentifier("hair_" + (i),
					"drawable", context.getPackageName());
			
			mFeatrueArrayList.get(0).add(drawable);
		}
		
		mFeatrueArrayList.add(new ArrayList<Integer>());
		
		// 简单根据文件名读取drawable的脸型数据
		// 此处i从1开始
		for (int i = 1; i <= 15; i++) {
			int drawable = context.getResources().getIdentifier("face_" + (i),
					"drawable", context.getPackageName());
			
			mFeatrueArrayList.get(1).add(drawable);
		}
		
		mFeatrueArrayList.add(new ArrayList<Integer>());
		// 简单根据文件名读取drawable的眼睛数据
		// 此处i从1开始
		for (int i = 1; i <= 15; i++) {
			int drawable = context.getResources().getIdentifier("eye_" + (i),
					"drawable", context.getPackageName());
			mFeatrueArrayList.get(2).add(drawable);
		}
		
		mFeatrueArrayList.add(new ArrayList<Integer>());
		// 简单根据文件名读取drawable的眉毛数据
		// 此处i从1开始
		for (int i = 1; i <= 15; i++) {
			int drawable = context.getResources().getIdentifier("eyebrow_" + (i),
					"drawable", context.getPackageName());
			mFeatrueArrayList.get(3).add(drawable);
		}
		
		mFeatrueArrayList.add(new ArrayList<Integer>());
		// 简单根据文件名读取drawable的鼻子数据
		// 此处i从1开始
		for (int i = 1; i <= 15; i++) {
			int drawable = context.getResources().getIdentifier("nose_" + (i),
					"drawable", context.getPackageName());
			mFeatrueArrayList.get(4).add(drawable);
		}
		
		mFeatrueArrayList.add(new ArrayList<Integer>());
		// 简单根据文件名读取drawable的嘴巴数据
		// 此处i从1开始
		for (int i = 1; i <= 15; i++) {
			int drawable = context.getResources().getIdentifier("mouth_" + (i),
					"drawable", context.getPackageName());
			mFeatrueArrayList.get(5).add(drawable);
		}

		return mFeatrueArrayList;
	}
	
	public static void initSvgPoint(Context context) {
		if (eyebrowPointsList == null) eyebrowPointsList = FileUtil.getAssetsFileAndParse(context, "data", "eyebrowData.js");
		if (eyePointsList == null) eyePointsList = FileUtil.getAssetsFileAndParse(context, "data", "eyeData.js");
		if (facePointsList == null) facePointsList = FileUtil.getAssetsFileAndParse(context, "data", "faceData.js");
		if (nosePointsList == null) nosePointsList = FileUtil.getAssetsFileAndParse(context, "data", "noseData.js");
		if (mouthPointsList == null) mouthPointsList = FileUtil.getAssetsFileAndParse(context, "data", "mouthData.js");
	}
	
	
	/**
	 * 保存Account
	 * 
	 * @param context
	 * @param token
	 */
	public static void saveAccount(Context context, String account)
	{
		SharedPreferences sp = context.getSharedPreferences(APP_NAME,
				Context.MODE_PRIVATE);
		sp.edit().putString("account", account).commit();
	}

	/**
	 * 获取Account
	 * 
	 * @param context
	 * @return
	 */
	public static String getAccount(Context context)
	{
		SharedPreferences sp = context.getSharedPreferences(APP_NAME,
				Context.MODE_PRIVATE);
		return sp.getString("account", null);
	}
	
}
