package com.newchip.tool.leaktest.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.jeremyliao.liveeventbus.LiveEventBus
import com.newchip.tool.leaktest.R
import com.power.baseproject.utils.LiveEventBusConstants

class SettingLeftTable @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : BaseSettingLeftTable(context, attrs, defStyleAttr, R.layout.fragment_setting_left_tab_new),
    View.OnClickListener {

    private var viewList: ArrayList<TextView>? = null
    override fun initView(view: View) {
        view.apply {
            viewList = arrayListOf(
                findViewById(R.id.tv_language),
                findViewById(R.id.tv_wifi),
                findViewById(R.id.tv_log),
                findViewById(R.id.tvUnit),
                findViewById(R.id.tv_develop),
                findViewById(R.id.tv_about)
            )
        }
        for (textview in viewList!!) {
            textview.setOnClickListener(this)
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.tv_language -> {
                fragmentManger(0)
                LiveEventBus.get(LiveEventBusConstants.SETTING_TITLE_NAME).post(1)
            }
            R.id.tv_wifi -> {
                fragmentManger(1)
                LiveEventBus.get(LiveEventBusConstants.SETTING_TITLE_NAME).post(2)
            }
            R.id.tv_log -> {
                fragmentManger(2)
                LiveEventBus.get(LiveEventBusConstants.SETTING_TITLE_NAME).post(3)
            }
            R.id.tvUnit ->{
                fragmentManger(3)
                LiveEventBus.get(LiveEventBusConstants.SETTING_TITLE_NAME).post(4)

            }
            R.id.tv_develop -> {
                fragmentManger(4)
                LiveEventBus.get(LiveEventBusConstants.SETTING_TITLE_NAME).post(5)
            }
            R.id.tv_about -> {
                fragmentManger(5)
                LiveEventBus.get(LiveEventBusConstants.SETTING_TITLE_NAME).post(6)
            }
        }
    }

//    fun setOnLeftBack(block: () -> Unit) {
//        findViewById<TextView>(R.id.btn_left_back).setOnClickListener {
//            block()
//        }
//    }

    override fun fragmentManger(position: Int) {
        super.fragmentManger(position)
        for (i in viewList!!.indices) {
            viewList!![i].isSelected = (position == i)
        }
    }

    override fun destroy() {
        super.destroy()
        viewList?.clear()
        viewList = null
    }
}