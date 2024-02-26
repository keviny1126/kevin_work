/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cnlaunch.physics.wifi.settings;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.NetworkInfo.State;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.widget.TextView;

import com.cnlaunch.bluetooth.R;
import com.cnlaunch.physics.utils.MLog;


public class AccessPointCustom implements Comparable<AccessPointCustom>{
    static final String TAG = "AccessPointCustom";
    public static final int INVALID_NETWORK_ID = -1;
     
    private WifiInfo mInfo;
    private NetworkInfo mNetworkInfo;
    private String mSummary;
    private Context mContext;
    
    private int networkId;
    public String ssid;
    String bssid;
    private WifiConfiguration mConfig;
    private int security;
    private  ScanResult mScanResult;
    private int mRssi;    
    int pskType = PskType.UNKNOWN;
    AccessPointCustom(Context context, ScanResult result) {
    	mContext = context;
    	mConfig = null;
        loadResult(result);
        refresh();
    }
    AccessPointCustom(Context context, WifiConfiguration config) {
    	mContext = context;
    	mScanResult = null;
        loadConfig(config);
        refresh();
    }
    private void loadResult(ScanResult result) {  
    	networkId = INVALID_NETWORK_ID;
    	ssid = result.SSID;
    	bssid = result.BSSID;
        security = getSecurity(result);
        if (security == SecurityMode.SECURITY_PSK)
            pskType = getPskType(result);
        mRssi = result.level;
        mScanResult = result;
    }  
    private void loadConfig(WifiConfiguration config) {
    	networkId = config.networkId;
        ssid = (config.SSID == null ? "" : removeDoubleQuotes(config.SSID));
        bssid = config.BSSID;
        security = getSecurity(config);    
        mRssi =  Integer.MAX_VALUE;
        mConfig = config;
    }
    @Override
    public int compareTo(AccessPointCustom accessPoint) {
        if (!(accessPoint instanceof AccessPointCustom)) {
            return 1;
        }
        AccessPointCustom other = (AccessPointCustom) accessPoint;
        // Active one goes first.
        if (isActive() && !other.isActive()) return -1;
        if (!isActive() && other.isActive()) return 1;

        // Reachable one goes before unreachable one.
        if (mRssi != Integer.MAX_VALUE && other.mRssi == Integer.MAX_VALUE) return -1;
        if (mRssi == Integer.MAX_VALUE && other.mRssi != Integer.MAX_VALUE) return 1;

        // Configured one goes before unconfigured one.
        if (networkId != INVALID_NETWORK_ID&& other.networkId == INVALID_NETWORK_ID) return -1;
        if (networkId == INVALID_NETWORK_ID&& other.networkId != INVALID_NETWORK_ID) return 1;

        // Sort by signal strength.
        int difference = WifiManager.compareSignalLevel(other.mRssi, mRssi);
        if (difference != 0) {
            return difference;
        }
        // Sort by ssid.
        return ssid.compareToIgnoreCase(other.ssid);
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof AccessPointCustom)) return false;
        return (this.compareTo((AccessPointCustom) other) == 0);
    }

    @Override
    public int hashCode() {
        int result = 0;
        if (mInfo != null) result += 13 * mInfo.hashCode();
        result += 19 * mRssi;
        result += 23 * networkId;
        result += 29 * ssid.hashCode();
        return result;
    }

    boolean update(ScanResult result) {
        if (ssid.equals(result.SSID) && security == getSecurity(result)) {
            if (WifiManager.compareSignalLevel(result.level, mRssi) > 0) {
                mRssi = result.level;                
            }
            // This flag only comes from scans, is not easily saved in config
            if (security == SecurityMode.SECURITY_PSK) {
                pskType = getPskType(result);
            }
            mScanResult = result;
            refresh();
            return true;
        }
        return false;
    }

    

    void update(WifiInfo info, NetworkInfo networkInfo) {
        if (info != null && isInfoForThisAccessPoint(info)) {
            mRssi = info.getRssi();
            mInfo = info;
            mNetworkInfo = networkInfo;
            refresh();
        } else if (mInfo != null) {
            mInfo = null;
            mNetworkInfo = null;
            refresh();
        }
    }

    /**
     * Return whether this is the active connection.
     * For ephemeral connections (networkId is invalid), this returns false if the network is
     * disconnected.
     */
    boolean isActive() {
        return mNetworkInfo != null && (networkId != INVALID_NETWORK_ID || mNetworkInfo.getState() != State.DISCONNECTED);
    }

    /**
     * Updates the title and summary; may indirectly call notifyChanged().
     */
    private void refresh() {       
        // Force new summary
    	mSummary =null;

        // Update to new summary
        StringBuilder summary = new StringBuilder();
        if (isActive()) { // This is the active connection
            summary.append(Summary.get(mContext, getState(),networkId == INVALID_NETWORK_ID));
        }else if (mRssi == Integer.MAX_VALUE) { // Wifi out of range
            summary.append(mContext.getString(R.string.wifi_not_in_range));
        } else { // In range, not disabled.
            if (mConfig != null) { // Is saved network
                summary.append(mContext.getString(R.string.wifi_remembered));
            }
        }
        mSummary = summary.toString();
    }
    static String removeDoubleQuotes(String string) {
        int length = string.length();
        if ((length > 1) && (string.charAt(0) == '"')
                && (string.charAt(length - 1) == '"')) {
            return string.substring(1, length - 1);
        }
        return string;
    }
    static int getSecurity(WifiConfiguration config) {
        if (config.allowedKeyManagement.get(KeyMgmt.WPA_PSK)) {
            return SecurityMode.SECURITY_PSK;
        }
        if (config.allowedKeyManagement.get(KeyMgmt.WPA_EAP) ||
                config.allowedKeyManagement.get(KeyMgmt.IEEE8021X)) {
            return SecurityMode.SECURITY_EAP;
        }
        return (config.wepKeys[0] != null) ? SecurityMode.SECURITY_WEP : SecurityMode.SECURITY_NONE;
    }

    private static int getSecurity(ScanResult result) {
        if (result.capabilities.contains("WEP")) {
            return SecurityMode.SECURITY_WEP;
        } else if (result.capabilities.contains("PSK")) {
            return SecurityMode.SECURITY_PSK;
        } else if (result.capabilities.contains("EAP")) {
            return SecurityMode.SECURITY_EAP;
        }
        return SecurityMode.SECURITY_NONE;
    }
    private static int getPskType(ScanResult result) {
        boolean wpa = result.capabilities.contains("WPA-PSK");
        boolean wpa2 = result.capabilities.contains("WPA2-PSK");
        if (wpa2 && wpa) {
            return PskType.WPA_WPA2;
        } else if (wpa2) {
            return PskType.WPA2;
        } else if (wpa) {
            return PskType.WPA;
        } else {
            Log.w(TAG, "Received abnormal flag string: " + result.capabilities);
            return PskType.UNKNOWN;
        }
    }
    
    /** Return whether the given {@link WifiInfo} is for this access point. */
    private boolean isInfoForThisAccessPoint(WifiInfo info) {
        if (networkId != INVALID_NETWORK_ID) {
            return networkId == info.getNetworkId();
        } else {
            // Might be an ephemeral connection with no WifiConfiguration. Try matching on SSID.
            // (Note that we only do this if the WifiConfiguration explicitly equals INVALID).
            // TODO: Handle hex string SSIDs.
            return ssid.equals(removeDoubleQuotes(info.getSSID()));
        }
    }
    DetailedState getState() {
        return mNetworkInfo != null ? mNetworkInfo.getDetailedState() : null;
    }
    int getLevel() {
        if (mRssi == Integer.MAX_VALUE) {
            return -1;
        }
        return WifiManager.calculateSignalLevel(mRssi, 4);
    }
    public String convertToRemoteJson(){
    	String jsonString = "";
    	try {
            JSONObject json = new JSONObject();
            json.put("SSID", ssid);
            json.put("BSSID", bssid);
            json.put("rssi", mRssi);
            json.put("securityMode",security );
            json.put("pskType",pskType);
            json.put("summary", mSummary);
            json.put("isActive",isActive());
            json.put("isSave",(mConfig!=null)?true:false);
            json.put("networkId",networkId);
            jsonString =  json.toString();
            if (MLog.isDebug) {
                MLog.d(TAG, "json.toString()=" + jsonString);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    	return jsonString;
    }
}
