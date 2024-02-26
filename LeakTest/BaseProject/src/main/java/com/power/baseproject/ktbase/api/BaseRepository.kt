package com.power.baseproject.ktbase.api

import com.power.baseproject.db.entity.TestData
import com.power.baseproject.ktbase.model.BaseResponse
import com.power.baseproject.ktbase.model.DataState
import com.power.baseproject.utils.log.LogUtil
import com.power.baseproject.ktbase.model.StateLiveData

open class BaseRepository() {

    suspend fun <T : Any> executeResp(
        block: suspend () -> BaseResponse<T>,
        stateLiveData: StateLiveData<T>
    ) {

        var baseResponse = BaseResponse<T>()

        try {

            baseResponse.dataState = DataState.STATE_LOADING
            stateLiveData.postValue(baseResponse)
            //开始请求数据
            val invoke = block()
            //将结果复制给baseResp
            baseResponse = invoke
            if (baseResponse.code == 0 || baseResponse.code == 60000
                ||baseResponse.code == 200
                || baseResponse.code in 20010000..20019999
                || baseResponse.code in 20060000..20069999
                || baseResponse.code in 20070000..20079999
                || baseResponse.code in 20040000..20049999) {
                //请求成功，判断数据是否为空，
                //因为数据有多种类型，需要自己设置类型进行判断
                if (baseResponse.data == null
                    || baseResponse.data is List<*>
                    && (baseResponse.data as List<*>).size == 0
                ) {
                    //TODO: 数据为空,结构变化时需要修改判空条件
                    baseResponse.dataState = DataState.STATE_EMPTY
                } else {
                    //请求成功并且数据不为空的情况下，为STATE_SUCCESS
                    baseResponse.dataState = DataState.STATE_SUCCESS
                }
            } else {
                //服务器请求错误
                baseResponse.dataState = DataState.STATE_FAILED
            }

        } catch (e: Exception) {
            //非后台返回错误，捕获到的异常
            baseResponse.dataState = DataState.STATE_ERROR
        } finally {
            stateLiveData.postValue(baseResponse)
        }
    }

    /**
     * 过滤服务器结果，失败抛异常
     * @param block 请求体方法，必须要用suspend关键字修饰
     * @param success 成功回调
     * @param error 失败回调 可不传
     * @param isShowDialog 是否显示加载框
     * @param loadingMessage 加载框提示内容
     */
    suspend fun <T> request(
        block: suspend () -> BaseResponse<T>,
        stateLiveData: StateLiveData<T>,
        isShowLoading: Boolean = true,
        loadingMessage: String = "loading..."
    ) {

        val baseResponse = BaseResponse<T>()
        var response: BaseResponse<T>? = null

        //如果需要弹窗 通知Activity/fragment弹窗
        runCatching {
            if (isShowLoading) {
                baseResponse.dataState = DataState.STATE_LOADING
                stateLiveData.postValue(baseResponse)
            }
            //请求体
            response = block()
        }.onSuccess {
            //网络请求完成 关闭弹窗
            when {
                response == null -> {
                    baseResponse.dataState = DataState.STATE_EMPTY
                }
                response?.code == 0 || response?.code == 60000 -> {
                    baseResponse.data = response?.data
                    baseResponse.code = 0
                    baseResponse.dataState = DataState.STATE_SUCCESS
                }
                response?.code == 200 || response?.code in 20010000..20019999
                        || response?.code in 20060000..20069999
                        || response?.code in 20070000..20079999
                        || response?.code in 20040000..20049999 -> {
                    baseResponse.data = response?.data
                    baseResponse.code = 0
                    baseResponse.dataState = DataState.STATE_SUCCESS
                }
                else -> {
                    baseResponse.code = response?.code ?: -1
                    baseResponse.message = response?.message ?: response?.msg
                    baseResponse.dataState = DataState.STATE_FAILED
                }
            }
            stateLiveData.postValue(baseResponse)

        }.onFailure {
            baseResponse.dataState = DataState.STATE_ERROR
            LogUtil.e("ykw", "接口请求失败错误----------------error:===$it")
            stateLiveData.postValue(baseResponse)
        }
    }

    /**
     * 过滤服务器结果，失败抛异常
     * @param block 请求体方法，必须要用suspend关键字修饰
     * @param success 成功回调
     * @param error 失败回调 可不传
     * @param isShowDialog 是否显示加载框
     * @param loadingMessage 加载框提示内容
     */
    suspend fun <T> requestNotBaseResponse(
        block: suspend () -> T,
        stateLiveData: StateLiveData<T>,
        isShowLoading: Boolean = false,
    ) {

        val baseResponse = BaseResponse<T>()
        var response: T? = null

        //如果需要弹窗 通知Activity/fragment弹窗
        runCatching {
            if (isShowLoading) {
                baseResponse.dataState = DataState.STATE_LOADING
                stateLiveData.postValue(baseResponse)
            }
            //请求体
            response = block()
        }.onSuccess {
            //网络请求完成 关闭弹窗
            when {
                response == null -> {
                    baseResponse.dataState = DataState.STATE_EMPTY
                }
                response != null -> {//结构体不为空就判断请求成功，code情况到具体界面判断
                    baseResponse.dataState = DataState.STATE_SUCCESS
                    baseResponse.data = response
                    baseResponse.code = 0
                }
            }
            stateLiveData.postValue(baseResponse)

        }.onFailure {
            baseResponse.dataState = DataState.STATE_ERROR
            LogUtil.e("ykw", "----------------it:===$it")
            stateLiveData.postValue(baseResponse)
        }
    }

    /**
     * 过滤服务器结果，失败抛异常
     * @param block 请求体方法，必须要用suspend关键字修饰
     * @param success 成功回调
     * @param error 失败回调 可不传
     * @param isShowDialog 是否显示加载框
     * @param loadingMessage 加载框提示内容
     */
    suspend fun requestSsUpload(
        block: suspend () -> BaseResponse<Any>,
        diagNo: String,
        data: TestData
    ): BaseResponse<TestData> {

        val baseResponse = BaseResponse<TestData>()
        var response: BaseResponse<Any>? = null

        //如果需要弹窗 通知Activity/fragment弹窗
        runCatching {
            //请求体
            response = block()
        }.onSuccess {
            //网络请求完成 关闭弹窗
            when {
                response == null -> {
                    baseResponse.dataState = DataState.STATE_EMPTY
                }

                response?.code == 200 -> {
                    baseResponse.data = data
                    baseResponse.code = 0
                    baseResponse.msg = diagNo//上传功能保存流水号
                    baseResponse.dataState = DataState.STATE_SUCCESS
                }

                else -> {
                    baseResponse.code = response?.code ?: -1
                    baseResponse.message = response?.message ?: response?.msg
                    baseResponse.dataState = DataState.STATE_FAILED
                }
            }
        }.onFailure {
            baseResponse.dataState = DataState.STATE_ERROR
            LogUtil.e("kevin", "接口请求失败错误----------------error:===$it")
        }
        return baseResponse
    }
}