package com.newchip.tool.leaktest.ui.factory

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.cnlaunch.physics.serialport.util.AnalysisLeakData
import com.google.gson.Gson
import com.jeremyliao.liveeventbus.LiveEventBus
import com.newchip.tool.leaktest.R
import com.newchip.tool.leaktest.ui.setting.SettingViewModel
import com.newchip.tool.leaktest.utils.AnalysisTools
import com.newchip.tool.leaktest.utils.DeviceConnectUtils
import com.newchip.tool.leaktest.utils.LeakSerialOrderUtils
import com.power.baseproject.bean.BackOrderInfo
import com.power.baseproject.bean.ConfigLeakInfoBean
import com.power.baseproject.bean.DeviceInfoBean
import com.power.baseproject.bean.LeakDataBean
import com.power.baseproject.bean.TestOnOrOffResult
import com.power.baseproject.ktbase.model.BaseViewModel
import com.power.baseproject.ktbase.model.MainRepo
import com.power.baseproject.utils.ByteHexHelper
import com.power.baseproject.utils.ConstantsUtils
import com.power.baseproject.utils.EasyPreferences
import com.power.baseproject.utils.LiveEventBusConstants
import com.power.baseproject.utils.log.LogUtil
import com.power.insulationtester.utils.bean.UpdateDownloadbinData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols

class AgingViewModel(val repo: MainRepo) : BaseViewModel() {
    val leakDataLivedata = MutableLiveData<LeakDataBean>()//实时气压值回调
    val testSwitchLivedata = MutableLiveData<TestOnOrOffResult>()//测试启停结果回调
    val exhaustLivedata = MutableLiveData<TestOnOrOffResult>()//测试启停结果回调

    var downloadStateLiveData = MutableLiveData<UpdateDownloadbinData>()
    var deviceInfoLiveData = MutableLiveData<DeviceInfoBean>()
    var stateSwitchLiveData = MutableLiveData<String>()
    var needStartUpdate = false
    var printTempFlag = 0
    val symbols = DecimalFormatSymbols().apply {
        decimalSeparator = '.'
    }

    fun init() {
    }

    private val resistanceDfOne = DecimalFormat("00.00").apply {
        roundingMode = RoundingMode.HALF_UP
        isGroupingUsed = false
        decimalFormatSymbols = symbols
    }

    private val resistanceDfTwo = DecimalFormat("000.0").apply {
        roundingMode = RoundingMode.HALF_UP
        isGroupingUsed = false
        decimalFormatSymbols = symbols
    }

    private val resistanceDfThree = DecimalFormat("0000").apply {
        roundingMode = RoundingMode.HALF_UP
        isGroupingUsed = false
        decimalFormatSymbols = symbols
    }

    private val resistanceDf = DecimalFormat().apply {
        roundingMode = RoundingMode.HALF_UP
        isGroupingUsed = false
        decimalFormatSymbols = symbols
    }

    fun showValue(value: Float, unit: String): String {
        return when (unit) {
            ConstantsUtils.PA -> {
                if (value > 9999) value.toInt().toString() else resistanceDfThree.format(value)
            }

            ConstantsUtils.KPA -> {
                when {
                    value < 1 && value > 0 -> {
                        resistanceDf.maximumFractionDigits = 3
                        resistanceDf.minimumFractionDigits = 3
                        resistanceDf.format(value)
                    }

                    value >= 100 -> resistanceDfTwo.format(value)

                    else -> resistanceDfOne.format(value)
                }
            }

            ConstantsUtils.PSI -> {
                if (value == 0f) {
                    "0.00000"
                } else {
                    resistanceDf.maximumFractionDigits = 5
                    resistanceDf.minimumFractionDigits = 5
                    resistanceDf.format(value)
                }
            }

            else -> resistanceDfOne.format(value)
        }
    }

    fun showTextSize(context: Context, value: String): Float {
        return when (value.replace(".", "").length) {
            in 0..4 -> context.resources.getDimension(R.dimen.sp_28)
            5 -> context.resources.getDimension(R.dimen.sp_24)
            6 -> context.resources.getDimension(R.dimen.sp_20)
            else -> context.resources.getDimension(R.dimen.sp_16)
        }
    }

