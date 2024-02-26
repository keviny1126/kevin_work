package com.cnlaunch.physics.downloadbin;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;
import com.cnlaunch.physics.DPUHardwareInformation;
import com.cnlaunch.physics.DeviceFactoryManager;
import com.cnlaunch.physics.downloadbin.util.Analysis;
import com.cnlaunch.physics.downloadbin.util.DpuOrderUtils;
import com.cnlaunch.physics.downloadbin.util.MyFactory;
import com.cnlaunch.physics.entity.AnalysisData;
import com.cnlaunch.physics.entity.DPUHardwareInfo;
import com.cnlaunch.physics.entity.DPUSoftInfo;
import com.cnlaunch.physics.impl.IPhysics;
import com.cnlaunch.physics.listener.OnDownloadBinListener;
import com.cnlaunch.physics.utils.Bridge;
import com.cnlaunch.physics.utils.ByteHexHelper;
import com.cnlaunch.physics.utils.DeviceUtils;
import com.cnlaunch.physics.utils.DownloadBinWriteByte;
import com.cnlaunch.physics.utils.MLog;
import com.cnlaunch.physics.utils.Tools;
import com.cnlaunch.physics.utils.Tools.RateTestParameters;
import com.power.baseproject.utils.log.LogUtil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

/**
 * DownloadBin相关功能类 不能用于远程对象，即放置在服务中 [A brief description]
 *
 * @author bichuanfeng
 * @version 1.0
 * @date 2014-3-8
 *
 *
 */
public class DownloadBinUpdate {
	public static final int DOWNLOADBIN_SWITCH_MODEL_FAIL = 1;// 切换模式失败
	public static final int DOWNLOADBIN_CHECK_FAIL = 2;// 校验失败
	public static final int DOWNLOADBIN_SEND_FILENAME_FAIL = 3;// 发送文件名字失败
	public static final int DOWNLOADBIN_MD5_FAIL = 4;// MD5校验失败
	public static final int DOWNLOADBIN_MD5_SUCCESS = 5;// MD5校验成功
	public static final int DOWNLOADBIN_FAIL = 6;// 升级失败
	public static final int DOWNLOADBIN_SUCCESS = 7;// 升级成功
	public static final int DOWNLOADBIN_UPING = 8;// 正在升级
	public static final int DOWNLOADBIN_VERSION = 9;// 获取下位机DownloadBin版本号
	public static final int DPU_RESET_FAILED = 10;// 读取下位机信息失败
	public static final int DOWNLOADBIN_RESET_DPU_FAIL = 11;// 复位失败
	public static final int DOWNLOADBIN_SEND_UPDATE_FAIL = 12;// sendUpdate失败
	public static final int DOWNLOADBIN_SET_BAUDRATE_FAIL = 13;// setBautrate失败
	public static final int DOWNLOADBIN_SET_ADDRESSANDSIZE_FAIL = 14;// setAddressAndSize失败
	public static final int DOWNLOADBIN_FILE_NOT_EXIST = 15;// 升级文件不存在
	public static final int DOWNLOADBIN_FILE_NOT_SUPPORT_TRUCK = 16;// 升级文件不支持重卡固件
	public static final int DOWNLOADBIN_FILE_NOT_SUPPORT_CAR = 17;// 升级文件不支持小车固件
	public static final int DOWNLOADBIN_VEHICLE_VOLTAGE_VALUE = 18;// 获取车辆电压
	public static final int DPU_READ_VOLTAGE_NOT_SUPPORT = 19;// 接头不支持读取车辆电压
	public static final int DOWNLOADBIN_DATA_REPEAT_TEST = 20;// 接头通讯数据重复测试
	public static final int DOWN_DOWNLOADBIN_FILE_SUCCESS = 21;//下载固件并解压成功
	public static final int DOWN_DOWNLOADBIN_FILE_ERROR = 22;//下载固件失败
	private static Context mContext;
	private IPhysics mIPhysics = null; // 连接通道接口:目前包含蓝牙对象、串口对象
	private static final String TAG = "DownloadBinUp";
	private static final int PKG_SIZE = 4 * 1024; // DownloadBin每次上传的块大小 4096

	private static final int PKG_SIZE_HD = 2 * 1024;// HD DownloadBin每次上传的块大小
	// 2048
	private OnDownloadBinListener mOnDownloadBinListener; // DownloadBin更新回调
	private UpdateDownloadBinRunable mUpdateBinRunable; // DownloadBin更新线程
	private GetDPUVersionRunable mGetDPUVersionRunable; // 获取版本号线程
	private GetVehicleVoltageRunable mGetVehicleVoltageRunable; // 获取车辆电压
	private boolean isStopUpdate;
	private static final String UPDATE_OK_COMMAND = "4f4b21";
	private static final String RESET_CONNECTOR_RECEIVE_COMMAND = "3f";
	private static final String WRITE_DATA_RESPOND_COMMAND = "55AA0007FFF8610061";

	public DownloadBinUpdate(OnDownloadBinListener onDownloadBinListener, IPhysics iPhysics) {
		mIPhysics = iPhysics;
		this.mOnDownloadBinListener = onDownloadBinListener;
	}

	/**
	 * 升级DOWNLOAD_BIN任务
	 */
	public void updateAsync(String pDownBin_Path, String pDownBin_Name) {
		updateAsync(pDownBin_Path, pDownBin_Name, false);
	}

	/**
	 * 升级DOWNLOAD_BIN任务,增加加密downloadbin支持
	 */
	public void updateAsync(String pDownBin_Path, String pDownBin_Name, boolean isEncryptDownloadBin) {
		if (MLog.isDebug) {
			MLog.e(TAG, "DownloadBinUpdate updateAsync isEncryptDownloadBin=" + isEncryptDownloadBin);
		}
		if (mUpdateBinRunable == null) {
			mUpdateBinRunable = new UpdateDownloadBinRunable(pDownBin_Path, pDownBin_Name, isEncryptDownloadBin);
		}
		isStopUpdate = false;
		Thread t = new Thread(mUpdateBinRunable);
		t.start();
	}

	class UpdateDownloadBinRunable implements Runnable {
		String downBin_Path;
		String downBin_Name;
		boolean isEncryptDownloadBin;

		public UpdateDownloadBinRunable(String pDownBin_Path, String pDownBin_Name, boolean isEncryptDownloadBin) {
			downBin_Path = pDownBin_Path;
			downBin_Name = pDownBin_Name;
			this.isEncryptDownloadBin = isEncryptDownloadBin;
		}

		@Override
		public void run() {
			try {
				if (Tools.isTruck() && !Tools.isCarAndHeavyduty()) {
					Thread.sleep(2000);
					updateDownloadBinHD(downBin_Path, downBin_Name, isEncryptDownloadBin);
				} else if (DeviceUtils.getInstance().getUpdate103Device()) {
					updateDownloadBin103(downBin_Path, downBin_Name);
					DeviceUtils.getInstance().setUpdate103Device(false);
				} else {
					updateDownloadBin(downBin_Path, downBin_Name, isEncryptDownloadBin);
				}

			} catch (Exception e) {
				// 提示固件升级失败
				mOnDownloadBinListener.OnDownloadBinListener(DOWNLOADBIN_FAIL, 0, 0);
			}
		}
	}

	/**
	 * 获取DPU版本号任务
	 */
	public void getDPUVersionAsync(String serialNo, String baseDir) {
		if (mGetDPUVersionRunable == null) {
			mGetDPUVersionRunable = new GetDPUVersionRunable(serialNo, baseDir);
		}
		Thread t = new Thread(mGetDPUVersionRunable);
		t.start();
	}

	class GetDPUVersionRunable implements Runnable {
		private String serialNo;
		private String dir;

		public GetDPUVersionRunable(String serialNo, String baseDir) {
			this.serialNo = serialNo;
			this.dir = baseDir;
		}

		@Override
		public void run() {
			String downloadBinVer = "";
			String receiveBuff = "";
			boolean isSuccess = false;
			String mode = "00"; // 默认boot模式"00" ;
			try {
				if (Tools.isTruck() && !Tools.isCarAndHeavyduty()) {
					receiveBuff = resetDPU();
					if (receiveBuff.equalsIgnoreCase(RESET_CONNECTOR_RECEIVE_COMMAND)) {
						isSuccess = true;
					} else {
						isSuccess = false;
					}
					if (isSuccess) {
						try {
							Thread.sleep(1000);// 收完4个3f后才发6f指令
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						downloadBinVer = readDPUDownloadbinVersionInfo2105(serialNo);
						mOnDownloadBinListener.OnDownloadBinListener(DOWNLOADBIN_VERSION, downloadBinVer);
					} else {
						mOnDownloadBinListener.OnDownloadBinListener(DOWNLOADBIN_RESET_DPU_FAIL, "");
						return;
					}
				} else {
					isSuccess = readDPUHardwareInfo(serialNo, dir);
					if (isSuccess == false) {
						mOnDownloadBinListener.OnDownloadBinListener(DPU_RESET_FAILED, "");
						return;
					}

					downloadBinVer = readDPUDownloadbinVersionInfo2105(serialNo);
//					String runningmode = currentState2107();
//					MLog.e(TAG, "runningmode= "+runningmode);
					mOnDownloadBinListener.OnDownloadBinListener(DOWNLOADBIN_VERSION, downloadBinVer);
				}
			} catch (Exception ex) {
				mOnDownloadBinListener.OnDownloadBinListener(DPU_RESET_FAILED, "");
			}
		}
	}

	/**
	 * 获取DPU硬件版本信息
	 *
	 * @param serialNo
	 */
	private boolean readDPUHardwareInfo(String serialNo, String baseDir) {
		String[] info = readDPUDeviceInfo2103(mIPhysics);
		if (info == null) {
			return false;
		} else {
			if (Tools.isTruck() && !Tools.isCarAndHeavyduty()) {
				if (info != null && info.length == 1) { // 蓝牙发回的信息条数为1
					String[] infoTemp = {"", "", "", "", "", ""};
					infoTemp[2] = info[0];
					DeviceUtils.getInstance().saveDPUHardwareInfo(serialNo, baseDir, infoTemp);
				}
			} else {
				if (info != null && info.length >= 5) { // 蓝牙发回的信息条数为5
					DeviceUtils.getInstance().saveDPUHardwareInfo(serialNo, baseDir, info);
					if (DeviceFactoryManager.getInstance().getLinkMode() == DeviceFactoryManager.LINK_MODE_COM) {
						DPUHardwareInformation.getInstance(baseDir).putDPUHardwareInfo(new DPUHardwareInfo(info));
					}
				}
			}
			return true;
		}
	}

	public void stopUpdate() {
		isStopUpdate = true;
	}

