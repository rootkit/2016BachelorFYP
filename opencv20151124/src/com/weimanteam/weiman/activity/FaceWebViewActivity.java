package com.weimanteam.weiman.activity;

import java.util.ArrayList;

import org.opencv.core.Point;
import org.opencv.core.Rect;

import com.shizhefei.view.indicator.FragmentListPageAdapter;
import com.shizhefei.view.indicator.IndicatorViewPager;
import com.shizhefei.view.indicator.ScrollIndicatorView;
import com.shizhefei.view.indicator.IndicatorViewPager.IndicatorFragmentPagerAdapter;
import com.shizhefei.view.indicator.slidebar.ColorBar;
import com.shizhefei.view.indicator.transition.OnTransitionTextListener;
import com.weimanteam.weiman.R;
import com.weimanteam.weiman.activity.CameraUIActivity;
import com.weimanteam.weiman.bean.FacialFeaturesRect;
import com.weimanteam.weiman.config.Global;
import com.weimanteam.weiman.fragment.GridFeatureImageFragment;
import com.weimanteam.weiman.fragment.MoreFragment;
import com.weimanteam.weiman.fragment.GridFeatureImageFragment.WebViewfunction;
import com.weimanteam.weiman.model.FacialFeaturesScaleAndMoveModel;
import com.weimanteam.weiman.util.FacialFeaturesPointUtil;
import com.weimanteam.weiman.util.FileUtil;


import android.os.Bundle;
import android.os.Parcelable;
import android.R.integer;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.LocalActivityManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TabHost.OnTabChangeListener;

