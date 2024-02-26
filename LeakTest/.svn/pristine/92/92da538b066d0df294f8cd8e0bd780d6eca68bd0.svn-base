package com.newchip.tool.leaktest.ui.data.adapter

import android.content.Context
import android.graphics.Typeface
import com.newchip.tool.leaktest.databinding.ItemDataListLayoutBinding
import com.power.baseproject.db.entity.TestData
import com.power.baseproject.ktbase.ui.BaseAdapter

class TestDataAdapter(context: Context, dataList: MutableList<TestData>) :
    BaseAdapter<ItemDataListLayoutBinding, TestData>(context, dataList) {

    private var selectListener: ((Boolean) -> Unit)? = null
    fun checkSelectListener(listener: (Boolean) -> Unit) {
        this.selectListener = listener
    }

    override fun convert(v: ItemDataListLayoutBinding, t: TestData, position: Int) {
        v.tvName.text = t.workpieceNo
        v.tvName.typeface = if (clickPos == position) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
        v.cbSelected.isSelected = t.isChecked
        v.cbSelected.setOnClickListener {
            run {
                t.isChecked = !v.cbSelected.isSelected
                v.cbSelected.isSelected = t.isChecked
                selectListener?.let {
                    it(checkSelectItem())
                }
            }
        }
    }

    private fun checkSelectItem(): Boolean {
        for (data in listDatas) {
            if (!data.isChecked) {
                return false
            }
        }
        return true
    }

    fun getSelectData(): MutableList<TestData> {
        val selectList = mutableListOf<TestData>()
        for (data in listDatas) {
            if (data.isChecked) {
                selectList.add(data)
            }
        }
        return selectList
    }

    fun setSelectAll(needSelectAll: Boolean) {
        for (data in listDatas) {
            data.isChecked = needSelectAll
        }
        notifyDataSetChanged()
    }

    fun setSelectData(position: Int, isChecked: Boolean) {
        val data = listDatas[position]
        data.isChecked = isChecked
        notifyItemChanged(position)
    }
}