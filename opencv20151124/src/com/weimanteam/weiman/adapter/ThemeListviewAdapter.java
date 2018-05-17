package com.weimanteam.weiman.adapter;

import java.util.ArrayList;
import java.util.Map;

import com.weimanteam.weiman.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ThemeListviewAdapter extends BaseAdapter{

	private LayoutInflater mInflater;//得到一个LayoutInfalter对象用来导入布局
	public ArrayList<Map<String,String>> mDataList;
	public final class ViewHolder{
		//public ImageButton ibtn;
		public TextView name;
		public TextView description;
		public TextView price;
	};

	public ThemeListviewAdapter(Context context,ArrayList<Map<String, String>> data) {
		super();
		this.mInflater = LayoutInflater.from(context);
		mDataList = data;
		}
	@Override
	public int getCount() {
		return getDate().size();//返回数组的长度
		}
	
	
	private ArrayList<Map<String, String>> getDate(){
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
			convertView = mInflater.inflate(R.layout.listview_item,null);
			holder = new ViewHolder();
			holder.name = (TextView) convertView.findViewById(R.id.name);
			holder.description = (TextView) convertView.findViewById(R.id.description);
			holder.price = (TextView) convertView.findViewById(R.id.price);
			//holder.ibtn = (ImageButton)convertView.findViewById(R.id.imageButtonHead);
			convertView.setTag(holder);//绑定ViewHolder对象
			}else{
				holder = (ViewHolder)convertView.getTag();//取出ViewHolder对象
				}
		holder.name.setText(getDate().get(position).get("name").toString());
		holder.description.setText(getDate().get(position).get("description").toString());
		holder.price.setText(getDate().get(position).get("price").toString());
		
		
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
	
	
	

}
