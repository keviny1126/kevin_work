package com.power.baseproject.ktbase.model

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.power.baseproject.bean.ClientAppInfo
import com.power.baseproject.bean.FirmwareInfo
import com.power.baseproject.bean.IpBean
import com.power.baseproject.bean.ProductInfo
import com.power.baseproject.bean.ProductListBean
import com.power.baseproject.bean.UploadRequest
import com.power.baseproject.db.entity.TestData
import com.power.baseproject.ktbase.api.BaseRepository
import com.power.baseproject.ktbase.api.HttpApi
import com.power.baseproject.ktbase.api.HttpOpApi
import com.power.baseproject.ktbase.api.HttpSmartSafeApi
import com.power.baseproject.utils.log.LogUtil
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import okio.Buffer
import okio.BufferedSource
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.nio.charset.Charset
import java.util.concurrent.Executors

class MainRepo(
    val service: HttpApi,
    val smartSafeApi: HttpSmartSafeApi,
    val opApi: HttpOpApi
) : BaseRepository() {
    /**
     * 获取产品列表
     */
    suspend fun getProductList(
        page: Int,
        size: Int,
        stateLiveData: StateLiveData<ProductListBean<List<ProductInfo>>>
    ) {
        request(
            { service.getProductList(page, size) },
            stateLiveData
        )
    }

    /**
     * 获取固件详情信息
     * id = 1 固件id号 name= 名称 ver=   版本号
     */
    suspend fun getFirmwareInfo(
        id: String,
        name: String,
        ver: String,
        stateLiveData: StateLiveData<FirmwareInfo>
    ) {
        request(
            { service.getFirmwareInfo(id, name, ver) },
            stateLiveData
        )
    }

    /**
     * 列出所有固件
     * 参数 page=1 页面 size  = 500 每页记录条数
     */
    suspend fun getFirmwareListAll(
        page: Int, size: Int,
        stateLiveData: StateLiveData<List<FirmwareInfo>>
    ) {
        request(
            { service.getFirmwareListAll(page, size) },
            stateLiveData
        )
    }

    /**
     * 列出某个产品下的所有固件
     * 参数
     * page=1 页面
     * size  = 500 每页记录条数
     * pid  = 产品id  可选
     * name = 产品名 可选
     * pid与name任意选择一个
     */
    suspend fun getProductFirmwareList(
        page: Int, size: Int, pid: String? = null, name: String? = null,
        stateLiveData: StateLiveData<List<FirmwareInfo>>
    ) {
        request(
            { service.getProductFirmwareList(page, size, pid, name) },
            stateLiveData
        )
    }

    /**
     * 列出某个产品下的最新版本固件
     * 参数
     * page=1 页面
     * size  = 500 每页记录条数
     * pid  = 产品id  可选
     * name = 产品名 可选
     * pid与name任意选择一个
     */
    suspend fun getProductLastFirmware(
        page: Int,
        size: Int,
        pid: String? = null,
        name: String? = null,
        stateLiveData: StateLiveData<List<FirmwareInfo>>
    ) {
        request(
            { service.getProductLastFirmware(page, size, pid, name) },
            stateLiveData
        )
    }

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
    suspend fun getProductFirmwareVersion(
        page: Int,
        size: Int,
        pid: String? = null,
        name: String? = null,
        version: String,
        stateLiveData: StateLiveData<List<FirmwareInfo>>
    ) {
        request(
            { service.getProductFirmwareVersion(page, size, pid, name, version) },
            stateLiveData
        )
    }

    suspend fun checkSoftVersionForSS(
        appVersion: String,
        appkey: String,
        sn: String,
        softVersion: String,
        softKey: String,
        stateLiveData: StateLiveData<ClientAppInfo>
    ) {
        request(
            { smartSafeApi.checkAppVersion(appkey, appVersion, "zh-CN", softVersion, softKey, sn) },
            stateLiveData
        )
    }

    suspend fun getTimeZoneTwo(url: String, liveData: StateLiveData<IpBean>) {
        val baseResponse = BaseResponse<IpBean>()
        var response: IpBean? = null

        runCatching {
            //请求体
            response = service.getTimeGoneTwo(url)
        }.onSuccess {
            //网络请求完成 关闭弹窗
            when {
                response == null -> {
                    baseResponse.dataState = DataState.STATE_EMPTY
                }

                response?.timezone != null -> {
                    baseResponse.data = response
                    baseResponse.code = 0
                    baseResponse.dataState = DataState.STATE_SUCCESS
                }

                else -> {
                    baseResponse.code = -1
                    baseResponse.message = "请求失败"
                    baseResponse.dataState = DataState.STATE_FAILED
                }
            }
            liveData.postValue(baseResponse)

        }.onFailure {
            baseResponse.dataState = DataState.STATE_ERROR
            LogUtil.e("kevin", "接口请求失败错误----------------error:===$it")
            liveData.postValue(baseResponse)
        }
    }

    suspend fun getTimeZone(url: String, liveData: MutableLiveData<String>) {
        var bodyString: String? = null
        runCatching {
            //请求体
            val res = service.getTimeGone(url)
            val responseBody = res.body()
            var charset = Charset.forName("UTF-8")
            val contentType = responseBody?.contentType()
            if (contentType != null) {
                charset = contentType.charset(charset)
            }
            val source: BufferedSource? = responseBody?.source()
            source?.request(Long.MAX_VALUE) // Buffer the entire body.
            val buffer: Buffer? = source?.buffer()
            val clone = buffer?.clone()
            bodyString = clone?.readString(charset)
        }.onSuccess {
            //网络请求完成 关闭弹窗
            when (bodyString) {
                null -> {
                    liveData.postValue(null)
                }

                else -> {
                    liveData.postValue(bodyString)
                }
            }
        }.onFailure {
            LogUtil.e("ykw", "接口请求失败错误----------------error:===$it")
            liveData.postValue(null)
        }
    }

    suspend fun downloadFile(
        url: String?,
        path: String,
        stateLiveData: StateLiveData<String>,
        isShowLoading: Boolean = true,
        progressCallback: (Int) -> Unit
    ) {
        LogUtil.i("ykw", "------下载地址 url:$url ,本地地址path:$path")
        val baseResponse = BaseResponse<String>()
        var res: Response<ResponseBody>? = null
        if (url.isNullOrEmpty()) {
            baseResponse.dataState = DataState.STATE_FAILED
            stateLiveData.postValue(baseResponse)
            return
        }
        runCatching {
            if (isShowLoading) {
                baseResponse.dataState = DataState.STATE_LOADING
                stateLiveData.postValue(baseResponse)
            }
            res = service.downloadFile(url)
        }.onSuccess {
            val responseBody = res?.body()
            val binPath = saveFile(responseBody, path, progressCallback)
            if (binPath.isNotEmpty()) {
                baseResponse.dataState = DataState.STATE_SUCCESS
                baseResponse.data = binPath
                stateLiveData.postValue(baseResponse)
                return
            }
            baseResponse.dataState = DataState.STATE_FAILED
            stateLiveData.postValue(baseResponse)
        }.onFailure {
            LogUtil.e("ykw", "接口请求失败错误----------------error:===$it")
            baseResponse.dataState = DataState.STATE_FAILED
            baseResponse.message = it.toString()
            stateLiveData.postValue(baseResponse)
        }
    }

    fun saveFile(body: ResponseBody?, filePath: String,progressCallback: (Int) -> Unit): String {
        if (body == null) {
            return ""
        }
        var input: InputStream? = null
        try {
            val file = File(filePath)
            if (file.exists()) {
                file.delete()
            }
            file.createNewFile()

            input = body.byteStream()
            val fos = FileOutputStream(filePath)
            fos.use { output ->
                val buffer = ByteArray(4 * 1024) // or other buffer size
                var read: Int
                while (input.read(buffer).also { read = it } != -1) {
                    output.write(buffer, 0, read)
                }
                output.flush()
            }
            return filePath
        } catch (e: Exception) {
            Log.e("saveFile", e.toString())
        } finally {
            input?.close()
        }
        return ""
    }

//    private fun saveFile(
//        body: ResponseBody?,
//        filePath: String,
//        progressCallback: (Int) -> Unit
//    ): String {
//        if (body == null) {
//            return ""
//        }
//        val input: InputStream?
//        try {
//            val file = File(filePath)
//            if (file.exists()) {
//                file.delete()
//            }
//            file.createNewFile()
//
//            input = body.byteStream()
//            val fos = FileOutputStream(filePath)
//            val buffer = ByteArray(4 * 1024) // or other buffer size
//            var totalBytesRead = 0L
//            var bytesRead: Int
//            val executor = Executors.newSingleThreadExecutor()
//
//            executor.execute {
//                try {
//                    while (input.read(buffer).also { bytesRead = it } != -1) {
//                        fos.write(buffer, 0, bytesRead)
//                        totalBytesRead += bytesRead
//                        val progress = (totalBytesRead * 100 / body.contentLength()).toInt()
//                        progressCallback(progress)
//                    }
//                    fos.flush()
//                } catch (e: IOException) {
//                    // 处理保存文件出错的情况
//                } finally {
//                    input.close()
//                    fos.close()
//                    executor.shutdown()
//                }
//            }
//            return filePath
//        } catch (e: Exception) {
//            Log.e("saveFile", e.toString())
//        }
//        return ""
//    }

    suspend fun uploadReportData(
        diagNo: String,
        testNo: String,
        deviceNo: String,
        reportType: Int,
        userid: String,
        data: TestData
    ): BaseResponse<TestData> {
        val gson = Gson()
        val bean = UploadRequest(diagNo, testNo, deviceNo, reportType, userid, data)
        val requestBody = gson.toJson(bean).toRequestBody("application/json".toMediaTypeOrNull())
        return requestSsUpload({
            opApi.uploadReportData(
                requestBody
            )
        }, diagNo, data)
    }
}