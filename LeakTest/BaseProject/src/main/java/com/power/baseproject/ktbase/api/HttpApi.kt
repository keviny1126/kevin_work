package com.power.baseproject.ktbase.api

import com.power.baseproject.bean.*
import com.power.baseproject.ktbase.model.BaseResponse
import com.power.baseproject.utils.ApiConfig
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Streaming
import retrofit2.http.Url

interface HttpApi {

    @Streaming
    @GET
    suspend fun downloadFile(@Url url: String): Response<ResponseBody>

    @GET
    suspend fun getTimeGone(@Url url: String):Response<ResponseBody>

    @GET
    suspend fun getTimeGoneTwo(@Url url: String):IpBean
    /**
     * 获取产品列表
     */
    @GET(ApiConfig.PRODUCT_LIST)
    suspend fun getProductList(
        @Query("page") page: Int,
        @Query("size") size: Int
    ): BaseResponse<ProductListBean<List<ProductInfo>>>

    /**
     * 获取固件详情信息
     * id = 1 固件id号 name= 名称ver=   版本号
     */
    @GET(ApiConfig.FIRMWARE_INFO)
    suspend fun getFirmwareInfo(
        @Query("id") id: String,
        @Query("name") name: String,
        @Query("ver") ver: String
    ): BaseResponse<FirmwareInfo>

    /**
     * 列出所有固件
     * 参数 page=1 页面 size  = 500 每页记录条数
     */
    @GET(ApiConfig.FIRMWARE_LIST_ALL)
    suspend fun getFirmwareListAll(
        @Query("page") page: Int,
        @Query("size") size: Int
    ): BaseResponse<List<FirmwareInfo>>

    /**
     * 列出某个产品下的所有固件
     * 参数
     * page=1 页面
     * size  = 500 每页记录条数
     * pid  = 产品id  可选
     * name = 产品名 可选
     * pid与name任意选择一个
     */
    @GET(ApiConfig.PRODUCT_FIRMWARE_LIST)
    suspend fun getProductFirmwareList(
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Query("pid") pid: String? = null,
        @Query("name") name: String? = null,
    ): BaseResponse<List<FirmwareInfo>>

    /**
     * 列出某个产品下的最新版本固件
     * 参数
     * page=1 页面
     * size  = 500 每页记录条数
     * pid  = 产品id  可选
     * name = 产品名 可选
     * pid与name任意选择一个
     */
    @GET(ApiConfig.PRODUCT_LAST_FIRMWARE)
    suspend fun getProductLastFirmware(
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Query("pid") pid: String? = null,
        @Query("name") name: String? = null,
    ): BaseResponse<List<FirmwareInfo>>

    /**
     * 列出某个产品下的某一个版本下的固件
     * 参数
     * page=1 页面
     * size  = 500 每页记录条数
     * pid  = 产品id  可选
     * name = 产品名 可选
     * pid与name任意选择一个
     * version =4444 固件的版本号
     */
    @GET(ApiConfig.PRODUCT_FIRMWARE_VERSION)
    suspend fun getProductFirmwareVersion(
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Query("pid") pid: String? = null,
        @Query("name") name: String? = null,
        @Query("version") version: String
    ): BaseResponse<List<FirmwareInfo>>
}