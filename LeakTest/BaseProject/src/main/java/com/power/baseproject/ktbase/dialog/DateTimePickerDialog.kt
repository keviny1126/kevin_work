package com.power.baseproject.ktbase.dialog

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.DatePicker
import android.widget.LinearLayout
import android.widget.NumberPicker
import android.widget.TimePicker
import com.power.baseproject.R
import com.power.baseproject.databinding.DateTimePickerLayoutBinding
import java.util.*

class DateTimePickerDialog(context: Context) : BaseDialog(context) {
    private var mContentView: View? = null
    private var mNumberPicker: List<NumberPicker>? = null
    private var dtBinding: DateTimePickerLayoutBinding =
        DateTimePickerLayoutBinding.inflate(layoutInflater)

    init {
        mContentView = dtBinding.root

        val window = window
        if (window != null) {
            val lp = window.attributes
            val width = (getContext().resources.displayMetrics.widthPixels * 0.5).toInt()
            lp.width = width
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT
            window.attributes = lp
            window.decorView.background.alpha = 0
        }
    }

    override fun createContentView(): View? {
        return mContentView
    }

    fun showDialog(
        title: Int = R.string.common_dialog_tip,
        confirm: Int = R.string.btn_confirm,
        cancel: Int = R.string.btn_cancel,
        mConfirmClick: View.OnClickListener? = null,
        timeListener: TimePicker.OnTimeChangedListener? = null,
        listener: DatePicker.OnDateChangedListener? = null
    ) {
        binding.tvTitle.setText(title)
        val cal = Calendar.getInstance()
        dtBinding.datePicker.init(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DATE),
            listener
        )
        dtBinding.timePicker.setOnTimeChangedListener(timeListener)
        dtBinding.timePicker.setIs24HourView(true)
        setConfirmOnClickListener(
            confirm,
            true,
            bg = R.drawable.selector_common_button,
            mClickListener = mConfirmClick
        )
        setCancelOnClickListener(
            cancel,
            true,
            mClickListener = null
        )
        show()
    }

    private fun setPickerStyle(datePicker: DatePicker) {
        mNumberPicker = findNumberPicker(datePicker)
        for (numberPicker in mNumberPicker!!) {
            resizeNumberPicker(numberPicker);
        }
    }

    private fun findNumberPicker(viewGroup: ViewGroup): List<NumberPicker> {
        val npList = arrayListOf<NumberPicker>()
        var child: View? = null
        for (i in 0 until viewGroup.childCount) {
            child = viewGroup.getChildAt(i)
            if (child is NumberPicker) {
                npList.add(child)
            } else if (child is LinearLayout) {
                val result = findNumberPicker(child as ViewGroup)
                if (result.isNotEmpty()) {
                    return result
                }
            }
        }
        return npList
    }

    private fun resizeNumberPicker(np: NumberPicker) {
        val params = LinearLayout.LayoutParams(
            context.resources.getDimension(R.dimen.dp_32).toInt(),
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(10, 0, 10, 0)
        np.layoutParams = params
    }
}