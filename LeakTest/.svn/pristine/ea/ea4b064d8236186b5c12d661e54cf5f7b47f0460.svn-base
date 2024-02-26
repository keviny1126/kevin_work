package com.newchip.tool.leaktest.ui.data

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.newchip.tool.leaktest.R
import com.newchip.tool.leaktest.databinding.FragmentDataDetailBinding
import com.power.baseproject.bean.ConfigLeakInfoBean
import com.power.baseproject.bean.PointerTempDate
import com.power.baseproject.ktbase.ui.BaseAppFragment
import com.power.baseproject.utils.clicks
import com.power.baseproject.utils.log.LogUtil
import com.power.baseproject.widget.NToast
import kotlinx.coroutines.*


class DataDetailFragment : BaseAppFragment<FragmentDataDetailBinding>() {
    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentDataDetailBinding.inflate(inflater, container, false)

    var isTesting = false
    var totalTime = 0
    var readyTime = 10//准备时间，默认为10s
    var configInfo: ConfigLeakInfoBean? = null
    private var drawJob: Job? = null
    private var tempDataList: ArrayList<PointerTempDate>? = null
    private var pointerList = mutableMapOf<Float, Float>()
    private var leakage: String? = null
    override fun initView() {
        setTitle(R.string.data_detail)
        refreshBtnStatusUI(false)
        initClick()
    }

    override fun initData() {
        super.initData()
        pointerList.clear()
        val bundle = arguments
        if (bundle != null) {
            val testData = arguments?.let { DataDetailFragmentArgs.fromBundle(it).testData }
            if (testData == null) {
                NToast.shortToast(mContext, R.string.data_is_empty)
                return
            }

            binding.tvTitleShow.text = testData.workpieceNo
            val testResult = testData.testResult
            mVb.tvTestStatus.text = if (testResult.isNullOrEmpty()) "----" else testResult
            val gson = Gson()
            val configJson = testData.configInfo
            if (!configJson.isNullOrEmpty()) {
                configInfo = gson.fromJson(testData.configInfo, ConfigLeakInfoBean::class.java)
                initUI(configInfo)
            }
            leakage = testData.leakage
            if (leakage != null) {
                mVb.tvLeakageValue.text = leakage
                mVb.dpvShowLeakvalue.realPreValue = leakage!!.toFloat()
            }
            val testPress = testData.testPressure
            if (testPress != null) {
                mVb.dpvShowPrevalue.realPreValue = testPress.toFloat()
                mVb.tvPressureValue.text = "${testPress}kpa"
            }
            val pointerJson = testData.pointerList
            if (!pointerJson.isNullOrEmpty()) {
                tempDataList =
                    gson.fromJson(pointerJson, object : TypeToken<List<PointerTempDate>>() {}.type)
                for (data in tempDataList!!) {
                    pointerList[data.time] = data.pressureValue
                }
                launch {
                    delay(100)
                    mVb.dpvShowProgress.setAllPointer(pointerList)

                    mVb.tvPrepareTime.text = getString(R.string.prepare_time, "${readyTime}S")
                    mVb.tvInflationTime.text =
                        getString(
                            R.string.inflation_time,
                            "${configInfo!!.inflationTime}S",
                            "${configInfo!!.inflationTime}S"
                        )
                    mVb.tvVoltageStabilizationTime.text =
                        getString(
                            R.string.voltage_stabilization_time,
                            "${configInfo!!.stabilizationTime}S",
                            "${configInfo!!.stabilizationTime}S"
                        )
                    mVb.tvLeakTime.text = getString(
                        R.string.leak_time,
                        "${configInfo!!.detectionTime}S",
                        "${configInfo!!.detectionTime}S"
                    )
                    mVb.tvExhaustTime.text = getString(
                        R.string.exhaust_time,
                        "${configInfo!!.exhaustTime}S",
                        "${configInfo!!.exhaustTime}S"
                    )
                }
            }
        }
    }

