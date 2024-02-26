package com.power.baseproject.bean

import java.io.Serializable

class ClientInfo : Serializable {
//        "clientInfo" : {
//        "isImportant" : 0 ,              // 是否强制更新,0否1是  (前端处理)
//        "version" : "1.000.01",          // 更新版本号
//        "url" : "http://xxxxxxxxx/xx",   // 文件下载地址
//        "size" : "12081990",             // 文件大小(单位:字节;类型:字符串)
//        "remark": "测试2",
//        "name": "1"
//    }
    // "clientInfo" : {}     // 空对象,表示无版本更新

    var isImportant = 0//是否强制更新,0否1是  (前端处理)
    var version: String? = null// 更新版本号
    var url: String? = null//文件下载地址
    var size: String? = null// 文件大小(单位:字节;类型:字符串)
    var remark: String? = null
    var name: String? = null
    override fun toString(): String {
        return "ClientInfo(isImportant=$isImportant, version=$version, url=$url, size=$size, remark=$remark, name=$name)"
    }

}