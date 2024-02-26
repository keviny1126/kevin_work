package com.newchip.tool.leaktest.ui.data

import android.hardware.usb.UsbDevice
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mjdev.libaums.fs.UsbFile
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jeremyliao.liveeventbus.LiveEventBus
import com.newchip.tool.leaktest.R
import com.newchip.tool.leaktest.databinding.FragmentDataNewBinding
import com.newchip.tool.leaktest.ui.data.adapter.TestDataAdapter
import com.newchip.tool.leaktest.utils.UsbHelper
import com.newchip.tool.leaktest.widget.FactoryService
import com.power.baseproject.bean.ConfigLeakInfoBean
import com.power.baseproject.bean.ExcelInfo
import com.power.baseproject.bean.PointerTempDate
import com.power.baseproject.bean.SaveExcelConfigInfo
import com.power.baseproject.common.UsbListener
import com.power.baseproject.db.entity.TestData
import com.power.baseproject.ktbase.dialog.MessageDialog
import com.power.baseproject.ktbase.ui.BaseAppFragment
import com.power.baseproject.utils.CmdControl
import com.power.baseproject.utils.ConstantsUtils
import com.power.baseproject.utils.DateUtils
import com.power.baseproject.utils.EasyPreferences
import com.power.baseproject.utils.ExcelUtil
import com.power.baseproject.utils.FileUtils
import com.power.baseproject.utils.LiveEventBusConstants
import com.power.baseproject.utils.PathUtils
import com.power.baseproject.utils.Tools
import com.power.baseproject.utils.clicks
import com.power.baseproject.utils.log.LogUtil
import com.power.baseproject.widget.NToast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel

class DataFragmentNew : BaseAppFragment<FragmentDataNewBinding>() {

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentDataNewBinding.inflate(inflater, container, false)

    private val vm: DataViewModel by viewModel()
    private var selectAll = false
    private var messageDialog: MessageDialog? = null
    private var configInfo: ConfigLeakInfoBean? = null
    private var tempDataList: ArrayList<PointerTempDate>? = null
    private var pointerList = mutableMapOf<Float, Float>()
    private var testDataList = mutableListOf<TestData>()
    private var usbHelper: UsbHelper? = null
    private lateinit var mAdapter: TestDataAdapter
    private var isSaveScreen = false
    private var curPressUnit = ConstantsUtils.KPA
    private var curLeakageUnit = ConstantsUtils.PA

    override fun initView() {
        setTitle(R.string.data_management)
        initRecycleView()
        initClicks()
        if ("host" != CmdControl.readOtgState()) {
            CmdControl.setHost()
        }
        initUsb()
        val factoryFlag = EasyPreferences.instance[ConstantsUtils.FACTORY_SERVICE_START_FLAG, true]
        if (factoryFlag) {
            if (Tools.iServiceRunning(mContext, FactoryService::class.java.simpleName)) {
                LiveEventBus.get(LiveEventBusConstants.CLOSE_FACTORY_TOOL).post(3)
                return
            }
            LiveEventBus.get(LiveEventBusConstants.SERVICE_STOP_INIT).post(true)
        }
    }

    private fun initUsb() {
        usbHelper = UsbHelper(mContext, object : UsbListener {
            override fun insertUsb(device_add: UsbDevice) {
                NToast.shortToast(mContext, R.string.usb_device_insert)
            }

            override fun removeUsb(device_remove: UsbDevice) {
                NToast.shortToast(mContext, R.string.usb_device_extract)
            }

            override fun getReadUsbPermission(usbDevice: UsbDevice) {
                LogUtil.d("kevin", "------getReadUsbPermission------usbDevice:$usbDevice")
                if (isSaveScreen) saveScreenToUsb() else saveDate()
            }

            override fun failedReadUsb(usbDevice: UsbDevice?) {
            }
        })
    }

    private fun initRecycleView() {
        mAdapter = TestDataAdapter(mContext, testDataList)
        mVb.rcvDataList.layoutManager = LinearLayoutManager(context)
        mVb.rcvDataList.adapter = mAdapter
    }

