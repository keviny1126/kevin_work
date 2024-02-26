package com.cnlaunch.physics.utils;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;

import com.cnlaunch.physics.bluetooth.BluetoothManager;
import com.cnlaunch.physics.downloadbin.util.Analysis;
import com.cnlaunch.physics.downloadbin.util.OrderMontage;
import com.cnlaunch.physics.entity.AnalysisData;
import com.cnlaunch.physics.entity.DPUHardwareInfo;
import com.cnlaunch.physics.entity.DPUSoftInfo;
import com.cnlaunch.physics.impl.IPhysics;

public class DeviceUtils {
	
	private static DeviceUtils deviceUtils;
	private String mSerialNo = "";
	private boolean update103Device = false;
	
	private DeviceUtils(){
		
	}
	
	/**
	 * 
	 * @param context
	 * @return
	 */
	public static DeviceUtils getInstance(){
		if(deviceUtils == null){
			deviceUtils = new DeviceUtils();
		}
		
		return deviceUtils;
	}
	
	
	
	/**
	 * 判断本地是否存储有DPU接头硬件版本信息
	 * @param serialNo
	 * @return
	 */
	public boolean hasDPUHardwareInfo(String serialNo){
		return DeviceProperties.getInstance(serialNo).hasHardwareInfo();
	}
	
	/**
	 * 判断本地是否存储有DPU接头软件版本信息
	 * @param serialNo
	 * @return
	 */
	public boolean hasDPUSoftInfo(String serialNo){
		return DeviceProperties.getInstance(serialNo).hasSoftInfo();
	}
	
	
	
	
	
	/**
	 * 从本地获取DPU硬件版本信息
	 * @param serialNo 序列号
	 * @return DPU硬件版本信息对象
	 */
	public DPUHardwareInfo getDPUHardwareInfo(String serialNo){
		return  DeviceProperties.getInstance(serialNo).getDPUHardwareInfo();
	}
	/**
	 * 从本地获取DPU硬件版本信息
	 * @param serialNo 序列号
	 * @param baseDir 读取文件目录路径
	 * @return DPU硬件版本信息对象
	 */
	public DPUHardwareInfo getDPUHardwareInfo(String serialNo,String baseDir){
		return  DeviceProperties.getInstance(serialNo,baseDir).getDPUHardwareInfo();
	}
	
	
	/**
	 * 保存DPU接头硬件版本信息
	 * @param serialNo 序列号
	 * @param dpInfo DPU硬件版本信息对象
	 */
	public void saveDPUHardwareInfo(String serialNo,DPUHardwareInfo dpInfo){
		saveDPUHardwareInfo(serialNo, null, dpInfo);
	}
	public void saveDPUHardwareInfo(String serialNo,String[] info){
		saveDPUHardwareInfo(serialNo, null, info);
	}
	public void saveDPUHardwareInfo(String serialNo,String baseDir,String[] info){
		DPUHardwareInfo dpInfo = new DPUHardwareInfo(info);
		saveDPUHardwareInfo(serialNo, baseDir, dpInfo);
	}
	/**
	 * 保存DPU接头硬件版本信息
	 * @param serialNo 序列号
	 * @param baseDir 保存接头信息的文件目录路径
	 * @param dpInfo DPU硬件版本信息对象
	 */
	public void saveDPUHardwareInfo(String serialNo,String baseDir,DPUHardwareInfo	 dpInfo){
		DeviceProperties.getInstance(serialNo, baseDir).saveDPUHardwareInfo(dpInfo);
	}
	
	
	/**
	 * 从本地获取DPU接头软件版本信息
	 * @param serialNo 序列号
	 * @return DPU软件版本信息对象
	 */
	public DPUSoftInfo getDPUSoftInfo(String serialNo){
		return DeviceProperties.getInstance(serialNo).getDPUSoftInfo();
	}
	/**
	 * 从本地获取DPU接头软件版本信息
	 * @param serialNo 序列号
	 * @param baseDir 读取文件目录路径
	 * @return DPU软件版本信息对象
	 */
	public DPUSoftInfo getDPUSoftInfo(String serialNo,String baseDir){
		return DeviceProperties.getInstance(serialNo,baseDir).getDPUSoftInfo();
	}
	
	
	/**
	 * 保存DPU接头软件版本信息
	 * @param serialNo 序列号
	 * @param dpInfo DPU软件版本信息对象
	 */
	public void saveDPUSoftInfo(String serialNo,DPUSoftInfo dpInfo){
		saveDPUSoftInfo(serialNo, null, dpInfo);
	}
	public void saveDPUSoftInfo(String serialNo,String[] info){
		saveDPUSoftInfo(serialNo, null, info);
	}
	public void saveDPUSoftInfo(String serialNo,ArrayList<String> info){
		saveDPUSoftInfo(serialNo, null, info);
	}
	public void saveDPUSoftInfo(String serialNo,String baseDir,String[] info){
		DPUSoftInfo dsInfo = new DPUSoftInfo(info);
		saveDPUSoftInfo(serialNo, baseDir, dsInfo);
	}
	public void saveDPUSoftInfo(String serialNo,String baseDir,ArrayList<String> info){
		DPUSoftInfo dsInfo = new DPUSoftInfo(info);
		saveDPUSoftInfo(serialNo, baseDir, dsInfo);
	}
	/**
	 * 保存DPU接头软件版本信息
	 * @param serialNo 序列号
	 * @param baseDir 保存接头信息文件目录路径
	 * @param dpInfo DPU软件版本信息对象
	 */
	public void saveDPUSoftInfo(String serialNo,String baseDir,DPUSoftInfo	 dpInfo){
		DeviceProperties.getInstance(serialNo, baseDir).saveDPUSoftInfo(dpInfo);
	}
	
