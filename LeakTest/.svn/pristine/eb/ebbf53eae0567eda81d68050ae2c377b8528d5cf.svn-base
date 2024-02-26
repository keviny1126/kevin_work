package com.power.baseproject.widget

import android.app.Dialog
import android.content.Context
import android.view.WindowManager
import com.power.baseproject.databinding.LoadingDialogLayoutBinding

class LoadDialog(context: Context) : Dialog(context) {
    private var binding: LoadingDialogLayoutBinding =
        LoadingDialogLayoutBinding.inflate(layoutInflater)

    init {
        getContext().setTheme(android.R.style.Theme_DeviceDefault_Dialog_NoActionBar_MinWidth)
        val view = binding.root
        super.setContentView(view)
        val window = window
        if (window != null) {
            val lp = window.attributes
            val width = (getContext().resources.displayMetrics.widthPixels * 0.38).toInt()
            lp.width = width
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT
            window.attributes = lp
            window.decorView.background.alpha = 0
        }
    }

    fun show(msg: String, isCancel: Boolean = true) {
        binding.tvShowMsg.text = msg
        setCancelable(isCancel)
        show()
    }
}