package com.weimanteam.weiman.activity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;

import com.weimanteam.weiman.R;
import com.weimanteam.weiman.activity.FaceWebViewActivity;
import com.weimanteam.weiman.bean.FacialFeaturesRect;
import com.weimanteam.weiman.config.Global;
import com.weimanteam.weiman.config.Params;
import com.weimanteam.weiman.model.FacialFeaturesScaleAndMoveModel;
import com.weimanteam.weiman.model.OpenCVModel;
import com.weimanteam.weiman.util.FacialFeaturesPointUtil;
import com.weimanteam.weiman.util.FileUtil;
import com.weimanteam.weiman.util.ImageUtils;
import com.weimanteam.weiman.util.NativeImageUtil;
import com.weimanteam.weiman.util.TakePhotoUtil;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

public class CameraUIActivity extends Activity {
	private ImageView capture_btn,document_btn;
	private Button nextBtn;
	private ImageView face;
	private Bitmap faceBitmap;
	private Uri uri;
	private static final String TAG = "activity.CameraUIActivity";
	private static final int CROP_PICTURE = 0;
	private static final int CROP_PICTURE_ANDROID_5 = 2;
	private static final int CHOOSE_BIG_PICTURE = 3;
	private static final int CHOOSE_SMALL_PICTURE = 4;

	static {
		System.loadLibrary("opencv_java");
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_camera_ui);
		
		FileUtil.copyDataFile2LocalDir(this);
		Global.getFeatureImageData(this);
		Global.initSvgPoint(this);
		
