package com.cnlaunch.physics.wifi.settings;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.text.TextUtils;

import com.cnlaunch.physics.utils.MLog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by xiefeihong on 2017/09/29.
 * BaseWiFiManager
 */
public class BaseWiFiManager {
    protected static String TAG=BaseWiFiManager.class.getSimpleName();
	protected  WifiManager mWifiManager;
    private static ConnectivityManager mConnectivityManager;
    protected Context mContext;
    protected NetworkInfo mLastNetworkInfo;
    protected WifiInfo mLastInfo;
    BaseWiFiManager(Context context) {
        // 取得WifiManager对象
        mWifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        mContext = context;
        mLastNetworkInfo = null;
        mLastInfo = null;
    }

    /**
     * 添加开放网络配置
     *
     * @param ssid SSID
     * @return NetworkId
     */
    public int setOpenNetwork(String ssid) {
        if (TextUtils.isEmpty(ssid)) {
            return -1;
        }
        WifiConfiguration wifiConfiguration = getConfigFromConfiguredNetworksBySsid(ssid);
        if (null == wifiConfiguration) {
            // 生成配置
            WifiConfiguration wifiConfig = new WifiConfiguration();
            wifiConfig.SSID = addDoubleQuotation(ssid);
            wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
            wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            wifiConfig.allowedAuthAlgorithms.clear();
            wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
            wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            // 添加配置并返回NetworkID
            return addNetwork(wifiConfig);
        } else {
            // 返回NetworkID
            return wifiConfiguration.networkId;
        }
    }

    /**
     * 添加WEP网络配置
     *
     * @param ssid     SSID
     * @param password 密码
     * @return NetworkId
     */
    public int setWEPNetwork(String ssid, String password) {
        if (TextUtils.isEmpty(ssid) || TextUtils.isEmpty(password)) {
            return -1;
        }
        WifiConfiguration wifiConfiguration = getConfigFromConfiguredNetworksBySsid(ssid);
        if (null == wifiConfiguration) {
            // 添加配置
            WifiConfiguration wifiConfig = new WifiConfiguration();
            wifiConfig.SSID = addDoubleQuotation(ssid);
            wifiConfig.wepKeys[0] = "\"" + password + "\"";
            wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
            wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            wifiConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            wifiConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
            wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
            // 添加配置并返回NetworkID
            return addNetwork(wifiConfig);
        } else {
            // 更新配置并返回NetworkID
            wifiConfiguration.wepKeys[0] = "\"" + password + "\"";
            return updateNetwork(wifiConfiguration);
        }
    }

    /**
     * 添加WPA网络配置
     *
     * @param ssid     SSID
     * @param password 密码
     * @return NetworkId
     */
    public int setWPA2Network(String ssid, String password) {
        if (TextUtils.isEmpty(ssid) || TextUtils.isEmpty(password)) {
            return -1;
        }
        WifiConfiguration wifiConfiguration = getConfigFromConfiguredNetworksBySsid(ssid);
        if (null == wifiConfiguration) {
            WifiConfiguration wifiConfig = new WifiConfiguration();
            wifiConfig.SSID = addDoubleQuotation(ssid);
            wifiConfig.preSharedKey = "\"" + password + "\"";
            wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
            wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            wifiConfig.status = WifiConfiguration.Status.ENABLED;
            wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
            wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            // 添加配置并返回NetworkID
            return addNetwork(wifiConfig);
        } else {
            // 更新配置并返回NetworkID
            wifiConfiguration.preSharedKey = "\"" + password + "\"";
            return updateNetwork(wifiConfiguration);
        }
    }

    /**
     * 通过热点名获取热点配置
     *
     * @param ssid 热点名
     * @return 配置信息
     */
    public WifiConfiguration getConfigFromConfiguredNetworksBySsid(String ssid) {
        ssid = addDoubleQuotation(ssid);
        List<WifiConfiguration> existingConfigs = getConfiguredNetworks();
        if (null != existingConfigs) {
            for (WifiConfiguration existingConfig : existingConfigs) {
                if (existingConfig.SSID.equals(ssid)) {
                    return existingConfig;
                }
            }
        }
        return null;
    }


