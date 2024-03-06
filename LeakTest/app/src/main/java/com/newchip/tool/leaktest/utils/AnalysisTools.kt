package com.newchip.tool.leaktest.utils

import com.cnlaunch.physics.utils.ByteHexHelper
import com.power.baseproject.bean.CmdBackResult
import com.power.baseproject.bean.DeviceInfoBean
import com.power.baseproject.bean.LeakDataBean
import com.power.baseproject.bean.TestOnOrOffResult
import kotlin.experimental.and

object AnalysisTools {
    const val TAG = "AnalysisTools"

    /**
     * 获取采集卡设备信息命令 function 01 ; f03;
     */
    fun analysisDeviceInfo(receiveBuffer: ByteArray?): CmdBackResult<DeviceInfoBean> {
        val cmdBackResult = CmdBackResult<DeviceInfoBean>(false)
        receiveBuffer?.let {
            val bean = DeviceInfoBean()
            if (it.size >= 16) {
                val arrayName = arrayListOf<Byte>()
                for (i in 0..15) {
                    if (it[i] == 0x00.toByte()) {
                        //过滤空值
                        continue
                    }
                    arrayName.add(it[i])
                }

                bean.deviceName = String(arrayName.toByteArray())

                if (it.size >= 30) {
                    val arrayNum = arrayListOf<Byte>()
                    for (i in 0..13) {
                        if (it[i + 16] == 0x00.toByte()) {
                            //过滤空值
                            continue
                        }
                        arrayNum.add(it[i + 16])
                    }
                    bean.deviceNum = String(arrayNum.toByteArray())
                    if (it.size >= 34) {
                        cmdBackResult.result = true
                        val sbVersion = StringBuffer()
                        sbVersion.append(it[32] and 0xff.toByte())
                            .append(".").append(it[31] and 0xff.toByte())
                            .append(".").append(it[30] and 0xff.toByte())

                        bean.softVersion = sbVersion.toString()
                        bean.hardwareVersion = it[33].toInt().toString()
                        if (it.size >= 48) {
                            val arraySn = arrayListOf<Byte>()
                            for (i in 34 until 48) {
                                if (it[i] == 0x00.toByte()) {
                                    //过滤空值
                                    continue
                                }
                                arraySn.add(it[i])
                            }
                            bean.deviceSn = String(arraySn.toByteArray())
                        }
                    }

                }
                cmdBackResult.data = bean
            }
        }
        return cmdBackResult
    }

    /**
     * 采集卡启动/停止命令 function 02;
     */
    fun analysisTestSwitchResult(receiveBuffer: ByteArray?): CmdBackResult<TestOnOrOffResult> {
        val cmdBackResult = CmdBackResult<TestOnOrOffResult>(false)
        receiveBuffer?.let {
            val bean = TestOnOrOffResult()
            if (it.isNotEmpty()) {
                bean.handleType = ByteHexHelper.byteToHexString(it[0])
                if (it.size > 4) {
                    val dataArray = byteArrayOf(it[4], it[3], it[2], it[1])
                    bean.currentPa = ByteHexHelper.intPackLength(dataArray)
                    if (it.size > 5) {
                        cmdBackResult.result = true
                        bean.ackResult = ByteHexHelper.byteToHexString(it[5])
                    }
                }
                cmdBackResult.data = bean
            }
        }
        return cmdBackResult
    }

    /**
     * 排气命令 function 03;
     */
    fun analysisExhaustResult(receiveBuffer: ByteArray?): CmdBackResult<TestOnOrOffResult> {
        val cmdBackResult = CmdBackResult<TestOnOrOffResult>(false)
        receiveBuffer?.let {
            val bean = TestOnOrOffResult()
            if (it.size > 1) {
                cmdBackResult.result = true
                bean.handleType = ByteHexHelper.byteToHexString(it[0])
                bean.ackResult = ByteHexHelper.byteToHexString(it[1])
                cmdBackResult.data = bean
            }
        }
        return cmdBackResult
    }

