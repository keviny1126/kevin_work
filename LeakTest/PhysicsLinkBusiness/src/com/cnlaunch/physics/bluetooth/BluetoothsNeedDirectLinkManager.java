package com.cnlaunch.physics.bluetooth;

import java.util.HashMap;
import java.util.Map;
/*用于记录蓝牙是否需要强制使用直连，
 * 用于sdp协议查找蓝牙可用channel失败时的特别处理。
 */
public class BluetoothsNeedDirectLinkManager {
	private Map<String, Boolean> mBluetoothsNeedDirectLinkStateMap;
	private static BluetoothsNeedDirectLinkManager mBluetoothsNeedDirectLinkManager=null;
	public static BluetoothsNeedDirectLinkManager getInstance() {
		if (mBluetoothsNeedDirectLinkManager == null) {
			mBluetoothsNeedDirectLinkManager = new BluetoothsNeedDirectLinkManager();
		}
		return mBluetoothsNeedDirectLinkManager;
	}
	public BluetoothsNeedDirectLinkManager(){
		mBluetoothsNeedDirectLinkStateMap  = new HashMap<String, Boolean>();
	}
	/**
	 * 设置蓝牙是否需要直连
	 * @param isNeed
	 */
	public void setBluetoothNeedDirectLinkState(String serialNo,boolean isNeed){
		mBluetoothsNeedDirectLinkStateMap.put(serialNo, isNeed);
	}
	public boolean getBluetoothNeedDirectLinkState(String serialNo){
		Boolean state = mBluetoothsNeedDirectLinkStateMap.get(serialNo);
		if(state!=null){
			return state;
		}
		else {
			return false;
		}
	}
}
