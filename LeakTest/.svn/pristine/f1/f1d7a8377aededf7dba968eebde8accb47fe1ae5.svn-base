package com.power.baseproject.utils

import android.content.Context
import android.view.View
import com.power.baseproject.utils.ViewClickDelay.SPACE_TIME
import com.power.baseproject.utils.ViewClickDelay.contextHash
import com.power.baseproject.utils.ViewClickDelay.hash
import com.power.baseproject.utils.ViewClickDelay.lastClickTime
import com.power.baseproject.utils.ViewClickDelay.lastOrderTime

object ViewClickDelay {
    const val MIN_DELAY_TIME = 500 // 两次点击间隔不能少于500ms
    private var lastNoRecordClickTime: Long = 0
    var hash: Int = 0
    var contextHash: Int = 0
    var lastClickTime: Long = 0
    var lastOrderTime: Long = 0
    var SPACE_TIME: Long = 300

    val isFastClick: Boolean
        get() {
            var flag = true
            val currentClickTime = System.currentTimeMillis()
            if (currentClickTime - lastClickTime >= MIN_DELAY_TIME) {
                flag = false
            }
            lastClickTime = currentClickTime
            lastNoRecordClickTime = currentClickTime
            return flag
        }
}

infix fun View.clicks(clickAction: () -> Unit) {
    this.setOnClickListener {
        if (this.hashCode() != hash) {
            hash = this.hashCode()
            lastClickTime = System.currentTimeMillis()
            SoundUtils.instance.playClickSound()
            clickAction()
        } else {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastClickTime > SPACE_TIME) {
                lastClickTime = System.currentTimeMillis()
                SoundUtils.instance.playClickSound()
                clickAction()
            }
        }
    }
}

infix fun Context.isFastTimer(action: () -> Unit) {
    if (this.hashCode() != contextHash) {
        contextHash = this.hashCode()
        lastOrderTime = System.currentTimeMillis()
        action()
    } else {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastOrderTime > SPACE_TIME) {
            lastOrderTime = System.currentTimeMillis()
            action()
        }
    }
}