    private fun initClicks() {
        setOnTitleLongClick({
            isSaveScreen = true
            saveScreenToUsb()
        })
        setOnBackClick {
            findNavController().popBackStack()
        }

        mVb.btnSelect clicks {
            selectAll = !selectAll
            mAdapter.setSelectAll(selectAll)
            mVb.btnSelect.text =
                if (selectAll) getString(R.string.unselect_all) else getString(R.string.select_all)
        }

        mVb.btnDelete.setOnClickListener {
            val beanList = mAdapter.getSelectData()
            if (beanList.isEmpty()) {
                showMessage(getString(R.string.select_delete_data)) {}
                return@setOnClickListener
            }
            showMessage(getString(R.string.confirm_delete_data, beanList.size), cancelClick = {}) {
                deleteData(beanList)
            }
        }

        mVb.btnSaveUsb clicks {
            isSaveScreen = false
            saveDate()
        }

        mVb.editSearch.doAfterTextChanged { text ->
            if (text.isNullOrEmpty()) {
                initData()
            }
        }

        mVb.btnSearch clicks {
            launch(Dispatchers.Main) {
                val key = mVb.editSearch.text.toString()
                if (key.isNotEmpty()) {
                    val dataList = vm.getSearchKeyDataList(mContext, key)
                    mVb.tvListNum.text = getString(
                        R.string.all_data_num,
                        if (dataList.isNullOrEmpty()) 0 else dataList.size
                    )
                    if (dataList.isNullOrEmpty()) {
                        mAdapter.listDatas.clear()
                        mAdapter.notifyDataSetChanged()
                        return@launch
                    }
                    dataList.sortByDescending { it.testTime }
                    testDataList = dataList
                    mAdapter.listDatas = testDataList
                    mAdapter.notifyDataSetChanged()

                    mAdapter.clickPos = 0
                    showCurrentInfo(testDataList[0])
                    mAdapter.notifyItemChanged(0)//.setSelectData(0, true)
                } else {
                    initData()
                }
            }
        }

        mAdapter.itemClick { vb, i ->
            showCurrentInfo(mAdapter.listDatas[i])
//            mAdapter.setSelectData(i, true)
            mAdapter.notifyDataSetChanged()
        }

        mAdapter.checkSelectListener {
            selectAll = it
            mVb.btnSelect.text =
                if (selectAll) getString(R.string.unselect_all) else getString(R.string.select_all)
        }
    }

    override fun createObserver() {
    }

    override fun initData() {
        super.initData()
        launch(Dispatchers.Main) {
            val dataList = vm.getDataList(mContext)
            mVb.tvListNum.text = getString(
                R.string.all_data_num,
                if (dataList.isNullOrEmpty()) 0 else dataList.size
            )
            if (dataList.isNullOrEmpty()) {
                NToast.shortToast(mContext, R.string.data_is_empty)
                clearInfo()
                return@launch
            }
            dataList.sortByDescending { it.testTime }
            testDataList = dataList
            mAdapter.listDatas = testDataList
            mAdapter.notifyDataSetChanged()

            mAdapter.clickPos = 0
            showCurrentInfo(testDataList[0])
            mAdapter.notifyItemChanged(0)//.setSelectData(0, true)
        }
    }

    private fun deleteData(beanList: MutableList<TestData>) {
        launch(Dispatchers.Main) {
            showCenterLoading(R.string.loading_delete)
            val result = vm.deleteData(mContext, beanList)

            val dataList = vm.getDataList(mContext)
            hideCenterLoading()
            if (result > 0) NToast.shortToast(
                mContext,
                R.string.delete_success
            ) else NToast.shortToast(
                mContext,
                R.string.delete_failed
            )
            if (testDataList.isEmpty()) {
                NToast.shortToast(mContext, R.string.data_is_empty)
                return@launch
            }
            dataList?.sortByDescending { it.testTime }
            if (dataList.isNullOrEmpty()) {
                testDataList.clear()
                clearInfo()
            } else testDataList = dataList
            mVb.tvListNum.text = getString(
                R.string.all_data_num,
                if (testDataList.isNullOrEmpty()) 0 else testDataList.size
            )
            mAdapter.listDatas = testDataList
            mAdapter.notifyDataSetChanged()
        }
    }

