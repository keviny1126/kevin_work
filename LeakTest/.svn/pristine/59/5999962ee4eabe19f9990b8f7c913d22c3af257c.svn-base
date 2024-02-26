package com.cnlaunch.physics.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.cnlaunch.physics.entity.DPUHardwareInfo;
import com.cnlaunch.physics.entity.DPUSoftInfo;

/**
 * 保存DPU接头信息
 * 
 * @author chengminghui
 * 
 */
public class DeviceProperties {
	private static final String ACTIVATE_TIME = "activateTime";
	private static final String BLACKLIST_STATE = "blacklistState";
	private static final String STOP_STATE = "stopState";
	/** 默认的保存目录 sdcard/cnlaunch/X-431 PAD II/ 目录下 **/
	private static String PACKAGE_PATH="X-431 PAD II";
	private static String BASE_PARH = Environment
			.getExternalStorageDirectory().getAbsolutePath()
			+ File.separator
			+ "cnlaunch" + File.separator + PACKAGE_PATH + File.separator;
	/** 保存的文件名 **/
	private static final String FILE_NAME = "deviceInfo";
	private Properties mProperties;
	private String mPath;
	private static Map<String, DeviceProperties> dpMap = new HashMap<String, DeviceProperties>();
	private String mSerialNo;

	public static DeviceProperties getInstance(String serialNo) {
		return getInstance(serialNo, null);
	}

	public static DeviceProperties getInstance(String serialNo, String baseDir) {
		if (!dpMap.containsKey(serialNo)) {
			DeviceProperties dp = new DeviceProperties(serialNo, baseDir);
			dpMap.put(serialNo, dp);
		}
		return dpMap.get(serialNo);
	}

	/**
	 * 
	 * @param serialNo
	 *            序列号
	 * @param baseDir
	 *            保存信息文件的基础目录
	 */
	private DeviceProperties(String serialNo, String baseDir) {
		this.mSerialNo = serialNo;
		mPath = getPath(serialNo, baseDir);
		mProperties = new Properties();

	}

	public static void setetPACKAGE_PATH(String package_path)
	{
		PACKAGE_PATH=package_path;
	}

	private String getPath(String serialNo, String baseDir) {
		String path = "";
		if (baseDir == null) {
			BASE_PARH = Environment.getExternalStorageDirectory().getAbsolutePath()+ File.separator + "cnlaunch" + File.separator + PACKAGE_PATH + File.separator;
			path = BASE_PARH + serialNo + File.separator + FILE_NAME;
		} else {
			path = baseDir + File.separator + serialNo + File.separator
					+ FILE_NAME;
		}
		return path;
	}

