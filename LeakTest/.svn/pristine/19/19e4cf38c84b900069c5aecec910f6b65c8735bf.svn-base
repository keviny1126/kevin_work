package com.cnlaunch.physics.utils.message;
/** 
*通信协议
*1.概述
*此格式定义，主要用于实现正常通信演示。不保证通信效率及通信安全，但需保证能进一步演化增强。
*传输层，以Message为传输单元。数据处理时，以Message为最小单位
*(进一步，需将传输单元定义为“Message Segment”，以封闭分包组包的过程)
*
*2.Message格式定义（协议版本， 标志位，   长度，	  消息内容
*协议版本：8位 1字节
*标志位：8位 1字节
*长度：32位 4字节
*消息内容：最大长度 MaxInt32 4字节
*
*3.Message Body格式定义
*消息类型	消息数据
*消息类型：32位，高8位为消息类型来源，后24位为消息类型
*消息数据：MaxInt32 – 32
*
*
*3.1类型来源 
*
*服务器接收的类型 
*1来自客户端
*客户端接收的类型
*2 来自服务端
*
*3.2消息类型
*来源	类型	消息描述	示例数据
*0x1	0x000001	客户诊断操作回复	NULL
*0x1	0x000002	客户其他操作备用	NULL
*0x2	0x000003	服务器已经接受其他远程诊断，请关闭当前连接	{“username”=”123456,password=”123456”}
*0x2	0x000004	服务器送出诊断数据	{“reason”=”crash”}或{“reason”=”normal”}
*
*3.3消息数据
*可以为二进制数据，也可以为文本
 * @author xiefeihong
 *
 */
public class RemoteMessage {
	/**
	 * 协议版本
	 */
	private byte typecode; //协议版本， 标志位，   长度，	  消息内容
	/**
	 * 标志位
	 */
	private byte flag;
	/**
	 * 长度
	 */
	private int size;
	/**
	 * 消息内容
	 */
	private byte[] content;

	public synchronized byte getTypecode() {
		return typecode;
	}

	public synchronized void setTypecode(byte typecode) {
		this.typecode = typecode;
	}

	public synchronized byte getFlag() {
		return flag;
	}

	public synchronized void setFlag(byte flag) {
		this.flag = flag;
	}

	public synchronized int getSize() {
		return size;
	}

	public synchronized void setSize(int size) {
		this.size = size;
	}

	public synchronized byte[] getContent() {
		return content;
	}

	public synchronized void setContent(byte[] content) {
		this.content = content;
	}
	@Override
	protected void	finalize(){
		this.content = null;
	}
	/**
	 * 注释：四字节数组到int的转换
	 * 
	 * 
	 * @param b
	 * @return
	 */
	public static int bytesToInt(byte[] b, int offset) {
		int s = 0;
		int s0 = b[offset + 0] & 0xff;// 最低位
		int s1 = b[offset + 1] & 0xff;
		int s2 = b[offset + 2] & 0xff;
		int s3 = b[offset + 3] & 0xff;// 最高位
		s3 <<= 24;
		s2 <<= 16;
		s1 <<= 8;
		s = s0 | s1 | s2 | s3;
		return s;
	}

	public static int bytesToInt(byte[] b) {
		int s = 0;
		int s0 = b[0] & 0xff;// 最低位
		int s1 = b[1] & 0xff;
		int s2 = b[2] & 0xff;
		int s3 = b[3] & 0xff;//最高位
		s3 <<= 24;
		s2 <<= 16;
		s1 <<= 8;
		s = s0 | s1 | s2 | s3;
		return s;
	}

	/**
	 * 注释：int到字节数组的转换
	 * 
	 * 
	 * @param number
	 * @return
	 */
	public static byte[] intToBytes(int number) {
		int temp = number;
		byte[] b = new byte[4];
		for (int i = 0; i < b.length; i++) {
			b[i] = new Integer(temp & 0xff).byteValue();// 将最低位保存在最低位
			temp = temp >> 8; // 向右移8位
		}
		return b;
	}

	public RemoteMessage() {
		this.typecode = 1; //默认值
		this.flag = 2; //默认值
		size = 0;
		this.content =null;
	}
	public RemoteMessage(byte[] content) {
		this();
		this.size = content.length;
		this.content = content;
	}
	public RemoteMessage(byte typecode, byte flag, byte[] content) {
		this.typecode = typecode;
		this.flag = flag;
		size = content.length;
		this.content = content;
	}

	public byte[] toBytes() {
		/*
		 * typecode 1字节 flag 1字节 size 4字节 content 实际长度content.length;
		 */
		int bufferSize = 1 + 1 + 4 + content.length;
		byte[] buffer = new byte[bufferSize];

		buffer[0] = typecode;
		buffer[1] = flag;
		byte[] tempSizeBytes = intToBytes(size);
		System.arraycopy(tempSizeBytes, 0, buffer, 2, tempSizeBytes.length);
		System.arraycopy(content, 0, buffer, 6, content.length);
		tempSizeBytes = null;
		return buffer;
	}

