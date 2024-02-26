package com.cnlaunch.physics.downloadbin.util;

import java.io.File;
import java.nio.Buffer;
/**
 * 蓝牙byte[]数据拼接
 * @author Administrator
 *
 */
public abstract class OrderMontage {

	/**
	 * 读取时钟
	 * 
	 * @return
	 */
	public abstract byte[] readClock2102();

	/**
	 * 设置时钟
	 * 
	 * @return
	 */
	public abstract byte[] setClock2101();

	/**
	 * 读取DPU接头硬件版本信息
	 * 
	 * @return
	 */
	public abstract byte[] DPUVerInfo2103();

	/**
	 * 读取DPU接头库文件版本(车型名称，车型版本，语言)
	 * 
	 * @return
	 */
	public abstract byte[] DPUKuVer2104();

	/**
	 * 取DPU接头软件版本(boot,download,诊断软件)
	 * 
	 * @return
	 */
	public abstract byte[] DPUVer2105();

	/**
	 * 取DPU接头防篡改标识
	 * 
	 * @return
	 */
	public abstract byte[] DPU2106();

	/**
	 * 写DPU接头序列号
	 * 
	 * @param mode
	 *            写入模式
	 * @param serialNum
	 *            序列号
	 * @return
	 */

	public abstract byte[] writeDPUSerialNum2107(String mode, String serialNum);

	/**
	 * 设置蓝牙名称 蓝牙名称最多16个半角字符
	 * 
	 * @param name
	 * @return
	 */
	public abstract byte[] setBluetoothName2108(String name);
	
	/**
	 * 复位DPU运行模式(用于切换诊断模式时) GetMode=0(返回当前工作模式,不会复位) 四种模式切换： SMARTBOX=1;
	 * MYCAR=2; CREADER=3; CRECORDER=4; -------- OBD=5; QUICKDIAG=6;
	 * 
	 * @param mode
	 * @return
	 */
	public abstract byte[] transferDPUMode2109(int mode);

	/**
	 * 清除flash数据
	 * 
	 * @param name
	 * @return
	 */
	public abstract byte[] clearFlash210A(String code);

	/**
	 * 设置或修改安全密码指令
	 * 
	 * @param oldPw
	 * @param newPw
	 * @return
	 */
	public abstract byte[] modifyPw210B(String oldPw, String newPw);

	/**
	 * 恢复初始密码
	 * 
	 * @param str
	 *            加密字符串
	 * @return
	 */
	public abstract byte[] resumePw210f(String str);
	
	/**
	 * 验证安全密码指令
	 * 
	 * @param oldPw
	 * @return
	 */
	public abstract byte[] resumePw2110(String oldPw);

	/**
	 * 跳转至download代码入口
	 * 
	 * @return
	 */
	public abstract byte[] download2111();

	/***
	 * 写接头配置文件
	 * 
	 * @param fileLength
	 *            待写入数据文件长度
	 * @param content
	 *            待写入文件内容
	 * @return
	 */
	public abstract byte[] writeConFile2112(Short fileLength, Buffer content);

	/**
	 * 读接头配置文件
	 * 
	 * @return
	 */
	public abstract byte[] readConFile2113();

	/**
	 * 读取当前状态
	 * 
	 * @return
	 */
	public abstract byte[] currentStatus2114();

	/**
	 * 读DPU接头Licence
	 * 
	 * @return
	 */
	public abstract byte[] DPULicence2115();

	/**
	 * 读DPU接头蓝牙地址
	 * 
	 * @return
	 */
	public abstract byte[] DPUBluetoothAddress2116();

	/**
	 * 验证DPU接头
	 * 
	 * @param ucContext
	 * @param ucSignature
	 * @return
	 */
	public abstract byte[] checkDPU2117(String ucContext, String ucSignature);

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
	public abstract byte[] writeConnector2118(String fileName, Short fileSize,
			Buffer fileContent);

	/**
	 * 读接头数据文件
	 * 
	 * @param fileName
	 *            文件名称
	 * @return
	 */
	public abstract byte[] readConnector2119(String fileName);

	/**
	 * 退出简单诊断调度
	 * 
	 * @return
	 */
	public abstract byte[] exitSimpleDiagnostic211a();

	/**
	 * 进入低功耗模式(仅针对蓝牙)
	 * 
	 * @return
	 */
	public abstract byte[] intoLowPowerMode211b();

	/*********** 软件升级0x24 ************/

	/**
	 * 准备从端升级命令(车型文件) 主端发送至从端命令
	 * 
	 * @param buffer
	 * @return
	 */
	public abstract byte[] upgrade2401(String buffer);

	/**
	 * 升级数据文件名发送命令 主端发送至从端命令
	 * 
	 * @param fileName
	 *            文件名称
	 * @param fileSize
	 *            文件长度
	 * @return
	 */
	public abstract byte[] upgradeFileName2402(String fileName, Long fileSize);

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
	public abstract byte[] upgradeFileContent2403(Long writePosition,
			Short dataLength, Buffer fileContent);

	/**
	 * 升级数据文件内容校验数据发送命令 主端发送至从端命令
	 * 
	 * @param md5Str
	 *            md5校验字节 32字节
	 * @return
	 */
	public abstract byte[] upgradeFileConVerify2404(String md5Str);

	/**
	 * 完成升级命令 主端发送至从端命令
	 * 
	 * @return
	 */
	public abstract byte[] upgradeComplete2405();

