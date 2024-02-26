package com.cnlaunch.physics.downloadbin.util;


import com.cnlaunch.physics.utils.ByteHexHelper;
import com.cnlaunch.physics.utils.MLog;
import com.power.baseproject.utils.log.LogUtil;

import java.io.File;
import java.nio.Buffer;
import java.text.DecimalFormat;
import java.util.Calendar;

/**
 * 蓝牙byte[]数据拼接
 * @author Administrator
 *
 */
public class OrderMontageForCar extends OrderMontage{
	public static final  String  TAG = "OrderMontageForCar";
	// 起始标志
	/** 起始标志 */
	private final static String startCode = "55aa";

	/** 目标地址 */
	private final static String target = "F0";

	/** smartbox30_linux 目标地址 */
	private final static String smartbox30_linux_target = "F1";

	/** 源地址 */
	private final static String source = "F8";

	/** 计数器 */
	private static String counter = "";

	/** 包长度 */
	private static String packLengths = "";

	/** 命令字command word CW */
	private static String commandWord = "";

	/** 数据区 */
	private static String dataArea = "";

	/** 包校验 */
	private static String packVerify = "";
	private static OrderMontageForCar instance;
	private OrderMontageForCar() {
		
	}
	public static OrderMontageForCar getInstance() {
		if (instance == null) {
			instance = new OrderMontageForCar();
		}
		return instance;
	}

	/**
	 * 读取时钟
	 * 
	 * @return
	 */
	public byte[] readClock2102() {
		byte[] o2102 = null ;
		String counters = ByteHexHelper.RandomMethod();
		while (counters.equalsIgnoreCase(counter)) {
			counters = ByteHexHelper.RandomMethod();
		}
		counter = counters;
		commandWord = "2102";
		dataArea = "";
		packLengths = ByteHexHelper.packLength(counter + commandWord + dataArea);
		packVerify = ByteHexHelper.packVerify(target, source, packLengths, counter, commandWord, dataArea);
		String order = startCode + target + source + packLengths + counter + commandWord + dataArea + packVerify;
		
		//Log.e("bcf","蓝牙通讯的完整指令为： " + order);
		o2102 = ByteHexHelper.hexStringToBytes(order);
		return o2102 ;
	}

	/**
	 * 拦截指令,6505
	 *
	 * @return
	 */
	public byte[] resetConnector6505(String counters) {
		byte[] o6505 = null;
		counter = counters;
		commandWord = "6505";
		dataArea = "00";
		packLengths = ByteHexHelper.packLength(counter + commandWord + dataArea);
		packVerify = ByteHexHelper.packVerify(target, source, packLengths, counter,
		commandWord, dataArea);
		String order = startCode + target + source + packLengths + counter
		+ commandWord + dataArea + packVerify;
		o6505 = ByteHexHelper.hexStringToBytes(order);
		return o6505;
	}

	/**
	 * 设置时钟
	 * 
	 * @return
	 */
	public byte[] setClock2101() {
		byte[] o2101 = null;
		String counters = ByteHexHelper.RandomMethod();
		while (counters.equalsIgnoreCase(counter)) {
			counters = ByteHexHelper.RandomMethod();
		}
		counter = counters;
		commandWord = "2101";
		dataArea = ByteHexHelper.currentData();
		packLengths = ByteHexHelper.packLength(counter + commandWord + dataArea);
		packVerify = ByteHexHelper.packVerify(target, source, packLengths, counter,
				commandWord, dataArea);
		String order = startCode + target + source + packLengths + counter
				+ commandWord + dataArea + packVerify;
		
		//Log.e("bcf","蓝牙通讯的完整指令为： " + order);
		o2101 = ByteHexHelper.hexStringToBytes(order);
		return o2101;
	}

	/**
	 * 读取DPU接头硬件版本信息
	 * 
	 * @return
	 */
	public byte[] DPUVerInfo2103() {
		byte[] o2103 = null;
		String counters = ByteHexHelper.RandomMethod();
		while (counters.equalsIgnoreCase(counter)) {
			counters = ByteHexHelper.RandomMethod();
		}
		counter = counters;
		commandWord = "2103";
		dataArea = "";
		packLengths = ByteHexHelper.packLength(counter + commandWord + dataArea);
		packVerify = ByteHexHelper.packVerify(target, source, packLengths, counter,
				commandWord, dataArea);
		String order = startCode + target + source + packLengths + counter
				+ commandWord + dataArea + packVerify;
		
		//Log.e("bcf","蓝牙通讯的完整指令为： " + order);
		o2103 = ByteHexHelper.hexStringToBytes(order);
		return o2103;
	}

	/**
	 * 读取DPU接头库文件版本(车型名称，车型版本，语言)
	 * 
	 * @return
	 */
	public byte[] DPUKuVer2104() {
		byte[] o2104 = null;
		String counters = ByteHexHelper.RandomMethod();
		while (counters.equalsIgnoreCase(counter)) {
			counters = ByteHexHelper.RandomMethod();
		}
		counter = counters;
		commandWord = "2104";
		dataArea = "";
		packLengths = ByteHexHelper.packLength(counter + commandWord + dataArea);
		packVerify = ByteHexHelper.packVerify(target, source, packLengths, counter,
				commandWord, dataArea);
		String order = startCode + target + source + packLengths + counter
				+ commandWord + dataArea + packVerify;
		
		//Log.e("bcf","蓝牙通讯的完整指令为： " + order);
		o2104 = ByteHexHelper.hexStringToBytes(order);
		return o2104;
	}

	/**
	 * 取DPU接头软件版本(boot,download,诊断软件)
	 * 
	 * @return
	 */
	public byte[] DPUVer2105() {
		byte[] o2105 = null;
		String counters = ByteHexHelper.RandomMethod();
		while (counters.equalsIgnoreCase(counter)) {
			counters = ByteHexHelper.RandomMethod();
		}
		counter = counters;
		commandWord = "2105";
		dataArea = "";
		packLengths = ByteHexHelper.packLength(counter + commandWord + dataArea);
		packVerify = ByteHexHelper.packVerify(target, source, packLengths, counter,
				commandWord, dataArea);
		String order = startCode + target + source + packLengths + counter
				+ commandWord + dataArea + packVerify;
		//Log.e("bcf","蓝牙通讯的完整指令为： " + order);
		o2105 = ByteHexHelper.hexStringToBytes(order);
		return o2105;
	}

	/**
	 * 取DPU接头防篡改标识
	 * 
	 * @return
	 */
	public byte[] DPU2106() {
		byte[] o2106 = null;
		String counters = ByteHexHelper.RandomMethod();
		while (counters.equalsIgnoreCase(counter)) {
			counters = ByteHexHelper.RandomMethod();
		}
		counter = counters;
		commandWord = "2106";
		dataArea = "";
		packLengths = ByteHexHelper.packLength(counter + commandWord + dataArea);
		packVerify = ByteHexHelper.packVerify(target, source, packLengths, counter,
				commandWord, dataArea);
		String order = startCode + target + source + packLengths + counter
				+ commandWord + dataArea + packVerify;
		
		//Log.e("bcf","蓝牙通讯的完整指令为： " + order);
		o2106 = ByteHexHelper.hexStringToBytes(order);
		return o2106;
	}

	/**
	 * 写DPU接头序列号
	 * 
	 * @param mode
	 *            写入模式
	 * @param serialNum
	 *            序列号
	 * @return
	 */

	public byte[] writeDPUSerialNum2107(String mode, String serialNum) {
		byte[] o2107 = null;
		String counters = ByteHexHelper.RandomMethod();
		while (counters.equalsIgnoreCase(counter)) {
			counters = ByteHexHelper.RandomMethod();
		}
		counter = counters;
		commandWord = "2107";
		dataArea = mode + serialNum;
		packLengths = ByteHexHelper.packLength(counter + commandWord + dataArea);
		packVerify = ByteHexHelper.packVerify(target, source, packLengths, counter,
				commandWord, dataArea);
		String order = startCode + target + source + packLengths + counter
				+ commandWord + dataArea + packVerify;
		
		//Log.e("bcf","蓝牙通讯的完整指令为： " + order);
		o2107 = ByteHexHelper.hexStringToBytes(order);
		return o2107;

	}

