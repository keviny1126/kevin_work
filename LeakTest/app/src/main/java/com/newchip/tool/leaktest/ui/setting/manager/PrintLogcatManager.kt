package com.newchip.tool.leaktest.ui.setting.manager

import com.power.baseproject.utils.FileUtils
import com.power.baseproject.utils.PathUtils
import com.power.baseproject.utils.log.LogUtil
import kotlinx.coroutines.*
import java.io.*

class PrintLogcatManager : CoroutineScope by CoroutineScope(
    Dispatchers.Default
) {
    private var mPid = 0
    private var PATH_LOGCAT_SERIAL: String
    var saveJob: Job? = null

    init {
        mPid = android.os.Process.myPid()
        val path = PathUtils.getLogcatPath()
        PATH_LOGCAT_SERIAL = path + SERIAL_LOG
        FileUtils.checkPathIsExists(PATH_LOGCAT_SERIAL)
    }

    fun initSaveLogJob(tag: String, fileName: String) {
        launch(Dispatchers.Main) {
            closeLogJob()
            delay(10)
            saveJob = startSaveLog(tag, fileName)
            saveJob?.start()
        }
    }

    fun closeLogJob() {
        launch(Dispatchers.Main) {
            saveJob?.cancel()
            saveJob = null
        }
    }

    private fun startSaveLog(tag: String, fileName: String): Job {
        return launch(start = CoroutineStart.LAZY, context = Dispatchers.IO) {
            //val cmd = "logcat ${tag}:d | grep \"($mPid)\""
            val cmd = arrayOf("logcat", "-s", "adb logcat $tag")
            val logcatProc = Runtime.getRuntime().exec(cmd)
            var out: FileOutputStream? = null
            var mReader: BufferedReader? = null
            try {
                out = FileOutputStream(File(PATH_LOGCAT_SERIAL, "logcat-$fileName.log"), true)
                mReader = BufferedReader(InputStreamReader(logcatProc?.inputStream), 1024)
                var line: String? = null
                while (isActive && mReader.readLine().also { line = it } != null) {
                    if (line.isNullOrEmpty()) {
                        continue
                    }
                    out.write((line + "\n").toByteArray())
                }
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                logcatProc?.destroy()
                try {
                    mReader?.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                try {
                    out?.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    companion object {
        const val SERIAL_LOG = "SerialPortLog"
        val instance: PrintLogcatManager by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            PrintLogcatManager()
        }
    }
}