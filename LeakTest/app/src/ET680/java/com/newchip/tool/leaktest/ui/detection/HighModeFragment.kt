package com.newchip.tool.leaktest.ui.detection

import android.annotation.SuppressLint
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import com.jeremyliao.liveeventbus.LiveEventBus
import com.newchip.tool.leaktest.R
import com.newchip.tool.leaktest.databinding.FragmentLeaktestModeBinding
import com.newchip.tool.leaktest.ui.MainViewModel
import com.newchip.tool.leaktest.ui.setting.manager.PrintLogcatManager
import com.newchip.tool.leaktest.widget.FactoryService
import com.power.baseproject.bean.ConfigLeakInfoBean
import com.power.baseproject.bean.PointerTempDate
import com.power.baseproject.db.entity.TestData
import com.power.baseproject.ktbase.dialog.MessageDialog
import com.power.baseproject.ktbase.ui.BaseAppFragment
import com.power.baseproject.utils.*
import com.power.baseproject.utils.Tools.convertUnit
import com.power.baseproject.utils.Tools.toUnitString
import com.power.baseproject.utils.log.LogUtil
import com.power.baseproject.widget.NToast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.math.abs
import kotlin.random.Random

class HighModeFragment : BaseAppFragment<FragmentLeaktestModeBinding>() {
    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentLeaktestModeBinding.inflate(inflater, container, false)

    val vm: MainViewModel by activityViewModels()
    var msgDialog: MessageDialog? = null
    var isTesting = false
    var totalTime = 0
    var readyTime = 10//准备时间，默认为10s
    var tempPaList = mutableListOf<Int>()
    var loopQueue: ConcurrentLinkedQueue<Int>? = null
    var tempStatus: String? = null
    private lateinit var configInfo: ConfigLeakInfoBean
    private lateinit var metricConfigInfo: ConfigLeakInfoBean//转为公制配置
    private var tempCurPa = -1f
    private var tempCurMaxPa = 0f
    private var actualValue = 0f//当前真实值
    private var virtualValue = 0f//虚拟值

    private var tempDataList = mutableListOf<PointerTempDate>()
    private var stablePressureValue = 0//稳定压力值
    private var testResult: String? = null
    private var needPrintLog = false
    private var checkInflationTime = 0f
    private var tempFlag = 0
    private var FLAG_COUNTER = 2
    private var queueTempSize = 0
    private lateinit var curPressUnit: String
    private lateinit var curLeakageUnit: String

    private var upperMaxLimit = 20//超过充气上限10kpa
    private var upperLimit = 20//显示上限
    private var lowPressLimit = 3
    private var lowMinLimit = 1
    private var offsetValueLimit = 0.01f
    private var lowStabilizationValueLimit = 0.2f
    private var initLeakValue = "0000"
    private var initPressValue = "00.00"

    override fun initView() {
        setTitle(R.string.high_mode)
        refreshBtnStatusUI(false, showProgress = false)
        initClick()
        needPrintLog = EasyPreferences.instance[ConstantsUtils.LOG_SHOW_FLAG, false]
        val factoryFlag = EasyPreferences.instance[ConstantsUtils.FACTORY_SERVICE_START_FLAG, true]
        if (factoryFlag) {
            if (Tools.iServiceRunning(mContext, FactoryService::class.java.simpleName)) {
                LiveEventBus.get(LiveEventBusConstants.CLOSE_FACTORY_TOOL).post(3)
                return
            }
            LiveEventBus.get(LiveEventBusConstants.SERVICE_STOP_INIT).post(true)
        }
    }

    override fun initData() {
        super.initData()
        curPressUnit = EasyPreferences.instance[ConstantsUtils.PRESS_UNIT_TYPE, 0].toUnitString()
        curLeakageUnit =
            EasyPreferences.instance[ConstantsUtils.LEAKAGE_UNIT_TYPE, 1].toUnitString()
        configInfo = vm.getHighConfigData()
        metricConfigInfo = configInfo.convertUnitForMetric()
        configInfo = configInfo.switchUnits(curPressUnit, curLeakageUnit)
        initUI(configInfo)
        checkInflationTime = if (configInfo.inflationTime > 4) {
            readyTime + 4f
        } else {
            readyTime.toFloat() + configInfo.inflationTime / 2
        }
    }

