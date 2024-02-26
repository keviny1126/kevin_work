package com.cnlaunch.physics.wifi.settings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import com.cnlaunch.physics.utils.MLog;
import com.cnlaunch.physics.wifi.settings.listener.OnWifiEnabledListener;
import com.cnlaunch.physics.wifi.settings.listener.OnWifiScanResultsListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;


/**
 * Created by xiefeihong on 2017/09/29.
 * Wifi管理,用于完整的wifi控制
 */
public class WiFiManagerForRemote extends BaseWiFiManager {
    private static final String TAG = "WiFiManagerForRemote";
    private static WiFiManagerForRemote mWiFiManager;
    private boolean isCanScan;
    List<AccessPointCustom> mAccessPointCustoms;
    private final IntentFilter mFilter;
    private  NetworkBroadcastReceiver mNetworkBroadcastReceiver;
    private WiFiManagerForRemote(Context context) {
        super(context);
        isCanScan = true;
        mAccessPointCustoms = null;  
        mFilter = new IntentFilter();
        mFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        mFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        mFilter.addAction(WifiManager.NETWORK_IDS_CHANGED_ACTION);
        mFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        mFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        mFilter.addAction(WifiManager.RSSI_CHANGED_ACTION);
        mNetworkBroadcastReceiver = new NetworkBroadcastReceiver(this);
    }
    public void registerNetworkBroadcastReceiver(){
    	mContext.registerReceiver(mNetworkBroadcastReceiver, mFilter);
    }
    public void unRegisterNetworkBroadcastReceiver(){
    	try{
    		mContext.unregisterReceiver(mNetworkBroadcastReceiver);
    	}
    	catch(Exception e){
    		e.printStackTrace();
    	}
    }
    public static WiFiManagerForRemote getInstance(Context context) {
        if (null == mWiFiManager) {
            synchronized (WiFiManagerForRemote.class) {
                if (null == mWiFiManager) {
                    mWiFiManager = new WiFiManagerForRemote(context);
                }
            }
        }
        return mWiFiManager;
    }

    /**
     * 打开Wifi
     */
    public void openWiFi() {
        if (!isWifiEnabled() && null != mWifiManager) {
            mWifiManager.setWifiEnabled(true);
        }
    }

    /**
     * 关闭Wifi
     */
    public void closeWiFi() {
        if (isWifiEnabled() && null != mWifiManager) {
            mWifiManager.setWifiEnabled(false);
        }
    }
    /**
     * 连接到开放网络
     *
     * @param ssid 热点名
     * @return 配置是否成功
     */
    public boolean connectOpenNetwork(String ssid) {
        // 获取networkId
        int networkId = setOpenNetwork(ssid);
        if (-1 != networkId) {
            // 保存配置
            boolean isSave = saveConfiguration();
            // 连接网络
            boolean isEnable = enableNetwork(networkId);

            return isSave && isEnable;
        }
        return false;
    }

    /**
     * 连接到WEP网络
     *
     * @param ssid     热点名
     * @param password 密码
     * @return 配置是否成功
     */
    public boolean connectWEPNetwork(String ssid, String password) {
        // 获取networkId
        int networkId = setWEPNetwork(ssid, password);
        if (-1 != networkId) {
            // 保存配置
            boolean isSave = saveConfiguration();
            // 连接网络
            boolean isEnable = enableNetwork(networkId);

            return isSave && isEnable;
        }
        return false;
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
        if (-1 != networkId) {
            // 保存配置
            boolean isSave = saveConfiguration();
            // 连接网络
            boolean isEnable = enableNetwork(networkId);
            return isSave && isEnable;       
        }
        return false;
    }
    /**
     * 连接到保存过的网络
     *
     * @param networkId
     * @return 配置是否成功
     */
    public boolean connectSavedNetwork(int networkId) {
        if (-1 != networkId) {
            // 连接网络
            boolean isEnable = enableNetwork(networkId);
            return  isEnable;
        }
        return false;
    }

    /**
     * 广播接收者
     */
    private   class NetworkBroadcastReceiver extends BroadcastReceiver {
    	 private WiFiManagerForRemote mWifiSettings;
    	public NetworkBroadcastReceiver(WiFiManagerForRemote wifiSettings){
    		mWifiSettings = wifiSettings;
    	}
        @Override
        public void onReceive(Context context, Intent intent) {
            WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            String action = intent.getAction();
            if (MLog.isDebug) {
                MLog.d(TAG, "onReceive  action ="+action );
            }
            // WIFI状态发生变化
            if (action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) { 
            	int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,WifiManager.WIFI_STATE_UNKNOWN);
            	mWifiSettings.updateWifiState(state);
            	 switch (state) {
                 case WifiManager.WIFI_STATE_ENABLED:
                	 mWifiSettings.mOnWifiEnabledListener.onWifiEnabled(true);
                     break;
                 case WifiManager.WIFI_STATE_DISABLED:
                	 mWifiSettings.mOnWifiEnabledListener.onWifiEnabled(false);
                     break;
            	 }
            	
            }
            else if (action.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
                    /*if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                        boolean isUpdated = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false);
                        MLog.d(TAG, "onReceive: WIFI扫描  " + (isUpdated ? "完成" : "未完成"));
                    } else {
                        MLog.d(TAG, "onReceive: WIFI扫描完成");
                    }*/
                // WIFI扫描完成 
            	mWifiSettings.updateAccessPoints();
            	mWifiSettings.mOnWifiScanResultsListener.onScanResults(mWifiSettings.mAccessPointCustoms);
            }
            // WIFI连接状态发生改变
            else if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
                NetworkInfo info = (NetworkInfo) intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                mWifiSettings.updateAccessPoints();
                mWifiSettings.updateNetworkInfo(info);
                mWifiSettings.mOnWifiScanResultsListener.onScanResults(mWifiSettings.mAccessPointCustoms);
            }
            