    /**
     * 获取WIFI的开关状态
     *
     * @return WIFI的可用状态
     */
    public boolean isWifiEnabled() {
        return null != mWifiManager && mWifiManager.isWifiEnabled();
    }

    /**
     * 控制WIFI的开关状态
     * @param enabled
     */
    public void setWifiEnabled(boolean enabled) {
        if (mWifiManager != null) {
            mWifiManager.setWifiEnabled(enabled);
        }
    }
    /**
     * 判断WIFI是否连接
     *
     * @return 是否连接
     */
    public boolean isWifiConnected() {
        //在4g,wifi可以同时运行的系统用于判断wifi是否接入热点此方法更加正确
        WifiInfo wifiInfo = getConnectionInfo();
        if(MLog.isDebug) {
            MLog.d(TAG, " WifiInfo ConnectionInfo " + wifiInfo);
        }
        if (mWifiManager != null && mWifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED
                && wifiInfo != null  && wifiInfo.getIpAddress()!=0) {
            return true;
        }
        return false;
        /*if (null != mConnectivityManager) {
            NetworkInfo networkInfo = mConnectivityManager.getActiveNetworkInfo();
            return null != networkInfo && networkInfo.isConnected() && networkInfo.getType() == ConnectivityManager.TYPE_WIFI;
        }*/
    }
    public boolean isWifiConnectedWithMatchedSSID(String matchedSSID) {
        if(isWifiConnected()){
            WifiInfo wifiInfo = getConnectionInfo();
            if (null != wifiInfo) {
                String ssid =wifiInfo.getSSID();
                if(TextUtils.isEmpty(ssid)==false && ssid.contains(matchedSSID)){
                    return true;
                }
            }
        }
        return  false;
    }

    /**
     * 判断设备是否有网
     *
     * @return 网络状态
     */
    boolean hasNetwork() {
        if (null != mConnectivityManager) {
            NetworkInfo networkInfo = mConnectivityManager.getActiveNetworkInfo();
            return networkInfo != null && networkInfo.isAvailable();
        }
        return false;
    }

    /**
     * 获取当前正在连接的WIFI信息
     *
     * @return 当前正在连接的WIFI信息
     */
    public WifiInfo getConnectionInfo() {
        if (null != mWifiManager) {
            return mWifiManager.getConnectionInfo();
        }
        return null;
    }

    /**
     * 扫描附近的WIFI
     */
    public void startScan() {
        if (null != mWifiManager) {
            mWifiManager.startScan();
        }
    }

    

    /**
     * 排除重复
     *
     * @param scanResults 带处理的数据
     * @return 去重数据
     */
    public static ArrayList<ScanResult> excludeRepetition(List<ScanResult> scanResults) {
        HashMap<String, ScanResult> hashMap = new HashMap<String, ScanResult>();
        for (ScanResult scanResult : scanResults) {
            String ssid = scanResult.SSID;
            if (TextUtils.isEmpty(ssid)) {
                continue;
            }
            ScanResult tempResult = hashMap.get(ssid);
            if (null == tempResult) {
                hashMap.put(ssid, scanResult);
                continue;
            }
            if (WifiManager.calculateSignalLevel(tempResult.level, 100) < WifiManager.calculateSignalLevel(scanResult.level, 100)) {
                hashMap.put(ssid, scanResult);
            }
        }
        ArrayList<ScanResult> results = new ArrayList<ScanResult>();
        for (Map.Entry<String, ScanResult> entry : hashMap.entrySet()) {
            results.add(entry.getValue());
        }
        return results;
    }

    /**
     * 获取配置过的WIFI信息
     *
     * @return 配置信息
     */
    private List<WifiConfiguration> getConfiguredNetworks() {
        if (null != mWifiManager) {
            return mWifiManager.getConfiguredNetworks();
        }
        return null;
    }

    /**
     * 保持配置
     *
     * @return 保持是否成功
     */
    boolean saveConfiguration() {
        return null != mWifiManager && mWifiManager.saveConfiguration();
    }

