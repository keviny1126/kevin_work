package com.power.insulationtester.utils.bean

import java.io.Serializable

data class UpdateDownloadbinData(var stateType: Int):Serializable {//stateType 0:false 1:true 2:await
    var message: String? = null
}
