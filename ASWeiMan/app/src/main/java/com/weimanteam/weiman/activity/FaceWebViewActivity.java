package com.weimanteam.weiman.activity;

import java.io.File;
import java.util.ArrayList;

import org.opencv.core.Point;

import com.larvalabs.svgandroid.SVGParser;
import com.shizhefei.view.indicator.FragmentListPageAdapter;
import com.shizhefei.view.indicator.IndicatorViewPager;
import com.shizhefei.view.indicator.ScrollIndicatorView;
import com.shizhefei.view.indicator.IndicatorViewPager.IndicatorFragmentPagerAdapter;
import com.shizhefei.view.indicator.slidebar.ColorBar;
import com.shizhefei.view.indicator.transition.OnTransitionTextListener;
import com.weimanteam.weiman.R;
import com.weimanteam.weiman.bean.FacialFeaturesRect;
import com.weimanteam.weiman.config.Global;
import com.weimanteam.weiman.fragment.GridFeatureImageFragment;
import com.weimanteam.weiman.fragment.MoreFragment;
import com.weimanteam.weiman.fragment.GridFeatureImageFragment.WebViewfunction;
import com.weimanteam.weiman.model.FacialFeaturesScaleAndMoveModel;
import com.weimanteam.weiman.model.ScaleAndMoveModel;
import com.weimanteam.weiman.model.ScaleAndMoveModel.MatchPosition;
import com.weimanteam.weiman.util.BezierUtil;
import com.weimanteam.weiman.util.FacialFeaturesPointUtil;
import com.weimanteam.weiman.util.FileUtil;
import com.weimanteam.weiman.util.SnapUtil;


import android.os.Bundle;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.Region;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;


