package com.power.baseproject.bean

import java.io.Serializable

class LeakDataBean : Serializable {
    var currentState: String? =
        null//0x00： 空闲状态 ;0x01： 准备状态;0x02： 充气状态;0x03： 稳定状态;0x04： 检测状态;0x05： 排气状态
    var currentPa = 0//实时气压,单位Pa
    var currentTimeCount = 0
    var valVeState:String = "0"//充气阀门是否打开 0x80充满；0未充满
}