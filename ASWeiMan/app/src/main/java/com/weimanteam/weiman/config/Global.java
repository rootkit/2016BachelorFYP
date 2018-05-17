package com.weimanteam.weiman.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

import org.opencv.core.Point;

import com.weimanteam.weiman.bean.FacialFeaturesRect;
import com.weimanteam.weiman.util.FileUtil;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * 全局类
 * 
 * @author HHX
 * @version 创建时间:2015年11月04日13:59:41
 * 
 */
public class Global {
	public static final String APP_NAME = "WeiMan";
	public static final int LIBRARY_SIZE = 46;

	// 素材库中全部的素材存储在mFeatureArrayList中
	final public static ArrayList<ArrayList<Integer>> mFeatrueArrayList = new ArrayList<ArrayList<Integer>>();

	// 匹配算法之后选出匹配度最高的前六个素材图片存储在showFeatureArrayList中
	public static ArrayList<ArrayList<Integer>> showFeatureArrayList = new ArrayList<ArrayList<Integer>>();

	// 匹配算法之后选出的匹配度最高前六位下标index
	public static ArrayList<int[]> sortedIndex = new ArrayList<int[]>();
	
	// GridView中每个list最后一次点击的index纪录
	public static int[] lastClickedIndex = new int [20];

	// 真人脸变形和平移后确定在webView中的位置
	public static volatile FacialFeaturesRect scaledAndMovedFace;
	public static volatile FacialFeaturesRect scaledAndMovedEye;
	public static volatile FacialFeaturesRect scaledAndMovedNose;
	public static volatile FacialFeaturesRect scaledAndMovedEyebrow;
	public static volatile FacialFeaturesRect scaledAndMovedMouth;
	public static volatile FacialFeaturesRect scaledAndMovedHat;

	// 从Data.js中提取出来的各个控制点的字符串
	public static ArrayList<ArrayList<String>> hairStringList;
	public static ArrayList<ArrayList<String>> faceStringList;
	public static ArrayList<ArrayList<String>> eyeStringList;
	public static ArrayList<ArrayList<String>> noseStringList;
	public static ArrayList<ArrayList<String>> eyebrowStringList;
	public static ArrayList<ArrayList<String>> mouthStringList;
	public static ArrayList<ArrayList<String>> hatStringList;

	// 保存LBP matching之后配对的五官id,用于传给下一个activity
	public static int mouth_index = 0;
	public static int nose_index = 0;
	public static int eye_index = 0;
	public static int eyebrow_index = 0;
	public static int face_index = 0;
	public static int hair_index ;

	public static ArrayList<int[]> FaceFeaturePoint = new ArrayList<int[]>();

	public static void initSortedHair(Context context, int sex) {
		if(sex == 0){
			hair_index = (int)(Math.random()*3+1);
			int[] hairIndex = { 1, 2 ,3, 4, 0, 5};
			sortedIndex.add(hairIndex);
		}
		else if (sex == 1){
			hair_index = 0;
			int[] hairIndex = { 0, 1, 2 ,3, 4, 5};
			sortedIndex.add(hairIndex);
		}
		
	}

	
	
