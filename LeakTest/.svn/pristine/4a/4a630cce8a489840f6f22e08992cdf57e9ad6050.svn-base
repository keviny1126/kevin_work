package com.newchip.tool.leaktest.utils

import android.content.Context
import android.net.ConnectivityManager

object NetworkUtil {
    //判断网络状态，有网络返回true
    fun isConnected(context: Context?): Boolean {
        return isNetworkConnected(context)
    }

    //判断手机是否有网络连接
    private fun isNetworkConnected(context: Context?): Boolean {
        return context?.let {
            val mConnectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            mConnectivityManager.activeNetworkInfo?.isAvailable
        } ?: false
    }
}