    /**
     * 连接到网络
     *
     * @param networkId NetworkId
     * @return 连接结果
     */
    boolean enableNetwork(int networkId) {
        if (null != mWifiManager) {
            boolean isDisconnect = disconnectCurrentWifi();
            boolean isEnableNetwork = mWifiManager.enableNetwork(networkId, true);
            boolean isSave = true;
            // 26 android 8.0 以后的产品无需调用该方法
            if(Build.VERSION.SDK_INT<26) {
                isSave =mWifiManager.saveConfiguration();
            }
            boolean isReconnect = mWifiManager.reconnect();
            return isDisconnect && isEnableNetwork && isSave && isReconnect;
        }
        return false;
    }


    /**
     * 断开指定 WIFI
     *
     * @param netId netId
     * @return 是否断开
     */
    public boolean disconnectWifi(int netId) {
        if (null != mWifiManager) {
            boolean isDisable = mWifiManager.disableNetwork(netId);
            boolean isDisconnect = mWifiManager.disconnect();
            return isDisable && isDisconnect;
        }
        return false;
    }

    /**
     * 断开当前的WIFI
     *
     * @return 是否断开成功
     */
    public boolean disconnectCurrentWifi() {
        WifiInfo wifiInfo = getConnectionInfo();
        if (null != wifiInfo) {
            int networkId = wifiInfo.getNetworkId();
            return disconnectWifi(networkId);
        } else {
            // 断开状态
            return true;
        }
    }

    /**
     * 删除配置
     *
     * @param netId netId
     * @return 是否删除成功
     */
    public boolean deleteConfig(int netId) {
        if (null != mWifiManager) {
            boolean isDisable = mWifiManager.disableNetwork(netId);
            boolean isRemove = mWifiManager.removeNetwork(netId);
            boolean isSave = true;
            // 26 android 8.0 以后的产品无需调用该方法
            if(Build.VERSION.SDK_INT<26) {
                isSave = mWifiManager.saveConfiguration();
            }
            return isDisable && isRemove && isSave;
        }
        return false;
    }

    /**
     * 计算WIFI信号强度
     *
     * @param rssi WIFI信号
     * @return 强度
     */
    public int calculateSignalLevel(int rssi) {
        return WifiManager.calculateSignalLevel(rssi, 5);
    }

    /**
     * 获取WIFI的加密方式
     *
     * @param scanResult WIFI信息
     * @return 加密方式
     */
    public int getSecurityMode(ScanResult scanResult) {
        String capabilities = scanResult.capabilities;
        if (capabilities.contains("WPA")) {
            return SecurityMode.SECURITY_PSK;
        } else if (capabilities.contains("WEP")) {
            return SecurityMode.SECURITY_WEP;
        } else {
            // 没有加密
            return SecurityMode.SECURITY_NONE;
        }
    }

    /**
     * 添加双引号
     *
     * @param text 待处理字符串
     * @return 处理后字符串
     */
    public String addDoubleQuotation(String text) {
        if (TextUtils.isEmpty(text)) {
            return "";
        }
        return "\"" + text + "\"";
    }
    /**
     * 添加网络配置
     *
     * @param wifiConfig 配置信息
     * @return NetworkId
     */
    private int addNetwork(WifiConfiguration wifiConfig) {
        if (null != mWifiManager) {
            int networkId = mWifiManager.addNetwork(wifiConfig);
            // 26 android 8.0 以后的产品无需调用该方法
            if(Build.VERSION.SDK_INT<26) {
                if (-1 != networkId) {
                    boolean isSave = mWifiManager.saveConfiguration();
                    if (isSave) {
                        return networkId;
                    }
                }
            }
            else{
                return networkId;
            }
        }
        return -1;
    }

    /**
     * 更新网络配置
     *
     * @param wifiConfig 配置信息
     * @return NetworkId
     */
    private int updateNetwork(WifiConfiguration wifiConfig) {
        if (null != mWifiManager) {
            int networkId = mWifiManager.updateNetwork(wifiConfig);
            if (-1 != networkId) {
                // 26 android 8.0 以后的产品无需调用该方法
                if(Build.VERSION.SDK_INT<26) {
                    boolean isSave = mWifiManager.saveConfiguration();
                    if (isSave) {
                        return networkId;
                    }
                }
                else{
                    return networkId;
                }
            }
        }
        return -1;
    }
}
