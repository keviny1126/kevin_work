package com.newchip.tool.leaktest.utils

import android.annotation.SuppressLint
import android.content.Context
import com.power.baseproject.utils.ByteHexHelper
import com.power.baseproject.utils.ConstantsUtils
import com.power.baseproject.utils.EasyPreferences
import com.power.baseproject.utils.Tools
import com.power.baseproject.utils.log.LogUtil
import com.ss.core.IOChannel
import com.ss.core.api.ForwardClient
import com.ss.core.api.ForwardServer
import com.ss.protocol.bean.request.CmdReq
import com.ss.protocol.frame.BaseFrame

class ForwardUtils private constructor() {
    var needInit = true
    var connected = false
    var client = false

    companion object {
        const val DEVICE_MODEL = "rk3326"
        const val TAG = "ForwardUtils"
        const val CONNECT = 1
        const val DISCONNECT = 2
        const val RECEIVE_DATA = 3
        const val FORWARD_DELAY = 1000// 延迟
        const val FORWARD_COUNT = 10// 设备个数
        const val FORWARD_PORT = 33333
        val instance: ForwardUtils by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            ForwardUtils()
        }
    }

    init {
//        client = !android.os.Build.MODEL.contains(DEVICE_MODEL, true)
        LogUtil.i(TAG, "-------------DEVICE：${android.os.Build.MODEL}")
    }

    fun initForward(context: Context, block: (type: Int, msg: String) -> Unit) {
        if (client) resetAndStartForwardClient(context, block) else resetAndStartForwardServer(
            context,
            block
        )
    }

    private fun resetAndStartForwardServer(
        context: Context,
        block: (type: Int, msg: String) -> Unit
    ) {
        val serialNum = EasyPreferences.instance[ConstantsUtils.DEVICE_NUMBER]
        LogUtil.d(TAG, "ForwardServer ----------- serialNum:$serialNum")
        if (serialNum.isNullOrEmpty()) {
            return
        }
        needInit = false
        ForwardServer.getInstance().reset()
        ForwardServer.getInstance().addListener(object : ForwardServer.Listener {
            override fun onStarted() {
                LogUtil.i(TAG, "ForwardServer ----------- onStarted")
            }

            override fun onStopped() {
                LogUtil.i(TAG, "ForwardServer ----------- onStopped")
            }

            override fun onConnected() {
                LogUtil.d(TAG, "ForwardServer ----------- onConnected")
                connected = true
                block(CONNECT, "")
            }

            override fun onDisconnected() {
                LogUtil.e(TAG, "ForwardServer ----------- onDisconnected")
                connected = false
                block(DISCONNECT, "")
            }

            override fun onError(code: Int, tr: Throwable?) {
                LogUtil.e(TAG, "ForwardServer ----------- onError:$code")
            }

            override fun onReceive(data: ByteArray?, offset: Int, length: Int) {
                LogUtil.i(
                    TAG,
                    "ForwardServer ----------- onReceive:${ByteHexHelper.bytesToHexString(data)}"
                )
            }
        })

        ForwardServer.getForwardChannel().addListener(object : IOChannel.Listener {
            override fun stateChange(p0: Int) {
            }

            override fun receive(p0: BaseFrame) {
                kotlin.runCatching {
                    val data = CmdReq(p0.bytes).also { LogUtil.i(TAG, it.index.toString()) }.extData
                    val cmd = ByteHexHelper.bytesToHexString(data)
                    LogUtil.i(TAG, "当前下发指令：$cmd")
                    //透传给下位机
                    block(RECEIVE_DATA, cmd)
                }.onFailure {
                    LogUtil.e(TAG, "onFailure" + it.stackTraceToString())
                }
            }
        })
        val packageName: String =
            Tools.getCurrentPackageName(context) ?: "com.newchip.tool.leaktest"

        ForwardServer.getInstance()
            .start(
                ByteHexHelper.hexStringToBytes(serialNum), packageName,
                FORWARD_PORT
            )
    }

    private fun resetAndStartForwardClient(
        context: Context,
        block: (type: Int, msg: String) -> Unit
    ) {
        needInit = false
        val packageName: String =
            Tools.getCurrentPackageName(context) ?: "com.newchip.tool.leaktest"
        ForwardClient.getInstance().reset()
        ForwardClient.getInstance().addListener(object : ForwardClient.Listener {
            override fun onConnected(forwardDevice: ForwardClient.ForwardDevice) {
                LogUtil.d(TAG, "ForwardClient ----------- onConnected")
                connected = true
                block(CONNECT, "")
            }

            override fun onDisconnected(forwardDevice: ForwardClient.ForwardDevice) {
                LogUtil.e(TAG, "ForwardClient ----------- onDisconnected")
                connected = false
                ForwardClient.getInstance().disconnect()
                block(DISCONNECT, "")
            }

            override fun onError(code: Int, tr: Throwable) {
                LogUtil.e(TAG, "ForwardClient ----------- onError:$code")
            }

            override fun onReceive(data: ByteArray, offset: Int, length: Int) {


            }
        })
        ForwardClient.getForwardChannel().addListener(object : IOChannel.Listener {
            override fun stateChange(p0: Int) {
            }

            override fun receive(p0: BaseFrame) {
                kotlin.runCatching {
                    val data = CmdReq(p0.bytes).also { LogUtil.i(TAG, it.index.toString()) }.extData
                    val cmd = ByteHexHelper.bytesToHexString(data)
                    LogUtil.i(TAG, "当前接收指令：$cmd")
                    //接收被控端指令，处理指令
                    block(RECEIVE_DATA, cmd)
                }
            }

        })
        ForwardClient.getInstance()
            .scan(packageName, FORWARD_DELAY, FORWARD_COUNT, object : ForwardClient.ScanCallBack {
                override fun onScanResult(forwardDevice: ForwardClient.ForwardDevice) {
                    LogUtil.i(
                        TAG,
                        "ForwardClient onScanResult : ${forwardDevice.host}-${forwardDevice.port}-${forwardDevice.serialNo}"
                    )

                }

                @SuppressLint("SuspiciousIndentation")
                override fun onBatchScanResults(results: List<ForwardClient.ForwardDevice>) {
                    for (fd: ForwardClient.ForwardDevice in results) {
                        LogUtil.i(
                            TAG,
                            "ForwardClient onBatchScanResults : ${fd.host}-${fd.port}-${fd.serialNo}"
                        )
//                        if (fd.port == 12121) {
//                            ForwardClient.getInstance().connect(fd)
//                        }
                    }
                }

                override fun onScanFailed() {}
                override fun onScanCompletion() {}
            })
    }

    fun sendCmd(cmd: String) {
        LogUtil.d(TAG, "透传指令cmd===$cmd ,当前连接状态:$connected")
        if (!connected) {
            return
        }
        val sendOrder = ByteHexHelper.hexStringToBytes(cmd)
        val cmdReq = CmdReq(ByteArray(0), ByteArray(0), 0, ByteArray(0), sendOrder)
        if (client) {
            ForwardClient.getForwardChannel().send(cmdReq)
            return
        }
        ForwardServer.getForwardChannel().send(cmdReq)
    }
}