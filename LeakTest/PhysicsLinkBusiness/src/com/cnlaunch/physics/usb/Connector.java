package com.cnlaunch.physics.usb;
/**
 * 
 * USB状态常量管理
 * <br/>
 * 
 * 
 * @author LiangHua
 * @version V1.0
 * @date 2015.08
 */
public  class Connector {
	/* 接头操作/运行状态 */
	public static final int STATE_SUCCESS = 0;// OK
	public static final int STATE_RW_TIMEOUT = -1;// read/write time out/device is close
	public static final int STATE_UNKNOWN = -2;// Unknow state
	public static final int STATE_INSTANCE_FAILED = -3;// // Connector class instance failed
	public static final int STATE_NO_INSTANCE = -4;// No Connector class instance
	public static final int STATE_INSTANCED = -5;// Connector class instance
	public static final int STATE_OPEN_FAILED = -6;// Connector open failed
	public static final int STATE_RUNNING = -7;// Connector is Running
	public static final int STATE_CLOSED = -8;// Connector was Colsed
	public static final int STATE_CONNECTING = -9;// Connector is connecting
	public static final int STATE_CONNECT_FAILED = -10;// Connector connect failed
	public static final int STATE_CONNECTED = -11;// Connector connected
	public static final int STATE_DISCONNECT = -12;// Connector disconnect
	public static final int STATE_NO_DEVICE_DETECTED = -13;// No Connector device detected
	public static final int STATE_DEVICE_NOT_SUPPORT = -14;// the device is not support
	public static final int STATE_GOT_DEVICE_NOT_OPEN = -15;// Get Connector device, but not be open
	public static final int STATE_ARG_ERROR = -16;// Argument error
	public static final int STATE_NO_PERMISSION = -17;// No Connector device access permission
	public static final int STATE_HAS_PERMISSION = -18;// Has Connector device access permission
	public static final int STATE_NO_EXCLUSIVE_ACCESS = -19;// No PERMISSION for exclusive access Connector(USB)
	public static final int STATE_NO_EP_IN = -20;// No endpoint for in(USB)
	public static final int STATE_NO_EP_OUT = -21;// No endpoint for out(USB)
	public static final int STATE_NO_ANY_EP = -22;// No any endpoint for out end in(USB)
	public static final int STATE_GET_SUPPORT_DEV_ID_FAILED = -23;// Get support ID for Connector device failed(USB)

	
}