    private fun showCurrentInfo(data: TestData) {
        mVb.ppvShowProgress.clearData()
        pointerList.clear()
        configInfo = null

        mVb.tvShowTime.text = data.testTime
        mVb.tvShowPressure.text = data.testPressure
        mVb.tvShowLeakage.text = data.leakage
        val testResult = testResult(data.testResult)
        mVb.tvShowResult.text = if (testResult.isEmpty()) "----" else testResult

        val gson = Gson()
        val configJson = data.configInfo
        if (!configJson.isNullOrEmpty()) {
            configInfo = gson.fromJson(data.configInfo, ConfigLeakInfoBean::class.java)
            if (configInfo == null) {
                return
            }
            curPressUnit = configInfo!!.pressureUnit ?: ConstantsUtils.KPA
            curLeakageUnit = configInfo!!.leakageUnit ?: ConstantsUtils.PA

            mVb.tvPressure.text =
                getString(R.string.test_pressure).replace("kpa", "$curPressUnit", true)
            mVb.tvLeakage.text =
                getString(R.string.leakage_title).replace("pa", "$curLeakageUnit", true)

            mVb.tvShowSn.text = configInfo!!.workpieceNo

            mVb.ppvShowProgress.setPressureUnit(curPressUnit, 70f)
            mVb.ppvShowProgress.setYMaxValue(configInfo!!.upperPressureLimit + configInfo!!.upperPressureLimit / 5)
            mVb.ppvShowProgress.setXMaxValue(
                configInfo!!.inflationTime,
                configInfo!!.stabilizationTime,
                configInfo!!.detectionTime,
                configInfo!!.exhaustTime
            )
            mVb.tvPrepareTime.text = getString(R.string.prepare_time, "${0}S")
            mVb.tvInflationTime.text =
                getString(R.string.inflation_time, "${0}S", "${configInfo?.inflationTime}S")
            mVb.tvVoltageStabilizationTime.text =
                getString(
                    R.string.voltage_stabilization_time,
                    "${0}S",
                    "${configInfo?.stabilizationTime}S"
                )
            mVb.tvLeakTime.text =
                getString(R.string.leak_time, "${0}S", "${configInfo?.detectionTime}S")
            mVb.tvExhaustTime.text =
                getString(R.string.exhaust_time, "${0}S", "${configInfo?.exhaustTime}S")

            val pointerJson = data.pointerList
            if (!pointerJson.isNullOrEmpty()) {
                tempDataList =
                    gson.fromJson(pointerJson, object : TypeToken<List<PointerTempDate>>() {}.type)
                if (tempDataList.isNullOrEmpty()) {
                    return
                }
                var endTime = 0f
                for (bean in tempDataList!!) {
                    pointerList[bean.time] = bean.pressureValue
                    endTime = bean.time
                }
                launch {
                    delay(100)
                    mVb.ppvShowProgress.setAllPointer(pointerList)

                    mVb.tvPrepareTime.text =
                        getString(
                            R.string.prepare_time,
                            if (endTime >= 10) "${10}S" else "${endTime.toInt()}S"
                        )
                    if (endTime > 10) {
                        mVb.tvInflationTime.text =
                            getString(
                                R.string.inflation_time,
                                if (endTime >= 10 + configInfo!!.inflationTime) "${configInfo!!.inflationTime}S" else "${endTime.toInt() - 10}S",
                                "${configInfo?.inflationTime}S"
                            )
                    }
                    if (endTime > 10 + configInfo!!.inflationTime) {
                        mVb.tvVoltageStabilizationTime.text =
                            getString(
                                R.string.voltage_stabilization_time,
                                if (endTime >= 10 + configInfo!!.inflationTime + configInfo!!.stabilizationTime) "${configInfo!!.stabilizationTime}S" else "${endTime.toInt() - 10 - configInfo!!.inflationTime}S",
                                "${configInfo?.stabilizationTime}S"
                            )
                    }
                    if (endTime > 10 + configInfo!!.inflationTime + configInfo!!.stabilizationTime) {
                        mVb.tvLeakTime.text =
                            getString(
                                R.string.leak_time,
                                if (endTime >= 10 + configInfo!!.inflationTime + configInfo!!.stabilizationTime + configInfo!!.detectionTime) "${configInfo!!.detectionTime}S" else "${endTime.toInt() - 10 - configInfo!!.inflationTime - configInfo!!.stabilizationTime}S",
                                "${configInfo?.detectionTime}S"
                            )
                    }
                    if (endTime > 10 + configInfo!!.inflationTime + configInfo!!.stabilizationTime + configInfo!!.detectionTime) {
                        mVb.tvExhaustTime.text =
                            getString(
                                R.string.exhaust_time,
                                if (endTime >= 10 + configInfo!!.inflationTime + configInfo!!.stabilizationTime + configInfo!!.detectionTime + configInfo!!.exhaustTime) "${configInfo!!.exhaustTime}S" else "${endTime.toInt() - 10 - configInfo!!.inflationTime - configInfo!!.stabilizationTime - configInfo!!.detectionTime}S",
                                "${configInfo?.exhaustTime}S"
                            )
                    }
                }
            }
        }
    }

