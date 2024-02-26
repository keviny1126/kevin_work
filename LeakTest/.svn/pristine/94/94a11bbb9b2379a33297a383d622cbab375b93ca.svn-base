package com.power.baseproject.bean

import java.io.Serializable

class WifiData : Comparable<WifiData>,Serializable {
    //WIFI名称
    var name: String? = null

    //WIFI密码
    var password: String? = null

    //信号强度
    var signal = 0

    //加密方式
    var capabilities: String? = null

    fun compareWith(wifiData: WifiData): Boolean {
        return if (name != wifiData.name) //WIFI名称不同肯定是不同的WIFI
        {
            false
        } else {
            var cap1 = ""
            var cap2 = ""
            if (capabilities!!.contains("WEP") || capabilities!!.contains("wep")) {
                cap1 = "wep"
            } else if (capabilities!!.contains("WPA2") || capabilities!!.contains("wpa2")) {
                cap1 = "wpa2"
            } else if (capabilities!!.contains("WPA1") || capabilities!!.contains("wpa1")) {
                cap1 = "wpa1"
            }
            if (wifiData.capabilities!!.contains("WEP") || wifiData.capabilities!!.contains("wep")) {
                cap2 = "wep"
            } else if (wifiData.capabilities!!.contains("WPA2") || wifiData.capabilities!!.contains(
                    "wpa2"
                )
            ) {
                cap2 = "wpa2"
            } else if (wifiData.capabilities!!.contains("WPA1") || wifiData.capabilities!!.contains(
                    "wpa1"
                )
            ) {
                cap2 = "wpa1"
            }
            cap1 == cap2
        }
    }

    override fun toString(): String {
        return "WifiData{" +
                "name='" + name + '\'' +
                ", password='" + password + '\'' +
                ", signal=" + signal +
                ", capabilities='" + capabilities + '\'' +
                '}'
    }

    override fun compareTo(wifiData: WifiData): Int {
        return wifiData.signal - signal
    }
}