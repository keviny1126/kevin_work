package com.cnlaunch.physics;

import android.text.TextUtils;

import java.io.File;
import com.cnlaunch.physics.utils.Constants;

/**
 * DPU 接头序列号，下位机版本管理，一个序列号对应一个下位机版本
 * 增加Smartbox3.0 升级的应用软件和系统版本号记录
 * @author xiefeihong
 *
 */
public class DPUDownloadbinVersionManager{
	private static final String TAG = "DPUDownloadbinVersionManager";
	private String settingsFile;
	private PropertyFileOperation mPropertyFileOperation;
	private static DPUDownloadbinVersionManager mDPUDownloadbinVersionManager = null;
	private static final String SMARTBOX30_SYSTEM_VERSION = "smartbox30_system_version";
	private static final String SMARTBOX30_APPLICATION_VERSION = "smartbox30_application_version";
	private static final String COMPOSITE_TPMS_DOWNLOADBIN_VERSION = "composite_tpms_downloadbin_version";
	private static final String COMPOSITE_TPMS_BOOT_VERSION = "composite_tpms_boot_version";
	/**
	 * SMARTBOX30升级支持标记，0 或者“”表示未判断，大于0 表示支持系统升级
	 */
	private static final String SMARTBOX30_SUPPORT_UPDATE_FLAG = "smartbox30_support_update_flag";
	/**
	 * 因为接头下位机版本信息应该存在于进程整个生命周期，所以采用单例实现方式
	 * @param packagePath 产品包目录 一般使用PathUtils.getPackagePath()传入
	 * @return
	 */
	public static DPUDownloadbinVersionManager getInstance(String packagePath) {
		if (mDPUDownloadbinVersionManager == null)
			mDPUDownloadbinVersionManager = new DPUDownloadbinVersionManager(packagePath);
		return mDPUDownloadbinVersionManager;
	}
	private DPUDownloadbinVersionManager(String PackagePath){
		settingsFile = PackagePath + File.separator + Constants.DPU_DOWNLOADBIN_INFORMATION;
		mPropertyFileOperation = new PropertyFileOperation(settingsFile);
	}
	public String getDownloadbinVersion(String serialNo) {
		if(DeviceFactoryManager.getInstance().isSimulatorDiagnose()){
			//模拟通讯
			return "V99.99";
		}
		else {
			String downloadbinVersion = mPropertyFileOperation.get(serialNo);
			if (downloadbinVersion == null) {
				downloadbinVersion = "";
			}
			return downloadbinVersion;
		}
	}
	public  void  setDownloadbinVersion(String serialNo,String downloadbinVersion) {
		if(DeviceFactoryManager.getInstance().isSimulatorDiagnose()){
			//模拟通讯
			return;
		}
		if(downloadbinVersion!=null) {
			mPropertyFileOperation.put(serialNo, downloadbinVersion);
		}
	}

	/**
	 * 获取混合TPMS模组下位机版本
	 * @param serialNo
	 * @return
	 */
	public String getCompositeTPMSDownloadbinVersion(String serialNo) {
		String compositeTpmsDownloadbinVersionKey = String.format("%s.%s",serialNo,COMPOSITE_TPMS_DOWNLOADBIN_VERSION);
		String compositeTpmsDownloadbinVersion = mPropertyFileOperation.get(compositeTpmsDownloadbinVersionKey);
		if(compositeTpmsDownloadbinVersion == null) {
			compositeTpmsDownloadbinVersion = "";
		}
		return compositeTpmsDownloadbinVersion;
	}

	/**
	 * 设置混合TPMS模组下位机版本
	 * @param serialNo
	 * @param compositeTpmsDownloadbinVersion
	 */
	public  void  setCompositeTPMSDownloadbinVersion(String serialNo,String compositeTpmsDownloadbinVersion) {
		String compositeTpmsDownloadbinVersionKey = String.format("%s.%s",serialNo,COMPOSITE_TPMS_DOWNLOADBIN_VERSION);
		if(compositeTpmsDownloadbinVersion!=null) {
			mPropertyFileOperation.put(compositeTpmsDownloadbinVersionKey, compositeTpmsDownloadbinVersion);
		}
	}
	/**
	 * 获取混合TPMS模组BOOT版本
	 * @param serialNo
	 * @return
	 */
	public String getCompositeTPMSBootVersion(String serialNo) {
		String compositeTpmsBootVersionKey = String.format("%s.%s",serialNo,COMPOSITE_TPMS_BOOT_VERSION);
		String compositeTpmsBootVersion = mPropertyFileOperation.get(compositeTpmsBootVersionKey);
		if(compositeTpmsBootVersion == null) {
			compositeTpmsBootVersion = "";
		}
		return compositeTpmsBootVersion;
	}

