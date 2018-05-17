package com.weimanteam.weiman.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.weimanteam.weiman.R;

import com.weimanteam.weiman.adapter.ThemeListviewAdapter;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ThemeActivity extends Activity{
	private ListView mListView;
	
	private Button btnThemeBack;
	private ArrayList<Map<String, String>> mDataList = new ArrayList<Map<String, String>>();
	//数据初始化
	private void setData(){
		Map<String,String> mMap = new HashMap<String, String>();
		mMap.put("name", "春节拜年");
		mMap.put("description", "2016春节拜年最潮表情");
		mMap.put("price", "免费");
		mDataList.add(mMap);

		mMap = new HashMap<String, String>();
		mMap.put("name", "自驾游");
		mMap.put("description", "自驾游，最酷表情组合");
		mMap.put("price", "￥");
		mDataList.add(mMap);
		
		mMap = new HashMap<String, String>();
		mMap.put("name", "欧洲环游");
		mMap.put("description", "最全欧洲各国风情服饰全套");
		mMap.put("price", "￥");
		mDataList.add(mMap);
		
		mMap = new HashMap<String, String>();
		mMap.put("name", "毕业季");
		mMap.put("description", "校园记忆的最美时光");
		mMap.put("price", "￥");
		mDataList.add(mMap);
		
	}
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.themelistview_ui);
		
		setData();
    
    final ThemeListviewAdapter madapter = new ThemeListviewAdapter(this,mDataList);
    
    mListView = (ListView)findViewById(R.id.listView1);
    mListView.setAdapter(madapter);
    madapter.notifyDataSetChanged();
    
    mListView.setOnItemClickListener(new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			// TODO Auto-generated method stub
			String str = ((TextView)view.findViewById(R.id.name)).getText().toString() + " "
					+ ((TextView)view.findViewById(R.id.description)).getText().toString() + " "
					+ ((TextView)view.findViewById(R.id.price)).getText().toString();
			
			Toast.makeText(ThemeActivity.this,str, Toast.LENGTH_LONG).show();
			
		}  
    }); 
    
    btnThemeBack = (Button)findViewById(R.id.btnThemeBack);
    btnThemeBack.setOnClickListener(new OnClickListener(){

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
//			Bundle mBundle = new Bundle();
//			Intent mIntent = new Intent();
//			mIntent.setClass(ThemeActivity.this, MainActivity.class);
//			mIntent.putExtras(mBundle);
//			startActivity(mIntent);
			ThemeActivity.this.finish();
			
		}
    	
    });
    
    }
	
	
	

	
}
