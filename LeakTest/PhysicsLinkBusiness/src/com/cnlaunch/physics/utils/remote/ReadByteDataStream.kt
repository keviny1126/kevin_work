package com.cnlaunch.physics.utils.remote

import com.cnlaunch.physics.impl.IPhysics
import com.cnlaunch.physics.utils.ByteHexHelper
import com.cnlaunch.physics.utils.MLog
import com.power.baseproject.utils.log.LogUtil
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

/**
 * 连接到设备接收数据
 */
open class ReadByteDataStream(
    private val mIPhysics: IPhysics?,
    private val mInStream: InputStream,
    private val mOutStream: OutputStream
) : Runnable {
    @JvmField
    var bytes = 0
    protected val bufferSize = 1024 * 5 //5120,返回包最大数据帧长度不超过5500，所以定为5120;

    @JvmField
    var buffer: ByteArray = ByteArray(bufferSize)

    @JvmField
    var totalBuffer: ByteArray

    @JvmField
    var maxbufferSize: Int = bufferSize * 2
    var mIsNotExit: Boolean
    private val mDeviceDataProcessor: DeviceDataProcessor?

    /**
     * 清除数据缓存空间
     */
    open fun clearTotalBuffer() {
        mDeviceDataProcessor?.clearTotalBuffer()
    }

    override fun run() {
        if (mIPhysics == null) {
            return
        }
        dataStreamProcess()
    }

    /**
     * 输入数据流处理逻辑
     */
    fun dataStreamProcess() {
        bytes = 0
        while (mIsNotExit) {
            try {
                bytes = mInStream.read(buffer)
                LogUtil.e(
                    TAG,
                    "ReadByteDataStream.run(). buffer=" + ByteHexHelper.bytesToHexStringWithSearchTable(
                        buffer,
                        0,
                        bytes
                    )
                )
                if (bytes > 0) {
                    mDeviceDataProcessor!!.dataItemProcess()
                } else {
                    if (bytes != 0) {
                        break
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
                // 诊断日志写入此处的蓝牙通讯异常信息可能会给诊断工程师排错带来影响，去掉此处信息
                if (MLog.isDebug) MLog.d(TAG, "read data error" + e.message)
                break
            } catch (e1: Exception) {
                e1.printStackTrace()
                break
            }
        }
        try {
            mIPhysics!!.command_wait = false
            mIPhysics.command = ""
        } catch (ex1: Exception) {
            ex1.printStackTrace()
        }
    }

    open fun cancel() {
        try {
            if (MLog.isDebug) {
                MLog.d(TAG, "cancel()")
            }
            mIsNotExit = false
            mInStream.close()
            mOutStream.close()
            mDeviceDataProcessor?.cancel()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        private val TAG = ReadByteDataStream::class.java.simpleName
    }

    init {
        totalBuffer = ByteArray(maxbufferSize)
        mIsNotExit = true
        mDeviceDataProcessor =
            DeviceDataProcessor(this, mIPhysics!!, mInStream, mOutStream)
    }
}