    private fun initUI(info: ConfigLeakInfoBean?) {
        if (info == null) {
            return
        }
        LogUtil.i("kevin", "----getConfigInfo:$info")
        mVb.tvShowSerialNo.text = getString(R.string.serial_number, info.workpieceNo)
        mVb.tvShowTarget.text = getString(R.string.target_value, "${info.testPressure}kpa")

        mVb.tvPrepareTime.text = getString(R.string.prepare_time, "${0}S")
        mVb.tvInflationTime.text =
            getString(R.string.inflation_time, "0S", "${info.inflationTime}S")
        mVb.tvVoltageStabilizationTime.text =
            getString(R.string.voltage_stabilization_time, "0S", "${info.stabilizationTime}S")
        mVb.tvLeakTime.text = getString(R.string.leak_time, "0S", "${info.detectionTime}S")
        mVb.tvExhaustTime.text = getString(R.string.exhaust_time, "0S", "${info.exhaustTime}S")

        totalTime =
            readyTime + info.inflationTime + info.stabilizationTime + info.detectionTime + info.exhaustTime
        mVb.dpvShowProgress.setYMaxValue(info.upperPressureLimit+5)
        mVb.dpvShowProgress.setXMaxValue(
            info.inflationTime,
            info.stabilizationTime,
            info.detectionTime,
            info.exhaustTime
        )
        mVb.dpvShowPrevalue.setMinAndMaxValue(0f, info.upperPressureLimit + 5)
        mVb.dpvShowLeakvalue.setMinAndMaxValue(0f, info.leakageAlarm.toFloat() * 3)

//        mVb.tvPrepareTime.layoutParams =
//            LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 10f)
//        mVb.tvInflationTime.layoutParams = LinearLayout.LayoutParams(
//            0,
//            LinearLayout.LayoutParams.WRAP_CONTENT,
//            info.inflationTime.toFloat()
//        )
//        mVb.tvVoltageStabilizationTime.layoutParams = LinearLayout.LayoutParams(
//            0,
//            LinearLayout.LayoutParams.WRAP_CONTENT,
//            info.stabilizationTime.toFloat()
//        )
//        mVb.tvLeakTime.layoutParams = LinearLayout.LayoutParams(
//            0,
//            LinearLayout.LayoutParams.WRAP_CONTENT,
//            info.detectionTime.toFloat()
//        )
//        mVb.tvExhaustTime.layoutParams = LinearLayout.LayoutParams(
//            0,
//            LinearLayout.LayoutParams.WRAP_CONTENT,
//            info.exhaustTime.toFloat()
//        )
    }

    private fun initClick() {
        setOnBackClick {
            findNavController().popBackStack()
        }

        mVb.imgBtnSetting.setOnClickListener {
            if (isTesting) {
                NToast.shortToast(context, getString(R.string.stop_test_first))
                return@setOnClickListener
            }

        }

        mVb.imgBtnStart clicks {
            startTest()
        }

        mVb.imgBtnStop clicks {
            stopTest()
        }
    }

    @SuppressLint("SetTextI18n")
    override fun createObserver() {

    }

    override fun lazyLoadData() {

    }