    /**
     * 补气命令 function 04;
     */
    fun analysisReInflationResult(receiveBuffer: ByteArray?): CmdBackResult<Boolean> {
        val cmdBackResult = CmdBackResult<Boolean>(false)
        receiveBuffer?.let {
            if (it.isNotEmpty()) {
                cmdBackResult.result = true
                cmdBackResult.data = (it[0].toInt() == 0x01)
            }
        }
        return cmdBackResult
    }

    /**
     * 采集卡主动上传数据指令 function 10
     */
    fun analysisLeakData(receiveBuffer: ByteArray?): CmdBackResult<LeakDataBean> {
        val cmdBackResult = CmdBackResult<LeakDataBean>(false)
        receiveBuffer?.let {
            val bean = LeakDataBean()
            if (it.isNotEmpty()) {
                val state = ByteHexHelper.byteToHexString(it[0])
                bean.currentState = state
                if (it.size >= 5) {
                    cmdBackResult.result = true
                    val dataArray = byteArrayOf(it[4], it[3], it[2], it[1])
                    val data = ByteHexHelper.bytesToHexString(dataArray)
                    bean.currentPa =
                        ((data.toLong(16) xor 0x80000000) - 0x80000000).toInt()//ByteHexHelper.intPackLength(dataArray)
                    if (it.size >= 9) {
                        val countArray = byteArrayOf(it[8], it[7], it[6], it[5])
                        bean.currentTimeCount = ByteHexHelper.intPackLength(countArray)
                        if (it.size >= 10) {
                            bean.valVeState = ByteHexHelper.byteToHexString(it[9])
                        }
                    }
                }
                cmdBackResult.data = bean
            }
        }
        return cmdBackResult
    }

    /**
     *  实时状态包命令,心跳包 function 11
     *  0x00： 空闲状态
     *  0x01： 准备状态
     *  0x02： 充气状态
     *  0x03： 稳定状态
     *  0x04： 检测状态
     *  0x05： 排气状态
     */
    fun analysisHeartbeat(receiveBuffer: ByteArray?): CmdBackResult<String> {
        val cmdBackResult = CmdBackResult<String>(false)
        receiveBuffer?.let {
            if (it.isNotEmpty()) {
                val state = ByteHexHelper.byteToHexString(it[0])
                cmdBackResult.data = state
            }
        }
        return cmdBackResult
    }

    /**
     *  设备状态状态切换命令 function 12
     *  0x00： 空闲状态
     *  0x01： 准备状态
     *  0x02： 充气状态
     *  0x03： 稳定状态
     *  0x04： 检测状态
     *  0x05： 排气状态
     */
    fun analysisSwitchState(receiveBuffer: ByteArray?): CmdBackResult<String> {
        val cmdBackResult = CmdBackResult<String>(false)
        receiveBuffer?.let {
            if (it.isNotEmpty()) {
                val state = ByteHexHelper.byteToHexString(it[0])
                cmdBackResult.result = true
                cmdBackResult.data = state
            }
        }
        return cmdBackResult
    }

    fun analysisClearAdc(receiveBuffer: ByteArray?): CmdBackResult<String> {
        val cmdBackResult = CmdBackResult<String>(false)
        receiveBuffer?.let {
            if (it.isNotEmpty()) {
                val state = ByteHexHelper.byteToHexString(it[0])
                if (state == "01") {
                    cmdBackResult.result = true
                }
            }
        }
        return cmdBackResult
    }

    fun analysisSnOrder(recData: ByteArray?): Boolean {
        var deviceSn = "00"
        var receiveSn = "00"
        var ack = 0

        if (recData != null) {
            if (recData.size >= 14) {
                val snDevice = StringBuilder()
                for (i in 0 until 14) {
                    snDevice.append(recData[i].toString())
                }
                deviceSn = snDevice.toString()

                if (recData.size >= 15) {
                    ack = recData[14].toInt()

                    if (recData.size >= 29) {
                        val snReceive = StringBuilder()
                        for (i in 15 until 29) {
                            snReceive.append(recData[i].toString())
                        }
                        receiveSn = snReceive.toString()
                    }
                }
            }
        }

        return ack == 1
    }
}