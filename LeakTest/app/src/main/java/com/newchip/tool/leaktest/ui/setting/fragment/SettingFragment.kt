package com.newchip.tool.leaktest.ui.setting.fragment

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.jeremyliao.liveeventbus.LiveEventBus
import com.newchip.tool.leaktest.R
import com.newchip.tool.leaktest.databinding.FragmentSettingBinding
import com.newchip.tool.leaktest.ui.setting.SettingViewModel
import com.newchip.tool.leaktest.widget.FactoryService
import com.power.baseproject.ktbase.ui.BaseAppFragment
import com.power.baseproject.utils.ConstantsUtils
import com.power.baseproject.utils.EasyPreferences
import com.power.baseproject.utils.LiveEventBusConstants
import com.power.baseproject.utils.Tools
import org.koin.androidx.viewmodel.ext.android.viewModel


class SettingFragment : BaseAppFragment<FragmentSettingBinding>() {
    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentSettingBinding.inflate(inflater, container, false)

    val vm: SettingViewModel by viewModel()
    override fun initView() {
        setTitle(R.string.system_setting)
        mVb.settingTableView.init(childFragmentManager, vm)
        initObserver()
        val factoryFlag = EasyPreferences.instance[ConstantsUtils.FACTORY_SERVICE_START_FLAG,true]
        if (factoryFlag) {
            if (Tools.iServiceRunning(mContext, FactoryService::class.java.simpleName)) {
                LiveEventBus.get(LiveEventBusConstants.CLOSE_FACTORY_TOOL).post(3)
                return
            }
            LiveEventBus.get(LiveEventBusConstants.SERVICE_STOP_INIT).post(true)
        }
    }

    private fun initObserver() {
        setOnTitleLongClick({})
        setOnBackClick {
            findNavController().popBackStack()
        }
        LiveEventBus.get(LiveEventBusConstants.SETTING_TITLE_NAME, Int::class.java).observe(this) {
            when (it) {
                1 -> setTitle(R.string.language)
                2 -> setTitle(R.string.wifi_connect)
                3 -> setTitle(R.string.log_management)
                4 -> setTitle(R.string.development_maintenance)
                5 -> setTitle(R.string.about)
                else -> setTitle(R.string.system_setting)
            }
        }
    }

    override fun createObserver() {
    }

    override fun lazyLoadData() {
    }
}