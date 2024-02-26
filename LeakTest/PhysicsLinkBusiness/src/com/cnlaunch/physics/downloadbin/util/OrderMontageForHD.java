package com.cnlaunch.physics.downloadbin.util;
import com.cnlaunch.physics.utils.ByteHexHelper;
import com.cnlaunch.physics.utils.MLog;

import java.io.File;
import java.nio.Buffer;
/**
 * 蓝牙byte[]数据拼接
 * @author Administrator
 *
 */
public class OrderMontageForHD extends OrderMontage{
	public static final  String  TAG = "OrderMontageForHD";
	/** 起始标志 */
	private final static String startCode = "55aa";
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
	private static OrderMontageForHD INSTANCE;
	
	private OrderMontageForHD() {
		
	}

	public static OrderMontageForHD getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new OrderMontageForHD();
		}
		return INSTANCE;
	}
	private String generateCommonCommandOrder (int len,String currentDataArea){
		// 低位
		int len_l = (int) (len & 255);
		// 高位
		int len_h = (int) ((len >> 8) & 255);
		// 低位取反
		int _len_l = ~len_l & 0xff;
		// 高位取反
		int _len_h = ~len_h & 0xff;

		packLengths = matchLen(Integer.toHexString(len_h))
				+ matchLen(Integer.toHexString(len_l))
				+ matchLen(Integer.toHexString(_len_h))
				+ matchLen(Integer.toHexString(_len_l));

		packVerify = ByteHexHelper.XOR(packLengths + currentDataArea);

		String order = startCode + packLengths + currentDataArea + packVerify;
		if(MLog.isDebug) {
			MLog.d(TAG, "generateCommonCommandOrder： " + order);
		}
		return order;
	}
	/**
	 * 读取时钟
	 * 
	 * @return
	 */
	public byte[] readClock2102() {
		byte[] o2102 = null ;
		int len =  dataArea.getBytes().length;//
		String order = generateCommonCommandOrder(len,dataArea);
		o2102 = ByteHexHelper.hexStringToBytes(order);
		return o2102 ;
	}

	/**
	 * 设置时钟
	 * 
	 * @return
	 */
	public byte[] setClock2101() {
		byte[] o2101 = null;
		int len =  dataArea.getBytes().length;//
		String order = generateCommonCommandOrder(len,dataArea);
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
		String dataArea2103="55AA0007FFF8AC76DA";		
		String order = dataArea2103;
		if(MLog.isDebug) {
			MLog.d(TAG, "DPUVerInfo2103： " + order);
		}
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
		int len = 4+ (dataArea.getBytes().length/2)+1;
		String order = generateCommonCommandOrder(len,dataArea);
		if(MLog.isDebug) {
			MLog.d(TAG, "DPUKuVer2104： " + order);
		}
		o2104 = ByteHexHelper.hexStringToBytes(order);
		return o2104;
	}

	/**
	 * 读取DPU接头库文件版本(车型名称，车型版本，语言)
	 * 
	 * @return
	 */
	public byte[] sendcommad(String dataArea) {
		byte[] o2104 = null;
		int len = 4+ ((dataArea.getBytes().length+1)/2)+1;//
		String order = generateCommonCommandOrder(len,dataArea);
		if(MLog.isDebug) {
			MLog.d(TAG, "sendcommad： " + order);
		}
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
		String dataArea2105="55AA0006FFF96F6F";		
		String order = dataArea2105;
		if(MLog.isDebug) {
			MLog.d(TAG, "DPUVer2105： " + order);
		}
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
		int len = dataArea.getBytes().length;//
		String order = generateCommonCommandOrder(len,dataArea);
		if(MLog.isDebug) {
			MLog.d(TAG, "DPU2106： " + order);
		}
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
		int len = dataArea.getBytes().length;//
		String order = generateCommonCommandOrder(len,dataArea);
		if(MLog.isDebug) {
			MLog.d(TAG, "writeDPUSerialNum2107： " + order);
		}
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
		int len = dataArea.getBytes().length;//
		String order = generateCommonCommandOrder(len,dataArea);
		if(MLog.isDebug) {
			MLog.d(TAG, "setBluetoothName2108： " + order);
		}
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
		int len =  dataArea.getBytes().length;//
		String order = generateCommonCommandOrder(len,dataArea);
		if(MLog.isDebug) {
			MLog.d(TAG, "transferDPUMode2109： " + order);
		}
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
		int len = dataArea.getBytes().length;//
		String order = generateCommonCommandOrder(len,dataArea);
		if(MLog.isDebug) {
			MLog.d(TAG, "clearFlash210A： " + order);
		}
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
		int len =  dataArea.getBytes().length;//
		String order = generateCommonCommandOrder(len,dataArea);
		if(MLog.isDebug) {
			MLog.d(TAG, "modifyPw210B： " + order);
		}
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
		int len = dataArea.getBytes().length;//
		String order = generateCommonCommandOrder(len,dataArea);
		if(MLog.isDebug) {
			MLog.d(TAG, "resumePw210f： " + order);
		}
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
		int len =  dataArea.getBytes().length;//
		String order = generateCommonCommandOrder(len,dataArea);
		if(MLog.isDebug) {
			MLog.d(TAG, "resumePw2110： " + order);
		}
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
		int len =  dataArea.getBytes().length;//
		String order = generateCommonCommandOrder(len,dataArea);
		if(MLog.isDebug) {
			MLog.d(TAG, "download2111： " + order);
		}
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
		int len =  dataArea.getBytes().length;//
		String order = generateCommonCommandOrder(len,dataArea);
		if(MLog.isDebug) {
			MLog.d(TAG, "writeConFile2112： " + order);
		}
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
		int len =  dataArea.getBytes().length;//
		String order = generateCommonCommandOrder(len,dataArea);
		if(MLog.isDebug) {
			MLog.d(TAG, "readConFile2113： " + order);
		}
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
		int len =  dataArea.getBytes().length;//
		String order = generateCommonCommandOrder(len,dataArea);
		if(MLog.isDebug) {
			MLog.d(TAG, "currentStatus2114： " + order);
		}
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
		int len =  dataArea.getBytes().length;//
		String order = generateCommonCommandOrder(len,dataArea);
		if(MLog.isDebug) {
			MLog.d(TAG, "DPULicence2115： " + order);
		}
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
		int len =  dataArea.getBytes().length;//
		String order = generateCommonCommandOrder(len,dataArea);
		if(MLog.isDebug) {
			MLog.d(TAG, "DPUBluetoothAddress2116： " + order);
		}
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
		int len =  dataArea.getBytes().length;//
		String order = generateCommonCommandOrder(len,dataArea);
		if(MLog.isDebug) {
			MLog.d(TAG, "checkDPU2117： " + order);
		}
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
	public byte[] writeConnector2118(String fileName, Short fileSize, Buffer fileContent) {
		byte[] o2118 = null;
		int len = dataArea.getBytes().length;//
		String order = generateCommonCommandOrder(len,dataArea);
		if(MLog.isDebug) {
			MLog.d(TAG, "writeConnector2118： " + order);
		}
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
		int len =  dataArea.getBytes().length;//
		String order = generateCommonCommandOrder(len,dataArea);
		if(MLog.isDebug) {
			MLog.d(TAG, "readConnector2119： " + order);
		}
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
		int len = dataArea.getBytes().length;//
		String order = generateCommonCommandOrder(len,dataArea);
		if(MLog.isDebug) {
			MLog.d(TAG, "exitSimpleDiagnostic211a： " + order);
		}
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
		int len =  dataArea.getBytes().length;//
		String order = generateCommonCommandOrder(len,dataArea);
		if(MLog.isDebug) {
			MLog.d(TAG, "intoLowPowerMode211b： " + order);
		}
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
		int len =  dataArea.getBytes().length;//
		String order = generateCommonCommandOrder(len,dataArea);
		if(MLog.isDebug) {
			MLog.d(TAG, "upgrade2401： " + order);
		}
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
		int len =  dataArea.getBytes().length;//
		String order = generateCommonCommandOrder(len,dataArea);
		if(MLog.isDebug) {
			MLog.d(TAG, "upgradeFileName2402： " + order);
		}
		o2402 = ByteHexHelper.hexStringToBytes(order);
		return o2402;
	}
	/**
	 * 发送文件名称和文件长度
	 * @param file
	 * @return
	 */
	public byte[] sendFileNameAndLength2412(File file){
		return null;
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
	public byte[] upgradeFileContent2403(Long writePosition, Short dataLength, Buffer fileContent) {
		byte[] o2403 = null;
		int len =  dataArea.getBytes().length;//
		String order = generateCommonCommandOrder(len,dataArea);
		if(MLog.isDebug) {
			MLog.d(TAG, "upgradeFileContent2403： " + order);
		}
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
		int len =  dataArea.getBytes().length;//
		String order = generateCommonCommandOrder(len,dataArea);
		if(MLog.isDebug) {
			MLog.d(TAG, "upgradeFileConVerify2404： " + order);
		}
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
		int len = dataArea.getBytes().length;//
		String order = generateCommonCommandOrder(len,dataArea);
		if(MLog.isDebug) {
			MLog.d(TAG, "upgradeComplete2405： " + order);
		}
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
		int len = dataArea.getBytes().length;//
		String order = generateCommonCommandOrder(len,dataArea);
		if(MLog.isDebug) {
			MLog.d(TAG, "breakpointResume2406： " + order);
		}
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
		int len = dataArea.getBytes().length;//
		String order = generateCommonCommandOrder(len,dataArea);
		if(MLog.isDebug) {
			MLog.d(TAG, "updateFirmware2407： " + order);
		}
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
		int len =dataArea.getBytes().length;//
		String order = generateCommonCommandOrder(len,dataArea);
		if(MLog.isDebug) {
			MLog.d(TAG, "readModelFileInfo2408： " + order);
		}
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
		int len = dataArea.getBytes().length;//
		String order = generateCommonCommandOrder(len,dataArea);
		if(MLog.isDebug) {
			MLog.d(TAG, "link2501： " + order);
		}
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
		int len =  dataArea.getBytes().length;//
		String order = generateCommonCommandOrder(len,dataArea);
		if(MLog.isDebug) {
			MLog.d(TAG, "requestConnect2502： " + order);
		}
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
		int len =  dataArea.getBytes().length;//
		String order = generateCommonCommandOrder(len,dataArea);
		if(MLog.isDebug) {
			MLog.d(TAG, "securityCheck2503： " + order);
		}
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
		int len =  dataArea.getBytes().length;//
		String order = generateCommonCommandOrder(len,dataArea);
		if(MLog.isDebug) {
			MLog.d(TAG, "disconnected2504： " + order);
		}
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
		String dataArea2505="55aa00000000c005687f4f5d";
		String order = dataArea2505;
		if(MLog.isDebug) {
			MLog.d(TAG, "resetConnector2505： " + order);
		}
		o2505 = ByteHexHelper.hexStringToBytes(order);
		return o2505;
	}

	/**
	 * 进入一键诊断系统列表
	 * 
	 * @return
	 */
	public byte[] diagnosticList2500() {
		byte[] o2500 = null;
		int len =  dataArea.getBytes().length;//
		String order = generateCommonCommandOrder(len,dataArea);
		if(MLog.isDebug) {
			MLog.d(TAG, "diagnosticList2500： " + order);
		}
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
		String dataArea2701no = ByteHexHelper.bytesToHexString(param);
		MLog.e(TAG,"smartBox2701No------>>>>>  "+dataArea2701no);
		int len = 4+ ((dataArea2701no.getBytes().length+1)/2)+1;//
		String order = generateCommonCommandOrder(len,dataArea2701no);
		if(MLog.isDebug) {
			MLog.d(TAG, "smartBox2701No： " + order);
		}
		o2701 = ByteHexHelper.hexStringToBytes(order);
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
		int len =  dataArea.getBytes().length;//
		String order = generateCommonCommandOrder(len,dataArea);
		if(MLog.isDebug) {
			MLog.d(TAG, "sendFileNameAndLength2402： " + order);
		}
		o2402 = ByteHexHelper.hexStringToBytes(order);
		return o2402;
	}
	/**
	 * 发送升级文件MD5校验
	 * @param md5
	 * @return
	 */
	public byte[] sendUpdateFileMd52404(String md5) {
		//文件名跟文件长度
		byte[] o2404 = null;
		int len = dataArea.getBytes().length;//
		String order = generateCommonCommandOrder(len, dataArea);
		if (MLog.isDebug) {
			MLog.d(TAG, "sendUpdateFileMd52404： " + order);
		}
		o2404 = ByteHexHelper.hexStringToBytes(order);
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
		return o2414;
	}
	/**
	 * 完成升级确认
	 */
	public byte[] ValidateUpdateFinished2405() {
		//文件名跟文件长度
		byte[] o2405 = null;
		int len =  dataArea.getBytes().length;//
		String order = generateCommonCommandOrder(len, dataArea);
		if (MLog.isDebug) {
			MLog.d(TAG, "ValidateUpdateFinished2405： " + order);
		}
		o2405 = ByteHexHelper.hexStringToBytes(order);
		return o2405;
	}
	/**
	 * 验证所有文件的MD5信息
	 * @return
	 */
	public byte[] ValidateAllFilesMd52408() {
		//文件名跟文件长度
		byte[] o2408 = null;
		int len = dataArea.getBytes().length;//
		String order = generateCommonCommandOrder(len, dataArea);
		if (MLog.isDebug) {
			MLog.d(TAG, "ValidateAllFilesMd52408： " + order);
		}
		o2408 = ByteHexHelper.hexStringToBytes(order);
		return o2408;
	}
	
	/**
	 * 5.4.3	升级数据文件内容发送命令
	 * @param param
	 * @return
	 */
	public byte[] sendUpdateFilesContent2403(byte[] param) {
		//文件名跟文件长度
		byte[] o2403 = null;
		return o2403;
	}
	/**
	 * 5.4.3	升级数据文件内容发送命令
	 * @param param
	 * @return
	 */
	public byte[] sendUpdateFilesContent2413(byte[] param) {
		//文件名跟文件长度
		byte[] o2413 = null;
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
		if(MLog.isDebug) {
			MLog.d(TAG, "sendUpdateFilesContent2403： " + o2403);
		}
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
		String dataArea2701 = ByteHexHelper.bytesToHexString(param);
		MLog.d(TAG,"smartBox2701------>>>>>"+dataArea2701);
		int len = 4+ ((dataArea2701.getBytes().length+1)/2)+1;//
		String order = generateCommonCommandOrder(len, dataArea2701);
		if (MLog.isDebug) {
			MLog.d(TAG, "smartBox2701： " + order);
		}
		o2701 = ByteHexHelper.hexStringToBytes(order);
		return o2701;
	}
	
	/**
	 * 发送UPSTE!指令
	 * 
	 * @return
	 */
	public byte[] sendUpdate2505() {
		byte[] o2505 = null;		
		String dataArea2505="55504441544521";
		String order = dataArea2505;
		if(MLog.isDebug) {
			MLog.d(TAG, "sendUpdate2505： " + order);
		}
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
		String dataArea2505="55AA000EFFF1630001C20000000000A0";
		String order = dataArea2505;
		if(MLog.isDebug) {
			MLog.d(TAG, "setBautrate2505： " + order);
		}
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
		String dataArea2505="55AA000EFFF1620001000000068000E5";
		String order = dataArea2505;
		if(MLog.isDebug) {
			MLog.d(TAG, "setAddressAndSize2505： " + order);
		}
		o2505 = ByteHexHelper.hexStringToBytes(order);
		return o2505;
	}
	
	public static String matchLen(String len) {
		while (len.length() < 2) {
			len = "0" + len;
		}
		return len;
	}
	/**
	 *读当前接头是否已经烧录
	 *
	 * @return
	 */
	public byte[] resetConnector2107(){
		byte[] o2107 = null;
		return o2107;
	}

	/**
	 *  读取车辆电压信息
	 * @author weizhongxuan
	 * @time 2016/10/13 14:22
	 */
	public byte[] vehicleVoltageInfo2120() {
		byte[] o2120 = null;
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
		return new byte[]{};
	}
	public byte[] generateCommonCommand(byte[] command,byte[] data ) {
		return generateCommonCommand(ByteHexHelper.bytesToHexStringWithSearchTable(command),
				ByteHexHelper.bytesToHexStringWithSearchTable(data));
	}
	/**
	 *生成通用Smartbox30 linux系统交互指令
	 * @param command
	 * @param data
	 * @return
	 */
	public  byte[] generateSmartbox30LinuxCommonCommand(String command,String data ) {
		return new byte[]{};
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
	public  byte[] dpuDateSoftcodeRegister2131(int datNum,String datDate){
		return new byte[]{};
	}
	public  byte[] softVer2134(String softPackageId, String versionNo){
		return new byte[]{};
	}
	public  byte[] activation2133(String effectDate, String sysCode){
		return new byte[]{};
	}

	@Override
	public byte[] readRandom212101() {
		return new byte[0];
	}
	public byte[] sendEncResult212102(String encResult) {
		return new byte[0];
	}
}
