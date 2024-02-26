package com.newchip.tool.leaktest.widget

import android.content.Context
import android.view.View
import com.newchip.tool.leaktest.R
import com.newchip.tool.leaktest.databinding.DialogLoadingProgressLayoutBinding
import com.power.baseproject.ktbase.dialog.BaseDialog

class ProgressLoadingDialog(context: Context) : BaseDialog(context) {
    private var mContentView: View? = null
    private var pbBinding: DialogLoadingProgressLayoutBinding =
        DialogLoadingProgressLayoutBinding.inflate(layoutInflater)

    init {
        mContentView = pbBinding.root
        binding.tvTitle.visibility = View.GONE
    }

    override fun createContentView(): View? {
        return mContentView
    }

    fun setProgressBar(value: Int) {
        pbBinding.tvShowMsg.text = context.getString(R.string.download_file_progress, "$value%")
    }
}