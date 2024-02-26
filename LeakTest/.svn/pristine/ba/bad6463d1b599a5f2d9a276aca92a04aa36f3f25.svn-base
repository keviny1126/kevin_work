package com.cnlaunch.physics.bluetooth.remote;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import com.cnlaunch.physics.utils.MLog;
import android.bluetooth.BluetoothDevice;
import android.os.Build;

/**
 * 蓝牙配对工具类
 * 
 * @author zhangshengda
 * @date 2014-8-26
 */

public class PairUtils {

	/**
	 * 与设备配对 参考源码：platform/packages/apps/Settings.git
	 * /Settings/src/com/android/settings/bluetooth/CachedBluetoothDevice.java
	 */
	static public boolean createBond(Class btClass, BluetoothDevice btDevice) throws Exception {
		Method createBondMethod = btClass.getMethod("createBond");
		Boolean returnValue = (Boolean) createBondMethod.invoke(btDevice);
		return returnValue.booleanValue();
	}

	/**
	 * 与设备解除配对 参考源码：platform/packages/apps/Settings.git
	 * /Settings/src/com/android/settings/bluetooth/CachedBluetoothDevice.java
	 */
	static public boolean removeBond(Class btClass, BluetoothDevice btDevice) throws Exception {
		Method removeBondMethod = btClass.getMethod("removeBond");
		Boolean returnValue = (Boolean) removeBondMethod.invoke(btDevice);
		return returnValue.booleanValue();
	}

	static public boolean setPin(Class btClass, BluetoothDevice btDevice, String str) throws Exception {
		try {
			Method removeBondMethod = btClass.getDeclaredMethod("setPin", new Class[] { byte[].class });
			Boolean returnValue = (Boolean) removeBondMethod.invoke(btDevice, new Object[] { str.getBytes() });
		} catch (SecurityException e) {
			// throw new RuntimeException(e.getMessage());
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// throw new RuntimeException(e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;

	}

	/** 取消用户输入 */
	static public boolean cancelPairingUserInput() throws Exception {

		Class<?> clazz = BluetoothDevice.class;// class name goes here
		// instance call
		Constructor<?> ctor = clazz.getConstructor();
		Object instance = ctor.newInstance();

		Method cancelPairing = clazz.getMethod("cancelPairingUserInput");
		boolean isBoolean = (Boolean) cancelPairing.invoke(instance);
		return isBoolean;
	}

	// 取消配对
	static public boolean cancelBondProcess(Class btClass, BluetoothDevice device)

	throws Exception {
		Method createBondMethod = btClass.getMethod("cancelBondProcess");
		Boolean returnValue = (Boolean) createBondMethod.invoke(device);
		return returnValue.booleanValue();
	}

	/**
	 * 
	 * @param clsShow
	 */
	static public void printAllInform(Class clsShow) {
		try {
			// 取得所有方法
			Method[] hideMethod = clsShow.getMethods();
			int i = 0;
			for (; i < hideMethod.length; i++) {
				// Log.e("method name", hideMethod[i].getName() +
				// ";and the i is:" + i);
			}
			// 取得所有常量
			Field[] allFields = clsShow.getFields();
			for (i = 0; i < allFields.length; i++) {
				// Log.e("Field name", allFields[i].getName());
			}
		} catch (SecurityException e) {
			// throw new RuntimeException(e.getMessage());
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// throw new RuntimeException(e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void createDeviceBond(BluetoothDevice btDevice){
		int sdk = Build.VERSION.SDK_INT;
		String strPsw = "0000";		
		if (sdk <= 15) 
		{
			try {
				setPin(btDevice.getClass(), btDevice, strPsw); // 手机和蓝牙采集器配对
				if (createBond(btDevice.getClass(), btDevice)) {
					// 配对成功
				}
				cancelPairingUserInput();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} 
		else {
			try {
				setPin(btDevice.getClass(), btDevice, strPsw); // 手机和蓝牙采集器配对
				MLog.d("PairUtils", "Success to add the PIN.");
				try {
					btDevice.getClass().getMethod("setPairingConfirmation",boolean.class).invoke(btDevice, true);
					MLog.d("PairUtils","Success to setPairingConfirmation.");
				} catch (Exception e) {
					e.printStackTrace();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}