package com.power.baseproject.utils.log

import android.util.Log
import java.io.*
import java.util.*

/**
 * autour: tanwu
 * date: 2017/8/15 12:11
 * description:
 * version:
 * modify by:
 * update: 2017/8/15
 */
object FileLog {
    @JvmStatic
    fun printFile(
        tag: String?,
        targetDirectory: File,
        fileName: String?,
        headString: String,
        msg: String
    ) {
        var fileName = fileName
        fileName = fileName ?: FileLog.fileName
        if (save(targetDirectory, fileName, msg)) {
            Log.d(
                tag,
                headString + " save log success ! location is >>>" + targetDirectory.absolutePath + "/" + fileName
            )
        } else {
            Log.e(tag, headString + "save log fails !")
        }
    }

    private fun save(dic: File, fileName: String?, msg: String): Boolean {
        val file = File(dic, fileName)
        try {
            val outputStream: OutputStream = FileOutputStream(file)
            val outputStreamWriter = OutputStreamWriter(outputStream, "UTF-8")
            outputStreamWriter.write(msg)
            outputStreamWriter.flush()
            outputStream.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            return false
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
            return false
        } catch (e: IOException) {
            e.printStackTrace()
            return false
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
        return true
    }

    private val fileName: String
        private get() {
            val random = Random()
            return "KLog_" + java.lang.Long.toString(
                System.currentTimeMillis() + random.nextInt(
                    10000
                )
            ).substring(4) + ".txt"
        }
}