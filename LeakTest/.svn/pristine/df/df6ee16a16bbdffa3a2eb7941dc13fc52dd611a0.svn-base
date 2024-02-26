package com.newchip.tool.leaktest.widget

import android.content.Context
import android.text.InputFilter
import android.text.InputType
import android.view.View
import com.newchip.tool.leaktest.R
import com.newchip.tool.leaktest.databinding.ConfigParameterLayoutBinding
import com.power.baseproject.ktbase.dialog.BaseDialog
import com.power.baseproject.utils.ConstantsUtils
import com.power.baseproject.utils.EasyPreferences
import com.power.baseproject.utils.Tools.convertUnit
import com.power.baseproject.utils.Tools.toDecimalNotationString
import com.power.baseproject.utils.Tools.toUnitString
import com.power.baseproject.widget.NToast

class ConfigParameterDialog(context: Context) : BaseDialog(context) {
    private var mContentView: View? = null
    private var mVb: ConfigParameterLayoutBinding =
        ConfigParameterLayoutBinding.inflate(layoutInflater)
    private var minValue = 0f
    private var maxValue = 0f
    private var curPressUnit: String
    private var curLeakageUnit: String

    init {
        mContentView = mVb.root
        curPressUnit = EasyPreferences.instance[ConstantsUtils.PRESS_UNIT_TYPE, 0].toUnitString()
        curLeakageUnit =
            EasyPreferences.instance[ConstantsUtils.LEAKAGE_UNIT_TYPE, 1].toUnitString()
    }

    override fun createContentView(): View? {
        return mContentView
    }

    fun setTitle(title: String) {
        binding.tvTitle.text = title
    }

    fun setInputType(type: Int, value: String) {
        mVb.editParameter.inputType = type
        mVb.editParameter.setText(value)
        mVb.editParameter.setSelection(mVb.editParameter.text.length)
        if (type == InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL) {
            //浮点数,设置小数点后最多5位
            val decimalFilter = InputFilter { source, start, end, dest, dstart, dend ->
                val input = dest.toString() + source.toString()
                if (input.matches(Regex("^\\d+(\\.\\d{0,5})?$"))) {
                    null
                } else {
                    ""
                }
            }
            mVb.editParameter.filters = arrayOf(decimalFilter)
        }
    }

    /**
     * @param type 配置参数: 1.泄露量；4.测试压力；6.压力上限；8.压力下限；
     */
    fun setMsgType(type: Int) {
        val lowLimit: String
        val upperLimit: String
        val msg = when (type) {
            1 -> {
                minValue = curLeakageUnit.convertUnit(1f, ConstantsUtils.PA)
                maxValue = curLeakageUnit.convertUnit(1000f, ConstantsUtils.PA)
                lowLimit = "${
                    if (curLeakageUnit == ConstantsUtils.PA) minValue else minValue.toDecimalNotationString(
                        5
                    )
                }$curLeakageUnit"
                upperLimit = "${
                    if (curLeakageUnit == ConstantsUtils.PA) maxValue else maxValue.toDecimalNotationString(
                        5
                    )
                }$curLeakageUnit"
                context.getString(R.string.valid_range, lowLimit, upperLimit)
            }

            2 -> {
                minValue = 1f
                maxValue = 1000f
                context.getString(R.string.valid_range, "1L", "1000L")
            }

            3 -> {
                minValue = 1f
                maxValue = 1000f
                context.getString(R.string.valid_range, "1s", "1000s")
            }

