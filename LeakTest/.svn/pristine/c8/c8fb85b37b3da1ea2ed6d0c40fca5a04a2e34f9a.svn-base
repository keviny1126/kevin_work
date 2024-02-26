package com.cnlaunch.physics.wifi.custom;

import android.content.Context;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import com.cnlaunch.physics.utils.MLog;
import com.cnlaunch.second.wifi.SecondWifiManager;

import java.util.ArrayList;
import java.util.List;

public class TrebleSecondWiFiManager implements ISecondWiFiManager {
    private static final String TAG = "TrebleSecondWiFiManager";
    private SecondWifiManager mSecondWifiManager;
    private Context mContext;
    public TrebleSecondWiFiManager(Context context) {
        mContext = context;
        mSecondWifiManager = SecondWifiManager.getInstance();
        if (mSecondWifiManager != null) {
            try {
                Log.d(TAG, "set SecondWifiManager success version=" + mSecondWifiManager.getInternalReleaseVersionCode());
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }
    /**
     * 查询wifi 开关
     */
    @Override
    public boolean isEnabled() {
        boolean isEnabled = false;
        try {
            isEnabled = mSecondWifiManager.getWifiEnabled();
            if( MLog.isDebug){
                MLog.d(TAG," isEnabled() =" + isEnabled);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isEnabled;
    }

    /**
     * 打开或关闭wifi，需要在线程中执行 并考虑线程同步
     *
     * @param enabled
     * @return
     */
    @Override
    public synchronized boolean setWifiEnabled(boolean enabled) {
        boolean isSuccess=true;
        if( MLog.isDebug) {
            MLog.d(TAG, "setWifiEnabled("+enabled+") start");
        }
        try {
            if (enabled) {
                SecondWiFiUtil.setPowerOn(mContext);
                isSuccess=mSecondWifiManager.setWifiEnabled(true);
                return isSuccess;
            } else {
                isSuccess=mSecondWifiManager.setWifiEnabled(false);
                if(isSuccess) {
                    isSuccess=deleteIpRule();
                    SecondWiFiUtil.setPowerOff(mContext);
                }
                return isSuccess;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 执行热点扫描需要在线程中执行 wait 3 second get scanresult
     */
    @Override
    public boolean startScan() {
        boolean isSuccess = false;
        try {
            if( MLog.isDebug) {
                MLog.d(TAG, "SCAN TYPE=ONLY start");
            }
            String returnValue = mSecondWifiManager.doCustomSupplicantCommand("SCAN TYPE=ONLY");
            isSuccess = SecondWiFiUtil.convertBooleanForCommandResult(returnValue);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isSuccess;
    }



    /**
     * 获取扫描结果需要在线程中执行
     */
    @Override
    public List<AccessPointCustomInterface> getScanResult() {
        return  getScanResult(false);
    }
    @Override
    public List<AccessPointCustomInterface> getScanResult(boolean isDebug) {
        try {
            if( MLog.isDebug) {
                MLog.d(TAG, "SCAN_RESULTS start");
            }
            String scanResults = mSecondWifiManager.doCustomSupplicantCommand("SCAN_RESULTS");
            return SecondWiFiUtil.coverScanResult(scanResults,isDebug,this);

        } catch(Exception e) {
            e.printStackTrace();
            return new ArrayList<AccessPointCustomInterface>();
        }
    }

    /**
     * 增加或修改网络 需要在线程中执行
     */
    @Override
    public boolean addORUpdateNetwork(String ssid, String password) {
        try {
            String r;
            String networkID;
            String newNetworkID = "";
            newNetworkID = mSecondWifiManager.doCustomSupplicantCommand("ADD_NETWORK");
            if (TextUtils.isEmpty(newNetworkID) || newNetworkID.equals("null")) {
                return false;
            }
            networkID = newNetworkID;
            if (MLog.isDebug) {
                MLog.d(TAG, "ADD_NETWORK " + networkID);
            }

            r = mSecondWifiManager.doCustomSupplicantCommand("SET_NETWORK " + networkID + " ssid \"" + ssid + "\"");
            if (MLog.isDebug) {
                MLog.d(TAG, "SET_NETWORK " + networkID + " ssid \"" + ssid + "\"" + r);
            }
            if (!SecondWiFiUtil.convertBooleanForCommandResult(r)) {
                return false;
            }

            r = mSecondWifiManager.doCustomSupplicantCommand("SET_NETWORK " + networkID + " key_mgmt WPA-PSK");
            if (MLog.isDebug) {
                MLog.d(TAG, "SET_NETWORK " + networkID + " key_mgmt WPA-PSK " + r);
            }
            if (!SecondWiFiUtil.convertBooleanForCommandResult(r)) {
                return false;
            }

            r =mSecondWifiManager.doCustomSupplicantCommand("SET_NETWORK " + networkID + " psk \"" + password + "\"");
            if (MLog.isDebug) {
                MLog.d(TAG, "SET_NETWORK " + networkID + " psk \"" + password + "\"" + r);
            }
            if (!SecondWiFiUtil.convertBooleanForCommandResult(r)) {
                return false;
            }

            r = mSecondWifiManager.doCustomSupplicantCommand("SET_NETWORK " + networkID + " scan_ssid 1");
            if (MLog.isDebug) {
                MLog.d(TAG, "SET_NETWORK " + networkID + " scan_ssid 1" + r);
            }
            if (!SecondWiFiUtil.convertBooleanForCommandResult(r)) {
                return false;
            }

            r = mSecondWifiManager.doCustomSupplicantCommand("SELECT_NETWORK " + networkID);
            if (MLog.isDebug) {
                MLog.d(TAG, "SELECT_NETWORK " + r);
            }
            if (!SecondWiFiUtil.convertBooleanForCommandResult(r)) {
                return false;
            }
            return  true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * DHCP 获取ip和网关 需要在线程中执行
     */
    @Override
    public  boolean requestIPWithDHCP() {
        boolean isSuccess = false;
        try {
            if (MLog.isDebug) {
                MLog.d(TAG, "DHCP start");
            }
            String dhcpIp = mSecondWifiManager.requestIPWithDHCP();
            if (TextUtils.isEmpty(dhcpIp) || dhcpIp.equals("null")) {
                isSuccess = false;
            }
            else {
                isSuccess = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            isSuccess = false;
        }
        return isSuccess;
    }

    /**
     * 设置静态IP 需要在线程中执行 192.168.100.10-192.168.100.149区域ip可用
     */
    @Override
    public boolean setStaticIP() {
        boolean isSuccess = false;
        try {
            if (MLog.isDebug) {
                MLog.d(TAG, "STATIC start");
            }
            isSuccess = mSecondWifiManager.setStaticIP("192.168.100.144");
        } catch (Exception e) {
            e.printStackTrace();
            isSuccess = false;
        }
        return isSuccess;
    }

    /**
     * 查询wifi连接状态 需要在线程中执行
     * CONNECTED,CONNECTEDING,DISCONNECTED
     */
    @Override
    public CustomWiFiControlForDualWiFi.WiFiState getCurrentWiFiState(String ssid) {
        CustomWiFiControlForDualWiFi.WiFiState wifiState = new CustomWiFiControlForDualWiFi.WiFiState();
        wifiState.ssid = ssid;
        if (isEnabled() == false) {
            if (MLog.isDebug) {
                MLog.d(TAG, "getCurrentWiFiState state is WPAState.NONE isEnabled() =false");
            }
            return wifiState;
        }
        try {
            if (MLog.isDebug) {
                MLog.d(TAG, "STATUS start");
            }
            String status = "";
            status = mSecondWifiManager.getCurrentState("");

            if (MLog.isDebug) {
                MLog.d(TAG, "getCurrentWiFiState STATUS " + status);
            }
            if (TextUtils.isEmpty(status) || status.equals("null")) {
                wifiState.wpaState = CustomWiFiControlForDualWiFi.WPAState.NONE;
            } else {
                if (status.equalsIgnoreCase("CONNECTED")) {
                    wifiState.wpaState = CustomWiFiControlForDualWiFi.WPAState.CONNECTED;
                } else if (status.equalsIgnoreCase("CONNECTEDING")) {
                    wifiState.wpaState = CustomWiFiControlForDualWiFi.WPAState.CONNECTING;
                } else {
                    wifiState.wpaState = CustomWiFiControlForDualWiFi.WPAState.NONE;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return wifiState;
    }



    /**
     * 设置路由 需要延时5秒 需要在线程中执行
     */
    @Override
    public boolean setIpRule(final String routeRage,final String gateway) {
        boolean isSuccess = false;
        try {
            if (MLog.isDebug) {
                MLog.d(TAG, "setIpRule start");
            }
            String routeRage1, gateway1;
            if (TextUtils.isEmpty(routeRage) || TextUtils.isEmpty(gateway)) {
                routeRage1 = "192.168.100.0/24";
                gateway1 = "192.168.100.1";
            } else {
                routeRage1 = routeRage;
                gateway1 = gateway;
            }
            isSuccess = mSecondWifiManager.addIpRouteForIPV4(routeRage1, gateway1);
            if (!isSuccess) {
                return false;
            }
            isSuccess = mSecondWifiManager.addIpRule();
        } catch (Exception e) {
            e.printStackTrace();
            isSuccess = false;
        }
        return isSuccess;
    }
    /**
     * 设置路由 需要延时5秒 需要在线程中执行
     */
    @Override
    public  boolean setIpRule() {
        return setIpRule("","");
    }
    @Override
    public boolean deleteIpRule() {
        boolean isSuccess = true;
        if (MLog.isDebug) {
            MLog.d(TAG, " deleteIpRule ");
        }
        try {
            mSecondWifiManager.deleteIpRule();
        } catch (Exception e) {
            e.printStackTrace();
            isSuccess = false;
        }
        return isSuccess;
    }
}