    private fun ConfigLeakInfoBean.convertUnitForMetric(): ConfigLeakInfoBean {
        return ConfigLeakInfoBean(
            workpieceNo,
            workpieceVolume,
            ConstantsUtils.KPA.convertUnit(testPressure, pressureUnit ?: ConstantsUtils.KPA),
            ConstantsUtils.KPA.convertUnit(upperPressureLimit, pressureUnit ?: ConstantsUtils.KPA),
            ConstantsUtils.KPA.convertUnit(lowerPressureLimit, pressureUnit ?: ConstantsUtils.KPA),
            ConstantsUtils.KPA,
            ConstantsUtils.PA.convertUnit(leakageAlarm, leakageUnit ?: ConstantsUtils.PA),
            ConstantsUtils.PA,
            inflationTime,
            stabilizationTime,
            detectionTime,
            exhaustTime,
            testMode
        )
    }

    override fun needLoaddialog(): Boolean {
        return true
    }

    private fun initLimit() {
        upperLimit = curPressUnit.convertUnit(20f, ConstantsUtils.KPA).toInt()
        upperMaxLimit = curPressUnit.convertUnit(20f, ConstantsUtils.KPA).toInt()
        offsetValueLimit = curPressUnit.convertUnit(0.01f, ConstantsUtils.KPA)
        lowMinLimit = curPressUnit.convertUnit(1f, ConstantsUtils.KPA).toInt()
        lowPressLimit = curPressUnit.convertUnit(3f, ConstantsUtils.KPA).toInt()
        lowStabilizationValueLimit = curPressUnit.convertUnit(0.2f, ConstantsUtils.KPA)
    }

    private fun initUI(info: ConfigLeakInfoBean) {
        LogUtil.i("kevin", "----getConfigInfo:$info")
        initLimit()
        mVb.pressureUnit.text = "($curPressUnit)"
        mVb.leakUnit.text = "($curLeakageUnit)"

        mVb.tvShowSerialNo.text = info.workpieceNo
        mVb.tvShowTarget.text = "${info.testPressure}${curPressUnit}"

        mVb.tvPrepareTime.text = getString(R.string.prepare_time, "0S")
        mVb.tvInflationTime.text =
            getString(R.string.inflation_time, "0S", "${info.inflationTime}S")
        mVb.tvVoltageStabilizationTime.text =
            getString(R.string.voltage_stabilization_time, "0S", "${info.stabilizationTime}S")
        mVb.tvLeakTime.text = getString(R.string.leak_time, "0S", "${info.detectionTime}S")
        mVb.tvExhaustTime.text = getString(R.string.exhaust_time, "0S", "${info.exhaustTime}S")

        totalTime =
            readyTime + info.inflationTime + info.stabilizationTime + info.detectionTime + info.exhaustTime
        mVb.dpvShowProgress.setPressureUnit(curPressUnit,if (curPressUnit == ConstantsUtils.PA) 70f else 50f)
        mVb.dpvShowProgress.setYMaxValue(info.upperPressureLimit + upperLimit)
        mVb.dpvShowProgress.setXMaxValue(
            info.inflationTime,
            info.stabilizationTime,
            info.detectionTime,
            info.exhaustTime
        )
        mVb.dpvShowPrevalue.setMinAndMaxValue(0f, info.upperPressureLimit + upperLimit,curPressUnit)
        mVb.dpvShowLeakvalue.setMinAndMaxValue(0f, info.leakageAlarm * 2,curLeakageUnit)
    }

