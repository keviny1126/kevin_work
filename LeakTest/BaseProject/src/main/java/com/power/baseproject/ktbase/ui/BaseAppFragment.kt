package com.power.baseproject.ktbase.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.viewbinding.ViewBinding
import com.power.baseproject.R
import com.power.baseproject.databinding.BaseLayoutFragmentBinding
import com.power.baseproject.utils.DeviceUtils
import com.power.baseproject.utils.StatusBarUtil
import com.power.baseproject.utils.Tools
import com.power.baseproject.utils.clicks
import com.power.baseproject.widget.NToast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

abstract class BaseAppFragment<VB : ViewBinding> : BaseFragment(),
    CoroutineScope by CoroutineScope(
        Dispatchers.Main
    ) {
    private lateinit var _mVb: VB
    protected val mVb get() = _mVb
    private lateinit var _binding: BaseLayoutFragmentBinding
    protected val binding get() = _binding
    protected lateinit var mRootview: View

    /**
     * 当侵入状态栏时， 状态栏的占位控件
     */
    var mStatusPlaceholderView: View? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val linearLayout = LinearLayout(activity)
        linearLayout.orientation = LinearLayout.VERTICAL
        linearLayout.layoutParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        _binding = BaseLayoutFragmentBinding.inflate(inflater, container, false)
        val view = binding.root
        initStatusBar(false, R.color.color_00A65C, null)
        if (setToolbarView()) {
            mStatusPlaceholderView = View(context)
            mStatusPlaceholderView!!.layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, DeviceUtils.getStatuBarHeight(
                    requireContext()
                )
            )
            mStatusPlaceholderView!!.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.transparent
                )
            )
            linearLayout.addView(mStatusPlaceholderView)
        }

        _mVb = getViewBinding(inflater, container)
        binding.flTotalBody.addView(mVb.root)

        linearLayout.addView(view)

        mRootview = linearLayout
        return linearLayout
    }

    fun showOrHideHeadView(visible: Boolean) {
        binding.rlRoot.visibility = if (visible) View.VISIBLE else View.GONE
    }

    fun setTitle(titleId: Int) {
        binding.tvTitleShow.setText(titleId)
    }

    fun setOnBackClick(block: () -> Unit) {
        binding.imgLeft.visibility = View.VISIBLE
        binding.imgLeft clicks {
            block()
        }
    }

    fun setOnRightClick(block: () -> Unit) {
        binding.imgRight.visibility = View.VISIBLE
        binding.imgRight clicks {
            block()
        }
    }

    fun setOnTitleLongClick(block: () -> Unit, needScreen: Boolean = true) {
        binding.tvTitleShow.setOnLongClickListener {
            launch {
                if (needScreen) {
                    val result = Tools.saveScreenImage()
                    if (result) NToast.shortToast(
                        mContext,
                        R.string.screen_cap_success
                    ) else NToast.shortToast(
                        mContext,
                        R.string.screen_cap_failed
                    )
                }
                block()
            }
            true
        }
    }

    open fun initStatusBar(light: Boolean, statusBarColor: Int, view: View?) {
        if (activity != null) {
            if (statusBarColor != 0) {
                StatusBarUtil.setColorNoTranslucent(
                    requireActivity(),
                    ContextCompat.getColor(requireActivity(), statusBarColor)
                )
            } else {
                StatusBarUtil.setTranslucentForImageView(requireActivity(), 0, view)
            }
            if (light) {
                StatusBarUtil.setLightMode(requireActivity())
            }
        }
    }

    protected open fun setToolbarView(): Boolean {
        return false
    }

    protected abstract fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): VB

    override fun onDestroy() {
        (this as CoroutineScope).cancel()
        super.onDestroy()
    }
}