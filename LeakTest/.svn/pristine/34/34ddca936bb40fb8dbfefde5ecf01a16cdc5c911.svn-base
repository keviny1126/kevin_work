package com.cnlaunch.physics.utils;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;

import com.power.baseproject.utils.SystemPropertiesInvoke;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;


public class NetworkUtil {
    private static final String TAG = NetworkUtil.class.getSimpleName();

    public static enum PskType {
        UNKNOWN,
        WPA,
        WPA2,
        WPA_WPA2
    }

    public static final int SECURITY_NONE = 0;
    public static final int SECURITY_WEP = 1;
    public static final int SECURITY_PSK = 2;
    public static final int SECURITY_EAP = 3;


    private Context context;
    private static NetworkUtil mInstance;

    public static NetworkUtil getInstance(Context context) {
        if (null == mInstance) {
            mInstance = new NetworkUtil(context);
        }
        return mInstance;
    }

    private NetworkUtil(Context context) {
        this.context = context;
    }

    public boolean getConnectWIFI() {
        return getConnectWIFI(context);
    }

    public static boolean getConnectWIFI(Context context) {
        if (context != null) {
			/*ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
			if (mNetworkInfo != null && mNetworkInfo.isAvailable() && mNetworkInfo.isConnected()) {
				if (mNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
					return true;
				}
			}*/
            //在4g,wifi可以同时运行的系统用于判断wifi是否接入热点此方法更加正确
            WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = null;
            if (null != wifiManager) {
                wifiInfo = wifiManager.getConnectionInfo();
            }
            if (MLog.isDebug) {
                MLog.d(TAG, " WifiInfo ConnectionInfo " + wifiInfo);
            }
            if (wifiManager != null && wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED
                    && wifiInfo != null && wifiInfo.getIpAddress() != 0) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * 获取局域网广播地址
     *
     * @return
     */
    public static InetAddress getBroadcastAddress(Context context) throws UnknownHostException {
        if (isWifiApEnabled(context)) {
            return InetAddress.getByName("192.168.43.255");
        }
        WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        DhcpInfo dhcp = wifi.getDhcpInfo();
        MLog.d("NetworkUtil", "wifi.getDhcpInfo()" + dhcp.toString());
        if (dhcp == null) {
            return InetAddress.getByName("255.255.255.255");
        }
        int netmask = 0;
        if (dhcp.netmask == 0) {
            String netmasks = null;
            String deviceType = SystemPropertiesInvoke.getString("cnlaunch.product.type");
            if (TextUtils.isEmpty(deviceType) == false) {
                netmasks = SystemPropertiesInvoke.getString("dhcp.wlan0.mask");
                if (MLog.isDebug)
                    MLog.d("NetworkUtil", "dhcp.wlan0.mask netmasks = " + netmasks);
            } else {
                netmasks = getNetMarsk();
                if (MLog.isDebug)
                    MLog.d("NetworkUtil", "android 6.0 or 7.0 netmasks = " + netmasks);
            }
            byte[] ipBytes = ipToBytes(netmasks);
            if (MLog.isDebug)
                MLog.d("NetworkUtil", String.format("dhcp. mask netmasks = %d.%d.%d.%d", (ipBytes[0] & 0xff), ipBytes[1] & 0xff, ipBytes[2] & 0xff, ipBytes[3] & 0xff));
            if (ipBytes != null) {
                netmask = bytesToInt(ipBytes);
            }
        } else {
            netmask = dhcp.netmask;
        }
        if (MLog.isDebug)
            MLog.d("NetworkUtil", String.format("dhcp.ipAddress int = %d,netmasks int = %d", dhcp.ipAddress, netmask));
        int broadcast = (dhcp.ipAddress & netmask) | ~netmask;
        byte[] quads = new byte[4];
        for (int k = 0; k < 4; k++) {
            quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
        }
        return InetAddress.getByAddress(quads);
    }


    /**
     * 把IP地址转化为int
     *
     * @param ipAddr
     * @return int
     */
    public static byte[] ipToBytes(String ipAddr) {
        byte[] ret = new byte[4];
        try {
            String[] ipArr = ipAddr.split("\\.");
            if (ipArr == null || ipArr.length != 4) {
                return null;
            }
            ret[0] = (byte) (Integer.parseInt(ipArr[0]) & 0xFF);
            ret[1] = (byte) (Integer.parseInt(ipArr[1]) & 0xFF);
            ret[2] = (byte) (Integer.parseInt(ipArr[2]) & 0xFF);
            ret[3] = (byte) (Integer.parseInt(ipArr[3]) & 0xFF);
            return ret;
        } catch (Exception e) {
            MLog.d("NetworkUtil", ipAddr + " is invalid IP");
            return null;
        }
    }

    /**
     * 根据位运算把 byte[] -> int
     * 高字节在前，低字节在后
     *
     * @param bytes
     * @return int
     */
    public static int bytesToInt(byte[] bytes) {
        int addr = bytes[0] & 0xFF;
        addr |= ((bytes[1] & 0xFF) << 8);
        addr |= ((bytes[2] & 0xFF) << 16);
        addr |= ((bytes[3] & 0xFF) << 24);
        return addr;
    }


    /**
     * 获取子网掩码 解决android 6.0以上系统存在原生bug
     *
     * @return
     */
    private static String getNetMarsk() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                if (networkInterface.getDisplayName().contains("wlan")) {
                    List<InterfaceAddress> ncAddrList = networkInterface.getInterfaceAddresses();
                    for (InterfaceAddress interfaceAddress : ncAddrList) {
                        InetAddress address = interfaceAddress.getAddress();
                        if (address instanceof Inet4Address) {
                            return calcMaskByPrefixLength(interfaceAddress.getNetworkPrefixLength());
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public static String calcMaskByPrefixLength(int paramInt) {
        int mask = -1 << (32 - paramInt);
        int partsNum = 4;
        int bitsOfPart = 8;
        int maskParts[] = new int[partsNum];
        int selector = 0x000000ff;
        for (int i = 0; i < maskParts.length; i++) {
            int pos = maskParts.length - 1 - i;
            maskParts[pos] = (mask >> (i * bitsOfPart)) & selector;
        }
        String result = "";
        result = result + maskParts[0];
        for (int i = 1; i < maskParts.length; i++) {
            result = result + "." + maskParts[i];
        }
        return result;
    }

    /**
     * 是否为wifi热点
     *
     * @return
     */
    public static Boolean isWifiApEnabled(Context context) {
        try {
            WifiManager manager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            Method method = manager.getClass().getMethod("isWifiApEnabled");
            return (Boolean) method.invoke(manager);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 开热点手机获得其他连接手机IP的方法
     *
     * @return 其他手机IP 数组列表
     */
    public static ArrayList<String> getConnectedIP() {
        ArrayList<String> connectedIp = new ArrayList<String>();
        try {
            BufferedReader br = new BufferedReader(new FileReader("/proc/net/arp"));
            String line;
            while ((line = br.readLine()) != null) {
                String[] splitted = line.split(" +");
                if (splitted != null && splitted.length >= 4) {
                    String ip = splitted[0];
                    if (!ip.equalsIgnoreCase("ip")) {
                        connectedIp.add(ip);
                    }
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return connectedIp;
    }

    public static int getWiFiAccessPointSecurity(WifiConfiguration config) {
        if (config.allowedKeyManagement.get(KeyMgmt.WPA_PSK)) {
            return SECURITY_PSK;
        }
        if (config.allowedKeyManagement.get(KeyMgmt.WPA_EAP) ||
                config.allowedKeyManagement.get(KeyMgmt.IEEE8021X)) {
            return SECURITY_EAP;
        }
        return (config.wepKeys[0] != null) ? SECURITY_WEP : SECURITY_NONE;
    }


    public static String getWiFiAccessPointPassword(WifiConfiguration config) {
        return getWiFiAccessPointPassword(config, -1);
    }

    public static String getWiFiAccessPointPassword(WifiConfiguration config, int currentSecurityType) {
        String password = "";
        int securityType = -1;
        if (currentSecurityType == -1) {
            securityType = NetworkUtil.getWiFiAccessPointSecurity(config);
        } else {
            securityType = currentSecurityType;
        }
        switch (securityType) {
            case NetworkUtil.SECURITY_NONE:
                break;
            case NetworkUtil.SECURITY_WEP:
                password = config.wepKeys[0];
                break;
            case NetworkUtil.SECURITY_PSK:
                password = config.preSharedKey;
                break;
            case NetworkUtil.SECURITY_EAP:
                break;
        }
        return password;
    }

    public static int getWiFiAccessPointSecurity(ScanResult result) {
        if (result.capabilities.contains("WEP")) {
            return SECURITY_WEP;
        } else if (result.capabilities.contains("PSK")) {
            return SECURITY_PSK;
        } else if (result.capabilities.contains("EAP")) {
            return SECURITY_EAP;
        }
        return SECURITY_NONE;
    }

    public static PskType getWiFiAccessPointSecurityPskType(ScanResult result) {
        boolean wpa = result.capabilities.contains("WPA-PSK");
        boolean wpa2 = result.capabilities.contains("WPA2-PSK");
        if (wpa2 && wpa) {
            return PskType.WPA_WPA2;
        } else if (wpa2) {
            return PskType.WPA2;
        } else if (wpa) {
            return PskType.WPA;
        } else {
            return PskType.UNKNOWN;
        }
    }

    public static String getConnectedAPIP(Context context) {
        try {
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            int ip = wifiInfo.getIpAddress();
            if (ip != 0) {
                return intToIp(ip);
            } else {
                return "";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private static String intToIp(int paramInt) {
        return (paramInt & 0xFF) + "." + (0xFF & paramInt >> 8) + "." + (0xFF & paramInt >> 16) + "."
                + (0xFF & paramInt >> 24);
    }
}
