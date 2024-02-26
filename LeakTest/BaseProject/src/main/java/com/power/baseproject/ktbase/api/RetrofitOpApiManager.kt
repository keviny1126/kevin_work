package com.power.baseproject.ktbase.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

open class RetrofitOpApiManager {
    companion object {
        private const val BASE_URL = "https://opapi.newsmartsafe.cn"
        private const val BASE_URL_TEST = "http://119.91.117.9:9510"
        private const val CONNECTED_TOME_OUT = 30L
        private const val WRITE_TOME_OUT = 30L
        private const val READE_TOME_OUT = 30L
        val instance: RetrofitOpApiManager by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            RetrofitOpApiManager()
        }
    }

    private var okhttpClient: OkHttpClient
    private var mRetrofit: Retrofit

    init {
        val logger = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC }

        okhttpClient = OkHttpClient.Builder()
            .retryOnConnectionFailure(true)
            .connectTimeout(CONNECTED_TOME_OUT, TimeUnit.SECONDS)
            .readTimeout(READE_TOME_OUT, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TOME_OUT, TimeUnit.SECONDS)
            .addInterceptor(logger)
            .addNetworkInterceptor(RequestIntercept(null))
            .build()

        //注意：如果只是用Paging、Flow不需要LiveDataCallAdapterFactory或CoroutineCallAdapterFactory。
        //其实用了协程后，是不需要添加addCallAdapterFactory的
        mRetrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
//            .baseUrl(BASE_URL_TEST)
            .client(okhttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            //.addCallAdapterFactory(LiveDataCallAdapterFactory())
            .build()

    }

    fun <T> getService(serviceClass: Class<T>): T {
        return mRetrofit.create(serviceClass)
    }
}