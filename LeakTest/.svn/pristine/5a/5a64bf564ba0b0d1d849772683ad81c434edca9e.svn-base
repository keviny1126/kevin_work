package com.cnlaunch.physics.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
public class PhysicsCommonUtils {

	/**
	 * 根据key获取config.properties里面的值
	 * 
	 * @param context
	 * @param key
	 * @return
	 */
	public static String getProperty(Context context, String key) {
		return ConfigPropertiesOperation.getInstance(context).getProperty(key);
	}

	public static String getVersion(Context context) {
		try {
			PackageManager manager = context.getPackageManager();
			PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
			String version = info.versionName;
			return "V" + version;
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}
}
