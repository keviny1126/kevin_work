package com.power.baseproject.utils

import android.text.TextUtils
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

class PropertiesUtils public constructor(path: String) {
    private var filePath: String? = null
    var file: File

    init {
        filePath = path
        if (TextUtils.isEmpty(filePath)) {
            throw NullPointerException("filePath is null");
        }
        file = File(filePath!!)
        if (!file.exists()) {
            try {
                file.createNewFile()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            file.parentFile?.mkdirs()
        }
    }

    private fun get(): Properties {
        var fis: FileInputStream? = null
        val props = Properties()
        try {
            if (!file.exists()) {
                try {
                    file.createNewFile()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                file.parentFile?.mkdirs()
            }
            fis = FileInputStream(file)
            props.load(fis)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            if (null != fis) {
                try {
                    fis.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        return props
    }

    /**
     * 对外提供的get方法
     * @param key
     * @return
     */
    public fun get(key: String): String? {
        val props = get()
        return props.getProperty(key)
    }

    private fun setProps(p: Properties) {
        var fos: FileOutputStream? = null
        try {
            fos = FileOutputStream(file)
            p.store(fos, null)
            fos.flush()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            if (null != fos) {
                try {
                    fos.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    /**
     * 对外提供的保存key value方法
     * @param key
     * @param value
     */
    public fun set(key: String, value: String) {
        val props = get()
        props.setProperty(key, value)
        setProps(props)
    }

    /**
     * 对外提供的删除方法
     * @param key
     */
    public fun remove(vararg key: String) {
        val props = get()
        for (k in key) {
            props.remove(k)
        }
        setProps(props)
    }
}