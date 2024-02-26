package com.cnlaunch.physics.listener;

import com.cnlaunch.physics.wifi.DPUWiFiAPConfig;
import com.cnlaunch.physics.wifi.DPUWiFiModeConfig;


/**
 * OnWiFiModeListener wifi通讯模式相关的监听
 * [A brief description]
 * 
 * @author xiefeihong
 * @version 1.0
 * @date 2016-10-26
 * 
 *
 */
public interface OnWiFiModeListener {
	/**
	 * 生成或者获取wifi模式成功
	 */
	public static final int WIFI_MODE_CONFIG_SUCCESS  = 0;
	/**
	 * 生成或者获取wifi模式失败
	 */
	public static final int WIFI_MODE_CONFIG_FAIL  = 1;
	
	public static final int WIFI_TEST_SEND_DATAGRAM  = 2;
	public static final int WIFI_TEST_RECEIVE_DATAGRAM  = 3;
	public static final int WIFI_TEST_CONNECT_START  = 4;
	public static final int WIFI_TEST_CONNECT_SUCCESS  = 5;
	public static final int WIFI_TEST_CONNECT_FAIL  = 6;
	
	/**
	 * 生成或者获取wifi模式 跳转到boot模式失败
	 */
	public static final int DPU_SWITCH_MODE_FAIL  = 7;
	/**
	 * 设置wifi配置相关的监听
	 * @param state
	 */
	public void OnSetWiFiModeConfigListener(int state);
	/**
	 * 获取wifi配置相关的监听
	 * @param state
	 */
	public void OnGetWiFiModeConfigListener(int state,DPUWiFiModeConfig config);

	/**
	 * 设置wifi热点配置相关的监听
	 * @param state
	 */
	public void OnSetWiFiAPConfigListener(int state);
	/**
	 * 获取wifi热点信息相关的监听
	 * @param state
	 */
	public void OnGetWiFiAPConfigListener(int state, DPUWiFiAPConfig dpuWiFiAPConfig);
	/**
	 * wifi测试相关的监听
	 * @param state
	 * @param description
	 */
	public void OnTestWiFiModeWorkListener(int state, String description);
	
	
}
