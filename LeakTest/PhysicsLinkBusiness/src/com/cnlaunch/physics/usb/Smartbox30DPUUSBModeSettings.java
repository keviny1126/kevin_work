package com.cnlaunch.physics.usb;

import com.cnlaunch.physics.utils.Constants;
import com.cnlaunch.physics.downloadbin.util.MyFactory;
import com.cnlaunch.physics.impl.IPhysics;
import com.cnlaunch.physics.listener.OnUSBModeListener;
import com.cnlaunch.physics.listener.OnWiFiModeListener;
import com.cnlaunch.physics.utils.MLog;
import com.cnlaunch.physics.utils.Tools;

public class Smartbox30DPUUSBModeSettings implements IUSBModeSettings{
	private final static String  TAG = "Smartbox30DPUUSBModeSettings";
	public Smartbox30DPUUSBModeSettings(){
	}
	/**
	 * 异步设置USB工作模式
	 */
	@Override
	public void setDPUUSBModeAsync(IPhysics iPhysics, OnUSBModeListener onUSBModeListener, int workMode){
		DPUUSBModeConfigSetterRunnable mDPUUSBModeConfigSetterRunnable = new DPUUSBModeConfigSetterRunnable(iPhysics,onUSBModeListener,workMode);
		Thread t=new Thread(mDPUUSBModeConfigSetterRunnable);
		t.start();
	}
	/**
	 * 异步获取USB工作模式
	 *
	 */
	@Override
	public void getDPUUSBModeAsync(IPhysics iPhysics, OnUSBModeListener onUSBModeListener){
		DPUUSBModeConfigGetterRunnable mDPUUSBModeConfigGetterRunnable = new DPUUSBModeConfigGetterRunnable(iPhysics,onUSBModeListener);
		Thread t=new Thread(mDPUUSBModeConfigGetterRunnable);
		t.start();
	}
	private class DPUUSBModeConfigSetterRunnable implements Runnable {
		private int mWorkMode;
		private  OnUSBModeListener mOnUSBModeListener;
		private IPhysics  mIPhysics;
		public DPUUSBModeConfigSetterRunnable(IPhysics iPhysics, OnUSBModeListener onUSBModeListener,int workMode) {
			mWorkMode = workMode;
			mOnUSBModeListener = onUSBModeListener;
			mIPhysics = iPhysics;
		}

		@Override
		public void run() {
			try {
				Boolean state = setDPUUSBMode(mIPhysics,mWorkMode);
				if (mOnUSBModeListener != null) {
					if (state) {
						mOnUSBModeListener.OnSetUSBModeConfigListener(OnUSBModeListener.USB_MODE_CONFIG_SUCCESS,mWorkMode);
					} else {
						mOnUSBModeListener.OnSetUSBModeConfigListener(OnUSBModeListener.USB_MODE_CONFIG_FAIL,mWorkMode);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				if (mOnUSBModeListener != null) {
					mOnUSBModeListener.OnSetUSBModeConfigListener(OnUSBModeListener.USB_MODE_CONFIG_FAIL,mWorkMode);
				}
			}
		}
	}

	private class DPUUSBModeConfigGetterRunnable implements Runnable {
		private  OnUSBModeListener mOnUSBModeListener;
		private IPhysics  mIPhysics;
		public DPUUSBModeConfigGetterRunnable(IPhysics iPhysics, OnUSBModeListener onUSBModeListener) {
			mOnUSBModeListener = onUSBModeListener;
			mIPhysics = iPhysics;
		}

		@Override
		public void run() {
			try {
				int workMode = getDPUUSBModeInformation(mIPhysics);
				if (mOnUSBModeListener != null) {
					if (workMode != Constants.USB_WORK_MODE_UNKNOWN) {
						mOnUSBModeListener.OnGetUSBModeConfigListener(OnWiFiModeListener.WIFI_MODE_CONFIG_SUCCESS, workMode);
					} else {
						mOnUSBModeListener.OnGetUSBModeConfigListener(OnWiFiModeListener.WIFI_MODE_CONFIG_FAIL, workMode);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				if (mOnUSBModeListener != null) {
					mOnUSBModeListener.OnGetUSBModeConfigListener(OnWiFiModeListener.WIFI_MODE_CONFIG_FAIL, Constants.USB_WORK_MODE_UNKNOWN);
				}
			}
		}
	}

	/**
	 * 配置USB工作模式
	 */
	private static Boolean setDPUUSBMode(IPhysics iPhysics,int workMode) {
		Boolean state = false;
		int maxWaitTime = 20000;
		byte[] dpuUSBModeSettingsOrder = null;
		byte[] dpuUSBModeReceiveBuffer = null;
		dpuUSBModeSettingsOrder = MyFactory.creatorForOrderMontage().generateSmartbox30LinuxCommonCommand(new byte[]{0x2c, 0x04}, new byte[]{(byte) (workMode & 0xff)});
		dpuUSBModeReceiveBuffer = Tools.dpuSmartbox30CommandOperation(iPhysics, dpuUSBModeSettingsOrder, maxWaitTime);
		if (dpuUSBModeReceiveBuffer != null && dpuUSBModeReceiveBuffer.length >= 1) {
			if (dpuUSBModeReceiveBuffer[0] == 0) {
				state = true;
			}
		}
		if (MLog.isDebug) {
			MLog.d(TAG, "setDPUUSBMode. state = " + state);
		}
		return state;
	}

	/**
	 * 获取USB工作模式信息
	 */
	private static int getDPUUSBModeInformation(IPhysics iPhysics) {
		int workMode = Constants.USB_WORK_MODE_UNKNOWN;
		byte[] dpuUSBModeOrder = MyFactory.creatorForOrderMontage().generateSmartbox30LinuxCommonCommand(new byte[]{0x2c,0x24}, null);
		byte[] dpuUSBModeReceiveBuffer = Tools.dpuSmartbox30CommandOperation(iPhysics,dpuUSBModeOrder);
		if(dpuUSBModeReceiveBuffer!=null && dpuUSBModeReceiveBuffer.length>=2
				&& dpuUSBModeReceiveBuffer[1] == 0){
			if(dpuUSBModeReceiveBuffer[0] == Constants.USB_WORK_MODE_WITH_SERIALPORT){
				workMode = Constants.USB_WORK_MODE_WITH_SERIALPORT;
			}
			else if(dpuUSBModeReceiveBuffer[0] == Constants.USB_WORK_MODE_WITH_ETHERNET){
				workMode = Constants.USB_WORK_MODE_WITH_ETHERNET;
			}
			else if(dpuUSBModeReceiveBuffer[0] == Constants.USB_WORK_MODE_WITH_BULK){
				workMode = Constants.USB_WORK_MODE_WITH_BULK;
			}
		}
		return  workMode;
	}


}