    private fun clearInfo() {
        mVb.ppvShowProgress.clearData()
        pointerList.clear()
        configInfo = null

        mVb.tvShowTime.text = ""
        mVb.tvShowPressure.text = ""
        mVb.tvShowLeakage.text = ""
        mVb.tvShowResult.text = ""
        mVb.tvShowSn.text = ""

        mVb.tvPrepareTime.text = getString(R.string.prepare_time, "${0}S")
        mVb.tvInflationTime.text = getString(R.string.inflation_time, "${0}S", "${0}S")
        mVb.tvVoltageStabilizationTime.text =
            getString(
                R.string.voltage_stabilization_time,
                "${0}S",
                "${0}S"
            )
        mVb.tvLeakTime.text =
            getString(R.string.leak_time, "${0}S", "${0}S")
        mVb.tvExhaustTime.text =
            getString(R.string.exhaust_time, "${0}S", "${0}S")
    }

    override fun lazyLoadData() {
    }

    private suspend fun saveDateList(dirPath: String, dataList: MutableList<TestData>) {
        val gson = Gson()
        for ((index, data) in dataList.withIndex()) {
            val dPath = PathUtils.getPath(dirPath, data.workpieceNo + "_" + index)
            FileUtils.checkPathIsExists(dPath)
            val filePaths = dPath + "DataList_$index.xls"
            val infoList = mutableListOf<ExcelInfo>()
            val configBean = ExcelInfo()
            configBean.sheetName = getString(R.string.config)
            configBean.colName = arrayOf("", "")
            infoList.add(configBean)

            val dataBean = ExcelInfo()
            //设置Excel第一行表头
            dataBean.colName = arrayOf(
                getString(R.string.time),
                getString(R.string.status),
                getString(R.string.test_pressure).replace("kpa", "$curPressUnit", true),
                getString(R.string.leakage_title).replace("pa", "$curLeakageUnit", true)
            )
            //设置Excel的Sheet名
            dataBean.sheetName = getString(R.string.detailed)
            infoList.add(dataBean)
            ExcelUtil.initExcel(filePaths, infoList)

            val configInfo = gson.fromJson(data.configInfo, ConfigLeakInfoBean::class.java)
            val saveData = SaveExcelConfigInfo()
            saveData.configInfo = configInfo
            saveData.testResult = testResult(data.testResult)
            saveData.testTime = data.testTime
            val tempDataList: ArrayList<PointerTempDate> = gson.fromJson(
                data.pointerList,
                object : TypeToken<List<PointerTempDate>>() {}.type
            )

            ExcelUtil.writeInfoToExcel(mContext, saveData, tempDataList, filePaths)

            if (!data.imagePath.isNullOrEmpty()) {
                FileUtils.copyFile(data.imagePath!!, dPath + "image_$index.jpg")
            }
        }
    }

    private fun testResult(testResult: String?): String {
        return when (testResult) {
            "0" -> getString(R.string.qualified)
            "1" -> getString(R.string.inflation_failure)
            "2" -> getString(R.string.leak_failure)
            "3" -> getString(R.string.active_stop_test)
            else -> testResult ?: ""
        }
    }

    private fun showMessage(
        msg: String,
        cancelClick: View.OnClickListener? = null,
        onClickListener: View.OnClickListener
    ) {
        messageDialog?.dismiss()
        messageDialog = null

        messageDialog = MessageDialog(mContext)
        messageDialog?.showMessage(
            msg = msg,
            mCancelClick = cancelClick,
            mConfirmClick = onClickListener
        )
    }