    private fun initClick() {
        setOnTitleLongClick({ })
        setOnBackClick {
            if (isTesting) {
                NToast.shortToast(context, getString(R.string.stop_test_first))
                return@setOnBackClick
            }
            findNavController().popBackStack()
        }

        mVb.tvSettingClick.setOnClickListener {
            if (isTesting) {
                NToast.shortToast(context, getString(R.string.stop_test_first))
                return@setOnClickListener
            }
            val action =
                HighModeFragmentDirections.actionHighModeFragmentToHighModeConfigFragment()
            findNavController().navigate(action)
        }

        mVb.tvStartClick clicks {
            ConstantsUtils.printLog =
                ConstantsUtils.PRINT_LOG + "_" + DateUtils.getDateToString(
                    System.currentTimeMillis(),
                    "yyyyMMdd_HHmmss"
                )
            vm.getDeviceInfo()
            showMessageDialog(getString(R.string.whether_start_test), mCancelClick = {}) {
                sendOnTest()
            }
        }

        mVb.tvStopClick clicks {
            showMessageDialog(getString(R.string.whether_stop_test), mCancelClick = {}) {
                testResult = "3"//getString(R.string.active_stop_test)
                //mVb.tvTestStatus.text = getString(R.string.active_stop_test)
                setTestStatus(R.string.active_stop_test)
                sendCloseTest()
            }
        }
    }

    @SuppressLint("RestrictedApi")
    private fun setTestStatus(status: Int) {
        val maxSize = mContext.resources.getDimension(R.dimen.sp_23).toInt()
        mVb.tvTestStatus.text = getString(status)
        mVb.tvTestStatus.setAutoSizeTextTypeUniformWithConfiguration(
            8,
            maxSize,
            2,
            TypedValue.COMPLEX_UNIT_SP
        )
    }

