package com.cnlaunch.physics.wifi.settings;

/**
 * Created by xiefeihong on 2017/09/29.
 * OPEN, WEP, WPA, WPA2（hide）
 */

public  class SecurityMode {
    /**
     * These values are matched in string arrays -- changes must be kept in sync
     */
	public static final int SECURITY_NONE = 0;
	public static final int SECURITY_WEP = 1;
	public static final int SECURITY_PSK = 2;
	public static final int SECURITY_EAP = 3;
}
