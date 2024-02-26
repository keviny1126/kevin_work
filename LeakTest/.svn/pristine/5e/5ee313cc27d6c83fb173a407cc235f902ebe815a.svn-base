package com.newchip.tool.leaktest

import android.os.Bundle
import android.view.Gravity
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.newchip.tool.leaktest.databinding.ActivityDataDetailReportBinding
import com.power.baseproject.bean.ConfigLeakInfoBean
import com.power.baseproject.bean.PointerTempDate
import com.power.baseproject.db.DataRepository
import com.power.baseproject.db.entity.TestData
import com.power.baseproject.utils.ConstantsUtils
import com.power.baseproject.widget.NToast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ShowReportActivity : AppCompatActivity(),
    CoroutineScope by CoroutineScope(Dispatchers.Default) {
    private lateinit var mVb: ActivityDataDetailReportBinding

    private var configInfo: ConfigLeakInfoBean? = null
    private var pointerList = mutableMapOf<Float, Float>()
    private var testDataList = mutableListOf<TestData>()
    private var tempDataList: ArrayList<PointerTempDate>? = null
    private var curPressUnit = ConstantsUtils.KPA
    private var curLeakageUnit = ConstantsUtils.PA
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mVb = ActivityDataDetailReportBinding.inflate(layoutInflater)
        val view = mVb.root
        setContentView(view)
        initData()
        initWindow()
    }

    private fun initWindow() {
        val window = window
        if (window != null) {
            val lp = window.attributes
            val width = (resources.displayMetrics.widthPixels * 0.9).toInt()
            val height = (resources.displayMetrics.heightPixels * 0.9).toInt()
            lp.width = width
            lp.height = height//WindowManager.LayoutParams.WRAP_CONTENT
            lp.gravity = Gravity.CENTER
            window.attributes = lp
        }
    }

    private fun initData() {
        launch(Dispatchers.Main) {
            clearInfo()
            val intent = intent ?: return@launch
            val data = intent.data ?: return@launch
            val id = data.getQueryParameter("id") ?: return@launch

            val dataList = withContext(Dispatchers.IO) {
                DataRepository.instance.getAllTestData(this@ShowReportActivity)
            }

            if (dataList.isNullOrEmpty()) {
                NToast.shortToast(this@ShowReportActivity, R.string.data_is_empty)
                return@launch
            }
            testDataList = dataList
            for (bean in testDataList) {
                if (bean.id == id.toInt()) {
                    showCurrentInfo(bean)
                    break
                }
            }
        }
    }

    private fun showCurrentInfo(data: TestData) {
        mVb.ppvShowProgress.clearData()
        pointerList.clear()
        configInfo = null

        mVb.tvShowTime.text = data.testTime
        mVb.tvShowPressure.text = data.testPressure
        mVb.tvShowLeakage.text = data.leakage
        val testResult = testResult(data.testResult)
        mVb.tvShowResult.text = testResult.ifEmpty { "----" }

        val gson = Gson()
        val configJson = data.configInfo
        if (!configJson.isNullOrEmpty()) {
            configInfo = gson.fromJson(data.configInfo, ConfigLeakInfoBean::class.java) ?: return
            curPressUnit = configInfo!!.pressureUnit ?: ConstantsUtils.KPA
            curLeakageUnit = configInfo!!.leakageUnit ?: ConstantsUtils.PA
            mVb.tvPressure.text =
                getString(R.string.test_pressure).replace("kpa", "$curPressUnit", true)
            mVb.tvLeakage.text =
                getString(R.string.leakage_title).replace("pa", "$curLeakageUnit", true)

            mVb.tvShowSn.text = configInfo!!.workpieceNo

            mVb.ppvShowProgress.setPressureUnit(curPressUnit, 70f)
            mVb.ppvShowProgress.setYMaxValue(configInfo!!.upperPressureLimit + 5)
            mVb.ppvShowProgress.setXMaxValue(
                configInfo!!.inflationTime,
                configInfo!!.stabilizationTime,
                configInfo!!.detectionTime,
                configInfo!!.exhaustTime
            )
            mVb.tvPrepareTime.text = getString(R.string.prepare_time, "${0}S")
            mVb.tvInflationTime.text =
                getString(R.string.inflation_time, "${0}S", "${configInfo?.inflationTime}S")
            mVb.tvVoltageStabilizationTime.text =
                getString(
                    R.string.voltage_stabilization_time,
                    "${0}S",
                    "${configInfo?.stabilizationTime}S"
                )
            mVb.tvLeakTime.text =
                getString(R.string.leak_time, "${0}S", "${configInfo?.detectionTime}S")
            mVb.tvExhaustTime.text =
                getString(R.string.exhaust_time, "${0}S", "${configInfo?.exhaustTime}S")

            val pointerJson = data.pointerList
            if (!pointerJson.isNullOrEmpty()) {
                tempDataList =
                    gson.fromJson(pointerJson, object : TypeToken<List<PointerTempDate>>() {}.type)
                if (tempDataList.isNullOrEmpty()) {
                    return
                }
                var endTime = 0f
                for (bean in tempDataList!!) {
                    pointerList[bean.time] = bean.pressureValue
                    endTime = bean.time
                }
                launch {
                    delay(100)
                    mVb.ppvShowProgress.setAllPointer(pointerList)

                    mVb.tvPrepareTime.text =
                        getString(
                            R.string.prepare_time,
                            if (endTime >= 10) "${10}S" else "${endTime.toInt()}S"
                        )
                    if (endTime > 10) {
                        mVb.tvInflationTime.text =
                            getString(
                                R.string.inflation_time,
                                if (endTime >= 10 + configInfo!!.inflationTime) "${configInfo!!.inflationTime}S" else "${endTime.toInt() - 10}S",
                                "${configInfo?.inflationTime}S"
                            )
                    }
                    if (endTime > 10 + configInfo!!.inflationTime) {
                        mVb.tvVoltageStabilizationTime.text =
                            getString(
                                R.string.voltage_stabilization_time,
                                if (endTime >= 10 + configInfo!!.inflationTime + configInfo!!.stabilizationTime) "${configInfo!!.stabilizationTime}S" else "${endTime.toInt() - 10 - configInfo!!.inflationTime}S",
                                "${configInfo?.stabilizationTime}S"
                            )
                    }
                    if (endTime > 10 + configInfo!!.inflationTime + configInfo!!.stabilizationTime) {
                        mVb.tvLeakTime.text =
                            getString(
                                R.string.leak_time,
                                if (endTime >= 10 + configInfo!!.inflationTime + configInfo!!.stabilizationTime + configInfo!!.detectionTime) "${configInfo!!.detectionTime}S" else "${endTime.toInt() - 10 - configInfo!!.inflationTime - configInfo!!.stabilizationTime}S",
                                "${configInfo?.detectionTime}S"
                            )
                    }
                    if (endTime > 10 + configInfo!!.inflationTime + configInfo!!.stabilizationTime + configInfo!!.detectionTime) {
                        mVb.tvExhaustTime.text =
                            getString(
                                R.string.exhaust_time,
                                if (endTime >= 10 + configInfo!!.inflationTime + configInfo!!.stabilizationTime + configInfo!!.detectionTime + configInfo!!.exhaustTime) "${configInfo!!.exhaustTime}S" else "${endTime.toInt() - 10 - configInfo!!.inflationTime - configInfo!!.stabilizationTime - configInfo!!.detectionTime}S",
                                "${configInfo?.exhaustTime}S"
                            )
                    }
                }
            }
        }
    }

    private fun testResult(testResult: String?): String {
        return when (testResult) {
            "0" -> getString(R.string.qualified)
            "1" -> getString(R.string.inflation_failure)
            "2" -> getString(R.string.leak_failure)
            "3" -> getString(R.string.active_stop_test)
            else -> testResult ?: ""
        }
    }

    private fun clearInfo() {
        mVb.ppvShowProgress.clearData()
        pointerList.clear()
        configInfo = null

        mVb.tvShowTime.text = ""
        mVb.tvShowPressure.text = ""
        mVb.tvShowLeakage.text = ""
        mVb.tvShowResult.text = ""
        mVb.tvShowSn.text = ""

        mVb.tvPrepareTime.text = getString(R.string.prepare_time, "${0}S")
        mVb.tvInflationTime.text = getString(R.string.inflation_time, "${0}S", "${0}S")
        mVb.tvVoltageStabilizationTime.text =
            getString(
                R.string.voltage_stabilization_time,
                "${0}S",
                "${0}S"
            )
        mVb.tvLeakTime.text =
            getString(R.string.leak_time, "${0}S", "${0}S")
        mVb.tvExhaustTime.text =
            getString(R.string.exhaust_time, "${0}S", "${0}S")
    }

    override fun onDestroy() {
        (this as CoroutineScope).cancel()
        super.onDestroy()
    }
}