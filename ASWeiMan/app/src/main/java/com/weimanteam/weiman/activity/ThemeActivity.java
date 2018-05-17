package com.weimanteam.weiman.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.weimanteam.weiman.R;
import com.weimanteam.weiman.adapter.ThemeViewAdapter;
import com.weimanteam.weiman.bean.Photo;
import com.weimanteam.weiman.util.FileUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.sina.weibo.SinaWeibo;
import cn.sharesdk.tencent.qq.QQ;
import cn.sharesdk.wechat.friends.Wechat;
import cn.sharesdk.wechat.moments.WechatMoments;

public class ThemeActivity extends Activity implements PlatformActionListener{
	private GridView mListView;
	private ImageView ImageViewBack;
	private ThemeViewAdapter madapter;
	private ArrayList<Photo> mDataList = new ArrayList<Photo>();


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.themegridview_ui);
		mListView = (GridView) findViewById(R.id.gridView1);
		PlatformActionListener PAListener = this;
		madapter = new ThemeViewAdapter(this, mDataList,PAListener);
		mListView.setAdapter(madapter);
		initPhoto();
		ImageViewBack = (ImageView) findViewById(R.id.btnThemeBack);
		ImageViewBack.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ThemeActivity.this.finish();
			}
		});

	}
	
	private void initPhoto() {
		File file = FileUtil.getOutputMediaDirectory(this);
		if (file != null) {
			mDataList.clear();
			mDataList.addAll(FileUtil.getFileBitmaps(file));
		}
		madapter.notifyDataSetChanged();
	}
	
	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
    public void onCancel(Platform arg0, int arg1) {//回调的地方是子线程，进行UI操作要用handle处理
        handler.sendEmptyMessage(5);

    }

    @Override
    public void onComplete(Platform arg0, int arg1, HashMap<String, Object> arg2) {//回调的地方是子线程，进行UI操作要用handle处理
        if (arg0.getName().equals(SinaWeibo.NAME)) {// 判断成功的平台是不是新浪微博
            handler.sendEmptyMessage(1);
        } else if (arg0.getName().equals(Wechat.NAME)) {
            handler.sendEmptyMessage(1);
        } else if (arg0.getName().equals(WechatMoments.NAME)) {
            handler.sendEmptyMessage(3);
        } else if (arg0.getName().equals(QQ.NAME)) {
            handler.sendEmptyMessage(4);
        }

    }

    @Override
    public void onError(Platform arg0, int arg1, Throwable arg2) {//回调的地方是子线程，进行UI操作要用handle处理
        arg2.printStackTrace();
        Message msg = new Message();
        msg.what = 6;
        msg.obj = arg2.getMessage();
        handler.sendMessage(msg);
    }

    Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    Toast.makeText(getApplicationContext(), "微博分享成功", Toast.LENGTH_LONG).show();
                    break;

                case 2:
                    Toast.makeText(getApplicationContext(), "微信分享成功", Toast.LENGTH_LONG).show();
                    break;
                case 3:
                    Toast.makeText(getApplicationContext(), "朋友圈分享成功", Toast.LENGTH_LONG).show();
                    break;
                case 4:
                    Toast.makeText(getApplicationContext(), "QQ分享成功", Toast.LENGTH_LONG).show();
                    break;

                case 5:
                    Toast.makeText(getApplicationContext(), "取消分享", Toast.LENGTH_LONG).show();
                    break;
                case 6:
                    Toast.makeText(getApplicationContext(), "分享失败啊" + msg.obj, Toast.LENGTH_LONG).show();
                    break;

                default:
                    break;
            }
        }

    };
	

}
