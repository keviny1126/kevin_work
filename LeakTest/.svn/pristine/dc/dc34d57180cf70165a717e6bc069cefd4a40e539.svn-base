package com.cnlaunch.physics.bluetooth.ble;

import java.util.List;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.os.Handler;

public class BluetoothLeScannerManager {
	private BluetoothLeScanner mBluetoothLeScanner;
	private Handler mHandler;
	private BluetoothLeScannerManagerCallBack mBluetoothLeScannerManagerCallBack;
	public BluetoothLeScannerManager(BluetoothAdapter bluetoothAdapter,BluetoothLeScannerManagerCallBack bluetoothLeScannerManagerCallBack){
		mBluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
		mBluetoothLeScannerManagerCallBack = bluetoothLeScannerManagerCallBack;
	}
	public void startScan() {
		if (mBluetoothLeScanner != null) {
			mBluetoothLeScanner.startScan(mScanCallback);
			if (mBluetoothLeScannerManagerCallBack != null) {
				mBluetoothLeScannerManagerCallBack.onStartScan();
			}
		}
		else{
			stopScan();
		}

	}
	public void	stopScan(){
		if(mBluetoothLeScanner != null) {
			mBluetoothLeScanner.stopScan(mScanCallback);
		}
		if(mBluetoothLeScannerManagerCallBack!=null){
			mBluetoothLeScannerManagerCallBack.onStopScan();
		}
	}
	private ScanCallback mScanCallback = new ScanCallback(){

		@Override
		public void onScanResult(int callbackType, ScanResult result) {
			if(mBluetoothLeScannerManagerCallBack!=null){
				mBluetoothLeScannerManagerCallBack.onScanResult(result.getDevice(),result.getRssi());
			}
		}

		@Override
		public void onBatchScanResults(List<ScanResult> results) {
			if(mBluetoothLeScannerManagerCallBack!=null){
				for(ScanResult scanResult :results){
					mBluetoothLeScannerManagerCallBack.onScanResult(scanResult.getDevice(),scanResult.getRssi());
				}
			}
			
		}

		@Override
		public void onScanFailed(int errorCode) {
			if(mBluetoothLeScannerManagerCallBack!=null){
				mBluetoothLeScannerManagerCallBack.onScanFailed(errorCode);
			}
		}
		
	};
	public interface BluetoothLeScannerManagerCallBack{
		void onStartScan();
		void onStopScan();
		void onScanResult(BluetoothDevice bluetoothDevice,int rssi);
		void onScanFailed(int errorCode);
	}
}