@SuppressLint("SetJavaScriptEnabled") 
public class FaceWebViewActivity extends FragmentActivity implements WebViewfunction{
	private WebView webView;
	private RelativeLayout relativelayoutWebview;
	private RelativeLayout relativelayoutOp;
	private IndicatorViewPager indicatorViewPager;
	private LayoutInflater inflate;
	private String[] names = {"发型", "脸型", "眼睛", "眉毛", "鼻子","嘴巴", "耳朵", "装饰"};
	private int size = names.length;
	private ScrollIndicatorView indicator;
	private Button btnback, btnShare;
	private static final String TAG = "activity.FaceWebViewActivity";
	private double faceRatio = 0;
	private int mistakeX = -58;//系统误差，为了弥补JS返回的计算结果向一个方向偏移的问题
	private int mistakeY = -35;//系统误差
	//以下两个数值表示当前用户点击的类型和数据，用于第一次和第二次与webView的交互的缓存。主要是为了异步线程的回调。
	private int currentType = 0;
	private int currentPosition = 0;
	@SuppressLint("JavascriptInterface") @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		
		btnback = (Button)findViewById(R.id.btnBack);
		btnback.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				FaceWebViewActivity.this.finish();
			}
		});
		
		btnShare = (Button)findViewById(R.id.btnShare);
		btnShare.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
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
		webView.loadUrl("file:///android_asset/test1.html");
	}
	
	class JavaScriptInterface {
		Context context;
		public JavaScriptInterface(Context activity){
			this.context = activity;
		}
		
		@JavascriptInterface
		public void setRect(double top, double bottom, double left, double right, double scrollLeft) {
			Log.i(TAG, "top: " + top + "  bottom: " + bottom + " left: " + left + " right: " + right);
			Toast.makeText(context, "top: " + top + "  bottom: " + bottom + " left: " + left + " right: " + right + " scrollLeft: " + scrollLeft, Toast.LENGTH_SHORT).show();
			
			FacialFeaturesRect svgRect = new FacialFeaturesRect((int)left, (int)top, (int)(right - left), (int)(bottom - top));
			Log.i(TAG, "svgRect: " + svgRect);
			FacialFeaturesRect targetRect = getScaledAndMovedRectByType(currentType);
			FacialFeaturesScaleAndMoveModel facialModel;
			
			double faceRatioX = (double)targetRect.width / svgRect.width;
			Log.i(TAG, "Xratio: " + faceRatioX);
			double faceRatioY = (double)targetRect.height / svgRect.height;
			Log.i(TAG, "Yratio: " + faceRatioY);
			faceRatio = faceRatioX < faceRatioY ? faceRatioX :faceRatioY;
			//!!!
			faceRatio = 0.028;
			//!!!
			facialModel = new FacialFeaturesScaleAndMoveModel(faceRatio, targetRect.getCenterPoint());
			
			FacialFeaturesRect scaledAndMoveedsvgEyeRect = (FacialFeaturesRect) facialModel.scaleAndMoveRect2(svgRect);
			Log.i(TAG, "targetRect: " + targetRect);
			Log.i(TAG, "scaledAndMoveedsvgEyeRect: " + scaledAndMoveedsvgEyeRect);
			Log.i(TAG, "ratio: " + facialModel.ratio);
			Log.i(TAG, "deltaX: " + facialModel.deltaX);
			Log.i(TAG, "deltaY:" + facialModel.deltaY);
			
			SetRectRunnable r = new SetRectRunnable(facialModel.deltaX + mistakeX, facialModel.deltaY + mistakeY, facialModel.ratio);
			FaceWebViewActivity.this.runOnUiThread(r);
		}
		
	}
	
	class SetRectRunnable implements Runnable {
		int x;
		int y;
		double r;
		public SetRectRunnable(int x, int y , double r) {
			this.x = x;
			this.y = y;
			this.r = r;
		}
		
		@Override
		public void run() {
			webView.loadUrl("javascript:facialConfigChange(" + currentType+ "," + currentPosition + ","+ x + "," + y + "," + r + "," + 2 +  ")");
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
			if (position < 6) {
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
	
//	private String getFun(int id) {
//		switch (id) {
//		case 0:
//			return "javascript:hairChange(";
//		case 1:
//			return "javascript:faceChange(";
//		case 2:
//			return "javascript:eyeChange(";
//		case 3:
//			return "javascript:eyebrowChange(";
//		case 4:
//			return "javascript:noseChange(";
//		case 5:
//			return "javascript:mouthChange(";
//		default:
//			break;
//		}
//		return null;
//	}

//	@Override
//	public void faceFeatureChange(int type, int position) {
//		String mTypeString = getFun(type);
//		//防止webView中的html崩溃
//		if (mTypeString == null) 
//			return;
//		webView.loadUrl(mTypeString + position + ")");
//	}

	@Override
	public void faceFeatureScaleAndMove(int type, int position) {
		//第一步，设置未缩放的svg图像在webView，是的，非常大的数值，所以不会显示在640*640的浏览器窗口里。但是我们可以通过过
		//Js代码知道它的Rect
		webView.loadUrl("javascript:facialConfigChange(" + type+ "," + position + ","+ 0 + "," + 0 + "," + 1.0 + "," + 0 + ")");
		currentType = type;
		currentPosition = position;
		
		ArrayList<ArrayList<Point>> pointsList = getPointListByType(type);
		if (pointsList == null) return;
//		Log.i(TAG, "length: " + pointsList.size() + "  type: " + type + " position: " + position);
//		Log.i(TAG, "pointsList: " + pointsList.get(position));
		FacialFeaturesRect svgEyeRect = FacialFeaturesPointUtil.getRect(pointsList.get(position));//0表示svg中的eye0
		Log.i(TAG, "svg Rect： " + svgEyeRect);
//		FacialFeaturesRect targetRect = getScaledAndMovedRectByType(type);
//		Log.i(TAG, "target Rect： " + targetRect);
//		FacialFeaturesScaleAndMoveModel eyeModel;
//		
////		faceRatio  = 0.027;
//		if (type == 1) {
//			faceRatio = (double)targetRect.width / svgEyeRect.width;
//			eyeModel = new FacialFeaturesScaleAndMoveModel(faceRatio, targetRect.getCenterPoint());
//		} else {
//			faceRatio = (double)targetRect.width / svgEyeRect.width;
//			eyeModel = new FacialFeaturesScaleAndMoveModel(faceRatio, targetRect.getCenterPoint());
//		}
//		
//		FacialFeaturesRect scaledAndMoveedsvgEyeRect = (FacialFeaturesRect) eyeModel.scaleAndMoveRect2(svgEyeRect);
//		Log.i(TAG, "ratio: " + eyeModel.ratio);
//		Log.i(TAG, "deltaX: " + eyeModel.deltaX);
//		Log.i(TAG, "deltaY:" + eyeModel.deltaY);
//		Log.i(TAG, scaledAndMoveedsvgEyeRect + "");
//		
//		webView.loadUrl("javascript:facialConfigChange(" + type+ "," + position + ","+ eyeModel.deltaX + "," + eyeModel.deltaY + "," + eyeModel.ratio + ")");
	}
	
	private ArrayList<ArrayList<Point>> getPointListByType(int type) {
		switch (type) {
		case 0:
			return null;
		case 1:
			return Global.facePointsList;
		case 2:
			return Global.eyePointsList;
		case 3:
			return Global.eyebrowPointsList;
		case 4:
			return Global.nosePointsList;
		case 5:
			return Global.mouthPointsList;
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
		default:
			break;
		}
		return null;
	}
	
//	private Object getHtmlObject(){
//		Object insertObj = new Object(){
//			@JavascriptInterface
//			public String HtmlcallJava(){
//				return "Html call Java";
//			}
//			@JavascriptInterface
//			public String HtmlcallJava2(final String param){
//				return "Html call Java : " + param;
//			}
//			@JavascriptInterface
//			public void JavacallHtml(){
//				runOnUiThread(new Runnable() {
//					@Override
//					public void run() {
//						webView.loadUrl("javascript: showFromHtml()");
//						Toast.makeText(FaceWebViewActivity.this, "clickBtn", Toast.LENGTH_SHORT).show();
//					}
//				});
//			}
//			@JavascriptInterface
//			public void JavacallHtml2(){
//				runOnUiThread(new Runnable() {
//					@Override
//					public void run() {
//						webView.loadUrl("javascript: showFromHtml2('IT-homer blog')");
//						Toast.makeText(FaceWebViewActivity.this, "clickBtn2", Toast.LENGTH_SHORT).show();
//					}
//				});
//			}
//		};
//		
//		return insertObj;
//	}
}
