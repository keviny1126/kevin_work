package com.newchip.tool.leaktest.ui

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.cnlaunch.physics.impl.IPhysics
import com.cnlaunch.physics.serialport.SerialPortManager
import com.cnlaunch.physics.serialport.util.AnalysisLeakData
import com.google.gson.Gson
import com.jeremyliao.liveeventbus.LiveEventBus
import com.newchip.tool.leaktest.R
import com.newchip.tool.leaktest.ui.setting.SettingViewModel
import com.newchip.tool.leaktest.utils.AnalysisTools
import com.newchip.tool.leaktest.utils.DeviceConnectUtils
import com.newchip.tool.leaktest.utils.ForwardUtils
import com.newchip.tool.leaktest.utils.LeakSerialOrderUtils
import com.newchip.tool.leaktest.utils.NetworkUtil
import com.power.baseproject.bean.BackOrderInfo
import com.power.baseproject.bean.ClientAppInfo
import com.power.baseproject.bean.ConfigLeakInfoBean
import com.power.baseproject.bean.DeviceInfoBean
import com.power.baseproject.bean.FirmwareInfo
import com.power.baseproject.bean.IpBean
import com.power.baseproject.bean.LeakDataBean
import com.power.baseproject.bean.TestOnOrOffResult
import com.power.baseproject.db.DataRepository
import com.power.baseproject.db.entity.TestData
import com.power.baseproject.ktbase.model.BaseResponse
import com.power.baseproject.ktbase.model.BaseViewModel
import com.power.baseproject.ktbase.model.DataState
import com.power.baseproject.ktbase.model.MainRepo
import com.power.baseproject.ktbase.model.StateLiveData
import com.power.baseproject.utils.ByteHexHelper
import com.power.baseproject.utils.ConstantsUtils
import com.power.baseproject.utils.DateUtils
import com.power.baseproject.utils.EasyPreferences
import com.power.baseproject.utils.FileUtils
import com.power.baseproject.utils.LiveEventBusConstants
import com.power.baseproject.utils.PathUtils
import com.power.baseproject.utils.Tools
import com.power.baseproject.utils.log.LogUtil
import com.power.insulationtester.utils.bean.DownloadbinProgressData
import com.power.insulationtester.utils.bean.UpdateDownloadbinData
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream

class MainViewModel(val repo: MainRepo) : BaseViewModel() {
    var deviceSerialLiveData = MutableLiveData<BaseResponse<Boolean>>()
    val leakDataLivedata = MutableLiveData<LeakDataBean>()//实时气压值回调
    val testSwitchLivedata = MutableLiveData<TestOnOrOffResult>()//测试启停结果回调
    val exhaustLivedata = MutableLiveData<TestOnOrOffResult>()//测试启停结果回调
    val reInflationLivedata = MutableLiveData<Boolean>()//测试启停结果回调
    var firmwareLastVersion = StateLiveData<List<FirmwareInfo>>()//服务器固件版本回调
    var appLastVersion = StateLiveData<List<FirmwareInfo>>()//服务器固件版本回调
    var downloadLiveData = StateLiveData<String>()//下载固件回调
    var downloadAppLiveData = StateLiveData<String>()
    var appLastVersionForSS = StateLiveData<ClientAppInfo>()
    var checkAppUpdateSS = StateLiveData<ClientAppInfo>()
    var firmwareLastVersionForSS = StateLiveData<ClientAppInfo>()

    var downloadStateLiveData = MutableLiveData<UpdateDownloadbinData>()
    var downloadProgressLiveData = MutableLiveData<DownloadbinProgressData>()
    var deviceInfoLiveData = MutableLiveData<DeviceInfoBean>()
    var stateSwitchLiveData = MutableLiveData<String>()
    var timeZoneLiveData = StateLiveData<IpBean>()
    var needStartUpdate = false

    var heartJob: Job? = null
    var upgrading = false
    var printTempFlag = 0
    val symbols = DecimalFormatSymbols().apply {
        decimalSeparator = '.'
    }

    fun init() {
    }

    private val resistanceDfOne = DecimalFormat("00.00").apply {
        roundingMode = RoundingMode.HALF_UP
        isGroupingUsed = false
        decimalFormatSymbols = symbols
    }

    private val resistanceDfTwo = DecimalFormat("000.0").apply {
        roundingMode = RoundingMode.HALF_UP
        isGroupingUsed = false
        decimalFormatSymbols = symbols
    }

