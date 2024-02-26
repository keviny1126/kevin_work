package com.cnlaunch.physics.wifi;

import com.cnlaunch.physics.impl.IPhysics;
import com.cnlaunch.physics.listener.OnWiFiModeListener;

/**
 * 因为历史原因，接口类名无法修改
 * 支持WIFI的一系列操作
 */
public interface IWiFiModeSettings {
	/**
	 * 异步设置wifi工作模式
	 * @param dpuWiFiModeConfig
	 */
	void setDPUWiFiModeAsync(IPhysics iPhysics, OnWiFiModeListener onWiFiModeListener,DPUWiFiModeConfig dpuWiFiModeConfig);
	/**
	 * 异步获取wifi工作模式
	 *
	 */
	void getDPUWiFiModeAsync(IPhysics  iPhysics,OnWiFiModeListener onWiFiModeListener);

	/**
	 * 异步设置wifi热点 信道
	 */
	void setDPUWiFiAPConfigAsync(IPhysics iPhysics, OnWiFiModeListener onWiFiModeListener,DPUWiFiAPConfig dpuWiFiAPConfig);
	/**
	 * 异步获取wifi热点配置
	 * *
	 */
	void getDPUWiFiAPConfigAsync(IPhysics  iPhysics,OnWiFiModeListener onWiFiModeListener);
}