    @SuppressLint("SetTextI18n")
    override fun createObserver() {
        vm.testSwitchLivedata.observe(this) {
            LogUtil.d(
                "kevin",
                "测试启停结果回调 state:${it}====lifecycle.currentState:${lifecycle.currentState}"
            )
            if (lifecycle.currentState != Lifecycle.State.RESUMED) {
                return@observe
            }
            if (it != null) {
                when (it.handleType) {
                    "01" -> {//启动测试
                        //排气判断标准暂时使用应答指令，后续如果需要，根据当前气压值来判断是否需要排气
                        if (it.ackResult == "02") {
                            vm.sendExhaustOrder("01")
                            return@observe
                        }
                        startTest()
                    }

                    "02" -> {//停止测试
                        if (isTesting) {
                            isTesting = false
                            stopTest()
                        }
                    }
                }
            }
        }
        vm.stateSwitchLiveData.observe(this) {
            if (lifecycle.currentState != Lifecycle.State.RESUMED) {
                return@observe
            }
            when (it) {
                "01" -> {
                    //mVb.tvTestStatus.text = getString(R.string.preparing)
                    setTestStatus(R.string.preparing)
                }

                "02" -> {
                    //mVb.tvTestStatus.text = getString(R.string.inflating)
                    setTestStatus(R.string.inflating)
                }

                "03" -> {
                    //mVb.tvTestStatus.text = getString(R.string.under_voltage_stabilization)
                    setTestStatus(R.string.under_voltage_stabilization)
                }

                "04" -> {
                    //mVb.tvTestStatus.text = getString(R.string.testing)
                    setTestStatus(R.string.testing)
                }

                "05" -> {
                    //mVb.tvTestStatus.text = getString(R.string.exhausting)
                    setTestStatus(R.string.exhausting)
                }
            }
        }
        vm.exhaustLivedata.observe(this) {
            LogUtil.d(
                "kevin",
                "排气指令结果回调 state:${it}======lifecycle.currentState:${lifecycle.currentState}"
            )
            if (lifecycle.currentState != Lifecycle.State.RESUMED) {
                return@observe
            }
            if (it != null && it.handleType == "01") {
                when (it.ackResult) {
                    "01" -> {//收到排气指令应答 开始启动排气
                        showCenterLoading(R.string.bleeding)
                    }

                    "02" -> {//排气结束 MCU排气结束自动上转
                        hideCenterLoading()
                        vm.sendExhaustOrder("02")
                        if (needPrintLog) {
                            PrintLogcatManager.instance.closeLogJob()
                        }
                    }
                }
            }
        }
        vm.leakDataLivedata.observe(this) {
            if (!isTesting) {//非测试状态不接收指令
                return@observe
            }
            if (lifecycle.currentState != Lifecycle.State.RESUMED) {
                return@observe
            }
            tempPaList.add(it.currentPa)
            if (tempPaList.size < 10 && it.currentState != "00") {
                return@observe
            }
            val averageValue = Tools.getAverageValue(tempPaList)
            val curPa = curPressUnit.convertUnit(averageValue.toFloat(), ConstantsUtils.PA)
            tempPaList.clear()
            actualValue = curPa
            virtualValue =
                if (DEBUG) curPa else getVirtualValue(curPa, it.currentState, it.valVeState)

            val curTime = (it.currentTimeCount * TIME_INTERVAL).toFloat() / 1000
            LogUtil.d(
                "kevin",
                "实时气压值回调 状态:${it.currentState} ---当前气压:${averageValue}--虚拟气压：${virtualValue}---当前时间:$curTime" +
                        "------it.currentTimeCount：${it.currentTimeCount} --- 充气状态：${it.valVeState} ==tempCurMaxPa:${tempCurMaxPa}"
            )
            when (it.currentState) {
                "01" -> {
                    mVb.tvPressureValue.text = initPressValue
                    mVb.tvPressureValue.textSize = vm.showTextSize(mContext, initPressValue)
                    mVb.dpvShowPrevalue.realPreValue = 0f
                }

                "05", "00" -> {
                }

                else -> {
                    if (tempCurMaxPa < curPa) {
                        tempCurMaxPa = curPa
                    }
                    val value = vm.showValue(virtualValue, curPressUnit)
                    mVb.tvPressureValue.text = value
                    mVb.tvPressureValue.textSize = vm.showTextSize(mContext, value)
//                    mVb.tvPressureBackground.text =
//                        when {
//                            virtualValue >= 100 -> vm.showValueTwo(0f)
//                            unitType == 1 -> vm.showValue(0f)
//                            else -> vm.showValueOne(0f)
//                        }
                    if (abs(tempCurPa - curPa) > offsetValueLimit) {
                        mVb.dpvShowPrevalue.realPreValue = virtualValue
                        tempCurPa = curPa
                    }
                }
            }

            when (it.currentState) {
                "00" -> {
                    //空闲状态
                }

                "01" -> {
                    // 准备状态
                    leakDataPrepareState(curTime, 0f)
                }

                "02" -> {
                    //充气状态
                    leakDataInflateState(curTime, virtualValue)
                }

                "03" -> {
                    //稳定状态
                    leakDataStabilizationState(curTime, virtualValue, averageValue)
                }

                "04" -> {
                    //检测泄露状态
                    leakDataTestState(curTime, virtualValue, averageValue)
                }

                "05" -> {
                    //排气状态
                    var exhaustValue = virtualValue
                    if (actualValue < virtualValue) {
                        exhaustValue = actualValue
                    }
                    leakDataExhaustState(curTime, exhaustValue)
                }
            }
            tempStatus = it.currentState
            if (curTime >= (readyTime + configInfo.inflationTime + configInfo.stabilizationTime
                        + configInfo.detectionTime + configInfo.exhaustTime)
            ) {
                var exhaustValue = virtualValue
                if (actualValue < virtualValue) {
                    exhaustValue = actualValue
                }
                qualifiedFinish(curTime, exhaustValue)
            }
        }
    }

    override fun lazyLoadData() {
    }

    private fun qualifiedFinish(curTime: Float, curPa: Float) {
        if (isTesting) {
            isTesting = false
            //mVb.tvTestStatus.text = getString(R.string.qualified)
            setTestStatus(R.string.qualified)

            tempDataList.add(PointerTempDate(curTime, getString(R.string.finish), curPa, 0f))
            mVb.dpvShowProgress.setPointData(curTime, curPa)

            mVb.tvExhaustTime.text = getString(
                R.string.exhaust_time,
                "${configInfo.exhaustTime}S",
                "${configInfo.exhaustTime}S"
            )
            testResult = "0"//getString(R.string.qualified)
            stopTest()
        }
    }

