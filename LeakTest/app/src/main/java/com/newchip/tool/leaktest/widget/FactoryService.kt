package com.newchip.tool.leaktest.widget

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.WifiManager
import android.os.*
import androidx.lifecycle.Observer
import com.cnlaunch.physics.impl.IPhysics
import com.cnlaunch.physics.serialport.SerialPortManager
import com.cnlaunch.physics.serialport.util.AnalysisLeakData
import com.cnlaunch.physics.serialport.util.OrderMontageForLeak
import com.cnlaunch.physics.utils.ByteHexHelper
import com.jeremyliao.liveeventbus.LiveEventBus
import com.newchip.tool.leaktest.R
import com.newchip.tool.leaktest.ui.setting.manager.WifiConnect
import com.newchip.tool.leaktest.utils.DeviceConnectUtils
import com.newchip.tool.leaktest.utils.LeakSerialOrderUtils
import com.power.baseproject.bean.BackOrderInfo
import com.power.baseproject.utils.LiveEventBusConstants
import com.power.baseproject.utils.SoundUtils
import com.power.baseproject.utils.log.LogUtil
import kotlinx.coroutines.*

class FactoryService : Service(), CoroutineScope by CoroutineScope(
    Dispatchers.Default
) {
    private var mFactoryDevice: IPhysics? = null
    private var mDevice: IPhysics? = null
    private var heartJob: Job? = null
    private var mcuHeartJob: Job? = null

    private var needSendTempOrder = false//等待下位机返回结果
    private var controlCmd = true//控制权
    private var timeOutFlag = 0//超时控制
    var upgrading = false
    private var needSendToMcuConnectStatus = true//首次收到D0，下发给MCU，
    private var tempD0Order: String? = null//D0指令

    private val tempOrderList = mutableListOf<String>()
    private var observer: Observer<BackOrderInfo<*>>? = null
    private var wifiReceiver: WifiReceiver? = null

    companion object {
        const val TAG = "FactoryService"
        const val TAG_MCU = "FS_MCU"
        const val TAG_TOOL = "FS_TOOL"
        const val CHANNEL_ID_STRING = "FactoryService_01"
        const val DEBUG = false

        @JvmStatic
        fun startAction(context: Context) {
            val intent = Intent(context, FactoryService::class.java)
            context.startService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val builder: Notification.Builder =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val notificationManager =
                        getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                    val mChannel = NotificationChannel(
                        CHANNEL_ID_STRING,
                        "factory",
                        NotificationManager.IMPORTANCE_HIGH
                    )
                    notificationManager.createNotificationChannel(mChannel)
                    Notification.Builder(
                        this,
                        CHANNEL_ID_STRING
                    )
                } else {
                    Notification.Builder(this)
                }
            val contentIndent = PendingIntent.getActivity(
                this, 0, Intent(
                    this,
                    FactoryService::class.java
                ), PendingIntent.FLAG_UPDATE_CURRENT
            )
            builder.setContentIntent(contentIndent)
                .setWhen(System.currentTimeMillis()) //设置时间发生时间
                .setOngoing(false)
                .setSmallIcon(R.color.transparent)
                .setContentTitle("FactoryService") //设置下拉列表里的标题
            val notification = builder.build()
            startForeground(-123, notification)
        } else {
            startForeground(-123, Notification())
        }
        LogUtil.e(TAG, "---------启动工厂测试服务------")
        startFunction()
    }

    private fun startFunction() {
        launch {
            var connectToolResult = false
            var connectDeviceResult = false
            for (i in 0..3) {
                if (connectFactorySerial()) {
                    connectToolResult = true
                    break
                }
                LogUtil.e(TAG, "工装板 串口连接失败 ，等待三秒后重连")
                delay(3000)
            }
            for (i in 0..3) {
                if (connectDeviceSerial()) {
                    connectDeviceResult = true
                    break
                }
                LogUtil.e(TAG, "MCU 串口连接失败 ，等待三秒后重连")
                delay(3000)
            }
            if (connectToolResult && connectDeviceResult) {
                mFactoryDevice = DeviceConnectUtils.instance.getFactoryDevice()
                mDevice = DeviceConnectUtils.instance.getCurrentDevice()
                initThread()
                return@launch
            }
            LiveEventBus.get(LiveEventBusConstants.CLOSE_FACTORY_TOOL).post(0)
        }
    }

    private fun initThread() {
        if (mFactoryDevice == null || mDevice == null) {
            LiveEventBus.get(LiveEventBusConstants.CLOSE_FACTORY_TOOL).post(1)
            return
        }
        initHeartThread()
        observer = Observer<BackOrderInfo<*>> {
            val device = it.device
            val backOrder = it.cmd
            when (device) {
                mDevice -> dealDevice(backOrder)
                mFactoryDevice -> dealTool(backOrder)
            }
        }
        if (observer != null) {
            LiveEventBus.get(
                LiveEventBusConstants.SERIAL_PORT_RECEIVER_DATA,
                BackOrderInfo::class.java
            )
                .observeForever(observer!!)
        }
        initReceiver()
    }

    private fun dealDevice(backOrder: String) {
        if (backOrder.isNotEmpty()) {
//            LogUtil.i(TAG, "接收MCU的指令----------backorder:$backOrder")
            val receiveOrder = ByteHexHelper.hexStringToBytes(backOrder)
            val receiveData =
                AnalysisLeakData.instance.analysisOperateData(receiveOrder)
            if (receiveData.state) {
                when (receiveData.funcType) {
                    "11" -> {
                    }
                    else -> {
                        LogUtil.d(TAG_MCU, "接收MCU的指令转发给工装:$backOrder ----- RS485权限是否回收:$controlCmd")
                        if (receiveData.funcType == "d0") {
                            tempD0Order = backOrder
                        }
                        if (controlCmd) {
                            needSendTempOrder = false
                            if (tempOrderList.isNotEmpty()) {
                                for (order in tempOrderList) {
                                    LeakSerialOrderUtils.transCommand(mFactoryDevice, order)
                                }
                                tempOrderList.clear()
                            }
                            LeakSerialOrderUtils.transCommand(mFactoryDevice, backOrder)
                            return
                        }
                        needSendTempOrder = true
                        tempOrderList.add(backOrder)
                    }
                }
//                when (receiveData.funcType) {
//                    "11" -> {//心跳}
//                    "01" -> {//过去设备信息}
//                    "f0" -> {//升级预备帧}
//                    "f1" -> {//升级数据包帧}
//                    "f3" -> {//固件升级结果应答}
//                    "e0" -> {//设备SN码配置}
//                    "e1" -> {//定制SN码配置}
//                    "e2" -> {//电磁阀测试指令}
//                    "e3" -> {//气压传感器测试指令}
//                    "e4" -> {//WIFI测试指令}
//                    "e5" -> {//RTC测试指令}
//                    "e6" -> {//音响测试指令}
//                    "e8" -> {//比例阀PCA芯片测试指令}
//                    "ef" -> {//断开生产测试指令}
//                }
            }
        }
    }

    private fun dealTool(backOrder: String) {
        if (backOrder.isNotEmpty()) {
//            LogUtil.i(TAG, "接收工装板的指令 --- backorder:$backOrder")
            val receiveOrder = ByteHexHelper.hexStringToBytes(backOrder)
            val receiveData =
                AnalysisLeakData.instance.analysisFactoryOperateData(receiveOrder)
            if (receiveData.state) {
                timeOutFlag = 0//接收到工装板指令，就重置标志位
                when (receiveData.funcType) {
                    "00" -> {
//                        LogUtil.i(TAG_TOOL, "<接收工装板的指令--------RS485权限回收-------->")
                        controlCmd = true
                    }
                    "01" -> {
                        //重置D0
                        needSendToMcuConnectStatus = true
                        LogUtil.i(TAG_TOOL, "接收工装板获取设备信息的指令，转发给 MCU :$backOrder")
                        LeakSerialOrderUtils.transCommand(mDevice, backOrder)
                    }
                    "d0" -> {
                        if (needSendToMcuConnectStatus || tempD0Order.isNullOrEmpty()) {
                            needSendToMcuConnectStatus = false
                            LogUtil.i(TAG_TOOL, "接收工装板D0指令，转发给 MCU :$backOrder")
                            LeakSerialOrderUtils.transCommand(mDevice, backOrder)
                            return
                        }
                        if (!tempD0Order.isNullOrEmpty()) {
                            if (controlCmd) {
                                LeakSerialOrderUtils.transCommand(mFactoryDevice, tempD0Order!!)
                                return
                            }
                            tempOrderList.add(tempD0Order!!)
                            needSendTempOrder = true
                        }
                    }
                    "e4" -> {
                        launch {
                            LogUtil.i(TAG_TOOL, "<接收工装板的指令--------启动wifi测试-------->")
                            WifiConnect.instance.openWifi()
                            delay(500)
                            WifiConnect.instance.scanWifi()
                        }
                    }
                    "e5" -> {
                        LogUtil.i(TAG_TOOL, "<接收工装板的指令--------启动rtc测试-------->")
                        launch {
                            receiveData.receiveBuffer?.let {
                                if (it.size > 8) {
                                    val timeBytes = byteArrayOf(
                                        it[1], it[2], it[3], it[4], it[5], it[6], it[7], it[8]
                                    )
                                    val time = ByteHexHelper.LongPackLength(timeBytes)
                                    (getSystemService(Context.ALARM_SERVICE) as AlarmManager).setTime(
                                        time
                                    )
                                    val curTime = System.currentTimeMillis()

                                    var hexTime = curTime.toString(16)
                                    var len: Int = hexTime.length
                                    while (len < 16) {
                                        hexTime = "0$hexTime"
                                        len = hexTime.length
                                    }
                                    val sendOrder = ByteHexHelper.bytesToHexString(
                                        OrderMontageForLeak.instance.createRtcBackOrder(
                                            curTime - time <= 1000,
                                            hexTime
                                        )
                                    )
                                    if (controlCmd) {
                                        LeakSerialOrderUtils.transCommand(mFactoryDevice, sendOrder)
                                        return@launch
                                    }
                                    tempOrderList.add(sendOrder)
                                    needSendTempOrder = true

                                }
                            }

                        }
                    }
                    "e6" -> {
                        launch {
                            LogUtil.i(TAG_TOOL, "接收工装板的指令，转发给 MCU :$backOrder")
                            if (SoundUtils.instance.playSound(3000)) {
                                val speakerAck =
                                    OrderMontageForLeak.instance.createSpeakerAckOrder()
                                LogUtil.i(TAG_TOOL, "音响播放完毕，响应给工装板指令 :$speakerAck")
                                if (controlCmd) {
                                    LeakSerialOrderUtils.transCommand(mFactoryDevice, speakerAck)
                                    return@launch
                                }
                                tempOrderList.add(speakerAck)
                                needSendTempOrder = true
                            }
                        }
                    }
                    else -> {
                        LogUtil.i(TAG_TOOL, "接收工装板的指令，转发给 MCU :$backOrder")
                        LeakSerialOrderUtils.transCommand(mDevice, backOrder)
                    }
//                                "01" -> {//过去设备信息}
//                                "f0" -> {//升级预备帧}
//                                "f1" -> {//升级数据包帧}
//                                "f3" -> {//固件升级结果应答}
//                                "e0" -> {//设备SN码配置 }
//                                "e1" -> {//定制SN码配置}
//                                "e2" -> {//电磁阀测试指令}
//                                "e3" -> {//气压传感器测试指令}
//                                "e4" -> {//WIFI测试指令}
//                                "e5" -> {//RTC测试指令}
//                                "e6" -> {//音响测试指令}
//                                "e8" -> {//比例阀PCA芯片测试指令}
//                                "ef" -> {//断开生产测试指令}
                }
            }
        }
    }


    private fun initHeartThread() {
        launch(Dispatchers.Main) {
            heartJob?.cancel()
            heartJob = null
            mcuHeartJob?.cancel()
            mcuHeartJob = null
            delay(10)
            heartJob = startHeartbeatThread()
            heartJob?.start()
            mcuHeartJob = startMcuHeartThread()
            mcuHeartJob?.start()
        }
    }

    private suspend fun connectFactorySerial(): Boolean {
        DeviceConnectUtils.instance.closeFactoryDevice()
        val deviceManager: IPhysics? =
            DeviceConnectUtils.instance.createFactorySerialManager(this@FactoryService)
        if (deviceManager != null) {
            val serialManager =
                if (deviceManager is SerialPortManager) deviceManager else null
            LogUtil.i(TAG, "工装板 串口连接" + if (serialManager != null) "成功" else "失败")
            if (serialManager != null) {
                return true
            }
        }
        return false
    }

    private suspend fun connectDeviceSerial(): Boolean {
        DeviceConnectUtils.instance.closeCurrentDevice()
        val deviceManager: IPhysics? =
            DeviceConnectUtils.instance.createSerialManager(this@FactoryService, "806030")
        if (deviceManager != null) {
            val serialManager =
                if (deviceManager is SerialPortManager) deviceManager else null
            LogUtil.i(TAG, "设备 串口连接" + if (serialManager != null) "成功" else "失败")
            if (serialManager != null) {
                return true
            }
        }
        return false
    }

    private fun startHeartbeatThread(): Job {
        return launch(start = CoroutineStart.LAZY, context = Dispatchers.IO) {
            try {
                var tempControlFlag = 0
//                var tempWaitFlag = 0
                while (isActive) {
                    delay(250)
                    if (!controlCmd) {
                        tempControlFlag++
                        if (tempControlFlag > 3) {
                            LogUtil.e(TAG, "《----控制释放指令超时，重新获取控制权---》")
                            controlCmd = true
                        }
                        continue
                    }
                    if (needSendTempOrder && tempOrderList.isNotEmpty()) {
                        needSendTempOrder = false
                        for (order in tempOrderList) {
                            LeakSerialOrderUtils.transCommand(mFactoryDevice, order)
                        }
                        tempOrderList.clear()
                    }
                    timeOutFlag++
                    if (timeOutFlag > 4 && !DEBUG) {
                        //工装板超时未应答，退出服务
                        LogUtil.e(TAG, "《----工装板超时未应答，退出服务---》")
                        break
                    }
                    LeakSerialOrderUtils.getRs485InOrder(mFactoryDevice!!)
                    controlCmd = false
                    tempControlFlag = 0
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                LiveEventBus.get(LiveEventBusConstants.CLOSE_FACTORY_TOOL).post(2)
                LogUtil.e(TAG, "------------------------控制线程结束------------------------")
            }
        }
    }

    private fun initReceiver() {
        wifiReceiver = WifiReceiver()
        val intentFilter = IntentFilter()
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION)
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION)
        registerReceiver(wifiReceiver, intentFilter)
    }

    internal inner class WifiReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                //扫描已完成，结果可用
                WifiManager.SCAN_RESULTS_AVAILABLE_ACTION -> {
                    val list =
                        WifiConnect.instance.changeToWifiData(WifiConnect.instance.scanResultsWifi())
                    for (bean in list) {
                        LogUtil.i(TAG, "扫描已完成，结果可用：bean:$bean")
                    }
                    if (!list.isNullOrEmpty()) {
                        if (controlCmd) {
                            LeakSerialOrderUtils.sendWifiBackOrder(mFactoryDevice, list[0].signal)
                            return
                        }
                        val sendOrder =
                            OrderMontageForLeak.instance.createWifiBackOrder(
                                ByteHexHelper.intToHexString(
                                    list[0].signal
                                )
                            )
                        tempOrderList.add(ByteHexHelper.bytesToHexString(sendOrder))
                        needSendTempOrder = true
                    }
                }
                //Wi-Fi连接状态
                WifiManager.NETWORK_STATE_CHANGED_ACTION -> {

                }
                //指示Wi-Fi已启用、禁用
                WifiManager.WIFI_STATE_CHANGED_ACTION -> {

                }
            }
        }
    }

    private fun startMcuHeartThread(): Job {
        return launch(start = CoroutineStart.LAZY, context = Dispatchers.IO) {
            try {
                while (isActive) {
                    if (mDevice != null) {
                        LeakSerialOrderUtils.sendHeartBeatOrder(mDevice!!)
                    }
                    delay(1500)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onDestroy() {
        if (wifiReceiver != null) {
            unregisterReceiver(wifiReceiver)
            wifiReceiver = null
        }
        stopForeground(true)
        heartJob?.cancel()
        heartJob = null
        mcuHeartJob?.cancel()
        mcuHeartJob = null
        DeviceConnectUtils.instance.closeFactoryDevice()
//        DeviceConnectUtils.instance.closeCurrentDevice()
        if (observer != null) {
            LiveEventBus.get(
                LiveEventBusConstants.SERIAL_PORT_RECEIVER_DATA,
                BackOrderInfo::class.java
            ).removeObserver(observer!!)
        }
        (this as CoroutineScope).cancel()
        LogUtil.e(TAG, "---------工厂测试服务停止------")
        LiveEventBus.get(LiveEventBusConstants.SERVICE_STOP_INIT).post(true)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}