package com.power.baseproject.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.io.Serializable

// 该注解代表数据库一张表，tableName为该表名字，不设置则默认类名
// 注解必须有！！tableName可以不设置
@Entity(tableName = "TestData")
data class TestData(
    // 该标签指定该字段作为表的主键, 自增长。注解必须有！！
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null,

    // 该注解设置当前属性在数据库表中的列名和类型，注解可以不设置，不设置默认列名和属性名相同
    @ColumnInfo(name = "workpiece_no", typeAffinity = ColumnInfo.TEXT)
    var workpieceNo: String? = null,//数据显示的名称

    @ColumnInfo(name = "test_time", typeAffinity = ColumnInfo.TEXT)
    var testTime: String? = null,//测试时间

    @ColumnInfo(name = "test_pressure", typeAffinity = ColumnInfo.TEXT)
    var testPressure: String? = null,//测试压力

    @ColumnInfo(name = "leakage", typeAffinity = ColumnInfo.TEXT)
    var leakage: String? = null,//泄露量

    @ColumnInfo(name = "test_result", typeAffinity = ColumnInfo.TEXT)
    var testResult: String? = null,//测试结论

    @ColumnInfo(name = "config_info", typeAffinity = ColumnInfo.TEXT)
    var configInfo: String? = null,//当前测试的配置信息

    @ColumnInfo(name = "pointer_list")
    var pointerList: String? = null,//实时电压点位列表

    @ColumnInfo(name = "image_path", typeAffinity = ColumnInfo.TEXT)
    var imagePath: String? = null,//截图路径

    @ColumnInfo(name = "deviceSN", typeAffinity = ColumnInfo.TEXT)
    var deviceSN: String? = null,//设备序列号

    @ColumnInfo(name = "reportNo", typeAffinity = ColumnInfo.TEXT)
    var reportNo: String? = null,//流水号

    // 该标签用来告诉系统忽略该字段或者方法，顾名思义：不生成列
    @Ignore
    var isChecked: Boolean = false
) : Serializable