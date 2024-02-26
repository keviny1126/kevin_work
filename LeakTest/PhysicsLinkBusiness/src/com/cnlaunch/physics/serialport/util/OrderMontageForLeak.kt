package com.cnlaunch.physics.serialport.util

import com.cnlaunch.physics.utils.ByteHexHelper
import com.power.baseproject.utils.ConstantsUtils
import com.power.baseproject.utils.log.LogUtil

/**
 * 帧结构	帧头(2B)	命令(2B)	用户数据长度(2B)	保留位(1B)	保留位(1B)	校验值(1B)	用户数据数据(NB)
 * 帧结构	HEADER	CMD	        LEN	          R1	        R2	   CHECKSUM	       DAT[N]
 * HEADER:起始帧头
 * 固定值:0xA5A5
 * CHECKSUM :和校验方法(ADD8)
 * 校验数据：HEADER + CMD + R1 + R2 + LEN + DAT
 */
class OrderMontageForLeak {
    companion object {
        const val TAG = "OrderMontageForInsulation"
        const val beginSign = "A5A5"//HEADER  帧头 2bytes
        val instance: OrderMontageForLeak by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            OrderMontageForLeak()
        }
    }

    var add8Verify: String? = null//CRC校验码
    private var commandWord = ""//命令 2bytes
    private var R1 = ""//保留位1
    private var R2 = ""//保留位2

    fun createRs485InOrder(): ByteArray? {
        val order: ByteArray?
        commandWord = "0000"
        R1 = "00"
        R2 = "00"
        val data = "01"
        val str = combinationOrder(data, commandWord, R1, R2)
        LogUtil.e(ConstantsUtils.printLog, "RS485控制权释放指令 0x0000 发送指令：$str")
        order = ByteHexHelper.hexStringToBytes(str)
        return order
    }

    /**
     * 获取采集卡设备信息命令
     * 用于首次连接和通信中上位机获取信息
     * 请求帧：
     *     帧头	       命令码	    包长	   保留字1	保留字2	检验和	操作信息
     * 0xA5 0xA5	0x01 0x00	0x01 0x00	0x00	0x00	 CS	    0x00
     */
    fun createGetDeviceInfoOrder(): ByteArray? {
        val order: ByteArray?
        commandWord = "0100"
        R1 = "00"
        R2 = "00"
        val data = "00"
        val str = combinationOrder(data, commandWord, R1, R2)
        LogUtil.e(ConstantsUtils.printLog, "获取采集卡设备信息命令 0x0001 发送指令：$str")
        order = ByteHexHelper.hexStringToBytes(str)
        return order
    }

    /**
     * 采集卡启动/停止命令
     * 请求帧：
     *    帧头	        命令码	    包长	    保留字1	保留字2	检验和	操作类型	    设置压力	压力上限	    压力下限	充气时间	稳定时间	     检测时间	      排气时间
     *  0xA5 0xA5	0x02 0x00	0x11 0x00	0x00	0x00	CS	 Handle_Type	Set_Pa	Upper_Pa	Down_Pa	Fill_TM	Stable_TM	Check_TM	Exhaust_TM
     *                                                     1 byte	1 byte	    4 byte	4 byte	    4 byte	1 byte	1 byte	    1 byte	        1 byte
     */
    fun createTestOnOrOffOrder(
        start: Boolean,
        setPa: String = "00000000",
        upperPa: String = "00000000",
        downPa: String = "00000000",
        fillTime: String = "0000",
        stableTime: String = "0000",
        checkTime: String = "0000",
        exhaustTime: String = "0000",
        preparationTime: String = "0A"
    ): ByteArray? {
        val order: ByteArray?
        commandWord = "0200"
        R1 = "00"
        R2 = "00"
        val sb = StringBuffer()
        if (start) sb.append("01") else sb.append("02")
        sb.append(reverseHex(setPa)).append(reverseHex(upperPa)).append(reverseHex(downPa))
            .append(preparationTime).append(reverseHex(fillTime)).append(reverseHex(stableTime))
            .append(reverseHex(checkTime)).append(reverseHex(exhaustTime))
        val str = combinationOrder(sb.toString(), commandWord, R1, R2)
        LogUtil.e(ConstantsUtils.printLog, "采集卡启动/停止命令 0x0002 发送指令：$str")
        order = ByteHexHelper.hexStringToBytes(str)
        return order
    }

    fun createExhaustOrder(data: String): ByteArray? {
        val order: ByteArray?
        commandWord = "0300"
        R1 = "00"
        R2 = "00"
        val str = combinationOrder(data, commandWord, R1, R2)
        LogUtil.e(ConstantsUtils.printLog, "排气指令 0x0003 发送指令：$str")
        order = ByteHexHelper.hexStringToBytes(str)
        return order
    }

    /**
     * Handle_Type(操作类型):
    0x00: 按照之前的设定气压值重启一次充气进程
    0x01: 当前气压值下 加压 Pa气压
    0x02: 当前气压值下 减压 Pa气压
     */
    fun createReInflationOrder(type: String): ByteArray? {
        val order: ByteArray?
        commandWord = "0400"
        R1 = "00"
        R2 = "00"
        val str = combinationOrder(type, commandWord, R1, R2)
        LogUtil.e(ConstantsUtils.printLog, "重新充气 0x0004 发送指令：$str")
        order = ByteHexHelper.hexStringToBytes(str)
        return order
    }

    /**
     * 实时状态包命令 心跳包
     *      帧头	        命令码	包长	        保留字1	保留字2	检验和	    保留信息1	    保留信息2
     * 0xA5 0xA5	0x11 0x00	0x02 0x00	0x00	0x00	  CS          0x00	      0x00
     *                                                      1 byte	      1 byte     1 byte
     * Current_State(状态信息)：
     * 0x00： 空闲状态
     * 0x01： 准备状态
     * 0x02： 充气状态
     * 0x03： 稳定状态
     * 0x04： 检测状态
     * 0x05： 排气状态
     */
    fun createHeartBeatOrder(data1: String = "00", data2: String = "00"): ByteArray? {
        val order: ByteArray?
        commandWord = "1100"
        R1 = "00"
        R2 = "00"
        val data = data1 + data2
        val str = combinationOrder(data, commandWord, R1, R2)
        LogUtil.e(ConstantsUtils.printLog, "实时状态包命令 心跳包 0x0011 发送指令：$str")
        order = ByteHexHelper.hexStringToBytes(str)
        return order
    }

    /**
     * 实时状态包命令 应答
     *      帧头	        命令码	包长	        保留字1	保留字2	检验和	    状态信息
     * 0xA5 0xA5	0x12 0x00	0x01 0x00	0x00	0x00	  CS    Current_State
     *                                                      1 byte	    1 byte
     * Current_State(状态信息)：
     * 0x00： 空闲状态
     * 0x01： 准备状态
     * 0x02： 充气状态
     * 0x03： 稳定状态
     * 0x04： 检测状态
     * 0x05： 排气状态
     */
    fun createStatusAckOrder(data: String): ByteArray? {
        val order: ByteArray?
        commandWord = "1200"
        R1 = "00"
        R2 = "00"
        val str = combinationOrder(data, commandWord, R1, R2)
        LogUtil.e(ConstantsUtils.printLog, "实时状态包命令 应答 0x0012 发送指令：$str")
        order = ByteHexHelper.hexStringToBytes(str)
        return order
    }

    /**
     * 固件升级开始命令
     *      帧头	        命令码	包长	        保留字1	保留字2	检验和	升级包大小	升级包检验和	升级数据总包数	      软件版本	硬件版本
     * 0xA5 0xA5	0xF0 0x00	0x10 0x00	0x00	0x00	CS	Packages_Size	Packages_CS	Total_Package	Soft_Ver	Hardware_Ver
     *                                                     1 byte	4 byte	        4 byte	    4 byte	        3 byte	    1 byte
     * Packages_Size(升级包大小)
     * Packages_CS(升级包检验和)
     * Total_Package(升级数据总包数):单个升级包为 1KB
     * Soft_Ver(软件版本)
     * Hardware_Ver(硬件版本)
     */
    fun updatePrepOrder(
        fileSize: String,
        fileCs: String,
        fileVer: String,
        totalPackage: String,
        hardwareVer: String = "00"
    ): ByteArray? {
        val order: ByteArray?
        commandWord = "F000"
        R1 = "00"
        R2 = "00"
        val data =
            reverseHex(fileSize) + reverseHex(fileCs) + totalPackage + reverseHex(
                fileVer
            ) + hardwareVer

        val str = combinationOrder(data, commandWord, R1, R2)
        LogUtil.e(ConstantsUtils.printLog, "固件升级开始命令 发送指令：$str")
        order = ByteHexHelper.hexStringToBytes(str)
        return order
    }

    /**
     * 固件升级数据包命令
     *      帧头	        命令码	包长	        保留字1	保留字2	检验和	当前包数	        升级包数据
     * 0xA5 0xA5	0xF1 0x00	0x01 0x04	0x00	0x00	CS	    Packages_ID	Packages_Data
     * Packages_ID(当前包数):
     * 1-255
     * Packages_Data(升级包数据):
     * 最大1024byte 最小1byte;
     */
    fun updateDataOrder(packageCount: String, upData: String): ByteArray? {
        val order: ByteArray?
        commandWord = "F100"
        R1 = "00"
        R2 = "00"
        val data = packageCount + upData
        val str = combinationOrder(data, commandWord, R1, R2)
        LogUtil.e(ConstantsUtils.printLog, "固件升级数据包命令 发送指令：$str")
        order = ByteHexHelper.hexStringToBytes(str)
        return order
    }

    fun ackUpdateOrder(data: String): ByteArray? {
        val order: ByteArray?
        commandWord = "F300"
        R1 = "00"
        R2 = "00"
        val str = combinationOrder(data, commandWord, R1, R2)
        LogUtil.e(ConstantsUtils.printLog, "固件升级结果应答 发送指令：$str")
        order = ByteHexHelper.hexStringToBytes(str)
        return order
    }

    fun test(data1: String, data2: String, data3: String): ByteArray {
        val order: ByteArray?
        commandWord = "1080"
        R1 = "00"
        R2 = "00"
        val data = data1 + reverseHex(data2) + reverseHex(data3)
        val str = combinationOrder(data, commandWord, R1, R2)
        LogUtil.e(ConstantsUtils.printLog, "模拟测试数据 发送指令：$str")
        order = ByteHexHelper.hexStringToBytes(str)
        return order
    }

    fun createSerialNumOrder(serialNo: String): ByteArray {
        val order: ByteArray?
        commandWord = "E000"
        R1 = "00"
        R2 = "00"
        val str = combinationOrder(serialNo, commandWord, R1, R2)
        LogUtil.e(ConstantsUtils.printLog, "写入序列号 发送指令：$str")
        order = ByteHexHelper.hexStringToBytes(str)
        return order
    }

    fun createWifiBackOrder(signal: String): ByteArray {
        val order: ByteArray?
        commandWord = "E480"
        R1 = "00"
        R2 = "00"
        val data = "02$signal"
        val str = combinationOrder(data, commandWord, R1, R2)
        LogUtil.e(ConstantsUtils.printLog, "wifi信号响应指令：$str")
        order = ByteHexHelper.hexStringToBytes(str)
        return order
    }

    fun createRtcBackOrder(result: Boolean, time: String): ByteArray {
        val order: ByteArray?
        commandWord = "E580"
        R1 = "00"
        R2 = "00"
        val data = if (result) "02$time" else "00$time"
        val str = combinationOrder(data, commandWord, R1, R2)
        LogUtil.e(ConstantsUtils.printLog, "wifi信号响应指令：$str")
        order = ByteHexHelper.hexStringToBytes(str)
        return order
    }

    fun createToolStatusOrder(signal: String): ByteArray {
        val order: ByteArray?
        commandWord = "D080"
        R1 = "00"
        R2 = "00"
        val data = "02$signal"
        val str = combinationOrder(data, commandWord, R1, R2)
        LogUtil.e(ConstantsUtils.printLog, "wifi信号响应指令：$str")
        order = ByteHexHelper.hexStringToBytes(str)
        return order
    }

    fun createSpeakerAckOrder(): String {
        val order: String
        commandWord = "e680"
        R1 = "00"
        R2 = "00"
        val data = "02"
        val str = combinationOrder(data, commandWord, R1, R2)
        LogUtil.e(ConstantsUtils.printLog, "音响响应指令：$str")
        order = str
        return order
    }

    fun createClearAdcOrder():  ByteArray {
        val order: ByteArray
        commandWord = "2000"
        R1 = "00"
        R2 = "00"
        val str = combinationOrder("01", commandWord, R1, R2)
        LogUtil.e(ConstantsUtils.printLog, "清理背景值命令 发送指令：$str")
        order = ByteHexHelper.hexStringToBytes(str)
        return order
    }

    private fun combinationOrder(
        data: String,
        commandWord: String,
        R1: String,
        R2: String
    ): String {
        val dataLength = packLength(data)
        val checkBytes =
            ByteHexHelper.hexStringToBytes(beginSign + commandWord + dataLength + R1 + R2 + data)
        add8Verify = ByteHexHelper.sumCheck(checkBytes, checkBytes.size)
        return beginSign + commandWord + dataLength + R1 + R2 + add8Verify + data
    }

    /**
     * 根据字符串得到十六进制包长度
     * 高低位切换
     * @param str
     * @return
     */
    private fun packLength(str: String): String {
        var hexLength = Integer.toHexString(str.length / 2) // 十进制转换为十六进制字符串
        var len = hexLength.length
        while (len < 4) {
            hexLength = "0$hexLength"
            len = hexLength.length
        }
        return reverseHex(hexLength)
    }

    public fun reverseHex(hex: String): String {
        val charArray = hex.toCharArray()
        val length = charArray.size
        val times = length / 2
        var c1i = 0
        while (c1i < times) {
            val c2i = c1i + 1
            val c1 = charArray[c1i]
            val c2 = charArray[c2i]
            val c3i = length - c1i - 2
            val c4i = length - c1i - 1
            charArray[c1i] = charArray[c3i]
            charArray[c2i] = charArray[c4i]
            charArray[c3i] = c1
            charArray[c4i] = c2
            c1i += 2
        }
        return String(charArray)
    }
}