package com.cnlaunch.physics.utils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Vector;

import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.cnlaunch.physics.PropertyFileOperation;

/**
 * 调试日志开关配置
 */
public class AppMlogConfig {
	protected static final String TAG = "AppMlogConfig";
	private final  String ROOT = "cnlaunch";
	private final  String CONFIG_DIR = "app_mlog";
	private final  String CONFIG_FILE = "app_mlog_config.properties";
	private final  String CONFIG_DEBUG_KEY = "debug";
	private final  String DEBUG_DIR = "app_mlog_debug";
	private boolean mDebugSwitch;
	private String settingsFile;
	private String appMLogDebugFolder;
	private String appMLogConfigFolder;
	private String debugAbsoluteFilename;
	private PropertyFileOperation mPropertyFileOperation;
	private  static AppMlogConfig mAppMLogConfig=null;
	private final static int RESERVE_APP_LOG_FILE_COUNT = 5;
	private AppLogRunState appLogRunState;
	/**
	 * 因为日志开关配置存在于进程整个生命周期，所以采用单例实现方式
	 */
	public  static  AppMlogConfig getInstance(){
		return getInstance("");
	}
	/**
	 * 因为日志开关配置存在于进程整个生命周期，所以采用单例实现方式
	 * @param packagePath 产品包目录 一般使用PathUtils.getPackagePath()传入
	 * @return
	 */
	private static  AppMlogConfig getInstance(String packagePath){
		if(mAppMLogConfig==null){
			mAppMLogConfig = new AppMlogConfig(packagePath);
		}
		return mAppMLogConfig;
	}
	private AppMlogConfig(String packagePath) {
		if(TextUtils.isEmpty(packagePath)){
			settingsFile =Environment.getExternalStorageDirectory().getPath() + File.separator + ROOT +
					File.separator + CONFIG_DIR +
					File.separator + CONFIG_FILE;
			appMLogDebugFolder = Environment.getExternalStorageDirectory().getPath() + File.separator + ROOT +
					File.separator + CONFIG_DIR +
					File.separator + DEBUG_DIR;
			appMLogConfigFolder =Environment.getExternalStorageDirectory().getPath() + File.separator + ROOT +
					File.separator + CONFIG_DIR;
		}
		else {
			settingsFile = packagePath + File.separator + CONFIG_DIR + File.separator + CONFIG_FILE;
			appMLogDebugFolder = packagePath + File.separator + CONFIG_DIR +File.separator + DEBUG_DIR;
			appMLogConfigFolder = packagePath + File.separator +CONFIG_DIR;
		}
		Log.d("AppMlogConfig",
				String.format("settingsFile=%s,appMLogDebugFolder=%s,appMLogConfigFolder=%s",settingsFile,appMLogDebugFolder,appMLogConfigFolder));
		mPropertyFileOperation = new PropertyFileOperation(settingsFile);
		mDebugSwitch = Boolean.parseBoolean(mPropertyFileOperation.get(CONFIG_DEBUG_KEY));
		Log.d("AppMlogConfig","DebugSwitch is"+mDebugSwitch);
		debugAbsoluteFilename = "";
		appLogRunState = new AppLogRunState();
	}

	public  void  setDebugSwitch(boolean isDebug) {
		mPropertyFileOperation.put(CONFIG_DEBUG_KEY,Boolean.toString(isDebug));
		mDebugSwitch = Boolean.parseBoolean(mPropertyFileOperation.get(CONFIG_DEBUG_KEY));
	}
	public boolean isDebug() {
		return mDebugSwitch;
	}

	/**
	 * 获取日志配置文件存放文件夹名称
	 * @return
     */
	public String getConfigFolder(){
		return  appMLogConfigFolder;
	}
	/**
	 * 获取debug日志文件存放文件夹名称
	 * @return
     */
	public String getDebugLogFolder(){
		return  appMLogDebugFolder;
	}

