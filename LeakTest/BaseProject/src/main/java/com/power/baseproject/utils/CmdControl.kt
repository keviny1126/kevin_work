package com.power.baseproject.utils

import android.content.Context
import android.content.pm.PackageManager
import com.power.baseproject.utils.log.LogUtil
import com.power.baseproject.utils.log.LogUtil.d
import com.power.baseproject.utils.log.LogUtil.e
import java.io.*

object CmdControl {
    private val TAG = CmdControl::class.java.simpleName
    private const val COMMAND_SU = "su"
    private const val COMMAND_SH = "sh"
    private const val COMMAND_LINE_END = "\n"
    private const val COMMAND_EXIT = "exit\n"

    fun setHost() {
        var process: Process? = null
        val os: DataOutputStream
        try {
            process = Runtime.getRuntime().exec(COMMAND_SU)
            os = DataOutputStream(process.outputStream)
            val operationCommand =
                "echo host > /sys/devices/platform/ff2c0000.syscon/ff2c0000.syscon:usb2-phy@100/otg_mode\n"
            os.writeBytes(operationCommand)
            os.writeBytes(COMMAND_LINE_END)
            os.writeBytes(COMMAND_EXIT)
            os.flush()
            os.close()
            process.waitFor()
        } catch (e: Exception) {
            e.printStackTrace()
            if (process != null) {
                try {
                    process.destroy()
                } catch (e1: Exception) {
                    e1.printStackTrace()
                }
            }
        }
    }

