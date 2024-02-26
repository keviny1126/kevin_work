package com.cnlaunch.physics.wifi;
import java.nio.charset.Charset;
import java.util.Arrays;
import com.cnlaunch.physics.DPUDeviceType;
import com.cnlaunch.physics.impl.IPhysics;
import com.cnlaunch.physics.listener.OnWiFiModeListener;
import com.cnlaunch.physics.utils.MLog;

public class DPUWiFiModeSettings {
	private final static String  TAG = "DPUWiFiModeSettings";
	private  OnWiFiModeListener mOnWiFiModeListener; 
	private IPhysics  mIPhysics;
	private  int mDPUDeviceType;
	public DPUWiFiModeSettings(IPhysics  iPhysics,OnWiFiModeListener onWiFiModeListener){
		this(iPhysics,onWiFiModeListener,DPUDeviceType.STANDARD);
	}
	public DPUWiFiModeSettings(IPhysics  iPhysics,OnWiFiModeListener onWiFiModeListener,int dpuDeviceType){
		mOnWiFiModeListener = onWiFiModeListener;
		mIPhysics = iPhysics;
		mDPUDeviceType = dpuDeviceType;
	}
	/*public void enableWiFi(boolean isEnable){

	}*/
	/**
	 * 异步设置wifi工作模式
	 * @param dpuWiFiModeConfig
	 */
	public void setDPUWiFiModeAsync(DPUWiFiModeConfig dpuWiFiModeConfig){		
		if(mDPUDeviceType == DPUDeviceType.SMARTBOX30){
			Smartbox30DPUWiFiModeSettings smartbox30DPUWiFiModeSettings =  new Smartbox30DPUWiFiModeSettings();
			smartbox30DPUWiFiModeSettings.setDPUWiFiModeAsync(mIPhysics,mOnWiFiModeListener,dpuWiFiModeConfig);
		}
		else{
			StandardDPUWiFiModeSettings standardDPUWiFiModeSettings = new StandardDPUWiFiModeSettings();
			standardDPUWiFiModeSettings.setDPUWiFiModeAsync(mIPhysics,mOnWiFiModeListener,dpuWiFiModeConfig);
		}
	}
	/**
	 * 异步获取wifi热点工作模式
	 * 
	 */
	public void getDPUWiFiModeAsync(){
		if(mDPUDeviceType == DPUDeviceType.SMARTBOX30){
			Smartbox30DPUWiFiModeSettings smartbox30DPUWiFiModeSettings =  new Smartbox30DPUWiFiModeSettings();
			smartbox30DPUWiFiModeSettings.getDPUWiFiModeAsync(mIPhysics,mOnWiFiModeListener);
		}
		else{
			StandardDPUWiFiModeSettings standardDPUWiFiModeSettings = new StandardDPUWiFiModeSettings();
			standardDPUWiFiModeSettings.getDPUWiFiModeAsync(mIPhysics,mOnWiFiModeListener);
		}
	}


	/**
	 * 异步设置wifi热点工作模式
	 * @param dpuWiFiAPConfig
	 */
	public void setDPUWiFiAPConfigAsync(DPUWiFiAPConfig dpuWiFiAPConfig){
		if(mDPUDeviceType == DPUDeviceType.SMARTBOX30){
			Smartbox30DPUWiFiModeSettings smartbox30DPUWiFiModeSettings =  new Smartbox30DPUWiFiModeSettings();
			smartbox30DPUWiFiModeSettings.setDPUWiFiAPConfigAsync(mIPhysics,mOnWiFiModeListener,dpuWiFiAPConfig);
		}
		else{
			StandardDPUWiFiModeSettings standardDPUWiFiModeSettings = new StandardDPUWiFiModeSettings();
			standardDPUWiFiModeSettings.setDPUWiFiAPConfigAsync(mIPhysics,mOnWiFiModeListener,dpuWiFiAPConfig);
		}
	}
	/**
	 * 异步获取wifi SSID广播是否开放
	 *
	 */
	public void getDPUWiFiAPConfigAsync(){
		if(mDPUDeviceType == DPUDeviceType.SMARTBOX30){
			Smartbox30DPUWiFiModeSettings smartbox30DPUWiFiModeSettings =  new Smartbox30DPUWiFiModeSettings();
			smartbox30DPUWiFiModeSettings.getDPUWiFiAPConfigAsync(mIPhysics,mOnWiFiModeListener);
		}
		else{
			StandardDPUWiFiModeSettings standardDPUWiFiModeSettings = new StandardDPUWiFiModeSettings();
			standardDPUWiFiModeSettings.getDPUWiFiAPConfigAsync(mIPhysics,mOnWiFiModeListener);
		}
	}
}
