package com.power.baseproject.utils

import android.annotation.TargetApi
import android.app.Activity
import android.graphics.Color
import android.os.Build
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.core.content.ContextCompat

/**
 * @Describe 状态栏工具类
 * @Author Jungle68
 * @Date 2016/12/15
 * @Contact 335891510@qq.com
 */
object StatusBarUtils {
    /**
     * 设置状态栏黑色字体图标，
     * 适配4.4以上版本MIUIV、Flyme和6.0以上版本其他Android1:MIUUI 2:Flyme 3:android6.0
     */
    const val STATUS_TYPE_MIUUI = 1
    const val STATUS_TYPE_FLYME = 2
    const val STATUS_TYPE_ANDROID_M = 3

    /**
     * 修改状态栏为全透明,侵入式
     *
     * @param activity
     */
    @TargetApi(19)
    fun transparencyBar(activity: Activity?) {
        if (activity == null) {
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = activity.window
            if (window != null) {
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS or WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
                //            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                          | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
//                          | View.SYSTEM_UI_FLAG_LAYOUT_STABLE  //确保应用布局的稳定
//                          | View.SYSTEM_UI_FLAG_FULLSCREEN ;
//            window.getDecorView().setSystemUiVisibility(uiOptions);
                window.decorView.systemUiVisibility = (
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN //Activity全屏显示，但状态栏不会被隐藏覆盖，状态栏依然可见，Activity顶端布局部分会被状态遮住。
                                //                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION  //效果同View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS) //需要设置这个 flag 才能调用 setStatusBarColor 来设置状态栏颜色
                window.statusBarColor = Color.TRANSPARENT
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            val window = activity.window
            window?.setFlags(
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
            )
        }
    }

    /**
     * 修改状态栏颜色，支持4.4以上版本，需要导入开源库 使用SystemBarTint库使
     *
     * @param activity
     * @param colorId
     */
    fun setStatusBarColor(activity: Activity, colorId: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (intgetType(activity.window) == 0) {
            } else {
                val window = activity.window
                window.statusBarColor = ContextCompat.getColor(activity, colorId)
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (intgetType(activity.window) == 0) {
            } else {       //使用SystemBarTint库使4.4版本状态栏变色，需要先将状态栏设置为透明
                transparencyBar(activity)
                val tintManager = SystemBarTintManager(activity)
                tintManager.isStatusBarTintEnabled = true
                tintManager.setStatusBarTintResource(colorId)
            }
        }
    }

    /**
     * 设置状态栏黑色字体图标，
     * 适配4.4以上版本MIUIV、Flyme和6.0以上版本其他Android
     *
     * @param activity
     * @return 1:MIUUI 2:Flyme 3:android6.0
     */
    fun statusBarLightMode(activity: Activity): Int {
        var result = 0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            activity.window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            result = STATUS_TYPE_ANDROID_M
            MIUISetStatusBarLightMode(activity.window, true)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (MIUISetStatusBarLightMode(activity.window, true)) {
                result = STATUS_TYPE_MIUUI
            } else if (FlymeSetStatusBarLightMode(activity.window, true)) {
                result = STATUS_TYPE_FLYME
            }
        }
        return result
    }

    /**
     * 设置状态栏白色字体图标，
     * 适配4.4以上版本MIUIV、Flyme和6.0以上版本其他Android
     *
     * @param activity
     * @return 1:MIUUI 2:Flyme 3:android6.0
     */
    fun statusBarDarkMode(activity: Activity): Int {
        var result = 0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (MIUISetStatusBarLightMode(activity.window, false)) {
                result = STATUS_TYPE_MIUUI
            } else if (FlymeSetStatusBarLightMode(activity.window, false)) {
                result = STATUS_TYPE_FLYME
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                activity.window.decorView.systemUiVisibility =
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
                result = STATUS_TYPE_ANDROID_M
            }
        }
        return result
    }

