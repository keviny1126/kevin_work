package com.power.baseproject.utils

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.content.Context
import android.os.SystemClock
import com.power.baseproject.ktbase.application.BaseApplication
import java.io.IOException
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


object DateUtils {
    /**
     * 日期格式yyyy-MM-dd字符串常量
     */
    const val DATE_FORMAT = "yyyy-MM-dd"
    const val DATE_FORMAT_2 = "yyyy/dd/MM"
    const val DATE_FORMAT2 = "yyyy-MM-dd HH:mm:ss"
    const val DATE_FORMAT_AMERICAN = "MM/dd/yyyy"
    const val DATE_FORMAT_AMERICAN2 = "MM/dd/yyyy HH:mm:ss"
    const val DATE_FORMAT_AMERICAN2_ZH = "yyyy/MM/dd HH:mm:ss"
    const val DATE_FORMAT_SOFT_CN = "yyyy-MM-dd"
    const val DATE_FORMAT_SOFT_EN = "MM/dd/yyyy"
    const val DATE_FORMAT_FILENAME = "yyyyMMdd_HHmmss"
    private val sdf_date_format = SimpleDateFormat(DATE_FORMAT, Locale.ENGLISH)
    private val dateTimeFormat = SimpleDateFormat(DATE_FORMAT2, Locale.CHINA)
    private val cale = Calendar.getInstance()
    private val tag = DateUtils::class.java.simpleName
    const val UPDATETIME = "updatetime"

    /**
     * 获取当前时间
     */
    fun getCurrentTime(format: String?): String {
        @SuppressLint("SimpleDateFormat") val sDateFormat = SimpleDateFormat(format)
        return sDateFormat.format(Date())
    }

