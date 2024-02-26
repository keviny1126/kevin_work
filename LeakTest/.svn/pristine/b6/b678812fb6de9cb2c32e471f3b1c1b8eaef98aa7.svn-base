package com.cnlaunch.physics.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.os.Environment;
import android.util.Log;

/**
 * 日志工具
 * 
 * @author xiefeihong
 * @date 2014-5-27
 */
public class MLog {
	private static boolean flag = false;
	public static boolean isDebug = false;
	private static Boolean MYLOG_WRITE_TO_FILE=false;
	private static String MYLOG_PATH_SDCARD_DIR=Environment.getExternalStorageDirectory().getPath()+"/cnlaunch/mlog.txt";
	private static SimpleDateFormat logfile = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS");
	public static void d(String tag, String log) {
		if (flag){
			Log.d(tag, log);
			writeLogtoFile(String.valueOf('d'), tag, log);
		}
	}

	public static void e(String tag, String log) {
		if (flag){
			Log.e(tag, log);
			writeLogtoFile(String.valueOf('e'), tag, log);
		}
	}

	public static void w(String tag, String log) {
		if (flag){
			Log.w(tag, log);
			writeLogtoFile(String.valueOf('w'), tag, log);
		}
	}

	public static void v(String tag, String log) {
		if (flag){
			Log.v(tag, log);
			writeLogtoFile(String.valueOf('v'), tag, log);
		}
	}

	public static void i(String tag, String log) {
		if (flag){
			Log.i(tag, log);
			writeLogtoFile(String.valueOf('i'), tag, log);
		}
	}


	/**
	 * @date 2014-10-23
	 * @param flag the flag to set
	 */
	public static void openDebug(boolean flag) {
		MLog.flag = flag;
		MLog.isDebug= flag;
	}
	/**
	 * 打开日志输出到文件开关
	 * @param flag
	 */
	public static void openWriteLogtoFileFlag(boolean flag) {
		MLog.MYLOG_WRITE_TO_FILE=flag;
	}
	
	/**
	 * 日志输出到文件
	 * 
	 * @return
	 * **/
	private static void writeLogtoFile(String mylogtype, String tag, String text) {
		if(MYLOG_WRITE_TO_FILE==false){
			return;
		}		
		Date nowtime = new Date();
		String needWriteMessage = logfile.format(nowtime) + "    " + mylogtype
				+ "    " + tag + "    " + text;
		File file = new File(MYLOG_PATH_SDCARD_DIR);
		saveCurrentLog(file,true,needWriteMessage);
	}
	public  static void writeLogTempFile(String cotent) {
		File file = new File(MYLOG_PATH_SDCARD_DIR);
		saveCurrentLog(file,true,cotent);
	}
	private static void saveCurrentLog(File file,boolean isAppend,String cotent) {
		try {
			FileWriter filerWriter = new FileWriter(file,isAppend);
			BufferedWriter bufWriter = new BufferedWriter(filerWriter);
			bufWriter.write(cotent);
			bufWriter.newLine();
			bufWriter.close();
			filerWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public static void cleanMLogFile() {
		if (!MYLOG_WRITE_TO_FILE) {
			File file = new File(MYLOG_PATH_SDCARD_DIR);
			if (file.exists()) {
				file.delete();
			}
		}
	}
	
}
