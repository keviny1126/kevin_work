package com.cnlaunch.physics.usb;

import com.cnlaunch.physics.impl.IPhysics;
import com.cnlaunch.physics.listener.OnUSBModeListener;

/**
 * 因为历史原因，接口类名无法修改
 * 支持USB的一系列操作
 */
public interface IUSBModeSettings {
	/**
	 * 异步设置usb工作模式
	 */
	void setDPUUSBModeAsync(IPhysics iPhysics, OnUSBModeListener onUSBModeListener,int workMode);
	/**
	 * 异步获取usb工作模式
	 *
	 */
	void getDPUUSBModeAsync(IPhysics  iPhysics,OnUSBModeListener onUSBModeListener);
}