    /**
     * 已知系统类型时，设置状态栏黑色字体图标。
     * 适配4.4以上版本MIUIV、Flyme和6.0以上版本其他Android
     *
     * @param activity
     * @param type     1:MIUUI 2:Flyme 3:android6.0
     */
    fun statusBarLightMode(activity: Activity, type: Int) {
        if (type == STATUS_TYPE_MIUUI) {
            MIUISetStatusBarLightMode(activity.window, true)
        } else if (type == STATUS_TYPE_FLYME) {
            FlymeSetStatusBarLightMode(activity.window, true)
        } else if (type == STATUS_TYPE_ANDROID_M) {
            activity.window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
    }

    /**
     * 清除MIUI或flyme或6.0以上版本状态栏黑色字体
     */
    fun StatusBarDarkMode(activity: Activity, type: Int) {
        if (type == STATUS_TYPE_MIUUI) {
            MIUISetStatusBarLightMode(activity.window, false)
        } else if (type == STATUS_TYPE_FLYME) {
            FlymeSetStatusBarLightMode(activity.window, false)
        } else if (type == STATUS_TYPE_ANDROID_M) {
            activity.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
        }
    }

    /**
     * 设置状态栏图标为深色和魅族特定的文字风格
     * 可以用来判断是否为Flyme用户
     *
     * @param window 需要设置的窗口
     * @param dark   是否把状态栏字体及图标颜色设置为深色
     * @return boolean 成功执行返回true
     */
    fun FlymeSetStatusBarLightMode(window: Window?, dark: Boolean): Boolean {
        var result = false
        if (window != null) {
            try {
                val lp = window.attributes
                val darkFlag = WindowManager.LayoutParams::class.java
                    .getDeclaredField("MEIZU_FLAG_DARK_STATUS_BAR_ICON")
                val meizuFlags = WindowManager.LayoutParams::class.java
                    .getDeclaredField("meizuFlags")
                darkFlag.isAccessible = true
                meizuFlags.isAccessible = true
                val bit = darkFlag.getInt(null)
                var value = meizuFlags.getInt(lp)
                value = if (dark) {
                    value or bit
                } else {
                    value and bit.inv()
                }
                meizuFlags.setInt(lp, value)
                window.attributes = lp
                result = true
            } catch (e: Exception) {
            }
        }
        return result
    }

    /**
     * 设置状态栏字体图标为深色，需要MIUIV6以上
     *
     * @param window 需要设置的窗口
     * @param dark   是否把状态栏字体及图标颜色设置为深色
     * @return boolean 成功执行返回true
     */
    fun MIUISetStatusBarLightMode(window: Window?, dark: Boolean): Boolean {
        var result = false
        if (window != null) {
            val clazz: Class<*> = window.javaClass
            try {
                var darkModeFlag = 0
                val layoutParams = Class.forName("android.view.MiuiWindowManager\$LayoutParams")
                val field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE")
                darkModeFlag = field.getInt(layoutParams)
                val extraFlagField = clazz.getMethod(
                    "setExtraFlags",
                    Int::class.javaPrimitiveType,
                    Int::class.javaPrimitiveType
                )
                if (dark) {
                    extraFlagField.invoke(window, darkModeFlag, darkModeFlag) //状态栏透明且黑色字体
                } else {
                    extraFlagField.invoke(window, 0, darkModeFlag) //清除黑色字体
                }
                result = true
            } catch (e: Exception) {
            }
        }
        return result
    }

    /**
     * 获取系统类型
     *
     * @param window
     * @return
     */
    fun intgetType(window: Window?): Int {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) return STATUS_TYPE_ANDROID_M
        if (window != null) {
            try {
                val lp = window.attributes
                val darkFlag = WindowManager.LayoutParams::class.java
                    .getDeclaredField("MEIZU_FLAG_DARK_STATUS_BAR_ICON")
                if (darkFlag != null) return STATUS_TYPE_FLYME
                val layoutParams = Class.forName("android.view.MiuiWindowManager\$LayoutParams")
                if (layoutParams != null) return STATUS_TYPE_MIUUI
            } catch (e: Exception) {
            }
        }
        return 0
    }
}