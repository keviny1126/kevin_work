package com.newchip.tool.leaktest.ui.detection

import android.text.InputType
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.newchip.tool.leaktest.R
import com.newchip.tool.leaktest.databinding.FragmentConfigBinding
import com.newchip.tool.leaktest.ui.MainViewModel
import com.newchip.tool.leaktest.widget.ConfigParameterDialog
import com.power.baseproject.bean.ConfigLeakInfoBean
import com.power.baseproject.ktbase.ui.BaseAppFragment
import com.power.baseproject.utils.ConstantsUtils
import com.power.baseproject.utils.EasyPreferences
import com.power.baseproject.utils.Tools.toUnitString
import com.power.baseproject.utils.clicks
import com.power.baseproject.utils.log.LogUtil
import com.power.baseproject.widget.NToast

class LowVoltageConfigFragment : BaseAppFragment<FragmentConfigBinding>() {
    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentConfigBinding.inflate(inflater, container, false)

    val vm: MainViewModel by activityViewModels()
    private lateinit var curPressUnit: String
    private lateinit var curLeakageUnit: String
    private var configDialog: ConfigParameterDialog? = null

    lateinit var configInfo: ConfigLeakInfoBean
    override fun initView() {
        setTitle(R.string.low_mode_config)
        initClick()
    }

    override fun initData() {
        super.initData()
        curPressUnit = EasyPreferences.instance[ConstantsUtils.PRESS_UNIT_TYPE, 0].toUnitString()
        curLeakageUnit =
            EasyPreferences.instance[ConstantsUtils.LEAKAGE_UNIT_TYPE, 1].toUnitString()
        configInfo = vm.getConfigData()
        configInfo = configInfo.switchUnits(curPressUnit, curLeakageUnit)
        updateData(configInfo)
    }

    private fun initClick() {
        setOnTitleLongClick({})
        setOnBackClick {
            findNavController().popBackStack()
        }
        setOnRightClick {
            saveData()
        }
        mVb.etWorkpieceNo clicks {
            showConfigDialog(
                mVb.tvWorkpieceNo.text.toString(),
                mVb.etWorkpieceNo,
                InputType.TYPE_CLASS_TEXT,
                0,
                needCheck = false
            )
        }
        mVb.etLeakageAlarm clicks {
            showConfigDialog(
                mVb.tvLeakageAlarm.text.toString(),
                mVb.etLeakageAlarm,
                InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL,
                1
            )
        }
        mVb.etWorkpieceVolume clicks {
            showConfigDialog(
                mVb.tvWorkpieceVolume.text.toString(),
                mVb.etWorkpieceVolume,
                InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL,
                2
            )
        }
        mVb.etInflationTime clicks {
            showConfigDialog(
                mVb.tvInflationTimeTitle.text.toString(),
                mVb.etInflationTime,
                InputType.TYPE_CLASS_NUMBER,
                3
            )
        }
        mVb.etTestPressure clicks {
            showConfigDialog(
                mVb.tvTestPressure.text.toString(),
                mVb.etTestPressure,
                InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL,
                4
            )
        }
        mVb.etStabilizationTime clicks {
            showConfigDialog(
                mVb.tvStabilizationTime.text.toString(),
                mVb.etStabilizationTime,
                InputType.TYPE_CLASS_NUMBER,
                5
            )
        }
        mVb.etUpperPressureLimit clicks {
            showConfigDialog(
                mVb.tvUpperPressureLimit.text.toString(),
                mVb.etUpperPressureLimit,
                InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL,
                6
            )
        }
        mVb.etDetectionTime clicks {
            showConfigDialog(
                mVb.tvDetectionTime.text.toString(),
                mVb.etDetectionTime,
                InputType.TYPE_CLASS_NUMBER,
                7
            )
        }
        mVb.etLowerPressureLimit clicks {
            showConfigDialog(
                mVb.tvLowerPressureLimit.text.toString(),
                mVb.etLowerPressureLimit,
                InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL,
                8
            )
        }
        mVb.etExhaustTimeTitle clicks {
            showConfigDialog(
                mVb.tvExhaustTimeTitle.text.toString(),
                mVb.etExhaustTimeTitle,
                InputType.TYPE_CLASS_NUMBER,
                9
            )
        }
    }

    override fun createObserver() {
    }

    override fun lazyLoadData() {
    }

