package com.power.baseproject.ktbase.dialog

import android.content.Context
import android.view.View
import com.power.baseproject.R

class MessageDialog(context: Context) : BaseDialog(context) {
    override fun createContentView(): View? {
        return null
    }

    public fun showMessage(
        title: Int = R.string.common_dialog_tip,
        msg: String,
        confirm: Int = R.string.btn_confirm,
        cancel: Int = R.string.btn_cancel,
        confirmBg: Int = R.drawable.selector_common_button,
        mCancelClick: View.OnClickListener? = null,
        mConfirmClick: View.OnClickListener? = null
    ) {
        binding.tvTitle.setText(title)
        binding.tvMessage.text = msg
        var singleBg = confirmBg
        if (mCancelClick != null) {
            setCancelOnClickListener(
                cancel,
                true,
                mClickListener = mCancelClick
            )
        }else{
            singleBg = R.drawable.selector_common_button
        }
        if (mConfirmClick != null) setConfirmOnClickListener(
            confirm,
            bg = singleBg,
            isAutoDismiss = true,
            mClickListener = mConfirmClick
        )
        show()
    }
}