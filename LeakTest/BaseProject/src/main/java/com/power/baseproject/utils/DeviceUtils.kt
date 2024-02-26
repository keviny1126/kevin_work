package com.power.baseproject.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.content.*
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Rect
import android.net.Uri
import android.os.*
import android.telephony.TelephonyManager
import android.text.TextPaint
import android.text.TextUtils
import android.util.DisplayMetrics
import android.view.Display
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import com.power.baseproject.R
import com.power.baseproject.ktbase.application.BaseApplication
import com.power.baseproject.utils.log.LogUtil
import java.io.File
import java.util.*
import kotlin.math.ceil

/**
 * @author LiuChao
 * @describe 设备相关的工具类方法
 * @date 2017/1/19
 * @contact email:450127106@qq.com
 */
object DeviceUtils {
    private const val TAG = "DeviceUtils"

    /**
     * 发送邮件
     *
     * @param context
     * @param subject 主题
     * @param content 内容
     * @param emails  邮件地址
     */
    fun sendEmail(
        context: Context, subject: String?,
        content: String?, vararg emails: String?
    ) {
        try {
            val intent = Intent(Intent.ACTION_SEND)
            // 模拟器
            // intent.setGift_code("text/plain");
            intent.setType("message/rfc822") // 真机
            intent.putExtra(Intent.EXTRA_EMAIL, emails)
            intent.putExtra(Intent.EXTRA_SUBJECT, subject)
            intent.putExtra(Intent.EXTRA_TEXT, content)
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
        }
    }

    /**
     * 调用系统安装了的应用分享
     *
     * @param context
     * @param title
     * @param url
     */
    fun showSystemShareOption(
        context: Activity,
        title: String, url: String
    ) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.setType("text/plain")
        intent.putExtra(Intent.EXTRA_SUBJECT, "分享：$title")
        intent.putExtra(Intent.EXTRA_TEXT, "$title $url")
        context.startActivity(Intent.createChooser(intent, "选择分享"))
    }

    /**
     * 判断是否存在sd卡
     *
     * @return
     */
    val isExitsSdcard: Boolean
        get() = Environment.getExternalStorageState() ==
                Environment.MEDIA_MOUNTED

    fun getStatuBarHeight(context: Context): Int {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            return 0
        }
        //        Class<?> c = null;
//        Object obj = null;
//        Field field = null;
//        int x = 0, sbar = 38;// 默认为38，貌似大部分是这样的
        val x = 0
        var sbar = 0 // 默认为38，貌似大部分是这样的
        try {
//            c = Class.forName("com.android.internal.R$dimen");
//            obj = c.newInstance();
//            field = c.getField("status_bar_height");
//            x = Integer.parseInt(field.get(obj).toString());
//            sbar = context.getResources()
//                    .getDimensionPixelSize(x);
            sbar = getStatusBarHeight(context)
        } catch (e1: Exception) {
            e1.printStackTrace()
        }