	/**
	 * 获取接头激活时间
	 * @param serialNo 序列号
	 * @return 激活时间
	 */
	public String getActivateTime(String serialNo){
		return getActivateTime(serialNo, null);
	}
	
	/**
	 * 获取接头激活时间
	 * @param serialNo 序列号
	 * @param baseDir 获取接头信息的文件目录路径
	 * @return 激活时间
	 */
	public String getActivateTime(String serialNo,String baseDir){
		return DeviceProperties.getInstance(serialNo,baseDir).getActivateTime();
	}
	
	/**
	 * 保存接头激活时间
	 * @param serialNo 序列号
	 * @param activateTime 激活时间
	 */
	public void saveActivateTime(String serialNo,String activateTime){
		saveActivateTime(serialNo, null, activateTime);
	}
	/**
	 * 保存接头激活时间
	 * @param serialNo 序列号
	 * @param baseDir 保存路径
	 * @param activateTime 激活时间
	 */
	public void saveActivateTime(String serialNo,String baseDir,String activateTime){
		DeviceProperties.getInstance(serialNo,baseDir).saveActivateTime(activateTime);
	}
	
	/**
	 * 保存Download版本号
	 * @param serialNo 序列号
	 * @param downloadVersion download版本
	 */
	public void saveDownloadVersion(String serialNo,String downloadVersion){
		saveDownloadVersion(serialNo, null, downloadVersion);
	}
	/**
	 * 保存Download版本号
	 * @param serialNo 序列号
	 * @param baseDir 存储路径
	 * @param downloadVersion download版本
	 */
	public void saveDownloadVersion(String serialNo,String baseDir,String downloadVersion){
		DeviceProperties.getInstance(serialNo, baseDir).saveDownloadVersion(downloadVersion);
	}
	/**
	 * 获取download版本
	 * @param serialNo 序列号
	 * @return download版本
	 */
	public String getDownloadVersion(String serialNo){
		return getDownloadVersion(serialNo, null);
	}
	
	/**
	 * 获取download版本
	 * @param serialNo 序列号
	 * @param baseDir 获取接头信息的文件目录路径
	 * @return download版本
	 */
	public String getDownloadVersion(String serialNo,String baseDir){
		return DeviceProperties.getInstance(serialNo,baseDir).getDownloadVersion();
	}
	/**
	 * 保存硬件版本号
	 * @param serialNo 序列号
	 * @param hardwareVersion 硬件版本
	 */
	public void saveHardwareVersion(String serialNo,String hardwareVersion){
		saveHardwareVersion(serialNo, null, hardwareVersion);
	}
	/**
	 * 保存硬件版本号
	 * @param serialNo 序列号
	 * @param baseDir 存储路径
	 * @param hardwareVersion 硬件版本
	 */
	public void saveHardwareVersion(String serialNo,String baseDir,String hardwareVersion){
		DeviceProperties.getInstance(serialNo, baseDir).saveHardwareVersion(hardwareVersion);
	}
	/**
	 * 获取序列号状态，已禁止，待禁止
	 * @param serialNo 序列号
	 * @return 已禁止，待禁止
	 */
	public String getSerialState(String serialNo){
		return getSerialState(serialNo, null);
	}

	/**
	 * 获取序列号状态，已停用，未停用
	 * @param serialNo 序列号
	 * @param baseDir 获取接头信息的文件目录路径
	 * @return 已禁止，待禁止
	 */
	public String getSerialStopState(String serialNo,String baseDir){
		return DeviceProperties.getInstance(serialNo,baseDir).getSerialNoStopState();
	}
	/**
	 * 获取序列号状态，已停用，未停用
	 * @param serialNo 序列号
	 * @return 已禁止，待禁止
	 */
	public String getSerialStopState(String serialNo){
		return getSerialStopState(serialNo, null);
	}

	/**
	 * 获取序列号状态，已禁止，待禁止
	 * @param serialNo 序列号
	 * @param baseDir 获取接头信息的文件目录路径
	 * @return 已禁止，待禁止
	 */
	public String getSerialState(String serialNo,String baseDir){
		return DeviceProperties.getInstance(serialNo,baseDir).getSerialState();
	}

	/**
	 * 保存接头状态
	 * @param serialNo 序列号
	 * @param state 激活时间
	 */
	public void saveSerialState(String serialNo,String state){
		saveSerialState(serialNo, null, state);
	}
	/**
	 * 保存接头状态
	 * @param serialNo 序列号
	 * @param baseDir 保存路径
	 * @param state 激活时间
	 */
	public void saveSerialStopState(String serialNo,String baseDir,String state){
		DeviceProperties.getInstance(serialNo,baseDir).saveSerialNoStopState(state);
	}
	/**
	 * 保存接头状态
	 * @param serialNo 序列号
	 * @param state 激活时间
	 */
	public void saveSerialStopState(String serialNo,String state){
		saveSerialStopState(serialNo, null, state);
	}
	/**
	 * 保存接头状态
	 * @param serialNo 序列号
	 * @param baseDir 保存路径
	 * @param state 激活时间
	 */
	public void saveSerialState(String serialNo,String baseDir,String state){
		DeviceProperties.getInstance(serialNo,baseDir).saveSerialState(state);
	}
	/**
	 * 保存蓝牙连接的序列号
	 * @param serialNo 序列号	 
	 */
	public void saveLinkSerialNo(String serialNo){
		mSerialNo = serialNo ;
	}
	/**
	 * 获取蓝牙连接的序列号 
	 */
	public String getLinkSerialNo(){
		return mSerialNo;
	}

	public boolean getUpdate103Device() {
		return update103Device;
	}

	public void setUpdate103Device(boolean update103Device) {
		this.update103Device = update103Device;
	}
}
