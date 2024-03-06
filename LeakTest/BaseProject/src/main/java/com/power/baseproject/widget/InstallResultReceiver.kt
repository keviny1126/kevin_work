package com.power.baseproject.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.power.baseproject.utils.CmdControl
import com.power.baseproject.utils.log.LogUtil.d
import com.power.baseproject.utils.log.LogUtil.i

class InstallResultReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        d(TAG, "InstallResultReceiver action:$action")
        when (action) {
            "wits.action.reboot" -> {
                i(TAG, "wits.action.reboot")
            }

            "wits.action.shutdown" -> {
                i(TAG, "wits.action.shutdown")
            }

            "android.intent.action.BOOT_COMPLETED" -> {
                i(TAG, "android.intent.action.BOOT_COMPLETED")
            }

            "RestartSerivcesForSystemEventReceiver" -> {
                i(TAG, "RestartSerivcesForSystemEventReceiver")
            }

            "android.intent.action.MEDIA_MOUNTED" -> {
                i(TAG, "android.intent.action.MEDIA_MOUNTE")
            }

            "android.intent.action.MEDIA_UNMOUNTEDD" -> {
                i(TAG, "android.intent.action.MEDIA_UNMOUNTEDD")
            }

            "android.intent.action.MEDIA_EJECT" -> {
                i(TAG, "android.intent.action.MEDIA_EJECT")
            }

            "android.intent.action.SERVICE_STATE" -> {
                i(TAG, "android.intent.action.SERVICE_STATE")
            }
        }
        if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            d(TAG, "安装反馈 ACTION_BOOT_COMPLETED")
            CmdControl.restartAppCommand(context)
        }

        if (action.equals(Intent.ACTION_PACKAGE_REPLACED)) {
            d(TAG, "安装反馈 ACTION_PACKAGE_REPLACED")
            CmdControl.restartAppCommand(context)
        }
    }

    companion object {
        private const val TAG = "InstallResultReceiver"
    }
}