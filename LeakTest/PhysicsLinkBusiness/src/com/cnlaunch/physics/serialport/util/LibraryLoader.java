/*
    Launch Android Client, LibraryLoader
    Copyright (c) 2014 LAUNCH Tech Company Limited
    http:www.cnlaunch.com
 */

package com.cnlaunch.physics.serialport.util;
import com.cnlaunch.physics.utils.MLog;
/**
 * [动态库加载工具类]
 * 
 * @author zengdengyi
 * @version 1.0
 * @date 2014-3-3
 * 
 **/
public class LibraryLoader {
	static final String TAG = "LibLoader";
	static public void load(String name) {
		final String LD_PATH = System.getProperty("java.library.path");
		MLog.d(TAG, "Trying to load library " + name + " from LD_PATH: "+ LD_PATH);
		try {
			System.loadLibrary(name);
		} catch (UnsatisfiedLinkError e) {
			MLog.e(TAG, e.toString());
		}
	}
}
