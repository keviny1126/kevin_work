package com.power.baseproject.ktbase.ui

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import com.power.baseproject.ktbase.application.BaseApplication
import com.power.baseproject.widget.LoadDialog

abstract class BaseFragment : Fragment() {

    private var handler = Handler()

    private var isFirst: Boolean = true

    /**
     * 中心加载弹框
     */
    protected var mCenterLoadingDialog: LoadDialog? = null
    protected lateinit var mContext: Context
    protected val mActivity by lazy { activity as BaseActivity }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        isFirst = true

        if (needLoaddialog()) {
            mCenterLoadingDialog = LoadDialog(requireActivity())
        }
        mContext = if (context == null) BaseApplication.getContext() else requireContext()

        initView()
        createObserver()
        initData()
        mActivity.currentFragment = this
    }

    override fun onResume() {
        super.onResume()
        onVisible()
    }

    /**
     * Fragment执行onViewCreated后触发
     */

    open fun initData() {}
    abstract fun initView()

    /**
     * 创建LiveData观察者 Fragment执行onViewCreated后触发
     */
    abstract fun createObserver()

    /**
     * 供子类初始化Databinding操作
     */
    open fun initDataBind() {}

    /**
     * fragment拦截返回键返回true，不拦截返回false
     */
    open fun onBackPressed(): Boolean {
        return false
    }

    /**
     * 是否需要懒加载
     */
    private fun onVisible() {
        Log.e("ykw", "${this.javaClass.name} --  ${lifecycle.currentState}")
        if (lifecycle.currentState == Lifecycle.State.STARTED && isFirst) {
            // 延迟加载 防止 切换动画还没执行完毕时数据就已经加载好了，这时页面会有渲染卡顿
            handler.postDelayed({
                lazyLoadData()
                isFirst = false
            }, lazyLoadTime())
        }
    }

    /**
     * 懒加载
     */
    abstract fun lazyLoadData()

    /**
     * 延迟加载 防止 切换动画还没执行完毕时数据就已经加载好了，这时页面会有渲染卡顿  bug
     * 这里传入你想要延迟的时间，延迟时间可以设置比转场动画时间长一点 单位： 毫秒
     * 不传默认 300毫秒
     * @return Long
     */
    open fun lazyLoadTime(): Long {
        return 300
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
}