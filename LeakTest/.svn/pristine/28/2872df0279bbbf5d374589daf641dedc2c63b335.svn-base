package com.newchip.tool.leaktest.ui.setting.manager

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.Uri
import android.net.wifi.ScanResult
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import com.power.baseproject.bean.WifiData
import com.power.baseproject.ktbase.application.BaseApplication
import com.power.baseproject.utils.HandlerUtils
import com.power.baseproject.utils.log.LogUtil
import java.util.*

@SuppressLint("MissingPermission")
class WifiConnect {
    private var context: Context = BaseApplication.getContext()
    private var mWifiConnectHandler: HandlerUtils.HandlerHolder? = null

    companion object {
        private const val TAG = "WifiConnect"
        val instance: WifiConnect by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) { WifiConnect() }
    }

    fun setHandler(handler: HandlerUtils.HandlerHolder?) {
        mWifiConnectHandler = handler
    }

    private val wifiManager: WifiManager =
        context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

    /**
     * WIFICIPHER_WEP是WEP ，WIFICIPHER_WPA是WPA，WIFICIPHER_NOPASS没有密码
     */
    enum class WifiCipherType {
        WIFICIPHER_WEP, WIFICIPHER_WPA, WIFICIPHER_NOPASS, WIFICIPHER_INVALID
    }

    // 打开wifi
    fun openWifi(): Boolean {
        return !wifiManager.isWifiEnabled && wifiManager.setWifiEnabled(true)
    }

    // 关闭wifi
    fun closeWifi() {
        if (wifiManager.isWifiEnabled) {
            wifiManager.isWifiEnabled = false
        }
    }

    //断开当前连接
    fun disconnectWifi(): Boolean {
        return wifiManager.disconnect()
    }

    //扫描wifi信息
    fun scanWifi() {
        if (wifiManager.isWifiEnabled) {
            //扫描热点 如果扫描不到热点 把targetSdkVersion改成22
            LogUtil.e("kevin", "---------开始扫描wifi----------")
            wifiManager.startScan()
        }
    }

    //获取热点信息 ScanResult.SSID 获取热点名字 ScanResult.level 获取热点信号强度
    fun scanResultsWifi(): List<ScanResult>? {
        return if (wifiManager.isWifiEnabled) {
            //扫描热点 如果扫描不到热点 把targetSdkVersion改成22
            filterScanResult(wifiManager.scanResults)
        } else null
    }

    // 查看以前是否也配置过这个网络
    private fun isExsits(SSID: String): WifiConfiguration? {
        val existingConfigs = wifiManager.configuredNetworks
        for (existingConfig in existingConfigs) {
            if (existingConfig.SSID == "\"" + SSID + "\"") {
                return existingConfig
            }
        }
        return null
    }

    //配置wifi信息
    private fun createWifiInfo(
        SSID: String, Password: String,
        Type: WifiCipherType
    ): WifiConfiguration {
        val config = WifiConfiguration()
        config.allowedAuthAlgorithms.clear()
        config.allowedGroupCiphers.clear()
        config.allowedKeyManagement.clear()
        config.allowedPairwiseCiphers.clear()
        config.allowedProtocols.clear()
        config.SSID = "\"" + SSID + "\""
        val tempConfig = isExsits(SSID)

        // nopass
        if (Type == WifiCipherType.WIFICIPHER_NOPASS) {
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE)
        }
        // wep
        if (Type == WifiCipherType.WIFICIPHER_WEP) {
            if (!TextUtils.isEmpty(Password)) {
                if (isHexWepKey(Password)) {
                    config.wepKeys[0] = Password
                } else {
                    config.wepKeys[0] = "\"" + Password + "\""
                }
            }
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN)
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED)
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE)
            config.wepTxKeyIndex = 0
        }
        // wpa
        if (Type == WifiCipherType.WIFICIPHER_WPA) {
            config.preSharedKey = "\"" + Password + "\""
            config.hiddenSSID = true
            config.allowedAuthAlgorithms
                .set(WifiConfiguration.AuthAlgorithm.OPEN)
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP)
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK)
            config.allowedPairwiseCiphers
                .set(WifiConfiguration.PairwiseCipher.TKIP)
            // 此处需要修改否则不能自动重联
            // config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP)
            config.allowedPairwiseCiphers
                .set(WifiConfiguration.PairwiseCipher.CCMP)
            config.status = WifiConfiguration.Status.ENABLED
        }
        return config
    }

    private fun turnGPSOn(mContext: Context) {
        val provider = Settings.Secure.getString(
            mContext.contentResolver,
            Settings.Secure.LOCATION_PROVIDERS_ALLOWED
        )
        if (!provider.contains("gps")) {
            val poke = Intent()
            poke.setClassName(
                "com.android.settings",
                "com.android.settings.widget.SettingsAppWidgetProvider"
            )
            poke.addCategory(Intent.CATEGORY_ALTERNATIVE)
            poke.data = Uri.parse("3")
            mContext.sendBroadcast(poke)
        }
    }

    fun turnOnGps(mContext: Context?) {
        val gpsIntent = Intent()
        gpsIntent.setClassName(
            "com.android.settings",
            "com.android.settings.widget.SettingsAppWidgetProvider"
        )
        gpsIntent.addCategory("android.intent.category.ALTERNATIVE")
        gpsIntent.data = Uri.parse("custom:3")
        try {
            PendingIntent.getBroadcast(mContext, 0, gpsIntent, 0).send()
        } catch (e: PendingIntent.CanceledException) {
            e.printStackTrace()
        }
    }

    /**
     * 检查wifi是否处开连接状态
     *
     * @return
     */
    val isWifiConnect: Boolean
        get() {
            val connManager = BaseApplication.getContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val mWifiInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
            return mWifiInfo!!.isConnected
        }
    val connectWifiInfo: WifiInfo?
        get() {
            if (isWifiConnect) {
                val mWifiManager = BaseApplication.getContext().applicationContext
                    .getSystemService(Context.WIFI_SERVICE) as WifiManager
                return mWifiManager.connectionInfo
            }
            return null
        }

    fun calculateLevel(scanresult: ScanResult): Int {
        return WifiManager.calculateSignalLevel(scanresult.level, 4)
    }

    /* public static WifiCipherType getWiFiAccessPointSecurityPskType(ScanResult result) {
        boolean wpa = result.capabilities.contains("WPA-PSK");
        boolean wpa2 = result.capabilities.contains("WPA2-PSK");
        if (wpa2 && wpa) {
            return WifiCipherType.WPA_WPA2;
        } else if (wpa2) {
            return WifiCipherType.WPA2;
        } else if (wpa) {
            return WifiCipherType.WPA;
        } else {
            return WifiCipherType.UNKNOWN;
        }
        WIFICIPHER_WEP, WIFICIPHER_WPA, WIFICIPHER_NOPASS, WIFICIPHER_INVALID
    }*/
    fun connect(ssid: String, password: String, capabilities: String) {
        val config = WifiConfiguration()
        config.allowedAuthAlgorithms.clear()
        config.allowedGroupCiphers.clear()
        config.allowedKeyManagement.clear()
        config.allowedPairwiseCiphers.clear()
        config.allowedProtocols.clear()
        config.SSID = "\"" + ssid + "\""
        Log.d(TAG, "<connect_wifi_name>:$ssid")
        Log.d(TAG, "<connect_password>:$password")
        Log.d(TAG, "<capabilities>:$capabilities")
        if (TextUtils.isEmpty(capabilities)) {
            Log.w(TAG, "connect.............................none")
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE)
        } else {
            if (capabilities.contains("WEP") || capabilities.contains("wep")) {
                Log.w(TAG, "connect.............................wep")
                config.preSharedKey = "\"" + password + "\""
                config.hiddenSSID = true
                config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED)
                config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP)
                config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP)
                config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40)
                config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104)
                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE)
                config.wepTxKeyIndex = 0
            } else if (capabilities.contains("WPA") || capabilities.contains("wpa")) {
                Log.w(TAG, "connect.............................wpa")
                config.preSharedKey = "\"" + password + "\""
                config.hiddenSSID = true
                config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN)
                config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP)
                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK)
                config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP)
                config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP)
                config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP)
                config.status = WifiConfiguration.Status.ENABLED
            } else {
                Log.w(TAG, "connect.............................none")
                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE)
            }
        }
        val existingConfigs = wifiManager.configuredNetworks
        for (existingConfig in existingConfigs) {
            if (existingConfig.SSID == "\"" + ssid + "\"") {
                wifiManager.removeNetwork(existingConfig.networkId)
                break
            }
        }
        val netID = wifiManager.addNetwork(config)
        wifiManager.enableNetwork(netID, true)
        Log.w(TAG, "connect.............................2")
    }

    /**
     * 断开网络连接
     */
    fun disconnect(ssid: String) {
        val existingConfigs = wifiManager.configuredNetworks
        if (existingConfigs != null) { // 解决空指针异常
            for (existingConfig in existingConfigs) {
                if (existingConfig.SSID == "\"" + ssid + "\"" || existingConfig.SSID == ssid) {
                    wifiManager.disableNetwork(existingConfig.networkId)
                    //wifiManager.disconnect();
                    break
                }
            }
        }
    }

    /**
     * 连接指定的wifi
     *
     * @param ssid
     * @param password
     * @param capabilities
     */
    fun connectByManager(ssid: String, password: String?, capabilities: String?) {
        mWifiConnectHandler?.removeCallbacksAndMessages(null)
        mWifiConnectHandler?.sendEmptyMessageDelayed(1, 15000)
        val wifiCg = isExsits(ssid)//如果存在就返回配置信息
        if (wifiCg == null) {//为空表示没有保存过wifi
            if (capabilities != null) {
                if (addWifi(ssid, password, capabilities)) {
                    val wifiConfig = isExsits((ssid))
                    if (wifiConfig != null) {
                        wifiManager.enableNetwork(wifiConfig.networkId, true)
                    }
                }
            }
        } else {//不为空表示：保存过wifi
            wifiManager.enableNetwork(wifiCg.networkId, true)
        }
    }

    /**
     * 添加wifi
     *
     * @param ssid
     * @param password
     * @param capabilities
     * @return
     */
    fun addWifi(ssid: String, password: String?, capabilities: String): Boolean {
        val existingConfigs = wifiManager.configuredNetworks

        /**
         * 原先已添加的wifi配置信息
         */
        var existingConfig: WifiConfiguration? = null
        for (tempWifiConfiguration in existingConfigs) {
            if (tempWifiConfiguration.SSID == "\"" + ssid + "\"") {
                existingConfig = tempWifiConfiguration
                //                wifiManager.removeNetwork(tempWifiConfiguration.networkId);
                break
            }
        }
        var config: WifiConfiguration? = null
        config = existingConfig ?: WifiConfiguration()
        config.allowedAuthAlgorithms.clear()
        config.allowedGroupCiphers.clear()
        config.allowedKeyManagement.clear()
        config.allowedPairwiseCiphers.clear()
        config.allowedProtocols.clear()
        config.SSID = "\"" + ssid + "\""
        if (TextUtils.isEmpty(capabilities)) {
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE)
        } else {
            if (capabilities.contains("WEP") || capabilities.contains("wep")) {
                config.hiddenSSID = true
                if (!TextUtils.isEmpty(password)) {
                    if ((password!!.toByteArray().size == 10 || password.toByteArray().size == 26 || password.toByteArray().size == 58) && password.matches(
                            "[0-9A-Fa-f]*".toRegex()
                        )
                    ) config.wepKeys[0] = password else config.wepKeys[0] = "\"" + password + "\""
                } else config.wepKeys[0] = password
                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE)
                config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN)
                config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED)
            } else if (capabilities.contains("WPA") || capabilities.contains("wpa")) {
                if (!TextUtils.isEmpty(password)) config.preSharedKey =
                    "\"" + password + "\"" else config.preSharedKey = password
                config.hiddenSSID = true
                config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN)
                config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP)
                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK)
                config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP)
                config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP)
                config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP)
                config.status = WifiConfiguration.Status.ENABLED
            } else {
                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE)
            }
        }
        var result = false
        /**
         * 新添加的wifi
         */
        result = if (existingConfig == null) {
            val netID = wifiManager.addNetwork(config)
            LogUtil.i("kevin","addNetwork====netId:$netID")
            netID >= 0
        } else { //更新wifi
            val netID = wifiManager.updateNetwork(config) //apply the setting
            LogUtil.i("kevin","updateNetwork====netId:$netID")
            netID != -1
        }
        return result
    }

    /**
     * 获取wifi列表转换成为固定对象的列表
     */
    fun changeToWifiData(wifiList: List<ScanResult>?): List<WifiData> {
        return if (wifiList != null && wifiList.isNotEmpty()) {
            val arrayList: ArrayList<WifiData> = ArrayList<WifiData>()
            for (s in wifiList) {
                val level = WifiManager.calculateSignalLevel(s.level, 4)
                if (level == -1) continue
                if (TextUtils.isEmpty(s.SSID)) continue
                if ("NVRAM WARNING: Err = 0x10" == s.SSID) continue
                val wifiData = WifiData()
                wifiData.name = s.SSID
                wifiData.signal = level
                wifiData.capabilities = s.capabilities
                arrayList.add(wifiData)
            }
            //按信号强度大小排序
            arrayList.sort()
            //将连接的放在第一个
            if (connectWifiInfo != null) {
                for (i in arrayList.indices) {
                    val wifiData: WifiData = arrayList[i]
                    if (connectWifiInfo != null && stringReplace(connectWifiInfo!!.ssid) == arrayList[i].name) {
                        arrayList.removeAt(i)
                        arrayList.add(0, wifiData)
                    }
                }
            }
            arrayList
        } else {
            ArrayList<WifiData>()
        }
    }

    private fun stringReplace(str: String): String {
        //去掉" "号
        return str.replace("\"", "")
    }

    fun isLocServiceEnable(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val network = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        return gps || network
    }

    /**
     * 使用 反射networkId 连接.
     */
    fun connectByNetworkId(manager: WifiManager?, networkId: Int) {
        if (manager == null) {
            return
        }
        try {
            val connect = manager.javaClass.getDeclaredMethod(
                "connect",
                Int::class.javaPrimitiveType,
                Class.forName("android.net.wifi.WifiManager\$ActionListener")
            )
            if (connect != null) {
                connect.isAccessible = true
                connect.invoke(manager, networkId, null)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun isHexWepKey(wepKey: String): Boolean {
        val len = wepKey.length
        // WEP-40, WEP-104, and some vendors using 256-bit WEP (WEP-232?)
        return !(len != 10 && len != 26 && len != 58) && isHex(wepKey)
    }

    private fun isHex(key: String): Boolean {
        for (i in key.length - 1 downTo 0) {
            val c = key[i]
            if (!(c in '0'..'9' || c in 'A'..'F' || (c in 'a'..'f'))
            ) {
                return false
            }
        }
        return true
    }

    /**
     * 判断网络是否连接
     *
     * @param context context
     * @return true/false
     */
    fun isNetConnected(context: Context): Boolean {
        val connectivity =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val info = connectivity.activeNetworkInfo
        if (info != null && info.isConnected) {
            if (info.state == NetworkInfo.State.CONNECTED) {
                return true
            }
        }
        return false
    }

    /**
     * 以 SSID 为关键字，过滤掉信号弱的选项
     *
     * @param list
     * @return
     */
    fun filterScanResult(list: MutableList<ScanResult>): List<ScanResult> {
        val linkedMap = LinkedHashMap<String, ScanResult>(list.size)
        for (rst in list) {
            if (linkedMap.containsKey(rst.SSID)) {
                if (rst.level > linkedMap[rst.SSID]!!.level) {
                    linkedMap[rst.SSID] = rst
                }
                continue
            }
            linkedMap[rst.SSID] = rst
        }
        list.clear()
        list.addAll(linkedMap.values)
        return list
    }


}