    fun readOtgState(): String? {
        var result: String? = null
        var process: Process? = null
        val os: DataOutputStream
        try {
            process = Runtime.getRuntime().exec(COMMAND_SU)
            os = DataOutputStream(process!!.outputStream)
            val operationCommand =
                "cat /sys/devices/platform/ff2c0000.syscon/ff2c0000.syscon:usb2-phy@100/otg_mode\n"
            os.writeBytes(operationCommand)
            os.writeBytes(COMMAND_LINE_END)
            os.writeBytes(COMMAND_EXIT)
            os.flush()
            os.close()
            try {
                val successResult = BufferedReader(
                    InputStreamReader(
                        process.inputStream, "UTF-8"
                    )
                )
                var line: String? = null
                while (successResult.readLine().also { line = it } != null) {
                    if (line.isNullOrEmpty()) {
                        continue
                    }
                    result = line
                }
                successResult.close()
                val errorResult = BufferedReader(
                    InputStreamReader(
                        process.errorStream, "UTF-8"
                    )
                )
                while (errorResult.readLine().also { line = it } != null) {
                    e(TAG, "指令错误信息 Result Error: $line")
                }
                errorResult.close()
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
            process.waitFor()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            if (process != null) {
                try {
                    process.destroy()
                } catch (e1: java.lang.Exception) {
                    e1.printStackTrace()
                }
            }
        }
        return result
    }

    /**
     * 静默安装
     *
     * @param filePath
     * @return result
     */
    fun installAPKSilently(context: Context, path: String): Int {
        var filePath = path
        var result = -1
        var process: Process? = null
        var successResult: BufferedReader? = null
        var errorResult: BufferedReader? = null
        try {
            val pm = context.packageManager
            val info = pm.getPackageArchiveInfo(
                filePath,
                PackageManager.GET_ACTIVITIES or PackageManager.GET_SERVICES
            )
                ?: return result
            val packageName: String? = Tools.getCurrentPackageName(context)
            filePath = filePath.replace(" ", "\\ ")
            d(TAG, "installAPKSilently filePath: $filePath")
            process = Runtime.getRuntime().exec(COMMAND_SU)
            val dataOutputStream = DataOutputStream(process.outputStream)
            dataOutputStream.writeBytes("chmod 777 $filePath\n")
            dataOutputStream.writeBytes(
                "LD_LIBRARY_PATH=/vendor/lib:/system/lib pm install -r " +
                        filePath + "\n"
            )
            if (packageName.equals(info.packageName, ignoreCase = true)) {
                var activity: String? = "am start -n "
                activity += info.packageName
                activity += '/'
                activity += info.activities[0].name
                dataOutputStream.writeBytes(activity)
            }
            dataOutputStream.flush()
            dataOutputStream.close()
            successResult = BufferedReader(InputStreamReader(process.inputStream, "UTF-8"))
            errorResult = BufferedReader(InputStreamReader(process.errorStream, "UTF-8"))
            var line: String? = null
            d(TAG, "installAPKSilently: end!")
            while ((successResult.readLine().also { line = it }) != null) {
                if (line.isNullOrEmpty()) {
                    continue
                }
                d(TAG, "Result: $line")
                if (line!!.contains("Success")) {
                    result = 0
                }
            }
            while ((errorResult.readLine().also { line = it }) != null) {
                if (line.isNullOrEmpty()) {
                    continue
                }
                if (line!!.contains("Failure") &&
                    line!!.contains("INSTALL_PARSE_FAILED_INCONSISTENT_CERTIFICATES")
                ) {
                    result = -2 // 表示已存在其他 签名的安装，需要手动卸载再安装。
                }
                d(TAG, "Result Error: $line")
            }
            d(TAG, "installAPKSilently: end1!")
            val value = process.waitFor()
            d(TAG, "installAPKSilently: end2! --$value")
        } catch (e: IOException) {
            e.printStackTrace()
            d(TAG, "installAPKSilently: IOException!")
        } catch (e: InterruptedException) {
            e.printStackTrace()
            d(TAG, "installAPKSilently: IOException1!")
        } finally {
            if (null != successResult) {
                try {
                    successResult.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            if (null != errorResult) {
                try {
                    errorResult.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        d(TAG, "installAPKSilently: end3!")
        return result
    }

    private fun execLinuxCommand(cmd: ArrayList<String>): Boolean {
        var process: Process? = null
        val os: DataOutputStream
        var result = true
        try {
            process = Runtime.getRuntime().exec(COMMAND_SU)
            os = DataOutputStream(process.outputStream)
            for (s in cmd) {
                if (s.isEmpty()) {
                    continue
                }
                e(TAG, "执行命令:$s")
                os.writeBytes(s)
                os.writeBytes(COMMAND_LINE_END)
                os.flush()
            }
            os.writeBytes(COMMAND_EXIT)
            os.flush()
            os.close()
            process.waitFor()
        } catch (e: Exception) {
            e.printStackTrace()
            result = false
        } finally {
            if (process != null) {
                try {
                    process.destroy()
                } catch (e1: Exception) {
                    e1.printStackTrace()
                }
            }
        }
        return result
    }

    fun restartAppCommand(context: Context) {
        val packageName = context.packageName?:"com.newchip.tool.leaktest"
        LogUtil.i("kevin","当前程序包名:$packageName")
        val cmd = arrayListOf(
            "am start -n $packageName/com.newchip.tool.leaktest.MainActivity"
        )
        execLinuxCommand(cmd)
    }

    fun updateBootAnimation(path: String): Boolean {
        val cmd = arrayListOf(
            "blockdev --setrw /dev/block/by-name/system",
            "mount -o remount,rw -t ext4 /dev/root",
            "cp $path /system/media/bootanimation.zip",
            "cd /system/media/",
            "chmod 777 bootanimation.zip"
        )
        return execLinuxCommand(cmd)
    }

    fun deleteBootAnimation(): Boolean {
        val cmd = arrayListOf(
            "blockdev --setrw /dev/block/by-name/system",
            "mount -o remount,rw -t ext4 /dev/root",
            "cd /system/media/",
            "rm bootanimation.zip"
        )
        return execLinuxCommand(cmd)
    }

    fun showOrHideStatusBar(hide: Boolean): Boolean {
        val statusCmd =
            if (hide) "am broadcast -a ntimes.intent.action.hide" else "am broadcast -a ntimes.intent.action.show"
        val cmd = arrayListOf(
            statusCmd
        )
        return execLinuxCommand(cmd)
    }

    fun setDeviceMode(): Boolean {
        val statusCmd =
            "echo otg > /sys/devices/platform/ff2c0000.syscon/ff2c0000.syscon:usb2-phy@100/otg_mode"
        val cmd = arrayListOf(
            statusCmd
        )
        return execLinuxCommand(cmd)
    }

    fun startLaunch(): Boolean {
        val statusCmd = "am start -n com.android.launcher3/com.android.launcher3.Launcher"
        val cmd = arrayListOf(
            statusCmd
        )
        return execLinuxCommand(cmd)
    }

    fun screenCap(filePath: String): Boolean {
        val statusCmd = "screencap -p $filePath"
        val cmd = arrayListOf(
            statusCmd
        )
        return execLinuxCommand(cmd)
    }

//    fun setTimeZone(): Boolean {
//        val cmd = arrayListOf(
//            "setprop persist.sys.timezone GMT+8",
//            "sync"
//        )
//        return execLinuxCommand(cmd)
//    }
}