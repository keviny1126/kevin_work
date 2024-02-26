package com.power.insulationtester.utils.bean

import java.io.Serializable
import java.math.BigDecimal

class DownloadbinProgressData:Serializable {
    var progress: Int = 0

    fun setProgress(writePos: Long, totalLen: Long) {
        val num = writePos.toFloat() / totalLen.toFloat()
        val b = BigDecimal((num * 100).toInt())
        progress = b.setScale(0, BigDecimal.ROUND_HALF_UP).toInt()
    }
}
