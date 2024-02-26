package com.power.baseproject.ktbase.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import com.power.baseproject.R
import com.power.baseproject.databinding.BaseDialogLayoutBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel


abstract class BaseDialog(context: Context) : Dialog(context),
    CoroutineScope by CoroutineScope(
        Dispatchers.Main
    ) {
    public lateinit var binding: BaseDialogLayoutBinding
    private var mCancelClickListener: View.OnClickListener? = null
    private var mConfirmClickListener: View.OnClickListener? = null
    private var isCancelAutoDismiss = true
    private var isConfirmAutoDismiss = true
    private var contentLayoutView: View? = null

    init {
        getContext().setTheme(android.R.style.Theme_DeviceDefault_Dialog_NoActionBar_MinWidth)
        binding = BaseDialogLayoutBinding.inflate(layoutInflater)
        val view = binding.root
        super.setContentView(view)
        binding.btnConfirm.setOnClickListener {
            doingConfirmClick(it)
        }
        binding.btnCancel.setOnClickListener {
            doingCancelClick(it)
        }

        val window = window
        if (window != null) {
            val lp = window.attributes
            val outValue = TypedValue()
            context.resources.getValue(
                R.dimen.base_dialog_width,
                outValue,
                true
            )
            val value = outValue.float
            val width = (getContext().resources.displayMetrics.widthPixels * value).toInt()
            lp.width = width
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT
            window.attributes = lp
            window.decorView.background.alpha = 0
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        contentLayoutView = createContentView()
        if (contentLayoutView != null) {
            binding.scvMessage.visibility = View.GONE
            setContentLayoutView(contentLayoutView!!)
        }
    }

    public abstract fun createContentView(): View?
    public fun setContentLayoutView(view: View) {
        val lp = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        view.layoutParams = lp
        binding.flContent.visibility = View.VISIBLE
        binding.flContent.addView(view)
    }

    public fun setDialogTitle(resText: Int) {
        binding.tvTitle.setText(resText)
    }

    public fun setCancelOnClickListener(
        resText: Int,
        isAutoDismiss: Boolean,
        mClickListener: View.OnClickListener?
    ) {
        binding.btnCancel.setText(resText)
        binding.btnCancel.visibility = View.VISIBLE
        isCancelAutoDismiss = isAutoDismiss
        mCancelClickListener = mClickListener
    }

    public fun setConfirmOnClickListener(
        resText: Int,
        isAutoDismiss: Boolean,
        bg: Int? = null,
        mClickListener: View.OnClickListener?
    ) {
        binding.btnConfirm.setText(resText)
        binding.btnConfirm.visibility = View.VISIBLE
        if (bg != null) binding.btnConfirm.setBackgroundResource(bg)
        isConfirmAutoDismiss = isAutoDismiss
        mConfirmClickListener = mClickListener
    }

    public fun doingCancelClick(v: View) {
        mCancelClickListener?.onClick(v)
        if (isCancelAutoDismiss) {
            dismiss()
        }
    }

    public fun doingConfirmClick(v: View) {
        mConfirmClickListener?.onClick(v)
        if (isConfirmAutoDismiss) {
            dismiss()
        }
    }

    override fun dismiss() {
        (this as CoroutineScope).cancel()
        super.dismiss()
    }
}