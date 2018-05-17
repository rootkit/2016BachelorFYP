package com.weimanteam.weiman.bean;

import android.graphics.Bitmap;

public class Photo implements Comparable<Photo> {
	private Bitmap bitmap;
	private String name;
	private String path;
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public Bitmap getBitmap() {
		return bitmap;
	}
	public void setBitmap(Bitmap bitmap) {
		this.bitmap = bitmap;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	@Override
	public int compareTo(Photo another) {
		// TODO Auto-generated method stub
		return name.compareTo(another.getName());
	}
	
	
}