	/**
	 * 设置蓝牙名称 蓝牙名称最多16个半角字符
	 * 
	 * @param name
	 * @return
	 */
	public byte[] setBluetoothName2108(String name) {
		byte[] o2108 = null;
		String counters = ByteHexHelper.RandomMethod();
		while (counters.equalsIgnoreCase(counter)) {
			counters = ByteHexHelper.RandomMethod();
		}
		counter = counters;
		commandWord = "2108";
		dataArea = name;
		packLengths = ByteHexHelper.packLength(counter + commandWord + dataArea);
		packVerify = ByteHexHelper.packVerify(target, source, packLengths, counter,
				commandWord, dataArea);
		String order = startCode + target + source + packLengths + counter
				+ commandWord + dataArea + packVerify;
		
		//Log.e("bcf","蓝牙通讯的完整指令为： " + order);
		o2108 = ByteHexHelper.hexStringToBytes(order);
		return o2108;
	}

	/**
	 * 复位DPU运行模式(用于切换诊断模式时) GetMode=0(返回当前工作模式,不会复位) 四种模式切换： SMARTBOX=1;
	 * MYCAR=2; CREADER=3; CRECORDER=4; -------- OBD=5; QUICKDIAG=6;
	 * 
	 * @param mode
	 * @return
	 */
	public byte[] transferDPUMode2109(int mode) {
		byte[] o2109 = null;
		String counters = ByteHexHelper.RandomMethod();
		while (counters.equalsIgnoreCase(counter)) {
			counters = ByteHexHelper.RandomMethod();
		}
		counter = counters;
		commandWord = "2109";
		DecimalFormat format = new DecimalFormat("00");
		dataArea = format.format(mode);
		packLengths = ByteHexHelper.packLength(counter + commandWord + dataArea);
		packVerify = ByteHexHelper.packVerify(target, source, packLengths, counter,
				commandWord, dataArea);
		String order = startCode + target + source + packLengths + counter
				+ commandWord + dataArea + packVerify;
		
		//Log.e("bcf","蓝牙通讯的完整指令为： " + order);
		o2109 = ByteHexHelper.hexStringToBytes(order);
		return o2109;
	}

	/**
	 * 清除flash数据
	 * 
	 * @param code
	 * @return
	 */
	public byte[] clearFlash210A(String code) {
		byte[] o210A = null;
		String counters = ByteHexHelper.RandomMethod();
		while (counters.equalsIgnoreCase(counter)) {
			counters = ByteHexHelper.RandomMethod();
		}
		counter = counters;
		commandWord = "210A";
		dataArea = code;
		packLengths = ByteHexHelper.packLength(counter + commandWord + dataArea);
		packVerify = ByteHexHelper.packVerify(target, source, packLengths, counter,
				commandWord, dataArea);
		String order = startCode + target + source + packLengths + counter
				+ commandWord + dataArea + packVerify;
		
		//Log.e("bcf","蓝牙通讯的完整指令为： " + order);
		o210A = ByteHexHelper.hexStringToBytes(order);
		return o210A;
	}

	/**
	 * 设置或修改安全密码指令
	 * 
	 * @param oldPw
	 * @param newPw
	 * @return
	 */
	public byte[] modifyPw210B(String oldPw, String newPw) {
		byte[] o210B = null;
		String counters = ByteHexHelper.RandomMethod();
		while (counters.equalsIgnoreCase(counter)) {
			counters = ByteHexHelper.RandomMethod();
		}
		counter = counters;
		commandWord = "210B";
		dataArea = oldPw + newPw;
		packLengths = ByteHexHelper.packLength(counter + commandWord + dataArea);
		packVerify = ByteHexHelper.packVerify(target, source, packLengths, counter,
				commandWord, dataArea);
		String order = startCode + target + source + packLengths + counter
				+ commandWord + dataArea + packVerify;
		
		//Log.e("bcf","蓝牙通讯的完整指令为： " + order);
		o210B = ByteHexHelper.hexStringToBytes(order);
		return o210B;
	}

	/**
	 * 恢复初始密码
	 * 
	 * @param str
	 *            加密字符串
	 * @return
	 */
	public byte[] resumePw210f(String str) {
		byte[] o210f = null;
		String counters = ByteHexHelper.RandomMethod();
		while (counters.equalsIgnoreCase(counter)) {
			counters = ByteHexHelper.RandomMethod();
		}
		counter = counters;
		commandWord = "210f";
		dataArea = str;
		packLengths = ByteHexHelper.packLength(counter + commandWord + dataArea);
		packVerify = ByteHexHelper.packVerify(target, source, packLengths, counter,
				commandWord, dataArea);
		String order = startCode + target + source + packLengths + counter
				+ commandWord + dataArea + packVerify;
		
		//Log.e("bcf","蓝牙通讯的完整指令为： " + order);
		o210f = ByteHexHelper.hexStringToBytes(order);
		return o210f;
	}

	/**
	 * 验证安全密码指令
	 * 
	 * @param oldPw
	 * @return
	 */
	public byte[] resumePw2110(String oldPw) {
		byte[] o2110 = null;
		String counters = ByteHexHelper.RandomMethod();
		while (counters.equalsIgnoreCase(counter)) {
			counters = ByteHexHelper.RandomMethod();
		}
		counter = counters;
		commandWord = "2110";
		dataArea = ByteHexHelper.dpuString(oldPw);
		packLengths = ByteHexHelper.packLength(counter + commandWord + dataArea);
		packVerify = ByteHexHelper.packVerify(target, source, packLengths, counter,
				commandWord, dataArea);
		String order = startCode + target + source + packLengths + counter
				+ commandWord + dataArea + packVerify;
		
		//Log.e("bcf","蓝牙通讯的完整指令为： " + order);
		o2110 = ByteHexHelper.hexStringToBytes(order);
		return o2110;
	}

	/**
	 * 跳转至download代码入口
	 * 
	 * @return
	 */
	public byte[] download2111() {
		byte[] o2111 = null;
		String counters = ByteHexHelper.RandomMethod();
		while (counters.equalsIgnoreCase(counter)) {
			counters = ByteHexHelper.RandomMethod();
		}
		counter = counters;
		commandWord = "2111";
		dataArea = "";
		packLengths = ByteHexHelper.packLength(counter + commandWord + dataArea);
		packVerify = ByteHexHelper.packVerify(target, source, packLengths, counter,
				commandWord, dataArea);
		String order = startCode + target + source + packLengths + counter
				+ commandWord + dataArea + packVerify;
		
		//Log.e("bcf","蓝牙通讯的完整指令为： " + order);
		o2111 = ByteHexHelper.hexStringToBytes(order);
		return o2111;
	}

	/***
	 * 写接头配置文件
	 * 
	 * @param fileLength
	 *            待写入数据文件长度
	 * @param content
	 *            待写入文件内容
	 * @return
	 */
	public byte[] writeConFile2112(Short fileLength, Buffer content) {
		byte[] o2112 = null;
		String counters = ByteHexHelper.RandomMethod();
		while (counters.equalsIgnoreCase(counter)) {
			counters = ByteHexHelper.RandomMethod();
		}
		counter = counters;
		commandWord = "2112";
		dataArea = fileLength + "" + content.toString();
		packLengths = ByteHexHelper.packLength(counter + commandWord + dataArea);
		packVerify = ByteHexHelper.packVerify(target, source, packLengths, counter,
				commandWord, dataArea);
		String order = startCode + target + source + packLengths + counter
				+ commandWord + dataArea + packVerify;
		
		//Log.e("bcf","蓝牙通讯的完整指令为： " + order);
		o2112 = ByteHexHelper.hexStringToBytes(order);
		return o2112;
	}

