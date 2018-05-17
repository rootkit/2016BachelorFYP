package com.weimanteam.weiman.fragment;

import java.util.ArrayList;

import android.R.integer;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.shizhefei.fragment.LazyFragment;
import com.weimanteam.weiman.R;
import com.weimanteam.weiman.adapter.FeatureImageAdapter;
import com.weimanteam.weiman.config.Global;

public class GridFeatureImageFragment extends LazyFragment {
	private ProgressBar progressBar;
	private TextView textView;
	private int tabIndex;
	public static final String INTENT_INT_INDEX = "intent_int_index";

	private GridView gv_featureimage;
	private ArrayList<Integer> mHoldFeatureArrayList;
	private WebViewfunction mWebViewfunction;
	@Override
	protected void onCreateViewLazy(Bundle savedInstanceState) {
		super.onCreateViewLazy(savedInstanceState);
		setContentView(R.layout.fragment_featureimage);
		tabIndex = getArguments().getInt(INTENT_INT_INDEX);
		
		init();
	}
	
	private void init() {
		mHoldFeatureArrayList = Global.showFeatureArrayList.get(tabIndex);
		
		gv_featureimage = (GridView) findViewById(R.id.gv_featureimage);
		gv_featureimage.setAdapter(new FeatureImageAdapter(getActivity(), mHoldFeatureArrayList, tabIndex));
		
		gv_featureimage.setOnItemClickListener(new GridItemClickedListener());
	}
	
	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
		
		if (activity instanceof WebViewfunction)
			mWebViewfunction = (WebViewfunction) activity;
	}

	@Override
	public void onDestroyViewLazy() {
		super.onDestroyViewLazy();
	}

	class GridItemClickedListener implements OnItemClickListener{
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			if (mWebViewfunction != null) {
				Global.lastClickedIndex[tabIndex] = arg2;
				if (tabIndex <= 5 ) {
					mWebViewfunction.faceFeatureScaleAndMove(tabIndex, 
							Global.sortedIndex.get(tabIndex)[arg2]);
				//当脸改变，也触发函数，重置发型、鼻子、嘴巴、眼睛、眉毛位置
					if (tabIndex == 1) {
						mWebViewfunction.faceFeatureScaleAndMove(0, 
								Global.sortedIndex.get(0)[Global.lastClickedIndex[0]]);
						mWebViewfunction.faceFeatureScaleAndMove(2, 
								Global.sortedIndex.get(2)[Global.lastClickedIndex[2]]);
						mWebViewfunction.faceFeatureScaleAndMove(3, 
								Global.sortedIndex.get(3)[Global.lastClickedIndex[3]]);
						mWebViewfunction.faceFeatureScaleAndMove(4, 
								Global.sortedIndex.get(4)[Global.lastClickedIndex[4]]);
						mWebViewfunction.faceFeatureScaleAndMove(5, 
								Global.sortedIndex.get(5)[Global.lastClickedIndex[5]]);
					}
				} else 
					mWebViewfunction.faceFeatureScaleAndMove(tabIndex, 
							arg2);
			}
		}
	}
	
	public interface WebViewfunction {
//		void faceFeatureChange(int type, int position);
		void faceFeatureScaleAndMove(int type, int position);
	}
	
}