    private fun updateData(configInfo: ConfigLeakInfoBean) {
        LogUtil.i("kevin", "当前配置数据：$configInfo")
        mVb.tvLeakageAlarm.text =
            getString(R.string.leakage_alarm).replace("pa/", "${configInfo.leakageUnit}/", true)
        mVb.tvTestPressure.text =
            getString(R.string.test_pressure).replace("kpa", "${configInfo.pressureUnit}", true)
        mVb.tvUpperPressureLimit.text =
            getString(R.string.upper_pressure_limit).replace(
                "kpa",
                "${configInfo.pressureUnit}",
                true
            )
        mVb.tvLowerPressureLimit.text =
            getString(R.string.lower_pressure_limit).replace(
                "kpa",
                "${configInfo.pressureUnit}",
                true
            )

        mVb.etWorkpieceNo.text = configInfo.workpieceNo
        mVb.etWorkpieceVolume.text = configInfo.workpieceVolume.toString()
        mVb.etDetectionTime.text = configInfo.detectionTime.toString()
        mVb.etExhaustTimeTitle.text = configInfo.exhaustTime.toString()
        mVb.etInflationTime.text = configInfo.inflationTime.toString()
        mVb.etLeakageAlarm.text = configInfo.leakageAlarm.toString()
        mVb.etLowerPressureLimit.text = configInfo.lowerPressureLimit.toString()
        mVb.etStabilizationTime.text = configInfo.stabilizationTime.toString()
        mVb.etTestPressure.text = configInfo.testPressure.toString()
        mVb.etUpperPressureLimit.text = configInfo.upperPressureLimit.toString()
    }

    private fun saveData() {
        if (mVb.etWorkpieceNo.text.isNullOrEmpty()
            || mVb.etWorkpieceVolume.text.isNullOrEmpty()
            || mVb.etDetectionTime.text.isNullOrEmpty()
            || mVb.etExhaustTimeTitle.text.isNullOrEmpty()
            || mVb.etInflationTime.text.isNullOrEmpty()
            || mVb.etLeakageAlarm.text.isNullOrEmpty()
            || mVb.etLowerPressureLimit.text.isNullOrEmpty()
            || mVb.etStabilizationTime.text.isNullOrEmpty()
            || mVb.etTestPressure.text.isNullOrEmpty()
            || mVb.etUpperPressureLimit.text.isNullOrEmpty()
        ) {
            NToast.shortToast(context, R.string.data_cannot_be_empty)
            return
        }

        val upperValue = mVb.etUpperPressureLimit.text.toString().toFloat()
        val lowerValue = mVb.etLowerPressureLimit.text.toString().toFloat()
        val pressValue = mVb.etTestPressure.text.toString().toFloat()
        if (upperValue < pressValue) {
            NToast.shortToast(context, R.string.upper_value_cannot_press)
            return
        }
        if (lowerValue > pressValue) {
            NToast.shortToast(context, R.string.lower_value_cannot_press)
            return
        }

        configInfo.workpieceNo = mVb.etWorkpieceNo.text.toString()
        configInfo.workpieceVolume = mVb.etWorkpieceVolume.text.toString().toFloat()
        configInfo.detectionTime = mVb.etDetectionTime.text.toString().toInt()
        configInfo.exhaustTime = mVb.etExhaustTimeTitle.text.toString().toInt()
        configInfo.inflationTime = mVb.etInflationTime.text.toString().toInt()
        configInfo.leakageAlarm = mVb.etLeakageAlarm.text.toString().toFloat()
        configInfo.stabilizationTime = mVb.etStabilizationTime.text.toString().toInt()
        configInfo.lowerPressureLimit = lowerValue
        configInfo.testPressure = pressValue
        configInfo.upperPressureLimit = upperValue
        configInfo.testMode = 0
        configInfo.pressureUnit = curPressUnit
        configInfo.leakageUnit = curLeakageUnit

        val result = vm.setConfigData(configInfo)
        NToast.shortToast(context, if (result) R.string.save_success else R.string.save_failed)
    }

    private fun showConfigDialog(
        title: String,
        view: TextView,
        type: Int,
        msgType: Int = 1,
        needCheck: Boolean = true
    ) {
        configDialog?.dismiss()
        configDialog = null

        configDialog = ConfigParameterDialog(mContext)
        configDialog?.setTitle(title)
        configDialog?.setInputType(type, view.text.toString())
        configDialog?.setMsgType(msgType)
        configDialog?.setCancelable(false)
        configDialog?.showDialog(needCheck = needCheck) {
            view.text = it
        }
    }
}