	private void load() {
		FileInputStream fis = null;
		try {
			File file = new File(mPath);
			File dir = file.getParentFile();
			if (!dir.exists()) {
				dir.mkdirs();
			}
			if (!file.exists()) {
				file.createNewFile();
			}
			fis = new FileInputStream(file);
			mProperties.load(fis);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				if (fis != null) {
					fis.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * 是否本地存储了接头硬件版本信息
	 * 
	 * @return
	 */
	public boolean hasHardwareInfo() {
		DPUHardwareInfo info = getDPUHardwareInfo();
		if (info == null) {
			return false;
		}
		return true;
	}

	/**
	 * 是否本地存储了接头软件版本信息
	 * 
	 * @return
	 */
	public boolean hasSoftInfo() {
		DPUSoftInfo info = getDPUSoftInfo();
		if(info == null){
			return false;
		}
		return true;
	}

	public void saveDPUHardwareInfo(DPUHardwareInfo info) {
		if (info == null) {
			return;
		}
		mProperties.setProperty(DPUHardwareInfo.ID, info.getId());
		mProperties.setProperty(DPUHardwareInfo.SERIAL_NO, info.getSerialNo());
		mProperties.setProperty(DPUHardwareInfo.VERSION, info.getVersion());
		mProperties.setProperty(DPUHardwareInfo.DATE, info.getDate());
		mProperties.setProperty(DPUHardwareInfo.DEVICE_TYPE,
				info.getDeviceType());
		store();
	}

	public DPUHardwareInfo getDPUHardwareInfo() {
		DPUHardwareInfo info = null;
		load();
		info = new DPUHardwareInfo();
		info.setId(mProperties.getProperty(DPUHardwareInfo.ID));
		info.setSerialNo(mProperties.getProperty(DPUHardwareInfo.SERIAL_NO));
		info.setVersion(mProperties.getProperty(DPUHardwareInfo.VERSION));
		info.setDate(mProperties.getProperty(DPUHardwareInfo.DATE));
		info.setDeviceType(mProperties.getProperty(DPUHardwareInfo.DEVICE_TYPE));
		if (info.isEmpty()) {
			return null;
		}
		return info;
	}

	public void saveDPUSoftInfo(DPUSoftInfo info) {
		if (info == null) {
			return;
		}
		mProperties
				.setProperty(DPUSoftInfo.BOOT_VERSION, info.getBootVersion());
		mProperties.setProperty(DPUSoftInfo.DOWNLOAD_VERSION,
				info.getDownloadSersion());
		mProperties.setProperty(DPUSoftInfo.DIAFNOSE_SOFT_VERSION,
				info.getDiagnoseSoftVersion());
		mProperties.setProperty(DPUSoftInfo.PRODUCT_FUNCTION_VERSION,
				info.getProductFunctionVersion());
		mProperties.setProperty(DPUSoftInfo.BOOT_103_VERSION,
				info.getBoot103Version());

		store();
	}

	public DPUSoftInfo getDPUSoftInfo() {
		DPUSoftInfo info = null;
		load();
		info = new DPUSoftInfo();
		info.setBootVersion(mProperties.getProperty(DPUSoftInfo.BOOT_VERSION));
		info.setDownloadSersion(mProperties
				.getProperty(DPUSoftInfo.DOWNLOAD_VERSION));
		info.setDiagnoseSoftVersion(mProperties
				.getProperty(DPUSoftInfo.DIAFNOSE_SOFT_VERSION));
		info.setProductFunctionVersion(mProperties
				.getProperty(DPUSoftInfo.PRODUCT_FUNCTION_VERSION));
		info.setBoot103Version(mProperties
				.getProperty(DPUSoftInfo.BOOT_103_VERSION));

		if(info.isEmpty()){
			return null;
		}
		return info;
	}

	private void store() {
		FileOutputStream fos = null;
		try {
			File file = new File(mPath);
			File dir = file.getParentFile();
			if (!dir.exists()) {
				dir.mkdirs();
			}
			if (!file.exists()) {
				file.createNewFile();
			}
			fos = new FileOutputStream(mPath);
			mProperties.store(fos, "utf-8");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				if (fos != null) {
					fos.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void saveActivateTime(String activateTime) {
		mProperties.setProperty(ACTIVATE_TIME, activateTime);
		store();
	}

	public String getActivateTime() {
		load();
		return mProperties.getProperty(ACTIVATE_TIME);
	}
	public void saveDownloadVersion(String downloadVersion) {
		if (TextUtils.isEmpty(downloadVersion)) {
			return;
		}
		mProperties.setProperty(DPUSoftInfo.DOWNLOAD_VERSION, downloadVersion);
		store();
	}
	public String getDownloadVersion() {
		load();
		return mProperties.getProperty(DPUSoftInfo.DOWNLOAD_VERSION);
	}
	public void saveHardwareVersion(String hardwareVersion) {
		if (TextUtils.isEmpty(hardwareVersion)) {
			return;
		}
		mProperties.setProperty(DPUHardwareInfo.VERSION, hardwareVersion);
		store();
	}
	public String getHardwareVersion() {
		load();
		return mProperties.getProperty(DPUHardwareInfo.VERSION);
	}
	public void saveSerialState(String state) {
		mProperties.setProperty(BLACKLIST_STATE, state);
		store();
	}
	public void saveSerialNoStopState(String state) {
		mProperties.setProperty(STOP_STATE, state);
		store();
	}

	public String getSerialState() {
		load();
		return mProperties.getProperty(BLACKLIST_STATE);
	}
	public String getSerialNoStopState() {
		load();
		return mProperties.getProperty(STOP_STATE);
	}
}