	/**
	 * 读接头配置文件
	 * 
	 * @return
	 */
	public byte[] readConFile2113() {
		byte[] o2113 = null;
		String counters = ByteHexHelper.RandomMethod();
		while (counters.equalsIgnoreCase(counter)) {
			counters = ByteHexHelper.RandomMethod();
		}
		counter = counters;
		commandWord = "2113";
		dataArea = "";
		packLengths = ByteHexHelper.packLength(counter + commandWord + dataArea);
		packVerify = ByteHexHelper.packVerify(target, source, packLengths, counter,
				commandWord, dataArea);
		String order = startCode + target + source + packLengths + counter
				+ commandWord + dataArea + packVerify;
		
		//Log.e("bcf","蓝牙通讯的完整指令为： " + order);
		o2113 = ByteHexHelper.hexStringToBytes(order);
		return o2113;
	}

	/**
	 * 读取当前状态
	 * 
	 * @return
	 */
	public byte[] currentStatus2114() {
		byte[] o2114 = null;
		String counters = ByteHexHelper.RandomMethod();
		while (counters.equalsIgnoreCase(counter)) {
			counters = ByteHexHelper.RandomMethod();
		}
		counter = counters;
		commandWord = "2114";
		dataArea = "";
		packLengths = ByteHexHelper.packLength(counter + commandWord + dataArea);
		packVerify = ByteHexHelper.packVerify(target, source, packLengths, counter,
				commandWord, dataArea);
		String order = startCode + target + source + packLengths + counter
				+ commandWord + dataArea + packVerify;
		
		//Log.e("bcf","蓝牙通讯的完整指令为： " + order);
		o2114 = ByteHexHelper.hexStringToBytes(order);
		return o2114;
	}

	/**
	 * 读DPU接头Licence
	 * 
	 * @return
	 */
	public byte[] DPULicence2115() {
		byte[] o2115 = null;
		String counters = ByteHexHelper.RandomMethod();
		while (counters.equalsIgnoreCase(counter)) {
			counters = ByteHexHelper.RandomMethod();
		}
		counter = counters;
		commandWord = "2115";
		dataArea = "";
		packLengths = ByteHexHelper.packLength(counter + commandWord + dataArea);
		packVerify = ByteHexHelper.packVerify(target, source, packLengths, counter,
				commandWord, dataArea);
		String order = startCode + target + source + packLengths + counter
				+ commandWord + dataArea + packVerify;
		
		//Log.e("bcf","蓝牙通讯的完整指令为： " + order);
		o2115 = ByteHexHelper.hexStringToBytes(order);
		return o2115;
	}

	/**
	 * 读DPU接头蓝牙地址
	 * 
	 * @return
	 */
	public byte[] DPUBluetoothAddress2116() {
		byte[] o2116 = null;
		String counters = ByteHexHelper.RandomMethod();
		while (counters.equalsIgnoreCase(counter)) {
			counters = ByteHexHelper.RandomMethod();
		}
		counter = counters;
		commandWord = "2116";
		dataArea = "";
		packLengths = ByteHexHelper.packLength(counter + commandWord + dataArea);
		packVerify = ByteHexHelper.packVerify(target, source, packLengths, counter,
				commandWord, dataArea);
		String order = startCode + target + source + packLengths + counter
				+ commandWord + dataArea + packVerify;
		
		//Log.e("bcf","蓝牙通讯的完整指令为： " + order);
		o2116 = ByteHexHelper.hexStringToBytes(order);
		return o2116;
	}

	/**
	 * 验证DPU接头
	 * 
	 * @param ucContext
	 * @param ucSignature
	 * @return
	 */
	public byte[] checkDPU2117(String ucContext, String ucSignature) {
		byte[] o2117 = null;
		String counters = ByteHexHelper.RandomMethod();
		while (counters.equalsIgnoreCase(counter)) {
			counters = ByteHexHelper.RandomMethod();
		}
		counter = counters;
		commandWord = "2117";
		dataArea = ucContext + ucSignature;
		packLengths = ByteHexHelper.packLength(counter + commandWord + dataArea);
		packVerify = ByteHexHelper.packVerify(target, source, packLengths, counter,
				commandWord, dataArea);
		String order = startCode + target + source + packLengths + counter
				+ commandWord + dataArea + packVerify;
		
		//Log.e("bcf","蓝牙通讯的完整指令为： " + order);
		o2117 = ByteHexHelper.hexStringToBytes(order);
		return o2117;
	}

	/**
	 * 写接头数据文件
	 * 
	 * @param fileName
	 *            文件名称
	 * @param fileSize
	 *            文件尺寸
	 * @param fileContent
	 *            文件内容
	 * @return
	 */
	public byte[] writeConnector2118(String fileName, Short fileSize,
			Buffer fileContent) {
		byte[] o2118 = null;
		String counters = ByteHexHelper.RandomMethod();
		while (counters.equalsIgnoreCase(counter)) {
			counters = ByteHexHelper.RandomMethod();
		}
		counter = counters;
		commandWord = "2118";
		dataArea = fileName + fileSize + fileContent.toString();
		packLengths = ByteHexHelper.packLength(counter + commandWord + dataArea);
		packVerify = ByteHexHelper.packVerify(target, source, packLengths, counter,
				commandWord, dataArea);
		String order = startCode + target + source + packLengths + counter
				+ commandWord + dataArea + packVerify;
		
		//Log.e("bcf","蓝牙通讯的完整指令为： " + order);
		o2118 = ByteHexHelper.hexStringToBytes(order);
		return o2118;
	}

	/**
	 * 读接头数据文件
	 * 
	 * @param fileName
	 *            文件名称
	 * @return
	 */
	public byte[] readConnector2119(String fileName) {
		byte[] o2119 = null;
		String counters = ByteHexHelper.RandomMethod();
		while (counters.equalsIgnoreCase(counter)) {
			counters = ByteHexHelper.RandomMethod();
		}
		counter = counters;
		commandWord = "2119";
		dataArea = fileName;
		packLengths = ByteHexHelper.packLength(counter + commandWord + dataArea);
		packVerify = ByteHexHelper.packVerify(target, source, packLengths, counter,
				commandWord, dataArea);
		String order = startCode + target + source + packLengths + counter
				+ commandWord + dataArea + packVerify;
		
		//Log.e("bcf","蓝牙通讯的完整指令为： " + order);
		o2119 = ByteHexHelper.hexStringToBytes(order);
		return o2119;
	}

	/**
	 * 退出简单诊断调度
	 * 
	 * @return
	 */
	public byte[] exitSimpleDiagnostic211a() {
		byte[] o211a = null;
		String counters = ByteHexHelper.RandomMethod();
		while (counters.equalsIgnoreCase(counter)) {
			counters = ByteHexHelper.RandomMethod();
		}
		counter = counters;
		commandWord = "211a";
		dataArea = "";
		packLengths = ByteHexHelper.packLength(counter + commandWord + dataArea);
		packVerify = ByteHexHelper.packVerify(target, source, packLengths, counter,
				commandWord, dataArea);
		String order = startCode + target + source + packLengths + counter
				+ commandWord + dataArea + packVerify;
		
		//Log.e("bcf","蓝牙通讯的完整指令为： " + order);
		o211a = ByteHexHelper.hexStringToBytes(order);
		return o211a;
	}

	/**
	 * 进入低功耗模式(仅针对蓝牙)
	 * 
	 * @return
	 */
	public byte[] intoLowPowerMode211b() {
		byte[] o211b = null;
		String counters = ByteHexHelper.RandomMethod();
		while (counters.equalsIgnoreCase(counter)) {
			counters = ByteHexHelper.RandomMethod();
		}
		counter = counters;
		commandWord = "211b";
		dataArea = "";
		packLengths = ByteHexHelper.packLength(counter + commandWord + dataArea);
		packVerify = ByteHexHelper.packVerify(target, source, packLengths, counter,
				commandWord, dataArea);
		String order = startCode + target + source + packLengths + counter
				+ commandWord + dataArea + packVerify;
		
		//Log.e("bcf","蓝牙通讯的完整指令为： " + order);
		o211b = ByteHexHelper.hexStringToBytes(order);
		return o211b;
	}

	/*********** 软件升级0x24 ************/

