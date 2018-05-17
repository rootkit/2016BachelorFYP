package com.weimanteam.weiman.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;

import com.weimanteam.weiman.R;
import com.weimanteam.weiman.activity.FaceWebViewActivity;
import com.weimanteam.weiman.bean.FacialFeaturesRect;
import com.weimanteam.weiman.config.Global;
import com.weimanteam.weiman.config.Params;
import com.weimanteam.weiman.model.FacialFeaturesScaleAndMoveModel;
import com.weimanteam.weiman.model.OpenCVModel;
import com.weimanteam.weiman.model.UserModel;
import com.weimanteam.weiman.util.BatchTestUtil;
import com.weimanteam.weiman.util.Const;
import com.weimanteam.weiman.util.FacialFeaturesPointUtil;
import com.weimanteam.weiman.util.FileUtil;
import com.weimanteam.weiman.util.ImageUtils;
import com.weimanteam.weiman.util.NativeImageUtil;
import com.weimanteam.weiman.util.TakePhotoUtil;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ActionBar.LayoutParams;
import android.app.AlertDialog.Builder;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.Toast;

public class CameraUIActivity extends Activity {
	private View mPopupWindowView;
	private EditText editText;
	private PopupWindow mPopupWindow;
	private AlertDialog sex_dialog,age_dialog;
	private Builder sex_builder, age_builder;
	private Button window_btn, select_age_btn,select_sex_btn, usage_btn;
	private ImageView capture_btn, document_btn;
	private ImageView face;
	private Mat faceMat;
	public static Bitmap faceBitmap;
	private File file;
	private Uri uri;
	private ImageView iv_next;
	public static double hairRatioOfWholeFace = 0.25;//头发占据整个脸的比例
	private final double fatterFaceRatio = 0.05;//变宽的脸的比例
	private static final String TAG = "CameraUIActivity";
	private static final int CROP_PICTURE = 0;
	private static final int CROP_PICTURE_ANDROID_5 = 2;
	private static final int CHOOSE_BIG_PICTURE = 3;
	private static final int CHOOSE_SMALL_PICTURE = 4;
	private static final int LIBRARY_SIZE = 35;
	private static final int TAG_FINISH_STRING = 0;
	
	static {
		System.loadLibrary("opencv_java");
	}

