package com.newchip.tool.leaktest.base

import android.graphics.Bitmap
import android.graphics.Color
import com.jeremyliao.liveeventbus.LiveEventBus
import com.power.baseproject.ktbase.application.BaseApplication
import com.power.baseproject.utils.*
import com.power.baseproject.utils.log.LogConfig
import com.power.baseproject.utils.log.LogUtil
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import java.io.IOException


class AppApplication : BaseApplication() {

    override fun onCreate() {
        super.onCreate()
        //setWallpaper("#000000")
        initKoin()
        initLiveEventBus()
        CrashManager.instance.init(this)
        LogConfig.instance.debugConfig()
        initConfig()
    }

    private fun initConfig() {
        val properties = Tools.getProperties(this)
        val product = Tools.getPropertySample(properties, ConstantsUtils.PRODUCT_NAME)
        if (!product.isNullOrEmpty()) {
            EasyPreferences.instance.put(ConstantsUtils.PRODUCT_NAME, product)
        }
    }

    private fun initLiveEventBus() {
        LiveEventBus
            .config()
            .enableLogger(false)
            .autoClear(true)
            .lifecycleObserverAlwaysActive(true)
    }

    private fun initKoin() {
        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@AppApplication)
            modules(appKtModule)
        }
    }

//    private fun setWallpaper(rgb: String) {
////    @SuppressLint("ResourceType")
////    InputStream input = MyApp.getContext().getResources().openRawResource(R.drawable.black);
//        val bitmap = Bitmap.createBitmap(512, 512, Bitmap.Config.ARGB_8888)
//        bitmap.eraseColor(Color.parseColor(rgb)) //填充颜色
//        try {
//            LogUtil.e("kevin", "setWallpaper...")
//            applicationContext.setWallpaper(bitmap)
//        } catch (e: IOException) {
//            e.printStackTrace()
//        }
//    }
}