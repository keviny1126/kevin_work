package com.power.baseproject.bean

import java.io.Serializable

class TestOnOrOffResult : Serializable {
    var handleType: String? = null
    var ackResult:String? = null
    var currentPa = 0
    override fun toString(): String {
        return "TestOnOrOffResult(handleType=$handleType, ackResult=$ackResult, currentPa=$currentPa)"
    }
}