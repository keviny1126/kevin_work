package com.power.baseproject.ktbase.api

import com.power.baseproject.ktbase.api.listener.DownloadListener
import okhttp3.Interceptor
import okhttp3.Response
import java.util.concurrent.Executor

class DownloadIntercept constructor(
    executor: Executor? = null,
    downloadListener: DownloadListener? = null
) : Interceptor {
    private val exTor = executor
    private val listener = downloadListener
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalResponse = chain.proceed(chain.request())
        return originalResponse.newBuilder()
            .body(DownloadResponseBody(originalResponse.body, exTor, listener)).build()
    }
}