	/**
	 * 准备从端升级命令(车型文件) 主端发送至从端命令
	 * 
	 * @param buffer
	 * @return
	 */
	public byte[] upgrade2401(String buffer) {
		byte[] o2401 = null;
		String counters = ByteHexHelper.RandomMethod();
		while (counters.equalsIgnoreCase(counter)) {
			counters = ByteHexHelper.RandomMethod();
		}
		counter = counters;
		commandWord = "2401";
		dataArea = buffer;
		packLengths = ByteHexHelper.packLength(counter + commandWord + dataArea);
		packVerify = ByteHexHelper.packVerify(target, source, packLengths, counter,
				commandWord, dataArea);
		String order = startCode + target + source + packLengths + counter
				+ commandWord + dataArea + packVerify;
		
		//Log.e("bcf","蓝牙通讯的完整指令为： " + order);
		o2401 = ByteHexHelper.hexStringToBytes(order);
		return o2401;
	}

	/**
	 * 升级数据文件名发送命令 主端发送至从端命令
	 * 
	 * @param fileName
	 *            文件名称
	 * @param fileSize
	 *            文件长度
	 * @return
	 */
	public byte[] upgradeFileName2402(String fileName, Long fileSize) {
		byte[] o2402 = null;
		String counters = ByteHexHelper.RandomMethod();
		while (counters.equalsIgnoreCase(counter)) {
			counters = ByteHexHelper.RandomMethod();
		}
		counter = counters;
		commandWord = "2402";
		dataArea = fileName + fileSize;
		packLengths = ByteHexHelper.packLength(counter + commandWord + dataArea);
		packVerify = ByteHexHelper.packVerify(target, source, packLengths, counter,
				commandWord, dataArea);
		String order = startCode + target + source + packLengths + counter
				+ commandWord + dataArea + packVerify;
		
		//Log.e("bcf","蓝牙通讯的完整指令为： " + order);
		o2402 = ByteHexHelper.hexStringToBytes(order);
		return o2402;
	}

	/**
	 * 升级数据文件内容发送命令 主端发送至从端命令
	 * 
	 * @param writePosition
	 *            写入位置
	 * @param dataLength
	 *            数据长度
	 * @param fileContent
	 *            文件内容缓冲区
	 * @return
	 */
	public byte[] upgradeFileContent2403(Long writePosition,
			Short dataLength, Buffer fileContent) {
		byte[] o2403 = null;
		String counters = ByteHexHelper.RandomMethod();
		while (counters.equalsIgnoreCase(counter)) {
			counters = ByteHexHelper.RandomMethod();
		}
		counter = counters;
		commandWord = "2403";
		dataArea = writePosition + dataLength + fileContent.toString();
		packLengths = ByteHexHelper.packLength(counter + commandWord + dataArea);
		packVerify = ByteHexHelper.packVerify(target, source, packLengths, counter,
				commandWord, dataArea);
		String order = startCode + target + source + packLengths + counter
				+ commandWord + dataArea + packVerify;
		
		//Log.e("bcf","蓝牙通讯的完整指令为： " + order);
		o2403 = ByteHexHelper.hexStringToBytes(order);
		return o2403;
	}

	/**
	 * 升级数据文件内容校验数据发送命令 主端发送至从端命令
	 * 
	 * @param md5Str
	 *            md5校验字节 32字节
	 * @return
	 */
	public byte[] upgradeFileConVerify2404(String md5Str) {
		byte[] o2404 = null;
		String counters = ByteHexHelper.RandomMethod();
		while (counters.equalsIgnoreCase(counter)) {
			counters = ByteHexHelper.RandomMethod();
		}
		counter = counters;
		commandWord = "2404";
		dataArea = md5Str;
		packLengths = ByteHexHelper.packLength(counter + commandWord + dataArea);
		packVerify = ByteHexHelper.packVerify(target, source, packLengths, counter,
				commandWord, dataArea);
		String order = startCode + target + source + packLengths + counter
				+ commandWord + dataArea + packVerify;
		
		//Log.e("bcf","蓝牙通讯的完整指令为： " + order);
		o2404 = ByteHexHelper.hexStringToBytes(order);
		return o2404;
	}

	/**
	 * 完成升级命令 主端发送至从端命令
	 * 
	 * @return
	 */
	public byte[] upgradeComplete2405() {
		byte[] o2405 = null;
		String counters = ByteHexHelper.RandomMethod();
		while (counters.equalsIgnoreCase(counter)) {
			counters = ByteHexHelper.RandomMethod();
		}
		counter = counters;
		commandWord = "2405";
		dataArea = "";
		packLengths = ByteHexHelper.packLength(counter + commandWord + dataArea);
		packVerify = ByteHexHelper.packVerify(target, source, packLengths, counter,
				commandWord, dataArea);
		String order = startCode + target + source + packLengths + counter
				+ commandWord + dataArea + packVerify;
		
		//Log.e("bcf","蓝牙通讯的完整指令为： " + order);
		o2405 = ByteHexHelper.hexStringToBytes(order);
		return o2405;
	}

	/**
	 * 断点续传命令 主端发送至从端命令
	 * 
	 * @return
	 */
	public byte[] breakpointResume2406() {
		byte[] o2406 = null;
		String counters = ByteHexHelper.RandomMethod();
		while (counters.equalsIgnoreCase(counter)) {
			counters = ByteHexHelper.RandomMethod();
		}
		counter = counters;
		commandWord = "2406";
		dataArea = "";
		packLengths = ByteHexHelper.packLength(counter + commandWord + dataArea);
		packVerify = ByteHexHelper.packVerify(target, source, packLengths, counter,
				commandWord, dataArea);
		String order = startCode + target + source + packLengths + counter
				+ commandWord + dataArea + packVerify;
		
		//Log.e("bcf","蓝牙通讯的完整指令为： " + order);
		o2406 = ByteHexHelper.hexStringToBytes(order);
		return o2406;
	}

	/**
	 * 启动更新固件命令 主端发送至从端命令
	 * 
	 * @return
	 */
	public byte[] updateFirmware2407() {
		byte[] o2407 = null;
		String counters = ByteHexHelper.RandomMethod();
		while (counters.equalsIgnoreCase(counter)) {
			counters = ByteHexHelper.RandomMethod();
		}
		counter = counters;
		commandWord = "2407";
		dataArea = "";
		packLengths = ByteHexHelper.packLength(counter + commandWord + dataArea);
		packVerify = ByteHexHelper.packVerify(target, source, packLengths, counter,
				commandWord, dataArea);
		String order = startCode + target + source + packLengths + counter
				+ commandWord + dataArea + packVerify;
		
		//Log.e("bcf","蓝牙通讯的完整指令为： " + order);
		o2407 = ByteHexHelper.hexStringToBytes(order);
		return o2407;
	}

	/**
	 * 读取DPU接头车型文件信息 主端发送至从端命令 buffer 包括(文件数量，文件名1,文件1 md5,文件名2,文件2 md5,文件名3,文件3
	 * md5,文件名4,文件4 md5,文件名5,文件5 md5,)
	 * 
	 * @return
	 */
	public byte[] readModelFileInfo2408(String buffer) {
		byte[] o2408 = null;
		String counters = ByteHexHelper.RandomMethod();
		while (counters.equalsIgnoreCase(counter)) {
			counters = ByteHexHelper.RandomMethod();
		}
		counter = counters;
		commandWord = "2408";
		dataArea = buffer;
		packLengths = ByteHexHelper.packLength(counter + commandWord + dataArea);
		packVerify = ByteHexHelper.packVerify(target, source, packLengths, counter,
				commandWord, dataArea);
		String order = startCode + target + source + packLengths + counter
				+ commandWord + dataArea + packVerify;
		
		//Log.e("bcf","蓝牙通讯的完整指令为： " + order);
		o2408 = ByteHexHelper.hexStringToBytes(order);
		return o2408;
	}

	/********************** 建立连接，安全校验，断开连接及链路0x25 ********************/

