package com.cnlaunch.physics;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.cnlaunch.physics.utils.Constants;
import com.cnlaunch.physics.utils.MLog;
import com.cnlaunch.physics.utils.NetworkUtil;
import com.cnlaunch.physics.wifi.DPUWiFiModeConfig;

import android.net.wifi.WifiConfiguration;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Pair;

/**
 * 连接对象设置信息
 * 
 * @author xiefeihong
 * 
 */
public class DPULinkSettingsInformation {
	private static final String TAG = "DPULinkSettingsInformation";
	private static DPULinkSettingsInformation mDPULinkSettingsInformation = null;
	private String settingsFile;
	private PropertyFileOperation mPropertyFileOperation;
	public static DPULinkSettingsInformation getInstance() {
		if (mDPULinkSettingsInformation == null)
			mDPULinkSettingsInformation = new DPULinkSettingsInformation();
		return mDPULinkSettingsInformation;
	}

	public DPULinkSettingsInformation() {
		settingsFile = Environment.getExternalStorageDirectory().getPath()
				+ File.separator + "cnlaunch" + File.separator
				+ Constants.DPU_SETTINGS_INFORMATION;
		mPropertyFileOperation = new PropertyFileOperation(settingsFile);
	}
	/**
	 * 保存wifi未设置时蓝牙通讯模式选择信息
	 */
	public void saveBluetoothPreferencesSetting(String serialNO,boolean bluetoothSwitch) {
		String bluetoothPreferencesSettingsKey = String.format("%1s.%2s", serialNO,Constants.LINK_MODE_BLUETOOTH_SWITCH);
		mPropertyFileOperation.put(bluetoothPreferencesSettingsKey,Boolean.valueOf(bluetoothSwitch).toString());
	}
	/**
	 * 获取wifi未设置时蓝牙通讯模式选择信息
	 */
	public boolean getBluetoothPreferencesSetting(String serialNO) {
		String bluetoothPreferencesSettings = mPropertyFileOperation.get(String.format("%1s.%2s", serialNO,
				Constants.LINK_MODE_BLUETOOTH_SWITCH));
		return Boolean.parseBoolean(bluetoothPreferencesSettings);
	}
	/**
	 * 保存wifi相关
	 */
	public void saveWiFiPreferencesSetting(String serialNO, boolean wifiSwitch,
			int work_mode, WifiConfiguration wifiConfiguration) {
		List<Pair<String, String>> pairList = new ArrayList<Pair<String, String>>();
		String wifiSwitchKey = String.format("%1s.%2s", serialNO,Constants.LINK_MODE_WIFI_SWITCH);
		String workModeKey = String.format("%1s.%2s", serialNO,Constants.WIFI_WORK_MODE);
		
		
		Pair<String, String> wifiSwitchPair = new Pair<String, String>(wifiSwitchKey, Boolean.valueOf(wifiSwitch).toString());
		pairList.add(wifiSwitchPair);
		String workModeValue = null;
		try {
			workModeValue = Integer.valueOf(work_mode).toString();
		} catch (Exception e) {
			e.printStackTrace();
			workModeValue = Integer.valueOf(Constants.WIFI_WORK_MODE_UNKNOWN).toString();
		}
		
		Pair<String, String> work_modePair = new Pair<String, String>(workModeKey, workModeValue);
		pairList.add(work_modePair);
		
		if (wifiConfiguration != null && work_mode!=Constants.WIFI_WORK_MODE_WITH_AP) {
			String ssidKey = String.format("%1s.%2s", serialNO,Constants.WIFI_AP_SSID);
			String securityTypeKey = String.format("%1s.%2s", serialNO,Constants.WIFI_AP_SECURITY);
			String passwordKey = String.format("%1s.%2s", serialNO,Constants.WIFI_AP_PASSWORD);
			
			String ssid = wifiConfiguration.SSID;
			int securityType = NetworkUtil.getWiFiAccessPointSecurity(wifiConfiguration);
			String password = NetworkUtil.getWiFiAccessPointPassword(wifiConfiguration, securityType);			 
			
			if (MLog.isDebug) {
				MLog.d(TAG,String.format(" saveWiFiPreferencesSetting WifiConfiguration SSID=%s Security=%d Password=%s",
								ssid, securityType, password));
			}			
			String securityTypeValue = null;
			try {
				securityTypeValue = Integer.valueOf(securityType).toString();
			} catch (Exception e) {
				e.printStackTrace();
				securityTypeValue = Integer.valueOf(NetworkUtil.SECURITY_NONE).toString();
			}

			Pair<String, String> ssidPair = new Pair<String, String>(ssidKey,ssid);
			pairList.add(ssidPair);
			Pair<String, String> securityTypePair = new Pair<String, String>(securityTypeKey, securityTypeValue);
			pairList.add(securityTypePair);
			Pair<String, String> passwordPair = new Pair<String, String>(passwordKey, password);
			pairList.add(passwordPair);
		}
		mPropertyFileOperation.put(pairList);
	}