		initUI();
		uri = Uri.fromFile(FileUtil.getOutputMediaFile(this));
		
	}

	private void initUI() {
		capture_btn = (ImageView) findViewById(R.id.capture);
		document_btn = (ImageView)findViewById(R.id.document);
		face = (ImageView)findViewById(R.id.imageView1);
		nextBtn = (Button)findViewById(R.id.button1);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if  (resultCode != Activity.RESULT_OK) {
			Toast.makeText(getApplicationContext(), "请重新选择图片", Toast.LENGTH_SHORT).show();
			return;
		}
		
        if (resultCode == Activity.RESULT_OK && requestCode == CROP_PICTURE) {
             doCropPhoto(uri);        
        }
        
        if (resultCode == Activity.RESULT_OK && requestCode == CROP_PICTURE_ANDROID_5) {
        	Uri selectedImage = data.getData();
        	String tempPicPath = TakePhotoUtil.getPath(this, selectedImage);
        	Uri newUri = Uri.parse("file:///" + tempPicPath);
        	if (newUri != null) {
        		doCropPhoto(newUri, uri);
        	}
        }	
        
		if (resultCode == Activity.RESULT_OK && requestCode == CHOOSE_BIG_PICTURE) {
			faceBitmap = TakePhotoUtil.decodeUriAsBitmap(uri, this);
//			Bitmap hold = OpenCVModel.canny_bitmap(bitmap);
			face.setImageBitmap(faceBitmap);
			findAsmLandmarks(faceBitmap);
		}
		
		if (resultCode == Activity.RESULT_OK && requestCode == CHOOSE_SMALL_PICTURE) {
			if (data != null) {
				faceBitmap = data.getParcelableExtra("data");
				face.setImageBitmap(faceBitmap);
			} else {
				Log.e("Exception", "CHOOSE_SMALL_PICTURE: data = " + data);
			}
		}
 
	}
	
	public void doTakePhoto(View view) {
		try {
			Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE, null);
//			intent.addCategory("android.intent.category.DEFAULT");
			intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);	
			startActivityForResult(intent, CROP_PICTURE);
		} catch (ActivityNotFoundException e) {
			Toast.makeText(this, "请重新选择图片", Toast.LENGTH_LONG).show();
		}
	}
	
	public void doPickPhoto(View view) {
			startActivityForResult(TakePhotoUtil.getIntentForAndroid5(uri),
					CROP_PICTURE_ANDROID_5);
	}
	
	private void doCropPhoto(Uri uri) {
		try {
			// 启动gallery去剪辑这个照片
			final Intent intent = TakePhotoUtil.getCropImageIntent2(uri);
			startActivityForResult(intent, CHOOSE_BIG_PICTURE);
		} catch (Exception e) {
			Toast.makeText(getApplicationContext(), "请重新选择图片", Toast.LENGTH_SHORT).show();
		}
	}
	
	private void doCropPhoto(Uri orgUri, Uri desUri) {
		try {
			// 启动gallery去剪辑这个照片
			final Intent intent = TakePhotoUtil.getCropImageIntent2(orgUri, desUri);
			startActivityForResult(intent, CHOOSE_BIG_PICTURE);
		} catch (Exception e) {
			Toast.makeText(getApplicationContext(), "请重新选择图片", Toast.LENGTH_SHORT).show();
		}
	}
	
	
	private void findAsmLandmarks(Bitmap src) {
//		if (asmBitmap != null) {
//			asmBitmap.recycle();
//		}
		Mat mat = new Mat();
		Utils.bitmapToMat(src, mat);
		new AsyncAsm(this).execute(mat);
	}
	
	//未来改进方案，替换AsyncTask框架。
	private class AsyncAsm extends AsyncTask<Mat, Integer, List<Integer>> {
		private Context context;
		private Mat src;

		public AsyncAsm(Context context) {
			this.context = context;
		}

		@Override
		protected List<Integer> doInBackground(Mat... mat0) {
			List<Integer> list = new ArrayList<Integer>();
			Mat src = mat0[0];
			this.src = src;

			int[] points = NativeImageUtil.FindFaceLandmarks(src, 1, 1);
			for (int i = 0; i < points.length; i++) {
				list.add(points[i]);
			}

			return list;
		}

		// run on UI thread
		@Override
		protected void onPostExecute(List<Integer> list) {
			CameraUIActivity.this.drawAsmPoints(this.src, list);
		}
	}
	
	private void drawAsmPoints(Mat src, List<Integer> list) {
		Mat dst = new Mat();
		src.copyTo(dst);

		int[] points = new int[list.size()];
		for (int i = 0; i < list.size(); i++) {
			points[i] = list.get(i);
		}

		if (points[0] == Params.ASMError.BAD_INPUT) {
			Toast.makeText(CameraUIActivity.this, "Cannot load image",
					Toast.LENGTH_SHORT).show();
		} else if (points[0] == Params.ASMError.INIT_FAIL) {
			Toast.makeText(CameraUIActivity.this, "Error in stasm_search_single!",
					Toast.LENGTH_SHORT).show();
		} else if (points[0] == Params.ASMError.NO_FACE_FOUND) {
			Toast.makeText(CameraUIActivity.this, "未找到人脸，请重新拍照",
					Toast.LENGTH_SHORT).show();
		} else {
//			for (int i = 0; i < points.length / 2 - 1; i++) {
//				Point p1 = new Point();
//				p1.x = points[2 * i];
//				p1.y = points[2 * i + 1];
//
//				Point p2 = new Point();
//				p2.x = points[2 * (i + 1)];
//				p2.y = points[2 * (i + 1) + 1];
//				Core.line(dst, p1, p2, new Scalar(255, 255, 255), 3);
//			}
			for (int i = 0; i < points.length - 1; i = i + 2) {
				Point p1 = new Point();
				p1.x = points[i];
				p1.y = points[i + 1];

				Core.line(dst, p1, p1, new Scalar(255, 255, 255), 3);
				Core.putText(dst, i / 2+ "", p1, Core.FONT_HERSHEY_PLAIN, 2, new Scalar(255, 255, 255));
			}
			
			FacialFeaturesRect mouthRect = FacialFeaturesPointUtil.getMouthRect(points);
			Core.rectangle(dst, mouthRect.tl(), mouthRect.br(), new Scalar(255, 255, 255));
//			System.out.println("嘴巴：" + mouthRect);
			
//			Mat temp = new Mat();
//			Utils.bitmapToMat(faceBitmap, temp);
//			Mat mouthMat = new Mat(temp, mouthRect);
			
			FacialFeaturesRect noseRect = FacialFeaturesPointUtil.getNoseRect(points);
			Core.rectangle(dst, noseRect.tl(), noseRect.br(), new Scalar(255, 255, 255));
//			System.out.println("鼻子：" + noseRect);
			
			FacialFeaturesRect eyeRect = FacialFeaturesPointUtil.getEyeRect(points);
			Core.rectangle(dst, eyeRect.tl(), eyeRect.br(), new Scalar(255, 255, 255));
//			System.out.println("眼睛：" + eyeRect);
			
			FacialFeaturesRect eyebrowRect = FacialFeaturesPointUtil.getEyebrowRect(points);
			Core.rectangle(dst, eyebrowRect.tl(), eyebrowRect.br(), new Scalar(255, 255, 255));
//			System.out.println("眉毛：" + eyebrowRect);
			
			FacialFeaturesRect faceRect = FacialFeaturesPointUtil.getFaceRect(points);
			Core.rectangle(dst, faceRect.tl(), faceRect.br(), new Scalar(255, 255, 255));
//			System.out.println("脸部：" + faceRect);
			
			//根据脸的高度初始化模型
			FacialFeaturesScaleAndMoveModel facialModel = new FacialFeaturesScaleAndMoveModel(faceRect.height);
			//缩放脸部
			FacialFeaturesRect scaledFace = (FacialFeaturesRect) facialModel.scaleRect(faceRect);
			FacialFeaturesRect scaledNose = (FacialFeaturesRect) facialModel.scaleRect(noseRect);
			FacialFeaturesRect scaledEye = (FacialFeaturesRect) facialModel.scaleRect(eyeRect);
			FacialFeaturesRect scaledEyebrow = (FacialFeaturesRect) facialModel.scaleRect(eyebrowRect);
			FacialFeaturesRect scaledmouth = (FacialFeaturesRect) facialModel.scaleRect(mouthRect);
			
			
			//比较缩放后的脸部中心和webView中心的距离，设置统一参数
			facialModel.setFaceCenterPoint(scaledFace.getCenterPoint());
			
			Global.scaledAndMovedFace = (FacialFeaturesRect)facialModel.moveRect(scaledFace);
			Global.scaledAndMovedEye = (FacialFeaturesRect)facialModel.moveRect(scaledEye);
			Global.scaledAndMovedNose = (FacialFeaturesRect)facialModel.moveRect(scaledNose);
			Global.scaledAndMovedEyebrow = (FacialFeaturesRect)facialModel.moveRect(scaledEyebrow);
			Global.scaledAndMovedMouth = (FacialFeaturesRect)facialModel.moveRect(scaledmouth);
			
			
			Core.rectangle(dst, facialModel.moveRect(scaledFace).tl(), facialModel.moveRect(scaledFace).br(), new Scalar(255, 255, 255));
			System.out.println("缩放平移脸部：" + Global.scaledAndMovedFace);
			
			Core.rectangle(dst, facialModel.moveRect(scaledNose).tl(), facialModel.moveRect(scaledNose).br(), new Scalar(255, 255, 255));
			System.out.println("缩放平移鼻子：" + Global.scaledAndMovedNose);
			
			Core.rectangle(dst, facialModel.moveRect(scaledEye).tl(), facialModel.moveRect(scaledEye).br(), new Scalar(255, 255, 255));
			System.out.println("缩放平移眼睛：" + Global.scaledAndMovedEye);
			
			Core.rectangle(dst, facialModel.moveRect(scaledEyebrow).tl(), facialModel.moveRect(scaledEyebrow).br(), new Scalar(255, 255, 255));
			System.out.println("缩放平移眉毛：" + Global.scaledAndMovedEyebrow);
			
			Core.rectangle(dst, facialModel.moveRect(scaledmouth).tl(), facialModel.moveRect(scaledmouth).br(), new Scalar(255, 255, 255));
			System.out.println("缩放平移嘴巴：" + Global.scaledAndMovedMouth);
			
			Bitmap bmp = ImageUtils.mat2Bitmap(dst);
//			asmBitmap = Bitmap.createBitmap(bmp);
			face.setImageBitmap(bmp);
			
			
			
//			---------------------------svg缩放平移
//			ArrayList<ArrayList<Point>> pointsList = FileUtil.getAssetsFileAndParse(this, "data", "eyeData.js");
//
//			FacialFeaturesRect svgEyeRect = FacialFeaturesPointUtil.getRect(pointsList.get(0));//0表示svg中的eye0
//			Log.i(TAG, "svg Eye： " + svgEyeRect);
//			FacialFeaturesScaleAndMoveModel eyeModel = new FacialFeaturesScaleAndMoveModel(Global.scaledAndMovedEye.width, svgEyeRect.width, Global.scaledAndMovedEye.getCenterPoint());
//			
//			
//			FacialFeaturesRect scaledAndMoveedsvgEyeRect = (FacialFeaturesRect) eyeModel.scaleAndMoveRect2(svgEyeRect);
//			Log.i(TAG, "ratio: " + eyeModel.ratio);
//			Log.i(TAG, "deltaX: " + eyeModel.deltaX);
//			Log.i(TAG, "deltaY:" + eyeModel.deltaY);
//			Log.i(TAG, scaledAndMoveedsvgEyeRect + "");
			
		}
	}
	
	public void toWebView(View view) {
		Intent i = new Intent(this, FaceWebViewActivity.class);
		startActivity(i);
	}
}
