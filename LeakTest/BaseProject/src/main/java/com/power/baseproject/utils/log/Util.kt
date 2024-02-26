package com.power.baseproject.utils.log

import android.content.Context
import android.net.ConnectivityManager
import android.text.TextUtils
import android.util.Log

/**
 * Created by zhaokaiqiang on 15/12/11.
 */
object Util {
    @JvmStatic
    fun isEmpty(line: String): Boolean {
        return TextUtils.isEmpty(line) || line == "\n" || line == "\t" || TextUtils.isEmpty(line.trim { it <= ' ' })
    }

    @JvmStatic
    fun printLine(tag: String?, isTop: Boolean) {
        if (isTop) {
            Log.d(
                tag,
                "╔═══════════════════════════════════════════════════════════════════════════════════════"
            )
        } else {
            Log.d(
                tag,
                "╚═══════════════════════════════════════════════════════════════════════════════════════"
            )
        }
    }

    fun isNetworkConnected(context: Context?): Boolean {
        if (context != null) {
            val mConnectivityManager = context
                .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val mNetworkInfo = mConnectivityManager.activeNetworkInfo
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable
            }
        }
        return false
    }
}