	/**
	 * 链路保持
	 * 
	 * @return
	 */
	public byte[] link2501() {
		byte[] o2501 = null;
		String counters = ByteHexHelper.RandomMethod();
		while (counters.equalsIgnoreCase(counter)) {
			counters = ByteHexHelper.RandomMethod();
		}
		counter = counters;
		commandWord = "2501";
		dataArea = "";
		packLengths = ByteHexHelper.packLength(counter + commandWord + dataArea);
		packVerify = ByteHexHelper.packVerify(target, source, packLengths, counter,
				commandWord, dataArea);
		String order = startCode + target + source + packLengths + counter
				+ commandWord + dataArea + packVerify;
		
		//Log.e("bcf","蓝牙通讯的完整指令为： " + order);
		o2501 = ByteHexHelper.hexStringToBytes(order);
		return o2501;
	}

	/**
	 * 请求连接
	 * 
	 * @return
	 */
	public byte[] requestConnect2502() {
		byte[] o2502 = null ;
		String counters = ByteHexHelper.RandomMethod();
		while (counters.equalsIgnoreCase(counter)) {
			counters = ByteHexHelper.RandomMethod();
		}
		counter = counters;
		commandWord = "2502";
		dataArea = "02";
		packLengths = ByteHexHelper.packLength(counter + commandWord + dataArea);
		packVerify = ByteHexHelper.packVerify(target, source, packLengths, counter, commandWord, dataArea);
		String order = startCode + target + source + packLengths + counter + commandWord + dataArea + packVerify;
		
		//Log.e("bcf","蓝牙通讯的完整指令为： " + order);
		o2502 = ByteHexHelper.hexStringToBytes(order);
		return o2502 ;
	}

	/**
	 * 安全校验
	 * 
	 * @return
	 */
	public byte[] securityCheck2503(String verify) {
		byte[] o2503 = null;
		String counters = ByteHexHelper.RandomMethod();
		while (counters.equalsIgnoreCase(counter)) {
			counters = ByteHexHelper.RandomMethod();
		}
		counter = counters;
		commandWord = "2503";
		dataArea = verify;
		packLengths = ByteHexHelper.packLength(counter + commandWord + dataArea);
		packVerify = ByteHexHelper.packVerify(target, source, packLengths, counter,
				commandWord, dataArea);
		String order = startCode + target + source + packLengths + counter
				+ commandWord + dataArea + packVerify;
		
		//Log.e("bcf","蓝牙通讯的完整指令为： " + order);
		o2503 = ByteHexHelper.hexStringToBytes(order);
		return o2503;
	}

	/**
	 * 关闭连接
	 * 
	 * @return
	 */
	public byte[] disconnected2504() {
		byte[] o2504 = null;
		String counters = ByteHexHelper.RandomMethod();
		while (counters.equalsIgnoreCase(counter)) {
			counters = ByteHexHelper.RandomMethod();
		}
		counter = counters;
		commandWord = "2504";
		dataArea = "";
		packLengths = ByteHexHelper.packLength(counter + commandWord + dataArea);
		packVerify = ByteHexHelper.packVerify(target, source, packLengths, counter,
				commandWord, dataArea);
		String order = startCode + target + source + packLengths + counter
				+ commandWord + dataArea + packVerify;
		
		//Log.e("bcf","蓝牙通讯的完整指令为： " + order);
		o2504 = ByteHexHelper.hexStringToBytes(order);
		return o2504;
	}

	/**
	 * 复位接头
	 * 
	 * @return
	 */
	public byte[] resetConnector2505() {
		byte[] o2505 = null;
		String counters = ByteHexHelper.RandomMethod();
		while (counters.equalsIgnoreCase(counter)) {
			counters = ByteHexHelper.RandomMethod();
		}
		counter = counters;
		commandWord = "2505";
		dataArea = "";
		packLengths = ByteHexHelper.packLength(counter + commandWord + dataArea);
		packVerify = ByteHexHelper.packVerify(target, source, packLengths, counter,
				commandWord, dataArea);
		String order = startCode + target + source + packLengths + counter
				+ commandWord + dataArea + packVerify;

//		Log.e("wzx","resetConnector2505 蓝牙通讯的完整指令为： " + order);
		o2505 = ByteHexHelper.hexStringToBytes(order);
		return o2505;
	}
	/**
	 *读当前接头是否已经烧录
	 *
	 * @return
	 */
	public byte[] resetConnector2107(){
		byte[] o2107 = null;
		String counters = ByteHexHelper.RandomMethod();
		while (counters.equalsIgnoreCase(counter)) {
			counters = ByteHexHelper.RandomMethod();
		}
		counter = counters;
		commandWord = "2107060003313000";
		dataArea = "";
		packLengths = ByteHexHelper.packLength(counter + commandWord + dataArea);
		packVerify = ByteHexHelper.packVerify(target, source, packLengths, counter,
				commandWord, dataArea);
		String order = startCode + target + source + packLengths + counter
				+ commandWord + dataArea + packVerify;
		//if(D)
		//System.out.println("蓝牙通讯的完整指令为： " + order);
		o2107 = ByteHexHelper.hexStringToBytes(order);
		return o2107;
	}
	/**
	 * 进入一键诊断系统列表
	 * 
	 * @return
	 */
	public byte[] diagnosticList2500() {
		byte[] o2500 = null;
		String counters = ByteHexHelper.RandomMethod();
		while (counters.equalsIgnoreCase(counter)) {
			counters = ByteHexHelper.RandomMethod();
		}
		counter = counters;
		commandWord = "2500";
		dataArea = "";
		packLengths = ByteHexHelper.packLength(counter + commandWord + dataArea);
		packVerify = ByteHexHelper.packVerify(target, source, packLengths, counter,
				commandWord, dataArea);
		String order = startCode + target + source + packLengths + counter
				+ commandWord + dataArea + packVerify;
		
		//Log.e("bcf","蓝牙通讯的完整指令为： " + order);
		o2500 = ByteHexHelper.hexStringToBytes(order);
		return o2500;
	}
	
	/**
	 * 直接发送
	 * @param param 完整命令
	 * @return
	 */
	
