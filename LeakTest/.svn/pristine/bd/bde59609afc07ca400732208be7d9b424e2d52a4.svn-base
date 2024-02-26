package com.cnlaunch.physics.serialport.util

import com.cnlaunch.physics.utils.ByteHexHelper

class AnalysisReceiveData {
    //接收的数据区
    var receiveBuffer: ByteArray? = null

    //校验的状态
    var state: Boolean = false

    //接收指令的功能类型
    var funcType: String? = null
    var reserve: ByteArray? = null
    override fun toString(): String {
        return "AnalysisReceiveData(数据区=${ByteHexHelper.bytesToHexString(receiveBuffer)}, 校验状态=$state, 功能类型=$funcType,保留位：${
            ByteHexHelper.bytesToHexString(
                reserve
            )
        })"
    }
}