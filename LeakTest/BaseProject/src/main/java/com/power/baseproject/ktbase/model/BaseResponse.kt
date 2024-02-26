package com.power.baseproject.ktbase.model

import java.io.Serializable

class BaseResponse<T> :Serializable {

    var data: T? = null
    var code:Int? = null
    var message: String? = null
    var dataState: DataState? = null
    var time:Long? = null
    var msg:String? = null
    override fun toString(): String {
        return "BaseResponse(data=$data, code=$code, message=$message, dataState=$dataState, time=$time, msg=$msg)"
    }

}

enum class DataState {
    STATE_CREATE,//创建
    STATE_LOADING,//加载中
    STATE_SUCCESS,//成功
    STATE_COMPLETED,//完成
    STATE_EMPTY,//数据为null
    STATE_FAILED,//接口请求成功但是服务器返回error
    STATE_ERROR,//请求失败
    STATE_UNKNOWN//未知
}