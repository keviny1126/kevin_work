package com.newchip.tool.leaktest.ui.setting.fragment

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.newchip.tool.leaktest.databinding.FragmentSettingUnitBinding
import com.newchip.tool.leaktest.ui.setting.adapter.UnitAdapter
import com.power.baseproject.ktbase.ui.BaseAppFragment
import com.power.baseproject.utils.ConstantsUtils
import com.power.baseproject.utils.EasyPreferences

class SettingUnitFragment : BaseAppFragment<FragmentSettingUnitBinding>() {
    private var mPressAdapter: UnitAdapter? = null
    private var mLeakageAdapter: UnitAdapter? = null
    private var mCurrPressSelect: Int = -1
    private var mCurrLeakageSelect: Int = -1
    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentSettingUnitBinding.inflate(inflater, container, false)

    override fun initView() {
        showOrHideHeadView(false)
        initRecycleView()
        initUnit()
        initClick()
    }

    private fun initUnit() {
        val pressUnit = EasyPreferences.instance[ConstantsUtils.PRESS_UNIT_TYPE, 0]
        val leakageUnit = EasyPreferences.instance[ConstantsUtils.LEAKAGE_UNIT_TYPE, 1]

        mCurrPressSelect = pressUnit
        mPressAdapter?.setSelectedId(mCurrPressSelect)

        mCurrLeakageSelect = leakageUnit
        mLeakageAdapter?.setSelectedId(mCurrLeakageSelect)
    }

    private fun initClick() {
        mPressAdapter?.itemClick { _, i ->
            EasyPreferences.instance.put(ConstantsUtils.PRESS_UNIT_TYPE, i)
            mPressAdapter?.notifyDataSetChanged()
        }

        mLeakageAdapter?.itemClick { _, i ->
            EasyPreferences.instance.put(ConstantsUtils.LEAKAGE_UNIT_TYPE, i)
            mLeakageAdapter?.notifyDataSetChanged()
        }
    }

    private fun initRecycleView() {
        val strList = arrayListOf(ConstantsUtils.KPA, ConstantsUtils.PA, ConstantsUtils.PSI)
        mPressAdapter = UnitAdapter(mContext, strList)
        mLeakageAdapter = UnitAdapter(mContext, strList)
        val layoutManager = LinearLayoutManager(context)
        mVb.rcvPressUnit.layoutManager = layoutManager
        mVb.rcvPressUnit.addItemDecoration(
            DividerItemDecoration(
                mContext,
                layoutManager.orientation
            )
        )
        val layoutManager2 = LinearLayoutManager(context)
        mVb.rcvLeakageUnit.layoutManager = layoutManager2
        mVb.rcvLeakageUnit.addItemDecoration(
            DividerItemDecoration(
                mContext,
                layoutManager2.orientation
            )
        )
        mVb.rcvPressUnit.adapter = mPressAdapter
        mVb.rcvLeakageUnit.adapter = mLeakageAdapter
    }

    override fun createObserver() {

    }

    override fun lazyLoadData() {
    }

    companion object {
        @JvmStatic
        fun newInstance() = SettingUnitFragment()
    }
}