    private fun leakDataPrepareState(curTime: Float, curPa: Float) {
        if (curTime <= readyTime) {
            tempDataList.add(PointerTempDate(curTime, getString(R.string.prepare), curPa, 0f))
            mVb.dpvShowProgress.setPointData(curTime, curPa)

            mVb.tvPrepareTime.text =
                getString(R.string.prepare_time, "${curTime.toInt()}S")
        }
    }

    private fun savePointer(curTime: Float, curPa: Float, state: String, leakage: Float = 0f) {
        tempFlag++
        if (tempFlag >= FLAG_COUNTER) {
            tempFlag = 0
            tempDataList.add(PointerTempDate(curTime, state, curPa, leakage))
        }
    }

    private fun leakDataInflateState(curTime: Float, curPa: Float) {
        if (curTime <= readyTime + configInfo.inflationTime) {
            mVb.dpvShowProgress.setPointData(curTime, curPa)

            mVb.tvInflationTime.text = getString(
                R.string.inflation_time,
                "${curTime.toInt() - readyTime}S",
                "${configInfo.inflationTime}S"
            )
            if (curTime < readyTime + 1) {
                mVb.tvPrepareTime.text =
                    getString(R.string.prepare_time, "${readyTime}S")
            }

            FLAG_COUNTER =
                if (configInfo.inflationTime > 100 && curPa == configInfo.testPressure) 10 else 2

            savePointer(curTime, curPa, getString(R.string.inflate))
        }


        when {
            configInfo.testPressure >= lowPressLimit -> {
                //当充气状态，超过压力上限或者值小于1kp，判断失败，停止测试
                if ((actualValue > configInfo.upperPressureLimit + upperMaxLimit
                            || (curTime >= checkInflationTime && actualValue < 1)) && !DEBUG
                ) {
                    errorInflation()
                }
            }

            else -> {
                if (actualValue > configInfo.upperPressureLimit + upperMaxLimit && !DEBUG) {
                    errorInflation()
                }
            }
        }
    }

    private fun errorInflation() {
        context?.isFastTimer {
            if (isTesting) {
                //mVb.tvTestStatus.text = getString(R.string.unqualified)
                setTestStatus(R.string.unqualified)

                mVb.dpvShowPrevalue.setDashboardColor(
                    ContextCompat.getColor(
                        mContext,
                        R.color.color_B91818
                    ), ContextCompat.getColor(mContext, R.color.color_B91818)
                )
                mVb.tvPressureValue.setTextColor(
                    ContextCompat.getColor(
                        mContext,
                        R.color.color_FF4753
                    )
                )
                testResult = "1"
                //getString(R.string.inflation_failure)//getString(R.string.exceed_pressure)
                sendCloseTest()
                showMessageDialog(getString(R.string.exceed_pressure)) {}
            }
        }
    }

    private fun leakDataStabilizationState(curTime: Float, curPa: Float, averageValue: Int) {
        addQueue(averageValue)
        if (curTime <= readyTime + configInfo.inflationTime + configInfo.stabilizationTime) {
            mVb.dpvShowProgress.setPointData(curTime, curPa)

            mVb.tvVoltageStabilizationTime.text = getString(
                R.string.voltage_stabilization_time,
                "${curTime.toInt() - readyTime - configInfo.inflationTime}S",
                "${configInfo.stabilizationTime}S"
            )
            if (curTime < readyTime + configInfo.inflationTime + 1) {
                mVb.tvInflationTime.text = getString(
                    R.string.inflation_time,
                    "${configInfo.inflationTime}S",
                    "${configInfo.inflationTime}S"
                )
            }

            FLAG_COUNTER =
                if (configInfo.stabilizationTime > 100 && curPa == configInfo.testPressure) 10 else 2

            savePointer(curTime, curPa, getString(R.string.stable_pressure))
        }
        //当稳定状态，超过压力上限，低于压力下限 或 小于 1k，均判断失败，停止测试
        if ((actualValue > configInfo.upperPressureLimit + upperMaxLimit
                    || checkQualifiedValue(actualValue)
                    || actualValue <= lowStabilizationValueLimit) && !DEBUG
        ) {
            context?.isFastTimer {
                if (isTesting) {
                    //mVb.tvTestStatus.text = getString(R.string.unqualified)
                    setTestStatus(R.string.unqualified)
                    mVb.dpvShowPrevalue.setDashboardColor(
                        ContextCompat.getColor(
                            mContext,
                            R.color.color_B91818
                        ), ContextCompat.getColor(mContext, R.color.color_B91818)
                    )
                    mVb.tvPressureValue.setTextColor(
                        ContextCompat.getColor(
                            mContext,
                            R.color.color_FF4753
                        )
                    )
                    testResult = "1"
                    //getString(R.string.inflation_failure)//getString(R.string.exceed_pressure)
                    sendCloseTest()
                    showMessageDialog(getString(R.string.exceed_pressure)) {}
                }
            }
        }
    }

