package com.weimanteam.weiman.model;

import java.io.Serializable;

public class UserModel implements Serializable
{
	private int age;
	private int sex;
	private boolean IsSetAge;
	private boolean IsSetSex;
	private static class SingletonHolder{
		static final UserModel INSTANCE = new UserModel();
	}
	
	public static UserModel getInstance(){
		return SingletonHolder.INSTANCE;
	}
	
	//用于判断做ASM五官定位之前用户是否设置好年龄与性别
	private UserModel(){
		IsSetAge = false;
		IsSetSex = false;
	}
	
	public boolean getIsSetAge(){
		return IsSetAge;
	}
	
	public boolean getIsSetSex(){
		return IsSetSex;
	}
	
	private Object readResolve() {
		return getInstance();
	}
	
	public int getAge() {
		return this.age;
	}
	
	public void setAge(int age) {
		this.age = age;
		IsSetAge = true;
	}
	
	public int getSex() {
		return this.sex;
	}
	
	public void setSex(int sex) {
		this.sex = sex;
		IsSetSex = true;
	}
}
