package com.newchip.tool.leaktest.widget

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.EditText
import com.newchip.tool.leaktest.R
import com.newchip.tool.leaktest.databinding.InputPasswordLayoutBinding
import com.power.baseproject.ktbase.dialog.BaseDialog

class InputPasswordDialog(context: Context) : BaseDialog(context) {
    private var mContentView: View? = null
    private var mVb: InputPasswordLayoutBinding =
        InputPasswordLayoutBinding.inflate(layoutInflater)

    init {
        mContentView = mVb.root
        binding.tvTitle.setText(R.string.connect_wifi)
        initClick()
    }

    override fun createContentView(): View? {
        return mContentView
    }

    private fun initClick() {
        mVb.cbEye.setOnCheckedChangeListener { buttonView, isChecked ->
            mVb.editPassword.transformationMethod =
                if (isChecked) HideReturnsTransformationMethod.getInstance() else PasswordTransformationMethod.getInstance()
        }
        mVb.editPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                mVb.toastShowError.visibility = View.GONE
            }

            override fun afterTextChanged(s: Editable?) {
            }

        })
    }

    fun setTitle(title:String){
        binding.tvTitle.text = title
    }

    fun getPswText(): String {
        return mVb.editPassword.text.toString()
    }

    fun getPswView(): EditText {
        return mVb.editPassword
    }

    fun showErrorMsg(msg: String) {
        mVb.toastShowError.visibility = View.VISIBLE
        mVb.toastShowError.text = msg
    }

    fun showDialog(
        confirm: Int = R.string.btn_confirm,
        cancel: Int = R.string.btn_cancel,
        mCancelClick: View.OnClickListener? = null,
        mConfirmClick: View.OnClickListener? = null
    ) {
        if (mConfirmClick != null) setConfirmOnClickListener(
            confirm,
            isAutoDismiss = true,
            mClickListener = mConfirmClick
        )
        if (mCancelClick != null) setCancelOnClickListener(
            cancel,
            true,
            mClickListener = mCancelClick
        )
        show()
    }

}