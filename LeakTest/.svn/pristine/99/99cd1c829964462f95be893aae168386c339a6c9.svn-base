package com.power.baseproject.ktbase.api

import com.power.baseproject.bean.ClientAppInfo
import com.power.baseproject.ktbase.model.BaseResponse
import com.power.baseproject.utils.ApiConfig
import okhttp3.RequestBody
import retrofit2.http.*

interface HttpOpApi {
    @POST(ApiConfig.UPLOAD_REPORT_DATA)
    suspend fun uploadReportData(
        @Body requestBody: RequestBody
    ): BaseResponse<Any>
}