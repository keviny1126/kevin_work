package com.power.baseproject.utils.log

import android.text.TextUtils
import com.power.baseproject.utils.log.BaseLog.printDefault
import com.power.baseproject.utils.log.FileLog.printFile
import com.power.baseproject.utils.log.JsonLog.printJson
import java.io.File
import java.util.*

/**
 * This is a Log tool，with this you can the following
 *
 *  1. use LogUtil.d(),you could print whether the method execute,and the default tag is current
 * class's name
 *  1. use LogUtil.d(msg),you could print log as before,and you could location the method with a
 * click in Android Studio Logcat
 *  1. use LogUtil.json(),you could print json string with well format automatic
 *
 *
 * @author zhaokaiqiang
 * github https://github.com/ZhaoKaiQiang/KLog
 * 15/11/17 扩展功能，添加对文件的支持
 * 15/11/18 扩展功能，增加对XML的支持，修复BUG
 * 15/12/8  扩展功能，添加对任意参数的支持
 * 15/12/11 扩展功能，增加对无限长字符串支持
 * 16/6/13  扩展功能，添加对自定义全局Tag的支持
 */
object LogUtil {
    @JvmField
    val LINE_SEPARATOR = System.getProperty("line.separator")
    const val NULL_TIPS = "Log with null object"
    private const val DEFAULT_MESSAGE = "execute"
    private const val PARAM = "Param"
    private const val NULL = "null"
    private const val TAG_DEFAULT = "LogUtil"
    private const val SUFFIX = ".java"
    const val JSON_INDENT = 4
    const val V = 0x1
    const val D = 0x2
    const val I = 0x3
    const val W = 0x4
    const val E = 0x5
    const val A = 0x6
    private const val JSON = 0x7
    private const val XML = 0x8
    private const val STACK_TRACE_INDEX = 5
    private val mGlobalTag: String? = null
    private const val mIsGlobalTagEmpty = true
    private var IS_SHOW_LOG = true
    fun init(isShowLog: Boolean) {
        IS_SHOW_LOG = isShowLog
    }

    //    public static void init(boolean isShowLog, @Nullable String tag) {
    //        IS_SHOW_LOG = isShowLog;
    //        mGlobalTag = tag;
    //        mIsGlobalTagEmpty = TextUtils.isEmpty(mGlobalTag);
    //    }
    fun v() {
        printLog(V, null, DEFAULT_MESSAGE)
    }

    fun v(msg: Any?) {
        printLog(V, null, msg!!)
    }

    fun v(tag: String?, vararg objects: Any) {
        printLog(V, tag, *objects)
    }

    fun d() {
        printLog(D, null, DEFAULT_MESSAGE)
    }

    fun d(msg: Any?) {
        printLog(D, null, msg!!)
    }

    fun d(tag: String?, vararg objects: Any) {
        printLog(D, tag, *objects)
    }

    fun i() {
        printLog(I, null, DEFAULT_MESSAGE)
    }

    fun i(msg: Any?) {
        printLog(I, null, msg!!)
    }

    fun i(tag: String?, vararg objects: Any) {
        printLog(I, tag, *objects)
    }

    fun w() {
        printLog(W, null, DEFAULT_MESSAGE)
    }

    fun w(msg: Any?) {
        printLog(W, null, msg!!)
    }

    fun w(tag: String?, vararg objects: Any) {
        printLog(W, tag, *objects)
    }

    fun e() {
        printLog(E, null, DEFAULT_MESSAGE)
    }

    fun e(msg: Any?) {
        printLog(E, null, msg!!)
    }

    fun e(tag: String?, vararg objects: Any) {
        printLog(E, tag, *objects)
    }

    fun a() {
        printLog(A, null, DEFAULT_MESSAGE)
    }

    fun a(msg: Any?) {
        printLog(A, null, msg!!)
    }

    fun a(tag: String?, vararg objects: Any) {
        printLog(A, tag, *objects)
    }

    fun json(jsonFormat: String?) {
        printLog(JSON, null, jsonFormat!!)
    }

    fun json(tag: String?, jsonFormat: String?) {
        printLog(JSON, tag, jsonFormat!!)
    }

    fun xml(xml: String?) {
        printLog(XML, null, xml!!)
    }

    fun xml(tag: String?, xml: String?) {
        printLog(XML, tag, xml!!)
    }

    fun file(targetDirectory: File, msg: Any) {
        printFile(null, targetDirectory, null, msg)
    }

    fun file(tag: String?, targetDirectory: File, msg: Any) {
        printFile(tag, targetDirectory, null, msg)
    }

    fun file(tag: String?, targetDirectory: File, fileName: String?, msg: Any) {
        printFile(tag, targetDirectory, fileName, msg)
    }

    private fun printLog(type: Int, tagStr: String?, vararg objects: Any) {
        if (!IS_SHOW_LOG) {
            return
        }
        val contents = wrapperContent(tagStr, *objects)
        val tag = contents[0]
        val msg = contents[1]
        val headString = contents[2]
        when (type) {
            V, D, I, W, E, A -> printDefault(type, tag!!, headString + msg)
            JSON -> printJson(tag, msg!!, headString!!)
            XML -> XmlLog.printXml(tag, msg, headString!!)
        }
    }

    private fun printFile(
        tagStr: String?,
        targetDirectory: File,
        fileName: String?,
        objectMsg: Any
    ) {
        if (!IS_SHOW_LOG) {
            return
        }
        val contents = wrapperContent(tagStr, objectMsg)
        val tag = contents[0]
        val msg = contents[1]
        val headString = contents[2]
        printFile(tag, targetDirectory, fileName, headString!!, msg!!)
    }

    private fun wrapperContent(tagStr: String?, vararg objects: Any): Array<String?> {
        val stackTrace = Thread.currentThread().stackTrace
        val targetElement = stackTrace[STACK_TRACE_INDEX]
        var className = targetElement.className
        val classNameInfo = className.split("\\.").toTypedArray()
        if (classNameInfo.isNotEmpty()) {
            className = classNameInfo[classNameInfo.size - 1] + SUFFIX
        }
        if (className.contains("$")) {
            className = className.split("\\$").toTypedArray()[0] + SUFFIX
        }
//        val methodName = targetElement.methodName
//        var lineNumber = targetElement.lineNumber
//        if (lineNumber < 0) {
//            lineNumber = 0
//        }
//        val methodNameShort = methodName.substring(0, 1).uppercase(Locale.getDefault()) + methodName.substring(1)
        var tag = tagStr ?: className
        if (mIsGlobalTagEmpty && TextUtils.isEmpty(tag)) {
            tag = TAG_DEFAULT
        } else if (!mIsGlobalTagEmpty) {
            tag = mGlobalTag
        }
        val msg = getObjectsString(*objects)
        val headString = ""//"[ ($className:$lineNumber)#$methodNameShort ] "
        return arrayOf(tag, msg, headString)
    }

    private fun getObjectsString(vararg objects: Any): String {
        return if (objects.size > 1) {
            val stringBuilder = StringBuilder()
            stringBuilder.append("\n")
            for (i in objects.indices) {
                val `object` = objects[i]
                stringBuilder.append(PARAM).append("[")
                    .append(i).append("]").append(" = ")
                    .append(`object`.toString()).append("\n")
            }
            stringBuilder.toString()
        } else {
            val `object` = objects[0]
            `object`.toString() ?: NULL
        }
    }
}