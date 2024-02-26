package com.newchip.tool.leaktest.ui.setting.manager

import android.content.Context
import android.text.TextUtils
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.power.baseproject.bean.DeviceWifiBean
import com.power.baseproject.utils.EasyPreferences

object WifiDataManager {

    const val WIFI = "wifi_data"

    /**
     * 插入一条WIFI
     *
     * @param wifiBean
     */
    fun insertWifi(wifiBean: DeviceWifiBean, context: Context) {
        val list = getList(context)
        list.add(wifiBean)
        save(list, context)
    }

    fun getList(context: Context): MutableList<DeviceWifiBean> {
        val json = EasyPreferences.instance[WIFI, ""]
        var list: MutableList<DeviceWifiBean> = arrayListOf()
        if (TextUtils.isEmpty(json)) {
            return list
        }
        list = Gson().fromJson(json, object : TypeToken<List<DeviceWifiBean>>() {}.type)
        return list
    }

    fun save(list: List<DeviceWifiBean>, context: Context) {
        EasyPreferences.instance.put(WIFI, Gson().toJson(list))
    }

    /**
     * 按条件查找WIFI记录
     */
    fun queryWifiByKey(name: String?, context: Context): List<DeviceWifiBean> {
        val json: String? = EasyPreferences.instance[WIFI, ""]
        val list: MutableList<DeviceWifiBean> = arrayListOf()
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(json)) {
            return list
        }
        val data: List<DeviceWifiBean> =
            Gson().fromJson(
                json,
                object : TypeToken<List<DeviceWifiBean>>() {}.type
            )
        for (wifi in data) {
            if (wifi.name.equals(name)) {
                list.add(wifi)
            }
        }
        return list
    }

    /**
     * 删除一条WIFI记录
     *
     * @param crpWifi
     */
    fun deleteWifi(wifiBean: DeviceWifiBean, context: Context) {
        val list = getList(context)
        if (list.isNotEmpty()) {
            for (i in list.indices) {
                val wifi: DeviceWifiBean = list[i]
                if (wifi.name.equals(wifiBean.name)) {
                    list.removeAt(i)
                    break
                }
            }
        }
        save(list, context)
    }
}