@SuppressLint("SetJavaScriptEnabled") 
public class FaceWebViewActivity extends FragmentActivity implements WebViewfunction{
	private WebView webView;
	private RelativeLayout relativelayoutWebview;
	private RelativeLayout relativelayoutOp;
	private IndicatorViewPager indicatorViewPager;
	private LayoutInflater inflate;
	private String[] names = {"发型", "脸型", "眼睛", "眉毛", "鼻子","嘴巴", "装饰"};
	private int size = names.length;
	private ScrollIndicatorView indicator;
	private ImageView back, share;
	private static final String TAG = "activity.com.weimanteam.weiman.activity.FaceWebViewActivity";
	private double ratio = 0;
	private FacialFeaturesRect faceRectHold = null;
	private double faceRatioHold = 0;
	//以下两个数值表示当前用户点击的类型和数据，用于第一次和第二次与webView的交互的缓存。主要是为了异步线程的回调。
	private int currentType = 0;
	private int currentPosition = 0;
	@SuppressLint("JavascriptInterface") @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		
		back = (ImageView)findViewById(R.id.iv_back);
		back.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				FaceWebViewActivity.this.finish();
			}
		});
		
		share = (ImageView)findViewById(R.id.iv_continue);
		share.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				File outputImage = FileUtil.getOutputMediaFileRandom(FaceWebViewActivity.this);
				Bitmap bitmap = SnapUtil.captureWebView(webView);
				FileUtil.writeBitmap(outputImage, bitmap);
				
				Intent i = new Intent(FaceWebViewActivity.this, ThemeActivity.class);
				startActivity(i);
			}
		});
		
		indicator = (ScrollIndicatorView) findViewById(R.id.siv_features);
		indicator.setScrollBar(new ColorBar(this, Color.RED, 5));
		
		// 选中和未选中的颜色调整，color目录下selector也有设置，可能有冲突
		int selectColorId = R.color.tab_top_text_2;
		int unSelectColorId = R.color.tab_top_text_1;
		indicator.setOnTransitionListener(new OnTransitionTextListener().setColorId(this, selectColorId, unSelectColorId));
		
		ViewPager viewPager = (ViewPager) findViewById(R.id.vp_features);
		
		viewPager.setOffscreenPageLimit(2);
		indicatorViewPager = new IndicatorViewPager(indicator, viewPager);
		inflate = LayoutInflater.from(getApplicationContext());
		indicatorViewPager.setAdapter(new MyAdapter(getSupportFragmentManager()));
		
		
		webView = (WebView) this.findViewById(R.id.webview);
		webView.requestFocus();
		
		relativelayoutWebview = (RelativeLayout) findViewById(R.id.relativelayout_webview);
		relativelayoutOp = (RelativeLayout) findViewById(R.id.rl_option);

		webView.setWebChromeClient(new WebChromeClient());
		
		webView.getSettings().setJavaScriptEnabled(true);
		webView.getSettings().setDefaultTextEncodingName("utf-8");
		
		webView.setHorizontalScrollBarEnabled(false);
		webView.setVerticalScrollBarEnabled(false);
		webView.addJavascriptInterface(new JavaScriptInterface(this), "jsObj");
		
		
		webView.setWebViewClient(new WebViewClient(){
			@Override
			public void onPageFinished(WebView view, String url){
				//webView.loadUrl("javascript:ChangeAllId(" + Global.hair_index + "," + Global.face_index + ","+ Global.eye_index + "," + Global.eyebrow_index + "," + 20 + "," + Global.mouth_index + ")");
				
				faceFeatureScaleAndMove(1,Global.face_index);
				faceFeatureScaleAndMove(2,Global.eye_index);
				faceFeatureScaleAndMove(3,Global.eyebrow_index);
				faceFeatureScaleAndMove(4,Global.nose_index);
				faceFeatureScaleAndMove(5,Global.mouth_index);
				faceFeatureScaleAndMove(0,Global.hair_index);
			}
		});
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		webView.postDelayed(new Runnable() {
			@Override
			public void run() {
				webView.loadUrl("file:///android_asset/test1.html");
			}
		}, 100);
		
	}


	class JavaScriptInterface {
		Context context;
		public JavaScriptInterface(Context activity){
			this.context = activity;
		}
		
		@JavascriptInterface
		public void setRect(double top, double bottom, double left, double right, double scrollLeft) {
			Log.i(TAG, "top: " + top + "  bottom: " + bottom + " left: " + left + " right: " + right);
		}
		
	}

	private class MyAdapter extends IndicatorFragmentPagerAdapter {
		public MyAdapter(FragmentManager fragmentManager) {
			super(fragmentManager);
		}

		@Override
		public int getCount() {
			return size;
		}

		@Override
		public View getViewForTab(int position, View convertView, ViewGroup container) {
			if (convertView == null) {
				convertView = inflate.inflate(R.layout.tab_top, container, false);
			}
			TextView textView = (TextView) convertView;
			textView.setText(names[position % names.length]);
			textView.setPadding(20, 0, 20, 0);
			return convertView;
		}

		@Override
		public Fragment getFragmentForPage(int position) {
			if (position < 7) {
				GridFeatureImageFragment featureImageFragment = new GridFeatureImageFragment();
				Bundle bundle = new Bundle();
				bundle.putInt(MoreFragment.INTENT_INT_INDEX, position);
				
				
				featureImageFragment.setArguments(bundle);
				return featureImageFragment;
			}
			
			
			MoreFragment fragment = new MoreFragment();
			Bundle bundle = new Bundle();
			bundle.putInt(MoreFragment.INTENT_INT_INDEX, position);
			fragment.setArguments(bundle);
			return fragment;
		}

		@Override
		public int getItemPosition(Object object) {
			return FragmentListPageAdapter.POSITION_NONE;
		}

	}
	

	@Override
	public void faceFeatureScaleAndMove(int type, int position) {
		ArrayList<Point> controlPoints = new ArrayList<Point>();
		ArrayList<ArrayList<String>> pointsStrings = getStringListByType(type);
		for (String string : pointsStrings.get(position)) {
			Path path = SVGParser.doPath(string);
			Region region = new Region();
		    region.setPath(path, new Region(0,0,10000,10000)); 
		    android.graphics.Rect r = region.getBounds();
			controlPoints.add(new Point(r.left, r.top));
			controlPoints.add(new Point(r.right, r.bottom));
		}
		
		//根据点集计算矩形
		FacialFeaturesRect svgEyeRect = FacialFeaturesPointUtil.getRect(controlPoints);//0表示svg中的eye0
		Log.i(TAG, "svg Rect： " + svgEyeRect);
		FacialFeaturesRect targetRect = getScaledAndMovedRectByType(type);
		//选择发型时需要临时根据脸生成一个targetRect
		if (type == 0) {
			//头发略比脸宽，1.15
			double r = (Global.scaledAndMovedFace.width * 1.15) / svgEyeRect.width;
			//脸的上部中点作为匹配点
			Point hold = Global.scaledAndMovedFace.getTopCenterPoint();
			if (faceRectHold != null && faceRatioHold != 0) {
				//底部位置减去高度，等于顶部位置,所以此时计算的头发高度和头顶平齐。
				hold.y = Global.scaledAndMovedFace.getBottomCenterPoint().y - faceRectHold.height * faceRatioHold;
				//上移
				hold.y *= 0.88;
			} else {
				//比目标脸略低，因为此时采用的是脸的底部匹配。
				hold.y *= 1.1;
			}
			
			ScaleAndMoveModel scaleAndMoveModel = new ScaleAndMoveModel(r, hold, svgEyeRect, MatchPosition.Topmiddle);
			System.out.println("scaleAndMoveModel.ratio: " + scaleAndMoveModel.ratio);
			webView.loadUrl("javascript:facialConfigChange(" + type+ "," + position + ","+ scaleAndMoveModel.deltaX + "," + scaleAndMoveModel.deltaY + "," + scaleAndMoveModel.ratio + ")");
			return;
		}
		
		if (type == 6){//hat
			double r = (Global.scaledAndMovedFace.width* 1.15) / svgEyeRect.width;
			Point hold = Global.scaledAndMovedFace.getTopCenterPoint();
			hold.y *= 2;//hat最低处比脸最高处低
			ScaleAndMoveModel scaleAndMoveModel = new ScaleAndMoveModel(r, hold, svgEyeRect, MatchPosition.Bottommiddle);
			webView.loadUrl("javascript:facialConfigChange(" + type+ "," + position + ","+ scaleAndMoveModel.deltaX + "," + scaleAndMoveModel.deltaY + "," + scaleAndMoveModel.ratio + ")");
		}
		
		if (targetRect == null) return;
		Log.i(TAG, "target Rect： " + targetRect);
		
		//依照宽度缩放脸部，但是匹配点改为最下方中点。
		ScaleAndMoveModel scaleAndMoveModel;
		if (type == 1) {
			ratio = (double)targetRect.width / svgEyeRect.width;
			if (position > 35 && position != 38) //因为38也是不带耳朵的，混进来了...
				ratio *= 1.15;
			
			faceRectHold = svgEyeRect;
			faceRatioHold = ratio;
			
			scaleAndMoveModel = new ScaleAndMoveModel(ratio, targetRect.getBottomCenterPoint(), svgEyeRect, MatchPosition.Bottommiddle);
		}  
		else if (type == 4) {
			//鼻子的缩放比例（宽度）乘以0.9，使之变小
			double horizontalRatio = ((double)targetRect.width * 0.9) / svgEyeRect.width;
			double verticalRatio = ((double)targetRect.height ) / svgEyeRect.height;
			ratio =  horizontalRatio < verticalRatio ? horizontalRatio : verticalRatio;
			
			//将原本固定的五官位置，改为随着不同脸的高度变化而变化
			Point newtargetPoint = targetRect.getCenterPoint();
			newtargetPoint.y +=  calMovingHeight(type);
			
			scaleAndMoveModel = new ScaleAndMoveModel(ratio, newtargetPoint, svgEyeRect, MatchPosition.Centermiddle);
		}
		//眉毛
		else if (type == 3) {
			ratio = (double)targetRect.width / svgEyeRect.width;
			
			//将原本固定的五官位置，改为随着不同脸的高度变化而变化
			Point newtargetPoint = targetRect.getBottomCenterPoint();
			newtargetPoint.y +=  calMovingHeight(type);
			//s
			scaleAndMoveModel = new ScaleAndMoveModel(ratio, newtargetPoint, svgEyeRect, MatchPosition.Bottommiddle);
		}
		else {
			ratio = (double)targetRect.width / svgEyeRect.width;
			
			//将原本固定的五官位置，改为随着不同脸的高度变化而变化
			Point newtargetPoint = targetRect.getCenterPoint();
			newtargetPoint.y +=  calMovingHeight(type);
			
			scaleAndMoveModel = new ScaleAndMoveModel(ratio, newtargetPoint, svgEyeRect, MatchPosition.Centermiddle);
		}																																																		

		Log.i(TAG, position + "dasdfasdfasdfas");
		webView.loadUrl("javascript:facialConfigChange(" + type+ "," + position + ","+ scaleAndMoveModel.deltaX + "," + scaleAndMoveModel.deltaY + "," + scaleAndMoveModel.ratio + ")");
	}
	
	
	private ArrayList<ArrayList<String>> getStringListByType(int type) {
		switch (type) {
		case 0:
			return Global.hairStringList;
		case 1:
			return Global.faceStringList;
		case 2:
			return Global.eyeStringList;
		case 3:
			return Global.eyebrowStringList;
		case 4:
			return Global.noseStringList;
		case 5:
			return Global.mouthStringList;
		case 6:
			return Global.hatStringList;
		default:
			break;
		}
		return null;
	}
	
	private FacialFeaturesRect getScaledAndMovedRectByType(int type) {
		switch (type) {
		case 0:
			return null;
		case 1:
			return Global.scaledAndMovedFace;
		case 2:
			return Global.scaledAndMovedEye;
		case 3:
			return Global.scaledAndMovedEyebrow;
		case 4:
			return Global.scaledAndMovedNose;
		case 5:
			return Global.scaledAndMovedMouth;
		case 6:
			return null;
		default:
			break;
		}
		return null;
	}
	
	//计算因为在脸部匹配采用了左右宽度恒定，底部对齐之后，导致不同的脸在webView中的显示高度不一样
	//而对于五官来说，它们的位置应该在脸的高度变化的同时也发生变化，此函数计算变化比例。
	//此函数应应用于鼻子、嘴巴、眼睛、眉毛这四个五官。
	private double calMovingHeight(int index){
		if (faceRectHold == null || faceRatioHold == 0)
			return 0;
		
//		//webView中显示的脸的实际高度
//		double faceHeight = faceRectHold.height * faceRatioHold;
//		//五官到脸底部的距离比上整个脸的高度,所得值用于计算五官相对于脸的位置
//		double standardRato = (Global.scaledAndMovedFace.getBottomCenterPoint().y - getScaledAndMovedRectByType(index).getCenterPoint().y) / (Global.scaledAndMovedFace.getBottomCenterPoint().y - Global.scaledAndMovedFace.getTopCenterPoint().y);
//		//对于用户选择的某个脸型,此时五官到脸最下方的距离
//		double actualHeight = faceHeight * standardRato;
//		double moveHeight = (Global.scaledAndMovedFace.getBottomCenterPoint().y - getScaledAndMovedRectByType(index).getCenterPoint().y) - actualHeight;
//		
//		return moveHeight;
		
		//以上的398到406，就是错误代码示范！！
		//之前的坑，这里回踩了~~
		//以上代码会最终导致，脸上五官整体偏上或者偏下，一般来说是偏下。原因在于CameraUIActivity.java文件中faceRect = faceRect.getHigherRect(hairRatioOfWholeFace); 人为改变位置，而此时又以改变后的位置当做标准位置计算。
		//所以采用的方法是，先临时复原原先的数据，再重新计算。
		
		//webView中显示的脸的实际高度
		double faceHeight = faceRectHold.height * faceRatioHold;
		
		//先得到ASM计算出来的脸的Rect，然后按照经验，增高一点点，而不是原来那么夸张。
		double whloeFace =  (Global.scaledAndMovedFace.getBottomCenterPoint().y - Global.scaledAndMovedFace.getTopCenterPoint().y) / (1 + CameraUIActivity.hairRatioOfWholeFace) * (1 + 0.151); // 0.140 这个值越小，整体越往上移动
		//五官到脸底部的距离比上整个脸的高度,所得值用于计算五官相对于脸的位置
		double standardRato = (Global.scaledAndMovedFace.getBottomCenterPoint().y - getScaledAndMovedRectByType(index).getCenterPoint().y) / whloeFace;
		//对于用户选择的某个脸型,此时五官到脸最下方的距离
		double actualHeight = faceHeight * standardRato;
		double moveHeight = (Global.scaledAndMovedFace.getBottomCenterPoint().y - getScaledAndMovedRectByType(index).getCenterPoint().y) - actualHeight;
		
		return moveHeight;
		
		
	}

}
