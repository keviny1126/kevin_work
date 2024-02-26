package com.power.baseproject.utils.log

import android.util.Log

/**
 * autour: tanwu
 * date: 2017/8/15 12:11
 * description:
 * version:
 * modify by:
 * update: 2017/8/15
 */
object BaseLog {
    @JvmStatic
    fun printDefault(type: Int, tag: String, msg: String) {
        var index = 0
        val maxLength = 4000
        val countOfSub = msg.length / maxLength
        if (countOfSub > 0) {
            for (i in 0 until countOfSub) {
                val sub = msg.substring(index, index + maxLength)
                printSub(type, tag, sub)
                index += maxLength
            }
            printSub(type, tag, msg.substring(index, msg.length))
        } else {
            printSub(type, tag, msg)
        }
    }

    private fun printSub(type: Int, tag: String, sub: String) {
        when (type) {
            LogUtil.V -> Log.v(tag, sub)
            LogUtil.D -> Log.d(tag, sub)
            LogUtil.I -> Log.i(tag, sub)
            LogUtil.W -> Log.w(tag, sub)
            LogUtil.E -> Log.e(tag, sub)
            LogUtil.A -> Log.wtf(tag, sub)
        }
    }
}