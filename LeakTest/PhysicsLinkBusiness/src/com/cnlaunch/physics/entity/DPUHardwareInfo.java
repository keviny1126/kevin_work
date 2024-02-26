package com.cnlaunch.physics.entity;

import android.text.TextUtils;

public class DPUHardwareInfo {
	public static String ID="id";
	public static String SERIAL_NO="serialNo";
	public static String VERSION="version";
	public static String DATE="date";
	public static String DEVICE_TYPE="deviceType";
	
	
	/**DPU 唯一标识**/
	private String id;
	/**产品序列号**/
	private String serialNo;
	/**PCB版本号**/
	private String version;
	/**日期**/
	private String date;
	/**设备类型**/
	private String deviceType;
	
	public DPUHardwareInfo(){
		id = "";
		serialNo = "";
		version =  "";
		date =  "";
		deviceType =  "";
	}
	
	public DPUHardwareInfo(String[] info){
		if(info != null && info.length >= 5){
			id = info[0];
			serialNo = info[1];
			version = info[2];
			date = info[3];
			deviceType = info[4];
		}
		else{
			id = "";
			serialNo = "";
			version =  "";
			date =  "";
			deviceType =  "";
		}
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getSerialNo() {
		return serialNo;
	}
	public void setSerialNo(String serialNo) {
		this.serialNo = serialNo;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public String getDeviceType() {
		return deviceType;
	}
	public void setDeviceType(String deviceType) {
		this.deviceType = deviceType;
	}
	/**
	 * 判断设备是否有效
	 * @return
	 */
	public boolean isEmpty(){
		if(TextUtils.isEmpty(id) || TextUtils.isEmpty(serialNo) || TextUtils.isEmpty(version) || TextUtils.isEmpty(deviceType) || TextUtils.isEmpty(date)){
			return true;
		}
		return false;
	}

	/**
	 * 判断设备是否有效，去掉deviceType条件
	 * @return
	 */
	public boolean isEmptyWithoutDeviceType(){
		if(TextUtils.isEmpty(id) || TextUtils.isEmpty(serialNo) || TextUtils.isEmpty(version)  || TextUtils.isEmpty(date)){
			return true;
		}
		return false;
	}
	/**
	 * 用于判断诊断板是否只缺少序列号，半成品与成品区别就在于序列号
	 * xfh2017/02/22 加入
	 * @return
	 */
	public boolean isBlankTest(){
		if(TextUtils.isEmpty(serialNo) && (TextUtils.isEmpty(id)==false  ||  
				TextUtils.isEmpty(version)==false  || 
				TextUtils.isEmpty(deviceType)==false  || 
				TextUtils.isEmpty(date)==false )){
			return true;
		}
		return false;
	}
	
	@Override
	public String toString() {
		return "DPUHardwareInfo [id=" + id + ", serialNo=" + serialNo
				+ ", version=" + version + ", date=" + date + ", deviceType="
				+ deviceType + "]";
	}
	
}
