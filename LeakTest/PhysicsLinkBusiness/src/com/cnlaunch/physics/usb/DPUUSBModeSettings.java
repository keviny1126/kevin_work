package com.cnlaunch.physics.usb;
import com.cnlaunch.physics.DPUDeviceType;
import com.cnlaunch.physics.impl.IPhysics;
import com.cnlaunch.physics.listener.OnUSBModeListener;

public class DPUUSBModeSettings {
	private final static String  TAG = "DPUUSBModeSettings";
	private  OnUSBModeListener mOnUSBModeListener;
	private IPhysics  mIPhysics;
	private  int mDPUDeviceType;
	public DPUUSBModeSettings(IPhysics  iPhysics,OnUSBModeListener onUSBModeListener){
		this(iPhysics,onUSBModeListener,DPUDeviceType.STANDARD);
	}
	public DPUUSBModeSettings(IPhysics  iPhysics,OnUSBModeListener onUSBModeListener,int dpuDeviceType){
		mOnUSBModeListener = onUSBModeListener;
		mIPhysics = iPhysics;
		mDPUDeviceType = dpuDeviceType;
	}
	/**
	 * 异步设置USB工作模式
	 */
	public void setDPUUSBModeAsync(int workMode){
		if(mDPUDeviceType == DPUDeviceType.SMARTBOX30){
			Smartbox30DPUUSBModeSettings smartbox30DPUUSBModeSettings =  new Smartbox30DPUUSBModeSettings();
			smartbox30DPUUSBModeSettings.setDPUUSBModeAsync(mIPhysics,mOnUSBModeListener,workMode);
		}
		else{
			StandardDPUUSBModeSettings standardDPUUSBModeSettings = new StandardDPUUSBModeSettings();
			standardDPUUSBModeSettings.setDPUUSBModeAsync(mIPhysics,mOnUSBModeListener,workMode);
		}
	}
	/**
	 * 异步获取USB工作模式
	 * 
	 */
	public void getDPUUSBModeAsync(){
		if(mDPUDeviceType == DPUDeviceType.SMARTBOX30){
			Smartbox30DPUUSBModeSettings smartbox30DPUUSBModeSettings =  new Smartbox30DPUUSBModeSettings();
			smartbox30DPUUSBModeSettings.getDPUUSBModeAsync(mIPhysics,mOnUSBModeListener);
		}
		else{
			StandardDPUUSBModeSettings standardDPUUSBModeSettings = new StandardDPUUSBModeSettings();
			standardDPUUSBModeSettings.getDPUUSBModeAsync(mIPhysics,mOnUSBModeListener);
		}
	}
}
