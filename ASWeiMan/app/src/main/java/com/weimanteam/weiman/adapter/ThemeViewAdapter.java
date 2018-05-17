package com.weimanteam.weiman.adapter;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.framework.Platform.ShareParams;
import cn.sharesdk.sina.weibo.SinaWeibo;
import cn.sharesdk.tencent.qq.QQ;
import cn.sharesdk.wechat.friends.Wechat;
import cn.sharesdk.wechat.moments.WechatMoments;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.weimanteam.weiman.R;
import com.weimanteam.weiman.activity.ThemeActivity;
import com.weimanteam.weiman.bean.Photo;
import com.weimanteam.weiman.util.FileUtil;
import com.weimanteam.weiman.util.ShareDialogUtil;

import android.content.Context;
import android.util.Log;
import android.util.MonthDisplayHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class ThemeViewAdapter extends BaseAdapter{

	private LayoutInflater mInflater;//得到一个LayoutInfalter对象用来导入布局
	public ArrayList<Photo> mDataList;
	private Context context;
	private PlatformActionListener PAListener;
	public final class ViewHolder{
		//public ImageButton ibtn;
		public TextView name;
		public TextView price;
		public ImageView photo;
		public FloatingActionButton share;
		public FloatingActionButton delete;
	};

	public ThemeViewAdapter(Context context,ArrayList<Photo> data,PlatformActionListener PAListener) {
		super();
		ShareSDK.initSDK(context);
		this.mInflater = LayoutInflater.from(context);
		this.context = context;
		this.PAListener = PAListener;
		mDataList = data;
	}
	
	@Override
	public int getCount() {
		return getDate().size();//返回数组的长度
	}
	
	private ArrayList<Photo> getDate(){
		return mDataList;
	}
	
	@Override
	public Object getItem(int position) {
		return getDate().get(position);
	}
	
	@Override
	public long getItemId(int position) {
		return position;
	}
	public View getView(final int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;
		//观察convertView随ListView滚动情况
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.themegridview_item,null);
			holder = new ViewHolder();
			holder.name = (TextView) convertView.findViewById(R.id.name);
			holder.price = (TextView) convertView.findViewById(R.id.price);
			holder.photo = (ImageView) convertView.findViewById(R.id.picture);
			holder.share = (FloatingActionButton) convertView.findViewById(R.id.FloatingBtn_share);
			holder.delete = (FloatingActionButton) convertView.findViewById(R.id.FloatingBtn_del);
			//holder.ibtn = (ImageButton)convertView.findViewById(R.id.imageButtonHead);
			convertView.setTag(holder);//绑定ViewHolder对象
			}else{
				holder = (ViewHolder)convertView.getTag();//取出ViewHolder对象
			}
		
		holder.name.setText(filter(getDate().get(position).getName()));
		holder.photo.setImageBitmap(getDate().get(position).getBitmap());
		holder.delete.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				
				Photo photoTemp = mDataList.get(position);
				mDataList.remove(position);
				File f = new File(photoTemp.getPath());
				f.delete();
				Toast.makeText(context, "图片已删除", Toast.LENGTH_SHORT).show();
				notifyDataSetChanged();
			}
		});
		holder.share.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				final String photoPath = mDataList.get(position).getPath();
				final String title = "我的微漫头像";
				final String text = "恭喜发财，大吉大利！";
				Toast.makeText(context, "分享图片",
						Toast.LENGTH_SHORT).show();
				
				final ShareDialogUtil shareDialog;
				shareDialog = new ShareDialogUtil(context);
				shareDialog.setCancelButtonOnClickListener(new View.OnClickListener() {
		                @Override
		                public void onClick(View v) {
		                    shareDialog.dismiss();
		                }
		            });
		            shareDialog.setOnItemClickListener(new AdapterView.OnItemClickListener() {
		                @Override
		                public void onItemClick(AdapterView<?> arg0, View arg1,
		                                        int arg2, long arg3) {
		                    HashMap<String, Object> item = (HashMap<String, Object>) arg0.getItemAtPosition(arg2);
		                    if (item.get("ItemText").equals("微博")) {
		                        //2、设置分享内容
		                        ShareParams sp = new ShareParams();
		                        sp.setText(text + "  http://www.baidu.com/"); //分享文本
		                        sp.setImagePath(photoPath);
		                        //3、非常重要：获取平台对象
		                        Platform sinaWeibo = ShareSDK.getPlatform(SinaWeibo.NAME);
		                        sinaWeibo.setPlatformActionListener(PAListener); // 设置分享事件回调
		                        // 执行分享
		                        sinaWeibo.share(sp);
		                    } else if (item.get("ItemText").equals("微信好友")) {
		                        Toast.makeText(context, "您点中了" + item.get("ItemText"), Toast.LENGTH_LONG).show();

		                        //2、设置分享内容
		                        ShareParams sp = new ShareParams();
		                        sp.setShareType(Platform.SHARE_IMAGE);//非常重要：一定要设置分享属性
		                        sp.setTitle(title);  //分享标题
		                        sp.setText(text + "  http://www.baidu.com/");   //分享文本
		                        sp.setImagePath(photoPath);
		                        sp.setUrl("http://www.baidu.com");   //网友点进链接后，可以看到分享的详情

		                        //3、非常重要：获取平台对象
		                        Platform wechat = ShareSDK.getPlatform(Wechat.NAME);
		                        wechat.setPlatformActionListener(PAListener); // 设置分享事件回调
		                        // 执行分享
		                        wechat.share(sp);


		                    } else if (item.get("ItemText").equals("朋友圈")) {
		                        //2、设置分享内容
		                        ShareParams sp = new ShareParams();
		                        sp.setShareType(Platform.SHARE_IMAGE); //非常重要：一定要设置分享属性
		                        sp.setTitle(title);  //分享标题
		                        sp.setText(text + "  http://www.baidu.com/");   //分享文本
		                        sp.setImagePath(photoPath);
		                        sp.setUrl("http://www.baidu.com");   //网友点进链接后，可以看到分享的详情

		                        //3、非常重要：获取平台对象
		                        Platform wechatMoments = ShareSDK.getPlatform(WechatMoments.NAME);
		                        wechatMoments.setPlatformActionListener(PAListener); // 设置分享事件回调
		                        // 执行分享
		                        wechatMoments.share(sp);

		                    } else if (item.get("ItemText").equals("QQ")) {
		                        //2、设置分享内容
		                        ShareParams sp = new ShareParams();
		                        sp.setTitle(title);
		                        sp.setText(text + "  http://www.baidu.com/");
		                        sp.setImagePath(photoPath);

		                        sp.setUrl("http://www.baidu.com");
		                        sp.setTitleUrl("http://www.baidu.com");  //网友点进链接后，可以看到分享的详情
		                        //3、非常重要：获取平台对象
		                        Platform qq = ShareSDK.getPlatform(QQ.NAME);
		                        
		                        
		                        qq.setPlatformActionListener(PAListener); // 设置分享事件回调
		                        
		                        // 执行分享
		                        qq.share(sp);
		                        Log.e("22","here!!!!!");
		                    }

		                    shareDialog.dismiss();

		                }
		            });
			}
		});
//		holder.photo.setImageBitmap(com.weimanteam.weiman.util.FileUtil.getLoacalBitmap(new File("/storage/emulated/0/Pictures/WeiMan/20151223212405.jpg")));

//		holder.price.setText(getDate().get(position).get("price").toString());
		
		
		/*
		holder.ibtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String name = holder.name.getText().toString();
        		String classs = holder.classs.getText().toString();
        		Map<String,String> mMap = new HashMap<String, String>();
        		mMap.put("name", name);
        		mMap.put("class", classs);
        		mDataList.remove(mMap);
        		notifyDataSetChanged();
				}
			});
		*/
		return convertView;
		}
	
	
	private String filter(String name) {
		String month = name.substring(4, 6);
		String day = name.substring(6, 8);
		String hour = name.substring(8, 10);
		return month + "月" + day + "日" + hour + "时";
	}
	
	

}
