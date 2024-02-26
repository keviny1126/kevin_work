package com.power.baseproject.bean

import com.power.baseproject.db.entity.TestData
import java.io.Serializable

data class DateAndRecordDataBean(var dateTime: String) : Serializable {
    var dataRecordList = arrayListOf<TestData>()
}