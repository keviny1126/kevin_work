package com.newchip.tool.leaktest.ui.data.adapter

import android.widget.CheckBox
import com.baozi.treerecyclerview.base.ViewHolder
import com.baozi.treerecyclerview.item.TreeItem
import com.jeremyliao.liveeventbus.LiveEventBus
import com.newchip.tool.leaktest.R
import com.power.baseproject.db.entity.TestData
import com.power.baseproject.utils.LiveEventBusConstants
import com.power.baseproject.utils.log.LogUtil

class DataItem : TreeItem<TestData>() {

    override fun getLayoutId(): Int {
        return R.layout.item_record_data_layout
    }

    override fun onBindViewHolder(viewHolder: ViewHolder) {
        viewHolder.setText(R.id.tv_workpiece_no, data.workpieceNo)
        viewHolder.setText(R.id.tv_testTime_value, data.testTime)
        viewHolder.setText(R.id.tv_testPressure_value, "${data.testPressure} kpa")
        viewHolder.setText(R.id.tv_leakageValue_value, "${data.leakage} pa")
        viewHolder.setText(R.id.tv_testResult_value, data.testResult)
        val parentItem = parentItem
        if (parentItem is DataDateItemParent) {
            viewHolder.setChecked(R.id.cb_select, parentItem.isSelect(this))
            viewHolder.getView<CheckBox>(R.id.cb_select).setOnClickListener {
                val index: Int = parentItem.selectItems.indexOf(this)
                LogUtil.i("kevin","是否选择 index:$index")
                if (index == -1) { //不存在则添加
                    parentItem.selectItems.add(this)
                } else { //存在则删除
                    parentItem.selectItems.removeAt(index)
                }
                LiveEventBus.get(LiveEventBusConstants.REFRESH_ON_ITEM_CLICK_VIEW).post("1")
            }
        }
    }

    //这个Item,在RecyclerView的每行所占比,只有RecyclerView设置了GridLayoutManager才会生效.
    //这里之所以用除法,是为了可以做到,只改变GridLayoutManager的总数,无需改变每个Item,当然也可以直接返回一个int值.
    override fun getSpanSize(maxSpan: Int): Int {
        return maxSpan / 3
    }
}