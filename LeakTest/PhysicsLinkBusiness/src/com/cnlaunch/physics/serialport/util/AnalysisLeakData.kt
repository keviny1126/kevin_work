package com.cnlaunch.physics.serialport.util

import com.cnlaunch.physics.utils.ByteHexHelper
import com.power.baseproject.utils.log.LogUtil

class AnalysisLeakData {
    companion object {
        const val TAG = "AnalysisInsulationData"
        val instance: AnalysisLeakData by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            AnalysisLeakData()
        }
    }

    /**
     * APP 发送请求连接 :  A5 A5 01 00 01 00 00 00 xx 01//
     * 设备回应发送连接成功: A5 A5 01 80 01 00 00 00 xx 01//
     */
    fun analysisOperateData(
        receiveData: ByteArray,
        sendCommand: ByteArray? = null
    ): AnalysisReceiveData {
        val analysisReceiveData = AnalysisReceiveData()
        val feedbackBagLength: Int // 返回指令的包长度
        if (receiveData.size > 2) {
            if (!ByteHexHelper.byteToHexString(receiveData[0]).equals("a5", true)
                || !ByteHexHelper.byteToHexString(receiveData[1]).equals("a5", true)
            ) {
                analysisReceiveData.state = false
                LogUtil.e(
                    TAG,
                    "analysisOperateData 数据解析返回指令 帧头校验失败 [0]:${
                        ByteHexHelper.byteToHexString(
                            receiveData[0]
                        )
                    }" +
                            ",[1]:${ByteHexHelper.byteToHexString(receiveData[1])}"
                )
                return analysisReceiveData
            }
            if (receiveData.size > 4) {
                if (sendCommand != null && sendCommand.size > 4) {
                    if (receiveData[2] != sendCommand[2]) {
                        analysisReceiveData.state = false
                        LogUtil.e(
                            TAG,
                            "analysisOperateData 数据解析返回指令 功能类型不对 [2]:${
                                ByteHexHelper.byteToHexString(
                                    receiveData[2]
                                )
                            }" +
                                    ",sendCommand[2]:${ByteHexHelper.byteToHexString(sendCommand[2])}"
                        )
                        return analysisReceiveData
                    }
                }
                if (ByteHexHelper.byteToHexString(receiveData[3]) != "80") {
                    analysisReceiveData.state = false
                    LogUtil.e(
                        TAG,
                        "analysisOperateData 数据解析返回指令 命令字校验失败 [2]:${
                            ByteHexHelper.byteToHexString(
                                receiveData[2]
                            )
                        }" +
                                ",[3]:${ByteHexHelper.byteToHexString(receiveData[3])}"
                    )
                    return analysisReceiveData
                }
                analysisReceiveData.funcType = ByteHexHelper.byteToHexString(receiveData[2])
                if (receiveData.size > 6) {
                    //高低位切换
                    val dataLen = byteArrayOf(receiveData[5], receiveData[4])
                    feedbackBagLength = ByteHexHelper.intPackLength(dataLen)
                    if (feedbackBagLength <= 0) {
                        analysisReceiveData.state = false
                        LogUtil.e(
                            TAG,
                            "analysisOperateData 数据解析返回指令 数据区数据长度 [5]:${
                                ByteHexHelper.byteToHexString(
                                    receiveData[5]
                                )
                            }" +
                                    ",[4]:${ByteHexHelper.byteToHexString(receiveData[4])}"
                        )
                        return analysisReceiveData
                    }
                    if (receiveData.size > 8) {
                        val reserve = byteArrayOf(receiveData[6], receiveData[7])
                        analysisReceiveData.reserve = reserve
                        if (receiveData.size >= 9 + feedbackBagLength) {
                            val dataBytes = ByteArray(feedbackBagLength)
                            for ((flag, i) in (9 until 9 + feedbackBagLength).withIndex()) {
                                dataBytes[flag] = receiveData[i]
                            }
                            val temp = byteArrayOf(
                                receiveData[0],
                                receiveData[1],
                                receiveData[2],
                                receiveData[3],
                                receiveData[4],
                                receiveData[5],
                                receiveData[6],
                                receiveData[7]
                            )
                            val checkBytes = ByteHexHelper.hexStringToBytes(
                                ByteHexHelper.bytesToHexString(temp) + ByteHexHelper.bytesToHexString(
                                    dataBytes
                                )
                            )
                            val add8Verify = ByteHexHelper.sumCheck(checkBytes, checkBytes.size)
                            if (add8Verify == ByteHexHelper.byteToHexString(receiveData[8])) {
                                analysisReceiveData.state = true
                                analysisReceiveData.receiveBuffer = dataBytes
                                return analysisReceiveData
                            }
                            LogUtil.i(
                                TAG,
                                "analysisOperateData add8Verify:$add8Verify=====ByteHexHelper.byteToHexString(receiveData[6]):${
                                    ByteHexHelper.byteToHexString(receiveData[8])
                                }"
                            )
                        }
                    }

                }
            }
        }
        return analysisReceiveData
    }

    /**
     * APP 发送请求连接 :  A5 A5 01 00 01 00 00 00 xx 01//
     * 设备回应发送连接成功: A5 A5 01 80 01 00 00 00 xx 01//
     */
    fun analysisFactoryOperateData(
        receiveData: ByteArray
    ): AnalysisReceiveData {
        val analysisReceiveData = AnalysisReceiveData()
        val feedbackBagLength: Int // 返回指令的包长度
        if (receiveData.size > 2) {
            if (!ByteHexHelper.byteToHexString(receiveData[0]).equals("a5", true)
                || !ByteHexHelper.byteToHexString(receiveData[1]).equals("a5", true)
            ) {
                analysisReceiveData.state = false
                return analysisReceiveData
            }
            if (receiveData.size > 4) {
                analysisReceiveData.funcType = ByteHexHelper.byteToHexString(receiveData[2])
                if (receiveData.size > 6) {
                    //高低位切换
                    val dataLen = byteArrayOf(receiveData[5], receiveData[4])
                    feedbackBagLength = ByteHexHelper.intPackLength(dataLen)
                    if (feedbackBagLength <= 0) {
                        analysisReceiveData.state = false
                        return analysisReceiveData
                    }
                    if (receiveData.size > 8) {
                        val reserve = byteArrayOf(receiveData[6], receiveData[7])
                        analysisReceiveData.reserve = reserve
                        if (receiveData.size >= 9 + feedbackBagLength) {
                            val dataBytes = ByteArray(feedbackBagLength)
                            for ((flag, i) in (9 until 9 + feedbackBagLength).withIndex()) {
                                dataBytes[flag] = receiveData[i]
                            }
                            val temp = byteArrayOf(
                                receiveData[0],
                                receiveData[1],
                                receiveData[2],
                                receiveData[3],
                                receiveData[4],
                                receiveData[5],
                                receiveData[6],
                                receiveData[7]
                            )
                            val checkBytes = ByteHexHelper.hexStringToBytes(
                                ByteHexHelper.bytesToHexString(temp) + ByteHexHelper.bytesToHexString(
                                    dataBytes
                                )
                            )
                            val add8Verify = ByteHexHelper.sumCheck(checkBytes, checkBytes.size)
                            if (add8Verify == ByteHexHelper.byteToHexString(receiveData[8])) {
                                analysisReceiveData.state = true
                                analysisReceiveData.receiveBuffer = dataBytes
                                return analysisReceiveData
                            }
                        }
                    }
                }
            }
        }
        return analysisReceiveData
    }
}