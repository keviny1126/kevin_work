package com.cnlaunch.physics.impl;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Timer;

import com.cnlaunch.physics.utils.Bridge;

import android.content.Context;

public interface IPhysics {
	public static final String ACTION_DPU_DEVICE_CONNECT_SUCCESS = "deviceConnectSuccessAction";
	public static final String ACTION_DPU_DEVICE_CONNECT_SUCCESS_WITH_BACKGROUND = "deviceConnectSuccessBackground";
	public static final String ACTION_DPU_DEVICE_CONNECT_FAIL = "deviceConnectFailedAction";
	public static final String ACTION_DPU_DEVICE_CONNECT_DISCONNECTED = "deviceConnectDisconnectedAction";
	public static final String ACTION_CUSTOM_WIFI_CONNECT_DISCONNECTED = "WifiConnectedDisconnected"; //双wifi中的自定义wifi已经从连接状态断开
	public static final String IS_CONNECT_FAIL = "is_connect_fail";
	public static final String CONNECT_FAIL_REASON = "connect_fail_reason";
	public static final String IS_DOWNLOAD_BIN_FIX = "isFix";
	public static final String CONNECT_MESSAGE_KEY = "message";
	String CONNECT_TYPE = "connect_type";
	
	public static final int STATE_NONE = 0; // 默认状态
	public static final int STATE_FAILED = 1; // 连接失败，目前只用于诊断过程中的连接状态说明
	public static final int STATE_CONNECTING = 2; // 正在连接中
	public static final int STATE_CONNECTED = 3; // 已连接到一个蓝牙设备

	public static final String ACTION_DIAG_CONNECTED  = "com.newchip.intent.action.connect";
	public static final String ACTION_DIAG_UNCONNECTED  = "com.newchip.intent.action.unconnect";

	public static final int REASON_UNKNOWN = 0;
	public static final int REASON_BLUETOOTH_NOPAIRED = -1;
	public static final int REASON_CONNECT_TIMEOUT = -2;
	public static final int REASON_CREATEBLUETOOTHSOCKET_FAILED = -3;
	public static final int REASON_INVALID_PARAMETER = -4;
	public static final int REASON_USB_NO_PERMISSION = -5;
	public static final int REASON_USB_OPENFAILED = -6;

	public static final String  SMARTBOX30_WIFI_CONNECT_ADDRESS ="192.168.100.1";
	public static final int     SMARTBOX30_WIFI_CONNECT_DIAGNOSE_PORT = 22488;
	public static final int     SMARTBOX30_WIFI_CONNECT_SYSTEM_PORT = 22400;
	public static final int     SMARTBOX30_DPU_CONNECTOR_UDP_REMOTE_PORT =22534 ;
	public static final int     SMARTBOX30_DPU_SYSTEM_UDP_REMOTE_PORT =22536 ;

	public static final String DEVICE_DATA_TYPE_LEAK = "device_data_type_leak";
	//wifi网卡模式UDP广播包端口：22534
	//下位机TCP Server端   诊断下位机透传端口 22488
	//下位机TCP Server端   A7端口 22400 目前android版本只有smartbox30接头系统升级时用到
	/**
	 * 获取连接状态
	 * @return
	 */
	public int getState();
	
	/**
	 * 获取读取到的完整指令
	 * 
	 * @return
	 */
	public String getCommand();

	/**
	 * 设置完整指令内容
	 * 
	 * @param command
	 */
	public void setCommand(String command);

	/**
	 * 获取读数据流
	 * @return
	 */
	public InputStream getInputStream();

	/**
	 * 获取写数据流
	 * @return
	 */
	public OutputStream getOutputStream();
	
	/**获取命令等待阻塞**/
	public boolean getCommand_wait();

	/**设置命令等待阻塞**/
	public void setCommand_wait(boolean wait);
	/**
	 * 获取上下文
	 */
	public Context getContext();
	/**
	 * 获取设备名称
	 * @return
	 */
	public String getDeviceName();
	/**
	 * 关闭设备
	 */
	public void  closeDevice();
	/**
	 * 物理关闭当前连接设备
	 */
	public void  physicalCloseDevice();
	/**
	 * 设置接头设备序列号
	 */
	public void setSerialNo(String serialNo);
	public String getSerialNo();
	
	/**
	 * 与重卡一代的复位信息相关
	 */
	public void setIsTruckReset(boolean isTruckReset);
	public boolean isTruckReset();
	/**
	 * 设备处于已经连接状态时，与ui交互设置
	 * 一般采用广播实现
	 */
	public void userInteractionWhenDPUConnected();
	/**
	 * 设置是否为固件升级处理模式
	 */
	public void setIsFix(boolean isFix);
	/**
	 * 设置是否为远程诊断客户端模式
	 */
	public void setIsRemoteClientDiagnoseMode(boolean isRemoteClientDiagnoseMode);
	public boolean getIsRemoteClientDiagnoseMode();

	/**
	 * 是否支持一问多答数据收发模式
	 */
	public void setIsSupportOneRequestMoreAnswerDiagnoseMode(boolean isSupportOneRequestMoreAnswerDiagnoseMode);
	public boolean getIsSupportOneRequestMoreAnswerDiagnoseMode();

	/**
	 * DHC 20200701
	 * 设置完整指令内容
	 * @param command
	 * @param isSupportSelfSend 是否支持自己外部通信（针对蓝牙一对多的项目）
	 */
	void setCommand(String command,boolean isSupportSelfSend);

	/**
	 * DHC 20200708
	 * 控制数据传输给需要的连接对象
	 * @param isSetCommandOfMainLink 设置数据是否传给蓝牙主连接（针对蓝牙一对多的项目）
	 */
	void setCommandStatus(boolean isSetCommandOfMainLink);

	boolean getCommandStatus();

	public void setDataType(String type);
	public String getDataType();
}
