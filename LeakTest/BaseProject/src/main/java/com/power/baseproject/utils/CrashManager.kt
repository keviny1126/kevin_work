package com.power.baseproject.utils

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import com.power.baseproject.utils.log.LogUtil
import java.io.File
import java.io.FileOutputStream
import java.io.PrintWriter
import java.io.StringWriter
import java.util.*

class CrashManager private constructor() : Thread.UncaughtExceptionHandler {
    private var mDefaultHandler: Thread.UncaughtExceptionHandler? = null
    private lateinit var mContext: Context
    private lateinit var PATH_LOGCAT_CRASH: String

    fun init(context: Context) {
        Thread.currentThread().uncaughtExceptionHandler = this
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        mContext = context
        val path = PathUtils.getLogcatPath()
        PATH_LOGCAT_CRASH = path + CRASH_LOG
        FileUtils.checkPathIsExists(PATH_LOGCAT_CRASH)
    }

    override fun uncaughtException(p0: Thread, p1: Throwable) {
        val crashFileName = saveCatchInfo2File(p1)
        LogUtil.e(TAG, "crash fileName --> $crashFileName")
        mDefaultHandler?.uncaughtException(p0, p1)
    }

    /**
     * 信息保存
     */
    private fun saveCatchInfo2File(ex: Throwable): String? {
        var fileName: String? = ""
        val sb = StringBuffer()
        for ((key, value) in obtainSimpleInfo(mContext).entries) {
            sb.append(key).append(" = ").append(value).append("\n")
        }
        sb.append(obtainExceptionInfo(ex))
        try {
            val dir = File(PATH_LOGCAT_CRASH)
            //这里文件名字可以自己根据项目修改
            fileName = (dir.toString()
                    + File.separator
                    + "crash-${
                DateUtils.getDateToString(
                    System.currentTimeMillis(),
                    "yyyyMMdd_HHmmss"
                )
            }.log")
            val fos = FileOutputStream(fileName)
            //把字符串转成字节数组
            fos.write(sb.toString().toByteArray())
            fos.flush()
            fos.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return fileName
    }

    /**
     * 获取一些简单的信息,软件版本，手机版本，型号等信息存放在HashMap中
     * 这里缺什么可以自己补全
     */
    private fun obtainSimpleInfo(context: Context): HashMap<String, String> {
        val map = HashMap<String, String>()
        val mPackageManager = context.packageManager
        var mPackageInfo: PackageInfo? = null
        try {
            mPackageInfo = mPackageManager.getPackageInfo(
                context.packageName, PackageManager.GET_ACTIVITIES
            )
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        map["versionName"] = mPackageInfo?.versionName ?: ""
        map["versionCode"] = "" + mPackageInfo?.versionCode
        map["MODEL"] = "" + Build.MODEL
        map["SDK_INT"] = "" + Build.VERSION.SDK_INT
        map["PRODUCT"] = "" + Build.PRODUCT
        map["MOBILE_INFO"] = getMobileInfo()
        return map
    }

    /**
     * 获取系统未捕捉的错误信息
     */
    private fun obtainExceptionInfo(throwable: Throwable): String {
        val stringWriter = StringWriter()
        val printWriter = PrintWriter(stringWriter)
        throwable.printStackTrace(printWriter)
        printWriter.close()
        return stringWriter.toString()
    }

    /**
     * 获取设备信息
     */
    private fun getMobileInfo(): String {
        val sb = StringBuffer()
        try {
            val fields = Build::class.java.declaredFields
            for (field in fields) {
                field.isAccessible = true
                val name = field.name
                val value = field[null].toString()
                sb.append("$name=$value")
                sb.append("\n")
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return sb.toString()
    }

    companion object {
        const val TAG = "CrashManager"
        const val CRASH_LOG = "CrashLog"
        val instance: CrashManager by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            CrashManager()
        }
    }
}