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
		mHoldFeatureArrayList = Global.mFeatrueArrayList.get(tabIndex);
		
		gv_featureimage = (GridView) findViewById(R.id.gv_featureimage);
		gv_featureimage.setAdapter(new FeatureImageAdapter(getActivity(), mHoldFeatureArrayList));
		
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
				mWebViewfunction.faceFeatureScaleAndMove(tabIndex, arg2);
			}
		}
		
	}
	
	public interface WebViewfunction {
//		void faceFeatureChange(int type, int position);
		void faceFeatureScaleAndMove(int type, int position);
	}
	
}