//        LogUtil.i("ykw", "-----获取状态栏高度----：$sbar")
        val h = getHeight(ActivityHandler.getInstance().currentActivity())
        if (sbar == 0) {
            sbar = context.resources.getDimensionPixelSize(R.dimen.dp_38)
        }
        return h.coerceAtLeast(sbar)
    }

    private fun getStatusBarHeight(context: Context): Int {
        // 获得状态栏高度
        val resourceId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
        return context.resources.getDimensionPixelSize(resourceId)
    }

    /**
     * 获得屏幕宽度
     */
    fun getScreenWidth(context: Context): Int {
        val wm: WindowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val outMetrics = DisplayMetrics()
        wm.defaultDisplay.getMetrics(outMetrics)
        return outMetrics.widthPixels
    }

    /**
     * 获得屏幕密度
     */
    fun getDisplayMetrics(context: Context): DisplayMetrics {
        val wm: WindowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val outMetrics = DisplayMetrics()
        wm.defaultDisplay.getMetrics(outMetrics)
        return outMetrics
    }

    /**
     * 获得屏幕高度
     */
    fun getScreenHeight(context: Context): Int {
        val wm: WindowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val outMetrics = DisplayMetrics()
        wm.defaultDisplay.getMetrics(outMetrics)
        return outMetrics.heightPixels
    }

    fun hasStatusBar(activity: Activity): Boolean {
        val attrs: WindowManager.LayoutParams = activity.getWindow().getAttributes()
        return attrs.flags and WindowManager.LayoutParams.FLAG_FULLSCREEN != WindowManager.LayoutParams.FLAG_FULLSCREEN
    }

    /**
     * 卸载软件
     *
     * @param context
     * @param packageName
     */
    fun uninstallApk(context: Context, packageName: String) {
        if (isPackageExist(context, packageName)) {
            val packageURI = Uri.parse("package:$packageName")
            val uninstallIntent = Intent(
                Intent.ACTION_DELETE,
                packageURI
            )
            context.startActivity(uninstallIntent)
        }
    }

    /**
     * 当前的包是否存在
     *
     * @param context
     * @param pckName
     * @return
     */
    fun isPackageExist(context: Context, pckName: String?): Boolean {
        try {
            val pckInfo: PackageInfo? = context.packageManager
                .getPackageInfo(pckName!!, 0)
            if (pckInfo != null) {
                return true
            }
        } catch (e: PackageManager.NameNotFoundException) {
            e.message?.let { LogUtil.e("TDvice", it) }
        }
        return false
    }

    fun copyTextToBoard(context: Context, string: String?) {
        if (TextUtils.isEmpty(string)) {
            return
        }
        val clip = context
            .getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clip.text = string
    }

    /**
     * 开启 app
     *
     * @param context
     * @param packageName
     */
    fun openApp(context: Context, packageName: String?) {
        var mainIntent: Intent? = context.packageManager
            .getLaunchIntentForPackage(packageName!!)
        if (mainIntent == null) {
            mainIntent = Intent(packageName)
        } else {
        }
        context.startActivity(mainIntent)
    }

    /**
     * 判断应用是否已经启动
     *
     * @param context     一个context
     * @param packageName 要判断应用的包名
     * @return boolean
     */
    fun isAppAlive(context: Context, packageName: String): Boolean {
        val activityManager: ActivityManager =
            context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val processInfos: List<ActivityManager.RunningAppProcessInfo> =
            activityManager.getRunningAppProcesses()
        for (i in processInfos.indices) {
            if (processInfos[i].processName == packageName) {
                LogUtil.i(
                    TAG,
                    String.format("the %s is running, isAppAlive return true", packageName)
                )
                return true
            }
        }
        LogUtil.i(TAG, String.format("the %s is not running, isAppAlive return false", packageName))
        return false
    }

    fun getIMEI(context: Context): String {
        val tel: TelephonyManager =
            context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        return tel.getDeviceId()
    }

    val phoneType: String
        get() = Build.MODEL

    fun isHaveMarket(context: Context): Boolean {
        val intent = Intent()
        intent.setAction("android.intent.action.MAIN")
        intent.addCategory("android.intent.category.APP_MARKET")
        val pm: PackageManager = context.packageManager
        val infos: List<ResolveInfo> = pm.queryIntentActivities(intent, 0)
        return infos.size > 0
    }

    fun setFullScreen(activity: Activity) {
        val params: WindowManager.LayoutParams = activity.getWindow()
            .getAttributes()
        activity.getWindow().setAttributes(params)
        activity.getWindow().addFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
        activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN)
    }

    fun cancelFullScreen(activity: Activity) {
        val params: WindowManager.LayoutParams = activity.getWindow()
            .getAttributes()
        activity.getWindow().setAttributes(params)
        activity.getWindow().clearFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
        activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN)
    }

    fun getPackageInfo(context: Context, pckName: String?): PackageInfo? {
        try {
            return context.packageManager
                .getPackageInfo(pckName!!, 0)
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return null
    }

    /**
     * 获取版本号
     *
     * @param context
     * @return
     */
    fun getVersionCode(context: Context): Int {
        var versionCode = 0
        versionCode = try {
            context.packageManager
                .getPackageInfo(
                    context.packageName,
                    0
                ).versionCode
        } catch (ex: PackageManager.NameNotFoundException) {
            0
        }
        return versionCode
    }

    /**
     * 获取指定包名应用的版本号
     *
     * @param context
     * @param packageName
     * @return
     */
    fun getVersionCode(context: Context, packageName: String?): Int {
        var versionCode = 0
        versionCode = try {
            context.packageManager
                .getPackageInfo(packageName!!, 0).versionCode
        } catch (ex: PackageManager.NameNotFoundException) {
            0
        }
        return versionCode
    }

    /**
     * 获取版本名
     *
     * @param context
     * @return
     */
    fun getVersionName(context: Context): String {
        var name = ""
        name = try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        } catch (ex: PackageManager.NameNotFoundException) {
            ""
        }
        return name
    }

    fun isScreenOn(context: Context): Boolean {
        val pm: PowerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        return pm.isScreenOn
    }

    /**
     * 安装应用
     *
     * @param context
     * @param file
     */
    fun installAPK(context: Context, file: File?) {
        if (file == null || !file.exists()) {
            return
        }
        val intent = Intent()
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.setAction(Intent.ACTION_VIEW)
        intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive")
        context.startActivity(intent)
    }

    fun getInstallApkIntent(file: File?): Intent {
        val intent = Intent()
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.setAction(Intent.ACTION_VIEW)
        intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive")
        return intent
    }

    /**
     * 拨打电话
     *
     * @param context
     * @param number
     */
    fun openDial(context: Context, number: String) {
        val uri = Uri.parse("tel:$number")
        val it = Intent(Intent.ACTION_DIAL, uri)
        context.startActivity(it)
    }

    fun openSMS(context: Context, smsBody: String?, tel: String) {
        val uri = Uri.parse("smsto:$tel")
        val it = Intent(Intent.ACTION_SENDTO, uri)
        it.putExtra("sms_body", smsBody)
        context.startActivity(it)
    }

    fun openDail(context: Context) {
        val intent = Intent(Intent.ACTION_DIAL)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    fun openSendMsg(context: Context) {
        val uri = Uri.parse("smsto:")
        val intent = Intent(Intent.ACTION_SENDTO, uri)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

//    fun openCamera(context: Context) {
//        val intent = Intent() // 调用照相机
//        intent.action = "android.media.action.STILL_IMAGE_CAMERA"
//        intent.flags = 0x34c40000
//        context.startActivity(intent)
//    }

    /**
     * 进入app设置详情页面
     */
    fun openAppDetail(context: Context) {
        val intent = Intent()
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS")
        intent.setData(Uri.fromParts("package", context.packageName, null))
        context.startActivity(intent)
    }

    /**
     * 是否是中文语言
     *
     * @param context
     * @return
     */
    fun isZhCN(context: Context): Boolean {
        val lang = context.resources.configuration.locale.country
        return lang.equals("CN", ignoreCase = true)
    }

    fun showSoftKeyboard(context: Context, view: View?) {
        (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).showSoftInput(
            view,
            InputMethodManager.SHOW_FORCED
        )
    }

    /**
     * 隐藏软键盘
     *
     * @param context
     * @param view
     */
    fun hideSoftKeyboard(context: Context, view: View?) {
        if (view == null) {
            return
        }
        val inputMethodManager =
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if (inputMethodManager.isActive && view.windowToken != null) {
            inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    /**
     * 是否是横屏
     *
     * @param context
     * @return
     */
    fun isLandscape(context: Context): Boolean {
        val flag: Boolean
        flag = if (context.resources.configuration.orientation == 2) {
            true
        } else {
            false
        }
        return flag
    }

    /**
     * 是否是竖屏
     *
     * @param context
     * @return
     */
    fun isPortrait(context: Context): Boolean {
        var flag = true
        if (context.resources.configuration.orientation != 1) {
            flag = false
        }
        return flag
    }

    /**
     * 启动App
     *
     * @param context
     * @param packagename 包名
     */
    fun startAppByPackageName(context: Context, packagename: String?) {

        // 通过包名获取此APP详细信息，包括Activities、services、versioncode、name等等
        var packageinfo: PackageInfo? = null
        try {
            packageinfo = context.packageManager.getPackageInfo(packagename!!, 0)
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        if (packageinfo == null) {
            return
        }

        // 创建一个类别为CATEGORY_LAUNCHER的该包名的Intent
        val resolveIntent = Intent(Intent.ACTION_MAIN, null)
        resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER)
        resolveIntent.setPackage(packageinfo.packageName)

        // 通过getPackageManager()的queryIntentActivities方法遍历
        val resolveinfoList: List<ResolveInfo> = context.packageManager
            .queryIntentActivities(resolveIntent, 0)
        val resolveinfo: ResolveInfo = resolveinfoList.iterator().next()
        if (resolveinfo != null) {
            // packagename = 参数packname
            val packageName: String = resolveinfo.activityInfo.packageName
            // 这个就是我们要找的该APP的LAUNCHER的Activity[组织形式：packagename.mainActivityname]
            val className: String = resolveinfo.activityInfo.name
            // LAUNCHER Intent
            val intent = Intent(Intent.ACTION_MAIN)
            intent.addCategory(Intent.CATEGORY_LAUNCHER)

            // 设置ComponentName参数1:packagename参数2:MainActivity路径
            val cn = ComponentName(packageName, className)
            intent.setComponent(cn)
            context.startActivity(intent)
        }
    }

    fun screenShot(activity: Activity): Bitmap {
        // 获取windows中最顶层的view
        val view: View = activity.getWindow().getDecorView()
        view.buildDrawingCache()

        // 获取状态栏高度
        val rect = Rect()
        view.getWindowVisibleDisplayFrame(rect)
        val statusBarHeights = rect.top
        val display: Display = activity.windowManager.defaultDisplay

        // 获取屏幕宽和高
        val widths: Int = display.width
        val heights: Int = display.height

        // 允许当前窗口保存缓存信息
        view.isDrawingCacheEnabled = true

        // 去掉状态栏
        val bmp: Bitmap = Bitmap.createBitmap(
            view.drawingCache, 0,
            statusBarHeights, widths, heights - statusBarHeights
        )

        // 销毁缓存信息
        view.destroyDrawingCache()
        return bmp
    }

    /**
     * 判断系统是否设置了默认浏览器
     *
     * @param context
     * @param intent
     * @return
     */
    fun hasPreferredApplication(context: Context, intent: Intent): Boolean {
        val pm: PackageManager = context.packageManager
        val info: ResolveInfo? = pm.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
        return "com.android.browser" != info?.activityInfo?.packageName
    }

    fun gc() {
        System.gc()
        System.runFinalization()
    }

    fun getAppName(context: Context, pID: Int): String? {
        var processName: String? = null
        val am: ActivityManager =
            context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val l: List<*> = am.getRunningAppProcesses()
        val i = l.iterator()
        val pm: PackageManager = context.packageManager
        while (i.hasNext()) {
            val info: ActivityManager.RunningAppProcessInfo =
                i.next() as ActivityManager.RunningAppProcessInfo
            try {
                if (info.pid == pID) {
                    processName = info.processName
                    return processName
                }
            } catch (e: Exception) {
                LogUtil.d("Process", "Error>> :$e")
            }
        }
        return processName
    }

    val sDCardAvailableSize: Long
        get() {
            if (isExitsSdcard) {
                val fs = StatFs(BaseApplication.getContext().getExternalFilesDir("")?.absolutePath)
                val count: Long = fs.availableBlocksLong
                val size: Long = fs.blockSizeLong
                return count * size / 1024 / 1024
            }
            return 0
        }

    /**
     * @param paint       文字控件 paint
     * @param str         源文字
     * @param totalWidth  文字限制的总宽度
     * @param defaultWord 默认文字
     * @return 截取文本控件上指定宽度的文字
     */
    fun getSubStringIndex(
        paint: TextPaint,
        str: String,
        totalWidth: Float,
        defaultWord: String
    ): String {
        if (TextUtils.isEmpty(str) || totalWidth <= 0) {
            return ""
        }
        if (paint.measureText(str) <= totalWidth) {
            return str
        }
        val defaultWidht: Float = paint.measureText(defaultWord)
        val width = totalWidth - defaultWidht
        val length = str.length
        var measurennums: Int = paint.breakText(str, true, width, null)
        if (measurennums > length) {
            measurennums = length
        }
        val suffix: String
        suffix = if (TextUtils.isEmpty(defaultWord)) {
            ""
        } else {
            "...$defaultWord"
        }
        return str.substring(0, measurennums) + suffix
    }

    /**
     * @param paint       文字控件 paint
     * @param str         源文字
     * @param totalWidth  文字限制的总宽度
     * @param defaultWord 默认文字
     * @return 截取文本控件需要的文字位置
     */
    fun getSubStringIndex(
        paint: TextPaint,
        str: CharSequence,
        totalWidth: Float,
        defaultWord: String?
    ): Int {
        if (TextUtils.isEmpty(str) || totalWidth <= 0) {
            return 0
        }
        val defaultWidht: Float = paint.measureText(defaultWord)
        val wordWidht: Float = paint.measureText(str.toString())
        val width = totalWidth - defaultWidht
        if (wordWidht < width) {
            return 0
        }
        val length = str.length
        var measurennums: Int = paint.breakText(str.toString(), true, width, null) - 1
        if (measurennums > length) {
            measurennums = length
        }
        return length - measurennums
    }

    /**
     * 获取字符数长度
     *
     * @param s
     * @return
     */
    fun getLength(s: String): Int {
        var valueLength = 0.0
        val chinese = "[\u4e00-\u9fa5]"
        for (i in s.indices) {
            val temp = s.substring(i, i + 1)
            // 判断是否为中文字符
            valueLength += if (temp.matches(chinese.toRegex())) {
                // 中文字符长度为1
                1.0
            } else {
                // 其他字符长度为0.5
                0.5
            }
        }
        return ceil(valueLength).toInt()
    }

    fun getNeedStrLength(src: String, use: String): Int {
        if (use.length >= src.length) {
            return 0
        }
        val count = getLength(use)
        if (count > src.length) {
            return 0
        }
        if (src.length - count <= 0) {
            return 0
        }
        var test = getLength(src.substring(src.length - count, src.length))
        var temp = 0
        while (test < count) {
            temp++
            if (src.length - (count + temp) <= 0) {
                break
            }
            test = getLength(src.substring(src.length - (count + temp), src.length))
        }
        return count + temp
    }

    // 设置刘海区域可用
    fun openFullScreenModel(mAc: Activity?) {
//        if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O_MR1) {
//            mAc.requestWindowFeature(Window.FEATURE_NO_TITLE);
//            WindowManager.LayoutParams lp = mAc.getWindow().getAttributes();
////            lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
//            mAc.getWindow().setAttributes(lp);
//        }
    }

    // 刘海高度
    fun getHeight(mAc: Activity?): Int {
//        View decorView = mAc.getWindow().getDecorView();
//        if (decorView != null) {
//            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
//                WindowInsets windowInsets = decorView.getRootWindowInsets();
//                if (windowInsets != null) {
////                    DisplayCutout displayCutout = windowInsets.getDisplayCutout();
////                    return displayCutout.getSafeInsetTop();
//                }
//            }
//        }
        return 0
    }

    /**
     * 启动组件
     *
     * @param componentName 组件名
     */
    fun enableComponent(context: Context, componentName: ComponentName) {
        //此方法用以启用和禁用组件，会覆盖Androidmanifest文件下定义的属性
        context.packageManager.setComponentEnabledSetting(
            componentName,
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )
    }

    /**
     * 禁用组件
     *
     * @param componentName 组件名
     */
    fun disableComponent(context: Context, componentName: ComponentName) {
        context.packageManager.setComponentEnabledSetting(
            componentName,
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP
        )
    }

    val eMUI: String?
        get() {
            var classType: Class<*>? = null
            var buildVersion: String? = null
            try {
                classType = Class.forName("android.os.SystemProperties")
                val getMethod = classType.getDeclaredMethod(
                    "get", *arrayOf<Class<*>>(
                        String::class.java
                    )
                )
                buildVersion =
                    getMethod.invoke(classType, *arrayOf<Any>("ro.build.version.emui")) as String
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return buildVersion
        }
    val appBit: String
        get() {
            var bitNum = "32"
            val apkBit = "arm64-v8a"
            val s64bits: Array<String> = Build.SUPPORTED_64_BIT_ABIS
            if (s64bits != null && s64bits.size > 0) {
                for (s in s64bits) {
                    if (s == apkBit) {
                        bitNum = "64"
                        return bitNum
                    }
                }
            }
            return bitNum
        }

    @SuppressLint("PrivateApi", "DiscouragedPrivateApi")
    fun changeSystemLanguage(locale: Locale?) {
        LogUtil.e("kevin","切换语言")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val locales = LocaleList(locale)
            if (locale != null) {
                try {
                    val classActivityManagerNative =
                        Class.forName("android.app.ActivityManagerNative")
                    val getDefault = classActivityManagerNative.getDeclaredMethod("getDefault")
                    val objIActivityManager = getDefault.invoke(classActivityManagerNative)
                    val classIActivityManager = Class.forName("android.app.IActivityManager")
                    val getConfiguration =
                        classIActivityManager.getDeclaredMethod("getConfiguration")
                    val config = getConfiguration.invoke(objIActivityManager) as Configuration
                    config.setLocales(locales)
                    val clzParams = arrayOf<Class<*>>(
                        Configuration::class.java
                    )
                    val updateConfiguration = classIActivityManager.getDeclaredMethod(
                        "updatePersistentConfiguration",
                        *clzParams
                    )
                    updateConfiguration.invoke(objIActivityManager, config)
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
            }
        } else {
            updateLanguage(locale)
        }
    }

    @SuppressLint("PrivateApi", "DiscouragedPrivateApi")
    private fun updateLanguage(locale: Locale?) {
        try {
            val objIActMag: Any
            val clzIActMag = Class.forName("android.app.IActivityManager")
            val clzActMagNative = Class.forName("android.app.ActivityManagerNative")
            val `mtdActMagNative$getDefault` = clzActMagNative.getDeclaredMethod("getDefault")
            objIActMag = `mtdActMagNative$getDefault`.invoke(clzActMagNative)
            val `mtdIActMag$getConfiguration` = clzIActMag.getDeclaredMethod("getConfiguration")
            val config = `mtdIActMag$getConfiguration`.invoke(objIActMag) as Configuration
            config.locale = locale
            val clzConfig = Class.forName("android.content.res.Configuration")
            val userSetLocale = clzConfig.getField("userSetLocale")
            userSetLocale[config] = true
            // 此处需要声明权限:android.permission.CHANGE_CONFIGURATION
            // 会重新调用 onCreate();
            val clzParams = arrayOf<Class<*>>(Configuration::class.java)
            val `mtdIActMag$updateConfiguration` =
                clzIActMag.getDeclaredMethod("updateConfiguration", *clzParams)
            `mtdIActMag$updateConfiguration`.invoke(objIActMag, config)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

}