    private fun startDraw(): Job {
        return launch(start = CoroutineStart.LAZY, context = Dispatchers.IO) {
            if (!tempDataList.isNullOrEmpty()) {
                for (data in tempDataList!!) {
                    val curTime = data.time
                    val curPa = data.pressureValue
                    LogUtil.e("kevin", "-----curTime:$curTime ===curPa:$curPa")
                    withContext(Dispatchers.Main) {
                        setPointData(curTime, curPa)
//                        mVb.dpvShowPrevalue.realPreValue = curPa
//                        mVb.tvPressureValue.text = "${curPa}kpa"
//                        if ((curTime <= readyTime + configInfo!!.inflationTime + configInfo!!.stabilizationTime + configInfo!!.detectionTime) &&
//                            (curTime >= readyTime + configInfo!!.inflationTime + configInfo!!.stabilizationTime)
//                        ) {
//                            val value = (configInfo!!.testPressure * 1000) - curPa * 1000
//                            if (value > 0) {
//                                mVb.dpvShowLeakvalue.realPreValue = value
//                                mVb.tvLeakageValue.text = "${value}pa"
//                            }
//                        }
                        when (data.state) {
                            "准备" -> {
                                mVb.tvPrepareTime.text =
                                    getString(R.string.prepare_time, "${curTime.toInt()}S")
                            }
                            "充气" -> {
                                mVb.tvInflationTime.text = getString(
                                    R.string.inflation_time,
                                    "${curTime.toInt() - readyTime}S",
                                    "${configInfo!!.inflationTime}S"
                                )
                                if (curTime < readyTime + 1) {
                                    mVb.tvPrepareTime.text =
                                        getString(R.string.prepare_time, "${readyTime}S")
                                }
                            }
                            "稳压" -> {
                                mVb.tvVoltageStabilizationTime.text = getString(
                                    R.string.voltage_stabilization_time,
                                    "${curTime.toInt() - readyTime - configInfo!!.inflationTime}S",
                                    "${configInfo!!.stabilizationTime}S"
                                )
                                if (curTime < readyTime + configInfo!!.inflationTime + 1) {
                                    mVb.tvInflationTime.text = getString(
                                        R.string.inflation_time,
                                        "${configInfo!!.inflationTime}S",
                                        "${configInfo!!.inflationTime}S"
                                    )
                                }
                            }
                            "泄漏" -> {
                                mVb.tvLeakTime.text = getString(
                                    R.string.leak_time,
                                    "${curTime.toInt() - readyTime - configInfo!!.inflationTime - configInfo!!.stabilizationTime}S",
                                    "${configInfo!!.detectionTime}S"
                                )
                                if (curTime < readyTime + configInfo!!.inflationTime + configInfo!!.stabilizationTime + 1) {
                                    mVb.tvVoltageStabilizationTime.text = getString(
                                        R.string.voltage_stabilization_time,
                                        "${configInfo!!.stabilizationTime}S",
                                        "${configInfo!!.stabilizationTime}S"
                                    )
                                }
                            }
                            "排气" -> {
                                mVb.tvExhaustTime.text = getString(
                                    R.string.exhaust_time,
                                    "${curTime.toInt() - readyTime - configInfo!!.inflationTime - configInfo!!.stabilizationTime - configInfo!!.detectionTime}S",
                                    "${configInfo!!.exhaustTime}S"
                                )
                                if (curTime < readyTime + configInfo!!.inflationTime + configInfo!!.stabilizationTime
                                    + configInfo!!.detectionTime + 1
                                ) {
                                    mVb.tvLeakTime.text = getString(
                                        R.string.leak_time,
                                        "${configInfo!!.detectionTime}S",
                                        "${configInfo!!.detectionTime}S"
                                    )
                                }
                            }
                            "空闲" -> {
                                mVb.tvExhaustTime.text = getString(
                                    R.string.exhaust_time,
                                    "${configInfo!!.exhaustTime}S",
                                    "${configInfo!!.exhaustTime}S"
                                )
                            }
                        }
                    }

                    delay(200)
                }
                stopTest()
            }
        }
    }

    private fun setPointData(curTime: Float, curPa: Float) {
        mVb.dpvShowProgress.setPointData(curTime, curPa)
    }

    /**
     * 开始启动测试
     */
    private fun startTest() {
        isTesting = true
        mVb.dpvShowProgress.clearData()
//        mVb.dpvShowLeakvalue.realPreValue = 0f
//        mVb.tvLeakageValue.text = "${0}pa"
//
//        mVb.dpvShowPrevalue.realPreValue = configInfo!!.lowerPressureLimit
//        mVb.tvPressureValue.text = "${0}kpa"

        refreshBtnStatusUI(isTesting)
        drawJob = startDraw()
        drawJob?.start()
    }

    /**
     * 停止测试
     */
    private fun stopTest() {
        launch(Dispatchers.Main) {
            isTesting = false
            refreshBtnStatusUI(isTesting)
            drawJob?.cancel()
            drawJob = null
        }
    }

    private fun refreshBtnStatusUI(isOn: Boolean) {
        mVb.imgBtnStart.isEnabled = !isOn
        mVb.imgBtnStop.isEnabled = isOn
    }
}