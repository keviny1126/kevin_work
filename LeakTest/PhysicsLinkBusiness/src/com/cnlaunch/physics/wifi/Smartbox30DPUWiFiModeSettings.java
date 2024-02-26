package com.cnlaunch.physics.wifi;

import android.net.wifi.WifiConfiguration;

import com.cnlaunch.physics.downloadbin.util.MyFactory;
import com.cnlaunch.physics.impl.IPhysics;
import com.cnlaunch.physics.listener.OnWiFiModeListener;
import com.cnlaunch.physics.utils.Constants;
import com.cnlaunch.physics.utils.MLog;
import com.cnlaunch.physics.utils.NetworkUtil;
import com.cnlaunch.physics.utils.Tools;

import java.nio.charset.Charset;
import java.util.Arrays;

public class Smartbox30DPUWiFiModeSettings implements IWiFiModeSettings {
	private final static String TAG = "Smartbox30DPUWiFiModeSettings";

	public static final int SMARTBOX30_SECURITY_NONE = 1;
	public static final int SMARTBOX30_SECURITY_PSK = 4;

	public Smartbox30DPUWiFiModeSettings() {
	}

	/**
	 * 异步设置wifi工作模式
	 *
	 * @param dpuWiFiModeConfig
	 */
	public void setDPUWiFiModeAsync(IPhysics iPhysics, OnWiFiModeListener onWiFiModeListener, DPUWiFiModeConfig dpuWiFiModeConfig) {
		DPUWiFiModeConfigSetterRunnable mDPUWiFiModeConfigSetterRunnable = new DPUWiFiModeConfigSetterRunnable(iPhysics, onWiFiModeListener, dpuWiFiModeConfig);
		Thread t = new Thread(mDPUWiFiModeConfigSetterRunnable);
		t.start();
	}

	/**
	 * 异步获取wifi工作模式
	 */
	public void getDPUWiFiModeAsync(IPhysics iPhysics, OnWiFiModeListener onWiFiModeListener) {
		DPUWiFiModeConfigGetterRunnable mDPUWiFiModeConfigGetterRunnable = new DPUWiFiModeConfigGetterRunnable(iPhysics, onWiFiModeListener);
		Thread t = new Thread(mDPUWiFiModeConfigGetterRunnable);
		t.start();
	}

	@Override
	public void setDPUWiFiAPConfigAsync(IPhysics iPhysics, OnWiFiModeListener onWiFiModeListener, DPUWiFiAPConfig dpuWiFiAPConfig) {
		DPUWiFiAPConfigSetterRunnable mDPUWiFiAPConfigSetterRunnable = new DPUWiFiAPConfigSetterRunnable(iPhysics, onWiFiModeListener, dpuWiFiAPConfig);
		Thread t = new Thread(mDPUWiFiAPConfigSetterRunnable);
		t.start();
	}

	@Override
	public void getDPUWiFiAPConfigAsync(IPhysics iPhysics, OnWiFiModeListener onWiFiModeListener) {
		DPUWiFiAPConfigGetterRunnable mDPUWiFiAPConfigGetterRunnable = new DPUWiFiAPConfigGetterRunnable(iPhysics, onWiFiModeListener);
		Thread t = new Thread(mDPUWiFiAPConfigGetterRunnable);
		t.start();
	}



	private class DPUWiFiModeConfigSetterRunnable implements Runnable {
		private DPUWiFiModeConfig mDPUWiFiModeConfig;
		private OnWiFiModeListener mOnWiFiModeListener;
		private IPhysics mIPhysics;

		public DPUWiFiModeConfigSetterRunnable(IPhysics iPhysics, OnWiFiModeListener onWiFiModeListener, DPUWiFiModeConfig dpuWiFiModeConfig) {
			mDPUWiFiModeConfig = dpuWiFiModeConfig;
			mOnWiFiModeListener = onWiFiModeListener;
			mIPhysics = iPhysics;
		}

