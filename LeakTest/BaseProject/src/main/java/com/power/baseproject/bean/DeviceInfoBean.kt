package com.power.baseproject.bean

import java.io.Serializable

class DeviceInfoBean : Serializable {
    var deviceSn: String? = null//设备SN
    var deviceNum: String? = null//设备编号
    var deviceName: String? = null//设备型号
    var softVersion: String? = null
    var hardwareVersion: String? = null
    override fun toString(): String {
        return "DeviceInfoBean(deviceSn=$deviceSn, deviceNum=$deviceNum, deviceName=$deviceName, softVersion=$softVersion, hardwareVersion=$hardwareVersion)"
    }

}