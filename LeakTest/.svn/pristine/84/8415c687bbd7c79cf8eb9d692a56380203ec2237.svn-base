package com.cnlaunch.physics.wifi.custom;

import android.content.Context;
import android.text.TextUtils;

import com.cnlaunch.physics.utils.Constants;
import com.cnlaunch.physics.utils.MLog;
import com.cnlaunch.physics.wifi.settings.PskType;
import com.cnlaunch.physics.wifi.settings.SecurityMode;
import com.power.baseproject.utils.EasyPreferences;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class SecondWiFiUtil {
    private static final String TAG = "SecondWiFiUtil";

    static int convertIntForCommandResult(String returnValue) {
        return 0;
    }

    static boolean convertBooleanForCommandResult(String returnValue) {
        if (TextUtils.isEmpty(returnValue) || returnValue.equals("null")) {
            return false;
        }
        if (returnValue.equals("OK")) {
            return true;
        } else {
            return false;
        }
    }

    static public List<AccessPointCustomInterface> coverScanResult(String scanResult, boolean isDebug, ISecondWiFiManager secondWiFiManager) {
        ArrayList<AccessPointCustomInterface> list = new ArrayList<AccessPointCustomInterface>();
        if (TextUtils.isEmpty(scanResult)) {
            return list;
        }
        //mac/bssid / frequency   signal level / flags / ssid
        //ac:85:3d:ba:6b:52	2462	-56	[WPA2-PSK-CCMP][ESS]	Launch_GN

        BufferedReader bufferedReader;
        bufferedReader = new BufferedReader(new StringReader(scanResult));
        if (bufferedReader != null) {
            try {
                String result = null;
                result = bufferedReader.readLine();//去掉第一行
                if (MLog.isDebug) {
                    MLog.d(TAG, "SCAN_RESULTS result " + result);
                }
                int rssi = Integer.MAX_VALUE;
                result = bufferedReader.readLine();
                while (result != null) {
                    if (MLog.isDebug) {
                        MLog.d(TAG, "SCAN_RESULTS result " + result);
                    }
                    String[] resultValue = result.split("\t");
                    if (MLog.isDebug) {
                        MLog.d(TAG, "SCAN_RESULTS resultValue.size " + resultValue.length);
                    }
                    if (resultValue.length >= 5) {
                        AccessPointCustomInterface accessPointCustomInterface = new AccessPointCustomInterface();
                        accessPointCustomInterface.mac = resultValue[0];
                        accessPointCustomInterface.bssid = resultValue[1];
                        try {
                            rssi = Integer.parseInt(resultValue[2]);
                        } catch (Exception e) {
                            rssi = Integer.MAX_VALUE;
                        }
                        accessPointCustomInterface.rssi = rssi;
                        accessPointCustomInterface.security = getSecurity(resultValue[3]);
                        accessPointCustomInterface.pskType = getPskType(resultValue[3]);
                        accessPointCustomInterface.ssid = resultValue[4];
                        accessPointCustomInterface.isActive = false;
                        if (TextUtils.isEmpty(accessPointCustomInterface.ssid) == false && isDebug == false) {
                            CustomWiFiControlForDualWiFi.WiFiState wiFiState = secondWiFiManager.getCurrentWiFiState(accessPointCustomInterface.ssid);
                            if (wiFiState.wpaState == CustomWiFiControlForDualWiFi.WPAState.CONNECTED) {
                                accessPointCustomInterface.isActive = true;
                            }
                        }
                        if (isDebug == false) {
                            if (accessPointCustomInterface.ssid.matches("([0-9]{12})")) {
                                list.add(accessPointCustomInterface);
                            }
                        } else {
                            list.add(accessPointCustomInterface);
                        }
                    }
                    result = bufferedReader.readLine();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    private static int getSecurity(String security) {
        if (security.contains("WEP")) {
            return SecurityMode.SECURITY_WEP;
        } else if (security.contains("PSK")) {
            return SecurityMode.SECURITY_PSK;
        } else if (security.contains("EAP")) {
            return SecurityMode.SECURITY_EAP;
        }
        return SecurityMode.SECURITY_NONE;
    }

    private static int getPskType(String security) {
        boolean wpa = security.contains("WPA-PSK");
        boolean wpa2 = security.contains("WPA2-PSK");
        if (wpa2 && wpa) {
            return PskType.WPA_WPA2;
        } else if (wpa2) {
            return PskType.WPA2;
        } else if (wpa) {
            return PskType.WPA;
        } else {
            if (MLog.isDebug) {
                MLog.d("getPskType", "Received abnormal flag string: " + security);
            }
            return PskType.UNKNOWN;
        }
    }

    private static String USB_SWITCH_POWER_PATH = "/sys/usb_switch/usbwifi";

    public static void setPowerOn(Context context) {
        powerOperation("1", context);
    }

    public static void setPowerOff(Context context) {
        powerOperation("0", context);
    }

    /**
     * 对于new BufferReader(new InputStreamReader(new InputStream(...)));这种多层次的流调用，
     * 只需对最外层执行close（），外层的会依次关闭里层的。
     *
     * @return
     */
    private static boolean isPowerOn() {
        BufferedReader br_wifi = null;
        StringBuffer sb_wifi = new StringBuffer("");
        try {
            br_wifi = new BufferedReader(new InputStreamReader(
                    new FileInputStream(new File(USB_SWITCH_POWER_PATH)), "UTF-8"));
            String s_wifi = "";
            while ((s_wifi = br_wifi.readLine()) != null) {
                sb_wifi.append(s_wifi);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (br_wifi != null) {
                try {
                    br_wifi.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            String power_state_string = sb_wifi.toString();
            if (MLog.isDebug) {
                MLog.d(TAG, "isPowerOn  power_state_string=" + power_state_string);
            }
            if (power_state_string != null && power_state_string.equals("1")) {
                return true;
            } else {
                return false;
            }
        }
    }

    /**
     * 1 表示上电
     * 0 表示关闭电源
     *
     * @param state
     */
    private static boolean powerOperation(String state, Context context) {
        boolean isNewDualWifiSupport = EasyPreferences.Companion.getInstance().get(Constants.IS_NEW_DUAL_WIFI_SUPPORT, false);
        if (!isNewDualWifiSupport) {
            return false;
        }
        if (state.equals("1") && isPowerOn()) {
            return true;
        } else if (state.equals("0") && !isPowerOn()) {
            return true;
        }
        if (MLog.isDebug) {
            MLog.d(TAG, "powerOperation start state=" + state);
        }
        FileWriter fr_wifi = null;
        try {
            fr_wifi = new FileWriter(new File(USB_SWITCH_POWER_PATH));
            fr_wifi.write(state);
            fr_wifi.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (fr_wifi != null) {
                try {
                    fr_wifi.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
