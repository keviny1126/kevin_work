
/**
 * isActive，isSave，rssi
 isActive 为真表示当前连接的活跃的wifi接入点
 rssi     数值表示信号强度，值为Integer.MAX_VALUE 表示不在范围内
 isSave   为真表示已经配置过的网络。wifi驱动程序确定
 根据wifi接入点信息，逻辑如下
 if (isActive) {
 支持"修改网络"，"取消保存网络"操作
 } else if (rssi != Integer.MAX_VALUE) {
 if (isSave) {
 支持"连接网络", "修改网络", "取消保存网络"操作
 } else {
 支持"连接网络"操作
 }
 } else {
 支持"修改网络"，"取消保存网络"操作
 }
 */
package com.cnlaunch.physics.wifi.custom;

/**
 * wifi热点实体类
 */
public class AccessPointCustomInterface  {
    static final String TAG = "AccessPointCustomInterface";
    public String summary;
    public String ssid;
    public String bssid;/**
     * These values are matched in string arrays -- changes must be kept in sync
     * SECURITY_NONE = 0;SECURITY_WEP = 1;SECURITY_PSK = 2;SECURITY_EAP = 3;
     */
    public int security;
    public int rssi;
    public int pskType;
    public boolean isActive;
    public boolean isSave;
    public int networkId;
    public String mac;

    @Override
    public String toString() {
        return "AccessPointCustomInterface{" +
                "summary='" + summary + '\'' +
                ", ssid='" + ssid + '\'' +
                ", bssid='" + bssid + '\'' +
                ", security=" + security +
                ", rssi=" + rssi +
                ", pskType=" + pskType +
                ", isActive=" + isActive +
                ", isSave=" + isSave +
                ", networkId=" + networkId +
                ",  mac=" +  mac +
                '}';
    }
}
