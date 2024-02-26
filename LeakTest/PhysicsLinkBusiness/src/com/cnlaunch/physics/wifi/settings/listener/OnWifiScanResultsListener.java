package com.cnlaunch.physics.wifi.settings.listener;



import java.util.List;
import com.cnlaunch.physics.wifi.settings.AccessPointCustom;

/**
 * Created by xiefeihong on 2017/10/14.
 * WIFI扫描结果的回调接口
 */
public interface OnWifiScanResultsListener {

    /**
     * 扫描结果的回调
     *
     * @param scanResults 扫描结果
     */
    void onScanResults(List<AccessPointCustom> accessPointCustoms);
}
