package com.newchip.tool.leaktest.utils

import android.content.Context
import com.cnlaunch.physics.DeviceFactoryManager
import com.cnlaunch.physics.impl.IPhysics
import com.cnlaunch.physics.serialport.SerialPortManager
import com.cnlaunch.physics.serialport.util.LeakTestTools
import com.cnlaunch.physics.usb.Connector
import com.cnlaunch.physics.usb.DPUUSBManager

class DeviceConnectUtils private constructor() {
    companion object {
        val instance: DeviceConnectUtils by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            DeviceConnectUtils()
        }
    }

    private var mCurrentDevice: IPhysics? = null
    private var mFactoryDevice: IPhysics? = null

    fun createSerialManager(context: Context, serialNo: String): IPhysics? {
        if (ForwardUtils.instance.client) {
            //主控端不需要连接串口，为了减小代码改动，虚拟一个串口出来
            val device = SerialPortManager(
                DeviceFactoryManager.getInstance(),
                context,
                serialNo,
                LeakTestTools.LEAK_DEVICE
            )
            device.dataType = IPhysics.DEVICE_DATA_TYPE_LEAK
            device.commandStatus = false
            mCurrentDevice = device
            return device
        }
        mCurrentDevice?.closeDevice()
        mCurrentDevice = null
        try {
            val device = SerialPortManager(
                DeviceFactoryManager.getInstance(),
                context,
                serialNo,
                LeakTestTools.LEAK_DEVICE
            )
            val state = device.connect()
            if (state == IPhysics.STATE_CONNECTED) {
                device.dataType = IPhysics.DEVICE_DATA_TYPE_LEAK
                device.commandStatus = false
                mCurrentDevice = device
                return mCurrentDevice as SerialPortManager
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun createFactorySerialManager(context: Context): IPhysics? {
        mFactoryDevice?.closeDevice()
        mFactoryDevice = null
        try {
            val device = SerialPortManager(
                DeviceFactoryManager.getInstance(),
                context,
                "806030",
                LeakTestTools.FACTORY_DEVICE
            )
            val state = device.connect()
            if (state == IPhysics.STATE_CONNECTED) {
                device.dataType = IPhysics.DEVICE_DATA_TYPE_LEAK
                device.commandStatus = false
                mFactoryDevice = device
                return mFactoryDevice as SerialPortManager
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun creatUsbManager(context: Context, serialNo: String): IPhysics? {
        if (mCurrentDevice != null) {
            if (mCurrentDevice != null) {
                mCurrentDevice!!.closeDevice()
                mCurrentDevice = null
            }
        }
        try {
            val device = DPUUSBManager(DeviceFactoryManager.getInstance(), context, false, serialNo)
            val status: Int = device.open(true)
            if (status != Connector.STATE_SUCCESS && status != Connector.STATE_NO_PERMISSION) {
                device.closeDevice()
                mCurrentDevice = null
            } else {
                device.dataType = IPhysics.DEVICE_DATA_TYPE_LEAK
                device.commandStatus = false
                mCurrentDevice = device
                return mCurrentDevice
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun closeCurrentDevice() {
        mCurrentDevice?.closeDevice()
        mCurrentDevice = null
    }

    fun getCurrentDevice(): IPhysics? {
        return mCurrentDevice
    }

    fun closeFactoryDevice() {
        mFactoryDevice?.closeDevice()
        mFactoryDevice = null
    }

    fun getFactoryDevice(): IPhysics? {
        return mFactoryDevice
    }
}