    private val resistanceDfThree = DecimalFormat("0000").apply {
        roundingMode = RoundingMode.HALF_UP
        isGroupingUsed = false
        decimalFormatSymbols = symbols
    }

    private val resistanceDf = DecimalFormat().apply {
        roundingMode = RoundingMode.HALF_UP
        isGroupingUsed = false
        decimalFormatSymbols = symbols
    }

    fun showValue(value: Float, unit: String): String {
        return when (unit) {
            ConstantsUtils.PA -> {
                if (value > 9999) value.toInt().toString() else resistanceDfThree.format(value)
            }

            ConstantsUtils.KPA -> {
                when {
                    value < 1 && value > 0 -> {
                        resistanceDf.maximumFractionDigits = 3
                        resistanceDf.minimumFractionDigits = 3
                        resistanceDf.format(value)
                    }

                    value >= 100 -> resistanceDfTwo.format(value)

                    else -> resistanceDfOne.format(value)
                }
            }

            ConstantsUtils.PSI -> {
                if (value == 0f) {
                    "0.00000"
                } else {
                    resistanceDf.maximumFractionDigits = 5
                    resistanceDf.minimumFractionDigits = 5
                    resistanceDf.format(value)
                }
            }

            else -> resistanceDfOne.format(value)
        }
    }

    fun showTextSize(context: Context, value: String): Float {
        return when (value.replace(".", "").length) {
            in 0..4 -> context.resources.getDimension(R.dimen.sp_28)
            5 -> context.resources.getDimension(R.dimen.sp_24)
            6 -> context.resources.getDimension(R.dimen.sp_20)
            else -> context.resources.getDimension(R.dimen.sp_16)
        }
    }

    fun showValueOne(value: Float): String {
        return resistanceDfOne.format(value)
    }

    fun showValueTwo(value: Float): String {
        return resistanceDfTwo.format(value)
    }

    fun showValueThree(value: Float): String {
        return resistanceDfThree.format(value)
    }

    fun dealCmdOrder(backOrder: String) {
        val receiveOrder = ByteHexHelper.hexStringToBytes(backOrder)
        val receiveData = AnalysisLeakData.instance.analysisOperateData(receiveOrder)
        if (!receiveData.state) {
            return
        }
        if (receiveData.funcType != "10") {
            LogUtil.d(ConstantsUtils.printLog, "<-----MainViewModel接收指令----> :$backOrder")
        }
        when (receiveData.funcType) {
            "01" -> {
                val bean = AnalysisTools.analysisDeviceInfo(receiveData.receiveBuffer)
                if (bean.result) {
                    val info = bean.data
                    LogUtil.e(ConstantsUtils.printLog, "设备信息:$info")
                    deviceInfoLiveData.postValue(info!!)
                }
            }

            "02" -> {//采集卡启动/停止命令
                val bean =
                    AnalysisTools.analysisTestSwitchResult(receiveData.receiveBuffer)
                if (bean.result) {
                    testSwitchLivedata.postValue(bean.data)
                }
            }

            "03" -> {//排气指令
                val bean =
                    AnalysisTools.analysisExhaustResult(receiveData.receiveBuffer)
                if (bean.result) {
                    exhaustLivedata.postValue(bean.data)
                }
            }
            "04" ->{
                //补气指令
                val bean =
                    AnalysisTools.analysisReInflationResult(receiveData.receiveBuffer)
                if (bean.result) {
                    reInflationLivedata.postValue(bean.data)
                }
            }

            "10" -> {
                val bean = AnalysisTools.analysisLeakData(receiveData.receiveBuffer)
                if (bean.result) {
                    leakDataLivedata.postValue(bean.data)
                }
                printTempFlag++
                if (printTempFlag > 9) {
                    LogUtil.d(ConstantsUtils.printLog, "接收指令 :$backOrder")
                    printTempFlag = 0
                }
            }

            "11" -> {
                //心跳包回复
                val bean = AnalysisTools.analysisHeartbeat(receiveData.receiveBuffer)
                if (bean.result) {
                    LogUtil.d("heartLog", "心跳包回复：${bean.data}")
                }
            }

            "12" -> {
                val bean = AnalysisTools.analysisSwitchState(receiveData.receiveBuffer)
                LogUtil.e("kevin", "------------切换状态:$bean")
                if (bean.result) {
                    stateSwitchLiveData.postValue(bean.data)
                    //状态切换应答
                    ackStateSwitch(bean.data)
                }
            }

            "f0" -> {
                receiveData.receiveBuffer?.let { it1 ->
                    if (it1.isNotEmpty()) {
                        when (com.cnlaunch.physics.utils.ByteHexHelper.byteToHexString(
                            it1[0]
                        )) {
                            "01" -> needStartUpdate = true
                            else -> {

                            }
                        }
                    }
                }
            }

            "f3" -> {
                val bean = AnalysisTools.analysisDeviceInfo(receiveData.receiveBuffer)
                if (bean.result) {
                    val info: DeviceInfoBean? = bean.data
                    LogUtil.i(
                        SettingViewModel.TAG,
                        "升级成功，返回固件信息-----info:${info}"
                    )

                    val updateBean = UpdateDownloadbinData(1)
                    updateBean.message = "固件升级成功！"
                    downloadStateLiveData.postValue(updateBean)
                }
            }

            "e0" -> {
                val result = AnalysisTools.analysisSnOrder(receiveData.receiveBuffer)
                LiveEventBus.get(LiveEventBusConstants.SET_SERIAL_NUM_RESULT)
                    .post(result)
            }
        }
    }

