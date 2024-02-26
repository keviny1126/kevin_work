package com.power.baseproject.utils.log

import android.util.Log
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

/**
 * autour: tanwu
 * date: 2017/8/15 12:11
 * description:
 * version:
 * modify by:
 * update: 2017/8/15
 */
object JsonLog {
    @JvmStatic
    fun printJson(tag: String?, msg: String, headString: String) {
        var message: String
        message = try {
            if (msg.startsWith("{")) {
                val jsonObject = JSONObject(msg)
                jsonObject.toString(LogUtil.JSON_INDENT)
            } else if (msg.startsWith("[")) {
                val jsonArray = JSONArray(msg)
                jsonArray.toString(LogUtil.JSON_INDENT)
            } else {
                msg
            }
        } catch (e: JSONException) {
            msg
        }
        Util.printLine(tag, true)
        message = headString + LogUtil.LINE_SEPARATOR + message
        Log.d(tag, message)
        /*   String[] lines = message.split(LogUtil.LINE_SEPARATOR);
        for (String line : lines) {
            Log.d(tag, "║ " + line);
        }*/Util.printLine(tag, false)
    }
}