	public byte[] smartBox2701No(byte[] param) {
		byte[] o2701 = null;
		String counters = ByteHexHelper.RandomMethod();
		while (counters.equalsIgnoreCase(counter)) {
			counters = ByteHexHelper.RandomMethod();
		}
		counter = counters;
		commandWord = "";
		dataArea = ByteHexHelper.bytesToHexString(param);
		packLengths = ByteHexHelper.packLength(counter + commandWord + dataArea);
		packVerify = ByteHexHelper.packVerify(target, source, packLengths, counter,
				commandWord, dataArea);
		String order = startCode + target + source + packLengths + counter
				+ commandWord + dataArea + packVerify;
		o2701 = ByteHexHelper.hexStringToBytes(order);
		
		//Log.e("bcf","蓝牙通讯的完整指令为： " + order);
		return o2701;
	}
	/**
	 * 发送文件名称和文件长度
	 * @param file
	 * @return
	*/
	public byte[] sendFileNameAndLength2402(File file)
	{
		//文件名跟文件长度
		byte[]param=DpuOrderUtils.fileNameAndLength(file.getName(), file);
		byte[] o2402 = null;
		String counters = ByteHexHelper.RandomMethod();
		while (counters.equalsIgnoreCase(counter)) {
			counters = ByteHexHelper.RandomMethod();
		}
		counter = counters;
		commandWord = "2402";
		dataArea = ByteHexHelper.bytesToHexString(param);
		packLengths = ByteHexHelper.packLength(counter + commandWord + dataArea);
		packVerify = ByteHexHelper.packVerify(target, source, packLengths, counter,
				commandWord, dataArea);
		String order = startCode + target + source + packLengths + counter
				+ commandWord + dataArea + packVerify;
		o2402 = ByteHexHelper.hexStringToBytes(order);
		
		//Log.e("bcf","蓝牙通讯的完整指令为： " + order);
		return o2402;
	}
	/**
	 * 发送文件名称和文件长度
	 * @param file
	 * @return
	 */
	public byte[] sendFileNameAndLength2412(File file)
	{
		//文件名跟文件长度
		byte[]param=DpuOrderUtils.fileNameAndLength(file.getName(), file);
		byte[] o2412 = null;
		String counters = ByteHexHelper.RandomMethod();
		while (counters.equalsIgnoreCase(counter)) {
			counters = ByteHexHelper.RandomMethod();
		}
		counter = counters;
		commandWord = "2412";
		dataArea = ByteHexHelper.bytesToHexString(param);
		packLengths = ByteHexHelper.packLength(counter + commandWord + dataArea);
		packVerify = ByteHexHelper.packVerify(target, source, packLengths, counter,
				commandWord, dataArea);
		String order = startCode + target + source + packLengths + counter
				+ commandWord + dataArea + packVerify;
		o2412 = ByteHexHelper.hexStringToBytes(order);

//		Log.e("wzx","蓝牙通讯的完整指令为： " + order);
		return o2412;
	}
	/**
	 * 发送升级文件MD5校验
	 * @param md5
	 * @return
	 */
	public byte[] sendUpdateFileMd52404(String md5)
	{
	//文件名跟文件长度
	byte[] o2404 = null;
	String counters = ByteHexHelper.RandomMethod();
	while (counters.equalsIgnoreCase(counter)) {
		counters = ByteHexHelper.RandomMethod();
	}
	counter = counters;
	commandWord = "2404";
	dataArea = ByteHexHelper.bytesToHexString(md5.getBytes());
	packLengths = ByteHexHelper.packLength(counter + commandWord + dataArea);
	packVerify = ByteHexHelper.packVerify(target, source, packLengths, counter,
			commandWord, dataArea);
	String order = startCode + target + source + packLengths + counter
			+ commandWord + dataArea + packVerify;
	o2404 = ByteHexHelper.hexStringToBytes(order);
	
	//Log.e("bcf","蓝牙通讯的完整指令为： " + order);
	return o2404;
	}
	/**
	 * 发送升级文件MD5校验
	 * @param md5
	 * @return
	 */
	public byte[] sendUpdateFileMd52414(String md5)
	{
		//文件名跟文件长度
		byte[] o2414 = null;
		String counters = ByteHexHelper.RandomMethod();
		while (counters.equalsIgnoreCase(counter)) {
			counters = ByteHexHelper.RandomMethod();
		}
		counter = counters;
		commandWord = "2414";
		dataArea = ByteHexHelper.bytesToHexString(md5.getBytes());
		packLengths = ByteHexHelper.packLength(counter + commandWord + dataArea);
		packVerify = ByteHexHelper.packVerify(target, source, packLengths, counter,
				commandWord, dataArea);
		String order = startCode + target + source + packLengths + counter
				+ commandWord + dataArea + packVerify;
		o2414 = ByteHexHelper.hexStringToBytes(order);

//		Log.e("wzx","蓝牙通讯的完整指令为： " + order);
		return o2414;
	}
	/**
	 * 完成升级确认
	 */
	public byte[] ValidateUpdateFinished2405()
	{
	//文件名跟文件长度
	byte[] o2405 = null;
	String counters = ByteHexHelper.RandomMethod();
	while (counters.equalsIgnoreCase(counter)) {
		counters = ByteHexHelper.RandomMethod();
	}
	counter = counters;
	commandWord = "2405";
	dataArea = "";
	packLengths = ByteHexHelper.packLength(counter + commandWord + dataArea);
	packVerify = ByteHexHelper.packVerify(target, source, packLengths, counter,
			commandWord, dataArea);
	String order = startCode + target + source + packLengths + counter
			+ commandWord + dataArea + packVerify;
	o2405 = ByteHexHelper.hexStringToBytes(order);
	
	//Log.e("bcf","蓝牙通讯的完整指令为： " + order);
	return o2405;
	}
	/**
	 * 验证所有文件的MD5信息
	 * @return
	 */
	public byte[] ValidateAllFilesMd52408()
	{
	//文件名跟文件长度
	byte[] o2408 = null;
	String counters = ByteHexHelper.RandomMethod();
	while (counters.equalsIgnoreCase(counter)) {
		counters = ByteHexHelper.RandomMethod();
	}
	counter = counters;
	commandWord = "2403";
	dataArea = "";
	packLengths = ByteHexHelper.packLength(counter + commandWord + dataArea);
	packVerify = ByteHexHelper.packVerify(target, source, packLengths, counter,
			commandWord, dataArea);
	String order = startCode + target + source + packLengths + counter
			+ commandWord + dataArea + packVerify;
	o2408 = ByteHexHelper.hexStringToBytes(order);
	
	//Log.e("bcf","蓝牙通讯的完整指令为： " + order);
	return o2408;
	}
	/**
	 * 5.4.3	升级数据文件内容发送命令
	 * @param  param
	 * @return
	 */
	public byte[] sendUpdateFilesContent2403(byte[] param)
	{
	//文件名跟文件长度
	byte[] o2403 = null;
	String counters = ByteHexHelper.RandomMethod();
	while (counters.equalsIgnoreCase(counter)) {
		counters = ByteHexHelper.RandomMethod();
	}
	counter = counters;
	commandWord = "2403";
	dataArea = ByteHexHelper.bytesToHexString(param);
	packLengths = ByteHexHelper.packLength(counter + commandWord + dataArea);
	packVerify = ByteHexHelper.packVerify(target, source, packLengths, counter,
			commandWord, dataArea);
	String order = startCode + target + source + packLengths + counter
			+ commandWord + dataArea + packVerify;
	o2403 = ByteHexHelper.hexStringToBytes(order);
	
	//Log.e("bcf","蓝牙通讯的完整指令为： " + order);
	return o2403;
	}
	/**
	 * 5.4.3	升级数据文件内容发送命令
	 * @param param
	 * @return
	 */
	public byte[] sendUpdateFilesContent2413(byte[] param)
	{
		//文件名跟文件长度
		byte[] o2413 = null;
		String counters = ByteHexHelper.RandomMethod();
		while (counters.equalsIgnoreCase(counter)) {
			counters = ByteHexHelper.RandomMethod();
		}
		counter = counters;
		commandWord = "2413";
		dataArea = ByteHexHelper.bytesToHexString(param);
		packLengths = ByteHexHelper.packLength(counter + commandWord + dataArea);
		packVerify = ByteHexHelper.packVerify(target, source, packLengths, counter,
				commandWord, dataArea);
		String order = startCode + target + source + packLengths + counter
				+ commandWord + dataArea + packVerify;
		o2413 = ByteHexHelper.hexStringToBytes(order);

//		Log.e("wzx","蓝牙通讯的完整指令为： " + order);
		return o2413;
	}
	/**
	 * 5.4.3	升级数据文件内容发送命令
	 * @param 
	 * @return
	 */
	public byte[] sendUpdateFilesContent2403(String addressbytes , String sendbuff)
	{
		byte[] o2403 = null;
		 String startCode = "55AA";	
		 String addressArea = addressbytes;//"00010400";
		 String packLengths = "00000400";
		//需要校验的数据为："040EFBF161"+addressArea+length+dataArea
		 String verifyStartData = "040EFBF161"+addressArea+packLengths;	 
		 dataArea = sendbuff;
		 
		packVerify = ByteHexHelper.packVerifyforjili(verifyStartData, dataArea);

		String order = startCode + verifyStartData + dataArea + packVerify;
		o2403 = ByteHexHelper.hexStringToBytes(order);
		return o2403;
	}
	/********************* SmartBox模式 0x27 ******************************/
	
	/**
	 * 直接发送
	 * @param param 完整命令
	 * @return
	 */
	
