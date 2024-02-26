package com.newchip.tool.leaktest.ui.setting.fragment

import android.content.Intent
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.newchip.tool.leaktest.R
import com.newchip.tool.leaktest.databinding.FragmentSeniorSettingBinding
import com.newchip.tool.leaktest.ui.factory.FactoryActivity
import com.power.baseproject.ktbase.dialog.MessageDialog
import com.power.baseproject.ktbase.ui.BaseAppFragment
import com.power.baseproject.utils.CmdControl
import com.power.baseproject.utils.ConstantsUtils
import com.power.baseproject.utils.FileUtils
import com.power.baseproject.utils.PathUtils
import com.power.baseproject.utils.ViewClickDelay
import com.power.baseproject.utils.clicks
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class SeniorSettingFragment : BaseAppFragment<FragmentSeniorSettingBinding>() {
    var msgDialog: MessageDialog? = null
    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentSeniorSettingBinding.inflate(inflater, container, false)

    private var clicknum = 0
    override fun initView() {
        setTitle(R.string.advanced_setting)
        initClick()
    }

    override fun onResume() {
        super.onResume()
        launch(Dispatchers.IO) {
            CmdControl.showOrHideStatusBar(true)
        }
    }

    private fun initClick() {
        setOnTitleLongClick({})
        setOnBackClick {
            activity?.finish()
        }
        mVb.updateBootAnimation clicks {
            val fileName = PathUtils.getZipPath() + ConstantsUtils.BOOT_ANIMATION_ZIP
            if (!FileUtils.checkFileIsExists(fileName)) {
                FileUtils.copyAssetsFile(mContext, PathUtils.getZipPath())
            }
            val result = CmdControl.updateBootAnimation(fileName)
            val msg =
                if (result) getString(R.string.boot_animation_update_success) else getString(R.string.boot_animation_update_failed)
            showMessageDialog(msg) {}
        }
        mVb.deleteBootAnimation clicks {
            val result = CmdControl.deleteBootAnimation()
            val msg =
                if (result) getString(R.string.boot_animation_delete_success) else getString(R.string.boot_animation_delete_failed)
            showMessageDialog(msg) {}
        }
        mVb.androidSetting clicks {
            launch(Dispatchers.IO) {
                CmdControl.showOrHideStatusBar(false)
            }
            val intent = Intent(Settings.ACTION_SETTINGS)
            startActivity(intent)
        }
        mVb.startAging clicks {
            FactoryActivity.actionStart(mContext)
        }
        mVb.tvShowFactory.setOnClickListener {
            if (clicknum == 0) {
                clicknum++
            }
            if (ViewClickDelay.isFastClick) {
                clicknum++
            } else {
                clicknum = 0
            }
            if (clicknum == 5) {
                showFactory()
            }
        }
    }

    private fun showFactory() {
        mVb.startAging.visibility = View.VISIBLE
        mVb.tvLine3.visibility = View.VISIBLE
        mVb.imgNext4.visibility = View.VISIBLE
        mVb.tvAging.visibility = View.VISIBLE
    }

    private fun showMessageDialog(
        msg: String,
        block: () -> Unit
    ) {
        if (msgDialog != null && msgDialog!!.isShowing) {
            msgDialog?.dismiss()
            msgDialog = null
        }
        msgDialog = MessageDialog(mContext)
        msgDialog?.showMessage(msg = msg, mConfirmClick = {
            block()
        })
    }

    override fun createObserver() {

    }

    override fun lazyLoadData() {

    }
}