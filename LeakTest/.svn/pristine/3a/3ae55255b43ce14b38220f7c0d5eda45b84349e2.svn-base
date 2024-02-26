package com.power.baseproject.common

import android.hardware.usb.UsbDevice

interface UsbListener {
    fun insertUsb(device_add: UsbDevice)

    //USB 移除
    fun removeUsb(device_remove: UsbDevice)

    //获取读取USB权限
    fun getReadUsbPermission(usbDevice: UsbDevice)

    //读取USB信息失败
    fun failedReadUsb(usbDevice: UsbDevice?)
}