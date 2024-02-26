package com.cnlaunch.physics;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.cnlaunch.physics.PropertyFileOperation;
import com.cnlaunch.physics.entity.DPUHardwareInfo;

import android.util.Pair;

/**
 * 保存下位机硬件信息，用于应用读取写入
 * 
 * @author xiefeihong
 * 
 */
public class DPUHardwareInformation {
	private static final String TAG = "DPUHardwareInformation";
	private static DPUHardwareInformation mDPUHardwareInformation = null;
	private String settingsFile;
	private PropertyFileOperation mPropertyFileOperation;
	private  DPUHardwareInfo mDPUHardwareInfo;
	/**
	 * 因为硬件信息应该存在于进程整个生命周期，所以采用单例实现方式
	 * @param PackagePath 产品包目录 一般使用PathUtils.getPackagePath()传入
	 * @return
     */
	public static DPUHardwareInformation getInstance(String PackagePath) {
		if (mDPUHardwareInformation == null)
			mDPUHardwareInformation = new DPUHardwareInformation(PackagePath);
		return mDPUHardwareInformation;
	}

	private DPUHardwareInformation(String PackagePath) {
		settingsFile = PackagePath
				+ File.separator + "dpu_hardware_information.ini";
		mPropertyFileOperation = new PropertyFileOperation(settingsFile);
		mDPUHardwareInfo = new DPUHardwareInfo();
		mDPUHardwareInfo.setId(mPropertyFileOperation.get(DPUHardwareInfo.ID));
		mDPUHardwareInfo.setSerialNo(mPropertyFileOperation.get(DPUHardwareInfo.SERIAL_NO));
		mDPUHardwareInfo.setVersion(mPropertyFileOperation.get(DPUHardwareInfo.VERSION));
		mDPUHardwareInfo.setDate(mPropertyFileOperation.get(DPUHardwareInfo.DATE));
		mDPUHardwareInfo.setDeviceType(mPropertyFileOperation.get(DPUHardwareInfo.DEVICE_TYPE));
	}

	/**
	 * 保存DPU硬件信息
	 * @param dpuHardwareInfo
     */
	public void putDPUHardwareInfo(DPUHardwareInfo dpuHardwareInfo){
		List<Pair<String, String>> pairList = new ArrayList<Pair<String, String>>();

		Pair<String, String> idPair = new Pair<String, String>(DPUHardwareInfo.ID, dpuHardwareInfo.getId());
		pairList.add(idPair);

		Pair<String, String> serialNoPair = new Pair<String, String>(DPUHardwareInfo.SERIAL_NO, dpuHardwareInfo.getSerialNo());
		pairList.add(serialNoPair);

		Pair<String, String> versionPair = new Pair<String, String>(DPUHardwareInfo.VERSION, dpuHardwareInfo.getVersion());
		pairList.add(versionPair);

		Pair<String, String> datePair = new Pair<String, String>(DPUHardwareInfo.DATE, dpuHardwareInfo.getDate());
		pairList.add(datePair);

		Pair<String, String> deviceTypePair = new Pair<String, String>(DPUHardwareInfo.DEVICE_TYPE, dpuHardwareInfo.getDeviceType());
		pairList.add(deviceTypePair);
		mPropertyFileOperation.put(pairList);
		mDPUHardwareInfo.setId(mPropertyFileOperation.get(DPUHardwareInfo.ID));
		mDPUHardwareInfo.setSerialNo(mPropertyFileOperation.get(DPUHardwareInfo.SERIAL_NO));
		mDPUHardwareInfo.setVersion(mPropertyFileOperation.get(DPUHardwareInfo.VERSION));
		mDPUHardwareInfo.setDate(mPropertyFileOperation.get(DPUHardwareInfo.DATE));
		mDPUHardwareInfo.setDeviceType(mPropertyFileOperation.get(DPUHardwareInfo.DEVICE_TYPE));
	}
	public DPUHardwareInfo getDPUHardwareInfo(){
		return mDPUHardwareInfo;
	}

}
