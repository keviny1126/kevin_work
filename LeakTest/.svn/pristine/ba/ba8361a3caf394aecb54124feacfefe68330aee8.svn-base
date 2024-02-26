package com.newchip.tool.leaktest.widget

import android.content.Context
import android.view.View
import com.newchip.tool.leaktest.databinding.DialogProgressLayoutBinding
import com.power.baseproject.ktbase.dialog.BaseDialog

class ProgressDialog(context: Context) : BaseDialog(context) {
    private var mContentView: View? = null
    private var pbBinding: DialogProgressLayoutBinding =
        DialogProgressLayoutBinding.inflate(layoutInflater)

    init {
        mContentView = pbBinding.root
    }

    override fun createContentView(): View? {
        return mContentView
    }
    fun hideDialogTitle() {
        binding.tvTitle.visibility = View.GONE
    }
    fun setProgressBar(value: Int) {
        pbBinding.pbItem.progress = value
        pbBinding.tvShowValue.text = "$value%"
    }
}