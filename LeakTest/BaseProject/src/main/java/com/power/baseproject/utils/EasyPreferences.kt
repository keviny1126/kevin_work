package com.power.baseproject.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.power.baseproject.ktbase.application.BaseApplication
import com.power.baseproject.utils.log.LogUtil
import java.io.*
import java.net.URLDecoder
import java.net.URLEncoder
import java.util.*

@SuppressLint("SdCardPath")

class EasyPreferences {
    private var mPreferences: SharedPreferences = BaseApplication.getContext().getSharedPreferences(
        shareName, Context.MODE_PRIVATE
    )

    fun put(key: String, value: Boolean): Boolean {
        val edit = mPreferences.edit()
        if (edit != null) {
            edit.putBoolean(getLowKey(key), value)
            return edit.commit()
        }
        return false
    }

    fun remove(key: String): Boolean {
        val edit = mPreferences.edit()
        if (edit != null) {
            edit.remove(getLowKey(key))
            return edit.commit()
        }
        return false
    }

    fun put(key: String, value: String?): Boolean {
        val edit = mPreferences.edit()
        if (edit != null) {
            edit.putString(getLowKey(key), value)
            return edit.commit()
        }
        return false
    }

    fun put(key: String, value: Int): Boolean {
        val edit = mPreferences.edit()
        if (edit != null) {
            edit.putInt(getLowKey(key), value)
            return edit.commit()
        }
        return false
    }

    fun put(key: String, value: Float): Boolean {
        val edit = mPreferences.edit()
        if (edit != null) {
            edit.putFloat(getLowKey(key), value)
            return edit.commit()
        }
        return false
    }

    fun put(key: String, value: Long): Boolean {
        val edit = mPreferences.edit()
        if (edit != null) {
            edit.putLong(getLowKey(key), value)
            return edit.commit()
        }
        return false
    }

    fun put(key: String, value: Set<String>?): Boolean {
        val edit = mPreferences.edit()
        if (edit != null) {
            edit.putStringSet(getLowKey(key), value)
            return edit.commit()
        }
        return false
    }

    fun <T> putToJson(key: String, t: T): Boolean {
        val edit = mPreferences.edit()
        if (edit != null) {
            val msg = Gson().toJson(t)
            LogUtil.i("kevin", "putToJson:$msg")
            edit.putString(getLowKey(key), msg)
            return edit.commit()
        }
        return false
    }

    operator fun get(key: String): String? {
        return mPreferences.getString(getLowKey(key), "")
    }

    operator fun get(key: String, defValue: String?): String? {
        return mPreferences.getString(getLowKey(key), defValue)
    }

    operator fun get(key: String, defValue: Boolean): Boolean {
        return mPreferences.getBoolean(getLowKey(key), defValue)
    }

    operator fun get(key: String, defValue: Int): Int {
        return mPreferences.getInt(getLowKey(key), defValue)
    }

    operator fun get(key: String, defValue: Float): Float {
        return mPreferences.getFloat(getLowKey(key), defValue)
    }

    operator fun get(key: String, defValue: Long): Long {
        return mPreferences.getLong(getLowKey(key), defValue)
    }

    operator fun get(key: String, defValue: Set<String?>?): Set<String>? {
        return mPreferences.getStringSet(getLowKey(key), defValue)
    }

    private fun getLowKey(key: String): String {
        var keyLow = key
        if (key.isNotEmpty()) {
            keyLow = key.lowercase(Locale.getDefault())
        }
        return keyLow
    }

    fun clearAll() {
        try {
            val fileName = "$shareName.xml"
            val path =
                StringBuilder(DATA_URL).append(BaseApplication.getContext().packageName)
                    .append(
                        SHARED_PREFS
                    )
            val file = File(path.toString(), fileName)
            if (file.exists()) {
                file.delete()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun clearData(): Boolean {
        return mPreferences.edit().clear().commit()
    }

    /**
     * 序列化对象
     *
     * @param object
     * @return
     */
    fun serialize(`object`: Any?): String? {
        val byteArrayOutputStream = ByteArrayOutputStream()
        var objectOutputStream: ObjectOutputStream? = null
        try {
            objectOutputStream = ObjectOutputStream(
                byteArrayOutputStream
            )
        } catch (e: IOException) {
            e.printStackTrace()
        }
        try {
            objectOutputStream!!.writeObject(`object`)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        var strResult: String? = null
        try {
            strResult = byteArrayOutputStream.toString("ISO-8859-1")
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }
        try {
            strResult = URLEncoder.encode(strResult, "UTF-8")
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }
        try {
            objectOutputStream!!.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        try {
            byteArrayOutputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return strResult
    }

    /**
     * 反序列化对象
     *
     * @param str
     * @return
     */
    fun deSerialization(str: String?): Any? {
        var buf: String? = null
        try {
            buf = URLDecoder.decode(str, "UTF-8")
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }
        var byteArrayInputStream: ByteArrayInputStream? = null
        try {
            byteArrayInputStream = ByteArrayInputStream(buf!!.toByteArray(charset("ISO-8859-1")))
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }
        var objectInputStream: ObjectInputStream? = null
        try {
            objectInputStream = ObjectInputStream(byteArrayInputStream)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        var `object`: Any? = null
        try {
            `object` = objectInputStream!!.readObject()
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        try {
            objectInputStream!!.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        try {
            byteArrayInputStream!!.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return `object`
    }

    companion object {
        const val DATA_URL = "/data/data/"
        const val shareName = "Preferences_Data"
        const val SHARED_PREFS = "/shared_prefs"
        val instance: EasyPreferences by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            EasyPreferences()
        }
    }
}