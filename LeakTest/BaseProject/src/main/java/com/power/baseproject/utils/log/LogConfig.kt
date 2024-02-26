package com.power.baseproject.utils.log

import android.text.TextUtils
import com.power.baseproject.utils.PathUtils
import com.power.baseproject.utils.PropertiesUtils

class LogConfig private constructor() {
    private var filePath: String? = null
    private var properties: PropertiesUtils? = null
    public var isDebug = false

    companion object {
        const val configFile = "log_config.properties"
        val instance: LogConfig by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            LogConfig()
        }
    }

    init {
        filePath = PathUtils.getTestConfigPath() + configFile
        properties = PropertiesUtils(filePath!!)
    }

    public fun debugConfig() {
        val config = properties?.get("debug_config")
        if (!TextUtils.isEmpty(config) && config == "1") {
            isDebug = true
        }
    }
}