package com.power.baseproject.bean

import java.io.Serializable

class ExcelInfo : Serializable {
    var sheetName: String? = null
    var colName: Array<String>? = null
    override fun toString(): String {
        return "ExcelInfo(sheetName=$sheetName, colName=${colName?.contentToString()})"
    }
}