		@Override
		public void run() {
			try {
				Boolean state = setDPUWiFiMode(mIPhysics, mDPUWiFiModeConfig);
				if (mOnWiFiModeListener != null) {
					if (state) {
						mOnWiFiModeListener.OnSetWiFiModeConfigListener(OnWiFiModeListener.WIFI_MODE_CONFIG_SUCCESS);
					} else {
						mOnWiFiModeListener.OnSetWiFiModeConfigListener(OnWiFiModeListener.WIFI_MODE_CONFIG_FAIL);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				if (mOnWiFiModeListener != null) {
					mOnWiFiModeListener.OnSetWiFiModeConfigListener(OnWiFiModeListener.WIFI_MODE_CONFIG_FAIL);
				}
			}
		}
	}

	private class DPUWiFiModeConfigGetterRunnable implements Runnable {
		private OnWiFiModeListener mOnWiFiModeListener;
		private IPhysics mIPhysics;

		public DPUWiFiModeConfigGetterRunnable(IPhysics iPhysics, OnWiFiModeListener onWiFiModeListener) {
			mOnWiFiModeListener = onWiFiModeListener;
			mIPhysics = iPhysics;
		}

		@Override
		public void run() {
			try {
				DPUWiFiModeConfig dpuWiFiModeConfig = getDPUWiFiModeInformation(mIPhysics);
				if (mOnWiFiModeListener != null) {
					if (dpuWiFiModeConfig != null && dpuWiFiModeConfig.getMode() != Constants.WIFI_WORK_MODE_UNKNOWN) {
						mOnWiFiModeListener.OnGetWiFiModeConfigListener(OnWiFiModeListener.WIFI_MODE_CONFIG_SUCCESS, dpuWiFiModeConfig);
					} else {
						mOnWiFiModeListener.OnGetWiFiModeConfigListener(OnWiFiModeListener.WIFI_MODE_CONFIG_FAIL, dpuWiFiModeConfig);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				if (mOnWiFiModeListener != null) {
					mOnWiFiModeListener.OnGetWiFiModeConfigListener(OnWiFiModeListener.WIFI_MODE_CONFIG_FAIL, null);
				}
			}
		}
	}


	private class DPUWiFiAPConfigSetterRunnable implements Runnable {
		private OnWiFiModeListener mOnWiFiModeListener;
		private IPhysics mIPhysics;
		DPUWiFiAPConfig mDPUWiFiAPConfig;
		public DPUWiFiAPConfigSetterRunnable(IPhysics iPhysics, OnWiFiModeListener onWiFiModeListener, DPUWiFiAPConfig dpuWiFiAPConfig) {
			mDPUWiFiAPConfig = dpuWiFiAPConfig;
			mOnWiFiModeListener = onWiFiModeListener;
			mIPhysics = iPhysics;
		}

		@Override
		public void run() {
			try {
				boolean state = setDPUWiFiAPConfig(mIPhysics, mDPUWiFiAPConfig);
				if(MLog.isDebug){
					MLog.d(TAG,"setDPUWiFiAPConfig state="+state);
				}
				if (mOnWiFiModeListener != null) {
					if (state) {
						mOnWiFiModeListener.OnSetWiFiAPConfigListener(OnWiFiModeListener.WIFI_MODE_CONFIG_SUCCESS);
					} else {
						mOnWiFiModeListener.OnSetWiFiAPConfigListener(OnWiFiModeListener.WIFI_MODE_CONFIG_FAIL);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				if (mOnWiFiModeListener != null) {
					mOnWiFiModeListener.OnSetWiFiAPConfigListener(OnWiFiModeListener.WIFI_MODE_CONFIG_FAIL);
				}
			}
		}
	}

	private class DPUWiFiAPConfigGetterRunnable implements Runnable {
		private OnWiFiModeListener mOnWiFiModeListener;
		private IPhysics mIPhysics;

		public DPUWiFiAPConfigGetterRunnable(IPhysics iPhysics, OnWiFiModeListener onWiFiModeListener) {
			mOnWiFiModeListener = onWiFiModeListener;
			mIPhysics = iPhysics;
		}

		@Override
		public void run() {
			try {
				DPUWiFiAPConfig dpuWiFiAPConfig = getDPUWiFiAPConfig(mIPhysics);
				if (mOnWiFiModeListener != null) {
					if (dpuWiFiAPConfig == null) {
						mOnWiFiModeListener.OnGetWiFiAPConfigListener(OnWiFiModeListener.WIFI_MODE_CONFIG_FAIL, dpuWiFiAPConfig);
					} else {
						mOnWiFiModeListener.OnGetWiFiAPConfigListener(OnWiFiModeListener.WIFI_MODE_CONFIG_SUCCESS, dpuWiFiAPConfig);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				if (mOnWiFiModeListener != null) {
					mOnWiFiModeListener.OnGetWiFiAPConfigListener(OnWiFiModeListener.WIFI_MODE_CONFIG_FAIL, null);
				}
			}
		}
	}

	/**
	 * 生成设置wifi sta 工作模式需要连接AP信息指令
	 *
	 * @return
	 */
	private static byte[] generateDPUWiFiModeSettingsSTACommand(DPUWiFiModeConfig dpuWiFiModeConfig) {
		WifiConfiguration config = dpuWiFiModeConfig.getConfig();
		if (dpuWiFiModeConfig.getConfig() == null) {
			return null;
		}
		String SSID = config.SSID;
		int securityType = NetworkUtil.getWiFiAccessPointSecurity(config);
		int smartbox30SecurityType = SMARTBOX30_SECURITY_NONE;
		switch (securityType) {
			case 0:
				smartbox30SecurityType = SMARTBOX30_SECURITY_NONE;
				break;
			case 2:
				smartbox30SecurityType = SMARTBOX30_SECURITY_PSK;
				break;
		}

		String password = NetworkUtil.getWiFiAccessPointPassword(config, securityType);
		//
		if (MLog.isDebug) {
			MLog.d(TAG, String.format(" WifiConfiguration SSID=%s Security=%d smartbox30SecurityType =%d Password=%s",
					SSID,
					securityType,
					smartbox30SecurityType,
					password)
			);
		}
		byte[] ssidBytes = SSID.getBytes(Charset.forName("US-ASCII"));
		int ssidLength = ssidBytes.length;
		byte[] passwordBytes = password.getBytes(Charset.forName("US-ASCII"));
		int passwordLength = passwordBytes.length;

		//总的wifi配置信息字符数组
		byte[] newDataBytes = new byte[1 + ssidLength + 1 + 1 + passwordLength];
		newDataBytes[0] = (byte) (ssidLength & 0xff);
		int currentIndex = 1;
		System.arraycopy(ssidBytes, 0, newDataBytes, currentIndex, ssidLength);
		currentIndex += ssidLength;
		newDataBytes[currentIndex++] = (byte) smartbox30SecurityType;
		newDataBytes[currentIndex++] = (byte) (passwordLength & 0xff);
		System.arraycopy(passwordBytes, 0, newDataBytes, currentIndex, passwordLength);
		return MyFactory.creatorForOrderMontage().generateSmartbox30LinuxCommonCommand(new byte[]{0x2c, 0x12}, newDataBytes);
	}

	/**
	 * 配置wifi工作模式
	 */
	private static Boolean setDPUWiFiMode(IPhysics iPhysics, DPUWiFiModeConfig dpuWiFiModeConfig) {
		Boolean state = false;
		int maxWaitTime = 20000;
		byte[] dpuWiFiModeSettingsOrder = null;
		byte[] dpuWiFiModeReceiveBuffer = null;
		if (dpuWiFiModeConfig.getMode() == Constants.WIFI_WORK_MODE_WITH_AP) {
			dpuWiFiModeSettingsOrder = MyFactory.creatorForOrderMontage().generateSmartbox30LinuxCommonCommand(new byte[]{0x2c, 0x14}, new byte[]{0x01});
			dpuWiFiModeReceiveBuffer = Tools.dpuSmartbox30CommandOperation(iPhysics, dpuWiFiModeSettingsOrder, maxWaitTime);
			if (dpuWiFiModeReceiveBuffer != null && dpuWiFiModeReceiveBuffer.length >= 1) {
				if (dpuWiFiModeReceiveBuffer[0] == 0) {
					state = true;
				}
			}
		} else if (dpuWiFiModeConfig.getMode() == Constants.WIFI_WORK_MODE_WITH_STA_MODE_NO_INTERACTION) {
			byte[] dpuWiFiModeSettingsSTAOrder = generateDPUWiFiModeSettingsSTACommand(dpuWiFiModeConfig);
			byte[] dpuWiFiModeSettingsSTAReceiveBuffer = Tools.dpuSmartbox30CommandOperation(iPhysics, dpuWiFiModeSettingsSTAOrder, maxWaitTime);
			if (dpuWiFiModeSettingsSTAReceiveBuffer != null && dpuWiFiModeSettingsSTAReceiveBuffer.length >= 1) {
				if (dpuWiFiModeSettingsSTAReceiveBuffer[0] == 0) {
					//无需再执行切换指令
					/*dpuWiFiModeSettingsOrder = MyFactory.creatorForOrderMontage().generateSmartbox30LinuxCommonCommand(new byte[]{0x2c, 0x14}, new byte[]{0x02});
					dpuWiFiModeReceiveBuffer = Tools.dpuSmartbox30CommandOperation(iPhysics, dpuWiFiModeSettingsOrder,maxWaitTime);
					if (dpuWiFiModeReceiveBuffer != null && dpuWiFiModeReceiveBuffer.length >= 1) {
						if (dpuWiFiModeReceiveBuffer[0] == 0) {
							state = true;
						}
					}*/
					state = true;
				}
			}
		}
		if (MLog.isDebug) {
			MLog.d(TAG, "setDPUWiFiMode. state = " + state);
		}
		return state;
	}

	/**
	 * 解析获取的wifi模式信息
	 *
	 * @param receiveBuffer 去掉了命令字与校验字的数据
	 * @return
	 */
	private static DPUWiFiModeConfig analysisDPUWiFiModeInformation(byte[] receiveBuffer) {
		DPUWiFiModeConfig dpuWiFiModeConfig = null;
		// 0x21,0x19 获取接头wifi相关工作信息
		// ----------0x01 表示获取wifi工作模式
		// 回复信息
		// 0x61,0x19,0x01,0x00,表示获取信息成功
		// ------------------0x01 表示接头wifi工作模式为热点模式
		// ------------------0x02+ssid+安全类型+密码 表示接头wifi工作模式为网卡模式
		if (receiveBuffer[1] == 0) {
			dpuWiFiModeConfig = new DPUWiFiModeConfig();
			switch (receiveBuffer[2]) {
				case 0x01:
					dpuWiFiModeConfig.setMode(Constants.WIFI_WORK_MODE_WITH_AP);
					break;
				case 0x02:
					dpuWiFiModeConfig.setMode(Constants.WIFI_WORK_MODE_WITH_STA_MODE_NO_INTERACTION);
					int currentIndex = 3;
					int ssidLength = (receiveBuffer[currentIndex] & 0xFF) * 256 + (receiveBuffer[currentIndex + 1] & 0xFF);
					String networkSSID = new String(receiveBuffer, currentIndex + 2, ssidLength, Charset.forName("US-ASCII"));
					currentIndex += 2 + ssidLength;
					int securityType = receiveBuffer[currentIndex];
					currentIndex += 1;
					int passwordLength = (receiveBuffer[currentIndex] & 0xFF) * 256 + (receiveBuffer[currentIndex + 1] & 0xFF);
					String password = new String(receiveBuffer, currentIndex + 2, passwordLength, Charset.forName("US-ASCII"));
					WifiConfiguration config = DPUWiFiModeConfig.packageWifiConfiguration(networkSSID,
							securityType,
							password);
					dpuWiFiModeConfig.setConfig(config);
					if (MLog.isDebug) {
						MLog.d(TAG, String.format(" analysisDPUWiFiModeInformation SSID=%s Security=%d Password=%s",
								networkSSID,
								securityType,
								password)
						);
					}
					break;
			}
			return dpuWiFiModeConfig;
		} else {
			return null;
		}
	}

	/**
	 * 获取wifi工作模式信息
	 */
	private static DPUWiFiModeConfig getDPUWiFiModeInformation(IPhysics iPhysics) {
		DPUWiFiModeConfig dpuWiFiModeConfig = null;
		byte[] dpuWiFiModeOrder = MyFactory.creatorForOrderMontage().generateSmartbox30LinuxCommonCommand(new byte[]{0x2c, 0x15}, null);
		byte[] dpuWiFiModeReceiveBuffer = Tools.dpuSmartbox30CommandOperation(iPhysics, dpuWiFiModeOrder);
		if (dpuWiFiModeReceiveBuffer != null && dpuWiFiModeReceiveBuffer.length >= 2
				&& dpuWiFiModeReceiveBuffer[1] == 0) {
			if (dpuWiFiModeReceiveBuffer[0] == 1) {
				dpuWiFiModeConfig = new DPUWiFiModeConfig();
				dpuWiFiModeConfig.setMode(Constants.WIFI_WORK_MODE_WITH_AP);
			} else if (dpuWiFiModeReceiveBuffer[0] == 2) {
				dpuWiFiModeConfig = new DPUWiFiModeConfig();
				dpuWiFiModeConfig.setMode(Constants.WIFI_WORK_MODE_WITH_STA_MODE_NO_INTERACTION);
			} else if (dpuWiFiModeReceiveBuffer[0] == 3 || dpuWiFiModeReceiveBuffer[1] != 0) {
				dpuWiFiModeConfig = new DPUWiFiModeConfig();
				dpuWiFiModeConfig.setMode(Constants.WIFI_WORK_MODE_UNKNOWN);
			}
		}
		return dpuWiFiModeConfig;
	}

	/**
	 * 生成获取wifista模式ip地址与端口请求数据包指令
	 *
	 * @param dpuWiFiModeConfig
	 * @return
	 */
	public static byte[] generateDPUWiFiDatagramRequestCommand(DPUWiFiModeConfig dpuWiFiModeConfig) {
		String serialNo = dpuWiFiModeConfig.getSerialNo();
		return MyFactory.creatorForOrderMontage().generateSmartbox30LinuxCommonCommand(new byte[]{0x00, (byte) 0x80}, null);
	}

	/**
	 * 生成获取wifista模式ip地址与端口请求数据包指令
	 *
	 * @return
	 */
	public static byte[] generateDPUWiFiDatagramRequestCommand2C29() {
		return MyFactory.creatorForOrderMontage().generateSmartbox30LinuxCommonCommand(new byte[]{0x2c, 0x29}, null);
	}

	/**
	 * 数据格式如下
	 * 发送数据：0x55,0xAA,0xF1,0xF8,0x00,0x03,0x01,0x00,0x80,0x8B
	 * 回复内容：0x55,0xAA,0xF8,0xF1,0x00,0x11,0x01,0x40,0x80,0x00,0x0C,0x39,0x38,0x36,0x34,0x39,0x30,0x30,0x30,0x30,0x31,0x31,0x38,0x57
	 * 解析验证通过udp获取接头的响应数据包指令
	 *
	 * @param sendOrder    完整请求命令
	 * @param receiveOrder 完整响应命令
	 * @return 正确则返回true
	 */
	public static boolean analysisDPUWiFiDatagramAnswerCommand(String serialNo, byte[] sendOrder, byte[] receiveOrder) {
		boolean state = false;
		int currentIndex;
		int contentLength;
		byte[] srcSerialNo = serialNo.getBytes(Charset.forName("US-ASCII"));
		if (receiveOrder.length <= 6) {
			return false;
		}
		currentIndex = 4;
		contentLength = (receiveOrder[currentIndex] & 0xFF) * 256 + (receiveOrder[currentIndex + 1] & 0xFF);
		if (contentLength <= receiveOrder.length - 4 - 1) {
			int serialNoLength = (receiveOrder[currentIndex + 5] & 0xFF) * 256 + (receiveOrder[currentIndex + 6] & 0xFF);
			//避免异常数据
			if (serialNoLength >= contentLength) {
				return false;
			}
			byte[] dstSerialNo = new byte[serialNoLength];
			System.arraycopy(receiveOrder, currentIndex + 7, dstSerialNo, 0, serialNoLength);
			if (MLog.isDebug) {
				MLog.d(TAG, "analysisDPUWiFiDatagramAnswerCommand receiveOrder serino = " + new String(dstSerialNo, Charset.forName("US-ASCII")));
			}
			if (srcSerialNo != null && dstSerialNo != null && Arrays.equals(srcSerialNo, dstSerialNo)) {
				state = true;
			}
		}
		return state;
	}

	/**
	 * 获取wifi SSID广播是否开放。
	 */
	private static DPUWiFiAPConfig getDPUWiFiAPConfig(IPhysics iPhysics) {
		DPUWiFiAPConfig dpuWiFiAPConfig =null ;
		byte[] dpuWiFiAPDisplayOrder = MyFactory.creatorForOrderMontage().generateSmartbox30LinuxCommonCommand(new byte[]{0x2c, 0x02}, null);
		byte[] dpuWiFiAPDisplayReceiveBuffer = Tools.dpuSmartbox30CommandOperation(iPhysics, dpuWiFiAPDisplayOrder);
		if (dpuWiFiAPDisplayReceiveBuffer != null && dpuWiFiAPDisplayReceiveBuffer.length >= 2) {
			dpuWiFiAPConfig =new DPUWiFiAPConfig() ;
			dpuWiFiAPConfig.setChannel(dpuWiFiAPDisplayReceiveBuffer[0] & 0xFF);
			if(dpuWiFiAPDisplayReceiveBuffer[1]==0) {
				dpuWiFiAPConfig.setSSIDBroadcastDisplay(false);
			}
			else{
				dpuWiFiAPConfig.setSSIDBroadcastDisplay(true);
			}
		}
		return dpuWiFiAPConfig;
	}



	private static boolean setDPUWiFiAPConfig(IPhysics iPhysics, DPUWiFiAPConfig dpuWiFiAPConfig) {
		boolean isSuccess = false;
		byte[] dpuWiFiAPDisplayOrder = MyFactory.creatorForOrderMontage().generateSmartbox30LinuxCommonCommand(new byte[]{0x2c, 0x02}, null);
		byte[] dpuWiFiAPDisplayReceiveBuffer = Tools.dpuSmartbox30CommandOperation(iPhysics, dpuWiFiAPDisplayOrder);
		if (dpuWiFiAPDisplayReceiveBuffer != null && dpuWiFiAPDisplayReceiveBuffer.length >= 2) {
			dpuWiFiAPDisplayReceiveBuffer[0] = (byte) (dpuWiFiAPConfig.getChannel() & 0xFF);
			if (dpuWiFiAPConfig.isSSIDBroadcastDisplay()) {
				dpuWiFiAPDisplayReceiveBuffer[1] = 1;
			} else {
				dpuWiFiAPDisplayReceiveBuffer[1] = 0;
			}
			byte[] dpuWiFiAPDisplaySetOrder = MyFactory.creatorForOrderMontage().generateSmartbox30LinuxCommonCommand(new byte[]{0x2c, 0x01}, dpuWiFiAPDisplayReceiveBuffer);
			byte[] dpuWiFiAPDisplaySetReceiveBuffer = Tools.dpuSmartbox30CommandOperation(iPhysics, dpuWiFiAPDisplaySetOrder);
			if (dpuWiFiAPDisplaySetReceiveBuffer != null && dpuWiFiAPDisplaySetReceiveBuffer.length >= 1) {
				if (dpuWiFiAPDisplaySetReceiveBuffer[0] == 0) {
					isSuccess = true;
				}
			}
		} else {
			isSuccess = false;
		}
		return isSuccess;
	}
}