	public byte[] smartBox2701(byte[] param) {
		byte[] o2701 = null;
		String counters = ByteHexHelper.RandomMethod();
		while (counters.equalsIgnoreCase(counter)) {
			counters = ByteHexHelper.RandomMethod();
		}
		counter = counters;
		commandWord = "2701";
		dataArea = ByteHexHelper.bytesToHexString(param);
		packLengths = ByteHexHelper.packLength(counter + commandWord + dataArea);
		packVerify = ByteHexHelper.packVerify(target, source, packLengths, counter,
				commandWord, dataArea);
		String order = startCode + target + source + packLengths + counter
				+ commandWord + dataArea + packVerify;
		o2701 = ByteHexHelper.hexStringToBytes(order);
		
		//Log.e("bcf","蓝牙通讯的完整指令为： " + order);
		return o2701;
	}

	/**
	 * 发送UPSTE!指令
	 * 
	 * @return
	 */
	public byte[] sendUpdate2505() {
		byte[] o2505 = null;		
		String dataArea2105="55504441544521";
		String order = dataArea2105;

		o2505 = ByteHexHelper.hexStringToBytes(order);
		return o2505;
	}

	/**
	 * 设置波特率指令
	 * 
	 * @return
	 */
	public byte[] setBautrate2505() {
		byte[] o2505 = null;
		
		String dataArea2105="55AA000EFFF1630001C20000000000A0";

		String order = dataArea2105;
		o2505 = ByteHexHelper.hexStringToBytes(order);
		return o2505;
	}
	
	/**
	 * 设置地址和大小
	 * 
	 * @return
	 */
	public byte[] setAddressAndSize2505() {
		byte[] o2505 = null;
		
		String dataArea2105="55AA000EFFF1620001000000068000E5";

		String order = dataArea2105;
		o2505 = ByteHexHelper.hexStringToBytes(order);
		return o2505;
	}

	/**
	 *  读取车辆电压信息
	 * @author weizhongxuan
	 * @time 2016/10/13 14:22
	 */
	public byte[] vehicleVoltageInfo2120() {
		byte[] o2120 = null;
		String counters = ByteHexHelper.RandomMethod();
		while (counters.equalsIgnoreCase(counter)) {
			counters = ByteHexHelper.RandomMethod();
		}
		counter = counters;
		commandWord = "2120";
		dataArea = "";
		packLengths = ByteHexHelper.packLength(counter + commandWord + dataArea);
		packVerify = ByteHexHelper.packVerify(target, source, packLengths, counter,
				commandWord, dataArea);
		String order = startCode + target + source + packLengths + counter
				+ commandWord + dataArea + packVerify;

		o2120 = ByteHexHelper.hexStringToBytes(order);
		return o2120;
	}
	
	/**
	 * 生成通用下位机指令
	 * 
	 * @author xiefeihong
	 * @time 2016/10/26 14:19	
	 * @param command //命令字
	 * @param data //数据
	 * @return
	 */
	public byte[] generateCommonCommand(String command,String data ) {
		byte[] commonCommand = null;
		String counters = ByteHexHelper.RandomMethod();
		while (counters.equalsIgnoreCase(counter)) {
			counters = ByteHexHelper.RandomMethod();
		}
		counter = counters;
		commandWord = command;
		dataArea = data;
		packLengths = ByteHexHelper.packLength(counter + commandWord + dataArea);
		packVerify = ByteHexHelper.packVerify(target, source, packLengths, counter,
				commandWord, dataArea);
		String order = startCode + target + source + packLengths + counter
				+ commandWord + dataArea + packVerify;

		commonCommand = ByteHexHelper.hexStringToBytes(order);
		return commonCommand;
	}
	public byte[] generateCommonCommand(byte[] command,byte[] data ) {
		return generateCommonCommand(ByteHexHelper.bytesToHexStringWithSearchTable(command),
																ByteHexHelper.bytesToHexStringWithSearchTable(data));
	}
	/**
	 * ykw
	 */
	public  byte[] dpuDateSoftcodeRegister2131(int datNum,String datDate) {
		byte[] o2131 = null;
		String counters = ByteHexHelper.RandomMethod();
		while (counters.equalsIgnoreCase(counter)) {
			counters = ByteHexHelper.RandomMethod();
		}
		counter = counters;
		commandWord = "2131";
		byte datNumByte = (byte)(datNum & 0xFF);
		String other = ByteHexHelper.byteToHexString(datNumByte)+datDate;
		dataArea =getCurrentData2131()+other;
		packLengths = ByteHexHelper.packLength(counter + commandWord + dataArea);
		packVerify = ByteHexHelper.packVerify(target, source, packLengths, counter, commandWord, dataArea);
		String order = startCode + target + source + packLengths + counter + commandWord + dataArea + packVerify;
		if (MLog.isDebug) {
			MLog.d("ykw", "指令拼接2131 counter:" + counters + ",dataArea:" + dataArea + ",packlengths:" + packLengths + ",packverify:" + packVerify);
		}
//		order = "55aaf0f8004620213111050b02173938323639303030303838312B42574D2B5631312E343905312C342C35183938323639303030303838312B415544492B5631322E343907332C31342C31352E";
		o2131 = ByteHexHelper.hexStringToBytes(order);
		if (MLog.isDebug) {
			MLog.d("ykw", "蓝牙通讯的完整指令为2131:" + order);
		}
		return o2131;
	}

	/**
	 * ykw
	 */
	public  byte[] softVer2134(String softPackageId, String versionNo) {
		byte[] o2134 = null;
		String counters = ByteHexHelper.RandomMethod();
		while (counters.equalsIgnoreCase(counter)) {
			counters = ByteHexHelper.RandomMethod();
		}
		counter = counters;
		commandWord = "2134";
		//命令字 软件包id+版本号
		dataArea = stringToHexString(softPackageId + versionNo.replace("V", "+"));//"BWM+11.49";
		packLengths = ByteHexHelper.packLength(counter + commandWord + dataArea);
		packVerify = ByteHexHelper.packVerify(target, source, packLengths, counter, commandWord, dataArea);
		String order = startCode + target + source + packLengths + counter + commandWord + dataArea + packVerify;
		if (MLog.isDebug) {
			MLog.d("ykw", "指令拼接2134 counter:" + counters + ",dataArea:" + dataArea + ",packlengths:" + packLengths + ",packverify:" + packVerify);
		}
//		order = "55aaf0f8000d14213442574D2B5631312E343902";
		o2134 = ByteHexHelper.hexStringToBytes(order);
		if (MLog.isDebug) {
			MLog.d("ykw", "蓝牙通讯的完整指令为2134:" + order);
		}
		return o2134;
	}

	/**
	 * 写激活码
	 * ykw
	 */
	public  byte[] activation2133(String effectDate, String sysCode) {
		byte[] o2133 = null;
		String counters = ByteHexHelper.RandomMethod();
		while (counters.equalsIgnoreCase(counter)) {
			counters = ByteHexHelper.RandomMethod();
		}
		counter = counters;
		commandWord = "2133";
		dataArea = getCurrentData2133(effectDate)+stringToHexString(sysCode);
		packLengths = ByteHexHelper.packLength(counter + commandWord + dataArea);
		packVerify = ByteHexHelper.packVerify(target, source, packLengths, counter, commandWord, dataArea);
		String order = startCode + target + source + packLengths + counter + commandWord + dataArea + packVerify;
		if(MLog.isDebug) {
			MLog.d("ykw", "指令拼接2133 counter:" + counters + ",dataArea:" + dataArea + ",packlengths:" + packLengths + ",packverify:" + packVerify);
		}
//		order = "55aaF0F8000e5521331105124a45cfba5bc3f4e6b7";
		o2133 = ByteHexHelper.hexStringToBytes(order);
		if(MLog.isDebug) {
			MLog.d("ykw", "蓝牙通讯的完整指令为2133:" + order);
		}
		return o2133;
	}

