package com.cnlaunch.physics.wifi.settings;

import android.content.Context;
import android.util.Log;

import com.cnlaunch.physics.utils.MLog;

/**
 * Created by xiefeihong on 2019/4/9.
 * Wifi管理，用于特定目的WIFI控制
 */

public class WiFiControlManager extends BaseWiFiManager {
    private static WiFiControlManager mWiFiManager;
    private WiFiControlManager(Context context) {
        super(context);
    }
    public static WiFiControlManager getInstance(Context context) {
        if (null == mWiFiManager) {
            synchronized (WiFiControlManager.class) {
                if (null == mWiFiManager) {
                    mWiFiManager = new WiFiControlManager(context);
                }
            }
        }
        return mWiFiManager;
    }
    /**
     * 连接到WPA2网络
     *
     * @param ssid     热点名
     * @return 配置是否成功
     */
    public boolean connectWPA2Network(String ssid, String password) {
        // 获取networkId
        int networkId = setWPA2Network(ssid, password);
        if(MLog.isDebug) {
            MLog.d(TAG, "networkId=" + networkId);
        }
        if (-1 != networkId) {
            // 保存配置
            //设置网络连接参数已经处理过保存网络配置，所以无需再执行该方法
            //boolean isSave = saveConfiguration();
            // 连接网络
            boolean isEnable = enableNetwork(networkId);
            return  isEnable;
        }
        return false;
    }
}
