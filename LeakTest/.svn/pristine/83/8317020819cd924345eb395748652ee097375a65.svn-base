package com.newchip.tool.leaktest.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.newchip.tool.leaktest.R
import com.newchip.tool.leaktest.ui.setting.*
import com.newchip.tool.leaktest.ui.setting.fragment.*

abstract class BaseSettingLeftTable @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    layoutId: Int
) : LinearLayout(context, attrs, defStyleAttr) {

    private var mFragmentManager: FragmentManager? = null
    private var mFragments: ArrayList<Fragment>? = null
    private lateinit var mViewModel: SettingViewModel
    private var currentFragment: Fragment? = null

    /**
     * 外部调用初始化，传入必要的参数
     *
     * @param fm
     */
    fun init(fm: FragmentManager?, viewModel: SettingViewModel) {
        mFragmentManager = fm
        mViewModel = viewModel
        if (mFragments == null) {
            mFragments = arrayListOf()
            mFragments?.apply {
                add(getCurrentFragment(0)!!)
                add(getCurrentFragment(1)!!)
                add(getCurrentFragment(2)!!)
                add(getCurrentFragment(3)!!)
                add(getCurrentFragment(4)!!)
                add(getCurrentFragment(5)!!)
            }
        }
        fragmentManger(viewModel.getPage() ?: 0)
    }

    /**
     * 初始化 设置点击事件。
     *
     * @param view /
     */
    @Suppress("LeakingThis")
    abstract fun initView(view: View)

    /**
     * 销毁，避免内存泄漏
     */
    open fun destroy() {
        if (null != mFragmentManager) {
            if (!mFragmentManager!!.isDestroyed)
                mFragmentManager = null
        }
        if (!mFragments.isNullOrEmpty()) {
            mFragments?.clear()
            mFragments = null
        }
    }

    /**
     * fragment的切换 实现底部导航栏的切换
     *
     * @param position 序号
     */
    protected open fun fragmentManger(position: Int) {
        mViewModel.setPage(position)
        val targetFg: Fragment = mFragments!![position]
        val transaction = mFragmentManager!!.beginTransaction()
        transaction.apply {
            if (currentFragment != null) {
                hide(currentFragment!!)
            }
            setReorderingAllowed(true)
            if (!targetFg.isAdded) {
                add(R.id.flSettingFragment, targetFg).commit()
            } else {
                show(targetFg).commit()
            }
        }
        currentFragment = targetFg
    }

    init {
        initView(View.inflate(context, layoutId, this))
    }

    private val languageFragment: SettingLanguageFragment by lazy { SettingLanguageFragment.newInstance() }
    private val wifiFragment: SettingWifiFragment by lazy { SettingWifiFragment.newInstance() }
    private val developMaintenanceFragment: SettingDevelopMaintenanceFragment by lazy { SettingDevelopMaintenanceFragment.newInstance() }
    private val logManagementFragment: SettingLogManagementFragment by lazy { SettingLogManagementFragment.newInstance() }
    private val aboutFragment: SettingAboutFragment by lazy { SettingAboutFragment.newInstance() }
    private val unitSetFragment: SettingUnitFragment by lazy { SettingUnitFragment.newInstance() }

    private fun getCurrentFragment(index: Int): Fragment? {
        return when (index) {
            0 -> languageFragment
            1 -> wifiFragment
            2 -> logManagementFragment
            3 -> unitSetFragment
            4 -> developMaintenanceFragment
            5 -> aboutFragment
            else -> null
        }
    }
}