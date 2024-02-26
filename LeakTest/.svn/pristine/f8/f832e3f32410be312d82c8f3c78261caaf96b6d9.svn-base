package com.cnlaunch.physics.wifi.custom;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.text.TextUtils;
import android.util.Log;
import com.cnlaunch.physics.utils.MLog;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class OldSecondWiFiManager implements ISecondWiFiManager {
    private static final String TAG = "OldSecondWiFiManager";
    private WifiManager wifiManager;
    private ConnectivityManager cm;
    boolean isExistsMethod_ConnectivityManager_SetIpRuleAdd;
    boolean isExistsMethod_ConnectivityManager_SetIpRouteAdd;
    boolean isExistsMethod_ConnectivityManager_DeleteIpRule;
    boolean isExistsMethod_WifiManager_doCustomSupplicantCommandRlt;
    boolean isExistsMethod_WifiManager_setWifiEnabledRlt;
    boolean isExistsMethod_WifiManager_getWifiEnabledRlt;

    Method method_ConnectivityManager_SetIpRuleAdd;
    Method method_ConnectivityManager_SetIpRouteAdd;
    Method method_ConnectivityManager_DeleteIpRule;
    Method method_WifiManager_doCustomSupplicantCommandRlt;
    Method method_WifiManager_setWifiEnabledRlt;
    Method method_WifiManager_getWifiEnabledRlt;
    private Context mContext;
    public OldSecondWiFiManager(Context context){
        mContext = context;
        wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        method_ConnectivityManager_SetIpRuleAdd = checkClassMethodExists("android.net.ConnectivityManager", "SetIpRuleAdd");
        method_ConnectivityManager_SetIpRouteAdd = checkClassMethodExists("android.net.ConnectivityManager", "SetIpRouteAdd");
        method_ConnectivityManager_DeleteIpRule = checkClassMethodExists("android.net.ConnectivityManager", "DeleteIpRule");
        method_WifiManager_doCustomSupplicantCommandRlt = checkClassMethodExists("android.net.wifi.WifiManager", "doCustomSupplicantCommandRlt");
        method_WifiManager_setWifiEnabledRlt = checkClassMethodExists("android.net.wifi.WifiManager", "setWifiEnabledRlt");
        method_WifiManager_getWifiEnabledRlt = checkClassMethodExists("android.net.wifi.WifiManager", "getWifiEnabledRlt");

        isExistsMethod_ConnectivityManager_SetIpRuleAdd = (method_ConnectivityManager_SetIpRuleAdd == null) ? false : true;
        isExistsMethod_ConnectivityManager_SetIpRouteAdd = (method_ConnectivityManager_SetIpRouteAdd == null) ? false : true;
        isExistsMethod_ConnectivityManager_DeleteIpRule = (method_ConnectivityManager_DeleteIpRule == null) ? false : true;
        isExistsMethod_WifiManager_doCustomSupplicantCommandRlt = (method_WifiManager_doCustomSupplicantCommandRlt == null) ? false : true;
        isExistsMethod_WifiManager_setWifiEnabledRlt = (method_WifiManager_setWifiEnabledRlt == null) ? false : true;
        isExistsMethod_WifiManager_getWifiEnabledRlt = (method_WifiManager_getWifiEnabledRlt == null) ? false : true;
        Log.d(TAG, ",isExistsMethod_ConnectivityManager_SetIpRuleAdd=" + isExistsMethod_ConnectivityManager_SetIpRuleAdd +
                ",isExistsMethod_ConnectivityManager_SetIpRouteAdd=" + isExistsMethod_ConnectivityManager_SetIpRouteAdd +
                ",isExistsMethod_ConnectivityManager_DeleteIpRule=" + isExistsMethod_ConnectivityManager_DeleteIpRule +
                ",isExistsMethod_WifiManager_doCustomSupplicantCommandRlt=" + isExistsMethod_WifiManager_doCustomSupplicantCommandRlt +
                ",isExistsMethod_WifiManager_setWifiEnabledRlt=" + isExistsMethod_WifiManager_setWifiEnabledRlt +
                ",isExistsMethod_WifiManager_getWifiEnabledRlt=" + isExistsMethod_WifiManager_getWifiEnabledRlt);
    }
    private String doCustomSupplicantCommandRltInvoke(Object invokeObject,String command){
        String returnValue=null;
        if (!isExistsMethod_WifiManager_doCustomSupplicantCommandRlt) {
            returnValue =  "";
        }
        else{
            try {
                returnValue = (String)method_WifiManager_doCustomSupplicantCommandRlt.invoke(invokeObject,command);
                if( MLog.isDebug) {
                    MLog.d(TAG, "doCustomSupplicantCommandRltInvoke returnValue="+returnValue);
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        return returnValue;
    }
    private Method checkClassMethodExists(String className, String methodName) {
        Method destMethod= null;
        try {
            Log.e(TAG, " checkClassMethodExists " + className + "   " + methodName);
            Method[] methods = null;
            methods = Class.forName(className).getDeclaredMethods();
            for (int i = 0; i < methods.length; i++) {
                Log.e(TAG, className + " method= " + methods[i].getName());
                if (methods[i].getName().equals(methodName)) {
                    Log.e(TAG, className + " find method= " + methods[i].getName());
                    destMethod= methods[i];
                    break;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "checkClassMethodExists error: " + e.toString());
            destMethod=null;
        }
        return destMethod;
    }
    /**
     * 查询wifi 开关
     */
    @Override
    public boolean isEnabled() {
        boolean isEnabled = false;
        if (!isExistsMethod_WifiManager_getWifiEnabledRlt) {
            return false;
        }
        try {
            isEnabled = (boolean) method_WifiManager_getWifiEnabledRlt.invoke(wifiManager);
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
        if (!isExistsMethod_WifiManager_setWifiEnabledRlt) {
            return false;
        }
        if( MLog.isDebug) {
            MLog.d(TAG, "wifiManager.setWifiEnabledRlt("+enabled+") start");
        }
        try {
            if (enabled) {
                isSuccess=(boolean)method_WifiManager_setWifiEnabledRlt.invoke(wifiManager, true);
                return isSuccess;
            } else {
                isSuccess=(boolean)method_WifiManager_setWifiEnabledRlt.invoke(wifiManager, false);
                if(isSuccess) {
                    deleteIpRule();
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
        if (!isExistsMethod_WifiManager_doCustomSupplicantCommandRlt) {
            return isSuccess;
        }
        try {
            if( MLog.isDebug) {
                MLog.d(TAG, "SCAN TYPE=ONLY start");
            }
            String returnValue = doCustomSupplicantCommandRltInvoke(wifiManager, "SCAN TYPE=ONLY");
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
        if (!isExistsMethod_WifiManager_doCustomSupplicantCommandRlt) {
            return new ArrayList<AccessPointCustomInterface>();
        }
        try {
            if( MLog.isDebug) {
                MLog.d(TAG, "SCAN_RESULTS start");
            }
            String scanResults = doCustomSupplicantCommandRltInvoke(wifiManager, "SCAN_RESULTS");;
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
        if (!isExistsMethod_WifiManager_doCustomSupplicantCommandRlt) {
            return false;
        }
        try {
            String r;
            String networkID;
            String newNetworkID = "";
            newNetworkID = doCustomSupplicantCommandRltInvoke(wifiManager, "ADD_NETWORK");
            if (TextUtils.isEmpty(newNetworkID) || newNetworkID.equals("null")) {
                return false;
            }
            networkID = newNetworkID;
            if (MLog.isDebug) {
                MLog.d(TAG, "ADD_NETWORK " + networkID);
            }

            r = doCustomSupplicantCommandRltInvoke(wifiManager, "SET_NETWORK " + networkID + " ssid \"" + ssid + "\"");
            if (MLog.isDebug) {
                MLog.d(TAG, "SET_NETWORK " + networkID + " ssid \"" + ssid + "\"" + r);
            }
            if (!SecondWiFiUtil.convertBooleanForCommandResult(r)) {
                return false;
            }

            r = doCustomSupplicantCommandRltInvoke(wifiManager, "SET_NETWORK " + networkID + " key_mgmt WPA-PSK");
            if (MLog.isDebug) {
                MLog.d(TAG, "SET_NETWORK " + networkID + " key_mgmt WPA-PSK " + r);
            }
            if (!SecondWiFiUtil.convertBooleanForCommandResult(r)) {
                return false;
            }

            r = doCustomSupplicantCommandRltInvoke(wifiManager, "SET_NETWORK " + networkID + " psk \"" + password + "\"");
            if (MLog.isDebug) {
                MLog.d(TAG, "SET_NETWORK " + networkID + " psk \"" + password + "\"" + r);
            }
            if (!SecondWiFiUtil.convertBooleanForCommandResult(r)) {
                return false;
            }

            r = doCustomSupplicantCommandRltInvoke(wifiManager, "SET_NETWORK " + networkID + " scan_ssid 1");
            if (MLog.isDebug) {
                MLog.d(TAG, "SET_NETWORK " + networkID + " scan_ssid 1" + r);
            }
            if (!SecondWiFiUtil.convertBooleanForCommandResult(r)) {
                return false;
            }

            r = doCustomSupplicantCommandRltInvoke(wifiManager, "SELECT_NETWORK " + networkID);
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
        if (!isExistsMethod_WifiManager_doCustomSupplicantCommandRlt) {
            return false;
        }
        try {
            if (MLog.isDebug) {
                MLog.d(TAG, "DHCP start");
            }
            String r = "";
            r = doCustomSupplicantCommandRltInvoke(wifiManager, "DHCP");
            if (MLog.isDebug) {
                MLog.d(TAG, "DHCP " + r);
            }
            if (TextUtils.isEmpty(r) || r.equals("null")) {
                isSuccess = false;
            } else {
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
        if (!isExistsMethod_WifiManager_doCustomSupplicantCommandRlt) {
            return false;
        }
        try {
            if (MLog.isDebug) {
                MLog.d(TAG, "STATIC start");
            }
            String r = "";
            r = doCustomSupplicantCommandRltInvoke(wifiManager, "STATIC 192.168.100.144");

            if (MLog.isDebug) {
                MLog.d(TAG, "STATIC " + r);
            }
            if (TextUtils.isEmpty(r) || r.equals("null")) {
                isSuccess = false;
            } else {
                isSuccess = true;
            }
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
        if (!isExistsMethod_WifiManager_doCustomSupplicantCommandRlt) {
            return wifiState;
        }
        try {
            if (MLog.isDebug) {
                MLog.d(TAG, "STATUS start");
            }
            String status = "";
            status = doCustomSupplicantCommandRltInvoke(wifiManager, "STATUS");

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
        if (!isExistsMethod_ConnectivityManager_SetIpRuleAdd) {
            return isSuccess;
        }
        try {
            if (MLog.isDebug) {
                MLog.d(TAG, "setIpRule start");
            }
            //cm.SetIpRuleAdd("192.168.100.0/24", "192.168.100.1");
            //cm.SetIpRouteAdd("192.168.100.1");
            if (TextUtils.isEmpty(routeRage) || TextUtils.isEmpty(gateway)) {
                method_ConnectivityManager_SetIpRuleAdd.invoke(cm, "192.168.100.0/24", "192.168.100.1");
                method_ConnectivityManager_SetIpRouteAdd.invoke(cm, "192.168.100.1");

                if (MLog.isDebug) {
                    MLog.d(TAG, "SetIpRuleAdd 192.168.100.0/24 ，192.168.100.1 ");
                }
                if (MLog.isDebug) {
                    MLog.d(TAG, "SetIpRouteAdd 192.168.100.1");
                }
            } else {
                method_ConnectivityManager_SetIpRuleAdd.invoke(cm, routeRage, gateway);
                method_ConnectivityManager_SetIpRouteAdd.invoke(cm, gateway);
                if (MLog.isDebug) {
                    MLog.d(TAG, "SetIpRuleAdd  " + routeRage + "," + gateway);
                }
                if (MLog.isDebug) {
                    MLog.d(TAG, "SetIpRouteAdd " + gateway);
                }
            }
            isSuccess = true;
        } catch (Exception e) {
            e.printStackTrace();
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
        if (MLog.isDebug) {
            MLog.d(TAG, " deleteIpRule ");
        }
        if (!isExistsMethod_ConnectivityManager_DeleteIpRule) {
            return false;
        }
        try {
            method_ConnectivityManager_DeleteIpRule.invoke(cm);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }
}
