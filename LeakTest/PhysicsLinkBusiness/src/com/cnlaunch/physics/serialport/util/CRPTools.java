/*
    Launch Android Client, CRPTools
    Copyright (c) 2014 LAUNCH Tech Company Limited
    http:www.cnlaunch.com
*/

package com.cnlaunch.physics.serialport.util;


/**
 * [CRP 工具类]
 * 
 * @author zengdengyi
 * @version 1.0
 * @date 2014-3-3
 * 
 **/
public class CRPTools {
	
	public static final String  SERIALPORT_LIB = "serial_port";//串口.so库
	public static final String  DEVICE_NAME_HEQIANG = "/dev/ttyS2";//和强设备要打开的串口编号
	public static final String  DEVICE_NAME_SANMU = "/dev/ttyMT0";//三木设备要打开的串口编号
	public static final int       BAUD_RATE = 115200;//串口波特率
	public static final String  SERIALPORT_VERSION = "/usr/ver.txt";//和强板子版本信息
}