            else if (action.equals(WifiManager.RSSI_CHANGED_ACTION)) {
            	mWifiSettings.updateNetworkInfo(null);    
            	mWifiSettings.mOnWifiScanResultsListener.onScanResults(mWifiSettings.mAccessPointCustoms);
            }
        }
    }


    

    OnWifiEnabledListener mOnWifiEnabledListener;
    OnWifiScanResultsListener mOnWifiScanResultsListener;


    public void setOnWifiEnabledListener(OnWifiEnabledListener listener) {
        mOnWifiEnabledListener = listener;
    }
    public void removeOnWifiEnabledListener() {
        mOnWifiEnabledListener = null;
    }


    public void setOnWifiScanResultsListener(OnWifiScanResultsListener listener) {
        mOnWifiScanResultsListener = listener;
    }
    public void removeOnWifiScanResultsListener() {
        mOnWifiScanResultsListener = null;
    }


    
    /** A restricted multimap for use in constructAccessPoints */
    private static class Multimap<K,V> {
        private final HashMap<K,List<V>> store = new HashMap<K,List<V>>();
        /** retrieve a non-null list of values with key K */
        List<V> getAll(K key) {
            List<V> values = store.get(key);
            return values != null ? values : Collections.<V>emptyList();
        }

        void put(K key, V val) {
            List<V> curVals = store.get(key);
            if (curVals == null) {
                curVals = new ArrayList<V>(3);
                store.put(key, curVals);
            }
            curVals.add(val);
        }
    }
    /** Returns sorted list of access points */
    private static List<AccessPointCustom> constructAccessPoints(Context context,WifiManager wifiManager, WifiInfo lastInfo, NetworkInfo lastNetworkInfo) {
        ArrayList<AccessPointCustom> accessPoints = new ArrayList<AccessPointCustom>();
        /** Lookup table to more quickly update AccessPoints by only considering objects with the
         * correct SSID.  Maps SSID -> List of AccessPoints with the given SSID.  */
        Multimap<String,AccessPointCustom> apMap = new Multimap<String, AccessPointCustom>();

        final List<WifiConfiguration> configs = wifiManager.getConfiguredNetworks();
        if (configs != null) {            
            for (WifiConfiguration config : configs) {               
            	AccessPointCustom accessPoint = new AccessPointCustom(context, config);
                if (lastInfo != null && lastNetworkInfo != null) {
                    accessPoint.update(lastInfo, lastNetworkInfo);
                }
                accessPoints.add(accessPoint);
                apMap.put(accessPoint.ssid, accessPoint);
            }
        }

        final List<ScanResult> results = wifiManager.getScanResults();
        if (results != null) {
            for (ScanResult result : results) {
                // Ignore hidden and ad-hoc networks.
                if (result.SSID == null || result.SSID.length() == 0 ||
                        result.capabilities.contains("[IBSS]")) {
                    continue;
                }

                boolean found = false;
                for (AccessPointCustom accessPoint : apMap.getAll(result.SSID)) {
                    if (accessPoint.update(result))
                        found = true;
                }
                if (!found) {
                	AccessPointCustom accessPoint = new AccessPointCustom(context, result);
                    if (lastInfo != null && lastNetworkInfo != null) {
                        accessPoint.update(lastInfo, lastNetworkInfo);
                    }
                    accessPoints.add(accessPoint);
                    apMap.put(accessPoint.ssid, accessPoint);
                }
            }
        }
        // Pre-sort accessPoints to speed preference insertion
        Collections.sort(accessPoints);
        return accessPoints;
    }
    /**
     * Shows the latest access points available with supplemental information like
     * the strength of network and the security for it.
     */
    private void updateAccessPoints() {   
        final int wifiState = mWifiManager.getWifiState();
        if(wifiState == WifiManager.WIFI_STATE_ENABLED){
                // AccessPoints are automatically sorted with TreeSet.
        	mAccessPointCustoms = constructAccessPoints(mContext, mWifiManager, mLastInfo,mLastNetworkInfo);
         }
    }
    
    
    private void updateNetworkInfo(NetworkInfo networkInfo) {      
    	if (!mWifiManager.isWifiEnabled()) {           
            return;
        }
        if (networkInfo != null && networkInfo.getDetailedState() == DetailedState.OBTAINING_IPADDR) {
        	isCanScan = false;
        } 
        else{
        	isCanScan = true;
        }

        mLastInfo = mWifiManager.getConnectionInfo();
        if (networkInfo != null) {
            mLastNetworkInfo = networkInfo;
        }
        if(mAccessPointCustoms!=null){
        	for(AccessPointCustom  accessPoint : mAccessPointCustoms){
        		accessPoint.update(mLastInfo, mLastNetworkInfo);
        	}
        }
    }
    
    private  void updateWifiState(int state) {
        mLastInfo = null;
        mLastNetworkInfo = null;
    }

	public boolean isCanScan() {
		return isCanScan;
	}

	public void setCanScan(boolean isCanScan) {
		this.isCanScan = isCanScan;
	}

	public IntentFilter getFilter() {
		return mFilter;
	}
    
}
