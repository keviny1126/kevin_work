package com.cnlaunch.physics.utils.remote

import com.cnlaunch.physics.impl.IPhysics
import com.cnlaunch.physics.utils.ByteHexHelper
import com.jeremyliao.liveeventbus.LiveEventBus
import com.power.baseproject.bean.BackOrderInfo
import com.power.baseproject.utils.LiveEventBusConstants
import java.io.BufferedWriter
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

class DeviceDataProcessor(
    var readByteDataStream: ReadByteDataStream,
    var mIPhysics: IPhysics,
    var mInStream: InputStream,
    var mOutStream: OutputStream
) {
    val START_CODE: ByteArray = byteArrayOf(0xA5.toByte(), 0xA5.toByte())
    var bufferedWriter: BufferedWriter? = null
    var isCommunicateTest: Boolean
    var isAvailable = false
    var totalBytes //当前totalBuffer 使用的总长度
            : Int

    fun clearTotalBuffer() {
        totalBytes = 0
    }

    fun dataItemProcess() {
        cacheData()
        totalBytes += readByteDataStream.bytes
        dataProcess()
    }

    /**
     * 缓存接收到的数据
     */
    protected fun cacheData() {
        if (totalBytes + readByteDataStream.bytes <= readByteDataStream.maxbufferSize) {
            System.arraycopy(
                readByteDataStream.buffer,
                0,
                readByteDataStream.totalBuffer,
                totalBytes,
                readByteDataStream.bytes
            )
        } else {
            totalBytes = 0
            System.arraycopy(
                readByteDataStream.buffer,
                0,
                readByteDataStream.totalBuffer,
                totalBytes,
                readByteDataStream.bytes
            )
        }
    }

    fun cancel() {
        try {
            //用于数据通讯测试 xfh2017/09/12
            if (isCommunicateTest) {
                bufferedWriter?.close()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    /**
     * 气密测试指令处理
     */
    private fun dataProcess() {
        // 验证包正确性
        isAvailable = false
        val index = ByteHexHelper.bytesHeadIndexOf(
            readByteDataStream.totalBuffer,
            START_CODE,
            0,
            totalBytes
        )
//            LogUtil.i(TAG, "-------包校验index起始位置：$index")
        if (index >= 0) {
            isAvailable = true
        }
        if (isAvailable) {
            if (index > 0) {
                val newTotalBytes = totalBytes - index
                System.arraycopy(
                    readByteDataStream.totalBuffer,
                    index,
                    readByteDataStream.totalBuffer,
                    0,
                    newTotalBytes
                )
                totalBytes = newTotalBytes
            }
//            LogUtil.i(
//                TAG,
//                "-------串口缓存--totalBytes：$totalBytes--totalBuffer：${
//                    ByteHexHelper.bytesToHexStringWithSearchTable(
//                        readByteDataStream.totalBuffer,
//                        0,
//                        totalBytes
//                    )
//                }"
//            )

            if (totalBytes >= 9) {
                val length: Int = ByteHexHelper.intPackLength(
                    byteArrayOf(
                        readByteDataStream.totalBuffer[5],
                        readByteDataStream.totalBuffer[4]
                    )
                )
//                    (readByteDataStream.totalBuffer[5] and 0xff.toByte()) * 256 + (readByteDataStream.totalBuffer[4] and 0xff.toByte())
                val totalLength = FIXED_LENGTH + length
//                LogUtil.i(
//                    TAG,
//                    "指令长度------totalLength：$totalLength ----判断是否继续: ${(totalLength in 1..totalBytes)} ----" +
//                            ",十六进制长度:${ByteHexHelper.bytesToHexString(byteArrayOf(readByteDataStream.totalBuffer[5],readByteDataStream.totalBuffer[4]))} "
//                )
                if (totalLength in 1..totalBytes) {//防止totalLength-1为负值导致崩溃
                    val receiveVerify = readByteDataStream.totalBuffer[8]
                    val checkBytes = ByteArray(totalLength - 1)
                    var flag = 0
                    for (i in 0 until totalLength) {
                        if (i != 8) {
                            checkBytes[flag] = readByteDataStream.totalBuffer[i]
                            flag++
                        }
                    }
                    val verify = ByteHexHelper.sumCheck(checkBytes, checkBytes.size)
//                    LogUtil.i(
//                        TAG,
//                        "校验------verify：$verify ----receiveVerify: ${
//                            ByteHexHelper.byteToHexString(receiveVerify)
//                        }"
//                    )
                    if (ByteHexHelper.hexStringToByte(verify) == receiveVerify) {
                        val command = ByteHexHelper.bytesToHexStringWithSearchTable(
                            readByteDataStream.totalBuffer,
                            0,
                            totalLength
                        )
                        mIPhysics.setCommand(command, true)
//                            LogUtil.d(TAG, "解析过滤后，下位机返回数据 command:$command")
                        mIPhysics.command_wait = false
                        LiveEventBus.get(LiveEventBusConstants.SERIAL_PORT_RECEIVER_DATA)
                            .post(BackOrderInfo(mIPhysics, command))
                    }
                    // 复位原状态
                    totalBytes -= totalLength
                    if (totalBytes > 0) {
                        System.arraycopy(
                            readByteDataStream.totalBuffer,
                            totalLength,
                            readByteDataStream.totalBuffer,
                            0,
                            totalBytes
                        )
//                        LogUtil.e(TAG, "剩余指令 totalBytes:$totalBytes")
                        if (totalBytes > 9) {
                            dataProcess()
                        }
                    }
                    return
                }
                if (totalLength <= 0 || totalLength >= 2000) {
                    //长度异常，移除a5a5
                    val newLengths = totalBytes - 2
                    System.arraycopy(
                        readByteDataStream.totalBuffer,
                        2,
                        readByteDataStream.totalBuffer,
                        0,
                        newLengths
                    )
                    totalBytes = newLengths
                }
            }
        }
    }

    companion object {
        const val FIXED_LENGTH = 9
        private val TAG = DeviceDataProcessor::class.java.simpleName
    }

    init {
        totalBytes = 0
        isCommunicateTest = false
    }
}