    fun saveDate() {
        launch {
            try {
                val storageDevices = usbHelper?.getDeviceList()
                if (storageDevices.isNullOrEmpty()) {
                    NToast.shortToast(mContext, R.string.usb_cannot)
                    return@launch
                }
                val dataList = mAdapter.getSelectData()
                if (dataList.isNullOrEmpty()) {
                    showMessage(getString(R.string.select_save_data)) {}
                    return@launch
                }
                showCenterLoading(R.string.loading_save)
                val dirPath = withContext(Dispatchers.IO) {
                    val dirPath = PathUtils.getDeviceDataPath(
                        DateUtils.getDateToString(
                            System.currentTimeMillis(),
                            DateUtils.DATE_FORMAT_FILENAME
                        )
                    )
                    FileUtils.checkPathIsExists(dirPath)
                    saveDateList(dirPath, dataList)
                    dirPath
                }
                var result = false
                for (storageDevice in storageDevices) {
                    val files = usbHelper?.readDevice(storageDevice)
                    if (files.isNullOrEmpty()) {
                        continue
                    }
                    result = withContext(Dispatchers.IO) {
                        var needCreateLogcat = true
                        var fileName: UsbFile? = null
                        val time =
                            DateUtils.getDateToString(System.currentTimeMillis(), "yyyyMMddHHmmss")
                        for (file in files) {
                            LogUtil.i("kevin", "U盘文件列表: ${file.name}")
                            if (file.name == ConstantsUtils.USB_DATA_DIRECTORY) {
                                needCreateLogcat = false
                                fileName = file.createFile("Data_$time.zip")
                                break
                            }
                        }
                        if (needCreateLogcat) {
                            val newDir = usbHelper?.getCurrentFolder()
                                ?.createDirectory(ConstantsUtils.USB_DATA_DIRECTORY)
                            fileName = newDir?.createFile("Data_$time.zip")
                        }
                        val fileSystem = usbHelper!!.fileSystem ?: return@withContext false
                        if (fileName == null) {
                            return@withContext false
                        }
                        FileUtils.ZipUsbFolder(dirPath, fileName, fileSystem)
                    }
                    storageDevice.close()
                }
                hideCenterLoading()
                if (result) {
                    showMessage(getString(R.string.save_usb_success)) {}
                    FileUtils.deleteDirectory(dirPath)
                } else showMessage(getString(R.string.save_usb_failed)) {}
            } catch (e: Exception) {
                LogUtil.i("kevin", "USB 错误: $e")
            }
        }
    }

    fun saveScreenToUsb() {
        launch {
            try {
                val storageDevices = usbHelper?.getDeviceList()
                if (storageDevices.isNullOrEmpty()) {
                    NToast.shortToast(mContext, R.string.usb_cannot)
                    return@launch
                }
                var result = false
                for (storageDevice in storageDevices) {
                    val files = usbHelper?.readDevice(storageDevice)
                    if (files.isNullOrEmpty()) {
                        continue
                    }
                    result = withContext(Dispatchers.IO) {
                        var needCreateLogcat = true
                        var fileName: UsbFile? = null
                        val time =
                            DateUtils.getDateToString(System.currentTimeMillis(), "yyyyMMddHHmmss")
                        for (file in files) {
                            LogUtil.i("kevin", "U盘文件列表: " + file.name)
                            if (file.name == ConstantsUtils.USB_PHOTO_DIRECTORY) {
                                needCreateLogcat = false
                                fileName =
                                    file.createFile("Photo_$time.zip")
                                break
                            }
                        }
                        if (needCreateLogcat) {
                            val newDir = usbHelper?.getCurrentFolder()
                                ?.createDirectory(ConstantsUtils.USB_PHOTO_DIRECTORY)
                            fileName = newDir?.createFile("Photo_$time.zip")
                        }
                        val fileSystem = usbHelper!!.fileSystem ?: return@withContext false
                        if (fileName == null) {
                            return@withContext false
                        }
                        FileUtils.ZipUsbFolder(PathUtils.getScreenCapPath(), fileName, fileSystem)
                    }
                    storageDevice.close()
                }
                if (result) NToast.shortToast(
                    mContext,
                    R.string.save_usb_success
                ) else NToast.shortToast(mContext, R.string.save_usb_failed)
            } catch (e: Exception) {
                LogUtil.i("kevin", "USB 错误: $e")
            }
        }
    }

    override fun needLoaddialog(): Boolean {
        return true
    }

    override fun onDestroy() {
        usbHelper?.finishUsbHelper()
        super.onDestroy()
    }
}