	public static void getFaceFeaturePoint(Context context) {

		BufferedReader reader = null;
		String s = null;
		try {
			reader = new BufferedReader(new InputStreamReader(context
					.getAssets()
					.open("data" + File.separator + "facePoint.txt")));
			while ((s = reader.readLine()) != null) {
				Log.e("s", s);
				String[] ss = s.split(" ");
				int[] points = new int[32];
				if (32 != ss.length)
					continue;
				for (int i = 0; i < ss.length; i++) {
					points[i] = Integer.parseInt(ss[i]);
					Log.e("22", points[i] + "");
				}
				FaceFeaturePoint.add(points);
			}
		} catch (IOException e) {
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
				}
			}
		}
	}

	public static ArrayList<ArrayList<Integer>> getSortedImageData(
			Context context) {
//		hair_index = sortedIndex.get(0)[0];
		face_index = sortedIndex.get(1)[0];
		eye_index = sortedIndex.get(2)[0];
		eyebrow_index = sortedIndex.get(3)[0];
		nose_index = sortedIndex.get(4)[0];
		mouth_index = sortedIndex.get(5)[0];
		//帽子没用上述写法
		
		int total = 7;//素材总类型数
		//移除所有素材
		int currentSize = showFeatureArrayList.size();
		for (int i = currentSize - 1; i >= 0; i--) {
			showFeatureArrayList.remove(i);
		}
		//新建素材容器
		//确保两者长度相等，即各个部位的素材都放进对应的ArrayList中
		while(showFeatureArrayList.size() < total) {
			showFeatureArrayList.add(new ArrayList<Integer>());
		}
		int SortedSize = sortedIndex.get(0).length;

		// 简单根据文件名读取drawable的发型数据
		for (int i = 0; i < SortedSize; i++) {
			int drawable = context.getResources().getIdentifier(
					"hair_" + sortedIndex.get(0)[i], "drawable",
					context.getPackageName());
			showFeatureArrayList.get(0).add(drawable);
		}

		// 简单根据文件名读取drawable的脸型数据
		for (int i = 0; i < SortedSize; i++) {
			int drawable = context.getResources().getIdentifier(
					"face_" + sortedIndex.get(1)[i], "drawable",
					context.getPackageName());
			showFeatureArrayList.get(1).add(drawable);
		}

		// 简单根据文件名读取drawable的眼睛数据
		for (int i = 0; i < SortedSize; i++) {
			int drawable = context.getResources().getIdentifier(
					"eye_" + sortedIndex.get(2)[i], "drawable",
					context.getPackageName());
			showFeatureArrayList.get(2).add(drawable);
		}

		// 简单根据文件名读取drawable的眉毛数据
		for (int i = 0; i < SortedSize; i++) {
			int drawable = context.getResources().getIdentifier(
					"eyebrow_" + sortedIndex.get(3)[i], "drawable",
					context.getPackageName());
			showFeatureArrayList.get(3).add(drawable);
		}

		// 简单根据文件名读取drawable的鼻子数据
		for (int i = 0; i < SortedSize; i++) {
			int drawable = context.getResources().getIdentifier(
					"nose_" + sortedIndex.get(4)[i], "drawable",
					context.getPackageName());
			showFeatureArrayList.get(4).add(drawable);
		}

		// 简单根据文件名读取drawable的嘴巴数据
		for (int i = 0; i < SortedSize; i++) {
			int drawable = context.getResources().getIdentifier(
					"mouth_" + sortedIndex.get(5)[i], "drawable",
					context.getPackageName());
			showFeatureArrayList.get(5).add(drawable);
		}

		// 帽子
		for (int i = 0; i < 1; i++) {
			int drawable = context.getResources().getIdentifier("hat_" + (i),
					"drawable", context.getPackageName());
			showFeatureArrayList.get(6).add(drawable);
		}

		return showFeatureArrayList;
	}

	public static ArrayList<ArrayList<Integer>> getFeatureImageData(
			Context context) {
		mFeatrueArrayList.add(new ArrayList<Integer>());

		// 简单根据文件名读取drawable的发型数据
		for (int i = 0; i <= 5; i++) {
			int drawable = context.getResources().getIdentifier("hair_" + (i),
					"drawable", context.getPackageName());

			mFeatrueArrayList.get(0).add(drawable);
		}

		mFeatrueArrayList.add(new ArrayList<Integer>());

		// 简单根据文件名读取drawable的脸型数据
		// 此处i从0开始
		for (int i = 0; i < LIBRARY_SIZE; i++) {
			int drawable = context.getResources().getIdentifier("face_" + (i),
					"drawable", context.getPackageName());

			mFeatrueArrayList.get(1).add(drawable);
		}

		mFeatrueArrayList.add(new ArrayList<Integer>());
		// 简单根据文件名读取drawable的眼睛数据
		// 此处i从0开始
		for (int i = 0; i < LIBRARY_SIZE; i++) {
			int drawable = context.getResources().getIdentifier("eye_" + (i),
					"drawable", context.getPackageName());
			mFeatrueArrayList.get(2).add(drawable);
		}

		mFeatrueArrayList.add(new ArrayList<Integer>());
		// 简单根据文件名读取drawable的眉毛数据
		// 此处i从0开始
		for (int i = 0; i < LIBRARY_SIZE; i++) {
			int drawable = context.getResources().getIdentifier(
					"eyebrow_" + (i), "drawable", context.getPackageName());
			mFeatrueArrayList.get(3).add(drawable);
		}

		mFeatrueArrayList.add(new ArrayList<Integer>());
		// 简单根据文件名读取drawable的鼻子数据
		// 此处i从0开始
		for (int i = 0; i < LIBRARY_SIZE; i++) {
			int drawable = context.getResources().getIdentifier("nose_" + (i),
					"drawable", context.getPackageName());
			mFeatrueArrayList.get(4).add(drawable);
		}

		mFeatrueArrayList.add(new ArrayList<Integer>());
		// 简单根据文件名读取drawable的嘴巴数据
		// 此处i从0开始
		for (int i = 0; i < LIBRARY_SIZE; i++) {
			int drawable = context.getResources().getIdentifier("mouth_" + (i),
					"drawable", context.getPackageName());
			mFeatrueArrayList.get(5).add(drawable);
		}

		mFeatrueArrayList.add(new ArrayList<Integer>());
		// 帽子
		for (int i = 0; i < 1; i++) {
			int drawable = context.getResources().getIdentifier("hat_" + (i),
					"drawable", context.getPackageName());
			mFeatrueArrayList.get(6).add(drawable);
		}

		return mFeatrueArrayList;
	}

	public static void initSvgString(Context context) {
		try {
			if (hairStringList == null)
				hairStringList = FileUtil.getAssetsFileAndParseString(context,
						"data", "hairData.js");
			if (eyebrowStringList == null)
				eyebrowStringList = FileUtil.getAssetsFileAndParseString(
						context, "data", "eyebrowData.js");
			if (eyeStringList == null)
				eyeStringList = FileUtil.getAssetsFileAndParseString(context,
						"data", "eyeData.js");
			if (faceStringList == null)
				faceStringList = FileUtil.getAssetsFileAndParseString(context,
						"data", "faceData.js");
			if (noseStringList == null)
				noseStringList = FileUtil.getAssetsFileAndParseString(context,
						"data", "noseData.js");
			if (mouthStringList == null)
				mouthStringList = FileUtil.getAssetsFileAndParseString(context,
						"data", "mouthData.js");
			if (hatStringList == null)
				hatStringList = FileUtil.getAssetsFileAndParseString(context,
						"data", "hatData.js");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