	public static String stringToHexString(String str) {
		String buffer = "";
		if (str != null && str.length() > 0) {
			byte[] src = str.getBytes();
			String result = ByteHexHelper.bytesToHexString(src);
			buffer = result;
		}
		return buffer;
	}
	/**
	 * 时间+激活码
	 */
	private static String getCurrentData2133(String effectDate) {
		StringBuffer stringBuffer = new StringBuffer();
		String[] dates = effectDate.split("\\-");
		if (dates != null && dates.length > 2) {
			int year = Integer.parseInt(dates[0].substring(2, dates[0].length()));
			int month = Integer.parseInt(dates[1]);
			int day = Integer.parseInt(dates[2]);
			byte yearByte = (byte)(year & 0xFF);
			byte monthByte = (byte)(month & 0xFF);
			byte dayByte = (byte)(day & 0xFF);
			if(MLog.isDebug) {
				MLog.d("ykw", "GetCurrentData2133--year=" + year + ", month=" + month + ", day=" + day);
			}
			stringBuffer.append(ByteHexHelper.byteToHexString(yearByte))
					.append(ByteHexHelper.byteToHexString(monthByte))
					.append(ByteHexHelper.byteToHexString(dayByte));
			if(MLog.isDebug) {
				MLog.d("ykw", "GetCurrentData2133--年月日:" + stringBuffer.toString());
			}
		}
		return stringBuffer.toString();
	}

	/**
	 * 时间+软件码个数+软件码(软件码长度+具体软件码)
	 */
	private static String getCurrentData2131() {
		StringBuffer stringBuffer = new StringBuffer();
		DecimalFormat decimalFormat = new DecimalFormat("00");
		Calendar calendar = Calendar.getInstance();
		String yearString = decimalFormat.format(calendar.get(Calendar.YEAR));
		int year = Integer.parseInt(yearString.substring(2, yearString.length()));
		int month = calendar.get(Calendar.MONTH) + 1;
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		byte yearByte = (byte)(year & 0xFF);
		byte monthByte = (byte)(month & 0xFF);
		byte dayByte = (byte)(day & 0xFF);
		if(MLog.isDebug) {
			MLog.d("ykw", "GetCurrentData2131--yearString=" + yearString + ", year=" + year + ", month=" + month + ", day=" + day);
		}
		stringBuffer.append(ByteHexHelper.byteToHexString(yearByte))
				.append(ByteHexHelper.byteToHexString(monthByte))
				.append(ByteHexHelper.byteToHexString(dayByte));
		if(MLog.isDebug) {
			LogUtil.INSTANCE.d("ykw", "年月日:" + stringBuffer.toString());
		}
		return stringBuffer.toString();
	}

	/**
	 *生成通用Smartbox30 linux系统交互指令
	 * @param command
	 * @param data
	 * @return
	 */
	public  byte[] generateSmartbox30LinuxCommonCommand(String command,String data ) {
		byte[] commonCommand ;
		String counters = ByteHexHelper.RandomMethod();
		while (counters.equalsIgnoreCase(counter)) {
			counters = ByteHexHelper.RandomMethod();
		}
		counter = counters;
		commandWord = command;
		dataArea = data;
		packLengths = ByteHexHelper.packLength(counter + commandWord + dataArea);
		packVerify = ByteHexHelper.packVerify(smartbox30_linux_target, source, packLengths, counter, commandWord, dataArea);
		String order = startCode + smartbox30_linux_target + source + packLengths + counter + commandWord + dataArea + packVerify;
		commonCommand = ByteHexHelper.hexStringToBytes(order);
		return commonCommand;
	}

	/**
	 * 生成通用Smartbox30 linux系统交互指令
	 * @param command
	 * @param data
	 * @return
	 */
	public  byte[] generateSmartbox30LinuxCommonCommand(byte[] command,byte[] data ) {
		return generateSmartbox30LinuxCommonCommand(ByteHexHelper.bytesToHexStringWithSearchTable(command),
				ByteHexHelper.bytesToHexStringWithSearchTable(data));
	}
	@Override
	public byte[] readRandom212101() {
		byte[] o212101 = null ;
		String counters = ByteHexHelper.RandomMethod();
		while (counters.equalsIgnoreCase(counter)) {
			counters = ByteHexHelper.RandomMethod();
		}
		counter = counters;
		commandWord = "212101";
		dataArea = "";
		packLengths = ByteHexHelper.packLength(counter + commandWord + dataArea);
		packVerify = ByteHexHelper.packVerify(target, source, packLengths, counter, commandWord, dataArea);
		String order = startCode + target + source + packLengths + counter + commandWord + dataArea + packVerify;

		//Log.e("bcf","蓝牙通讯的完整指令为： " + order);
		o212101 = ByteHexHelper.hexStringToBytes(order);
		return o212101 ;
	}
	@Override
	public byte[] sendEncResult212102(String encResult) {
		byte[] o212101 = null ;
		String counters = ByteHexHelper.RandomMethod();
		while (counters.equalsIgnoreCase(counter)) {
			counters = ByteHexHelper.RandomMethod();
		}
		counter = counters;
		commandWord = "212102";
		dataArea = encResult;
		packLengths = ByteHexHelper.packLength(counter + commandWord + dataArea);
		packVerify = ByteHexHelper.packVerify(target, source, packLengths, counter, commandWord, dataArea);
		String order = startCode + target + source + packLengths + counter + commandWord + dataArea + packVerify;

		//Log.e("bcf","蓝牙通讯的完整指令为： " + order);
		o212101 = ByteHexHelper.hexStringToBytes(order);
		return o212101 ;
	}

    /**
     * 胎压枪发送单位指令
     * 55aaf0f8001c042701612b01（激活参数项）02（单位类型）
     * @return
     */

    public byte[] TpmsgunUnit2701612b (String type,String unit) {
        byte[] o2701612b = null;
        String counters = ByteHexHelper.RandomMethod();
        while (counters.equalsIgnoreCase(counter)) {
            counters = ByteHexHelper.RandomMethod();
        }
        counter = counters;
        commandWord = "2701612b";
        dataArea = type+unit;
        packLengths = ByteHexHelper.packLength(counter + commandWord + dataArea);
        packVerify = ByteHexHelper.packVerify(target, source, packLengths, counter,
                commandWord, dataArea);
        String order = startCode + target + source + packLengths + counter
                + commandWord + dataArea + packVerify;
        o2701612b = ByteHexHelper.hexStringToBytes(order);

//        Log.e("ykw","胎压枪单位切换2701612b蓝牙通讯的完整指令为： " + order);
        return o2701612b;
    }

    /**
     * 胎压枪发送激活相关信息指令
     * 55aaf0f8001c042701612c(buf)
     * @return
     */

    public byte[] TpmsgunSoftInfo2701612c (String data) {
        byte[] o2701612c = null;
        String counters = ByteHexHelper.RandomMethod();
        while (counters.equalsIgnoreCase(counter)) {
            counters = ByteHexHelper.RandomMethod();
        }
        counter = counters;
        commandWord = "2701612c";
        dataArea = data;
        packLengths = ByteHexHelper.packLength(counter + commandWord + dataArea);
        packVerify = ByteHexHelper.packVerify(target, source, packLengths, counter,
                commandWord, dataArea);
        String order = startCode + target + source + packLengths + counter
                + commandWord + dataArea + packVerify;
        o2701612c = ByteHexHelper.hexStringToBytes(order);

//        Log.e("ykw","胎压枪激活信息2701612c蓝牙通讯的完整指令为： " + order);
        return o2701612c;
    }

    /**
     * 胎压枪发送轮胎位置指令
     * 55 aa F0 F8 00 08 4f 27 01 61 2d 00 02 01 03 42
     * @return
     */

    public byte[] TpmsgunChangeTires2701612d (String data) {
        byte[] o2701612d = null;
        String counters = ByteHexHelper.RandomMethod();
        while (counters.equalsIgnoreCase(counter)) {
            counters = ByteHexHelper.RandomMethod();
        }
        counter = counters;
        commandWord = "2701612d";
        dataArea = data;
        packLengths = ByteHexHelper.packLength(counter + commandWord + dataArea);
        packVerify = ByteHexHelper.packVerify(target, source, packLengths, counter,
                commandWord, dataArea);
        String order = startCode + target + source + packLengths + counter
                + commandWord + dataArea + packVerify;
        o2701612d = ByteHexHelper.hexStringToBytes(order);

//        Log.e("ykw","胎压枪切换轮胎位置2701612d蓝牙通讯的完整指令为： " + order);
        return o2701612d;
    }
}