	/**
	 * 断点续传命令 主端发送至从端命令
	 * 
	 * @return
	 */
	public abstract byte[] breakpointResume2406();

	/**
	 * 启动更新固件命令 主端发送至从端命令
	 * 
	 * @return
	 */
	public abstract byte[] updateFirmware2407();

	/**
	 * 读取DPU接头车型文件信息 主端发送至从端命令 buffer 包括(文件数量，文件名1,文件1 md5,文件名2,文件2 md5,文件名3,文件3
	 * md5,文件名4,文件4 md5,文件名5,文件5 md5,)
	 * 
	 * @return
	 */
	public abstract byte[] readModelFileInfo2408(String buffer);

	/********************** 建立连接，安全校验，断开连接及链路0x25 ********************/

	/**
	 * 链路保持
	 * 
	 * @return
	 */
	public abstract byte[] link2501();

	/**
	 * 请求连接
	 * 
	 * @return
	 */
	public abstract byte[] requestConnect2502();

	/**
	 * 安全校验
	 * 
	 * @return
	 */
	public abstract byte[] securityCheck2503(String verify);

	/**
	 * 关闭连接
	 * 
	 * @return
	 */
	public abstract byte[] disconnected2504();

	/**
	 * 复位接头
	 * 
	 * @return
	 */
	public abstract byte[] resetConnector2505();

	/**
	 * 进入一键诊断系统列表
	 * 
	 * @return
	 */
	public abstract byte[] diagnosticList2500();
	
	/**
	 * 直接发送
	 * @param param 完整命令
	 * @return
	 */
	
	public abstract byte[] smartBox2701No(byte[] param);
	/**
	 * 发送文件名称和文件长度
	 * @param file
	 * @return
	*/
	public abstract byte[] sendFileNameAndLength2402(File file);
	/**
	 * 发送文件名称和文件长度
	 * @param file
	 * @return
	 */
	public abstract byte[] sendFileNameAndLength2412(File file);
	/**
	 * 发送升级文件MD5校验
	 * @param md5
	 * @return
	 */
	public abstract byte[] sendUpdateFileMd52404(String md5);
	/**
	 * 发送升级文件MD5校验
	 * @param md5
	 * @return
	 */
	public abstract byte[] sendUpdateFileMd52414(String md5);

	/**
	 * 完成升级确认
	 */
	public abstract byte[] ValidateUpdateFinished2405();
	
	/**
	 * 验证所有文件的MD5信息
	 * @param md5info
	 * @return
	 */
	public abstract byte[] ValidateAllFilesMd52408();
	
	/**
	 * 5.4.3	升级数据文件内容发送命令
	 * @param md5info
	 * @return
	 */
	public abstract byte[] sendUpdateFilesContent2403(byte[] param);
	/**
	 * 5.4.3	升级数据文件内容发送命令
	 * @param md5info
	 * @return
	 */
	public abstract byte[] sendUpdateFilesContent2413(byte[] param);

	/**
	 * 5.4.3	升级数据文件内容发送命令
	 * @param md5info
	 * @return
	 */
	public abstract byte[] sendUpdateFilesContent2403(String addressbytes , String sendbuff);
	
	
	/********************* SmartBox模式 0x27 ******************************/
	
	/**
	 * 直接发送
	 * @param param 完整命令
	 * @return
	 */
	
	public abstract byte[] smartBox2701(byte[] param); 

	/**
	 * 发送update指令
	 * 
	 * @return
	 */
	public abstract byte[] sendUpdate2505();

	/**
	 * 设置波特率
	 * 
	 * @return
	 */
	public abstract byte[] setBautrate2505();

	/**
	 * 设置地址和大小
	 * 
	 * @return
	 */
	public abstract byte[] setAddressAndSize2505();

	/**
	 * 读当前接头是否已经烧录
	 *
	 * @return
	 */
	public abstract byte[] resetConnector2107();
	/**
	 *  读取车辆电压信息
	 * @author weizhongxuan
	 * @time 2016/10/13 14:21
	 */
	public abstract byte[] vehicleVoltageInfo2120();
	
	/**
	 * 生成通用下位机指令
	 * 
	 * @author xiefeihong
	 * @time 2016/10/26 14:19	
	 * @param command //命令字
	 * @param data //数据
	 * @return
	 */
	public abstract byte[] generateCommonCommand(String command,String data ) ;

	/**
	 * 生成通用下位机指令
	 * @param command
	 * @param data
	 * @return
	 */
	public abstract byte[] generateCommonCommand(byte[] command,byte[] data ) ;

	/**
	 *生成通用Smartbox30 linux系统交互指令
	 * @param command
	 * @param data
	 * @return
	 */
	public abstract byte[] generateSmartbox30LinuxCommonCommand(String command,String data ) ;

	/**
	 * 生成通用Smartbox30 linux系统交互指令
	 * @param command
	 * @param data
	 * @return
	 */
	public abstract byte[] generateSmartbox30LinuxCommonCommand(byte[] command,byte[] data ) ;
	public abstract byte[] dpuDateSoftcodeRegister2131(int datNum,String datDate);
	public abstract byte[] softVer2134(String softPackageId, String versionNo);
	public abstract byte[] activation2133(String effectDate, String sysCode);
	public abstract byte[] readRandom212101();
	public abstract byte[] sendEncResult212102(String encResult);
}