	/**
	 * 获取wifi相关
	 * 
	 * @return
	 */
	public boolean getWiFiSwitch(String serialNO) {
		String sWifiSwitch = mPropertyFileOperation.get(String.format("%1s.%2s", serialNO,
				Constants.LINK_MODE_WIFI_SWITCH));
		return Boolean.parseBoolean(sWifiSwitch);
	}

	public int getWiFiMode(String serialNO) {
		String int_work_mode = mPropertyFileOperation.get(String.format("%1s.%2s", serialNO,
				Constants.WIFI_WORK_MODE));
		int work_mode = Constants.WIFI_WORK_MODE_UNKNOWN;
		if (TextUtils.isEmpty(int_work_mode) == false) {
			try {
				work_mode = Integer.parseInt(int_work_mode);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return work_mode;
	}
	public String getSSID(String serialNO){
		return mPropertyFileOperation.get(String.format("%1s.%2s", serialNO, Constants.WIFI_AP_SSID));
	}
	/**
	 获取ssid对应的网络ID
	 **/
	public int getAPNetworkID(String serialNO){
		return toInt(mPropertyFileOperation.get(String.format("%1s.%2s", serialNO, Constants.WIFI_AP_NETWORK_ID)));
	}
	public int findMaxAPNetworkID(){
		List<Pair<String,String>> list = mPropertyFileOperation.getGroups(Constants.WIFI_AP_NETWORK_ID);
		int maxAPNetworkID = 1;
		for(int i=0;i<list.size();i++){
			int currentIntValue = toInt(list.get(i).second);
			if(maxAPNetworkID<currentIntValue){
				maxAPNetworkID = currentIntValue;
			}
		}
		return maxAPNetworkID;
	}
	public int toInt(String intValue){
		try {
			return Integer.parseInt(intValue);
		}
		catch (Exception e){
			e.printStackTrace();
			return 0;
		}
	}
	public void saveAPNetworkID(String serialNO,int apNetworkID){
		String apNetworkIDKey = String.format("%1s.%2s", serialNO,Constants.WIFI_AP_NETWORK_ID);
		String apNetworkIDValue = String.format("%d",apNetworkID);
		mPropertyFileOperation.put(apNetworkIDKey,apNetworkIDValue);
	}
	public String getPassword(String serialNO){
		return mPropertyFileOperation.get(String.format("%1s.%2s", serialNO, Constants.WIFI_AP_PASSWORD));
	}
	public int getSecurityType (String serialNO){
		int securityType = NetworkUtil.SECURITY_NONE;
		String sSecurityType = mPropertyFileOperation.get(String.format("%1s.%2s", serialNO,
				Constants.WIFI_AP_SECURITY));
		if (TextUtils.isEmpty(sSecurityType) == false) {
			try {
				securityType = Integer.parseInt(sSecurityType);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return securityType;
	}
	public WifiConfiguration getWiFiConfiguration(String serialNO) {
		String networkssid = mPropertyFileOperation.get(String.format("%1s.%2s", serialNO,
				Constants.WIFI_AP_SSID));
		String sSecurityType = mPropertyFileOperation.get(String.format("%1s.%2s", serialNO,
				Constants.WIFI_AP_SECURITY));
		int securityType = NetworkUtil.SECURITY_NONE;
		if (TextUtils.isEmpty(sSecurityType) == false) {
			try {
				securityType = Integer.parseInt(sSecurityType);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		String password = mPropertyFileOperation.get(String.format("%1s.%2s", serialNO,
				Constants.WIFI_AP_PASSWORD));
		if (TextUtils.isEmpty(password)){
			password="";
		}
		return DPUWiFiModeConfig.packageWifiConfiguration(networkssid,securityType, password);
	}



	/**
	 * 保存USB工作模式偏好设定
	 */
	public void saveUSBPreferencesSetting(String serialNO, int work_mode) {
		String workModeKey = String.format("%1s.%2s", serialNO,Constants.USB_WORK_MODE);
		String workModeValue = String.format("%d",work_mode);
		mPropertyFileOperation.put(workModeKey,workModeValue);
	}

	/**
	 * 获取USB工作模式偏好设定
	 * @param serialNO
	 * @return
	 */
	public int getUSBMode(String serialNO) {
		String int_work_mode = mPropertyFileOperation.get(String.format("%1s.%2s", serialNO, Constants.USB_WORK_MODE));
		int work_mode = Constants.USB_WORK_MODE_UNKNOWN;
		if (TextUtils.isEmpty(int_work_mode) == false) {
			try {
				work_mode = Integer.parseInt(int_work_mode);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return work_mode;
	}
}
