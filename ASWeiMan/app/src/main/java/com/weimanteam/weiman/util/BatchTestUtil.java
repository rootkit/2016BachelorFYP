package com.weimanteam.weiman.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.weimanteam.weiman.bean.FacialFeaturesRect;
import com.weimanteam.weiman.config.Global;
import com.weimanteam.weiman.config.Params;
import com.weimanteam.weiman.model.UserModel;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class BatchTestUtil {
	private static final String TAG = "TTTTT";
	private static final int LIBRARY_SIZE = 35;
	Context context;
	public BatchTestUtil(Context context) {
		this.context = context;
		Log.i(TAG, "测试开始");
		testLBP(context);
	}
	
	public void testLBP(Context context) {
		  
		for (int i = 0; i <= 10; i++) {
			if (i == 29) continue;
			Bitmap testFaceBitmap = FileUtil.getAssetsImage(context, "test", i+".png");
			Mat mat = new Mat();
			Holder h = new Holder(mat, testFaceBitmap, i);
			Utils.bitmapToMat(testFaceBitmap, mat);
			new AsyncAsm(context).execute(h);
			try {
				Thread.sleep(4000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	class Holder {
		public Holder(Mat mat, Bitmap testFaceBitmap, int index) {
			this.mat = mat;
			this.testFaceBitmap = testFaceBitmap;
			this.index = index;
		}
		int index;
		Mat mat;
		Bitmap testFaceBitmap;
	}
	
	// 未来改进方案，替换AsyncTask框架。
		private class AsyncAsm extends AsyncTask<Holder, Integer, List<Integer>> {
			private Context context;
			private Mat src;

			public AsyncAsm(Context context) {
				this.context = context;
			}

			@Override
			protected List<Integer> doInBackground(Holder... holder) {
				List<Integer> list = new ArrayList<Integer>();
				Mat src = holder[0].mat;
				this.src = src;

				int[] points = NativeImageUtil.FindFaceLandmarks(src, 1, 1);
				for (int i = 0; i < points.length; i++) {
					list.add(points[i]);
					
					//2016.3.11 这里可以输出ASM跑出来的点坐标
				}
				Log.i(TAG, "第" + holder[0].index + "张: ");
				StringBuffer sbBuffer = new StringBuffer();
				for (int i = 0; i < 32; i++) {
					sbBuffer.append(points[i] + " ");
				}
				Log.i(TAG, "" + sbBuffer.toString());
				
				BatchTestUtil.this.drawAsmPoints(this.src, list, holder[0].testFaceBitmap);
				
				return list;
			}

			// run on UI thread
			@Override
			protected void onPostExecute(List<Integer> list) {
				
			}
		}

	public void drawAsmPoints(Mat src, List<Integer> list, Bitmap drawAsmPoints) {
		Mat dst = new Mat();
		src.copyTo(dst);

		int[] points = new int[list.size()];
		int[] FacematchingPoints = new int[32];
		
		for (int i = 0; i < list.size(); i++) {
			points[i] = list.get(i);
		}

		if (points[0] == Params.ASMError.BAD_INPUT) {
			Toast.makeText(BatchTestUtil.this.context, "Cannot load image",
					Toast.LENGTH_SHORT).show();
			Log.i(TAG, "Cannot load image");
		} else if (points[0] == Params.ASMError.INIT_FAIL) {
			Toast.makeText(BatchTestUtil.this.context,
					"Error in stasm_search_single!", Toast.LENGTH_SHORT).show();
			Log.i(TAG, "Error in stasm_search_single");
		} else if (points[0] == Params.ASMError.NO_FACE_FOUND) {
			Toast.makeText(BatchTestUtil.this.context, "未找到人脸，请重新拍照",
					Toast.LENGTH_SHORT).show();
			Log.i(TAG, "未找到人脸");
		} else {

			for (int i = 0; i < points.length - 1; i = i + 2) {
				Point p1 = new Point();
				p1.x = points[i];
				p1.y = points[i + 1];
				if(i < 32){
					FacematchingPoints[i] = points[i];
					FacematchingPoints[i+1] = points[i+1];
				}else{
					Log.e("11", "finish storing points");
				}

				Core.line(dst, p1, p1, new Scalar(255, 255, 255), 3);
				Core.putText(dst, i / 2 + "", p1, Core.FONT_HERSHEY_PLAIN, 2,
						new Scalar(255, 255, 255));
			}

			//因为暂时发型和脸型不能做匹配，为了避免错误，先对对应id数组进行初始化。
			Global.initSortedHair(context,UserModel.getInstance().getSex());
			
			//Global.sortedIndex.add(sortedIndex)的顺序要按照GridView的标签栏对应。
			//分别是“发型-脸型-眼睛-眉毛-鼻子-嘴巴”
			
			//脸型
			FacialFeaturesRect faceRect = FacialFeaturesPointUtil.getFaceRect(points);
			StartFacematching(dst.size(), FacematchingPoints);
			//将脸部拉长，因为特征点最高取到额头，没取整个脸，偏小了。
			faceRect = faceRect.getHigherRect(0.25);
			Core.rectangle(dst, faceRect.tl(), faceRect.br(), new Scalar(255,255, 255));
			
			
			
			//眼睛
			FacialFeaturesRect eyeRect = FacialFeaturesPointUtil.getEyeRect(points);
//			Core.rectangle(dst, eyeRect.tl(), eyeRect.br(), new Scalar(255,255, 255));
//			StartLBPmatching("eye", eyeRect);
			
			// 左眼
			FacialFeaturesRect leftEye = FacialFeaturesPointUtil.getLeftEyeRect(points);
			Core.rectangle(dst, leftEye.tl(), leftEye.br(), new Scalar(255,255, 255));
			StartLBPmatching("eye", leftEye, drawAsmPoints);
			
			// 右眼
			FacialFeaturesRect rightEye = FacialFeaturesPointUtil.getRightEyeRect(points);
			Core.rectangle(dst, rightEye.tl(), rightEye.br(), new Scalar(255,255, 255));

			//眉毛
			FacialFeaturesRect eyebrowRect = FacialFeaturesPointUtil.getEyebrowRect(points);
//			Core.rectangle(dst, eyebrowRect.tl(), eyebrowRect.br(), new Scalar(255, 255, 255));
//			StartLBPmatching("eyebow", eyebrowRect);// 这里暂时写的是"eyebow",因为素材的眉毛文件名拼错了
			
			// 左眉毛
			FacialFeaturesRect leftEyebrow = FacialFeaturesPointUtil.getLeftEyebrowRect(points);
			Core.rectangle(dst, leftEyebrow.tl(), leftEyebrow.br(), new Scalar(255,255, 255));
			StartLBPmatching("eyebow", leftEyebrow, drawAsmPoints);
			
			// 右眉毛
			FacialFeaturesRect rightEyebrow = FacialFeaturesPointUtil.getRightEyebrowRect(points);
			Core.rectangle(dst, rightEyebrow.tl(), rightEyebrow.br(), new Scalar(255,255, 255));

			//鼻子
			FacialFeaturesRect noseRect = FacialFeaturesPointUtil.getNoseRect(points);
			StartLBPmatching("nose", noseRect, drawAsmPoints);
			noseRect = noseRect.getThinnerRect(0.2);
			Core.rectangle(dst, noseRect.tl(), noseRect.br(), new Scalar(255,255, 255));
			
			//嘴巴
			FacialFeaturesRect mouthRect = FacialFeaturesPointUtil.getMouthRect(points);
			Core.rectangle(dst, mouthRect.tl(), mouthRect.br(), new Scalar(255,255, 255));
			StartLBPmatching("mouth", mouthRect, drawAsmPoints);
			
			for(int[] hold : Global.sortedIndex) {
				Log.i(TAG, "LBP 结果： " + hold[0]);
			}
			Global.sortedIndex.clear();
		}
		
	}
		
		public void StartLBPmatching(String name, FacialFeaturesRect Rect, Bitmap testFaceBitmap) {
			Mat temp = new Mat();
			Utils.bitmapToMat(testFaceBitmap, temp);
			Mat mouthMat = new Mat(temp, Rect);

			List<Map.Entry<Integer,Double>> mappingList = null; 
			Map<Integer,Double> MapItem = new TreeMap<Integer,Double>();
			
			for (int i = 0; i <= LIBRARY_SIZE; i++) {

				String imgname = name + "_" + i + ".jpg";
				Mat compareMat = new Mat();
				Bitmap compareBitmap = FileUtil.getAssetsImage(
						context, "data", imgname);

				if (compareBitmap == null) {
					Log.e(TAG, "DoingLBP:Bitmap empty");
					continue;
				}
				Utils.bitmapToMat(compareBitmap, compareMat);

				if (compareMat.empty())
					Log.e(TAG, "DoingLBP:image empty");
				if (mouthMat.empty())
					Log.e(TAG, "DoingLBP:image empty");
				double mouth_temp = NativeImageUtil.LBPmatching(mouthMat,
						compareMat);
				MapItem.put(i, mouth_temp);
			}
			
			//通过ArrayList构造函数把map.entrySet()转换成list 
			mappingList = new ArrayList<Map.Entry<Integer,Double>>(MapItem.entrySet()); 
			
			//通过比较器实现比较排序 选出前六位匹配
			
			Collections.sort(mappingList, new Comparator<Map.Entry<Integer,Double>>(){ 
			public int compare(Map.Entry<Integer,Double> mapping1,Map.Entry<Integer,Double> mapping2){ 
				return mapping1.getValue().compareTo(mapping2.getValue()); 
			} 
			}); 
				
			int count = 0;
			int[] sortedIndex = new int[6];
			for(Map.Entry<Integer,Double> mapping:mappingList){ 
				sortedIndex[count++] = mapping.getKey();
				if(count == 6)
					break;
				Log.e("LBP result", name + " " + mapping.getKey()+":"+mapping.getValue());
			} 
			Global.sortedIndex.add(sortedIndex);
		}
		
		public void StartFacematching(org.opencv.core.Size srcSize, int[] points_A) {
			// 将用户图片的脸部16个Node的x和y轴坐标缩放到 600 * 600内，以便更好和素材库匹配。
			double scale_width = srcSize.width / 600;
			double scale_height = srcSize.height / 600;
			double scale = scale_height < scale_width ? scale_width : scale_height;
			for(int i = 0; i < 16; i++)
			{
				points_A[i * 2] /= scale;
				points_A[i * 2 + 1] /= scale;
			}
			
			int[] points = new int[46 * 32];
			for (int i = 0; i < Global.FaceFeaturePoint.size(); i++) {
				for (int j = 0; j < Global.FaceFeaturePoint.get(i).length; j++) {
					points[i * 32 + j] = Global.FaceFeaturePoint.get(i)[j];
				}
			}
			int[] sortedIndex;
			sortedIndex = NativeImageUtil.Facematching(points_A, points);
			for (int i = 0; i < 6; i++) {
				Log.i("12", "轮廓匹配排序 "+ sortedIndex[i]);
			}
			Global.sortedIndex.add(sortedIndex);
		}
}