    fun analysisDataForReceiver(owner: LifecycleOwner) {
        viewModelScope.launch(Dispatchers.IO) {
            LiveEventBus.get(
                LiveEventBusConstants.SERIAL_PORT_RECEIVER_DATA,
                BackOrderInfo::class.java
            )
                .observe(owner) {
//                    val backOrder = it.cmd
//                    if (ForwardUtils.instance.connected && !ForwardUtils.instance.client) {
//                        //被控端透传指令
//                        ForwardUtils.instance.sendCmd(backOrder)
//                        return@observe
//                    }
                    dealCmdOrder(it.cmd)
                }
        }
    }

    /**
     * 获取设备信息
     */
    fun getDeviceInfo() {
        val mCurrentDevice = DeviceConnectUtils.instance.getCurrentDevice() ?: return
        LeakSerialOrderUtils.getDeviceInfoOrder(mCurrentDevice)
    }

    /**
     * 开启/关闭测试
     */
    fun testSwitch(testState: Boolean, infoBean: ConfigLeakInfoBean) {
        val mCurrentDevice = DeviceConnectUtils.instance.getCurrentDevice() ?: return
        LeakSerialOrderUtils.testOnOrOffOrder(mCurrentDevice, testState, infoBean)
    }

    fun ackStateSwitch(testState: String?) {
        val mCurrentDevice = DeviceConnectUtils.instance.getCurrentDevice() ?: return
        if (testState == null) {
            return
        }
        LeakSerialOrderUtils.ackStateSwitch(mCurrentDevice, testState)
    }

    fun sendSerialNo(serialNo: String) {
        val mCurrentDevice = DeviceConnectUtils.instance.getCurrentDevice() ?: return

        LeakSerialOrderUtils.sendSerialNoOrder(mCurrentDevice, serialNo)
    }

    fun sendExhaustOrder(data: String) {
        val mCurrentDevice = DeviceConnectUtils.instance.getCurrentDevice() ?: return
        LeakSerialOrderUtils.sendExhaustOrder(mCurrentDevice, data)
    }
    fun sendReInflationOrder() {
        val mCurrentDevice = DeviceConnectUtils.instance.getCurrentDevice() ?: return
        LeakSerialOrderUtils.sendReInflationOrder(mCurrentDevice)
    }


    fun getConfigData(): ConfigLeakInfoBean {
        val json = EasyPreferences.instance[ConstantsUtils.LEAK_CONFIG_KEY]
        if (json != null && json.isNotEmpty()) {
            return Gson().fromJson(json, ConfigLeakInfoBean::class.java)
        }
        val bean = ConfigLeakInfoBean()
        bean.testMode = 0
        return bean
    }

    fun setConfigData(bean: ConfigLeakInfoBean): Boolean {
        return EasyPreferences.instance.putToJson(ConstantsUtils.LEAK_CONFIG_KEY, bean)
    }

