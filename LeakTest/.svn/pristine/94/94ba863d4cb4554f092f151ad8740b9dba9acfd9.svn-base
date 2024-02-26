package com.cnlaunch.physics.utils;
import java.util.Date;
import java.util.List;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.AuthAlgorithm;
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
/*
 * WiFi 连接配置类
 */
public class WiFiConnectUtils {
	private static final String TAG = "WiFiConnectUtils";
	private WifiManager wifiManager;

	private   OnWiFiConnectListener mOnWiFiConnectListener;
	//定义几种加密方式，一种是WEP，一种是WPA，还有没有密码的情况    
	public enum WifiCipherType {
		WIFICIPHER_WEP, WIFICIPHER_WPA, WIFICIPHER_NOPASS, WIFICIPHER_INVALID
	}

	//构造函数
	public WiFiConnectUtils(WifiManager wifiManager, OnWiFiConnectListener onWiFiConnectListener) {
		this.wifiManager = wifiManager;
		mOnWiFiConnectListener = onWiFiConnectListener;

	}
	private Handler mHandler= new Handler(Looper.getMainLooper()){
		/**
		 * Subclasses must implement this to receive messages.
		 *
		 * @param msg
		 */
		@Override
		public void handleMessage(Message msg) {
			if(msg.what == 0){
				if(mOnWiFiConnectListener!=null){
					mOnWiFiConnectListener.onConnectSucess();
				}
			}
			else if(msg.what == 1){
				if(mOnWiFiConnectListener!=null){
					mOnWiFiConnectListener.onConnectFail();
				}
			}
		}
	};
	// 提供一个外部接口，传入要连接的无线网
	public void connect(String ssid, String password, WifiCipherType type) {
		MLog.d(TAG, "Connect Info:SSID:" + ssid + " PWD:" + password + " TYPE:" + type);
		Thread thread = new Thread(new ConnectRunnable(ssid, password, type));
		thread.start();
	}

	class ConnectRunnable implements Runnable {
		private String ssid;
		private String password;
		private WifiCipherType type;

		public ConnectRunnable(String ssid, String password, WifiCipherType type) {
			this.ssid = ssid;
			this.password = password;
			this.type = type;
		}


		@Override
		public void run() {
			openWifi();
			// 开启wifi功能需要一段时间 要等到wifi
			// 状态变成WIFI_STATE_ENABLED的时候才能执行下面的语句
			long milliseconds = (new Date()).getTime();
			while (wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLING) {
				//10秒自动退出
				if (((new Date()).getTime() - milliseconds) > 10000){
					mHandler.sendEmptyMessage(1);
					return ;
				}
				try {
					Thread.sleep(1000);// 为了避免程序一直while循环，让它睡个100毫秒检测……
				} catch (InterruptedException ie) {
					mHandler.sendEmptyMessage(1);
				}
			}
			try {
				WifiConfiguration wifiConfig = createWifiInfo(ssid, password, type);
				if (wifiConfig == null) {
					mHandler.sendEmptyMessage(1);
					return;
				}
				WifiConfiguration tempConfig = isExsits(ssid); //查询之前是否配置过这个网络
				if (tempConfig != null) {
					wifiManager.removeNetwork(tempConfig.networkId);
					MLog.d(TAG, "remove old wifiConfig !");
				}
				int netID = wifiManager.addNetwork(wifiConfig);
				wifiManager.enableNetwork(netID, true);
				if(wifiManager.reconnect()){
					mHandler.sendEmptyMessage(1);
				}
				else{
					mHandler.sendEmptyMessage(0);
				}
			} catch (Exception e) {
				e.printStackTrace();
				mHandler.sendEmptyMessage(1);
			}
		}
	}

	// 查看以前是否也配置过这个网络
	private WifiConfiguration isExsits(String SSID) {
		List<WifiConfiguration> existingConfigs = wifiManager.getConfiguredNetworks();
		for (WifiConfiguration existingConfig : existingConfigs) {
			if (existingConfig.SSID.equals("\"" + SSID + "\"")) {
				return existingConfig;
			}
		}
		return null;
	}

	private WifiConfiguration createWifiInfo(String SSID, String Password, WifiCipherType Type) {
		WifiConfiguration config = new WifiConfiguration();
		config.allowedAuthAlgorithms.clear();
		config.allowedGroupCiphers.clear();
		config.allowedKeyManagement.clear();
		config.allowedPairwiseCiphers.clear();
		config.allowedProtocols.clear();
		config.SSID = "\"" + SSID + "\"";
		// nopass
		if (Type == WifiCipherType.WIFICIPHER_NOPASS) {
			config.wepKeys[0] = "";
			config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
			config.wepTxKeyIndex = 0;
		}
		// wep
		if (Type == WifiCipherType.WIFICIPHER_WEP) {
			if (!TextUtils.isEmpty(Password)) {
				if (isHexWepKey(Password)) {
					config.wepKeys[0] = Password;
				} else {
					config.wepKeys[0] = "\"" + Password + "\"";
				}
			}
			config.allowedAuthAlgorithms.set(AuthAlgorithm.OPEN);
			config.allowedAuthAlgorithms.set(AuthAlgorithm.SHARED);
			config.allowedKeyManagement.set(KeyMgmt.NONE);
			config.wepTxKeyIndex = 0;
		}
		// wpa
		if (Type == WifiCipherType.WIFICIPHER_WPA) {
			config.preSharedKey = "\"" + Password + "\"";
			config.hiddenSSID = true;
			config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
			config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
			config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
			// 此处需要修改否则不能自动重联
			// config.allowedProtocols.set(WifiConfiguration.Protocol.WPA); 
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
			config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
			config.status = WifiConfiguration.Status.ENABLED;
		}
		return config;
	}

	// 打开wifi功能
	private boolean openWifi() {
		boolean bRet = true;
		if (!wifiManager.isWifiEnabled()) {
			bRet = wifiManager.setWifiEnabled(true);
		}
		return bRet;
	}


	private static boolean isHexWepKey(String wepKey) {
		final int len = wepKey.length();
		// WEP-40, WEP-104, and some vendors using 256-bit WEP (WEP-232?)
		if (len != 10 && len != 26 && len != 58) {
			return false;
		}
		return isHex(wepKey);
	}

	private static boolean isHex(String key) {
		for (int i = key.length() - 1; i >= 0; i--) {
			final char c = key.charAt(i);
			if (!(c >= '0' && c <= '9' || c >= 'A' && c <= 'F' || c >= 'a' && c <= 'f')) {
				return false;
			}
		}
		return true;
	}
	public static interface OnWiFiConnectListener {
		void onConnectSucess();
		void onConnectFail();
	}
}