	@SuppressLint("HandlerLeak") 
	Handler myHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case TAG_FINISH_STRING:
				Intent i = new Intent(CameraUIActivity.this, FaceWebViewActivity.class);
				startActivity(i);
				break;
			}
			super.handleMessage(msg);
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_camera_ui);

		Log.e(TAG, "ONCREATE");
		FileUtil.copyDataFile2LocalDir(this);
		Global.getFeatureImageData(this);
		Global.initSvgString(this);
		Global.getFaceFeaturePoint(this);

		initUI();
		initPopUpWindow();
		file = FileUtil.getOutputMediaFile(this);
		uri = Uri.fromFile(file);
		
		
	}
	
	@Override
	protected void onResume() {
		super.onResume();
//		new BatchTestUtil(this);
	}

	private void initPopUpWindow() {
		// TODO Auto-generated method stub
		window_btn = (Button) findViewById(R.id.pop_up_window);
		View popupView = getLayoutInflater().inflate(R.layout.pop_up_window, null);
        select_age_btn = (Button) popupView.findViewById(R.id.select_age);
        select_sex_btn = (Button) popupView.findViewById(R.id.select_sex);
        usage_btn = (Button)popupView.findViewById(R.id.usage);
        //设置下拉菜单
        mPopupWindow = new PopupWindow(popupView, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, true);
        mPopupWindow.setTouchable(true);
        mPopupWindow.setOutsideTouchable(true);
        mPopupWindow.setBackgroundDrawable(new BitmapDrawable(getResources(), (Bitmap) null));
        mPopupWindow.setWidth(getWindowManager().getDefaultDisplay().getWidth()/2);
        mPopupWindow.setHeight(LayoutParams.WRAP_CONTENT);
       
        window_btn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mPopupWindow.showAsDropDown(v);
            }
        });
        //设置年龄弹出框
        editText = new EditText(this);
        age_builder = new Builder(this).setTitle("输入年龄")
        		.setView(editText)
        		.setNegativeButton("取消", null);
        age_builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				Select_Age(editText.getText().toString());
			}
		});
        age_dialog = age_builder.create();
        select_age_btn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				age_dialog.show();
			}
		});
      //设置性别弹出框
        String[] items = {"男","女"};
        sex_builder = new Builder(this).setTitle("设置性别")
        		.setItems(items, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						Select_Sex(which);
					}
				});
        sex_dialog = sex_builder.create();
        select_sex_btn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				sex_dialog.show();
			}
		});
	}
	
	private void Select_Age(String string) {
		// TODO Auto-generated method stub
		int age = -1;
		try {
			age = Integer.parseInt(string);
		} catch (Exception e) {
			// TODO: handle exception
		}
		if(age >= 0 && age <= 200){
			UserModel.getInstance().setAge(age);
			Toast.makeText(getApplicationContext(), "设置成功",
					Toast.LENGTH_SHORT).show();
		}else{
			Toast.makeText(getApplicationContext(), "设置失败，请输入正确数字(0-200)",
					Toast.LENGTH_SHORT).show();
		}
	}
	private void Select_Sex(int sex) {
		// TODO Auto-generated method stub
		if(sex == 1|| sex ==0){
			UserModel.getInstance().setSex(sex);
			Toast.makeText(getApplicationContext(), "设置成功",
					Toast.LENGTH_SHORT).show();
		}else{
			Toast.makeText(getApplicationContext(), "设置失败",
					Toast.LENGTH_SHORT).show();
		}
	}
	
	private void initUI() {
		capture_btn = (ImageView) findViewById(R.id.capture);
		document_btn = (ImageView) findViewById(R.id.document);
		face = (ImageView) findViewById(R.id.imageView1);
		iv_next = (ImageView)findViewById(R.id.iv_next);
		iv_next.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				myHandler.sendEmptyMessageDelayed(TAG_FINISH_STRING, 0);
			}
		});
		iv_next.setClickable(false);
		iv_next.setAlpha(100);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode != Activity.RESULT_OK) {
			Toast.makeText(getApplicationContext(), "请重新选择图片",
					Toast.LENGTH_SHORT).show();
			return;
		}

		if (resultCode == Activity.RESULT_OK && requestCode == CROP_PICTURE) {
			doCropPhoto(uri);
		}

		if (resultCode == Activity.RESULT_OK
				&& requestCode == CROP_PICTURE_ANDROID_5) {
			Uri selectedImage = data.getData();
			String tempPicPath = TakePhotoUtil.getPath(this, selectedImage);
			Uri newUri = Uri.parse("file:///" + tempPicPath);
			if (newUri != null) {
				doCropPhoto(newUri, uri);
			}
		}

		Log.e("asm", "find if asm?");
		
		
		if (resultCode == Activity.RESULT_OK
				&& requestCode == CHOOSE_BIG_PICTURE) {
			faceBitmap = TakePhotoUtil.decodeUriAsBitmap(uri, this);
			// Bitmap hold = OpenCVModel.canny_bitmap(bitmap);
			face.setImageBitmap(faceBitmap);
			findAsmLandmarks(faceBitmap);
		}

		if (resultCode == Activity.RESULT_OK
				&& requestCode == CHOOSE_SMALL_PICTURE) {
			if (data != null) {
				faceBitmap = data.getParcelableExtra("data");
				face.setImageBitmap(faceBitmap);
			} else {
				Log.e("Exception", "CHOOSE_SMALL_PICTURE: data = " + data);
			}
		}
		
		if (resultCode == Activity.RESULT_OK  && requestCode == Const.ADJUST_ASM_POINT) {
			//调整后的点的返回位置
			List<Integer> temp = data.getIntegerArrayListExtra(Const.ADJUST_ASM_POINT_BACK);
			//根据返回的头顶的点，计算出比例头发占据脸庞的比例, 
			hairRatioOfWholeFace = (double)((temp.get(14 * 2 + 1) - temp.get(temp.size() - 1))) / (temp.get(6 * 2 + 1) - temp.get(14 * 2 + 1));
			Log.i("ratio",  (temp.get(6 * 2 + 1) - temp.get(14 * 2 + 1)) + "");
			Log.i("ratio", (temp.get(14 * 2 + 1) - temp.get(temp.size() - 1)) + "");
			Log.i("ratio", hairRatioOfWholeFace + "");
			CameraUIActivity.this.drawAsmPoints(faceMat, temp);
		}
	}

	public void doTakePhoto(View view) {
		try {
			Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE, null);
			// intent.addCategory("android.intent.category.DEFAULT");
			intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
			startActivityForResult(intent, CROP_PICTURE);
		} catch (ActivityNotFoundException e) {
			Toast.makeText(this, "请重新选择图片", Toast.LENGTH_LONG).show();
		}
	}

	public void doPickPhoto(View view) {
		if (file.exists()) file.delete();
		startActivityForResult(TakePhotoUtil.getIntentForAndroid5(uri),
				CROP_PICTURE_ANDROID_5);
	}

	private void doCropPhoto(Uri uri) {
		try {
			// 启动gallery去剪辑这个照片
			final Intent intent = TakePhotoUtil.getCropImageIntent2(uri);
			startActivityForResult(intent, CHOOSE_BIG_PICTURE);
		} catch (Exception e) {
			Toast.makeText(getApplicationContext(), "请重新选择图片",
					Toast.LENGTH_SHORT).show();
		}
	}

	private void doCropPhoto(Uri orgUri, Uri desUri) {
		try {
			// 启动gallery去剪辑这个照片
			final Intent intent = TakePhotoUtil.getCropImageIntent2(orgUri,
					desUri);
			startActivityForResult(intent, CHOOSE_BIG_PICTURE);
		} catch (Exception e) {
			Toast.makeText(getApplicationContext(), "请重新选择图片",
					Toast.LENGTH_SHORT).show();
		}
	}

	private void findAsmLandmarks(Bitmap src) {
		// if (asmBitmap != null) {
		// asmBitmap.recycle();
		// }
		
		iv_next.setClickable(false);
		iv_next.setAlpha(100);
		
//		if (UserModel.getInstance().getIsSetSex() && UserModel.getInstance().getIsSetAge()){
			faceMat = new Mat();
			Utils.bitmapToMat(src, faceMat);
			Global.sortedIndex.clear();//清空上一次ASM的结果
			new AsyncAsm(this).execute(faceMat);
//		}else{
//			Toast.makeText(this, "请先选择性别和年龄。", Toast.LENGTH_LONG).show();
//		}
//		
	}

	// 未来改进方案，替换AsyncTask框架。
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
				
				//2016.3.11 这里可以输出ASM跑出来的点坐标
			}

			return list;
		}

		// run on UI thread
		@Override
		protected void onPostExecute(List<Integer> list) {
			toAdjustASMPointByUser(list);
//			com.weimanteam.weiman.activity.CameraUIActivity.this.drawAsmPoints(this.src, list);
		}
	}
	
	
	//跳转到ASM点的调整页面
	private void toAdjustASMPointByUser(List<Integer> list) {
		Intent intent = new Intent();
		intent.putIntegerArrayListExtra(Const.ADJUST_ASM_POINT_FROM, (ArrayList<Integer>) list);
		intent.setClass(this, AdjustASMPointActivity.class);
		startActivityForResult(intent, Const.ADJUST_ASM_POINT);
	}

	private void drawAsmPoints(Mat dst, List<Integer> list) {
//		Mat dst = new Mat();
//		src.copyTo(dst);

		int[] points = new int[list.size()];
		int[] FacematchingPoints = new int[32];
		
		for (int i = 0; i < list.size(); i++) {
			points[i] = list.get(i);
		}

		if (points[0] == Params.ASMError.BAD_INPUT) {
			Toast.makeText(CameraUIActivity.this, "Cannot load image",
					Toast.LENGTH_SHORT).show();
		} else if (points[0] == Params.ASMError.INIT_FAIL) {
			Toast.makeText(CameraUIActivity.this,
					"Error in stasm_search_single!", Toast.LENGTH_SHORT).show();
		} else if (points[0] == Params.ASMError.NO_FACE_FOUND) {
			Toast.makeText(CameraUIActivity.this, "未找到人脸，请重新拍照",
					Toast.LENGTH_SHORT).show();
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
			Global.initSortedHair(this,UserModel.getInstance().getSex());
			
			//Global.sortedIndex.add(sortedIndex)的顺序要按照GridView的标签栏对应。
			//分别是“发型-脸型-眼睛-眉毛-鼻子-嘴巴”
			
			//脸型
			FacialFeaturesRect faceRect = FacialFeaturesPointUtil.getFaceRect(points);
			
			StartFacematching(dst.size(), FacematchingPoints);
			//将脸部拉长，因为特征点最高取到额头，没取整个脸，偏小了。
			
			faceRect = faceRect.getHigherRect(hairRatioOfWholeFace);
//			faceRect = faceRect.getFatterRect(fatterFaceRatio);
			
			Core.rectangle(dst, faceRect.tl(), faceRect.br(), new Scalar(255,255, 255));
			
			
			
			//眼睛
			FacialFeaturesRect eyeRect = FacialFeaturesPointUtil.getEyeRect(points);
//			Core.rectangle(dst, eyeRect.tl(), eyeRect.br(), new Scalar(255,255, 255));
//			StartLBPmatching("eye", eyeRect);
			
			// 左眼
			FacialFeaturesRect leftEye = FacialFeaturesPointUtil.getLeftEyeRect(points);
			Core.rectangle(dst, leftEye.tl(), leftEye.br(), new Scalar(255,255, 255));
			StartLBPmatching("eye", leftEye);
			
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
			StartLBPmatching("eyebow", leftEyebrow);
			
			// 右眉毛
			FacialFeaturesRect rightEyebrow = FacialFeaturesPointUtil.getRightEyebrowRect(points);
			Core.rectangle(dst, rightEyebrow.tl(), rightEyebrow.br(), new Scalar(255,255, 255));

			//鼻子
			FacialFeaturesRect noseRect = FacialFeaturesPointUtil.getNoseRect(points);
			StartLBPmatching("nose", noseRect);
			noseRect = noseRect.getThinnerRect(0.2);
			Core.rectangle(dst, noseRect.tl(), noseRect.br(), new Scalar(255,255, 255));
			
			//嘴巴
			FacialFeaturesRect mouthRect = FacialFeaturesPointUtil.getMouthRect(points);
			Core.rectangle(dst, mouthRect.tl(), mouthRect.br(), new Scalar(255,255, 255));
			StartLBPmatching("mouth", mouthRect);
			
			for(int[] hold : Global.sortedIndex) {
				System.out.println("LBP 结果： " + hold[0]);
			}
			
			Global.getSortedImageData(this);
			
			
			// 根据脸的高度初始化模型
			FacialFeaturesScaleAndMoveModel facialModel = new FacialFeaturesScaleAndMoveModel(
					faceRect.height);
			// 缩放脸部
			FacialFeaturesRect scaledFace = (FacialFeaturesRect) facialModel
					.scaleRect(faceRect);
			FacialFeaturesRect scaledNose = (FacialFeaturesRect) facialModel
					.scaleRect(noseRect);
			FacialFeaturesRect scaledEye = (FacialFeaturesRect) facialModel
					.scaleRect(eyeRect);
			FacialFeaturesRect scaledEyebrow = (FacialFeaturesRect) facialModel
					.scaleRect(eyebrowRect);
			FacialFeaturesRect scaledmouth = (FacialFeaturesRect) facialModel
					.scaleRect(mouthRect);

			// 比较缩放后的脸部中心和webView中心的距离，设置统一参数
			facialModel.setFaceCenterPoint(scaledFace.getCenterPoint());

			Global.scaledAndMovedFace = (FacialFeaturesRect) facialModel
					.moveRect(scaledFace);
			Global.scaledAndMovedEye = (FacialFeaturesRect) facialModel
					.moveRect(scaledEye);
			Global.scaledAndMovedNose = (FacialFeaturesRect) facialModel
					.moveRect(scaledNose);
			Global.scaledAndMovedEyebrow = (FacialFeaturesRect) facialModel
					.moveRect(scaledEyebrow);
			Global.scaledAndMovedMouth = (FacialFeaturesRect) facialModel
					.moveRect(scaledmouth);

			 Core.rectangle(dst, facialModel.moveRect(scaledFace).tl(),
			 facialModel.moveRect(scaledFace).br(), new Scalar(255, 255,
			 255));
			 System.out.println("缩放平移脸部：" + Global.scaledAndMovedFace);
			
			 Core.rectangle(dst, facialModel.moveRect(scaledNose).tl(),
			 facialModel.moveRect(scaledNose).br(), new Scalar(255, 255,
			 255));
			 System.out.println("缩放平移鼻子：" + Global.scaledAndMovedNose);
			
			 Core.rectangle(dst, facialModel.moveRect(scaledEye).tl(),
			 facialModel.moveRect(scaledEye).br(), new Scalar(255, 255, 255));
			 System.out.println("缩放平移眼睛：" + Global.scaledAndMovedEye);
			
			 Core.rectangle(dst, facialModel.moveRect(scaledEyebrow).tl(),
			 facialModel.moveRect(scaledEyebrow).br(), new Scalar(255, 255,
			 255));
			 System.out.println("缩放平移眉毛：" + Global.scaledAndMovedEyebrow);
			
			 Core.rectangle(dst, facialModel.moveRect(scaledmouth).tl(),
			 facialModel.moveRect(scaledmouth).br(), new Scalar(255, 255,
			 255));
			 System.out.println("缩放平移嘴巴：" + Global.scaledAndMovedMouth);

			Bitmap bmp = ImageUtils.mat2Bitmap(dst);
			// asmBitmap = Bitmap.createBitmap(bmp);
			face.setImageBitmap(bmp);
			
			
			iv_next.setAlpha(255);
			iv_next.setClickable(true);
//			myHandler.sendEmptyMessageDelayed(TAG_FINISH_STRING, 3000);
		}
	}

	public void StartLBPmatching(String name, FacialFeaturesRect Rect) {
		Mat temp = new Mat();
		Utils.bitmapToMat(faceBitmap, temp);
		Mat mouthMat = new Mat(temp, Rect);

		List<Map.Entry<Integer,Double>> mappingList = null; 
		Map<Integer,Double> MapItem = new TreeMap<Integer,Double>();
		
		for (int i = 0; i <= LIBRARY_SIZE; i++) {

			String imgname = name + "_" + i + ".jpg";
			Mat compareMat = new Mat();
			Bitmap compareBitmap = FileUtil.getAssetsImage(
					CameraUIActivity.this, "data", imgname);

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
	
	
	public void toWebView(View view) {
//		Intent i = new Intent(this, com.weimanteam.weiman.activity.FaceWebViewActivity.class);
//		startActivity(i);
	}

}
