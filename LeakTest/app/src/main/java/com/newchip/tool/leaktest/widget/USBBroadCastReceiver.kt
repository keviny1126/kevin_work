package com.newchip.tool.leaktest.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import com.power.baseproject.common.UsbListener

class USBBroadCastReceiver : BroadcastReceiver() {
    companion object{
        const val ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION"
    }
    private lateinit var usbListener: UsbListener
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_USB_PERMISSION -> {
                //接受到自定义广播
                val usbDevice: UsbDevice? = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                    //允许权限申请
                    if (usbDevice != null) {
                        //回调
                        usbListener.getReadUsbPermission(usbDevice)
                    }
                } else {
                    usbListener.failedReadUsb(usbDevice)
                }
            }
            UsbManager.ACTION_USB_DEVICE_ATTACHED -> {
                //接收到存储设备插入广播
                val deviceAdd: UsbDevice? = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (deviceAdd != null) {
                    usbListener.insertUsb(deviceAdd)
                }
            }
            UsbManager.ACTION_USB_DEVICE_DETACHED -> {
                val deviceRemove: UsbDevice? = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (deviceRemove != null) {
                    usbListener.removeUsb(deviceRemove)
                }
            }

        }
    }

    fun setUsbListener(usbListener: UsbListener) {
        this.usbListener = usbListener
    }
}