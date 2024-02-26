package com.cnlaunch.physics.wifi;

import java.nio.charset.Charset;

import com.cnlaunch.physics.utils.MLog;
import com.cnlaunch.physics.utils.NetworkUtil;

import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.AuthAlgorithm;
import android.net.wifi.WifiConfiguration.KeyMgmt;

public class DPUWiFiModeConfig {
	private  int mMode;
	private  WifiConfiguration mConfig;
	private String mSerialNo;
	public DPUWiFiModeConfig(){
		this(-1,null,null);
	}
	
	public DPUWiFiModeConfig(int mode,WifiConfiguration config,String serialNo){
		mMode = mode;
		mConfig = config;
		mSerialNo = serialNo;
	}
	public int getMode() {
		return mMode;
	}
	public void setMode(int mode) {
		this.mMode = mode;
	}
	public WifiConfiguration getConfig() {
		return mConfig;
	}
	public void setConfig(WifiConfiguration config) {
		this.mConfig = config;
	}
	public String getSerialNo() {
		return mSerialNo;
	}
	public void setSerialNo(String SerialNo) {
		this.mSerialNo = SerialNo;
	}
	public static WifiConfiguration packageWifiConfiguration(String networkssid,int securityType,String password){
		WifiConfiguration config = new WifiConfiguration();		
		if(MLog.isDebug){
			MLog.d("DPUWiFiModeConfig", String.format(" WifiConfiguration SSID=%s Security=%d Password=%s",
					networkssid,
					securityType,
					password)
			);
		}
		config.SSID = networkssid;
		switch (securityType) {
		case NetworkUtil.SECURITY_NONE:
			config.allowedKeyManagement.set(KeyMgmt.NONE);
			break;
		case NetworkUtil.SECURITY_WEP:
			config.allowedKeyManagement.set(KeyMgmt.NONE);
			config.allowedAuthAlgorithms
					.set(AuthAlgorithm.OPEN);
			config.allowedAuthAlgorithms
					.set(AuthAlgorithm.SHARED);
			if (password.length() != 0) {
				int length = password.length();
				// WEP-40, WEP-104, and 256-bit WEP (WEP-232?)
				if ((length == 10 || length == 26 || length == 58)
						&& password.matches("[0-9A-Fa-f]*")) {
					config.wepKeys[0] = password;
				} else {
					config.wepKeys[0] = password ;
				}
			}
			break;
		case NetworkUtil.SECURITY_PSK:
			config.allowedKeyManagement.set(KeyMgmt.WPA_PSK);
			if (password.length() != 0) {
				if (password.matches("[0-9A-Fa-f]{64}")) {
					config.preSharedKey = password;
				} else {
					config.preSharedKey = password ;
				}
			}
			break;
		}
		if(MLog.isDebug){
			MLog.d("DPUWiFiModeConfig", String.format(" WifiConfiguration SSID=%s Security=%d Password=%s,%s",
					networkssid,
					securityType,
					config.wepKeys[0],config.preSharedKey)
			);
		}
		return config;
	}
}