    fun dealCmdOrder(backOrder: String) {
        val receiveOrder = ByteHexHelper.hexStringToBytes(backOrder)
        val receiveData = AnalysisLeakData.instance.analysisOperateData(receiveOrder)
        if (!receiveData.state) {
            return
        }
        when (receiveData.funcType) {
            "01" -> {
                val bean = AnalysisTools.analysisDeviceInfo(receiveData.receiveBuffer)
                if (bean.result) {
                    val info = bean.data
                    LogUtil.e(ConstantsUtils.printLog, "设备信息:$info")
                    deviceInfoLiveData.postValue(info!!)
                }
            }

            "02" -> {//采集卡启动/停止命令
                val bean =
                    AnalysisTools.analysisTestSwitchResult(receiveData.receiveBuffer)
                if (bean.result) {
                    testSwitchLivedata.postValue(bean.data)
                }
            }

            "03" -> {//排气指令
                val bean =
                    AnalysisTools.analysisExhaustResult(receiveData.receiveBuffer)
                if (bean.result) {
                    exhaustLivedata.postValue(bean.data)
                }
            }

            "10" -> {
                val bean = AnalysisTools.analysisLeakData(receiveData.receiveBuffer)
                if (bean.result) {
                    leakDataLivedata.postValue(bean.data)
                }
                printTempFlag++
                if (printTempFlag > 9) {
                    LogUtil.d(ConstantsUtils.printLog, "接收指令 :$backOrder")
                    printTempFlag = 0
                }
            }

            "11" -> {
                //心跳包回复
                val bean = AnalysisTools.analysisHeartbeat(receiveData.receiveBuffer)
                if (bean.result) {
                    LogUtil.d("heartLog", "心跳包回复：${bean.data}")
                }
            }

            "12" -> {
                val bean = AnalysisTools.analysisSwitchState(receiveData.receiveBuffer)
                LogUtil.e("kevin", "------------切换状态:$bean")
                if (bean.result) {
                    stateSwitchLiveData.postValue(bean.data)
                    //状态切换应答
                    ackStateSwitch(bean.data)
                }
            }

            "f0" -> {
                receiveData.receiveBuffer?.let { it1 ->
                    if (it1.isNotEmpty()) {
                        when (com.cnlaunch.physics.utils.ByteHexHelper.byteToHexString(
                            it1[0]
                        )) {
                            "01" -> needStartUpdate = true
                            else -> {

                            }
                        }
                    }
                }
            }

            "f3" -> {
                val bean = AnalysisTools.analysisDeviceInfo(receiveData.receiveBuffer)
                if (bean.result) {
                    val info: DeviceInfoBean? = bean.data
                    LogUtil.i(
                        SettingViewModel.TAG,
                        "升级成功，返回固件信息-----info:${info}"
                    )

                    val updateBean = UpdateDownloadbinData(1)
                    updateBean.message = "固件升级成功！"
                    downloadStateLiveData.postValue(updateBean)
                }
            }

            "e0" -> {
                val result = AnalysisTools.analysisSnOrder(receiveData.receiveBuffer)
                LiveEventBus.get(LiveEventBusConstants.SET_SERIAL_NUM_RESULT)
                    .post(result)
            }
        }
    }

    fun analysisDataForReceiver(owner: LifecycleOwner) {
        viewModelScope.launch(Dispatchers.IO) {
            LiveEventBus.get(
                LiveEventBusConstants.SERIAL_PORT_RECEIVER_DATA,
                BackOrderInfo::class.java
            )
                .observe(owner) {
                    dealCmdOrder(it.cmd)
                }
        }
    }

    /**
     * 获取设备信息
     */
    fun getDeviceInfo() {
        val mCurrentDevice = DeviceConnectUtils.instance.getCurrentDevice() ?: return
        LeakSerialOrderUtils.getDeviceInfoOrder(mCurrentDevice)
    }

    /**
     * 开启/关闭测试
     */
    fun testSwitch(testState: Boolean, infoBean: ConfigLeakInfoBean) {
        val mCurrentDevice = DeviceConnectUtils.instance.getCurrentDevice() ?: return
        LeakSerialOrderUtils.testOnOrOffOrder(mCurrentDevice, testState, infoBean)
    }

    fun ackStateSwitch(testState: String?) {
        val mCurrentDevice = DeviceConnectUtils.instance.getCurrentDevice() ?: return
        if (testState == null) {
            return
        }
        LeakSerialOrderUtils.ackStateSwitch(mCurrentDevice, testState)
    }

    fun sendExhaustOrder(data: String) {
        val mCurrentDevice = DeviceConnectUtils.instance.getCurrentDevice() ?: return
        LeakSerialOrderUtils.sendExhaustOrder(mCurrentDevice, data)
    }

    fun getConfigData(): ConfigLeakInfoBean {
        val json = EasyPreferences.instance[ConstantsUtils.LEAK_CONFIG_KEY]
        if (!json.isNullOrEmpty()) {
            return Gson().fromJson(json, ConfigLeakInfoBean::class.java)
        }
        val bean = ConfigLeakInfoBean()
        bean.testMode = 0
        return bean
    }
}