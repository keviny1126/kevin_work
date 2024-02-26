package com.newchip.tool.leaktest.ui.setting.fragment

import android.os.Build
import android.os.LocaleList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.newchip.tool.leaktest.R
import com.newchip.tool.leaktest.databinding.FragmentLanguageBinding
import com.newchip.tool.leaktest.ui.setting.adapter.LanguageAdapter
import com.power.baseproject.common.ActivityPackageManager
import com.power.baseproject.ktbase.dialog.MessageDialog
import com.power.baseproject.ktbase.ui.BaseAppFragment
import com.power.baseproject.utils.CmdControl
import com.power.baseproject.utils.DeviceUtils
import java.util.Locale

class SettingLanguageFragment : BaseAppFragment<FragmentLanguageBinding>() {

    private var mAdapter: LanguageAdapter? = null
    private var languageList = arrayListOf<String>()
    private var mCurrSelectedPosition: Int = -1
    private var messageDialog: MessageDialog? = null
    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentLanguageBinding.inflate(inflater, container, false)

    override fun initView() {
        showOrHideHeadView(false)
        val strList = resources.getStringArray(R.array.languages)
        for (language in strList) {
            languageList.add(language)
        }
        mAdapter = LanguageAdapter(requireContext(), languageList)
        val layoutManager = LinearLayoutManager(context)
        mVb.rcvLanguage.layoutManager = layoutManager
        mVb.rcvLanguage.addItemDecoration(
            DividerItemDecoration(
                mContext,
                layoutManager.orientation
            )
        )
        mVb.rcvLanguage.adapter = mAdapter
        initClick()
        initLanguage()
    }

    private fun initLanguage() {
        val locale: Locale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            LocaleList.getDefault()[0]
        } else {
            Locale.getDefault()
        }
        val id: Int = when (locale) {
            Locale.US -> LANG_EN
            Locale.SIMPLIFIED_CHINESE -> LANG_SIMPLIFIED_CHINESE
            Locale.TRADITIONAL_CHINESE -> LANG_TRADITIONAL_CHINESE
            Locale.GERMAN -> LANG_DE
            Locale.JAPANESE -> LANG_JA
            Locale.FRENCH -> LANG_FR
            Locale("es") -> LANG_ES
            Locale("pt") -> LANG_PT
            Locale.ITALIAN -> LANG_IT
            Locale("tr") -> LANG_TR
            Locale.KOREAN -> LANG_KO
            Locale("ru") -> LANG_RU
            Locale("ar") -> LANG_AR
            else -> LANG_SIMPLIFIED_CHINESE
        }
        mCurrSelectedPosition = id
        mAdapter?.setSelectedId(mCurrSelectedPosition)
    }

    private fun changeLanguage(position: Int) {
        if (mCurrSelectedPosition == position) {
            return
        }
        when (position) {
            LANG_EN -> DeviceUtils.changeSystemLanguage(Locale.US)
            LANG_SIMPLIFIED_CHINESE -> DeviceUtils.changeSystemLanguage(Locale.SIMPLIFIED_CHINESE)
            LANG_TRADITIONAL_CHINESE -> DeviceUtils.changeSystemLanguage(Locale.TRADITIONAL_CHINESE)
            LANG_DE -> DeviceUtils.changeSystemLanguage(Locale.GERMAN)
            LANG_JA -> DeviceUtils.changeSystemLanguage(Locale.JAPANESE)
            LANG_FR -> DeviceUtils.changeSystemLanguage(Locale.FRENCH)
            LANG_ES -> DeviceUtils.changeSystemLanguage(Locale("es"))
            LANG_PT -> DeviceUtils.changeSystemLanguage(Locale("pt"))
            LANG_IT -> DeviceUtils.changeSystemLanguage(Locale.ITALIAN)
            LANG_TR -> DeviceUtils.changeSystemLanguage(Locale("tr"))
            LANG_KO -> DeviceUtils.changeSystemLanguage(Locale.KOREAN)
            LANG_RU -> DeviceUtils.changeSystemLanguage(Locale("ru"))
            LANG_AR -> DeviceUtils.changeSystemLanguage(Locale("ar"))
            else -> DeviceUtils.changeSystemLanguage(Locale.SIMPLIFIED_CHINESE)
        }
        ActivityPackageManager.instance.finishAllActivity()
        CmdControl.restartAppCommand()
        activity?.finish()
    }

    private fun initClick() {
        mAdapter?.itemClick { _, i ->
            showMessage(getString(R.string.tip_lang_set)) {
                changeLanguage(i)
            }
        }
    }

    private fun showMessage(msg: String, onClickListener: View.OnClickListener) {
        messageDialog?.dismiss()
        messageDialog = null

        messageDialog = MessageDialog(mContext)
        messageDialog?.showMessage(
            msg = msg,
            mCancelClick = {},
            mConfirmClick = onClickListener
        )
    }

    override fun createObserver() {

    }

    override fun lazyLoadData() {
    }

    companion object {
        const val LANG_SIMPLIFIED_CHINESE = 0
        const val LANG_TRADITIONAL_CHINESE = 1
        const val LANG_EN = 2
        const val LANG_DE = 3
        const val LANG_JA = 4
        const val LANG_FR = 5
        const val LANG_ES = 6
        const val LANG_PT = 7
        const val LANG_IT = 8
        const val LANG_TR = 9
        const val LANG_KO = 10
        const val LANG_RU = 11
        const val LANG_AR = 12

        @JvmStatic
        fun newInstance() = SettingLanguageFragment()
    }
}