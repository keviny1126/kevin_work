package com.power.baseproject.ktbase.api

import android.view.Surface
import com.power.baseproject.ktbase.api.listener.DownloadListener
import com.power.baseproject.utils.log.LogUtil
import okhttp3.MediaType
import okhttp3.ResponseBody
import okio.Buffer
import okio.BufferedSource
import okio.ForwardingSource
import okio.Source
import okio.buffer
import java.util.concurrent.Executor

class DownloadResponseBody constructor(
    responseBody: ResponseBody? = null,
    executor: Executor? = null,
    downloadListener: DownloadListener? = null
) : ResponseBody() {
    private val resBody = responseBody
    private val exTor = executor
    private val listener = downloadListener
    private var bufferedSource: BufferedSource? = null

    override fun contentLength(): Long {
        if (resBody == null){
            return 0
        }
        return resBody.contentLength()
    }

    override fun contentType(): MediaType? {
        return resBody?.contentType()
    }

    override fun source(): BufferedSource {
        if (resBody == null){
            return source()
        }
        if (bufferedSource == null) {
            bufferedSource = source(resBody.source()).buffer()
        }
        return bufferedSource!!
    }

    private fun source(source: Source): Source {
        return object : ForwardingSource(source) {
            var totalBytesRead = 0L
            override fun read(sink: Buffer, byteCount: Long): Long {
                val bytesRead = super.read(sink, byteCount)
                // read() returns the number of bytes read, or -1 if this source is exhausted.
                if (listener != null && resBody != null) {
                    totalBytesRead += if (bytesRead != -1L) bytesRead else 0
                    LogUtil.d(
                        "kevin",
                        "已经下载的：" + totalBytesRead + "共有：" + resBody.contentLength()
                    )
                    val progress = (totalBytesRead * 100 / resBody.contentLength()).toInt()
                    if (exTor != null) {
                        exTor.execute { listener.onProgress(progress) }
                    } else {
                        listener.onProgress(progress)
                    }
                }
                return bytesRead
            }
        }
    }
}