	/**
	 * 返回debug日志文件存放完整路径，包括文件名
	 * @return
	 */
	public String getDebugAbsoluteFilename() {
		if(TextUtils.isEmpty(debugAbsoluteFilename)) {
			SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss", Locale.ENGLISH);
			try {
				// 判断文件夹是否存在
				// 如果不存在、则创建一个新的文件夹(一次创建多层目录，使用mkdirs()，不能使用mkdir())
				File path = new File(appMLogDebugFolder);
				if (!path.exists()) {
					path.mkdirs();
				}
				File file = new File(appMLogDebugFolder + File.separator + df.format(new Date()) + ".log");
				// 如果目标文件已经存在
				if (file.exists() == false) {
					try {
						file.createNewFile(); // 创建新文件
						debugAbsoluteFilename = file.getAbsolutePath();
					} catch (Exception e) {
						debugAbsoluteFilename = "";
					}
				} else {
					debugAbsoluteFilename = file.getAbsolutePath();
				}
			} catch (Exception e) {
				e.printStackTrace();
				debugAbsoluteFilename = "";
			}
			if(TextUtils.isEmpty(debugAbsoluteFilename)) {
				debugAbsoluteFilename = appMLogDebugFolder + File.separator + df.format(new Date()) + ".log";
			}
		}
		Log.d("AppMlogConfig","getDebugAbsoluteFilename  is"+debugAbsoluteFilename);
		return debugAbsoluteFilename;
	}

	/**
	 * 创建一个按照最旧日期排序在先的文件列表
	 * @param path
	 * @return
	 */
	private static Vector<File> scanAppLogFiles(String path){
		Vector<File> appLogFilesVector = new Vector<File>();
		File root = new File(path);
		File[] files = root.listFiles();
		if(files == null){
			return appLogFilesVector;
		}
		for (File file : files) {
			if (!file.isDirectory()) {
				//删除存在的.tmp文件
				if(file.getName().endsWith(".log")){
					appLogFilesVector.add(file);
				}
			}
		}
		//使用冒泡排序按照创建日期排序
		File flag  = null;
		for(int i = 0;i< appLogFilesVector.size()-1;i++){
			flag =  appLogFilesVector.elementAt(i);
			for(int j=i+1;j< appLogFilesVector.size();j++){
				if(flag.lastModified() > appLogFilesVector.elementAt(j).lastModified()){
					appLogFilesVector.setElementAt(appLogFilesVector.elementAt(j),i);
					appLogFilesVector.setElementAt(flag,j);
					flag =  appLogFilesVector.elementAt(i);
				}
			}
		}
		return appLogFilesVector;
	}

	/**
	 * 检查日志文件是否大于5，大于5删除最早的日志
	 * @param path
	 */
	private static void deleteExpiredFile(String path){
		Vector<File>  fileVector= scanAppLogFiles(path);
		if(fileVector.size() >= RESERVE_APP_LOG_FILE_COUNT){
			int needDeleteFileCount = fileVector.size()+1 - RESERVE_APP_LOG_FILE_COUNT;
			for(int i=0;i<needDeleteFileCount;i++){
				File file = fileVector.elementAt(i);
				if(file != null){
					file.delete();
				}
			}
		}
	}
	/**
	 * 保存应用日志
	 */
	public  void  saveAppLog() {
		try {
			boolean debug = isDebug();
			if(debug && appLogRunState.isAppLogProcessRunning()==false) {
				deleteExpiredFile(getDebugLogFolder());
				String command = String.format("logcat  -v  threadtime  -f   %s &", getDebugAbsoluteFilename());
				Log.d("AppMlogConfig", "save App Log command="+command);
				Process process = Runtime.getRuntime().exec(command);
				appLogRunState.setAppLogProcessRunning(true);
				appLogRunState.setProcess(process);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * 销毁应用日志收集进程
	 */
	public  void  reset(){
		debugAbsoluteFilename="";
		appLogRunState.reset();
	}
	public static class AppLogRunState{
		Process process; //  记录日志进程对象
		boolean isAppLogProcessRunning;
		public AppLogRunState(){
			isAppLogProcessRunning = false;
			process = null;
		}

		/**
		 * 复位日志状态
		 */
		public void reset() {
			Log.d(TAG, " AppLogRunState  reset ");
			if (process != null) {
				Log.d(TAG, " process.destroy() ");
				process.destroy();
				process = null;
			}
			isAppLogProcessRunning = false;
		}

		public Process getProcess() {
			return process;
		}

		public void setProcess(Process process) {
			this.process = process;
			if (MLog.isDebug) {
				MLog.d(TAG, " AppLogRunState  setProcess = "+this.process );
			}
		}

		public boolean isAppLogProcessRunning() {
			return isAppLogProcessRunning;
		}

		public void setAppLogProcessRunning(boolean appLogProcessRunning) {
			isAppLogProcessRunning = appLogProcessRunning;
		}
	}
}
