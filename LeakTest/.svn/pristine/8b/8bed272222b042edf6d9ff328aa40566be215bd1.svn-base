/*
    Launch Android Client, NToast
    Copyright (c) 2014 LAUNCH Tech Company Limited
    http:www.cnlaunch.com
 */
package com.power.baseproject.widget

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.text.TextUtils
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.power.baseproject.R

object NToast {
    private var resource = 0
    private var toast: Toast? = null
    fun initLayoutResourceID(resource: Int) {
        NToast.resource = resource
    }

    fun shortToast(context: Context?, resId: Int) {
        if (context != null) {
            showToast(context, context.getString(resId), Toast.LENGTH_SHORT)
        }
    }

    fun shortToast(context: Context?, text: String?) {
        if (context != null) {
            showToast(context, text, Toast.LENGTH_SHORT)
        }
    }

    fun longToast(context: Context?, resId: Int) {
        if (context != null) {
            showToast(context, context.getString(resId), Toast.LENGTH_LONG)
        }
    }

    fun longToast(context: Context?, text: String?) {
        if (context != null) {
            showToast(context, text, Toast.LENGTH_LONG)
        }
    }

    fun shortToast(context: Context?, resId: Int, gravity: Int) {
        if (context != null) {
            showToast(context, context.getString(resId), Toast.LENGTH_SHORT, gravity)
        }
    }

    fun shortToast(context: Context?, text: String?, gravity: Int) {
        if (context != null) {
            showToast(context, text, Toast.LENGTH_SHORT, gravity)
        }
    }

    fun longToast(context: Context?, resId: Int, gravity: Int) {
        if (context != null) {
            showToast(context, context.getString(resId), Toast.LENGTH_LONG, gravity)
        }
    }

    fun longToast(context: Context?, text: String?, gravity: Int) {
        if (context != null) {
            showToast(context, text, Toast.LENGTH_LONG, gravity)
        }
    }

    /**
     *
     * @param context
     * @param text
     * @param duration
     * @param canclePre 如果之前的Toast对象存在，取消前一个，只显示最后一个
     */
    @JvmOverloads
    fun showToast(context: Context, text: String?, duration: Int, canclePre: Boolean = false) {
        if (!TextUtils.isEmpty(text)) {
            if (canclePre && toast != null) {
                toast?.cancel()
            }
            toast = Toast(context)
            val inflate: LayoutInflater =
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            var v: View? = null
            v = if (0 != resource) {
                inflate.inflate(resource, null)
            } else {
                inflate.inflate(R.layout.layout_toast, null)
            }
            val tv: TextView = v?.findViewById<View>(R.id.toast_message) as TextView
            if (null != tv) {
                tv.setText(text)
            }
            toast?.setView(v)
            toast?.setDuration(duration)
            if (Build.MODEL != null && (Build.MODEL.contains("TB2-X30F")
                        || Build.MODEL.contains("TOPDON")
                        || Build.MODEL.contains("VCDS"))
            ) {
                toast?.setGravity(Gravity.CENTER, 0, -50)
            }
            toast?.show()
        }
    }

    fun showToast(context: Context, text: String?, duration: Int, gravity: Int) {
        if (!TextUtils.isEmpty(text)) {
            val toast = Toast(context)
            val inflate: LayoutInflater =
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            var v: View? = null
            v = if (0 != resource) {
                inflate.inflate(resource, null)
            } else {
                inflate.inflate(R.layout.layout_toast, null)
            }
            val tv: TextView = v?.findViewById<View>(R.id.toast_message) as TextView
            if (null != tv) {
                tv.setText(text)
            }
            if (Build.MODEL != null && Build.MODEL.contains("TB2-X30F")) {
                toast.setGravity(Gravity.CENTER, 0, -50)
            } else {
                toast.setGravity(gravity, 0, 0)
            }
            toast.setView(v)
            toast.setDuration(duration)
            toast.show()
        }
    }

    private var mToast: Toast? = null
    private var mTextView: TextView? = null

    @SuppressLint("StaticFieldLeak")
    fun showInstanceToast(context: Context, text: String?) {
        if (mToast == null) {
            synchronized(NToast::class.java) {
                if (mToast == null) {
                    mToast = Toast(context.applicationContext)
                    val inflate: LayoutInflater =
                        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                    val v: View = inflate.inflate(R.layout.layout_toast, null)
                    val tv: TextView =
                        v.findViewById<View>(R.id.toast_message) as TextView
                    if (null != tv) {
                        mTextView = tv
                    }
                    mToast?.setDuration(Toast.LENGTH_SHORT)
                    if (Build.MODEL != null && (Build.MODEL.contains("TB2-X30F")
                                || Build.MODEL.contains("TOPDON")
                                || Build.MODEL.contains("VCDS"))
                    ) {
                        mToast?.setGravity(Gravity.CENTER, 0, -50)
                    }
                    mToast?.setView(v)
                }
            }
        }
        if (mTextView != null) {
            mTextView?.setText(text)
            mToast?.show()
        }
    }
}