	/**
	 * 设置混合TPMS模组BOOT版本
	 * @param serialNo
	 * @param compositeTpmsBootVersion
	 */
	public  void  setCompositeTPMSBootVersion(String serialNo,String compositeTpmsBootVersion) {
		String compositeTpmsBootVersionKey = String.format("%s.%s",serialNo,COMPOSITE_TPMS_BOOT_VERSION);
		if(compositeTpmsBootVersion!=null) {
			mPropertyFileOperation.put(compositeTpmsBootVersionKey, compositeTpmsBootVersion);
		}
	}
	/**
	 * 获取Smartbox30 System Version
	 * @param serialNo
	 * @return
	 */
	public String getSmartbox30SystemVersion(String serialNo) {
		String smartbox30SystemVersionKey = String.format("%s.%s",serialNo,SMARTBOX30_SYSTEM_VERSION);
		String version = mPropertyFileOperation.get(smartbox30SystemVersionKey);
		if(version == null) {
			version = "";
		}
		return version;
	}

	/**
	 * 设置Smartbox30 System Version
	 * @param serialNo
	 * @param smartbox30SystemVersion
	 */
	public  void  setSmartbox30SystemVersion(String serialNo,String smartbox30SystemVersion) {
		if(smartbox30SystemVersion!=null) {
			String smartbox30SystemVersionKey = String.format("%s.%s",serialNo,SMARTBOX30_SYSTEM_VERSION);
			mPropertyFileOperation.put(smartbox30SystemVersionKey, smartbox30SystemVersion);
		}
	}
	/**
	 * 获取Smartbox30 Application Version
	 * @param serialNo
	 * @return
	 */
	public String getSmartbox30ApplicationVersion(String serialNo) {
		String smartbox30ApplicationVersionKey = String.format("%s.%s",serialNo,SMARTBOX30_APPLICATION_VERSION);
		String version = mPropertyFileOperation.get(smartbox30ApplicationVersionKey);
		if(version == null) {
			version = "";
		}
		return version;
	}

	/**
	 * 设置Smartbox30 Application Version
	 * @param serialNo
	 * @param smartbox30ApplicationVersion
	 */
	public  void  setSmartbox30ApplicationVersion(String serialNo,String smartbox30ApplicationVersion) {
		if(smartbox30ApplicationVersion!=null) {
			String smartbox30ApplicationVersionKey = String.format("%s.%s",serialNo,SMARTBOX30_APPLICATION_VERSION);
			mPropertyFileOperation.put(smartbox30ApplicationVersionKey, smartbox30ApplicationVersion);
		}
	}
	/**
	 * 获取Smartbox30 Support System Update Flag
	 * SMARTBOX30升级支持标记，0 或者“”表示未判断，大于0 表示支持系统升级,小于0表示不支持
	 * @param serialNo
	 * @return
	 */
	public int  getSmartbox30SupportUpdateFlag(String serialNo) {
		String smartbox30SupportUpdateFlagKey = String.format("%s.%s",serialNo,SMARTBOX30_SUPPORT_UPDATE_FLAG);
		String flag = mPropertyFileOperation.get(smartbox30SupportUpdateFlagKey);
		if(TextUtils.isEmpty(flag)) {
			return 0;
		}
		else{
			return Integer.parseInt(flag);
		}
	}
	/**
	 * 设置Smartbox30 Support System Update Flag
	 * SMARTBOX30升级支持标记，0 或者“”表示未判断，大于0 表示支持系统升级,小于0表示不支持
	 * @param serialNo
	 * @return
	 */
	public  void  setSmartbox30SupportUpdateFlag(String serialNo,String smartbox30SupportUpdateFlag) {
		if(smartbox30SupportUpdateFlag!=null) {
			String smartbox30SupportUpdateFlagKey = String.format("%s.%s",serialNo,SMARTBOX30_SUPPORT_UPDATE_FLAG);
			mPropertyFileOperation.put(smartbox30SupportUpdateFlagKey, smartbox30SupportUpdateFlag);
		}
	}
	/*
	public void dumpAllDPUDownloadbinVersion(){
		try{
			if (MLog.isDebug)  {
				MLog.d(TAG, "dumpAllDPUDownloadbinVersion");
				for (ConcurrentHashMap.Entry<String, String> entry : mDownloadbinVersionMap.entrySet()) {
					MLog.d(TAG, "dumpAllDPUDownloadbinVersion  serialNo = " + entry.getKey() + ", downloadVersion = " + entry.getValue());
				}
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}*/
}
