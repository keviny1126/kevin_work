package com.power.baseproject.bean

import java.io.Serializable

class FirmwareInfo :Serializable{
//    "lanCode": "cn",
//    "filePath": "http://tool.inewchip.cn/tools/files/《矩阵计算（第4版）》注释与参考文献.pdf",
//    "fileTitle": "123",
//    "fileCode": "122"
    var lanCode:String? = null
    var filePath:String? = null
    var fileTitle:String? = null
    var fileCode:String? = null
    override fun toString(): String {
        return "FirmwareInfo(lanCode=$lanCode, filePath=$filePath, fileTitle=$fileTitle, fileCode=$fileCode)"
    }
}