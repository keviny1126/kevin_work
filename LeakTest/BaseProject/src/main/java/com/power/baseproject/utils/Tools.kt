package com.power.baseproject.utils

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageInstaller
import android.content.pm.PackageInstaller.SessionParams
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Build
import android.os.LocaleList
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.power.baseproject.bean.ConfigLeakInfoBean
import com.power.baseproject.ktbase.application.BaseApplication
import com.power.baseproject.utils.log.LogUtil
import com.power.baseproject.widget.InstallResultReceiver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.*
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.math.abs
import kotlin.random.Random

object Tools {
    var mCurrentPackageName: String? = null
    fun requestPermission(
        context: Context,
        mPermissionAllList: ArrayList<String>
    ): ArrayList<String> {
        val mPermissionList = arrayListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (permission in mPermissionAllList) {
                if (ContextCompat.checkSelfPermission(
                        context,
                        permission
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    mPermissionList.add(permission)
                }
            }
        }
        return mPermissionList
    }

    fun getFloatNoMoreThanTwoDigits(number: Float): String {
        val format = DecimalFormat("#.##")
        //舍弃规则，RoundingMode.FLOOR表示直接舍弃。
        format.roundingMode = RoundingMode.FLOOR
        return format.format(number)
    }

    /**
     * 中位值平均滤波法
     */
    fun getAverageValue(valueList: MutableList<Int>): Int {
        var value = 0
        var tempValue = 0
        val discard = valueList.size / 4
        if (valueList.isNotEmpty()) {
            var tempList = valueList.sorted()
            if (tempList.size > discard * 2) {
                tempList = tempList.subList(discard, tempList.size - discard)
            }
            for (data in tempList) {
                tempValue += data
            }
            value = tempValue / tempList.size
        }
        return abs(value)
    }

    /**
     * 递推中位值平均滤波法
     */
    fun getRecursionMedianAverageValue(loopQueue: ConcurrentLinkedQueue<Int>?): Int? {
        if (loopQueue == null) {
            return null
        }
        val length = loopQueue.size
        if (length == 0) return null
        val array = arrayOfNulls<Int>(length)
        loopQueue.toArray(array)
        var value = 0
        var tempValue = 0
        val discard = array.size / 4
        if (array.isNotEmpty()) {
            array.sort()
            var tempList = array.toMutableList()
            if (tempList.size > discard * 2) {
                tempList = tempList.subList(discard, tempList.size - discard)
            }
            for (data in tempList) {
                if (data == null) {
                    continue
                }
                tempValue += data
            }
            value = tempValue / tempList.size
        }
        return abs(value)
    }

    suspend fun saveScreenImage(): Boolean {
        return withContext(Dispatchers.IO) {
            val fileName = "screen" + "_" + DateUtils.getDateToString(
                System.currentTimeMillis(),
                "yyyyMMddHHmmss"
            ) + ".png"
            val capPath = PathUtils.getScreenCapPath()
            FileUtils.checkPathIsExists(capPath)
            val filePath = capPath + fileName
            LogUtil.d("kevin", "获取截图路径：$filePath")
            CmdControl.screenCap(filePath)
        }
    }

