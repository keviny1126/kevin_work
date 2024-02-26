package com.power.baseproject.ktbase.application

import android.app.Application
import android.content.Context

abstract class BaseApplication : Application() {

    companion object {
        lateinit var mApp: BaseApplication

        fun getContext(): Context {
            return mApp.applicationContext
        }
    }

    override fun onCreate() {
        super.onCreate()
        mApp = this
    }

}