    fun uploadReportList(context: Context, reportList: List<TestData>, userId: String) {
        viewModelScope.launch {
            val sn = EasyPreferences.instance[ConstantsUtils.DEVICE_NUMBER]
            if (reportList.isEmpty()) {
                return@launch
            }
            val uploadFlow: Flow<BaseResponse<TestData>> = flow {
                reportList.forEach {
                    if (it.reportNo.isNullOrEmpty()) {
                        emit(uploadReport(it, userId))
                    }
                }
            }
            uploadFlow.flowOn(Dispatchers.IO).catch { throwable ->
                LogUtil.e("kevin", "throwable:$throwable")
                emit(BaseResponse())
            }.onEach {
                LogUtil.e("kevin", "接口上传结果:${it}")
                if (it.code == 0) {
                    //上传功能，保存流水号
                    val diagNo = it.msg
                    val tData = it.data
                    if (!diagNo.isNullOrEmpty() && tData != null) {
                        tData.reportNo = diagNo
                        if (tData.deviceSN.isNullOrEmpty()) {
                            //旧数据没有sn，上传功能后更新到数据库
                            tData.deviceSN = sn
                        }
                        DataRepository.instance.updateTestData(context, tData)
                    }
                }
            }.collect()

        }
    }
    private suspend fun uploadReport(data: TestData, userId: String): BaseResponse<TestData> {
        val sn = data.deviceSN ?: EasyPreferences.instance[ConstantsUtils.DEVICE_NUMBER]
        if (sn.isNullOrEmpty()) {
            return BaseResponse()
        }
        val curTime = if (data.testTime.isNullOrEmpty()) {
            System.currentTimeMillis() / 1000
        } else {
            DateUtils.getStringToDateTime(
                data.testTime!!,
                DateUtils.DATE_FORMAT2
            ) / 1000
        }
        val diagNo = "$curTime${Tools.getRandomString(6)}"
        val reportType = 3
        return repo.uploadReportData(diagNo, sn, sn, reportType, userId, data)
    }