    //这种方法状态栏是空白，显示不了状态栏的信息
    suspend fun saveCurrentImage(activity: FragmentActivity?, fileName: String): String? {
        if (activity == null) {
            return null
        }
        val filePath = PathUtils.getDataImagePath()
        FileUtils.checkPathIsExists(filePath)
        LogUtil.d("kevin", "获取截图路径：${filePath + fileName}")
        var foStream: FileOutputStream? = null
        try {
            //获取屏幕
            val bitmap = withContext(Dispatchers.Main) {
                val screenView = activity.window.decorView
                //开启绘图缓存
                screenView.isDrawingCacheEnabled = true
                val bitmap = Bitmap.createBitmap(
                    screenView.width,
                    screenView.height,
                    Bitmap.Config.ARGB_8888
                )
                //返回屏幕View的视图缓存
//            bitmap = screenView.drawingCache
                val canvas = Canvas(bitmap)
                screenView.draw(canvas)
                //清空绘图缓存
                screenView.isDrawingCacheEnabled = false
                //销毁view缓存bitmap
                screenView.destroyDrawingCache()
                bitmap
            }

            //输出到sd卡
            val file = File(filePath + fileName)
            if (!file.exists()) {
                file.createNewFile()
            }
            foStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, foStream)
            foStream.flush()
            LogUtil.d("kevin", "截图保存完成")
        } catch (e: Exception) {
            e.printStackTrace()
            LogUtil.i("kevin", e.toString())
        } finally {
            foStream?.close()
        }
        return filePath + fileName
    }

    fun getAppVersionName(context: Context): String {
        var versionName = ""
        try {
            val pm = context.packageManager
            val pi = pm.getPackageInfo(context.packageName, 0)
            versionName = pi.versionName
            if (versionName.isEmpty()) {
                return ""
            }
        } catch (e: Exception) {
            LogUtil.e("VersionInfo", "Exception", e)
        }
        return versionName
    }

    fun getAppVersionCode(context: Context): Long {
        return try {
            val pm = context.packageManager
            val pi = pm.getPackageInfo(context.packageName, 0)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                pi.longVersionCode
            } else {
                pi.versionCode.toLong()
            }
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()  // 或者进行适当的处理
            0  // 返回默认的版本号
        }
    }

    fun getCurrentPackageName(context: Context): String? {
        if (!mCurrentPackageName.isNullOrEmpty()) {
            return mCurrentPackageName
        }
        val info: PackageInfo
        try {
            info = context.packageManager.getPackageInfo(context.packageName, 0)
            mCurrentPackageName = info.packageName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return mCurrentPackageName
    }

    /**
     * 适配android9的安装方法。
     * 全部替换安装
     * @return
     */
    fun installApk(apkFilePath: String, mContext: Context = BaseApplication.getContext()): Boolean {
        val apkFile = File(apkFilePath)
        if (!apkFile.exists()) {
            return false
        }
        val packageInfo: PackageInfo? =
            mContext.packageManager.getPackageArchiveInfo(
                apkFilePath,
                PackageManager.GET_ACTIVITIES or PackageManager.GET_SERVICES
            )
        if (packageInfo != null) {
            val packageName = packageInfo.packageName
            val versionCode = packageInfo.versionCode
            val versionName = packageInfo.versionName
            val curCode = getAppVersionCode(mContext)
            LogUtil.d(
                "kevin",
                "待安装 app packageName=$packageName, versionCode=$versionCode, versionName=$versionName === 当前app version code:$curCode"
            )
            if (curCode > versionCode) {
                return false
            }
        }
        val packageInstaller: PackageInstaller =
            BaseApplication.getContext().packageManager.packageInstaller
        val sessionParams = SessionParams(SessionParams.MODE_FULL_INSTALL)
        sessionParams.setSize(apkFile.length())
        var mSessionId: Int = -1
        try {
            mSessionId = packageInstaller.createSession(sessionParams)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        //Log.d(TAG, "sessionId---->" + mSessionId);
        if (mSessionId !== -1) {
            val copySuccess = onTransfesApkFile(apkFilePath, mSessionId)
            //Log.d(TAG, "copySuccess---->" + copySuccess);
            if (copySuccess) {
                execInstallAPP(mSessionId)
            }
        }
        return true
    }

    /**
     * 通过文件流传输apk
     *
     * @param apkFilePath
     * @return
     */
    private fun onTransfesApkFile(apkFilePath: String, mSessionId: Int): Boolean {
        var `in`: InputStream? = null
        var out: OutputStream? = null
        var session: PackageInstaller.Session? = null
        var success = false
        try {
            val apkFile = File(apkFilePath)
            session =
                BaseApplication.getContext().packageManager.packageInstaller.openSession(mSessionId)
            out = session.openWrite("base.apk", 0, apkFile.length())
            `in` = FileInputStream(apkFile)
            var total = 0
            var c: Int
            val buffer = ByteArray(1024 * 1024)
            while (`in`.read(buffer).also { c = it } != -1) {
                total += c
                out.write(buffer, 0, c)
            }
            session.fsync(out)
            //Log.d(TAG, "streamed " + total + " bytes");
            success = true
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            session?.close()
            try {
                out?.close()
                `in`?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return success
    }

    /**
     * 执行安装并通知安装结果
     *
     */
    private fun execInstallAPP(mSessionId: Int) {
        var session: PackageInstaller.Session? = null
        try {
            session =
                BaseApplication.getContext().packageManager.packageInstaller.openSession(mSessionId)
            val intent = Intent(BaseApplication.getContext(), InstallResultReceiver::class.java)
            intent.action = "com.newchip.tool.leaktest.installresult"
            val pendingIntent = PendingIntent.getBroadcast(
                BaseApplication.getContext(),
                1,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
            session.commit(pendingIntent.intentSender)
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            session?.close()
        }
    }

    //根据包名 判断某APP是否安装
    fun checkApkExist(context: Context, packageName: String): Boolean {
        //  检查app是否有安装
        if (packageName.isEmpty())
            return false
        return try {
            val info = context.packageManager
                .getApplicationInfo(
                    packageName,
                    PackageManager.GET_UNINSTALLED_PACKAGES
                )
            LogUtil.d("kevin", info.toString())
            true
        } catch (e: PackageManager.NameNotFoundException) {
            LogUtil.d("kevin", e.toString())

            false
        }

    }

    //通过包名启动第三方应用
    @SuppressLint("QueryPermissionsNeeded")
    fun startAPK(context: Context, packageName: String, activityName: String) {
        LogUtil.d("kevin", "启动中。。。$packageName")
        var mainAct: String? = null
        val pkgMag = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)
        intent.flags = Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED or Intent.FLAG_ACTIVITY_NEW_TASK
        //如果已经启动apk，则直接将apk从后台调到前台运行（类似home键之后再点击apk图标启动），如果未启动apk，则重新启动
        @SuppressLint("WrongConstant")
        val list = pkgMag.queryIntentActivities(
            intent,
            PackageManager.GET_ACTIVITIES
        )
        for (i in list.indices) {
            val info = list[i]
            if (info.activityInfo.packageName == packageName) {
                mainAct = info.activityInfo.name
                break
            }
        }
        if (mainAct.isNullOrEmpty()) {
            return
        }
        LogUtil.d("kevin", "mainAct。。。:$mainAct")
        // 启动指定的activity页面
        //intent.component = ComponentName(packageName,activityName)
        //启动到app的主页或启动到原来留下的位置
        intent.component = ComponentName(packageName, mainAct)
        //启动app
        context.startActivity(intent)
        LogUtil.d("kevin", "启动成功。。。")
    }

    fun installOtherApk(mContext: Context, fileName: String) {
        LogUtil.d("kevin", "------installOtherApk------fileName:$fileName")
        val apkFile = File(fileName)
        val uri = Uri.fromFile(apkFile)
        val intent = Intent()
        intent.setClassName(
            "com.android.packageinstaller",
            "com.android.packageinstaller.PackageInstallerActivity"
        )
        intent.data = uri
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        mContext.startActivity(intent)
    }

    fun iServiceRunning(mContext: Context, className: String): Boolean {
        var isRunning = false
        val am = mContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val serviceList = am.getRunningServices(30)
        if (serviceList.isNullOrEmpty()) {
            return isRunning
        }
        for (service in serviceList) {
            LogUtil.i(
                "kevin",
                "iServiceRunning ---->${service.service.className} ----className:$className"
            )
            if (service.service.className.contains(className)) {
                isRunning = true
                break
            }
        }
        return isRunning
    }

    fun isChineseLanguage(): Boolean {
        val locale: Locale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            LocaleList.getDefault()[0]
        } else {
            Locale.getDefault()
        }
        return when (locale.language) {
            "zh" -> true
            else -> false
        }
    }

    fun getProperties(context: Context): Properties? {
        try {
            val props = Properties()
            val input = context.assets.open("config.properties")
            props.load(input)
            return props
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    fun getPropertySample(props: Properties?, key: String?): String? {
        return if (props == null) {
            ""
        } else props.getProperty(key)
    }

    fun getAppKey(): String {
        return when (EasyPreferences.instance[ConstantsUtils.PRODUCT_NAME]) {
            ProductConstants.ET30 -> {
                ConstantsUtils.SS_SERVICE_APP_KEY
            }

            ProductConstants.ET500 -> {
                ConstantsUtils.ET500_SERVICE_APP_KEY
            }

            ProductConstants.ELT500 -> {
                ConstantsUtils.ZY_SERVICE_APP_KEY
            }

            ProductConstants.EVT501 -> {
                ConstantsUtils.EVT501_SERVICE_APP_KEY
            }

            else -> {
                ConstantsUtils.SS_SERVICE_APP_KEY
            }
        }
    }

    fun getFirmwareKey(): String {
        return when (EasyPreferences.instance[ConstantsUtils.PRODUCT_NAME]) {
            ProductConstants.ET30 -> {
                ConstantsUtils.SS_SERVICE_FIRMWARE_KEY
            }

            ProductConstants.ET500 -> {
                ConstantsUtils.ET500_SERVICE_FIRMWARE_KEY
            }

            ProductConstants.ELT500 -> {
                ConstantsUtils.ZY_SERVICE_FIRMWARE_KEY
            }

            ProductConstants.EVT501 -> {
                ConstantsUtils.EVT501_SERVICE_FIRMWARE_KEY
            }

            else -> {
                ConstantsUtils.SS_SERVICE_FIRMWARE_KEY
            }
        }
    }

    /*    fun paToPsi(pa: Int): Float {
            val psi = pa * 0.000145037737797
            val decimalFormat = DecimalFormat("#.######")
            decimalFormat.roundingMode = RoundingMode.HALF_UP
            return decimalFormat.format(psi).toFloat()
        }

        fun psiToPa(psi: Float): Int {
            val pa = psi / 0.000145037737797
            val decimalFormat = DecimalFormat("#.###")
            decimalFormat.roundingMode = RoundingMode.HALF_UP
            return decimalFormat.format(pa).toFloat().toInt()
        }

        fun kpaToPsi(kpa: Float): Float {
            val psi = kpa * 0.145037737797
            val decimalFormat = DecimalFormat("#.###")
            decimalFormat.roundingMode = RoundingMode.HALF_UP
            return decimalFormat.format(psi).toFloat()
        }

        fun psiToKpa(psi: Float): Float {
            val kpa = psi / 0.145037737797
            val decimalFormat = DecimalFormat("#.###")
            decimalFormat.roundingMode = RoundingMode.HALF_UP
            return decimalFormat.format(kpa).toFloat()
        }*/

    fun convertToKpa(value: Float, unit: String): Float {
        return when (unit) {
            ConfigLeakInfoBean.PSI -> value / 0.145038f
            ConfigLeakInfoBean.PA -> value / 1000f
            else -> value
        }
    }

    fun convertToPa(value: Float, unit: String): Float {
        return when (unit) {
            ConfigLeakInfoBean.KPA -> value * 1000f
            ConfigLeakInfoBean.PSI -> value * 6894.76f
            else -> value
        }
    }

    fun convertToPsi(value: Float, unit: String): Float {
        return when (unit) {
            ConfigLeakInfoBean.KPA -> value * 0.145038f
            ConfigLeakInfoBean.PA -> value / 6894.76f
            else -> value
        }
    }

    fun String.convertUnit(value: Float, unit: String): Float {
        return when (this) {
            ConstantsUtils.PA -> convertToPa(value, unit)
            ConstantsUtils.KPA -> convertToKpa(value, unit)
            ConstantsUtils.PSI -> convertToPsi(value, unit)
            else -> value
        }
    }

    fun Float.toDecimalNotationString(maxNum: Int, minNum: Int = 1): String {
        val symbols = DecimalFormatSymbols()
        symbols.decimalSeparator = '.'
        val resistanceDf = DecimalFormat().apply {
            roundingMode = RoundingMode.HALF_UP
            isGroupingUsed = false
            maximumFractionDigits = maxNum
            decimalFormatSymbols = symbols
            if (minNum != 0) minimumFractionDigits = minNum
        }
        return resistanceDf.format(this)
    }

    fun Int.toUnitString(): String {
        return when (this) {
            0 -> ConstantsUtils.KPA
            1 -> ConstantsUtils.PA
            2 -> ConstantsUtils.PSI
            else -> ConstantsUtils.KPA
        }
    }

    fun getRandomString(index: Int): String {
        val charPool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9') // 定义字符池
        return (1..index)
            .map { Random.nextInt(0, charPool.size) }
            .map(charPool::get)
            .joinToString("")
    }
}


