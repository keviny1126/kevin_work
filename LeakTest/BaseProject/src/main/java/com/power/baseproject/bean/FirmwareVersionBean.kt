package com.power.baseproject.bean

import java.io.Serializable

data class FirmwareVersionBean(var result:Boolean):Serializable{
    var curVersion:String? = null
    var downloadPath:String? = null
}
