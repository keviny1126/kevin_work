package com.cnlaunch.physics.utils;
import android.content.Context;

/**
 * 下位机硬件信息
 * 当序列号为空时，需要连接接头，读取下位机信息
 * @author xiefeihong
 * 
 */
public class ConfigPropertiesOperation extends AssertPropertyFileOperation {
	private static ConfigPropertiesOperation mConfigPropertiesOperation = null;
	public static ConfigPropertiesOperation getInstance(Context context) {
		if (mConfigPropertiesOperation == null)
			mConfigPropertiesOperation = new ConfigPropertiesOperation(context);
		return mConfigPropertiesOperation;
	}
	private ConfigPropertiesOperation(Context context) {
		super(context,"config.properties");
	}
}
