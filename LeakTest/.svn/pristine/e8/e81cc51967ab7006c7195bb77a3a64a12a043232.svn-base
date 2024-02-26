package com.power.baseproject.utils

import android.content.Context
import android.util.Log
import com.power.baseproject.R
import com.power.baseproject.bean.ExcelInfo
import com.power.baseproject.bean.PointerTempDate
import com.power.baseproject.bean.SaveExcelConfigInfo
import jxl.Workbook
import jxl.WorkbookSettings
import jxl.format.Alignment
import jxl.format.Border
import jxl.format.BorderLineStyle
import jxl.format.Colour
import jxl.write.*
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.util.*

object ExcelUtil {
    private var arial14font: WritableFont? = null
    private var arial14format: WritableCellFormat? = null
    private var arial10font: WritableFont? = null
    private var arial10format: WritableCellFormat? = null
    private var arial12font: WritableFont? = null
    private var arial12format: WritableCellFormat? = null
    private const val UTF8_ENCODING = "UTF-8"

    /**
     * 单元格的格式设置 字体大小 颜色 对齐方式、背景颜色等...
     */
    private fun format() {
        try {
            arial14font = WritableFont(WritableFont.ARIAL, 18, WritableFont.BOLD)
            arial14font!!.colour = Colour.LIGHT_BLUE
            arial14format = WritableCellFormat(arial14font)
            arial14format!!.alignment = Alignment.CENTRE
            arial14format!!.setBorder(Border.ALL, BorderLineStyle.THIN)
            arial14format!!.setBackground(Colour.VERY_LIGHT_YELLOW)
            arial10font = WritableFont(WritableFont.ARIAL, 14, WritableFont.BOLD)
            arial10format = WritableCellFormat(arial10font)
            arial10format!!.alignment = Alignment.CENTRE
            arial10format!!.setBorder(Border.ALL, BorderLineStyle.THIN)
            arial10format!!.setBackground(Colour.GRAY_25)
            arial12font = WritableFont(WritableFont.ARIAL, 14)
            arial12format = WritableCellFormat(arial12font)
            //对齐格式
            arial10format!!.alignment = Alignment.CENTRE
            //设置边框
            arial12format!!.setBorder(Border.ALL, BorderLineStyle.THIN)
        } catch (e: WriteException) {
            e.printStackTrace()
        }
    }