    private fun checkQualifiedValue(actualValue: Float): Boolean {
        val standardValue = configInfo.testPressure
        val limitValue =if (standardValue > 50) standardValue * 0.05f else standardValue * 0.1f
        return actualValue < standardValue - limitValue
    }

    private fun leakDataTestState(curTime: Float, curPa: Float, averageValue: Int) {
        addQueue(averageValue)
        val recursValue = Tools.getRecursionMedianAverageValue(loopQueue) ?: averageValue
        if (tempStatus == "03" || stablePressureValue == 0) {
            stablePressureValue = recursValue
        }
        var value = abs(stablePressureValue - recursValue).toFloat()
        value = curLeakageUnit.convertUnit(value, ConstantsUtils.PA)
        if (curTime <= readyTime + configInfo.inflationTime + configInfo.stabilizationTime + configInfo.detectionTime) {
            mVb.tvLeakTime.text = getString(
                R.string.leak_time,
                "${curTime.toInt() - readyTime - configInfo.inflationTime - configInfo.stabilizationTime}S",
                "${configInfo.detectionTime}S"
            )
            if (curTime < readyTime + configInfo.inflationTime + configInfo.stabilizationTime + 1) {
                mVb.tvVoltageStabilizationTime.text = getString(
                    R.string.voltage_stabilization_time,
                    "${configInfo.stabilizationTime}S",
                    "${configInfo.stabilizationTime}S"
                )
            }
            mVb.dpvShowProgress.setPointData(curTime, curPa)

            FLAG_COUNTER =
                if (configInfo.detectionTime > 100 && curPa == configInfo.testPressure) 10 else 2
            savePointer(curTime, curPa, getString(R.string.spill), value)
        }
        mVb.dpvShowLeakvalue.realPreValue = value
        val leakValue = vm.showValue(value, curLeakageUnit)
        mVb.tvLeakageValue.text = leakValue
        mVb.tvLeakageValue.textSize = vm.showTextSize(mContext, leakValue)
//        mVb.tvLeakBackground.text = vm.showValue(0f)
        //泄漏量超过泄漏阀值，判断不合格，停止测试
        if ((value > configInfo.leakageAlarm) && !DEBUG) {
            context?.isFastTimer {
                if (isTesting) {
//                    mVb.tvTestStatus.text = getString(R.string.unqualified)
                    setTestStatus(R.string.unqualified)
                    mVb.dpvShowLeakvalue.setDashboardColor(
                        ContextCompat.getColor(
                            mContext,
                            R.color.color_B91818
                        ), ContextCompat.getColor(mContext, R.color.color_B91818)
                    )
                    mVb.tvLeakageValue.setTextColor(
                        ContextCompat.getColor(
                            mContext,
                            R.color.color_FF4753
                        )
                    )
                    testResult = "2"
                    //getString(R.string.leak_failure)//getString(R.string.leakage_exceed)
                    sendCloseTest()
                    showMessageDialog(getString(R.string.leakage_exceed)) {}
                }
            }
            return
        }
        mVb.dpvShowLeakvalue.setDashboardColor(
            ContextCompat.getColor(
                mContext,
                R.color.color_00A65C
            ), ContextCompat.getColor(mContext, R.color.color_076337)
        )
        mVb.tvLeakageValue.setTextColor(ContextCompat.getColor(mContext, R.color.color_27E28E))
    }

