package com.power.baseproject.utils

import android.text.TextUtils
import com.power.baseproject.ktbase.application.BaseApplication
import java.io.File

object PathUtils {

    const val TAG = "PathUtils"
    private const val ROOT_PATH = "LeakTester"
    const val DOWNLOAD_PATH = "firmwareWare"
    const val APP_DOWNLOAD_PATH = "appSoftware"
    const val IMAGE_PATH = "image"
    const val FACTORY_FIRMWARE_PATH = "FactoryFirmware"
    const val LOGCAT_PATH = "Logcat"

    /**
     * 获取SD卡路径
     *
     * @return
     */
    fun getSDCardPath(): String? {
        return BaseApplication.getContext().getExternalFilesDir(null)?.path
    }

    /**
     * 获取路径方法
     *
     * @param pathlist
     * @return
     */
    fun getPath(vararg pathlist: String?): String {
        val pathBuilder = StringBuilder()
        try {
            for (i in pathlist.indices) {
                if (!TextUtils.isEmpty(pathlist[i])) {
                    pathBuilder.append(pathlist[i])
                    if (i < pathlist.size - 1) {
                        pathBuilder.append("/")
                    }
                }
            }
            val file = File(pathBuilder.toString())
            if (!file.exists()) {
                FileUtils.createFileSafely(file)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return "$pathBuilder/"
    }

    /**
     * 项目根目录 InsulationTester
     */
    fun getRootPath(): String {
        return getPath(getSDCardPath(),ROOT_PATH)
    }

    /**
     *下载固件文件目录 InsulationTester/downloadbin/Vxx.xx
     */
    fun getDownloadPath(version:String):String{
        return getPath(getRootPath(),DOWNLOAD_PATH,version)
    }

    /**
     * 工厂环境下固件升级
     */
    fun getFactoryFirmwarePath():String{
        return getPath(getRootPath(),DOWNLOAD_PATH,FACTORY_FIRMWARE_PATH)
    }

    /**
     * 测试环境配置文件
     */
    fun getTestConfigPath():String{
        return getPath(getRootPath(),"LogConfig")
    }

    /**
     * 数据截图路径
     */
    fun getDataImagePath():String{
        return getPath(getRootPath(),IMAGE_PATH)
    }

    /**
     * APP下载路径
     */
    fun getAppSoftPath(version:String):String{
        return getPath(getRootPath(), APP_DOWNLOAD_PATH,version)
    }

    /**
     * 日志保存路径
     */
    fun getLogcatPath():String{
        return getPath(getRootPath(), LOGCAT_PATH)
    }
    /**
     * 压缩文件保存路径
     */
    fun getZipPath():String{
        return getPath(getRootPath(), "ZipFile")
    }
    /**
     * 压缩文件保存路径
     */
    fun getDeviceDataPath(date:String):String{
        return getPath(getRootPath(), "Data",date)
    }

    /**
     * 数据截图路径
     */
    fun getScreenCapPath():String{
        return getPath(getRootPath(),"Photo")
    }
}