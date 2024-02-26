package com.cnlaunch.physics;

import java.util.HashMap;
import java.util.Map;

import android.os.RemoteException;

import com.cnlaunch.physics.downloadbin.DownloadBinUpdate;
import com.cnlaunch.physics.remote.IRemoteDeviceFactoryManagerCallBack;
import com.cnlaunch.physics.utils.MLog;
import com.cnlaunch.physics.utils.Tools;

/**
 *  远程与本地切换开关
 * @author xiefeihong
 *
 */
public class RomoteLocalSwitch {
	private static class DPUProperty{
		IRemoteDeviceFactoryManagerCallBack mIRemoteDeviceFactoryManagerCallBack;
		boolean mIsTruck;
		boolean mIsCarAndHeavyduty;
		public DPUProperty(){
			mIRemoteDeviceFactoryManagerCallBack = null;
			mIsTruck =  false;
			mIsCarAndHeavyduty = false;
		}
		public DPUProperty(IRemoteDeviceFactoryManagerCallBack iRemoteDeviceFactoryManagerCallBack,
				boolean isTruck,boolean isCarAndHeavyduty){
			mIRemoteDeviceFactoryManagerCallBack = iRemoteDeviceFactoryManagerCallBack;
			mIsTruck =  isTruck;
			mIsCarAndHeavyduty = isCarAndHeavyduty;
		}
		public IRemoteDeviceFactoryManagerCallBack getIRemoteDeviceFactoryManagerCallBack() {
			return mIRemoteDeviceFactoryManagerCallBack;
		}
		public void setIRemoteDeviceFactoryManagerCallBack(
				IRemoteDeviceFactoryManagerCallBack iRemoteDeviceFactoryManagerCallBack) {
			this.mIRemoteDeviceFactoryManagerCallBack = iRemoteDeviceFactoryManagerCallBack;
		}
		public boolean isTruck() {
			return mIsTruck;
		}
		public void setIsTruck(boolean isTruck) {
			this.mIsTruck = isTruck;
		}
		public boolean isCarAndHeavyduty() {
			return mIsCarAndHeavyduty;
		}
		public void setIsCarAndHeavyduty(boolean isCarAndHeavyduty) {
			this.mIsCarAndHeavyduty = isCarAndHeavyduty;
		}
	}

	private boolean isRemoteMode ;
	private Map<String,DPUProperty> mDPUPropertyMap;
	private static RomoteLocalSwitch mRomoteLocalSwitch=null;
	public static RomoteLocalSwitch getInstance() {
		if (mRomoteLocalSwitch == null) {
			mRomoteLocalSwitch = new RomoteLocalSwitch();
		}
		return mRomoteLocalSwitch;
	}
	public RomoteLocalSwitch(){
		isRemoteMode = false;
		mDPUPropertyMap  = new HashMap<String,DPUProperty>();
	}
	public void setRemoteMode(boolean isRemoteMode){
		if(MLog.isDebug){
			MLog.d("RomoteLocalSwitch","current is remote mode "+isRemoteMode);
		}
		this.isRemoteMode = isRemoteMode;
	}
	public boolean isRemoteMode(){
		return isRemoteMode;
	}
	public void setDPUType(String serialNo,boolean isTruck,boolean isCarAndHeavyduty){
		DPUProperty dpuProperty = mDPUPropertyMap.get(serialNo);
		if(dpuProperty==null){
			dpuProperty  = new DPUProperty();
			dpuProperty.setIsTruck(isTruck);
			dpuProperty.setIsCarAndHeavyduty(isCarAndHeavyduty);
			mDPUPropertyMap.put(serialNo,  dpuProperty);
		}
		else{
			dpuProperty.setIsTruck(isTruck);
			dpuProperty.setIsCarAndHeavyduty(isCarAndHeavyduty);
		}
	}
	public void setRemoteDeviceFactoryManagerCallBack(String serialNo,IRemoteDeviceFactoryManagerCallBack remoteDeviceFactoryManagerCallBack){
		DPUProperty dpuProperty = mDPUPropertyMap.get(serialNo);
		if(dpuProperty==null){
			dpuProperty  = new DPUProperty();
			dpuProperty.setIRemoteDeviceFactoryManagerCallBack(remoteDeviceFactoryManagerCallBack);
			mDPUPropertyMap.put(serialNo,  dpuProperty);
		}
		else{
			dpuProperty.setIRemoteDeviceFactoryManagerCallBack(remoteDeviceFactoryManagerCallBack);
		}	
	}
	public IRemoteDeviceFactoryManagerCallBack getRemoteDeviceFactoryManagerCallBack(String serialNo){
		DPUProperty dpuProperty = mDPUPropertyMap.get(serialNo);
		if(dpuProperty==null){
			return null;
		}
		else{
			return dpuProperty.getIRemoteDeviceFactoryManagerCallBack();
		}
	}
	/**
	 * 连接对象设置通讯命令
	 * @param serialNo
	 * @param command
	 */
	public void setCommand(String serialNo,String command,boolean isTpmsManager){
		if(isRemoteMode()&&!isTpmsManager){
			try {
				IRemoteDeviceFactoryManagerCallBack callback= getRemoteDeviceFactoryManagerCallBack(serialNo);
				if(callback!=null){
					callback.send(command);
				}
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		else{
			DeviceFactoryManager.getInstance().send(command);
		}
	}
	/**
	 * 判断是否为重卡
	 * @return
	 */
	public boolean isTruck(String serialNo){
		if(isRemoteMode()){
			DPUProperty dpuProperty = mDPUPropertyMap.get(serialNo);
			if(dpuProperty==null){
				return false;
			}
			else{
				return dpuProperty.isTruck();
			}
		}
		else{
			return Tools.isTruck();
		}
	}
	
	/**
	 * 判断是否为二合一接头
	 * @return
	 */
	public boolean isCarAndHeavyduty(String serialNo){
		if(isRemoteMode()){
			DPUProperty dpuProperty = mDPUPropertyMap.get(serialNo);
			if(dpuProperty==null){
				return false;
			}
			else{
				return dpuProperty.isCarAndHeavyduty();
			}
		}
		else{
			return Tools.isCarAndHeavyduty();
		}
	}
}