    private fun leakDataExhaustState(curTime: Float, curPa: Float) {
        if (curTime <= readyTime + configInfo.inflationTime + configInfo.stabilizationTime
            + configInfo.detectionTime + configInfo.exhaustTime
        ) {
            mVb.dpvShowProgress.setPointData(curTime, curPa)
            mVb.tvExhaustTime.text = getString(
                R.string.exhaust_time,
                "${curTime.toInt() - readyTime - configInfo.inflationTime - configInfo.stabilizationTime - configInfo.detectionTime}S",
                "${configInfo.exhaustTime}S"
            )
            if (curTime < readyTime + configInfo.inflationTime + configInfo.stabilizationTime
                + configInfo.detectionTime + 1
            ) {
                mVb.tvLeakTime.text = getString(
                    R.string.leak_time,
                    "${configInfo.detectionTime}S",
                    "${configInfo.detectionTime}S"
                )
            }

            FLAG_COUNTER =
                if (configInfo.exhaustTime > 100 && curPa == configInfo.testPressure) 10 else 2
            savePointer(curTime, curPa, getString(R.string.exhaust))
        }
    }

    private fun getVirtualValue(currentValue: Float, state: String?, valveState: String): Float {
        val standardValue = configInfo.testPressure
//        val limitValue = standardValue * 0.05f
        val limitValue =if (standardValue > 50) standardValue * 0.05f else standardValue * 0.1f
        if (state == "02") {
            if (valveState == "01" && currentValue >= standardValue) {
                return standardValue
            }
            if (valveState == "80" && currentValue > standardValue - limitValue && currentValue >= lowPressLimit) {
                return standardValue
            }
            if (valveState == "00" && currentValue < tempCurMaxPa) {
                if (tempCurMaxPa >= standardValue) {
                    return standardValue
                }
                return tempCurMaxPa
            }
            if (currentValue > standardValue) {
                return standardValue
            }
            return currentValue
        }
        if (currentValue > standardValue - limitValue && currentValue >= lowPressLimit) {
            return standardValue
        }
        return currentValue
    }

    private fun initQueue(init: Boolean) {
        queueTempSize = 0
        loopQueue?.clear()
        loopQueue = null
        if (init) {
            loopQueue = ConcurrentLinkedQueue()
        }
    }

    private fun addQueue(value: Int) {
        if (queueTempSize < 10) {
            queueTempSize++
            loopQueue?.add(value)
            return
        }
        loopQueue?.poll()
        loopQueue?.add(value)
    }


    /**
     * 开始启动测试
     */
    private fun startTest() {
        isTesting = true
        tempCurPa = -1f
        tempCurMaxPa = 0f
        FLAG_COUNTER = 2
        tempFlag = 0
        initQueue(true)
        mVb.dpvShowProgress.clearData()
        mVb.dpvShowLeakvalue.realPreValue = 0f
        mVb.tvLeakageValue.text = initLeakValue
        mVb.tvLeakageValue.textSize = vm.showTextSize(mContext, initLeakValue)
//        mVb.tvLeakBackground.text = vm.showValueThree(0f)

        mVb.dpvShowPrevalue.setDashboardColor(
            ContextCompat.getColor(
                mContext,
                R.color.color_00A65C
            ), ContextCompat.getColor(mContext, R.color.color_076337)
        )
        mVb.dpvShowPrevalue.realPreValue = 0f//configInfo.lowerPressureLimit
        mVb.tvPressureValue.text = initPressValue
        mVb.tvPressureValue.textSize = vm.showTextSize(mContext, initPressValue)
//        mVb.tvPressureBackground.text = vm.showValueOne(0f)
        mVb.tvPressureValue.setTextColor(ContextCompat.getColor(mContext, R.color.color_27E28E))

        tempDataList.clear()
//        mVb.tvTestStatus.text = getString(R.string.preparing)
        setTestStatus(R.string.preparing)

        mVb.tvPrepareTime.text = getString(R.string.prepare_time, "0S")
        mVb.tvInflationTime.text =
            getString(R.string.inflation_time, "0S", "${configInfo.inflationTime}S")
        mVb.tvVoltageStabilizationTime.text =
            getString(R.string.voltage_stabilization_time, "0S", "${configInfo.stabilizationTime}S")
        mVb.tvLeakTime.text = getString(R.string.leak_time, "0S", "${configInfo.detectionTime}S")
        mVb.tvExhaustTime.text =
            getString(R.string.exhaust_time, "0S", "${configInfo.exhaustTime}S")

        testResult = "0"//getString(R.string.qualified)

        refreshBtnStatusUI(isTesting, true)
    }

