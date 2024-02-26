package com.newchip.tool.leaktest.ui.data.adapter

import android.widget.CheckBox
import com.baozi.treerecyclerview.base.ViewHolder
import com.baozi.treerecyclerview.factory.ItemHelperFactory
import com.baozi.treerecyclerview.item.TreeItem
import com.baozi.treerecyclerview.item.TreeSelectItemGroup
import com.jeremyliao.liveeventbus.LiveEventBus
import com.newchip.tool.leaktest.R
import com.power.baseproject.bean.DateAndRecordDataBean
import com.power.baseproject.ktbase.application.BaseApplication
import com.power.baseproject.utils.LiveEventBusConstants

class DataDateItemParent : TreeSelectItemGroup<DateAndRecordDataBean>() {

    init {
        isExpand = true
    }

    override fun onBindViewHolder(viewHolder: ViewHolder) {
        viewHolder.setText(
            R.id.tv_data_number,
            BaseApplication.getContext()
                .getString(R.string.record_number, data.dataRecordList.size.toString())
        )
        viewHolder.setText(R.id.tv_date, data.dateTime)
        viewHolder.setChecked(R.id.img_more, isExpand)
        viewHolder.getView<CheckBox>(R.id.img_more).setOnClickListener {
            //必须是TreeItemGroup才能展开折叠,并且type不能为 TreeRecyclerType.SHOW_ALL
            isExpand = !isExpand
//            selectAll(!isSelectAll, true)
//            LiveEventBus.get(LiveEventBusConstants.REFRESH_ON_ITEM_CLICK_VIEW).post("1")
        }
    }

    override fun initChild(data: DateAndRecordDataBean): MutableList<TreeItem<Any>> {
        return ItemHelperFactory.createItems(data.dataRecordList, DataItem::class.java, this)
    }

    override fun isCanExpand(): Boolean {
        return true
    }

    override fun getLayoutId(): Int {
        return R.layout.item_test_data_layout
    }
}