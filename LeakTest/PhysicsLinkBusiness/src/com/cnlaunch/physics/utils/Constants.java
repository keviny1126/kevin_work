/*
    Launch Android Client, Constant
    Copyright (c) 2014 LAUNCH Tech Company Limited
    http:www.cnlaunch.com
 */

package com.cnlaunch.physics.utils;

/**
 * [A brief description]
 *
 * @author huxinwu
 * @version 1.0
 * @date 2014-3-7
 **/
public abstract class Constants {
    public final static String VER_VALUE = "5.3.0";

    /**
     * 接头是否选择使用SerialPort通讯模式
     */
    public static final String LINK_MODE_SERIALPORT_SWITCH = "link_mode_serialport_switch";
    /**
     * 接头是否选择使用wifi通讯模式
     */
    public static final String LINK_MODE_WIFI_SWITCH = "link_mode_wifi_switch";
    /**
     * 模拟接头是否选择使用wifi通讯模式
     */
    public static final String LINK_MODE_WIFI_SWITCH_FOR_SIMULATE = "link_mode_wifi_switch_for_simulate";

    /**
     * 接头选择使用wifi通讯模式时，wifi工作模式
     */
    public static final String WIFI_WORK_MODE = "wifi_work_mode";

    /**
     * 接头选择使用smartlink的网络模式
     */
    public static final String SMART_LINK_SOCKET = "smart_link_socket";

    /**
     * wifi工作模式常量定义
     * WIFI_WORK_MODE_UNKNOWN  wifi工作模式未知
     * WIFI_WORK_MODE_WITH_AP  热点
     * WIFI_WORK_MODE_WITH_STA_MODE_NO_INTERACTION 网卡模式而且无交互界面（连接热点时不能输入用户名，密码）
     * WIFI_WORK_MODE_WITH_STA_MODE_HAVE_INTERACTION 网卡模式而且有交互界面
     */
    public static final int WIFI_WORK_MODE_UNKNOWN = 0;
    public static final int WIFI_WORK_MODE_WITH_AP = 1;
    public static final int WIFI_WORK_MODE_WITH_STA_MODE_NO_INTERACTION = 2;
    public static final int WIFI_WORK_MODE_WITH_STA_MODE_HAVE_INTERACTION = 3;

    /**
     * SMARTBOX30USB工作模式常量定义
     * USB_WORK_MODE_UNKNOWN  USB工作模式未知
     * USB_WORK_MODE_WITH_SERIALPORT  USB转串口
     * USB_WORK_MODE_WITH_ETHERNET 	USB转网卡
     * USB_WORK_MODE_WITH_BULK  USB BULK模式
     */
    public static final int USB_WORK_MODE_UNKNOWN = 0;
    public static final int USB_WORK_MODE_WITH_SERIALPORT = 1;
    public static final int USB_WORK_MODE_WITH_ETHERNET = 2;
    public static final int USB_WORK_MODE_WITH_BULK = 3;

    /**
     * SMARTBOX30支持接头序列号前缀
     */
    public final static String USB_WORK_MODE = "usb_work_mode";
    public static final String WIFI_AP_SSID = "AP_SSID";
    public static final String WIFI_AP_NETWORK_ID = "AP_NETWORK_ID";
    public static final String WIFI_AP_SECURITY = "AP_SECURITY";
    public static final String WIFI_AP_PASSWORD = "AP_PASSWORD";
    public static final String DPU_SETTINGS_INFORMATION = "dpu_settings_information.txt";
    public static final String DPU_DOWNLOADBIN_INFORMATION = "dpu_downloadbin_information.txt";

    public static final String PRODUCTTYPE_KEY = "productType";

    /**
     * 接头是否选择使用蓝牙通讯模式
     */
    public static final String LINK_MODE_BLUETOOTH_SWITCH = "link_mode_bluetooth_switch";

    public final static String EASYDIAG30_AND_MASTER30_PREFIX = "easydiag30_and_master30_serialno_prefix";

    /**
     * EASYDIAG4 和 ThinKDiag、iland2
     */
    public final static String ED4 = "98942,97986,97974,97977,98926,98927,96064";

    /**
     * 需要固件加密的
     */
    public final static String DOWNLOADBIN_ENCRY = "98942,97986,98927,96068,98897";
    /**
     * SMARTBOX30支持接头序列号前缀
     */
    public final static String SMARTBOX30_SUPPORT_SERIALNO_PREFIX = "smartbox30_support_serialno_prefix";
    /**
     * 是否支持双wifi
     */
    public final static String IS_SUPPORT_DUAL_WIFI = "is_support_dual_wifi";
    public final static String IS_SUPPORT_DUAL_WIFI_WITH_DISPLAY_LINK_SETTING = "is_support_dual_wifi_with_display_link_setting";
    public final static String SMARTBOX30_AP_PASSWORD = "12345678";


    /**
     * 动态串口
     */
    public static final String DYNAMIC_SERIAL_PORT = "dynamic_serial_port";

    public static final String IS_NEED_REPLACE_LAUNCH = "is_need_replace_launch";

    /**
     * smart ht 接头配置
     */
    public static final String SMARTLINKC_SUPPORT_SERIALNO_PREFIX = "smartlinkc_support_serialno_prefix";
    /**
     * 是否支持wifi优先，区分双wifi,wifi优先不一定存在两块wifi,但是优先使用wifi诊断
     */
    public final static String IS_SUPPORT_WIFI_PRIORITY = "is_support_wifi_priority";
    public static final String IS_NEW_DUAL_WIFI_SUPPORT = "is_new_dual_wifi_support";
    public final static String EUROMINI403_PREFIX = "euromini403_prefix";
}