    /**
     * 停止测试
     */
    private fun stopTest() {
        launch(Dispatchers.Main) {
            initQueue(false)
            withContext(Dispatchers.IO) {
                saveInfoToDB()
            }
            refreshBtnStatusUI(isTesting, true)
            mVb.dpvShowPrevalue.realPreValue = 0f
            mVb.tvPressureValue.text = initPressValue
            mVb.tvPressureValue.textSize = vm.showTextSize(mContext, initPressValue)
//            mVb.tvPressureBackground.text = vm.showValueOne(0f)
        }
        if (needPrintLog) {
            PrintLogcatManager.instance.closeLogJob()
        }
    }

    private suspend fun saveInfoToDB() {
        LogUtil.e("kevin", "<------------开始保存数据--------------->")
        val sn = EasyPreferences.instance[ConstantsUtils.DEVICE_NUMBER]
        val date = DateUtils.getDateToString(System.currentTimeMillis(), "yyyyMMdd_HHmmss")
        val filePath =
            Tools.saveCurrentImage(activity, configInfo.workpieceNo + "_" + date + ".jpg")
        val curTime = DateUtils.getDateToString(System.currentTimeMillis(), "yyyyMMddHHmmss")
        val bean = TestData()
        bean.workpieceNo = configInfo.workpieceNo + "_" + curTime
        bean.testPressure = configInfo.testPressure.toString()
        bean.testTime =
            DateUtils.getDateToString(System.currentTimeMillis(), DateUtils.DATE_FORMAT2)
        bean.leakage = mVb.tvLeakageValue.text.toString().toFloat().toString()
        bean.testResult = testResult
        val gson = Gson()
        bean.pointerList = gson.toJson(tempDataList)
        bean.configInfo = gson.toJson(configInfo)
        bean.imagePath = filePath
        bean.deviceSN = sn
        vm.setData(mContext, bean)
    }

    private fun refreshBtnStatusUI(isOn: Boolean, showProgress: Boolean) {
        LogUtil.i("kevin", "refreshBtnStatusUI 刷新按钮 是否测试模式:$isOn")
        mVb.tvStartClick.isEnabled = !isOn

        mVb.tvStopClick.isEnabled = isOn

//        mVb.dpvShowProgress.visibility = if (showProgress) View.VISIBLE else View.GONE
//        mVb.clXAxis.visibility = if (showProgress) View.VISIBLE else View.GONE
    }

    private fun showMessageDialog(
        msg: String,
        mCancelClick: View.OnClickListener? = null,
        block: () -> Unit
    ) {
        if (msgDialog != null && msgDialog!!.isShowing) {
            msgDialog?.dismiss()
            msgDialog = null
        }
        msgDialog = MessageDialog(mContext)
        msgDialog?.showMessage(msg = msg, mCancelClick = mCancelClick, mConfirmClick = {
            block()
        })
    }

    private fun sendOnTest() {
        vm.testSwitch(true, metricConfigInfo)
        if (needPrintLog) {
            PrintLogcatManager.instance.initSaveLogJob(
                ConstantsUtils.printLog,
                DateUtils.getDateToString(System.currentTimeMillis(), "yyyyMMdd_HHmmss")
            )
        }
    }

    private fun sendCloseTest() {
        vm.testSwitch(false, metricConfigInfo)
    }

    override fun onBackPressed(): Boolean {
        if (isTesting) {
            NToast.shortToast(context, getString(R.string.stop_test_first))
            return true
        }
        return super.onBackPressed()
    }

    override fun onDestroyView() {
        if (needPrintLog) {
            PrintLogcatManager.instance.closeLogJob()
        }
        super.onDestroyView()
    }

    companion object {
        const val TIME_INTERVAL = 40
        const val DEBUG = false
    }
}