/*
    Launch Android Client, OnBluetoothListener
    Copyright (c) 2014 LAUNCH Tech Company Limited
    http:www.cnlaunch.com
*/

package com.cnlaunch.physics.listener;

import java.util.ArrayList;

import android.bluetooth.BluetoothAdapter;

import com.cnlaunch.physics.entity.BluetoothListDto;


public interface OnBluetoothListener {

	/**
	 * @param mAdapter 蓝牙适配器
	 * 
	 * @param state 状态，1支持蓝牙，2不支持蓝牙，3蓝牙可用，4蓝牙不可用, 5完成
	 * STATE_SUPPORT = 1;
	 * STATE_UN_SUPPORT = 2;
	 * STATE_ENABLED = 3;
	 * STATE_UN_ENABLED = 4;
	 * STATE_COMPLETE = 5;
	 * 
	 * 		//如果不可用，请按实际情况处理：直接打开或者去设置页面
			if (bluetoothState == STATE_UN_ENABLED) {
				//直接打开
				mBluetoothAdapter.enable();
				mBluetoothAdapter.startDiscovery();
				
				//或者去设置页面
				//Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
				//context.startActivityForResult(enableIntent, RESULT_BLUETOOTH_CODE);
			}
	 * @param list 蓝牙列表数据
	 * @param result 返回结果
	 */
	public void onBluetooth(BluetoothAdapter mAdapter, int state, ArrayList<BluetoothListDto> list, Object result);
	
	/**
	 * 通知UI某一蓝牙设备连接成功
	 * @param address
	 */
	public void onBluetoothConnSuccess(String address);
	/**
	 * 通知UI蓝牙开始扫描设备
	 */
	public void onBluetoothScanStart();
	/**
	 * 通知UI蓝牙扫描完毕
	 */
	public void onBluetoothScanFinish();
}
