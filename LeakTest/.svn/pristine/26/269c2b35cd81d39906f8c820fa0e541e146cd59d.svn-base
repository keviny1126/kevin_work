package com.power.baseproject.ktbase.api

import com.power.baseproject.bean.ClientAppInfo
import com.power.baseproject.ktbase.model.BaseResponse
import com.power.baseproject.utils.ApiConfig
import retrofit2.http.*

interface HttpSmartSafeApi {
    /**
     * 1.请求头部信息(*表示必须):
     * app-client:  客户端名(*string)     客户端标识:  APPKEY
    app-version: 客户端版本号(*string) 应用版本:  例如: 1.001.21
    app-lang:    客户端语言(*string)   语言:  zh-CN,en-US (暂时 区分大小写)
     */
    @POST(ApiConfig.CHECK_APP_VERSION)
    @FormUrlEncoded
    suspend fun checkAppVersion(
        @Header("app-client") APPKEY: String,
        @Header("app-version") appVersion: String,
        @Header("app-lang") lang: String,
        @Field("version") version: String,
        @Field("appkey") appkey: String,
        @Field("sn") sn: String? = null
    ): BaseResponse<ClientAppInfo>

}