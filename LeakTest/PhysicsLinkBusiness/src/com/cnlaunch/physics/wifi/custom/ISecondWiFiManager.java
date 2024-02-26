package com.cnlaunch.physics.wifi.custom;

import java.util.List;

public interface ISecondWiFiManager {
    boolean isEnabled();
    boolean setWifiEnabled(boolean enabled);
    boolean startScan();
    List<AccessPointCustomInterface> getScanResult();
    List<AccessPointCustomInterface> getScanResult(boolean isDebug);
    boolean addORUpdateNetwork(String ssid, String password);
    boolean requestIPWithDHCP();
    boolean setStaticIP();
    CustomWiFiControlForDualWiFi.WiFiState getCurrentWiFiState(String ssid);
    boolean setIpRule(final String routeRage,final String gateway);
    boolean setIpRule();
    boolean deleteIpRule();
}
