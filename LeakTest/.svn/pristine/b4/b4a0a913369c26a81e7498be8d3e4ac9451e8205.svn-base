package com.newchip.tool.leaktest.utils

import android.text.TextUtils
import com.cnlaunch.physics.impl.IPhysics
import com.cnlaunch.physics.serialport.util.AnalysisLeakData
import com.cnlaunch.physics.serialport.util.OrderMontageForLeak
import com.cnlaunch.physics.utils.ByteHexHelper
import com.jeremyliao.liveeventbus.LiveEventBus
import com.power.baseproject.bean.ConfigLeakInfoBean
import com.power.baseproject.bean.UpdateInfoBean
import com.power.baseproject.utils.ConstantsUtils
import com.power.baseproject.utils.LiveEventBusConstants
import com.power.baseproject.utils.log.LogUtil
import java.util.Date

object LeakSerialOrderUtils {
    private const val TAG = "LeakSerialOrderUtils"
    private const val TIMEOUT = 5000

    fun transCommand(iPhysics: IPhysics?, backOrder: String): Boolean {
        if (iPhysics == null || backOrder.isEmpty()) {
            return false
        }
        val result: Boolean
        val sendOrder = ByteHexHelper.hexStringToBytes(backOrder)
        iPhysics.commandStatus = false
        iPhysics.command_wait = true
        result = try {
            //指令发送后由接收类统一处理
            iPhysics.setCommand("", true)
            val outputStream = iPhysics.outputStream
            outputStream?.write(sendOrder)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
        return result
    }

    fun sendWifiBackOrder(iPhysics: IPhysics?, signal: Int): Boolean {
        val sendOrder =
            OrderMontageForLeak.instance.createWifiBackOrder(ByteHexHelper.intToHexString(signal))
        if (iPhysics == null || sendOrder.isEmpty()) {
            return false
        }
        LogUtil.d("FS_TOOL", "下发wifi指令给工装:$sendOrder")
        return transCommand(iPhysics, ByteHexHelper.bytesToHexString(sendOrder))
    }

    fun commonSetCommand(iPhysics: IPhysics, sendOrder: ByteArray) {
        if (ForwardUtils.instance.client) {
            //下发指令透传给被控端
            ForwardUtils.instance.sendCmd(ByteHexHelper.bytesToHexString(sendOrder))
            return
        }
        iPhysics.commandStatus = false
        iPhysics.command_wait = true
        try {
            //指令发送后由接收类统一处理
            iPhysics.setCommand("", true)
            val outputStream = iPhysics.outputStream
            outputStream.write(sendOrder)
        } catch (e: Exception) {
            e.printStackTrace()
            LiveEventBus.get(LiveEventBusConstants.SERIAL_NEED_CONNECT).post(e)
        }
    }

    fun getDeviceInfoOrder(iPhysics: IPhysics) {
        val sendOrder =
            OrderMontageForLeak.instance.createGetDeviceInfoOrder() ?: return
        if (sendOrder.isEmpty()) {
            return
        }
        commonSetCommand(iPhysics, sendOrder)
    }

    fun testOnOrOffOrder(iPhysics: IPhysics, testState: Boolean, infoBean: ConfigLeakInfoBean) {

        var preValue = (infoBean.testPressure * 1000).toInt().toString(16)
        preValue = ByteHexHelper.convertHexStringNumBits(preValue, 8)

        var upperVol = (infoBean.upperPressureLimit * 1000).toInt().toString(16)
        upperVol = ByteHexHelper.convertHexStringNumBits(upperVol, 8)

        var lowVol = (infoBean.lowerPressureLimit * 1000).toInt().toString(16)
        lowVol = ByteHexHelper.convertHexStringNumBits(lowVol, 8)

        var fillTime = (infoBean.inflationTime).toString(16)
        fillTime = ByteHexHelper.convertHexStringNumBits(fillTime, 4)

        var stableTime = (infoBean.stabilizationTime).toString(16)
        stableTime = ByteHexHelper.convertHexStringNumBits(stableTime, 4)

        var checkTime = (infoBean.detectionTime).toString(16)
        checkTime = ByteHexHelper.convertHexStringNumBits(checkTime, 4)

        var exhaustTime = (infoBean.exhaustTime).toString(16)
        exhaustTime = ByteHexHelper.convertHexStringNumBits(exhaustTime, 4)

        val sendOrder =
            OrderMontageForLeak.instance.createTestOnOrOffOrder(
                testState,
                preValue,
                upperVol,
                lowVol,
                fillTime,
                stableTime,
                checkTime,
                exhaustTime
            )
                ?: return
        if (sendOrder.isEmpty()) {
            return
        }
        commonSetCommand(iPhysics, sendOrder)
    }

    fun ackStateSwitch(iPhysics: IPhysics, data: String) {
        val sendOrder =
            OrderMontageForLeak.instance.createStatusAckOrder(data) ?: return
        if (sendOrder.isEmpty()) {
            return
        }
        commonSetCommand(iPhysics, sendOrder)
    }

    fun sendHeartBeatOrder(iPhysics: IPhysics) {
        val sendOrder =
            OrderMontageForLeak.instance.createHeartBeatOrder() ?: return
        if (sendOrder.isEmpty()) {
            return
        }
        commonSetCommand(iPhysics, sendOrder)
    }

    fun sendExhaustOrder(iPhysics: IPhysics, data: String) {
        val sendOrder =
            OrderMontageForLeak.instance.createExhaustOrder(data) ?: return
        if (sendOrder.isEmpty()) {
            return
        }
        commonSetCommand(iPhysics, sendOrder)
    }

    fun sendReInflationOrder(iPhysics: IPhysics, type: String = "00") {
        val sendOrder =
            OrderMontageForLeak.instance.createReInflationOrder(type) ?: return
        if (sendOrder.isEmpty()) {
            return
        }
        commonSetCommand(iPhysics, sendOrder)
    }

    fun ackUpdateOrder(iPhysics: IPhysics, data: String) {
        val sendOrder =
            OrderMontageForLeak.instance.ackUpdateOrder(data) ?: return
        if (sendOrder.isEmpty()) {
            return
        }
        commonSetCommand(iPhysics, sendOrder)
    }

    private fun SnToHex(str: String): String {
        // 将字符串转换为字节数组
        val bytes = str.toByteArray()
        // 将字节数组转换为16进制字符串
        val hexString = bytes.joinToString("") { "%02X".format(it) }
        // 如果字符串长度不足14个字节，补充0
        val padding = "0".repeat((14 - bytes.size) * 2)
        LogUtil.i("kevin", "hexString:$hexString=========padding:$padding")
        return hexString + padding
    }

    fun sendSerialNoOrder(iPhysics: IPhysics, serialNo: String) {
        val hexSn = SnToHex(serialNo)
        val sendOrder =
            OrderMontageForLeak.instance.createSerialNumOrder(hexSn)
        if (sendOrder.isEmpty()) {
            return
        }
        commonSetCommand(iPhysics, sendOrder)
    }

    fun clearAdcOrder(iPhysics: IPhysics) {
        val sendOrder =
            OrderMontageForLeak.instance.createClearAdcOrder()
        if (sendOrder.isEmpty()) {
            return
        }
        commonSetCommand(iPhysics, sendOrder)
    }

    /**
     * 发送升级预备帧-新
     */
    suspend fun updatePrepFrame(
        iPhysics: IPhysics,
        bean: UpdateInfoBean
    ): String? {
        val size = bean.size!!
        var fileSize = ByteHexHelper.packLength(size)
        fileSize = ByteHexHelper.convertHexStringNumBits(fileSize, 8)

        var checksum = bean.checkSum!!.toString(16)
        checksum = ByteHexHelper.convertHexStringNumBits(checksum, 8)

        var version: String = bean.version!!
        if (version.contains("V")) {
            version = version.replace("V", "")
        }
        if (version.contains("v")) {
            version = version.replace("v", "")
        }
        val tempVer = version.split(".")

        val verSb = StringBuffer()
        for (ver in tempVer) {
            var temp = ver.toInt().toString(16)
            temp = ByteHexHelper.convertHexStringNumBits(temp, 2)
            verSb.append(temp)
        }
        val totalPackage =
            if ((size % ConstantsUtils.PACKAGE_SIZE) == 0L) size / ConstantsUtils.PACKAGE_SIZE else (size / ConstantsUtils.PACKAGE_SIZE) + 1
        var packageSize = Integer.toHexString(totalPackage.toInt())
        packageSize = ByteHexHelper.convertHexStringNumBits(packageSize, 2)

        var hardWareVer = bean.hardwareVer?.toInt()?.toString(16)
        hardWareVer = ByteHexHelper.convertHexStringNumBits(hardWareVer, 2)
        val sendOrder =
            OrderMontageForLeak.instance.updatePrepOrder(
                fileSize,
                checksum,
                verSb.toString(),
                packageSize,
                if (hardWareVer.isNullOrEmpty()) "00" else hardWareVer
            ) ?: return null
        var flag = 0
        var backOrder: String
        while (flag < 3) {
            writeDPUCommand(
                sendOrder, iPhysics, TIMEOUT
            )
            backOrder = iPhysics.command
            if (TextUtils.isEmpty(backOrder)) {
                flag++
                LogUtil.e("orderThread", "-------预备帧超时重发------")
                continue
            }
            val receiveOrder = ByteHexHelper.hexStringToBytes(backOrder)
            val receiveData = AnalysisLeakData.instance.analysisOperateData(receiveOrder)
            if (receiveData.state) {
                receiveData.receiveBuffer?.let {
                    if (it.isNotEmpty()) {
                        return ByteHexHelper.byteToHexString(it[0])
                    }
                }
            } else {
                flag++
            }
        }
        return null
    }

    /**
     * 发送升级文件的每包数据
     */
    suspend fun updateFirmwareBinFrame(
        iPhysics: IPhysics,
        counter: Int,
        upData: String
    ): String? {
        var temp = counter.toString(16)
        temp = ByteHexHelper.convertHexStringNumBits(temp, 2)

        val sendOrder =
            OrderMontageForLeak.instance.updateDataOrder(temp, upData)
                ?: return null
        var flag = 0
        var backOrder: String
        while (flag < 3) {
            writeDPUCommand(
                sendOrder, iPhysics, TIMEOUT
            )
            backOrder = iPhysics.command
            if (TextUtils.isEmpty(backOrder)) {
                flag++
                LogUtil.e("orderThread", "-------升级数据帧超时重发------")
                continue
            }
            val receiveOrder = ByteHexHelper.hexStringToBytes(backOrder)
            val receiveData = AnalysisLeakData.instance.analysisOperateData(receiveOrder)
            if (receiveData.state) {
                receiveData.receiveBuffer?.let {
                    if (it.size > 1) {
                        if (ByteHexHelper.byteToHexString(it[0]) != temp) {
                            LogUtil.e("orderThread", "-------当前包数不对，重发------")
                            flag++
                        } else {
                            return ByteHexHelper.byteToHexString(it[1])
                        }
                    }
                }
            } else {
                flag++
            }
        }
        return null
    }

    fun getRs485InOrder(iPhysics: IPhysics) {
        val sendOrder =
            OrderMontageForLeak.instance.createRs485InOrder() ?: return
        if (sendOrder.isEmpty()) {
            return
        }
        commonSetCommand(iPhysics, sendOrder)
    }

    /**
     * @param sendOrder
     * @param iPhysics
     * @param maxWaitTime 为发送指令后的最大等待时间
     */
    private fun writeDPUCommand(sendOrder: ByteArray, iPhysics: IPhysics, maxWaitTime: Int) {
        try {
            iPhysics.command_wait = true
            iPhysics.setCommand("", true)
            val outputStream = iPhysics.outputStream
            outputStream.write(sendOrder)
            val milliSeconds = Date().time
            while (!waitCommand(
                    iPhysics,
                    milliSeconds,
                    maxWaitTime
                )
            ) {
                //Verify that the serial number is correct
                val backOrder = iPhysics.command
                if (!TextUtils.isEmpty(backOrder)) {
                    val request = ByteHexHelper.hexStringToBytes(backOrder)
                    if (sendOrder[2] == request[2] && request[3] == 0x80.toByte()) {
                        break
                    }
                }
                iPhysics.command_wait = true
                iPhysics.setCommand("", true)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            iPhysics.command_wait = false
            iPhysics.setCommand("", true)
            LiveEventBus.get(LiveEventBusConstants.SERIAL_NEED_CONNECT).post(e)
        }
    }

    /**
     * 等待当前命令
     *
     * @param iPhysics
     * @param startMilliseconds
     * @param maxWaitTime
     * @return
     */
    private fun waitCommand(
        iPhysics: IPhysics,
        startMilliseconds: Long,
        maxWaitTime: Int
    ): Boolean {
        var isTimeOut = false
        while (iPhysics.command_wait) {
            if (Date().time - startMilliseconds > maxWaitTime) {
                iPhysics.command_wait = false
                iPhysics.setCommand("", true)
                isTimeOut = true
                break
            }
            try {
                Thread.sleep(10)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
        return isTimeOut
    }
}