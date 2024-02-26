package com.cnlaunch.physics.entity;

import android.bluetooth.BluetoothDevice;

/**
 * 蓝牙对象实体类
 * [A brief description]
 * 
 * @author bichuanfeng
 * @version 1.0
 * @date 2014-3-8
 * 
 *
 */
public class BluetoothListDto {
	String bluetoothName;
	String bluetoothAddress;
	String bluetoothPairWait;
	String bluetoothConnWait;
	boolean bluetoothConnStatus;
	boolean bluetoothPairStatus;
	BluetoothDevice bluetoothDevice;
	private int rssi;
	public BluetoothListDto() {
		rssi=Short.MIN_VALUE;
	}
	public String getBluetoothName() {
		return bluetoothName;
	}
	public void setBluetoothName(String bluetoothName) {
		this.bluetoothName = bluetoothName;
	}
	public String getBluetoothAddress() {
		return bluetoothAddress;
	}
	public void setBluetoothAddress(String bluetoothAddress) {
		this.bluetoothAddress = bluetoothAddress;
	}
	public boolean isBluetoothConnStatus() {
		return bluetoothConnStatus;
	}
	public void setBluetoothConnStatus(boolean bluetoothConnStatus) {
		this.bluetoothConnStatus = bluetoothConnStatus;
	}
	public boolean isBluetoothPairStatus() {
		return bluetoothPairStatus;
	}
	public void setBluetoothPairStatus(boolean bluetoothPairStatus) {
		this.bluetoothPairStatus = bluetoothPairStatus;
	}
	public BluetoothDevice getBluetoothDevice() {
		return bluetoothDevice;
	}
	public void setBluetoothDevice(BluetoothDevice bluetoothDevice) {
		this.bluetoothDevice = bluetoothDevice;
	}
	public String getBluetoothPairWait() {
		return bluetoothPairWait;
	}
	public void setBluetoothPairWait(String bluetoothPairWait) {
		this.bluetoothPairWait = bluetoothPairWait;
	}
	public String getBluetoothConnWait() {
		return bluetoothConnWait;
	}
	public void setBluetoothConnWait(String bluetoothConnWait) {
		this.bluetoothConnWait = bluetoothConnWait;
	}
	public int getRssi() {
		return rssi;
	}

	public void setRssi(int rssi) {
		this.rssi = rssi;
	}
}