	/**
	 * DownloadBin升级方法
	 *
	 * @return
	 * @throws Exception
	 */
	private String updateDownloadBin103(String pDownBin_Path, String pDownBin_Name) throws Exception {
		String mode = "";
		boolean iSuccess = false;
		File donwloadbin = new File(pDownBin_Path, pDownBin_Name);
		if (!donwloadbin.exists()) {
			MLog.e(TAG, "文件不存在");
			mOnDownloadBinListener.OnDownloadBinListener(DOWNLOADBIN_FILE_NOT_EXIST, 0, 0);
			return "1";
		}
		mode = currentState2114(mIPhysics, mOnDownloadBinListener);
		if (!mode.equalsIgnoreCase("00")) {
			if (!switchtoBootMode(mIPhysics, mOnDownloadBinListener)) {
				MLog.e(TAG, "切换到boot升级模式失败");
				mOnDownloadBinListener.OnDownloadBinListener(DOWNLOADBIN_FAIL, 0, 0);
				return "1";
			}
			// 切换成功之后,等待设备稳定
			try {
				Thread.sleep(2 * 1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
				mOnDownloadBinListener.OnDownloadBinListener(DOWNLOADBIN_FAIL, 0, 0);
				return "1";
			}
		}
		String verify = requestConnect2502();
		if (TextUtils.isEmpty(verify)) {
			MLog.e(TAG, "请求连接失败");
			mOnDownloadBinListener.OnDownloadBinListener(DOWNLOADBIN_FAIL, 0, 0);
			return "1";
		}
		if (!passwordVerify2503(verify)) {
			MLog.e(TAG, "校验失败");
			mOnDownloadBinListener.OnDownloadBinListener(DOWNLOADBIN_FAIL, 0, 0);
			return "1";
		}
		if (MLog.isDebug)
			MLog.e(TAG, "发送文件名字  = " + donwloadbin.getName());
		if (!SendFileNameAndLength(donwloadbin)) {
			MLog.e(TAG, "发送文件名字失败");
			mOnDownloadBinListener.OnDownloadBinListener(DOWNLOADBIN_FAIL, 0, 0);
			return "1";
		}
		long totalLen = donwloadbin.length();
		byte[] buff = new byte[PKG_SIZE];
		byte[] buffref = new byte[PKG_SIZE];
		Arrays.fill(buffref, (byte) 0xFF);
		InputStream fis;
		try {
			int count = 0;
			int writePos = 0;
			fis = new FileInputStream(donwloadbin);
			int counter = 0;
			while ((count = fis.read(buff)) > 0 && !isStopUpdate) {
				if (Arrays.equals(buff, buffref)) {
					writePos += count;
					if (MLog.isDebug)
						MLog.e(TAG, "无效数据不发送，只修改偏移地址  writePos==" + writePos + "totalLen ");
					mOnDownloadBinListener.OnDownloadBinListener(DOWNLOADBIN_UPING, writePos, totalLen);
					continue;
				}
				counter++;
				if (MLog.isDebug)
					MLog.e(TAG, "发送第" + counter + "包文件内容数据" + "count==" + count);
				byte[] rest = new byte[count];
				byte[] params = null;
				if (count < PKG_SIZE)// 最后一包数据
				{
					System.arraycopy(buff, 0, rest, 0, count);
					params = DpuOrderUtils.dataChunkParams(writePos, rest, count);
				} else {
					params = DpuOrderUtils.dataChunkParams(writePos, buff, count);
				}
				byte[] sendOrder = MyFactory.creatorForOrderMontage().sendUpdateFilesContent2413(params);
				if (MLog.isDebug)
					MLog.e(TAG, "sendUpdateFilesContent2413 sendOrder==" + ByteHexHelper.bytesToHexString(sendOrder));
				String backOrder = "";
				int flag = 0;
				if (sendOrder.length <= 0) {
					iSuccess = false;
					break;
				}
				while (flag < 3 && !isStopUpdate) {
					Tools.writeDPUCommand(sendOrder, mIPhysics);
					backOrder = mIPhysics.getCommand();
					if (TextUtils.isEmpty(backOrder)) {
						iSuccess = false;
						flag++;
						continue;
					}
					byte[] receiveOrder = ByteHexHelper.hexStringToBytes(backOrder);
					// 分析时无需与界面交互，所以去掉第三个参数mOnDownloadBinListener
					AnalysisData analysisData = MyFactory.creatorForAnalysis().analysis(sendOrder, receiveOrder);
					if (MyFactory.creatorForAnalysis().analysis2403(analysisData) == true) {
						iSuccess = true;
						break;
					} else {
						iSuccess = false;
						flag++;
					}
				}
				if (iSuccess) {
					writePos += count;
					if (MLog.isDebug)
						MLog.e(TAG, " writePos==" + writePos + "totalLen " + totalLen + "flag  " + flag + "isStopUpdate " + isStopUpdate);
					mOnDownloadBinListener.OnDownloadBinListener(DOWNLOADBIN_UPING, writePos, totalLen);
				} else {
					iSuccess = false;
					break;
				}
			}
			fis.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (!iSuccess) {
			mOnDownloadBinListener.OnDownloadBinListener(DOWNLOADBIN_FAIL, 0, 0);
			return "1";
		}
		// 写md5校验信息
		String md5_downloadbin = null;
		try {
			md5_downloadbin = ByteHexHelper.calculateSingleFileMD5sum(donwloadbin);
			if (!sendUpdateFileMd5New(md5_downloadbin)) {
				mOnDownloadBinListener.OnDownloadBinListener(DOWNLOADBIN_MD5_FAIL, 0, 0);
				MLog.e(TAG, "download.bin MD5校验失败!");
				return "1";
			}

		} catch (Exception e) {
			e.printStackTrace();
			mOnDownloadBinListener.OnDownloadBinListener(DOWNLOADBIN_MD5_FAIL, 0, 0);
			MLog.e(TAG, "download.bin MD5校验出现异常!");
			return "1";
		}
		// Download.bin 升级成功之后,等待3s ,让设备稳定再跳转
		try {
			Thread.sleep(3 * 1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		if (iSuccess) {
			mOnDownloadBinListener.OnDownloadBinListener(DOWNLOADBIN_SUCCESS, 0, 0);
			return "2";
		} else {
			mOnDownloadBinListener.OnDownloadBinListener(DOWNLOADBIN_FAIL, 0, 0);
			return "1";
		}
	}

	/**
	 * DownloadBin升级方法
	 * 增加加密downloadbin支持 xfh2018/03/20加入
	 *
	 * @return
	 * @throws Exception
	 */
	private String updateDownloadBin(String pDownBin_Path, String pDownBin_Name, boolean isEncryptDownloadBin) throws Exception {
		String mode = "";
		boolean iSuccess = false;
		if(MLog.isDebug){
			MLog.d(TAG, String.format("pDownBin_Path=%s,pDownBin_Name=%s",pDownBin_Path,pDownBin_Name));
		}
		File donwloadbin = new File(pDownBin_Path, pDownBin_Name);
		if (!donwloadbin.exists()) {
			MLog.e(TAG, "文件不存在");
			mOnDownloadBinListener.OnDownloadBinListener(DOWNLOADBIN_FILE_NOT_EXIST, 0, 0);
			return "1";
		}
		mode = currentState2114(mIPhysics, mOnDownloadBinListener);
		if (!mode.equalsIgnoreCase("00")) {
			if (!switchtoBootMode(mIPhysics, mOnDownloadBinListener)) {
				MLog.e(TAG, "切换到boot升级模式失败");
				// 切换不成功之后,也要等待设备稳定
				try {
					Thread.sleep(4 * 1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				mOnDownloadBinListener.OnDownloadBinListener(DOWNLOADBIN_SWITCH_MODEL_FAIL, 0, 0);
				return "1";
			}
			// 切换成功之后,等待设备稳定
			try {
				Thread.sleep(4 * 1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
				mOnDownloadBinListener.OnDownloadBinListener(DOWNLOADBIN_FAIL, 0, 0);
				return "1";
			}
		}
		String verify = requestConnect2502();
		if (TextUtils.isEmpty(verify)) {
			MLog.e(TAG, "请求连接失败");
			mOnDownloadBinListener.OnDownloadBinListener(DOWNLOADBIN_FAIL, 0, 0);
			return "1";
		}
		if (!passwordVerify2503(verify)) {
			MLog.e(TAG, "校验失败");
			mOnDownloadBinListener.OnDownloadBinListener(DOWNLOADBIN_FAIL, 0, 0);
			return "1";
		}
		if (MLog.isDebug)
			MLog.e(TAG, "发送文件名字  = " + donwloadbin.getName());
		if (!SendFileNameAndLength(donwloadbin)) {
			MLog.e(TAG, "发送文件名字失败");
			mOnDownloadBinListener.OnDownloadBinListener(DOWNLOADBIN_FAIL, 0, 0);
			return "1";
		}
		long totalLen = donwloadbin.length();
		byte[] buff = new byte[PKG_SIZE];
		byte[] buffref = new byte[PKG_SIZE];
		Arrays.fill(buffref, (byte) 0xFF);
		InputStream fis;
		try {
			int count = 0;
			int writePos = 0;
			fis = new FileInputStream(donwloadbin);
			int counter = 0;
			while ((count = fis.read(buff)) > 0 && !isStopUpdate) {
				if (Arrays.equals(buff, buffref)) {
					writePos += count;
					if (MLog.isDebug)
						MLog.e(TAG, "无效数据不发送，只修改偏移地址  writePos==" + writePos + "totalLen ");
					mOnDownloadBinListener.OnDownloadBinListener(DOWNLOADBIN_UPING, writePos, totalLen);
					continue;
				}
				counter++;
				if (MLog.isDebug)
					MLog.e(TAG, "发送第" + counter + "包文件内容数据" + "count==" + count);
				byte[] rest = new byte[count];
				byte[] params = null;
				if (count < PKG_SIZE)// 最后一包数据
				{
					System.arraycopy(buff, 0, rest, 0, count);
					params = DpuOrderUtils.dataChunkParams(writePos, rest, count);
				} else {
					params = DpuOrderUtils.dataChunkParams(writePos, buff, count);
				}
				byte[] sendOrder = MyFactory.creatorForOrderMontage().sendUpdateFilesContent2403(params);
				if (MLog.isDebug)
					MLog.e(TAG, "sendUpdateFilesContent2403 sendOrder==" + ByteHexHelper.bytesToHexString(sendOrder));
				String backOrder = "";
				int flag = 0;
				if (sendOrder.length <= 0) {
					iSuccess = false;
					break;
				}
				while (flag < 3 && !isStopUpdate) {
					Tools.writeDPUCommand(sendOrder, mIPhysics);
					backOrder = mIPhysics.getCommand();
					if (TextUtils.isEmpty(backOrder)) {
						iSuccess = false;
						flag++;
						continue;
					}
					byte[] receiveOrder = ByteHexHelper.hexStringToBytes(backOrder);
					// 分析时无需与界面交互，所以去掉第三个参数mOnDownloadBinListener
					if (MLog.isDebug)
						MLog.e(TAG, "sendUpdateFilesContent2403 analysis start");
					AnalysisData analysisData = MyFactory.creatorForAnalysis().analysis(sendOrder, receiveOrder);
					if (MLog.isDebug)
						MLog.e(TAG, "sendUpdateFilesContent2403 analysis2403 start");
					if (MyFactory.creatorForAnalysis().analysis2403(analysisData)) {
						iSuccess = true;
						break;
					} else {
						iSuccess = false;
						flag++;
					}
				}
				if (iSuccess) {
					writePos += count;
					if (MLog.isDebug)
						MLog.e(TAG, " writePos==" + writePos + "totalLen " + totalLen + "flag  " + flag + "isStopUpdate " + isStopUpdate);
					mOnDownloadBinListener.OnDownloadBinListener(DOWNLOADBIN_UPING, writePos, totalLen);
				} else {
					iSuccess = false;
					break;
				}
			}
			fis.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (!iSuccess) {
			mOnDownloadBinListener.OnDownloadBinListener(DOWNLOADBIN_FAIL, 0, 0);
			return "1";
		}
		// 写md5校验信息
		String md5_downloadbin = null;
		try {
			md5_downloadbin = ByteHexHelper.calculateSingleFileMD5sum(donwloadbin);
			if (!sendUpdateFileMd5(md5_downloadbin, isEncryptDownloadBin)) {
				mOnDownloadBinListener.OnDownloadBinListener(DOWNLOADBIN_MD5_FAIL, 0, 0);
				MLog.e(TAG, "download.bin MD5校验失败!");
				return "1";
			}

		} catch (Exception e) {
			e.printStackTrace();
			mOnDownloadBinListener.OnDownloadBinListener(DOWNLOADBIN_MD5_FAIL, 0, 0);
			MLog.e(TAG, "download.bin MD5校验出现异常!");
			return "1";
		}
		// Download.bin 升级成功之后,等待3s ,让设备稳定再跳转
		try {
			Thread.sleep(3 * 1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		if (iSuccess) {
			mOnDownloadBinListener.OnDownloadBinListener(DOWNLOADBIN_SUCCESS, 0, 0);
			return "2";
		} else {
			mOnDownloadBinListener.OnDownloadBinListener(DOWNLOADBIN_FAIL, 0, 0);
			return "1";
		}
	}

	/**
	 * DownloadBin升级方法
	 * 增加加密downloadbin支持 xfh2018/03/20加入
	 *
	 * @return
	 * @throws Exception
	 */
	private String updateDownloadBinHD(String pDownBin_Path, String pDownBin_Name, boolean isEncryptDownloadBin) throws Exception {
		boolean iSuccess = false;
		String startAddress = "";
		String updateCompleteFalse = "false";
		String updateCompleteTrue = "true";
		int startPos = 0;

		// 创建升级状态文件，用于保存是否升级完成
		File updateCompleteFile = new File(pDownBin_Path, "/updateCompleteFlag.txt");
		if (!updateCompleteFile.exists()) {
			updateCompleteFile.createNewFile();
		}
		writeByFileOutputStream(updateCompleteFile, updateCompleteFalse);

		// 升级前检查临时文件是否存在，若存在则删除
		File f = new File(pDownBin_Path, "/DOWNLOADTEMP.bin");
		if (f.exists())
			f.delete();
		File log = new File(pDownBin_Path, "/log.txt");
		if (log.exists())
			log.delete();
		// 检查升级文件DOWNLOAD.hex是否存在
		if (MLog.isDebug)
			MLog.e(TAG, "DownloadBinUpdate.updateDownloadBinHD.pDownBin_Path = " + pDownBin_Path);
		File donwloadhex = new File(pDownBin_Path, "/download.hex");
		if (!donwloadhex.exists()) {
			MLog.e(TAG, "文件不存在");
			mOnDownloadBinListener.OnDownloadBinListener(DOWNLOADBIN_FILE_NOT_EXIST, 0, 0);
			return "1";
		}
		try {
			String s = "";
			String s1 = "";
			// 文件绝对路径改成你自己的文件路径
			FileReader fr = new FileReader(donwloadhex);
			// 可以换成工程目录下的其他文本文件
			BufferedReader br = new BufferedReader(fr);
			// 创建临时文件DOWNLOAD.bin存储有效数据
			BufferedWriter writer = new BufferedWriter(new FileWriter(new File(pDownBin_Path, "/DOWNLOADTEMP.bin"), true));
			s1 = br.readLine();// 取出第一行
			startAddress = s1.substring(9, 13) + "0000";
			startPos = Integer.parseInt(startAddress, 16);
			while ((s1 = br.readLine()) != null) {
				if (s1.length() == 43) {// 只取需要写入数据的行
					s = s1.substring(9, 41);// 取出有效数据
					writer.write(s);
				} else {
					// 不做处理，直接读取下一行
				}
			}
			br.close();
			fr.close();
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			// close all
		}
		File donwloadbin = new File(pDownBin_Path, "/DOWNLOADTEMP.bin");
		// 复位，判断是否收到3F
		String receiveBuff = resetDPU();
		if (receiveBuff.equalsIgnoreCase(RESET_CONNECTOR_RECEIVE_COMMAND)) {
			iSuccess = true;
		} else {
			iSuccess = false;
		}
		if (!iSuccess) {
			MLog.d(TAG, "updateDownloadBin() --> 复位失败");
			mOnDownloadBinListener.OnDownloadBinListener(DOWNLOADBIN_RESET_DPU_FAIL, 0, 0);
			return "1";
		}
		// 发送UPDATE,判断是否收到OK
		iSuccess = sendUpdate();
		if (!iSuccess) {
			MLog.d(TAG, "updateDownloadBin() --> 接收OK! 失败");
			mOnDownloadBinListener.OnDownloadBinListener(DOWNLOADBIN_SEND_UPDATE_FAIL, 0, 0);
			return "1";
		}
		// 发送55 AA 00 0E FF F1 63 00 01 C2 00 00 00 00 00 A0 设置波特率
		iSuccess = setBautrate();
		if (!iSuccess) {
			MLog.d(TAG, "updateDownloadBin() --> 设置波特率 失败");
			mOnDownloadBinListener.OnDownloadBinListener(DOWNLOADBIN_SET_BAUDRATE_FAIL, 0, 0);
			return "1";
		}
		// 55 AA 00 0E FF F1 62 00 01 00 00 00 06 80 00 E5 设置写入地址和大小
		iSuccess = setAddressAndSize();
		if (!iSuccess) {
			MLog.d(TAG, "updateDownloadBin() --> 设置写入地址和大小 失败");
			mOnDownloadBinListener.OnDownloadBinListener(DOWNLOADBIN_SET_ADDRESSANDSIZE_FAIL, 0, 0);
			return "1";
		}

		byte[] buff = new byte[PKG_SIZE_HD];
		InputStream fis;
		try {
			int count = 0;
			int writePos = 0;
			int sendPos = 0;// 发送地址
			String sendbuff = "";
			String sendAddress = "";
			byte[] addressBytes = new byte[4];
			// 传输download.bin内容
			long totalLen = donwloadbin.length() / 2;
			fis = new FileInputStream(donwloadbin);
			// 创建临时文件存储有效数据
			BufferedWriter wrLog = new BufferedWriter(new FileWriter(new File(pDownBin_Path, "/log.txt"), true));
			while ((count = fis.read(buff)) > 0) {
				sendPos = startPos + writePos;// 初始位置+每次发命令累加后的地址
				addressBytes = ByteHexHelper.intToFourHexBytes(sendPos);// 10进制转换成4个字节
				sendAddress = ByteHexHelper.bytesToHexString(addressBytes);// 字节转换成string
				sendbuff = new String(buff);

				if (count < PKG_SIZE_HD)// 最后一包数据
				{
					sendbuff = "";// 清空sendbuff
					byte[] rest = new byte[PKG_SIZE_HD];
					// new 出来后全部赋值0
					for (int i = 0; i < PKG_SIZE_HD; i++) {
						rest[i] = '0';
					}
					System.arraycopy(buff, 0, rest, 0, count);
					sendbuff = new String(rest);
					// params = DpuOrderUtils.dataChunkParams(writePos, rest,
					// count);
				} else {
					// params = DpuOrderUtils.dataChunkParams(writePos, buff,
					// count);
				}
				byte[] sendOrder = MyFactory.creatorForOrderMontage().sendUpdateFilesContent2403(sendAddress, sendbuff);
				if (MLog.isDebug)
					MLog.d(TAG, "sendOrder = " + ByteHexHelper.bytesToHexString(sendOrder));
				wrLog.write(ByteHexHelper.bytesToHexString(sendOrder) + "\n");
				String backOrder = "";
				int flag = 0;
				if (sendOrder.length <= 0) {
					iSuccess = false;
					break;
				}
				while (flag < 3) {
					Tools.writeDPUCommand(sendOrder, mIPhysics);
					backOrder = mIPhysics.getCommand();
					if (TextUtils.isEmpty(backOrder)) {
						iSuccess = false;
						flag++;
						continue;
					}
					// MLog.d(TAG,
					// "sendUpdateFilesContent2403 --> backOrder = "+backOrder);
					if (backOrder.toUpperCase(Locale.ENGLISH).equals(WRITE_DATA_RESPOND_COMMAND)) {
						iSuccess = true;
						break;
					} else {
						iSuccess = false;
						flag++;
					}
				}
				if (iSuccess) {
					writePos += count / 2;
					mOnDownloadBinListener.OnDownloadBinListener(DOWNLOADBIN_UPING, writePos, totalLen);
				} else {
					iSuccess = false;
					break;
				}
			}
			fis.close();
			wrLog.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			// close all
		}
		if (!iSuccess) {
			MLog.e(TAG, "升级失败");
			mOnDownloadBinListener.OnDownloadBinListener(DOWNLOADBIN_FAIL, 0, 0);
			return "1";
		}
		// Download.bin 升级成功之后,等待3s ,让设备稳定再跳转
		try {
			Thread.sleep(3 * 1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		if (iSuccess) {
			// 发复位指令
			resetDPU();
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			MLog.d(TAG, "DownloadBinUpdate升级接头完成");
			writeByFileOutputStream(updateCompleteFile, updateCompleteTrue);
			mOnDownloadBinListener.OnDownloadBinListener(DOWNLOADBIN_SUCCESS, 0, 0);
			return "2";
		} else {
			MLog.e(TAG, "升级失败2");
			mOnDownloadBinListener.OnDownloadBinListener(DOWNLOADBIN_FAIL, 0, 0);
			return "1";
		}
	}

	/**
	 * 请求连接
	 *
	 * @return
	 */
	public String requestConnect2502() {
		String succeed = "";
		String backOrder = "";
		byte[] sendOrder = MyFactory.creatorForOrderMontage().requestConnect2502();
		int flag = 0;
		if (sendOrder.length <= 0) {
			return succeed;
		}
		while (flag < 3) {
			Tools.writeDPUCommand(sendOrder, mIPhysics);
			backOrder = mIPhysics.getCommand();
			if (TextUtils.isEmpty(backOrder)) {
				flag++;
				continue;
			}
			byte[] receiveOrder = ByteHexHelper.hexStringToBytes(backOrder);
			AnalysisData analysisData = MyFactory.creatorForAnalysis().analysis(sendOrder, receiveOrder, mOnDownloadBinListener);
			succeed = MyFactory.creatorForAnalysis().analysis2502(analysisData);
			if (succeed.length() > 0) {
				break;
			} else {
				flag++;
			}
		}
		return succeed;
	}

	/**
	 * 请求的验证码校验
	 *
	 * @return
	 */
	public boolean passwordVerify2503(String verify) {
		boolean succeed = false;
		String backOrder = "";
		byte[] sendOrder = MyFactory.creatorForOrderMontage().securityCheck2503(verify);
		int flag = 0;
		if (sendOrder.length <= 0) {
			return succeed;
		}
		while (flag < 3) {
			Tools.writeDPUCommand(sendOrder, mIPhysics);
			backOrder = mIPhysics.getCommand();
			if (TextUtils.isEmpty(backOrder)) {
				flag++;
				continue;
			}
			byte[] receiveOrder = ByteHexHelper.hexStringToBytes(backOrder);
			AnalysisData analysisData = MyFactory.creatorForAnalysis().analysis(sendOrder, receiveOrder, mOnDownloadBinListener);
			if (MyFactory.creatorForAnalysis().analysis2503(analysisData) == true) {
				succeed = true;
				break;
			} else {
				flag++;
			}
		}
		return succeed;
	}
	/**
	 * 读取TPMS 软件版本信息 最大等待时间由5秒改为2秒
	 *
	 * @return
	 **/
	public static DPUSoftInfo readTPMSVersionInfo2105(IPhysics iphysics) {
		ArrayList<String> softinfo = readDPUVersionInfo2105(iphysics,null);
		if (softinfo == null || softinfo.isEmpty()) {
			return null;
		}
		return new DPUSoftInfo(softinfo);
	}
	/**
	 * 读取dpu 软件版本信息 最大等待时间由5秒改为2秒
	 *
	 * @return
	 **/
	private static ArrayList<String> readDPUVersionInfo2105(IPhysics iphysics, OnDownloadBinListener onDownloadBinListener) {
		ArrayList<String> softinfo = null;
		String backOrder = "";
		Boolean hasGenerateNewCounter = false;
		byte[] sendOrder = MyFactory.creatorForOrderMontage().DPUVer2105();
		if (MLog.isDebug) {
			MLog.d(TAG, "readDPUVersionInfo2105.sendOrder = " + ByteHexHelper.bytesToHexString(sendOrder));
		}
		int flag = 0;
		if (sendOrder.length <= 0) {
			return null;
		}
		while (flag < 3) {
			Tools.writeDPUCommand(sendOrder, iphysics, 2000);
			backOrder = iphysics.getCommand();
			if (MLog.isDebug) {
				MLog.d(TAG, "readDPUVersionInfo2105().backOrder=" + backOrder);
			}
			if (TextUtils.isEmpty(backOrder)) {
				flag++;
				continue;
			}
			byte[] receiveOrder = ByteHexHelper.hexStringToBytes(backOrder);
			Analysis analysis = MyFactory.creatorForAnalysis();
			AnalysisData analysisData = analysis.analysis(sendOrder, receiveOrder, onDownloadBinListener);
			if (analysisData.getState()) {
				softinfo = analysis.analysis2105(analysisData);
				break;
			} else {
				// 校验状态不通过时，重新生成新流水号指令，并只生成一次
				// 对于重卡无流水号则理解为多发一次
				if (hasGenerateNewCounter == false) {
					sendOrder = MyFactory.creatorForOrderMontage().DPUVer2105();
					hasGenerateNewCounter = true;
					flag = 0;
					if (MLog.isDebug) {
						MLog.d(TAG, "DownloadBinUpdate.readDPUVersionInfo2105(). generate NewCounter sendOrder  = " + ByteHexHelper.bytesToHexString(sendOrder));
					}
				} else {
					flag++;
				}
			}
		}
		if (MLog.isDebug) {
			MLog.d(TAG, "DownloadBinUpdate.readDPUVersionInfo2105(). generate NewCounter sendOrder  = " + ByteHexHelper.bytesToHexString(sendOrder));
		}
		return softinfo;
	}

	private static String GetBootVersion(String serialNo, String packagePath) {
		String path = packagePath;//PathUtils.getPackagePath();
		String bootVersion = "";
		if (!(TextUtils.isEmpty(serialNo) || "".equals(serialNo) || "null".equals(serialNo.trim()))) {
			DPUSoftInfo dpuSoftInfo = DeviceUtils.getInstance().getDPUSoftInfo(serialNo, path);
			if (dpuSoftInfo != null) {
				bootVersion = dpuSoftInfo.getBootVersion();
			}
		}
		return bootVersion;
	}

	private static boolean checkBootOverOld(String bootVersion, String oldestVersion) {
		if (MLog.isDebug) {
			MLog.d(TAG, "checkBootOverOld : bootVersion = " + bootVersion + " oldestVersion = " + oldestVersion);
		}
		if ((TextUtils.isEmpty(bootVersion) || "".equals(bootVersion) || "null".equals(bootVersion.trim()))) {
			return false;
		}
		bootVersion = bootVersion.replace("V", "");
		oldestVersion = oldestVersion.replace("V", "");
		String[] temp = bootVersion.split("\\.");
		String[] tempoldest = oldestVersion.split("\\.");
		if (temp.length > 2) {
			try {
				for (int i = 0; i < tempoldest.length; i++) {
					int iTMP = Integer.parseInt(temp[i]);
					int iOldTmp = Integer.parseInt(tempoldest[i]);
					if (iTMP > iOldTmp) {
						return true;
					} else if (iTMP < iOldTmp) {
						return false;
					}
				}
			} catch (Exception ex) {
				return false;
			}
			return true;
		}
		return false;
	}

	public static boolean IsSuppVehicleVotageByReadBootVersion(IPhysics iphysics, String serialNo, String packagePath, String oldestVersion) {
		String bootVer = GetBootVersion(serialNo, packagePath);//读文件中boot版本
		if ((TextUtils.isEmpty(bootVer) || "".equals(bootVer) || "null".equals(bootVer.trim()))) {
			//直接读取下位机boot版本
			if (!DownloadBinUpdate.readDPUSoftVersionInfo(iphysics, serialNo)) {
				return false;
			}
			bootVer = GetBootVersion(serialNo, packagePath);//读文件中boot版本
		}
		return checkBootOverOld(bootVer, oldestVersion);
	}

	/**
	 * 读取dpu 软件版本信息
	 * 信息写入序列号对应的下位机版本信息文件
	 *
	 * @return
	 **/
	public static boolean readDPUSoftVersionInfo(IPhysics iphysics, String serialNo) {
		ArrayList<String> softinfo = readDPUVersionInfo2105(iphysics, null);
		if (softinfo == null || softinfo.isEmpty()) {
			return false;
		}
		if (Tools.isTruck() && !Tools.isCarAndHeavyduty()) {
			String downloadBinVer = "";
			downloadBinVer = softinfo.get(0).substring(6, 10);
			if (!downloadBinVer.isEmpty() || (downloadBinVer.length() >= 4)) {
				int versionH = ByteHexHelper.intPackLength(downloadBinVer.substring(0, 2));// 16进制转换成10进制
				int versionL = ByteHexHelper.intPackLength(downloadBinVer.substring(2, 4));
				downloadBinVer = "V" + versionH + "." + versionL;
				DeviceUtils.getInstance().saveDownloadVersion(serialNo, downloadBinVer);
			}
		} else {
			DeviceUtils.getInstance().saveDPUSoftInfo(serialNo, softinfo);
		}
		return true;
	}

	/**
	 * 读取dpu 软件版本信息
	 *
	 * @param iphysics
	 * @return 下位机downloadbin版本
	 */
	public static String readDPUDownloadBinVersionInfo(IPhysics iphysics) {
		String downloadBinVer = "";
		ArrayList<String> softinfo = readDPUVersionInfo2105(iphysics, null);
		if (softinfo == null || softinfo.isEmpty()) {
			return downloadBinVer;
		}
		if (Tools.isTruck() && !Tools.isCarAndHeavyduty()) {
			downloadBinVer = softinfo.get(0).substring(6, 10);
			if (!downloadBinVer.isEmpty() || (downloadBinVer.length() >= 4)) {
				int versionH = ByteHexHelper.intPackLength(downloadBinVer.substring(0, 2));// 16进制转换成10进制
				int versionL = ByteHexHelper.intPackLength(downloadBinVer.substring(2, 4));
				downloadBinVer = "V" + versionH + "." + versionL;
			}
		} else {
			downloadBinVer = softinfo.get(1);
		}
		if (MLog.isDebug)
			MLog.d(TAG, "downloadBinVer=" + downloadBinVer);
		return downloadBinVer;
	}
	/**
	 * 读取downloadbin版本信息
	 *
	 * @return
	 **/
	public static  String readDPUDownloadbinVersion(IPhysics iPhysics) {
		String downloadBinVer = "";
		ArrayList<String> softinfo = readDPUVersionInfo2105(iPhysics, null);
		if (softinfo == null || softinfo.isEmpty()) {
			return downloadBinVer;
		}
		if (Tools.isTruck() && !Tools.isCarAndHeavyduty()) {
			downloadBinVer = softinfo.get(0).substring(6, 10);
			if (!downloadBinVer.isEmpty() || (downloadBinVer.length() >= 4)) {
				int versionH = ByteHexHelper.intPackLength(downloadBinVer.substring(0, 2));// 16进制转换成10进制
				int versionL = ByteHexHelper.intPackLength(downloadBinVer.substring(2, 4));
				downloadBinVer = "V" + versionH + "." + versionL;
			}
		} else {
			downloadBinVer = softinfo.get(1);
			if (MLog.isDebug)
				MLog.d(TAG, "downloadBinVer000=" + downloadBinVer);
		}
		if (MLog.isDebug)
			MLog.d(TAG, "downloadBinVer=" + downloadBinVer);
		return downloadBinVer;
	}
	/**
	 * 读取dpu boot和downbin的软件版本信息
	 *
	 * @return
	 **/
	private String readDPUDownloadbinVersionInfo2105(String serialNo) {
		String downloadBinVer = "";
		ArrayList<String> softinfo = readDPUVersionInfo2105(mIPhysics, mOnDownloadBinListener);
		if (softinfo == null || softinfo.isEmpty()) {
			return downloadBinVer;
		}
		if (Tools.isTruck() && !Tools.isCarAndHeavyduty()) {
			downloadBinVer = softinfo.get(0).substring(6, 10);
			if (!downloadBinVer.isEmpty() || (downloadBinVer.length() >= 4)) {
				int versionH = ByteHexHelper.intPackLength(downloadBinVer.substring(0, 2));// 16进制转换成10进制
				int versionL = ByteHexHelper.intPackLength(downloadBinVer.substring(2, 4));
				downloadBinVer = "V" + versionH + "." + versionL;
				DeviceUtils.getInstance().saveDownloadVersion(serialNo, downloadBinVer);
			}
		} else {
			downloadBinVer = softinfo.get(1);
			if (MLog.isDebug)
				MLog.d(TAG, "downloadBinVer000=" + downloadBinVer);
			DeviceUtils.getInstance().saveDPUSoftInfo(serialNo, softinfo);
		}
		if (MLog.isDebug)
			MLog.d(TAG, "downloadBinVer=" + downloadBinVer);
		return downloadBinVer;
	}

	/**
	 * 读取当前状态Bootloader =0x00chenggong/download=0x05yijingshaoluguo
	 *
	 * @return
	 */
	public String currentState2107() {
		String runnningmode = "";
		String backOrder = "";
		byte[] sendOrder = MyFactory.creatorForOrderMontage().resetConnector2107();
		if (MLog.isDebug)
			MLog.d(TAG, "resetConnector2107.sendOrder = " + ByteHexHelper.bytesToHexString(sendOrder));
		int flag = 0;
		if (sendOrder.length <= 0) {
			return runnningmode;
		}
		while (flag < 3) {
			Tools.writeDPUCommand(sendOrder, mIPhysics);
			backOrder = mIPhysics.getCommand();
			if (MLog.isDebug)
				MLog.d(TAG, "currentState2107().backOrder=" + backOrder);
			if (TextUtils.isEmpty(backOrder)) {
				flag++;
				continue;
			}
			byte[] receiveOrder = ByteHexHelper.hexStringToBytes(backOrder);
			Analysis analysis = MyFactory.creatorForAnalysis();
			AnalysisData analysisData = analysis.analysis(sendOrder, receiveOrder, mOnDownloadBinListener);
			if (analysisData.getState()) {
				runnningmode = analysis.analysis2107(analysisData);
				if (MLog.isDebug)
					MLog.d(TAG, "downloadBinVer=" + runnningmode);
				break;
			} else {
				flag++;
			}
		}
		return runnningmode;
	}

	/**
	 * 读取dpu硬件版本信息版本信息 增加校验失败时，重新生成指令重发 缩短获取该指令等待时间 由最大时间5秒改为2秒
	 *
	 * @return
	 **/
	synchronized private static String[] readDPUDeviceInfo2103(IPhysics iPhysics) {
		String[] deviceinfo = null;
		String backOrder = "";
		// 记录是否已经生成过新流水号指令,用校验出错时判断
		Boolean hasGenerateNewCounter = false;
		byte[] sendOrder = MyFactory.creatorForOrderMontage().DPUVerInfo2103();
		if (MLog.isDebug) {
			MLog.d(TAG, "DownloadBinUpdate.readDPUDeviceInfo2103().sendOrder = " + ByteHexHelper.bytesToHexString(sendOrder));
		}
		int flag = 0;
		if (sendOrder.length <= 0) {
			return deviceinfo;
		}

		while (flag < 3) {
			if(flag==0 && hasGenerateNewCounter==false) {
				//第一次指令发送使用最大超时时间用于处理一些串口通讯长指令处理的异常退出，比如htt 异常退出
				Tools.writeDPUCommand(sendOrder, iPhysics);
			}
			else{
				Tools.writeDPUCommand(sendOrder, iPhysics, 2000);
			}
			backOrder = iPhysics.getCommand();
			if (TextUtils.isEmpty(backOrder)) {
				// 重新生成新流水号指令，并只生成一次
				if (hasGenerateNewCounter == false) {
					sendOrder = MyFactory.creatorForOrderMontage().DPUVerInfo2103();
					hasGenerateNewCounter = true;
					flag = 0;
					if (MLog.isDebug) {
						MLog.d(TAG, "DownloadBinUpdate.readDPUDeviceInfo2103(). generate NewCounter sendOrder  = " + ByteHexHelper.bytesToHexString(sendOrder));
					}
				} else {
					flag++;
				}
				continue;
			}
			if (MLog.isDebug) {
				MLog.d(TAG, "DownloadBinUpdate.readDPUDeviceInfo2103().backOrder = " + backOrder);
			}

			byte[] receiveOrder = ByteHexHelper.hexStringToBytes(backOrder);
			Analysis analysis = MyFactory.creatorForAnalysis();
			AnalysisData analysisData = analysis.analysis(sendOrder, receiveOrder);
			if (analysisData.getState()) {
				deviceinfo = analysis.analysis2103(analysisData);
				if (deviceinfo == null) {
					// 重新生成新流水号指令，并只生成一次
					if (hasGenerateNewCounter == false) {
						sendOrder = MyFactory.creatorForOrderMontage().DPUVerInfo2103();
						hasGenerateNewCounter = true;
						flag = 0;
						if (MLog.isDebug) {
							MLog.d(TAG, "DownloadBinUpdate.readDPUDeviceInfo2103(). generate NewCounter sendOrder  = " + ByteHexHelper.bytesToHexString(sendOrder));
						}
						continue;
					} else {
						break;
					}
				} else {
					break;
				}
			} else {
				// 校验状态不通过时，重新生成新流水号指令，并只生成一次
				// 对于重卡无流水号则理解为多发一次
				if (hasGenerateNewCounter == false) {
					sendOrder = MyFactory.creatorForOrderMontage().DPUVerInfo2103();
					hasGenerateNewCounter = true;
					flag = 0;
					if (MLog.isDebug) {
						MLog.d(TAG, "DownloadBinUpdate.readDPUDeviceInfo2103(). generate NewCounter sendOrder  = " + ByteHexHelper.bytesToHexString(sendOrder));
					}
				} else {
					flag++;
				}
			}
		}
		if (MLog.isDebug) {
			MLog.d(TAG, "DownloadBinUpdate.readDPUDeviceInfo2103().deviceinfo = " + deviceinfo);
		}

		return deviceinfo;
	}

	/**
	 * 重启DPU,只用于重卡
	 *
	 * @return
	 **/
	public String resetDPU() {
		String backOrder = "";
		byte[] sendOrder = MyFactory.creatorForOrderMontage().resetConnector2505();
		if (MLog.isDebug)
			MLog.d(TAG, "sendOrder=" + ByteHexHelper.bytesToHexString(sendOrder));
		int flag = 0;
		int count = 0; // 增加计数用于判断长时间一直收不到回复时无法退出循环
		while (flag < 3) {
			if (sendOrder.length > 0) {
				/*
				 * 优化下位机版本读取速度
				 */
				count = 0;
				mIPhysics.setCommand_wait(true);
				mIPhysics.setCommand("");
				OutputStream outputStream = mIPhysics.getOutputStream();
				try {
					if (MLog.isDebug)
						MLog.d(TAG, "DownloadBinUpdate.resetDPU().outputStream.write(sendOrder)=" + ByteHexHelper.bytesToHexString(sendOrder));
					mIPhysics.setIsTruckReset(true);
					outputStream.write(sendOrder);
				} catch (IOException e) {
					e.printStackTrace();
				}
				while (mIPhysics.getCommand_wait() && count < 40) {
					count++;
					try {
						Thread.sleep(250);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				backOrder = mIPhysics.getCommand();
				if (MLog.isDebug)
					MLog.d(TAG, "DownloadBinUpdate.resetDPU().backOrder=" + backOrder);
				mIPhysics.setIsTruckReset(false);
				if (backOrder == null || backOrder.equalsIgnoreCase(RESET_CONNECTOR_RECEIVE_COMMAND) == false) {
					backOrder = "";
					flag++;
					continue;
				} else {
					return backOrder;
				}
			} else {
				flag++;
			}
		}
		return backOrder;
	}

	public boolean resetDPUMode(int mode) {
		boolean successs = false;
		String backOrder = "";
		byte[] sendOrder = MyFactory.creatorForOrderMontage().transferDPUMode2109(mode);
		if (MLog.isDebug)
			MLog.d(TAG, "resetDPUMode().sendOrder=" + ByteHexHelper.bytesToHexString(sendOrder));
		int flag = 0;
		if (sendOrder.length <= 0) {
			return successs;
		}
		while (flag < 3) {
			Tools.writeDPUCommand(sendOrder, mIPhysics);
			backOrder = mIPhysics.getCommand();
			if (MLog.isDebug)
				MLog.d(TAG, "resetDPUMode().backOrder=" + backOrder);
			if (TextUtils.isEmpty(backOrder)) {
				flag++;
				continue;
			}
			byte[] receiveOrder = ByteHexHelper.hexStringToBytes(backOrder);
			Analysis analysis = MyFactory.creatorForAnalysis();
			AnalysisData analysisData = analysis.analysis(sendOrder, receiveOrder, mOnDownloadBinListener);
			if (analysisData.getState()) {
				successs = analysis.analysis2109(analysisData);
				if (MLog.isDebug)
					MLog.d(TAG, "backOrder=" + successs);
				break;
			} else {
				flag++;
			}
		}
		return successs;
	}

	/**
	 * 发送升级文件的文件名和文件长度
	 */
	public boolean SendFileNameAndLength(File donwloadbin) {
		boolean succeed = false;
		byte[] sendOrder;
		if (donwloadbin.getName().equalsIgnoreCase("DOWNLOAD103.bin")) {
			sendOrder = MyFactory.creatorForOrderMontage().sendFileNameAndLength2412(donwloadbin);
		} else {
			sendOrder = MyFactory.creatorForOrderMontage().sendFileNameAndLength2402(donwloadbin);
		}
		String backOrder = "";
		int flag = 0;
		if (sendOrder.length <= 0) {
			return succeed;
		}
		while (flag < 3) {
			Tools.writeDPUCommand(sendOrder, mIPhysics, 10000);
			backOrder = mIPhysics.getCommand();
			if (TextUtils.isEmpty(backOrder)) {
				flag++;
				continue;
			}
			byte[] receiveOrder = ByteHexHelper.hexStringToBytes(backOrder);
			AnalysisData analysisData = MyFactory.creatorForAnalysis().analysis(sendOrder, receiveOrder, mOnDownloadBinListener);
			if (MyFactory.creatorForAnalysis().analysis2402(analysisData) == true) {
				succeed = true;
				break;
			} else {
				succeed = false;
				flag++;
			}
		}
		return succeed;
	}

	/**
	 * 发送升级文件的MD5校验
	 * 加入加密downloadbin支持
	 *
	 * @param md5
	 * @return
	 */
	private boolean sendUpdateFileMd5(String md5, boolean isEncryptDownloadBin) {
		boolean succeed = false;
		byte[] sendOrder = MyFactory.creatorForOrderMontage().sendUpdateFileMd52404(md5);
		if (MLog.isDebug) {
			MLog.d(TAG, "sendUpdateFileMd5.sendOrder = " + ByteHexHelper.bytesToHexString(sendOrder) + " isEncryptDownloadBin=" + isEncryptDownloadBin);
		}
		String backOrder = "";
		int flag = 0;
		if (sendOrder.length <= 0) {
			return succeed;
		}
		while (flag < 3) {
			//最大超时时间改为30秒，适应easydiag加密需要
			if (isEncryptDownloadBin) {
				Tools.writeDPUCommand(sendOrder, mIPhysics, 30000);
			} else {
				Tools.writeDPUCommand(sendOrder, mIPhysics);
			}
			backOrder = mIPhysics.getCommand();
			if (TextUtils.isEmpty(backOrder)) {
				flag++;
				continue;
			}
			if (MLog.isDebug) {
				MLog.d(TAG, "sendUpdateFileMd5.backOrder = " + backOrder);
			}
			byte[] receiveOrder = ByteHexHelper.hexStringToBytes(backOrder);
			AnalysisData analysisData = MyFactory.creatorForAnalysis().analysis(sendOrder, receiveOrder, mOnDownloadBinListener);
			if (MyFactory.creatorForAnalysis().analysis2404(analysisData) == true) {
				succeed = true;
				break;
			} else {
				flag++;
			}
		}
		return succeed;
	}

	/**
	 * 发送升级文件的MD5校验
	 *
	 * @param md5
	 * @return
	 */
	private boolean sendUpdateFileMd5New(String md5) {
		boolean succeed = false;
		byte[] sendOrder = MyFactory.creatorForOrderMontage().sendUpdateFileMd52414(md5);
		String backOrder = "";
		int flag = 0;
		if (sendOrder.length <= 0) {
			return succeed;
		}
		while (flag < 3) {
			Tools.writeDPUCommand(sendOrder, mIPhysics);
			backOrder = mIPhysics.getCommand();
			if (MLog.isDebug)
				MLog.e(TAG, " sendUpdateFileMd52414 backorder=" + backOrder);
			if (TextUtils.isEmpty(backOrder)) {
				flag++;
				continue;
			}
			byte[] receiveOrder = ByteHexHelper.hexStringToBytes(backOrder);
			AnalysisData analysisData = MyFactory.creatorForAnalysis().analysis(sendOrder, receiveOrder, mOnDownloadBinListener);
			if (MyFactory.creatorForAnalysis().analysis2404(analysisData) == true) {
				succeed = true;
				break;
			} else {
				flag++;
			}
		}
		return succeed;
	}

	/**
	 * 验证所有文件的MD5
	 *
	 * @param md5info
	 * @return
	 */
	public boolean ValidateAllFilesMd5(HashMap<String, String> md5info) {
		Map<String, String> md5InDevice;
		String backOrder = "";
		byte[] sendOrder = MyFactory.creatorForOrderMontage().ValidateAllFilesMd52408();
		int flag = 0;
		while (flag < 3) {

			if (sendOrder.length > 0) {
				Bridge bridge = new Bridge();
				Runnable dynamic = new DownloadBinWriteByte(bridge, sendOrder, mIPhysics);
				mIPhysics.setCommand("");
				Thread t = new Thread(dynamic);
				t.start();
				try {
					t.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				backOrder = mIPhysics.getCommand();
			}
			byte[] receiveOrder = ByteHexHelper.hexStringToBytes(backOrder);
			AnalysisData analysisData = MyFactory.creatorForAnalysis().analysis(sendOrder, receiveOrder, mOnDownloadBinListener);
			if (analysisData.getState()) {
				// 进行MD5数据校验 对比
				byte[] status = DpuOrderUtils.filterOutCmdParameters(receiveOrder);
				if (status[0] == 0x00) {
					byte[] temp = DpuOrderUtils.filterOutCmdParameters(receiveOrder);
					// 用来记录校验错误的文件信息
					int fileNum = temp[0] << 8 | temp[1];
					int offset = 2;
					HashMap<String, String> md5info1 = new HashMap<String, String>();
					for (int i = 0; i < fileNum; i++) {
						// DPU_String 与 java 的String类型是有区别的！ 前者多了两个长度字节
						int fileNameLen = (temp[offset] << 8 | temp[offset + 1]);
						// 截取 DPU_String
						byte[] filename_bytes = new byte[fileNameLen - 1];
						for (int j = 0; j < fileNameLen - 1; j++) {
							filename_bytes[j] = temp[offset + 2 + j];
						}
						// 截取 md5字符串
						byte[] md5bytes = new byte[32];
						for (int j = 0; j < 32; j++) {
							md5bytes[j] = temp[offset + 2 + fileNameLen + j];
						}
						String fileName = new String(filename_bytes);// 文件名
						String md5OnDpu = new String(md5bytes);// 文件的md5
						md5info.put(fileName, md5OnDpu);
						offset += (2 + fileNameLen + 32);// 更新偏移量位置,计算的依据请参考DPU
					}
					md5InDevice = md5info1;
					Iterator<Map.Entry<String, String>> it = md5InDevice.entrySet().iterator();
					String md5Device;
					String md5Client;
					String fileName;
					while (it.hasNext()) {
						Map.Entry<String, String> e = it.next();
						fileName = e.getKey();
						md5Device = e.getValue();
						md5Client = md5info.get(fileName);
						if (!md5Client.equals(md5Device)) {
							return false;
						}
					}
				}
				return true;
			} else {
				flag++;
			}
		}
		return false;
	}

	/**
	 * 发送Update!请求 add by wzx
	 *
	 * @return 接收OK!
	 */
	public boolean sendUpdate() {
		String backOrder = "";
		boolean succeed = false;
		byte[] sendOrder = MyFactory.creatorForOrderMontage().sendUpdate2505();
		int flag = 0;
		while (flag < 3) {
			if (sendOrder.length > 0) {
				Bridge bridge = new Bridge();
				Runnable dynamic = new DownloadBinWriteByte(bridge, sendOrder, mIPhysics);
				mIPhysics.setCommand("");
				Thread t = new Thread(dynamic);
				t.start();
				try {
					t.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				backOrder = mIPhysics.getCommand();
				if (MLog.isDebug)
					MLog.d(TAG, "sendUpdate() --> backOrder = " + backOrder);
			}
			if (backOrder.toLowerCase().equals(UPDATE_OK_COMMAND)) {
				succeed = true;
				break;
			} else {
				flag++;
			}
		}
		return succeed;
	}

	/**
	 * 设置波特率 add by wzx
	 *
	 * @return
	 */
	public boolean setBautrate() {
		String backOrder = "";
		boolean succeed = false;
		byte[] sendOrder = MyFactory.creatorForOrderMontage().setBautrate2505();
		int flag = 0;
		while (flag < 1) {
			if (sendOrder.length > 0) {
				Bridge bridge = new Bridge();
				Runnable dynamic = new DownloadBinWriteByte(bridge, sendOrder, 500, mIPhysics);
				mIPhysics.setCommand("");
				Thread t = new Thread(dynamic);
				t.start();
				try {
					t.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				backOrder = mIPhysics.getCommand();
				if (MLog.isDebug)
					MLog.d(TAG, "setBautrate() --> backOrder = " + backOrder);
			}
			if (backOrder.toLowerCase().equals("55aa0007fff8630063")) {
				succeed = true;
				break;
			} else {
				flag++;
			}
		}
		return succeed;
	}

	/**
	 * 设置地址和大小 add by wzx
	 *
	 * @return
	 */
	public boolean setAddressAndSize() {
		String backOrder = "";
		boolean succeed = false;
		byte[] sendOrder = MyFactory.creatorForOrderMontage().setAddressAndSize2505();
		int flag = 0;
		while (flag < 1) {
			if (sendOrder.length > 0) {
				Bridge bridge = new Bridge();
				Runnable dynamic = new DownloadBinWriteByte(bridge, sendOrder, 500, mIPhysics);
				mIPhysics.setCommand("");
				Thread t = new Thread(dynamic);
				t.start();
				try {
					t.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				backOrder = mIPhysics.getCommand();
				if (MLog.isDebug)
					MLog.d(TAG, "setAddressAndSize() --> backOrder = " + backOrder);
			}
			if (backOrder.toLowerCase().equals("55aa0007fff8620062")) {
				succeed = true;
				break;
			} else {
				flag++;
			}
		}
		return succeed;
	}

	/**
	 * 完成升级确认
	 *
	 * @return
	 */
	public boolean ValidateUpdateFinished() {
		// 升级成功
		boolean succeed = false;
		byte[] sendOrder = MyFactory.creatorForOrderMontage().ValidateUpdateFinished2405();
		String backOrder = "";
		int flag = 0;
		while (flag < 3) {
			if (sendOrder.length > 0) {
				Bridge bridge = new Bridge();
				Runnable dynamic = new DownloadBinWriteByte(bridge, sendOrder, mIPhysics);
				mIPhysics.setCommand("");
				Thread t = new Thread(dynamic);
				t.start();
				try {
					t.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				backOrder = mIPhysics.getCommand();
			}
			byte[] receiveOrder = ByteHexHelper.hexStringToBytes(backOrder);
			AnalysisData analysisData = MyFactory.creatorForAnalysis().analysis(sendOrder, receiveOrder, mOnDownloadBinListener);
			if (MyFactory.creatorForAnalysis().analysis2405(analysisData) == true) {
				succeed = true;
				break;
			} else {
				flag++;
			}
		}
		return succeed;
	}

	/**
	 * 用FileOutputStream向文件写入内容
	 *
	 * @param file
	 * @param _sContent
	 * @throws IOException
	 */
	public static void writeByFileOutputStream(File file, String _sContent) throws IOException {
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(file);
			fos.write(_sContent.getBytes());
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (fos != null) {
				fos.close();
				fos = null;
			}
		}
	}

	/**
	 * 获取DPU硬件版本信息,并判断结果是否正确
	 *
	 * @param iPhysics
	 */
	public static boolean readDPUHardwareInfo(IPhysics iPhysics) {
		try {
			String[] info = readDPUDeviceInfo2103(iPhysics);
			if (info == null) {
				return false;
			} else {
				if (Tools.isTruck() && !Tools.isCarAndHeavyduty()) {
					return false;
				} else {
					return true;
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
	}

	/**
	 * 获取DPU硬件版本信息
	 *
	 * @param iPhysics
	 * @return
	 */
	public static String[] readNativeDPUHardwareInfo(IPhysics iPhysics) {
		try {
			return readDPUDeviceInfo2103(iPhysics);
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	/**
	 * 用户接头自动化测试工具测试
	 */
	public void testDPU() {
		TestDPURunable mGetDPUVersionRunable = new TestDPURunable();
		Thread t = new Thread(mGetDPUVersionRunable);
		t.start();
	}

	public void testDPU(boolean isDataSendTest, boolean isDataSendTestRepeat, boolean needSingleStepTest, boolean isDiscontinuous) {
		TestDPURunable mGetDPUVersionRunable = new TestDPURunable(isDataSendTest, isDataSendTestRepeat, needSingleStepTest, isDiscontinuous);
		Thread t = new Thread(mGetDPUVersionRunable);
		t.start();
	}

	class TestDPURunable implements Runnable {
		private boolean isDataSendTest;
		private boolean isDataSendTestRepeat;
		private boolean isDiscontinuous;
		private boolean needSingleStepTest;

		public TestDPURunable() {
			this(false, false, false, false);
		}

		public TestDPURunable(boolean isDataSendTest, boolean isDataSendTestRepeat, boolean needSingleStepTest, boolean isDiscontinuous) {
			this.isDataSendTest = isDataSendTest;
			this.isDataSendTestRepeat = isDataSendTestRepeat;
			this.needSingleStepTest = needSingleStepTest;
			this.isDiscontinuous = isDiscontinuous;
		}

		@Override
		public void run() {
			boolean isSuccess = false;
			try {
				// 测试所有通讯方式数据传送
				if (isDataSendTest) {
					/**
					 * 自动化测试工具相关的wifi数据测试
					 */
					int bufferSize = 256;// 5120,最大数据帧长度不超过5500，所以定为5120;
					byte[] totalbuffer = new byte[bufferSize];
					int totalLen = 0;
					if (DeviceFactoryManager.getInstance().getLinkMode() == DeviceFactoryManager.LINK_MODE_WIFI) {
						// 记录是否已经生成过新流水号指令,用校验出错时判断
						// 读取一段测试文本，发送到接头默认路径为如下目录
						String testSampleFile = Environment.getExternalStorageDirectory().getPath() + "/cnlaunch/dpu_wifi_test_sample.txt";
						String testFile = Environment.getExternalStorageDirectory().getPath() + "/cnlaunch/dpu_wifi_test.txt";
						FileInputStream fis = new FileInputStream(new File(testSampleFile));
						int len = 0;
						int packageSize = bufferSize;
						while (true) {
							if (totalLen >= (bufferSize)) {
								break;
							}
							len = fis.read(totalbuffer, totalLen, packageSize);
							if (len < packageSize) {
								totalLen += len;
								break;
							}
							totalLen += len;
						}
						fis.close();
					}
					BufferedWriter bufferedWriter = null;//new BufferedWriter(new FileWriter(testFile));
					// 重复发送100次
					int count = 1;
					Boolean state = true;
					if (this.isDataSendTestRepeat) {
						RateTestParameters rateTestParameters = new RateTestParameters();
						rateTestParameters.sendDataStartTime = (new Date()).getTime();
						rateTestParameters.receiveDataStartTime = (new Date()).getTime();
						rateTestParameters.receiveDataTime = 1;
						rateTestParameters.sendDataTime = 1;
						isSuccess = readDPUHardwareInfo(DeviceFactoryManager.getInstance().getCurrentDevice());
						if (isSuccess == false) {
							mOnDownloadBinListener.OnDownloadBinListener(DPU_RESET_FAILED, "");
							return;
						}
						//需要软复位接头开放逻辑
						/*if (DeviceFactoryManager.getInstance().getLinkMode() == DeviceFactoryManager.LINK_MODE_USB) {
							DeviceFactoryManager.getInstance().setResetStatus(true);
							Thread.sleep(1000);
						}
						isSuccess = resetDPUDevice2505(DeviceFactoryManager.getInstance().getCurrentDevice());
						usbEnterTest();*/
						if (DeviceFactoryManager.getInstance().getLinkMode() == DeviceFactoryManager.LINK_MODE_USB) {
							while (true) {
								mOnDownloadBinListener.OnDownloadBinListener(DOWNLOADBIN_DATA_REPEAT_TEST, String.format(" 计数器= %d sendRate = %d byte PS  receiveRate = %d byte PS", count, rateTestParameters.sendDataSum * 1000 / rateTestParameters.sendDataTime, rateTestParameters.receiveDataSum * 1000 / rateTestParameters.receiveDataTime));
								state = testDPUDeviceWithReadDPUHardwareInfo(mIPhysics, Arrays.copyOf(totalbuffer, totalLen), false, null, rateTestParameters);
								count++;
								if (!state) {
									mOnDownloadBinListener.OnDownloadBinListener(DPU_RESET_FAILED, "");
									break;
								}
							}
						}
						else {
							while (true) {
								mOnDownloadBinListener.OnDownloadBinListener(DOWNLOADBIN_DATA_REPEAT_TEST, String.format(" 计数器= %d sendRate = %d byte PS  receiveRate = %d byte PS", count, rateTestParameters.sendDataSum * 1000 / rateTestParameters.sendDataTime, rateTestParameters.receiveDataSum * 1000 / rateTestParameters.receiveDataTime));
								if (needSingleStepTest) {
									if (count++ % 1000 != 0)
										state = testDPUDeviceWiFiSendAndReceive(mIPhysics, Arrays.copyOf(totalbuffer, count), false, bufferedWriter);
									else
										state = testDPUDeviceWiFiSendAndReceive(mIPhysics, Arrays.copyOf(totalbuffer, totalLen), false, bufferedWriter);
								} else {
									if (count++ % 5 == 0)
										state = testDPUDeviceWiFiSendAndReceive(mIPhysics, Arrays.copyOf(totalbuffer, totalLen), false, null, rateTestParameters);
									else
										state = testDPUDeviceWiFiSendAndReceive(mIPhysics, Arrays.copyOf(totalbuffer, totalLen), false, null, rateTestParameters);
								}
								if (bufferedWriter != null) {
									bufferedWriter.write("\n\n");
								}
								if (!state) {
									mOnDownloadBinListener.OnDownloadBinListener(DPU_RESET_FAILED, "");
									break;
								}
							}
						}
					} else {
						state = testDPUDeviceWiFiSendAndReceive(mIPhysics, Arrays.copyOf(totalbuffer, totalLen), isDiscontinuous, bufferedWriter);
						if (!state) {
							mOnDownloadBinListener.OnDownloadBinListener(DPU_RESET_FAILED, "");
						}
					}
					if(bufferedWriter!=null) {
						bufferedWriter.close();
					}
					if (state) {
						mOnDownloadBinListener.OnDownloadBinListener(DOWNLOADBIN_SUCCESS, "");
					}
					return;
				} else {
					isSuccess = readDPUHardwareInfo(DeviceFactoryManager.getInstance().getCurrentDevice());
					if (isSuccess == false) {
						mOnDownloadBinListener.OnDownloadBinListener(DPU_RESET_FAILED, "");
						return;
					}
					if (DeviceFactoryManager.getInstance().getLinkMode() == DeviceFactoryManager.LINK_MODE_USB) {
						DeviceFactoryManager.getInstance().setResetStatus(true);
						Thread.sleep(1000);
					}
					isSuccess = resetDPUDevice2505(DeviceFactoryManager.getInstance().getCurrentDevice());
					usbEnterTest(isSuccess);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
				mOnDownloadBinListener.OnDownloadBinListener(DPU_RESET_FAILED, "");
			}
		}
	}
	/**
	 * 数据传输测试,带接收发送速率输出
	 */
	private static Boolean testDPUDeviceWithReadDPUHardwareInfo(IPhysics iPhysics, byte[] data, boolean isDiscontinuous, BufferedWriter bufferedWriter, RateTestParameters rateTestParameters) {
		String backOrder = "";
		String sendOrderString = "";
		Boolean hasGenerateNewCounter = false;
		Boolean state = false;
		SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS", Locale.ENGLISH);
		byte[] sendOrder = MyFactory.creatorForOrderMontage().generateCommonCommand(new byte[]{0x21, 0x03}, data);
		int flag = 0;
		if (sendOrder.length <= 0) {
			return state;
		}
		while (flag < 3) {
			if (MLog.isDebug) {
				MLog.d(TAG, "testDPUDeviceWithReadDPUHardwareInfo 2103 .sendOrder  ");
			}
			if (bufferedWriter != null) {
				try {
					sendOrderString = ByteHexHelper.bytesToHexString(sendOrder);
					bufferedWriter.write("\nrequest(" + mSimpleDateFormat.format(new Date()) + "):" + sendOrderString);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			Tools.writeDPUCommand(sendOrder, iPhysics, 5000, isDiscontinuous, rateTestParameters);
			backOrder = iPhysics.getCommand();
			if (TextUtils.isEmpty(backOrder)) {
				flag++;
				continue;
			}
			if (MLog.isDebug) {
				MLog.d(TAG, "testDPUDeviceWithReadDPUHardwareInfo 2103.backOrder = " + backOrder);
			}
			if (bufferedWriter != null) {
				try {
					bufferedWriter.write("\nAnswer(" + mSimpleDateFormat.format(new Date()) + "):" + backOrder);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			byte[] receiveOrder = ByteHexHelper.hexStringToBytes(backOrder);
			Analysis analysis = MyFactory.creatorForAnalysis();
			AnalysisData analysisData = analysis.analysis(sendOrder, receiveOrder);
			if (analysisData.getState()) {
				state = true;
				break;
			} else {
				// 校验状态不通过时，重新生成新流水号指令，并只生成一次
				// 对于重卡无流水号则理解为多发一次
				if (hasGenerateNewCounter == false) {
					sendOrder = MyFactory.creatorForOrderMontage().generateCommonCommand(new byte[]{0x21, 0x03}, data);
					hasGenerateNewCounter = true;
					flag = 0;
					if (MLog.isDebug) {
						MLog.d(TAG, "testDPUDeviceWithReadDPUHardwareInfo 2103. generate NewCounter sendOrder  = ");
					}
				} else {
					flag++;
				}
			}
		}
		if (MLog.isDebug) {
			MLog.d(TAG, "testDPUDeviceWithReadDPUHardwareInfo 2103. end ");
		}
		return state;
	}
	private void usbEnterTest(boolean  isSuccess) throws InterruptedException {
		// 支持有线测试
		if (DeviceFactoryManager.getInstance().getResetStatus()) {
			// 等待状态变更
			int times = 1;
			do {
				if (times > 10) {
					// 超时还原默认状态
					mOnDownloadBinListener.OnDownloadBinListener(DOWNLOADBIN_RESET_DPU_FAIL, "");
					return;
				}
				try {
					Thread.sleep(DeviceFactoryManager.GET_DEVICE_INFORMATION_DELAYTIME);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				times++;
				MLog.d(TAG, "Get Connector Ready is false");
			} while (DeviceFactoryManager.getInstance().getResetStatus());
			MLog.d(TAG, "Get Connector Ready is true");
			// 发送验证命令
			isSuccess = checkDPUDevice2117(DeviceFactoryManager.getInstance().getCurrentDevice());
			DeviceFactoryManager.getInstance().setResetStatus(true);
			Thread.sleep(1000);
			isSuccess = downloadDPUDevice2111(DeviceFactoryManager.getInstance().getCurrentDevice());
			times = 1;
			do {
				if (times > 10) {
					mOnDownloadBinListener.OnDownloadBinListener(DOWNLOADBIN_RESET_DPU_FAIL, "");
					return;
				}
				try {
					Thread.sleep(DeviceFactoryManager.GET_DEVICE_INFORMATION_DELAYTIME);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				times++;
				MLog.d(TAG, "Get Connector Ready is false");
			} while (DeviceFactoryManager.getInstance().getResetStatus());
			mOnDownloadBinListener.OnDownloadBinListener(DOWNLOADBIN_SUCCESS, "");
		} else {
			if (isSuccess == false) {
				mOnDownloadBinListener.OnDownloadBinListener(DOWNLOADBIN_RESET_DPU_FAIL, "");
			} else {
				mOnDownloadBinListener.OnDownloadBinListener(DOWNLOADBIN_SUCCESS, "");
			}
		}
	}
	public static Boolean downloadDPUDevice2111Tool(IPhysics iPhysics) {
		Boolean state = false;
		byte[] sendOrder = MyFactory.creatorForOrderMontage().download2111();
		if (MLog.isDebug) {
			MLog.d(TAG, "DownloadBinUpdate.resetConnector2505.sendOrder = " + ByteHexHelper.bytesToHexString(sendOrder));
		}
		if (sendOrder.length <= 0) {
			return state;
		}
		OutputStream outputStream = iPhysics.getOutputStream();
		try {
			outputStream.write(sendOrder);
		} catch (Exception e) {
			state = false;
			e.printStackTrace();
		}
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		if (DeviceFactoryManager.getInstance().getLinkMode() == DeviceFactoryManager.LINK_MODE_USB) {
			state = false;
		} else {
			state = true;
		}
		return state;
	}
	/**
	 * 自动化测试工具相关指令2111测试流程
	 *
	 * @param iPhysics
	 * @return
	 */
	public static Boolean downloadDPUDevice2111(IPhysics iPhysics) {
		String backOrder = "";
		Boolean state = false;
		// 记录是否已经生成过新流水号指令,用校验出错时判断
		Boolean hasGenerateNewCounter = false;
		byte[] sendOrder = MyFactory.creatorForOrderMontage().download2111();
		if (MLog.isDebug) {
			MLog.d(TAG, "DownloadBinUpdate.download2111.sendOrder = " + ByteHexHelper.bytesToHexString(sendOrder));
		}
		int flag = 0;
		if (sendOrder.length <= 0) {
			return state;
		}
		while (flag < 3) {
			Tools.writeDPUCommand(sendOrder, iPhysics);
			backOrder = iPhysics.getCommand();

			if (TextUtils.isEmpty(backOrder)) {
				flag++;
				continue;
			}
			if (MLog.isDebug) {
				MLog.d(TAG, "DownloadBinUpdate.download2111().backOrder = " + backOrder);
			}
			byte[] receiveOrder = ByteHexHelper.hexStringToBytes(backOrder);
			Analysis analysis = MyFactory.creatorForAnalysis();
			AnalysisData analysisData = analysis.analysis(sendOrder, receiveOrder);
			if (analysisData.getState()) {
				state = analysis.analysis2111(analysisData);
				break;
			} else {
				// 校验状态不通过时，重新生成新流水号指令，并只生成一次
				// 对于重卡无流水号则理解为多发一次
				if (hasGenerateNewCounter == false) {
					sendOrder = MyFactory.creatorForOrderMontage().download2111();
					hasGenerateNewCounter = true;
					flag = 0;
					if (MLog.isDebug) {
						MLog.d(TAG, "DownloadBinUpdate.download2111(). generate NewCounter sendOrder  = " + ByteHexHelper.bytesToHexString(sendOrder));
					}
				} else {
					flag++;
				}
			}
		}
		if (MLog.isDebug) {
			MLog.d(TAG, "DownloadBinUpdate.download2111().deviceinfo = " + state);
		}
		return state;
	}

	/**
	 * 自动化测试工具相关指令2117测试流程 license验证，用于具体接头验证
	 *
	 * @param iPhysics
	 * @return
	 */
	private static Boolean checkDPUDevice2117(IPhysics iPhysics) {
		String backOrder = "";
		Boolean state = false;
		// 记录是否已经生成过新流水号指令,用校验出错时判断
		Boolean hasGenerateNewCounter = false;
		// 985691000700
		String license985691000700 = "005C050000001565BCDA0BF11D5586270DBF73018C3DC03207200000000075200200B8000000000000000000000001000000000000000100000001000000910000000700000000000000000000002C2002001C000000000000000000000000DC014FE687B4053E736434C832318CA52A1CF48E3FF1F503E329161922476B6BA795D3BEA8E9277327EEEC3CB4D0F231A5D7D4859D0AD4F214EAD847197D752A43CAC74F7BDFAD241DDF8F75A35187E3B5B9505F1A10D473D8F2DEE79C8022626838D7B11C8CB33BCB6C37F306B56B2B69EA8C96C2225B8FFD4DA15EECB8261C7D922B3B5D3D7BFCD4B8AD11688C645C633DBC87CD2D53EC7794E4156018D97BD66DAD70CB4452B55025B0A60E46C4B429E26814C24700B96718790610D42EB1111253ABCDD09ED6E9F54F3DB8C1758FF3118213621E756B781A9E9483";
		String license985190077000 = "005C0500000066856491EE1764C598305C87B5AC7C9EE0432C0600000000C54059F16E674BD1021282E036AC8AC11D6E1612C524E3F092EDE3556888B813C51273B144670B916813BA75A77A43485259534C45525633332E303042C8FB0500DC014F1C69F1135D4013DB555E755F6DCA86E909404F60016A6A42942817E0723ED657423DF822D6734ACDF225567B0D07D55D37F9B8076D84F74FE98C3DDFFE9D32EBA7FCCD0F46A23486BDC9229947BB41481F0245A51F9AEE10E21282B838310614CDD24DC16C17759D6D6D5D51BCC33AE8817B892FC9E0E3C1F15A9D12A6F41DE2CD937C6919A0C0579748A0C90D0D3C6ABD5E85E216975B0CCC27C5C6DBBCDDB4CA28A754B556C8C3CAAE9C3362ACB4ACE6178C7DF7A8685F5C08917FCCA3AAE08078E0FC25457A68ECD593122B6CA9982E3799C918125C20E538";
		// 985690000002
		String license985690000002 = "005C05000000B24625CB58AB181F91E4FBE711D4DB3A8859CF200000000003000000000000000000000075600200B800000000000000000000000100000000000000555341464F52445634362E353000000000000000000000002C60020000DCB3D31F53D4F2BB942C157DD9D0675BF636DFEA4A47EE811B9DD4E976A6A11DDFE254D9BF44BD85483F01752F33D76C15C5D871D3F4899BE77A72A56CFCADF6D1558763A04A4FA0F0BA1A992040624B3CD95A73A88C809416B7A8A629C09E8F955C16F4862F9E0461BA1461F5B0DBE133F3D539AA98D099684CE97043EBF397E298C9228766FE7A42AEFB9315E8B341E6E7FD99AF5ABCF1D3760672C68882D3F654AA09F3840DCCF5541B7D8C5CBFAD43E06B278AF410947F5921CB7501E8B66D32BC073B056F6ECFF28EC8732C20A2561576405DE9E85ABA8BE4E404";
		String license985691251200 = "005C0500000013DC095A3ECE575F3B4F90C4A27D7C44F8108B2100000000000000000000000000000100000000000000555341464F52445634362E363600000000000000000000002C0002001C000000000000000000000004000000000000DC001AB5D5D9C469610638346B5C2B9EA928033BE19F876471CD811324C43D66EB2B9E63ACB5576A407BA0D8221A01F3B146F556F5BF8B9479CA4F46686DE05A7BF1BCC6B54CE00B391C7D0E6963A5E5621C1871A7049FB21E97895E5B02C0210CA6E9B65D3BA324AB1705F7CFB1DCDA52B8D7D5A10AA5D1D5842B2BC0D624189AA9D2D646342E9A7387E4E13D9E3F72CDCFEA7E5C9FF6B04B44C809930093BA2485AE5B35420DA5C2BFE0CAE48876BA6E6A084C2E0FD14DC6C8A70D1E03E53A3E058AB791C22594822ADE0FE42F2CEF0E06E417F6634E2F79ED3704CB";
		String license = license985691000700;//license985690000002;// license986590000300;//license986590000500;//license985690004800;//license986590000500;//license985691002600;//license985690003900;//license985691000700;//license985691002600;
		byte[] sendOrder = MyFactory.creatorForOrderMontage().checkDPU2117("", license);
		if (MLog.isDebug) {
			MLog.d(TAG, "DownloadBinUpdate.checkDPUDevice2117.sendOrder = " + ByteHexHelper.bytesToHexString(sendOrder));
		}
		int flag = 0;
		if (sendOrder.length <= 0) {
			return state;
		}
		while (flag < 3) {
			Tools.writeDPUCommand(sendOrder, iPhysics);
			backOrder = iPhysics.getCommand();

			if (TextUtils.isEmpty(backOrder)) {
				flag++;
				continue;
			}
			if (MLog.isDebug) {
				MLog.d(TAG, "DownloadBinUpdate.checkDPUDevice2117().backOrder = " + backOrder);
			}
			byte[] receiveOrder = ByteHexHelper.hexStringToBytes(backOrder);
			Analysis analysis = MyFactory.creatorForAnalysis();
			AnalysisData analysisData = analysis.analysis(sendOrder, receiveOrder);
			if (analysisData.getState() && Arrays.equals(new byte[]{0x00}, analysisData.getpReceiveBuffer())) {
				state = true;
				break;
			} else {
				// 校验状态不通过时，重新生成新流水号指令，并只生成一次
				// 对于重卡无流水号则理解为多发一次
				if (hasGenerateNewCounter == false) {
					sendOrder = MyFactory.creatorForOrderMontage().checkDPU2117("", license);
					hasGenerateNewCounter = true;
					flag = 0;
					if (MLog.isDebug) {
						MLog.d(TAG, "DownloadBinUpdate.checkDPUDevice2117(). generate NewCounter sendOrder  = " + ByteHexHelper.bytesToHexString(sendOrder));
					}
				} else {
					flag++;
				}
			}
		}
		if (MLog.isDebug) {
			MLog.d(TAG, "DownloadBinUpdate.checkDPUDevice2117().deviceinfo = " + state);
		}
		return state;
	}

	/**
	 * 数据传输测试
	 */
	public static Boolean testDPUDeviceWiFiSendAndReceive(IPhysics iPhysics, byte[] data, boolean isDiscontinuous, BufferedWriter bufferedWriter) {
		return testDPUDeviceWiFiSendAndReceive(iPhysics, data, isDiscontinuous, bufferedWriter, null);
	}

	/**
	 * 数据传输测试,带接收发送速率输出
	 */
	public static Boolean testDPUDeviceWiFiSendAndReceive(IPhysics iPhysics, byte[] data, boolean isDiscontinuous, BufferedWriter bufferedWriter, RateTestParameters rateTestParameters) {
		String backOrder = "";
		String sendOrderString = "";
		Boolean hasGenerateNewCounter = false;
		Boolean state = false;
		SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS", Locale.ENGLISH);
		byte[] sendOrder = MyFactory.creatorForOrderMontage().generateCommonCommand(new byte[]{0x21, 0x19, 0x02}, data);
		int flag = 0;
		if (sendOrder.length <= 0) {
			return state;
		}
		while (flag < 3) {
			if (MLog.isDebug) {
				MLog.d(TAG, "testDPUDeviceWiFiSendAndReceive 2119 .sendOrder  ");
			}
			if (bufferedWriter != null) {
				try {
					sendOrderString = ByteHexHelper.bytesToHexString(sendOrder);
					bufferedWriter.write("\nrequest(" + mSimpleDateFormat.format(new Date()) + "):" + sendOrderString);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			Tools.writeDPUCommand(sendOrder, iPhysics, 5000, isDiscontinuous, rateTestParameters);
			backOrder = iPhysics.getCommand();
			if (TextUtils.isEmpty(backOrder)) {
				flag++;
				continue;
			}
			if (MLog.isDebug) {
				MLog.d(TAG, "testDPUDeviceWiFiSendAndReceive 2119.backOrder = " + backOrder);
			}
			if (bufferedWriter != null) {
				try {
					bufferedWriter.write("\nAnswer(" + mSimpleDateFormat.format(new Date()) + "):" + backOrder);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			byte[] receiveOrder = ByteHexHelper.hexStringToBytes(backOrder);
			Analysis analysis = MyFactory.creatorForAnalysis();
			AnalysisData analysisData = analysis.analysis(sendOrder, receiveOrder);
			if (analysisData.getState()) {
				state = true;
				break;
			} else {
				// 校验状态不通过时，重新生成新流水号指令，并只生成一次
				// 对于重卡无流水号则理解为多发一次
				if (hasGenerateNewCounter == false) {
					sendOrder = MyFactory.creatorForOrderMontage().generateCommonCommand(new byte[]{0x21, 0x19, 0x02}, data);
					hasGenerateNewCounter = true;
					flag = 0;
					if (MLog.isDebug) {
						MLog.d(TAG, "testDPUDeviceWiFiSendAndReceive 2119. generate NewCounter sendOrder  = ");
					}
				} else {
					flag++;
				}
			}
		}
		if (MLog.isDebug) {
			MLog.d(TAG, "testDPUDeviceWiFiSendAndReceive 2119. end ");
		}
		return state;
	}

	/**
	 * 读取车辆电压 , 仅Dbscar II/III接头支持
	 *
	 * @author weizhongxuan
	 * @time 2016/10/13 14:01
	 */
	public void getVehicleVoltage() {
		if (mGetVehicleVoltageRunable == null) {
			mGetVehicleVoltageRunable = new GetVehicleVoltageRunable();
		}
		Thread t = new Thread(mGetVehicleVoltageRunable);
		t.start();
	}

	/**
	 * @author weizhongxuan
	 * @time 2016/10/13 14:02
	 */
	class GetVehicleVoltageRunable implements Runnable {

		@Override
		public void run() {
			String voltageValue = null;
			try {
				if (DeviceFactoryManager.getInstance().getDeviceName() == null || DeviceFactoryManager.getInstance().getDeviceName().contains("98454") ||
						DeviceFactoryManager.getInstance().getDeviceName().contains("98649")) {
					// 重卡不支持、二合一接头不支持
					mOnDownloadBinListener.OnDownloadBinListener(DPU_READ_VOLTAGE_NOT_SUPPORT, "");
					return;
				} else {
					voltageValue = readVehicleVoltageInfo();
					if (voltageValue == null) {
						mOnDownloadBinListener.OnDownloadBinListener(DPU_RESET_FAILED, "");
						return;
					} else {
						mOnDownloadBinListener.OnDownloadBinListener(DOWNLOADBIN_VEHICLE_VOLTAGE_VALUE, voltageValue);
					}
				}
			} catch (Exception ex) {
				mOnDownloadBinListener.OnDownloadBinListener(DPU_RESET_FAILED, "");
			}
		}
	}

	/**
	 * 读车辆电压信息
	 *
	 * @author weizhongxuan
	 * @time 2016/10/13 14:03
	 */
	private String readVehicleVoltageInfo() {
		String value = null;
		ArrayList<String> info = readVehicleVoltageInfo2120(mIPhysics);
		if (info == null) {
			return value;
		} else {
			// 解析结果
			if (info.size() > 0) {
				value = info.get(0);
			}
		}
		return value;
	}

	/**
	 * 读车辆电压信息
	 *
	 * @author weizhongxuan
	 * @time 2016/10/13 14:09
	 */
	private ArrayList<String> readVehicleVoltageInfo2120(IPhysics iPhysics) {
		ArrayList<String> info = null;
		String backOrder = "";
		byte[] sendOrder = MyFactory.creatorForOrderMontage().vehicleVoltageInfo2120();
		int flag = 0;
		if (sendOrder.length <= 0) {
			return info;
		}
		while (flag < 3) {
			Tools.writeDPUCommand(sendOrder, iPhysics);
			backOrder = iPhysics.getCommand();
			if (TextUtils.isEmpty(backOrder)) {
				flag++;
				continue;
			}
			byte[] receiveOrder = ByteHexHelper.hexStringToBytes(backOrder);
			Analysis analysis = MyFactory.creatorForAnalysis();
			AnalysisData analysisData = analysis.analysis(sendOrder, receiveOrder);
			if (analysisData.getState()) {
				info = analysis.analysis2120(analysisData);
				if (info.size() <= 0) {
					break;
				} else {
					return info;
				}
			} else {
				break;
			}
		}
		return info;
	}

	/**
	 * AIT诊断板半品测试 旧版boot
	 *
	 * @author xiefeihong
	 * @time 2017/02/15 11:26
	 */
	public static int aitBlankTest(IPhysics iPhysics, int maxWaitTime) {
		int status = -1;
		iPhysics.setCommand_wait(true);
		iPhysics.setCommand("");
		long milliseconds = (new Date()).getTime();
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		while (iPhysics.getCommand_wait()) {
			if (((new Date()).getTime() - milliseconds) > maxWaitTime) {
				iPhysics.setCommand_wait(false);
				iPhysics.setCommand("");
				break;
			}
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		String backOrder = iPhysics.getCommand();
		if (TextUtils.isEmpty(backOrder)) {
			status = -1;
		} else {
			byte[] receiveOrder = ByteHexHelper.hexStringToBytes(backOrder);
			if (ByteHexHelper.bytesIndexOf(receiveOrder, new byte[]{0x61, 0x19, 0x02, 0x00}) >= 0) {
				status = 0;
			} else if (ByteHexHelper.bytesIndexOf(receiveOrder, new byte[]{0x61, 0x19, 0x02, 0x01}) >= 0) {
				status = 1;
			}
		}
		return status;
	}

	/**
	 * AIT诊断板半品测试 新版boot 获取半成品测试分支 0 未知; 1 PCA; 2 老化测试; 0x21,0x19 获取接头相关工作信息
	 * ----------0x03 表示获取半成品工作方式(PCA测试，老化测试(io测试)） ----------回复指令
	 * 0x61,0x19,0x03,0x01 表示正在进行PCA测试 0x61,0x19,0x03,0x02 表示正在进行老化测试
	 *
	 * @author xiefeihong
	 * @time 2017/02/23 11:26
	 */
	public static int getAitBlankTestMethod(IPhysics iPhysics) {
		String backOrder = "";
		int state = 0;
		byte[] sendOrder = MyFactory.creatorForOrderMontage().generateCommonCommand(new byte[]{0x21, 0x19}, new byte[]{0x03});
		if (MLog.isDebug) {
			MLog.d(TAG, "DownloadBinUpdate.getAitBlankTestMethod.sendOrder = " + ByteHexHelper.bytesToHexString(sendOrder));
		}
		if (sendOrder.length <= 0) {
			return state;
		}
		int flag = 0;
		while (flag < 3) {
			Tools.writeDPUCommand(sendOrder, iPhysics);
			backOrder = iPhysics.getCommand();
			if (TextUtils.isEmpty(backOrder)) {
				flag++;
				continue;
			}
			if (MLog.isDebug) {
				MLog.d(TAG, "DownloadBinUpdate.getAitBlankTestMethod.backOrder = " + backOrder);
			}
			byte[] receiveOrder = ByteHexHelper.hexStringToBytes(backOrder);
			Analysis analysis = MyFactory.creatorForAnalysis();
			AnalysisData analysisData = analysis.analysis(sendOrder, receiveOrder);
			if (analysisData.getState()) {
				byte[] data = analysisData.getpReceiveBuffer();
				if (Arrays.equals(new byte[]{0x03, 0x01}, data)) {
					state = 1;
				} else if (Arrays.equals(new byte[]{0x03, 0x02}, data)) {
					state = 2;
				}
				break;
			} else {
				flag++;
			}
		}
		if (MLog.isDebug) {
			MLog.d(TAG, "DownloadBinUpdate.getAitBlankTestMethod state = " + state);
		}
		return state;
	}

	/**
	 * AIT诊断板半品测试 新版boot 半成品测试PCA测试分支,无需上下位机交互
	 *
	 * @author xiefeihong
	 * @time 2017/02/23 11:26
	 */
	public static boolean aitBlankTestPCAMethod(IPhysics iPhysics, int maxWaitTime) {
		return false;
	}

	/**
	 * AIT诊断板半品测试 新版boot 获取半成品老化测试分支 老化测试(io测试) 0x21,0x19 获取接头相关工作信息
	 * ----------0x02 获取老化测试信息 0x61,0x19,0x02,0x00,表示测试成功 0x61,0x19,0x02,0x01
	 * 表示测试不成功
	 *
	 * @author xiefeihong
	 * @time 2017/02/23 11:26
	 */
	public static boolean aitBlankTestAgeingMethod(IPhysics iPhysics, int maxWaitTime) {
		String backOrder = "";
		boolean state = false;
		byte[] sendOrder = MyFactory.creatorForOrderMontage().generateCommonCommand(new byte[]{0x21, 0x19}, new byte[]{0x02});
		if (MLog.isDebug) {
			MLog.d(TAG, "DownloadBinUpdate.aitBlankTestAgeingMethod.sendOrder = " + ByteHexHelper.bytesToHexString(sendOrder));
		}
		if (sendOrder.length <= 0) {
			return state;
		}
		int flag = 0;
		while (flag < 3) {
			Tools.writeDPUCommand(sendOrder, iPhysics);
			backOrder = iPhysics.getCommand();
			if (TextUtils.isEmpty(backOrder)) {
				flag++;
				continue;
			}
			if (MLog.isDebug) {
				MLog.d(TAG, "DownloadBinUpdate.aitBlankTestAgeingMethod.backOrder = " + backOrder);
			}
			byte[] receiveOrder = ByteHexHelper.hexStringToBytes(backOrder);
			Analysis analysis = MyFactory.creatorForAnalysis();
			AnalysisData analysisData = analysis.analysis(sendOrder, receiveOrder);
			if (analysisData.getState()) {
				byte[] data = analysisData.getpReceiveBuffer();
				if (Arrays.equals(new byte[]{0x02, 0x00}, data)) {
					state = true;
				} else if (Arrays.equals(new byte[]{0x02, 0x01}, data)) {
					state = false;
				}
				break;
			} else {
				flag++;
			}
		}
		return state;
	}


	/**
	 * 变更下位机当前运行模式
	 *
	 * @param iPhysics
	 * @return
	 */
	public static boolean changedDPURunningModeToSmartbox2109(IPhysics iPhysics) {
		String backOrder = "";
		Boolean isSucces = false;
		// 记录是否已经生成过新流水号指令,用校验出错时判断
		Boolean hasGenerateNewCounter = false;
		byte[] sendOrder = MyFactory.creatorForOrderMontage().generateCommonCommand(
				new byte[]{0x21, 0x09},
				new byte[]{0x01});
		if (MLog.isDebug) {
			MLog.d(TAG, "changedDPURunningModeToSmartbox2109 .sendOrder = " + ByteHexHelper.bytesToHexString(sendOrder));
		}
		int flag = 0;
		if (sendOrder.length <= 0) {
			return isSucces;
		}
		while (flag < 3) {
			Tools.writeDPUCommand(sendOrder, iPhysics);
			backOrder = iPhysics.getCommand();
			if (TextUtils.isEmpty(backOrder)) {
				// 重新生成新流水号指令，并只生成一次
				if (hasGenerateNewCounter == false) {
					sendOrder = MyFactory.creatorForOrderMontage().generateCommonCommand(
							new byte[]{0x21, 0x09},
							new byte[]{0x01});
					hasGenerateNewCounter = true;
					flag = 0;
					if (MLog.isDebug) {
						MLog.d(TAG, "changedDPURunningModeToSmartbox2109  generate NewCounter sendOrder  = " + ByteHexHelper.bytesToHexString(sendOrder));
					}
				} else {
					flag++;
				}
				continue;
			}
			if (MLog.isDebug) {
				MLog.d(TAG, "changedDPURunningModeToSmartbox2109.backOrder = " + backOrder);
			}
			byte[] receiveOrder = ByteHexHelper.hexStringToBytes(backOrder);
			Analysis analysis = MyFactory.creatorForAnalysis();
			AnalysisData analysisData = analysis.analysis(sendOrder, receiveOrder);
			if (analysisData.getState()) {
				byte[] receiveBuffer = analysisData.getpReceiveBuffer();
				if (MLog.isDebug) {
					MLog.d(TAG, "changedDPURunningModeToSmartbox2109 .data receiveBuffer = " + ByteHexHelper.bytesToHexString(receiveBuffer));
				}
				if (receiveBuffer != null && receiveBuffer[1] == 0) {
					isSucces = true;
				}
				break;
			} else {
				// 重新生成新流水号指令，并只生成一次
				if (hasGenerateNewCounter == false) {
					sendOrder = MyFactory.creatorForOrderMontage().generateCommonCommand(
							new byte[]{0x21, 0x09},
							new byte[]{0x01});
					hasGenerateNewCounter = true;
					flag = 0;
					if (MLog.isDebug) {
						MLog.d(TAG, "changedDPURunningModeToSmartbox2109  generate NewCounter sendOrder  = " + ByteHexHelper.bytesToHexString(sendOrder));
					}
				} else {
					flag++;
				}
			}
		}
		if (MLog.isDebug) {
			MLog.d(TAG, "changedDPURunningModeToSmartbox2109. end ");
		}
		return isSucces;
	}

	/**
	 * 读取当前状态Bootloader =0x00/download=0x01
	 *
	 * @return
	 */
	public static String currentState2114(IPhysics iPhysics, OnDownloadBinListener onDownloadBinListener) {
		byte[] sendOrder = MyFactory.creatorForOrderMontage().currentStatus2114();
		String backOrder = "";
		String runnningmode = "";
		int flag = 0;
		if (sendOrder.length <= 0) {
			return runnningmode;
		}
		while (flag < 3) {
			Tools.writeDPUCommand(sendOrder, iPhysics);
			backOrder = iPhysics.getCommand();
			if (TextUtils.isEmpty(backOrder)) {
				flag++;
				continue;
			}
			byte[] receiveOrder = ByteHexHelper.hexStringToBytes(backOrder);
			AnalysisData analysisData = MyFactory.creatorForAnalysis().analysis(sendOrder, receiveOrder, onDownloadBinListener);
			if (analysisData.getState()) {
				runnningmode = MyFactory.creatorForAnalysis().analysis2114(analysisData);
				break;
			} else {
				flag++;
			}
		}
		return runnningmode;
	}

	/**
	 * 切换到boot升级模式，启动更新固件命令
	 */
	private static boolean switchtoBootMode(IPhysics iPhysics, OnDownloadBinListener onDownloadBinListener) {
		boolean succeed = false;
		byte[] sendOrder = MyFactory.creatorForOrderMontage().updateFirmware2407();
		String backOrder = "";
		int flag = 0;
		if (sendOrder.length <= 0) {
			return succeed;
		}
		while (flag < 3) {
			Tools.writeDPUCommand(sendOrder, iPhysics);

			backOrder = iPhysics.getCommand();
			if (TextUtils.isEmpty(backOrder)) {
				flag++;
				continue;
			}
			byte[] receiveOrder = ByteHexHelper.hexStringToBytes(backOrder);
			AnalysisData analysisData = MyFactory.creatorForAnalysis().analysis(sendOrder, receiveOrder, onDownloadBinListener);
			if (MyFactory.creatorForAnalysis().analysis2407(analysisData) == true) {
				succeed = true;
				break;
			} else {
				flag++;
			}
		}
		return succeed;
	}

	/**
	 * 接头复位 ，不支持一代重卡。有线诊断模式，复位标志为失败。
	 *
	 * @param iPhysics
	 * @return
	 */
	public static Boolean resetDPUDevice2505(IPhysics iPhysics) {
		Boolean state = false;
		byte[] sendOrder = MyFactory.creatorForOrderMontage().resetConnector2505();
		if (MLog.isDebug) {
			MLog.d(TAG, "DownloadBinUpdate.resetConnector2505.sendOrder = " + ByteHexHelper.bytesToHexString(sendOrder));
		}
		if (sendOrder.length <= 0) {
			return state;
		}
		OutputStream outputStream = iPhysics.getOutputStream();
		try {
			outputStream.write(sendOrder);
		} catch (Exception e) {
			state = false;
			e.printStackTrace();
		}
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		if (DeviceFactoryManager.getInstance().getLinkMode() == DeviceFactoryManager.LINK_MODE_USB) {
			if(DeviceFactoryManager.getInstance().isStandAloneChip(iPhysics.getContext()) ||
					DeviceFactoryManager.getInstance().isStandAloneChipEthernet(iPhysics.getContext())){
				state = true;
			}
			else {
				state = false;
			}
		} else {
			state = true;
		}
		return state;
	}

	public static String dpuDateSoftcodeRegister2131(IPhysics iPhysics, int datNum, String datDate) {
		String succeed = "";
		String backOrder = "";
		byte[] sendOrder = MyFactory.creatorForOrderMontage().dpuDateSoftcodeRegister2131(datNum, datDate);
		if (MLog.isDebug) {
			MLog.d("ykw", "dpuDateSoftcodeRegister2131.sendOrder = " + ByteHexHelper.bytesToHexString(sendOrder));
		}
		int flag = 0;
		if (sendOrder.length <= 0) {
			return succeed;
		}
		while (flag < 3) {
			Tools.writeDPUCommand(sendOrder, iPhysics);
			backOrder = iPhysics.getCommand();
			if (MLog.isDebug) {
				MLog.d("ykw", "dpuDateSoftcodeRegister2131().backOrder=" + backOrder);
			}
			if (TextUtils.isEmpty(backOrder)) {
				flag++;
				continue;
			}
			byte[] receiveOrder = ByteHexHelper.hexStringToBytes(backOrder);
			Analysis analysis = MyFactory.creatorForAnalysis();
			AnalysisData analysisData = analysis.analysis(sendOrder, receiveOrder);
			if (analysisData.getState()) {
				succeed = analysis.analysis2131(analysisData);
				if (MLog.isDebug) {
					MLog.d("ykw", "dpuDateSoftcodeRegister2131====" + succeed);
				}
				break;
			} else {
				flag++;
			}
		}
		return succeed;
	}

	/**
	 * ykw
	 * 发送软件包ID+版本号
	 */
	public static boolean sendSoftidVersion2134(IPhysics iPhysics, String softId, String versionNo) {
		boolean succeed = false;
		String backOrder = "";
		String softPackageId = softId.toUpperCase(Locale.ENGLISH);
		if (MLog.isDebug) {
			MLog.d("ykw", "2134 参数:" + softId + ",versionNo:" + versionNo + " ,转化后:" + softPackageId);
		}
		byte[] sendOrder = MyFactory.creatorForOrderMontage().softVer2134(softPackageId, versionNo);
		if (MLog.isDebug) {
			MLog.d("ykw", "sendSoftidVersion2134.sendOrder = " + ByteHexHelper.bytesToHexString(sendOrder));
		}
		int flag = 0;
		if (sendOrder.length <= 0) {
			return succeed;
		}
		while (flag < 3) {
			Tools.writeDPUCommand(sendOrder, iPhysics);
			backOrder = iPhysics.getCommand();
			if (MLog.isDebug) {
				MLog.d("ykw", "sendSoftidVersion2134().backOrder=" + backOrder);
			}
			if (TextUtils.isEmpty(backOrder)) {
				flag++;
				continue;
			}
			byte[] receiveOrder = ByteHexHelper.hexStringToBytes(backOrder);
			Analysis analysis = MyFactory.creatorForAnalysis();
			AnalysisData analysisData = analysis.analysis(sendOrder, receiveOrder);
			if (analysisData.getState()) {
				if (analysis.analysis21xx(analysisData)) {
					succeed = true;
				}
				break;
			} else {
				flag++;
			}
		}
		if (MLog.isDebug) {
			MLog.d("ykw", "sendSoftidVersion2134 返回校验:" + succeed);
		}
		return succeed;
	}

	/**
	 * ykw
	 * 发送激活码
	 */
	public static boolean sendActivationCode2133(IPhysics iPhysics, String effectDate, String sysCode) {
		boolean succeed = false;
		String backOrder = "";
		byte[] sendOrder = MyFactory.creatorForOrderMontage().activation2133(effectDate, sysCode);
		if (MLog.isDebug) {
			MLog.d("ykw", "sendActivationCode2133.sendOrder = " + ByteHexHelper.bytesToHexString(sendOrder));
		}
		int flag = 0;
		if (sendOrder.length <= 0) {
			return succeed;
		}
		while (flag < 3) {
			Tools.writeDPUCommand(sendOrder, iPhysics);
			backOrder = iPhysics.getCommand();
			if (MLog.isDebug) {
				MLog.d("ykw", "sendActivationCode2133.backOrder=" + backOrder);
			}
			if (TextUtils.isEmpty(backOrder)) {
				flag++;
				continue;
			}
			byte[] receiveOrder = ByteHexHelper.hexStringToBytes(backOrder);
			Analysis analysis = MyFactory.creatorForAnalysis();
			AnalysisData analysisData = analysis.analysis(sendOrder, receiveOrder);
			if (analysisData.getState()) {
				if (analysis.analysis21xx(analysisData)) {
					succeed = true;
				}
				break;
			} else {
				flag++;
			}
		}
		if (MLog.isDebug) {
			MLog.d("ykw", "sendActivationCode2133 返回校验:" + succeed);
		}
		return succeed;
	}

	/**
	 * 读取随机数
	 *
	 * @return
	 **/
	public static byte[] readRandom212101(IPhysics iPhysics) {
		byte[] deviceinfo = null;
		String backOrder = "";
		// 记录是否已经生成过新流水号指令,用校验出错时判断
		Boolean hasGenerateNewCounter = false;
		byte[] sendOrder = MyFactory.creatorForOrderMontage().readRandom212101();
		if (MLog.isDebug) {
			MLog.d("yhx", "DownloadBinUpdate.readRandom212101().sendOrder = " + ByteHexHelper.bytesToHexString(sendOrder));
		}
		int flag = 0;
		if (sendOrder.length <= 0) {
			return deviceinfo;
		}
		while (flag < 3) {
			Tools.writeDPUCommand(sendOrder, iPhysics, 2000);
			backOrder = iPhysics.getCommand();
			if (TextUtils.isEmpty(backOrder)) {
				// 重新生成新流水号指令，并只生成一次
				if (hasGenerateNewCounter == false) {
					sendOrder = MyFactory.creatorForOrderMontage().readRandom212101();
					hasGenerateNewCounter = true;
					flag = 0;
					if (MLog.isDebug) {
						MLog.d("yhx", "DownloadBinUpdate.readRandom212101(). generate NewCounter sendOrder  = " + ByteHexHelper.bytesToHexString(sendOrder));
					}
				} else {
					flag++;
				}
				continue;
			}
			if (MLog.isDebug) {
				MLog.d("yhx", "DownloadBinUpdate.readRandom212101().backOrder = " + backOrder);
			}

			byte[] receiveOrder = ByteHexHelper.hexStringToBytes(backOrder);
			Analysis analysis = MyFactory.creatorForAnalysis();
			AnalysisData analysisData = analysis.analysis(sendOrder, receiveOrder);
			if (analysisData.getState()) {
				byte[] pReceiveBytes = analysisData.getpReceiveBuffer();
				LogUtil.INSTANCE.d("yhx", "pReceiveBytes=" + ByteHexHelper.bytesToHexString(pReceiveBytes));
				if (pReceiveBytes != null && pReceiveBytes.length > 1) {
					deviceinfo = new byte[pReceiveBytes.length - 1];
					for (int i = 0; i < deviceinfo.length; i++) {
						deviceinfo[i] = pReceiveBytes[i + 1];
					}
				}
				if (deviceinfo == null) {
					// 重新生成新流水号指令，并只生成一次
					if (hasGenerateNewCounter == false) {
						sendOrder = MyFactory.creatorForOrderMontage().readRandom212101();
						hasGenerateNewCounter = true;
						flag = 0;
						if (MLog.isDebug) {
							MLog.d("yhx", "DownloadBinUpdate.readRandom212101(). generate NewCounter sendOrder  = " + ByteHexHelper.bytesToHexString(sendOrder));
						}
						continue;
					} else {
						break;
					}
				} else {
					break;
				}
			} else {
				// 校验状态不通过时，重新生成新流水号指令，并只生成一次
				// 对于重卡无流水号则理解为多发一次
				if (hasGenerateNewCounter == false) {
					sendOrder = MyFactory.creatorForOrderMontage().readRandom212101();
					hasGenerateNewCounter = true;
					flag = 0;
					if (MLog.isDebug) {
						MLog.d(TAG, "DownloadBinUpdate.readRandom212101(). generate NewCounter sendOrder  = " + ByteHexHelper.bytesToHexString(sendOrder));
					}
				} else {
					flag++;
				}
			}
		}
		if (MLog.isDebug) {
			MLog.d("yhx", "DownloadBinUpdate.readRandom212101().deviceinfo = " + ByteHexHelper.bytesToHexString(deviceinfo));
		}

		return deviceinfo;
	}

	/**
	 * 验证加密数据
	 */
	public static boolean sendEncResult212102(IPhysics iPhysics, String encResult) {
		boolean succeed = false;
		String backOrder = "";
		byte[] sendOrder = MyFactory.creatorForOrderMontage().sendEncResult212102(encResult);
		if (MLog.isDebug) {
			MLog.d("yhx", "sendEncResult212102.sendOrder = " + ByteHexHelper.bytesToHexString(sendOrder));
		}
		int flag = 0;
		if (sendOrder.length <= 0) {
			return succeed;
		}
		while (flag < 3) {
			Tools.writeDPUCommand(sendOrder, iPhysics);
			backOrder = iPhysics.getCommand();
			if (MLog.isDebug) {
				MLog.d("yhx", "sendEncResult212102.backOrder=" + backOrder);
			}
			if (TextUtils.isEmpty(backOrder)) {
				flag++;
				continue;
			}
			byte[] receiveOrder = ByteHexHelper.hexStringToBytes(backOrder);
			Analysis analysis = MyFactory.creatorForAnalysis();
			AnalysisData analysisData = analysis.analysis(sendOrder, receiveOrder);
			LogUtil.INSTANCE.d("yhx", "212102.pReceiveData=" + ByteHexHelper.bytesToHexString(analysisData.getpReceiveBuffer()) + ",state=" + analysisData.getState());
			if (analysisData.getState()) {
				byte[] pReceiveBuffer = analysisData.getpReceiveBuffer();
				if (pReceiveBuffer != null && pReceiveBuffer.length > 1) {
					if ("00".equals(ByteHexHelper.byteToHexString(pReceiveBuffer[1]))) {
						succeed = true;
					}
				}
				break;
			} else {
				flag++;
			}
		}
		if (MLog.isDebug) {
			MLog.d("yhx", "sendEncResult212102 返回校验:" + succeed);
		}
		return succeed;
	}
	/**MUC和SE绑定信息请求：---- 芯片生产烧录用
	 REQ: 55 AA F0 F8 LEN(H) LEN(L) NUM 21  18 00CS
	 ANS: 55 AA F8 F0 LEN(H) LEN(L) NUM 61  18 00数据内容(N个字节) CS
	 */
	public static byte[] bindMCUAndSE(IPhysics iPhysics) {
		byte[] sendOrder = MyFactory.creatorForOrderMontage().generateCommonCommand(
				new byte[]{0x21,0x18},
				new byte[]{0x00});
		byte[] receiveBuffer = Tools.dpuCommonCommandOperation(iPhysics, sendOrder);
		if (MLog.isDebug) {
			MLog.d(TAG, "bindMCUAndSE =" + ByteHexHelper.bytesToHexString(receiveBuffer));
		}
		if (receiveBuffer != null && receiveBuffer.length >= 1) {
			return receiveBuffer;
		}
		return null;
	}
	/**
	 * 请求接头和SE信息
	 * REQ: 55 AA F0 F8 LEN(H) LEN(L) NUM 21  18 01 stdBaseInfo,请求类型 CS
	 * ANS: 55 AA F8 F0 LEN(H) LEN(L) NUM 61  18 01 数据内容(N个字节) CS
	 */
	public static byte[] getSEInformation(IPhysics iPhysics) {
		byte[] sendOrder = MyFactory.creatorForOrderMontage().generateCommonCommand(
				new byte[]{0x21,0x18},
				new byte[]{0x01});
		byte[] receiveBuffer = Tools.dpuCommonCommandOperation(iPhysics, sendOrder);
		if (receiveBuffer != null && receiveBuffer.length > 1) {
			//去掉子命令字01
			byte[] receiveBufferData = new byte[receiveBuffer.length-1];
			System.arraycopy(receiveBuffer,1,receiveBufferData,0,receiveBuffer.length-1);
			if (MLog.isDebug) {
				MLog.d(TAG, "getSEInformation =" + ByteHexHelper.bytesToHexString(receiveBufferData));
			}
			return receiveBufferData;
		}
		return null;
	}

	/**
	 * SE安全认证请求
	 * REQ: 55 AA F0 F8 LEN(H) LEN(L) NUM 21  18 02 数据内容(N个字节) CS
	 * ANS: 55 AA F8 F0 LEN(H) LEN(L) NUM 61  18 02 数据内容(N个字节) CS
	 */
	public static byte[] seSecurityAuthenticationRequest(IPhysics iPhysics, byte[] encResult) {
		byte[] sendOrder = MyFactory.creatorForOrderMontage().generateCommonCommand(
				new byte[]{0x21,0x18,0x02}, encResult);
		byte[] receiveBuffer = Tools.dpuCommonCommandOperation(iPhysics, sendOrder);
		if (MLog.isDebug) {
			MLog.d(TAG, "seSecurityAuthenticationRequest =" + ByteHexHelper.bytesToHexString(receiveBuffer));
		}
		if (receiveBuffer != null && receiveBuffer.length >= 1) {
			//去掉子命令字02
			byte[] receiveBufferData = new byte[receiveBuffer.length-1];
			System.arraycopy(receiveBuffer,1,receiveBufferData,0,receiveBuffer.length-1);
			if (MLog.isDebug) {
				MLog.d(TAG, "getSEInformation =" + ByteHexHelper.bytesToHexString(receiveBufferData));
			}
			return receiveBufferData;
		}
		return null;
	}

	/**
	 * SE安全认证请求
	 * REQ: 55 AA F0 F8 LEN(H) LEN(L) NUM 21  18 01 数据内容(N个字节) CS
	 * ANS: 55 AA F8 F0 LEN(H) LEN(L) NUM 61  18 01 数据内容(N个字节) CS
	 */
	public static byte[] seSecurityAuthenticationRequest211801(IPhysics iPhysics, byte[] encResult) {
		byte[] sendOrder = MyFactory.creatorForOrderMontage().generateCommonCommand(
				new byte[]{0x21,0x18,0x01}, encResult);
		LogUtil.INSTANCE.d("sarah", "发送211801=" + ByteHexHelper.bytesToHexString(sendOrder));
		byte[] receiveBuffer = Tools.dpuCommonCommandOperation(iPhysics, sendOrder);
		if (MLog.isDebug) {
			MLog.d(TAG, "seSecurityAuthenticationRequest =" + ByteHexHelper.bytesToHexString(receiveBuffer));
		}
		if (receiveBuffer != null && receiveBuffer.length >= 1) {
			//去掉子命令字02
			byte[] receiveBufferData = new byte[receiveBuffer.length-1];
			System.arraycopy(receiveBuffer,1,receiveBufferData,0,receiveBuffer.length-1);
			if (MLog.isDebug) {
				MLog.d(TAG, "getSEInformation =" + ByteHexHelper.bytesToHexString(receiveBufferData));
			}
			return receiveBufferData;
		}
		return null;
	}

	/**
	 * 发命令获取vin码
	 * REQ: 55 AA F0 F8 LEN(H) LEN(L) NUM 03  27 01 数据内容(N个字节) CS
	 */
	public static byte[] seSecurityAuthenticationRequestGetvin(IPhysics iPhysics, byte[] encResult) {
		byte[] sendOrder = MyFactory.creatorForOrderMontage().generateCommonCommand(
				new byte[]{0x27,0x01}, encResult);
		byte[] receiveBuffer = Tools.dpuCommonCommandOperation(iPhysics, sendOrder);
		if (MLog.isDebug) {
			MLog.d(TAG, "seSecurityAuthenticationRequest =" + ByteHexHelper.bytesToHexString(receiveBuffer));
		}
		if (receiveBuffer != null && receiveBuffer.length >= 1) {
			//去掉子命令字02
			byte[] receiveBufferData = new byte[receiveBuffer.length-1];
			System.arraycopy(receiveBuffer,1,receiveBufferData,0,receiveBuffer.length-1);
			if (MLog.isDebug) {
				MLog.d(TAG, "getSEInformation =" + ByteHexHelper.bytesToHexString(receiveBufferData));
			}
			return receiveBufferData;
		}
		return null;
	}
}