    /**
     * 根据 timestamp 生成各类时间状态串
     *
     * @param timestamp 距1970 00:00:00 GMT的秒数
     * @param format    格式
     * @return 时间状态串(如 ： 刚刚5分钟前)
     */
    @SuppressLint("SimpleDateFormat")
    fun getTimeState(timestamp: String?, format: String?): String {
        var timestamp = timestamp
        return if (timestamp == null || "" == timestamp) {
            ""
        } else try {
            timestamp = formatTimestamp(timestamp)
            val _timestamp = timestamp.toLong()
            if (System.currentTimeMillis() - _timestamp < 1 * 60 * 1000) {
                "刚刚"
            } else if (System.currentTimeMillis() - _timestamp < 30 * 60 * 1000) {
                ((System.currentTimeMillis() - _timestamp) / 1000 / 60).toString() + "分钟前"
            } else {
                val now = Calendar.getInstance()
                val c = Calendar.getInstance()
                c.timeInMillis = _timestamp
                if (c[Calendar.YEAR] == now[Calendar.YEAR] && c[Calendar.MONTH] == now[Calendar.MONTH] && c[Calendar.DATE] == now[Calendar.DATE]) {
                    val sdf = SimpleDateFormat("今天 HH:mm")
                    return sdf.format(c.time)
                }
                if (c[Calendar.YEAR] == now[Calendar.YEAR] && c[Calendar.MONTH] == now[Calendar.MONTH] && c[Calendar.DATE] == now[Calendar.DATE] - 1) {
                    val sdf = SimpleDateFormat("昨天 HH:mm")
                    sdf.format(c.time)
                } else if (c[Calendar.YEAR] == now[Calendar.YEAR]) {
                    var sdf: SimpleDateFormat? = null
                    sdf = if (format != null && !format.equals("", ignoreCase = true)) {
                        SimpleDateFormat(format)
                    } else {
                        SimpleDateFormat("M月d日 HH:mm:ss")
                    }
                    sdf.format(c.time)
                } else {
                    var sdf: SimpleDateFormat? = null
                    sdf = if (format != null && !format.equals("", ignoreCase = true)) {
                        SimpleDateFormat(format)
                    } else {
                        SimpleDateFormat("yyyy年M月d日 HH:mm:ss")
                    }
                    sdf.format(c.time)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    /**
     * 对时间戳格式进行格式化，保证时间戳长度为13位
     *
     * @param timestamp 时间戳
     * @return 返回为13位的时间戳
     */
    fun formatTimestamp(timestamp: String?): String {
        if (timestamp == null || "" == timestamp) {
            return ""
        }
        var tempTimeStamp: String? = timestamp + "00000000000000"
        val stringBuffer = StringBuffer(tempTimeStamp)
        return stringBuffer.substring(0, 13).also { tempTimeStamp = it }
    }

    /**
     * @param time 2014-12-6 11:18:00
     * @param time
     * @return
     * @data 2014-12-6
     */
    fun getTimestamp(time: String): Long {
        val simpledateformat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
        var date2: Date? = null
        try {
            date2 = simpledateformat.parse(time) // 将参数按照给定的格式解析参数
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return date2!!.time
    }

    fun getTimesDate(time: String): Date? {
        val simpledateformat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
        var date2: Date? = null
        try {
            date2 = simpledateformat.parse(time) // 将参数按照给定的格式解析参数
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return date2
    }

    fun getTimesDate(format: String, date: String): String {
        val simpledateformat = SimpleDateFormat(format, Locale.getDefault())
        var afterFormat = date
        try {
            val parseDate = simpledateformat.parse(date)
            afterFormat = simpledateformat.format(parseDate)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return afterFormat
    }

    fun getTimeForDate(time: String, locale: Locale): String {
        try {
            val sdf = SimpleDateFormat(DATE_FORMAT_SOFT_CN, Locale.getDefault())
            val d = sdf.parse(time)
            val sdf1 = SimpleDateFormat(DATE_FORMAT_SOFT_EN, locale)
            return sdf1.format(Date(d.time))
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }

    /**
     * 返回日期加X月后的日期
     *
     * @param date
     * @param i
     * @return
     */
    fun addMonth(date: String, i: Int): String {
        return try {
            val gCal = GregorianCalendar(
                date.substring(0, 4).toInt(),
                date.substring(5, 7).toInt() - 1,
                date.substring(8, 10).toInt()
            )
            gCal.add(GregorianCalendar.MONTH, i)
            sdf_date_format.format(gCal.time)
        } catch (e: Exception) {
            DateUtils.date
        }
    }

    /**
     * 获得服务器当前日期，以格式为：yyyy-MM-dd的日期字符串形式返回
     *
     * @return
     */
    val date: String
        get() = try {
            sdf_date_format.format(cale.time)
        } catch (e: Exception) {
            ""
        }

    val dateTime: String
        get() = try {
            dateTimeFormat.format(cale.time)
        } catch (e: Exception) {
            ""
        }

    /**
     * 根据时间获取格式化时间显示格式
     *
     * @param time
     * @return
     */
    fun formatDate(time: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault())
        return sdf.format(Date(time))
    }

    fun getFormatDate(time: String, format: String): String {
        val simpledateformat = SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault())
        val sdf = SimpleDateFormat(format, Locale.getDefault())
        var date = ""
        try {
            date = sdf.format(simpledateformat.parse(time)!!) // 将参数按照给定的格式解析参数
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return date
    }

    /**
     * 获取美国时间工具类
     *
     * @param time long型，10位或者13位
     * @return
     */
    fun getTimeForAmerican(time: String): String {
        val sdf = SimpleDateFormat(DATE_FORMAT_AMERICAN2, Locale.getDefault())
        val mTime = if (time.length > 10) time else time + "000"
        val date = Date(mTime.toLong())
        return sdf.format(date)
    }

    /**
     * 获取美国时间工具类
     *
     * @param time long型，10位或者13位
     * @return
     */
    fun getTimeForAmericanZH(time: String): String {
        val sdf = SimpleDateFormat(DATE_FORMAT_AMERICAN2_ZH, Locale.getDefault())
        val mTime = if (time.length > 10) time else time + "000"
        val date = Date(mTime.toLong())
        return sdf.format(date)
    }

    /**
     * 获取美国时间
     *
     * @param time
     * @param format 传入格式
     * @return
     */
    fun getTimeForAmerican(time: String, format: String?): String {
        val sdf = SimpleDateFormat(format, Locale.getDefault())
        val mTime = if (time.length > 10) time else time + "000"
        val date = Date(mTime.toLong())
        return sdf.format(date)
    }

    /**
     * 时间戳转换成字符窜
     *
     * @param milSecond
     * @param pattern
     * @return
     */
    fun getDateToString(milSecond: Long, pattern: String?): String {
        val date = Date(milSecond)
        val format = SimpleDateFormat(pattern, Locale.ENGLISH)
        return format.format(date)
    }

    /**
     * 将字符串转为时间戳
     *
     * @param dateString
     * @param pattern
     * @return
     */
    fun getStringToDateTime(dateString: String, pattern: String): Long {
        val date = getStringToDate(dateString, pattern)
        return date.time
    }

    /**
     * 获取日期字符串转化为Date对象
     *
     * @param dateString
     * @param pattern
     * @return
     */
    fun getStringToDate(dateString: String, pattern: String): Date {
        val dateFormat = SimpleDateFormat(pattern, Locale.ENGLISH)
        var date = Date()
        try {
            date = dateFormat.parse(dateString)!!
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return date
    }

    @Throws(IOException::class, InterruptedException::class)
    fun setDateTime(year: Int, month: Int, day: Int, hour: Int, minute: Int) {
        val c = Calendar.getInstance()
        c[Calendar.YEAR] = year
        c[Calendar.MONTH] = month -1
        c[Calendar.DAY_OF_MONTH] = day
        c[Calendar.HOUR_OF_DAY] = hour
        c[Calendar.MINUTE] = minute
        val `when` = c.timeInMillis
        if (`when` / 1000 < Int.MAX_VALUE) {
//            SystemClock.setCurrentTimeMillis(`when`)
            (BaseApplication.getContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager).setTime(`when`)
        }
        val now = Calendar.getInstance().timeInMillis
        if (now - `when` > 1000) throw IOException("failed to set Date.")
    }

    @Throws(IOException::class, InterruptedException::class)
    fun setDate(year: Int, month: Int, day: Int) {
        val c = Calendar.getInstance()
        c[Calendar.YEAR] = year
        c[Calendar.MONTH] = month-1
        c[Calendar.DAY_OF_MONTH] = day
        val `when` = c.timeInMillis
        if (`when` / 1000 < Int.MAX_VALUE) {
            SystemClock.setCurrentTimeMillis(`when`)

        }
        val now = Calendar.getInstance().timeInMillis
        if (now - `when` > 1000) throw IOException("failed to set Date.")
    }

    @Throws(IOException::class, InterruptedException::class)
    fun setTime(hour: Int, minute: Int) {
        val c = Calendar.getInstance()
        c[Calendar.HOUR_OF_DAY] = hour
        c[Calendar.MINUTE] = minute
        val `when` = c.timeInMillis
        if (`when` / 1000 < Int.MAX_VALUE) {
            SystemClock.setCurrentTimeMillis(`when`)
        }
        val now = Calendar.getInstance().timeInMillis
        if (now - `when` > 1000) throw IOException("failed to set Time.")
    }
}