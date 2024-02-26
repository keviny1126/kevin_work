package com.newchip.tool.leaktest.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.newchip.tool.leaktest.R
import com.newchip.tool.leaktest.databinding.FragmentMainBinding
import com.power.baseproject.ktbase.ui.BaseAppFragment
import com.power.baseproject.utils.CmdControl
import com.power.baseproject.utils.ViewClickDelay
import com.power.baseproject.utils.clicks

class MainFragment : BaseAppFragment<FragmentMainBinding>() {

    private var clicknum = 0
    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentMainBinding.inflate(inflater, container, false)

    override fun initView() {
        setTitle(R.string.main_title)
        initClick()
    }

    private fun initClick() {
        mVb.imgData clicks {
            val actions = MainFragmentDirections.actionMainFragmentToDataFragmentNew()
            findNavController().navigate(actions)
        }

        mVb.imgDetection clicks {
            val actions = MainFragmentDirections.actionMainFragmentToLowVoltageModeFragment()
            findNavController().navigate(actions)
        }

        mVb.imgSetting clicks {
            val actions = MainFragmentDirections.actionMainFragmentToSettingFragment()
            findNavController().navigate(actions)
        }
        binding.tvTitleShow.setOnClickListener {
            if (clicknum == 0) {
                clicknum++
            }
            if (ViewClickDelay.isFastClick) {
                clicknum++
            } else {
                clicknum = 0
            }
            if (clicknum == 5) {
                CmdControl.setDeviceMode()
            }
        }
        setOnTitleLongClick({})
    }

    override fun createObserver() {
    }

    override fun lazyLoadData() {
    }

    override fun onBackPressed(): Boolean {
        //首页不退出APP
        return true
    }
}