package com.power.baseproject.ktbase.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.power.baseproject.common.ActivityPackageManager
import com.power.baseproject.widget.LoadDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel

abstract class BaseActivity : AppCompatActivity(),
    CoroutineScope by CoroutineScope(Dispatchers.Main) {
    lateinit var currentFragment: BaseFragment

    /**
     * 中心加载弹框
     */
    protected var mCenterLoadingDialog: LoadDialog? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityPackageManager.instance.addActivity(this)
        if (needLoaddialog()) {
            mCenterLoadingDialog = LoadDialog(this)
        }
    }

    protected open fun needLoaddialog(): Boolean {
        return false
    }

    /**
     * 中心菊花
     *
     * @param msg
     */
    open fun showCenterLoading(msg: String) {
        mCenterLoadingDialog?.show(msg)
    }

    /**
     * 中心菊花
     *
     * @param msg
     */
    open fun showCenterLoading(msg: String, isCancel: Boolean) {
        mCenterLoadingDialog?.show(msg, isCancel)
    }

    /**
     * 中心菊花
     *
     * @param msg
     */
    open fun showCenterLoading(msg: Int, isCancel: Boolean) {
        mCenterLoadingDialog?.show(getString(msg), isCancel)
    }


    /**
     * 中心菊花
     *
     * @param msg
     */
    open fun showCenterLoading(msg: Int) {
        mCenterLoadingDialog?.show(getString(msg))
    }

    /**
     * 中心菊花
     */
    open fun hideCenterLoading() {
        mCenterLoadingDialog?.dismiss()
    }

    override fun onDestroy() {
        (this as CoroutineScope).cancel()
        ActivityPackageManager.instance.removeActivity(this)
        super.onDestroy()
    }
}