            4 -> {
                minValue = curPressUnit.convertUnit(1f, ConstantsUtils.KPA)
                maxValue = curPressUnit.convertUnit(30f, ConstantsUtils.KPA)
                lowLimit = "${
                    if (curPressUnit == ConstantsUtils.KPA) minValue else minValue.toDecimalNotationString(
                        3
                    )
                }$curPressUnit"
                upperLimit = "${
                    if (curPressUnit == ConstantsUtils.KPA) maxValue else maxValue.toDecimalNotationString(
                        3
                    )
                }$curPressUnit"
                context.getString(R.string.valid_range, lowLimit, upperLimit)
            }

            5 -> {
                minValue = 1f
                maxValue = 1000f
                context.getString(R.string.valid_range, "1s", "1000s")
            }

            6 -> {
                minValue = curPressUnit.convertUnit(1f, ConstantsUtils.KPA)
                maxValue = curPressUnit.convertUnit(35f, ConstantsUtils.KPA)
                lowLimit = "${
                    if (curPressUnit == ConstantsUtils.KPA) minValue else minValue.toDecimalNotationString(
                        3
                    )
                }$curPressUnit"
                upperLimit = "${
                    if (curPressUnit == ConstantsUtils.KPA) maxValue else maxValue.toDecimalNotationString(
                        3
                    )
                }$curPressUnit"
                context.getString(R.string.valid_range, lowLimit, upperLimit)
            }

            7 -> {
                minValue = 1f
                maxValue = 1000f
                context.getString(R.string.valid_range, "1s", "1000s")
            }

            8 -> {
                minValue = 0f
                maxValue = curPressUnit.convertUnit(30f, ConstantsUtils.KPA)
                lowLimit = "${minValue}$curPressUnit"
                upperLimit = "${
                    if (curPressUnit == ConstantsUtils.KPA) maxValue else maxValue.toDecimalNotationString(
                        3
                    )
                }$curPressUnit"
                context.getString(R.string.valid_range, lowLimit, upperLimit)
            }

            9 -> {
                minValue = 1f
                maxValue = 1000f
                context.getString(R.string.valid_range, "1s", "1000s")
            }

            10 -> {//高压测试压力
                minValue = curPressUnit.convertUnit(30f, ConstantsUtils.KPA)
                maxValue = curPressUnit.convertUnit(500f, ConstantsUtils.KPA)
                lowLimit = "${
                    if (curPressUnit == ConstantsUtils.KPA) minValue else minValue.toDecimalNotationString(
                        3
                    )
                }$curPressUnit"
                upperLimit = "${
                    if (curPressUnit == ConstantsUtils.KPA) maxValue else maxValue.toDecimalNotationString(
                        3
                    )
                }$curPressUnit"
                context.getString(R.string.valid_range, lowLimit, upperLimit)
            }

            11 -> {//高压上限
                minValue = curPressUnit.convertUnit(30f, ConstantsUtils.KPA)
                maxValue = curPressUnit.convertUnit(510f, ConstantsUtils.KPA)
                lowLimit = "${
                    if (curPressUnit == ConstantsUtils.KPA) minValue else minValue.toDecimalNotationString(
                        3
                    )
                }$curPressUnit"
                upperLimit = "${
                    if (curPressUnit == ConstantsUtils.KPA) maxValue else maxValue.toDecimalNotationString(
                        3
                    )
                }$curPressUnit"
                context.getString(R.string.valid_range, lowLimit, upperLimit)
            }

            12 -> {
                minValue = curLeakageUnit.convertUnit(1f, ConstantsUtils.PA)
                maxValue = curLeakageUnit.convertUnit(5000f, ConstantsUtils.PA)
                lowLimit = "${
                    if (curLeakageUnit == ConstantsUtils.PA) minValue else minValue.toDecimalNotationString(
                        5
                    )
                }$curLeakageUnit"
                upperLimit = "${
                    if (curLeakageUnit == ConstantsUtils.PA) maxValue else maxValue.toDecimalNotationString(
                        5
                    )
                }$curLeakageUnit"
                context.getString(R.string.valid_range, lowLimit, upperLimit)
            }

            13 -> {//下限压力范围
                minValue = 0f
                maxValue = curPressUnit.convertUnit(500f, ConstantsUtils.KPA)
                lowLimit = "${minValue}$curPressUnit"
                upperLimit = "${
                    if (curPressUnit == ConstantsUtils.KPA) maxValue else maxValue.toDecimalNotationString(
                        3
                    )
                }$curPressUnit"
                context.getString(R.string.valid_range, lowLimit, upperLimit)
            }

            else -> {
                ""
            }
        }
        mVb.showMessage.text = msg
    }

    fun showDialog(
        confirm: Int = R.string.btn_confirm,
        cancel: Int = R.string.btn_cancel,
        needCheck: Boolean,
        block: (parameter: String) -> Unit
    ) {
        setConfirmOnClickListener(
            confirm,
            isAutoDismiss = false,
            mClickListener = {
                val text = mVb.editParameter.text.toString()
                if (needCheck && !checkCompliance(text)) {
                    NToast.shortToast(context, context.getString(R.string.please_input_valid_range))
                    return@setConfirmOnClickListener
                }
                block(text)
                dismiss()
            }
        )
        setCancelOnClickListener(
            cancel,
            true,
            mClickListener = {}
        )
        show()
    }

    private fun checkCompliance(text: String): Boolean {
        val value = text.toFloatOrNull()
        if (value != null && (value in minValue..maxValue)) {
            return true
        }
        return false
    }
}