	public static RemoteMessage fromBytes(byte[] buffer) {
		RemoteMessage message = new RemoteMessage();
		message.typecode = buffer[0];
		message.flag = buffer[1];
		message.size = bytesToInt(buffer, 2);
		if (message.size > 0) {
			message.content = new byte[message.size];
			System.arraycopy(buffer, 6, message.content, 0, message.size);
		}
		return message;
	}
	public static class MessageSource  //消息来源
	{
		public final static  int CLIENT_TO_SERVER  = 1 ;  //客户端到服务器端消息
		public final static int SERVER_TO_CLIENT  = 2;  //服务器端到客户端消息
		public final static  int CLIENT_TO_SERVER_FOR_WIFI_SETTINGS  = 3 ;  //客户端到服务器端的WIFI设定消息
		public final static int SERVER_TO_CLIENT_FOR_WIFI_SETTINGS  = 4;  //服务器端到客户端的WIFI设定消息
	}
	/**
	 * 消息类型,最大值为24位
	 * @author xiefeihong
	 *
	 */
	public static class MessageCategory {
		public final static int CLIENT_TO_SERVER_DIAGNOSE_FEEDBACK_MESSAGE = 0x000001;
		public final static int CLIENT_TO_SERVER_BACKUP = 0x000002;
		public final static int SERVER_TO_CLIENT_CLOSE_CONNECT_FOR_WEB = 0x000003;
		public final static int SERVER_TO_CLIENT_DIAGNOSE_CONTENT = 0x000004;

		public final static int CLIENT_TO_SERVER_DIAGNOSE_FEEDBACK_PAGE_STREAM_MESSAGE = 0x000005;
		public final static int CLIENT_TO_SERVER_DIAGNOSE_FEEDBACK_MASK_MESSAGE = 0x000006;
		public final static int CLIENT_TO_SERVER_DIAGNOSE_FEEDBACK_BYTE_DATA_MESSAGE = 0x000007;
		public final static int CLIENT_TO_SERVER_DIAGNOSE_FEEDBACK_SEND_CUSTOM_DIALOG_MESSAGE = 0x000008;
		public final static int CLIENT_TO_SERVER_DIAGNOSE_FEEDBACK_SET_DATA_STREAM_RECORD_FLAG_MESSAGE = 0x000009;

		public final static int SERVER_TO_CLIENT_CLOSE_CONNECT_FOR_CLASSICS_BLUETOOTH_DEVICE = 0x000010;
		public final static int SERVER_TO_CLIENT_CLOSE_CONNECT_FOR_BLE_BLUETOOTH_DEVICE = 0x000011;

		public final static int CLIENT_TO_SERVER_DIAGNOSE_FEEDBACK_SET_REMOTEDATATYPE_MESSAGE = 0x000012;

		//wifi设置启动，用于服务器明确为wifi 设置操作，并开始一些初始化动作 无内容
		public final static int CLIENT_TO_SERVER_WIFI_SETTINGS_START_MESSAGE = 0X000013;
		//设置wifi状态
		// 内容格式参考：打开wifi {"state":true}，关闭wifi{"state":true}
		public final static int CLIENT_TO_SERVER_WIFI_SETTINGS_SET_WIFI_STATE_MESSAGE = 0X000014;
		//扫描wifi热点 无内容
		public final static int CLIENT_TO_SERVER_WIFI_SETTINGS_SCAN_WIFI_MESSAGE = 0x000015;
		//连接网络
		//内容格式参考为：{"security":0, "ssid":"Launch_staff", "password":""}
		//内容格式参考为：{"security":2, "ssid":"Launch_CN", "password":"12345678"}
		public final static int CLIENT_TO_SERVER_WIFI_SETTINGS_CONNECT_NETWORK_MESSAGE = 0x000016;
		//修改网络，添加网络
		//内容格式参考为：{"security":0, "ssid":"Launch_staff", "password":""}
		//内容格式参考为：{"security":2, "ssid":"Launch_CN", "password":"12345678"}
		public final static int CLIENT_TO_SERVER_WIFI_SETTINGS_SAVE_NETWORK_MESSAGE = 0x000017;
		//取消保存网络
		// 内容格式参考为：{"networkId":1}
		public final static int CLIENT_TO_SERVER_WIFI_SETTINGS_DELETE_NETWORK_MESSAGE = 0x000018;
		//从服务端返回的wifi状态信息 内容为：打开wifi {"state":true}，关闭wifi{"state":true}
		public final static int SERVER_TO_CLIENT_WIFI_SETTINGS_GET_WIFI_STATE_MESSAGE = 0X000019;
		//从服务端返回的扫描wifi热点信息 内容格式参考为：
		//[{"SSID":"Launch_staff","rssi":-54,"securityMode":0,"pskType":-1,"summary":"Saved","isActive":false,"isSave":true,"networkId":0},
		//{"SSID":"ceshi_5F","BSSID":"50:46:5d:5c:a9:b0","rssi":-55,"securityMode":2,"pskType":2,"summary":"","isActive":false,"isSave":false,"networkId":-1},
		//{"SSID":"360-yzp","BSSID":"b0:d5:9d:8c:dc:a9","rssi":-60,"securityMode":2,"pskType":3,"summary":"","isActive":false,"isSave":false,"networkId":-1}]
		public final static int SERVER_TO_CLIENT_WIFI_SETTINGS_GET_SCAN_RESULT_MESSAGE = 0x000020;

		//提示当前连接因为条件限制需要关闭,可供不同的消息来源使用
		public final static int SERVER_TO_CLIENT_CLOSE_CONNECT_FOR_OTHER_ACTION = 0x000021;
		//连接保存过的网络
		// 内容格式参考为：{"networkId":1}
		public final static int CLIENT_TO_SERVER_WIFI_SETTINGS_CONNECT_SAVED_NETWORK_MESSAGE = 0x000022;
	}
}