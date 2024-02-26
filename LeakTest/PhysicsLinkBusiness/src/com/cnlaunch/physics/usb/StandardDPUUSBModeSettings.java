package com.cnlaunch.physics.usb;

import com.cnlaunch.physics.impl.IPhysics;
import com.cnlaunch.physics.listener.OnUSBModeListener;

public class StandardDPUUSBModeSettings implements IUSBModeSettings{
	private final static String  TAG = "StandardDPUUSBModeSettings";
	public StandardDPUUSBModeSettings(){
	}

	@Override
	public void setDPUUSBModeAsync(IPhysics iPhysics, OnUSBModeListener onUSBModeListener, int workMode) {

	}

	@Override
	public void getDPUUSBModeAsync(IPhysics iPhysics, OnUSBModeListener onUSBModeListener) {

	}
}
