package com.power.baseproject.common

import android.app.Activity
import android.os.Process
import java.util.*
import kotlin.system.exitProcess

class ActivityPackageManager {
    private var activityStack: Stack<Activity> = Stack()

    fun addActivity(activity: Activity?) {
        activityStack.add(activity)
    }

    fun removeActivity(activity: Activity?) {
        activityStack.remove(activity)
    }

    fun currentActivity(): Activity? {
        return if (activityStack.size > 0) {
            activityStack.lastElement()
        } else null
    }

    /**
     * finish current activity from Stack
     */
    fun finishActivity() {
        if (activityStack.size > 0) {
            val activity: Activity = activityStack.lastElement()
            finishActivity(activity)
        }
    }

    /**
     * finish the Activity
     */
    fun finishActivity(activity: Activity?) {
        if (activity != null) {
            activityStack.remove(activity)
            activity.finish()
        }
    }

    fun finishAllActivity() {
        var i = 0
        val size: Int = activityStack.size
        while (i < size) {
            if (null != activityStack[i]) {
                activityStack[i].finish()
            }
            i++
        }
        activityStack.clear()
    }

    fun exit() {
        try {
            finishAllActivity()
            Process.killProcess(Process.myPid())
            exitProcess(0)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        val instance: ActivityPackageManager by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            ActivityPackageManager()
        }
    }

}