    /**
     * 初始化Excel表格
     *
     * @param filePath  存放excel文件的路径（path/demo.xls）
     * @param sheetName Excel表格的表名
     * @param colName   excel中包含的列名（可以有多个）
     */
    fun initExcel(filePath: String, info: MutableList<ExcelInfo>) {
        format()
        var workbook: WritableWorkbook? = null
        try {
            val file = File(filePath)
            if (!file.exists()) {
                file.createNewFile()
            } else {
                return
            }
            workbook = Workbook.createWorkbook(file)
            for (i in info.indices) {
                val bean = info[i]
                val sheet = workbook.createSheet(bean.sheetName, i)
                //创建标题栏
                sheet.addCell(Label(0, 0, filePath, arial14format) as WritableCell)
                for (col in bean.colName!!.indices) {
                    sheet.addCell(Label(col, 0, bean.colName!![col], arial10format))
                }
                //设置行高
                sheet.setRowView(0, 340)
            }
            workbook.write()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            if (workbook != null) {
                try {
                    workbook.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    /**
     * 初始化Excel表格
     *
     * @param filePath  存放excel文件的路径（path/demo.xls）
     * @param sheetName Excel表格的表名
     * @param colName   excel中包含的列名（可以有多个）
     */
    fun initExcel(filePath: String, sheetName: String, colName: Array<String>) {
        format()
        var workbook: WritableWorkbook? = null
        try {
            val file = File(filePath)
            if (!file.exists()) {
                file.createNewFile()
            } else {
                return
            }
            workbook = Workbook.createWorkbook(file)
            //设置表格的名字
            val sheet = workbook.createSheet(sheetName, 0)
            //创建标题栏
            sheet.addCell(Label(0, 0, filePath, arial14format) as WritableCell)
            for (col in colName.indices) {
                sheet.addCell(Label(col, 0, colName[col], arial10format))
            }
            //设置行高
            sheet.setRowView(0, 340)
            workbook.write()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            if (workbook != null) {
                try {
                    workbook.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun writeInfoToExcel(
        context: Context,
        saveData: SaveExcelConfigInfo,
        objList: List<PointerTempDate>?,
        fileName: String
    ) {
        var writebook: WritableWorkbook? = null
        var ins: InputStream? = null
        try {
            val setEncode = WorkbookSettings()
            setEncode.encoding = UTF8_ENCODING
            ins = FileInputStream(File(fileName))
            val workbook = Workbook.getWorkbook(ins)
            writebook = Workbook.createWorkbook(File(fileName), workbook)
            val sheetConfig = writebook.getSheet(0)
            val configInfo = saveData.configInfo
            if (configInfo != null) {
                sheetConfig.addCell(Label(0, 1, context.getString(R.string.leaktest_detection), arial12format))
                sheetConfig.addCell(
                    Label(
                        1,
                        1,
                        if (configInfo.testMode == 0) context.getString(R.string.low_mode) else context.getString(R.string.high_mode),
                        arial12format
                    )
                )
                sheetConfig.addCell(Label(0, 2, context.getString(R.string.workpiece_no), arial12format))
                sheetConfig.addCell(Label(1, 2, configInfo.workpieceNo, arial12format))
                sheetConfig.addCell(Label(0, 3, context.getString(R.string.workpiece_volume), arial12format))
                sheetConfig.addCell(
                    Label(
                        1,
                        3,
                        configInfo.workpieceVolume.toString(),
                        arial12format
                    )
                )
                sheetConfig.addCell(Label(0, 4, context.getString(R.string.test_pressure), arial12format))
                sheetConfig.addCell(Label(1, 4, configInfo.testPressure.toString(), arial12format))
                sheetConfig.addCell(Label(0, 5, context.getString(R.string.upper_pressure_limit), arial12format))
                sheetConfig.addCell(
                    Label(
                        1,
                        5,
                        configInfo.upperPressureLimit.toString(),
                        arial12format
                    )
                )
                sheetConfig.addCell(Label(0, 6, context.getString(R.string.lower_pressure_limit), arial12format))
                sheetConfig.addCell(
                    Label(
                        1,
                        6,
                        configInfo.lowerPressureLimit.toString(),
                        arial12format
                    )
                )

                sheetConfig.addCell(Label(0, 7, context.getString(R.string.leakage_alarm), arial12format))
                sheetConfig.addCell(Label(1, 7, configInfo.leakageAlarm.toString(), arial12format))
                sheetConfig.addCell(Label(0, 8, context.getString(R.string.inflation_time_title), arial12format))
                sheetConfig.addCell(Label(1, 8, configInfo.inflationTime.toString(), arial12format))
                sheetConfig.addCell(Label(0, 9, context.getString(R.string.stabilization_time), arial12format))
                sheetConfig.addCell(
                    Label(
                        1,
                        9,
                        configInfo.stabilizationTime.toString(),
                        arial12format
                    )
                )
                sheetConfig.addCell(Label(0, 10, context.getString(R.string.detection_time), arial12format))
                sheetConfig.addCell(
                    Label(
                        1,
                        10,
                        configInfo.detectionTime.toString(),
                        arial12format
                    )
                )
                sheetConfig.addCell(Label(0, 11, context.getString(R.string.exhaust_time_title), arial12format))
                sheetConfig.addCell(Label(1, 11, configInfo.exhaustTime.toString(), arial12format))
                sheetConfig.addCell(Label(0, 12, context.getString(R.string.test_time_title), arial12format))
                sheetConfig.addCell(Label(1, 12, saveData.testTime, arial12format))
                sheetConfig.addCell(Label(0, 13, context.getString(R.string.test_result_title), arial12format))
                sheetConfig.addCell(Label(1, 13, saveData.testResult, arial12format))
            }
            if (!objList.isNullOrEmpty()) {
                val sheet = writebook.getSheet(1)
                for (j in objList.indices) {
                    val (time, state, pressureValue, leakage) = objList[j]
                    val list = mutableListOf<String>()
                    // MyDataDTO 自定义实体类
                    list.add(time.toString())
                    list.add(state)
                    list.add(pressureValue.toString())
                    list.add(leakage.toString())
                    for (i in list.indices) {
                        sheet.addCell(Label(i, j + 1, list[i], arial12format))
                        if (list[i].length <= 4) {
                            //设置列宽
                            sheet.setColumnView(i, list[i].length + 8)
                        } else {
                            //设置列宽
                            sheet.setColumnView(i, list[i].length + 5)
                        }
                    }
                    //设置行高
                    sheet.setRowView(j + 1, 350)
                }
            }
            writebook.write()
            workbook.close()
           // Log.e("ExcelUtil", "导出Excel成功")
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            if (writebook != null) {
                try {
                    writebook.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            if (ins != null) {
                try {
                    ins.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }

    }

    /**
     * 将制定类型的List写入Excel中
     *
     * @param <T>
     * @param objList  待写入的list
     * @param fileName
     * @param mContext
    </T> */
    fun writeObjListToExcel(objList: List<PointerTempDate>?, fileName: String) {
        if (objList != null && objList.isNotEmpty()) {
            var writebook: WritableWorkbook? = null
            var ins: InputStream? = null
            try {
                val setEncode = WorkbookSettings()
                setEncode.encoding = UTF8_ENCODING
                ins = FileInputStream(File(fileName))
                val workbook = Workbook.getWorkbook(ins)
                writebook = Workbook.createWorkbook(File(fileName), workbook)
                val sheet = writebook.getSheet(0)
                for (j in objList.indices) {
                    val (time, state, pressureValue, leakage) = objList[j]
                    val list = mutableListOf<String>()
                    // MyDataDTO 自定义实体类
                    list.add(time.toString())
                    list.add(state)
                    list.add(pressureValue.toString())
                    list.add(leakage.toString())
                    for (i in list.indices) {
                        sheet.addCell(Label(i, j + 1, list[i], arial12format))
                        if (list[i].length <= 4) {
                            //设置列宽
                            sheet.setColumnView(i, list[i].length + 8)
                        } else {
                            //设置列宽
                            sheet.setColumnView(i, list[i].length + 5)
                        }
                    }
                    //设置行高
                    sheet.setRowView(j + 1, 350)
                }
                writebook.write()
                workbook.close()
             //   Log.e("ExcelUtil", "导出Excel成功")
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                if (writebook != null) {
                    try {
                        writebook.close()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                if (ins != null) {
                    try {
                        ins.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }
}