    fun getUserAndToken(context: Context): String? {
        val launcherUri = Uri.parse("content://com.ss.userinfo.provider/user_info")
        return context.contentResolver.query(launcherUri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                cursor.getString(0)
            } else {
                null
            }
        }
    }
    fun setData(context: Context, bean: TestData) {
        viewModelScope.launch(Dispatchers.IO) {
            DataRepository.instance.saveData(context, bean)
            if (NetworkUtil.isConnected(context)) {
                LiveEventBus.get(LiveEventBusConstants.CHECK_UPLOAD_DATA).post(true)
            }
        }
    }

    fun connectSerialPort(context: Context) {
        val response = BaseResponse<Boolean>()
        response.data = false

        viewModelScope.launch(Dispatchers.IO) {
            if (DeviceConnectUtils.instance.getCurrentDevice() != null) {
                DeviceConnectUtils.instance.closeCurrentDevice()
            }
            val deviceManager: IPhysics? =
                DeviceConnectUtils.instance.createSerialManager(context, "00000")
            if (deviceManager != null) {
                val serialManager =
                    if (deviceManager is SerialPortManager) deviceManager else null
                LogUtil.i(
                    "kevin",
                    "气密仪 串口连接" + if (serialManager != null) "成功" else "失败"
                )
                if (serialManager != null) {
                    response.data = true
                    response.dataState = DataState.STATE_SUCCESS
                    deviceSerialLiveData.postValue(response)
                    return@launch
                }
            }
            response.data = false
            response.dataState = DataState.STATE_FAILED
            deviceSerialLiveData.postValue(response)
        }
    }

    fun getFirmwareVersion() {
        viewModelScope.launch(Dispatchers.IO) {
            repo.getProductLastFirmware(
                1,
                500,
                name = if (ConstantsUtils.IS_TEST) ConstantsUtils.TEST_APP else ConstantsUtils.SS_PRODUCT_NAME,
                stateLiveData = firmwareLastVersion
            )
        }
    }

    fun getAppVersion(stateLiveData: StateLiveData<List<FirmwareInfo>> = appLastVersion) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.getProductLastFirmware(
                1,
                500,
                name = ConstantsUtils.SS_PRODUCT_NAME,
                stateLiveData = stateLiveData
            )
        }
    }

    fun checkAppVersionForSS(
        version: String,
        sn: String,
        stateLiveData: StateLiveData<ClientAppInfo> = appLastVersionForSS
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.checkSoftVersionForSS(
                version,
                ConstantsUtils.EVT501_SERVICE_APP_KEY,
                sn,
                version,
                ConstantsUtils.EVT501_SERVICE_APP_KEY,
                stateLiveData = stateLiveData
            )
        }
    }

    fun checkFirmwareVersionForSS(
        appVersion: String,
        softVersion: String,
        sn: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.checkSoftVersionForSS(
                appVersion,
                ConstantsUtils.EVT501_SERVICE_APP_KEY,
                sn,
                softVersion,
                ConstantsUtils.EVT501_SERVICE_FIRMWARE_KEY,
                stateLiveData = firmwareLastVersionForSS
            )
        }
    }

    /**
     * 开始下载固件
     */
    fun startDownload(url: String?, version: String, progressCallback: (Int) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.downloadFile(
                url,
                PathUtils.getDownloadPath(version) + ConstantsUtils.DOWNLOAD_BIN_FILENAME,
                downloadLiveData
            ) {}
        }
    }

    /**
     * 开始下载APP
     */
    fun startDownloadApp(url: String?, path: String, progressCallback: (Int) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.downloadFile(url, path, downloadAppLiveData) {}
        }
    }

    fun getTimeZoneTwo() {
        val url = "http://ip-api.com/json"
        viewModelScope.launch(Dispatchers.IO) {
            repo.getTimeZoneTwo(url, timeZoneLiveData)
        }
    }

    /**
     * 开始升级固件
     */
    fun startUpdate(mCurrentDevice: IPhysics?, filePath: String?) {
        val bean = UpdateDownloadbinData(0)
        val progressBean = DownloadbinProgressData()
        upgrading = true
        if (filePath.isNullOrEmpty()) {
            bean.stateType = 0
            bean.message = "没有升级文件！"
            downloadStateLiveData.postValue(bean)
            return
        }
        if (mCurrentDevice == null) {
            bean.stateType = 0
            bean.message = "你是沙雕吗，串口都没连接"
            downloadStateLiveData.postValue(bean)
            return
        }
        if (filePath.isEmpty()) {
            bean.stateType = 0
            bean.message = "文件路径为空，他喵的传个参数都能传空"
            downloadStateLiveData.postValue(bean)
            return
        }
        val downloadFile = File(filePath)
        if (!downloadFile.exists()) {
            bean.stateType = 0
            bean.message = "找不到升级文件，没文件升级个锤子"
            downloadStateLiveData.postValue(bean)
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            val checkFileInfo = FileUtils.readUpdateFile(downloadFile)
            if (!checkFileInfo.result) {
                bean.stateType = 0
                bean.message = "眼瞎呀，选的什么文件，赶紧换!"
                downloadStateLiveData.postValue(bean)
                return@launch
            }
            val prepResult =
                LeakSerialOrderUtils.updatePrepFrame(mCurrentDevice, checkFileInfo)

            when (prepResult) {
                "00" -> {
                    LogUtil.i(SettingViewModel.TAG, "指令收到，下位机开始擦除操作")
                    needStartUpdate = false
                    delay(2000)
                }

                "01" -> {
                    needStartUpdate = true
                    LogUtil.i(SettingViewModel.TAG, "成功APP可以下发数据包")
                }

                else -> {
                    val errorMsg = "升级预备帧返回 错误${prepResult}"
                    bean.stateType = 0
                    bean.message = errorMsg
                    downloadStateLiveData.postValue(bean)
                    return@launch
                }
            }
            if (!needStartUpdate) {//是否接收到开始升级标志
                bean.stateType = 0
                bean.message = "未接收到开始升级标志，时间超时"
                downloadStateLiveData.postValue(bean)
                return@launch
            }

            val totalLen = checkFileInfo.size!!//downloadFile.length()
            val readFileName = ConstantsUtils.UPDATE_FIRMWARE_XX_NAME
            var bin: BufferedInputStream? = null
            var fileIs: FileInputStream? = null
            var zipInputStream: ZipInputStream? = null

            try {
                var updateSuccess = false
                val zipFile = ZipFile(downloadFile)
                fileIs = FileInputStream(downloadFile)
                zipInputStream = ZipInputStream(BufferedInputStream(fileIs))
                var ze = zipInputStream.nextEntry
                while (ze != null) {
                    if (!ze.isDirectory) {
                        if (ze.name.contains(readFileName)) {
                            var count: Int
                            var writePos = 0L
                            var counter = 0
                            bin = BufferedInputStream(zipFile.getInputStream(ze))
                            val buff = ByteArray(ConstantsUtils.PACKAGE_SIZE)
                            while (bin.read(buff).also { count = it } > 0) {
                                counter++
                                LogUtil.i(
                                    SettingViewModel.TAG,
                                    "发送第$counter 包, 文件内容数据 count==$count ，剩余字节 (totalLen - writePos).toInt()：${(totalLen - writePos).toInt()}"
                                )
                                val params: ByteArray =
                                    if (count < ConstantsUtils.PACKAGE_SIZE || ((totalLen - writePos).toInt() == count)) // 最后一包数据
                                    {
                                        val rest = ByteArray(count)
                                        System.arraycopy(buff, 0, rest, 0, count)
                                        rest
                                    } else {
                                        buff
                                    }
                                val dataResult = LeakSerialOrderUtils.updateFirmwareBinFrame(
                                    mCurrentDevice,
                                    counter,
                                    com.cnlaunch.physics.utils.ByteHexHelper.bytesToHexString(params)
                                )
                                when (dataResult) {
                                    "01" -> {
                                        writePos += count
                                        updateSuccess = true
                                        progressBean.setProgress(writePos, totalLen)
                                        LogUtil.e(
                                            SettingViewModel.TAG,
                                            "升级过程中，progress:${progressBean.progress}, writePos==$writePos, totalLen :$totalLen "
                                        )
                                        downloadProgressLiveData.postValue(progressBean)
                                    }

                                    "02" -> {
                                        LogUtil.i(
                                            SettingViewModel.TAG,
                                            "升级成功 最后一包数据时候发送"
                                        )
                                        writePos += count
                                        updateSuccess = true
                                        progressBean.setProgress(writePos, totalLen)
                                        downloadProgressLiveData.postValue(progressBean)
                                    }

                                    else -> {
                                        LogUtil.i(
                                            SettingViewModel.TAG,
                                            "升级数据包返回 其他错误：$dataResult"
                                        )
                                        updateSuccess = false
                                        break
                                    }
                                }
                            }
                            bin.close()
                        }
                    }
                    zipInputStream.closeEntry()//读取下一个目录，作为循环条件
                    ze = zipInputStream.nextEntry
                }
                if (updateSuccess) {
                    bean.stateType = 2
                    bean.message = "升级帧发送完成，等待接收升级成功指令！"
                    downloadStateLiveData.postValue(bean)
                } else {
                    bean.stateType = 0
                    bean.message = "升级失败，过程中挂了"
                    downloadStateLiveData.postValue(bean)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                bean.stateType = 0
                bean.message = "升级失败，哪里崩了"
                downloadStateLiveData.postValue(bean)
            } finally {
                LogUtil.i(SettingViewModel.TAG, "升级结束，关闭io流")
                bin?.close()
                zipInputStream?.close()
                fileIs?.close()
            }
        }
    }

    fun sendHeartBeatOrder() {
//        if (!ForwardUtils.instance.client && ForwardUtils.instance.connected) {
//            return
//        }
        val mCurrentDevice = DeviceConnectUtils.instance.getCurrentDevice() ?: return
        LeakSerialOrderUtils.sendHeartBeatOrder(mCurrentDevice)
    }

    fun startHeartJob() {
        viewModelScope.launch(Dispatchers.Main) {
            closeJob()
            delay(10)
            heartJob = startHeartbeatThread()
            heartJob?.start()
        }
    }

    fun closeJob() {
        viewModelScope.launch(Dispatchers.Main) {
            heartJob?.cancel()
            heartJob = null
        }
    }

    private fun startHeartbeatThread(): Job {
        return viewModelScope.launch(start = CoroutineStart.LAZY, context = Dispatchers.IO) {
            try {
                LogUtil.e(
                    "heartLog",
                    "------------------------心跳包线程开始------------------------"
                )
                while (isActive) {
                    if (upgrading) {
                        delay(3000)
                        continue
                    }
                    sendHeartBeatOrder()
                    delay(1500)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                LogUtil.e(
                    "heartLog",
                    "------------------------心跳包线程结束------------------------"
                )
            }
        }
    }

    suspend fun timingClose(time: Int, long: Long = 1000): Boolean {
        var flag = 0
        while (flag < time) {
            flag++
            delay(long)
        }
        return false
    }
}