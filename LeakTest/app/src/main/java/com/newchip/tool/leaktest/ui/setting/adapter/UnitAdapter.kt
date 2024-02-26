package com.newchip.tool.leaktest.ui.setting.adapter

import android.content.Context
import android.view.View
import com.newchip.tool.leaktest.databinding.UnitItemLayoutBinding
import com.power.baseproject.ktbase.ui.BaseAdapter

class UnitAdapter(context: Context, unitList: MutableList<String>) :
    BaseAdapter<UnitItemLayoutBinding, String>(context, unitList) {
    override fun convert(v: UnitItemLayoutBinding, t: String, position: Int) {
        v.tvUnitName.text = t
        updateSelect(v, position)
    }

    private fun updateSelect(v: UnitItemLayoutBinding, position: Int) {
        if (clickPos == position) {
            v.imgSelect.visibility = View.VISIBLE
            return
        }
        v.imgSelect.visibility = View.GONE
    }

    fun setSelectedId(pos: Int) {
        clickPos = pos
        notifyDataSetChanged()
    }
}