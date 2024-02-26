package com.newchip.tool.leaktest.ui.data

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import com.baozi.treerecyclerview.adpater.TreeRecyclerAdapter
import com.baozi.treerecyclerview.adpater.TreeRecyclerType
import com.baozi.treerecyclerview.factory.ItemHelperFactory
import com.github.mjdev.libaums.UsbMassStorageDevice
import com.github.mjdev.libaums.fs.FileSystem
import com.github.mjdev.libaums.fs.UsbFile
import com.github.mjdev.libaums.partition.Partition
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jeremyliao.liveeventbus.LiveEventBus
import com.newchip.tool.leaktest.R
import com.newchip.tool.leaktest.databinding.FragmentDataBinding
import com.newchip.tool.leaktest.ui.data.adapter.DataDateItemParent
import com.newchip.tool.leaktest.ui.data.adapter.DataItem
import com.power.baseproject.bean.DateAndRecordDataBean
import com.power.baseproject.bean.PointerTempDate
import com.power.baseproject.db.entity.TestData
import com.power.baseproject.ktbase.dialog.MessageDialog
import com.power.baseproject.ktbase.ui.BaseAppFragment
import com.power.baseproject.utils.*
import com.power.baseproject.utils.ExcelUtil.initExcel
import com.power.baseproject.utils.ExcelUtil.writeObjListToExcel
import com.power.baseproject.utils.log.LogUtil
import com.power.baseproject.widget.NToast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel


class DataFragment : BaseAppFragment<FragmentDataBinding>() {
    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentDataBinding.inflate(inflater, container, false)

    private val vm: DataViewModel by viewModel()

    private lateinit var treeRecyclerAdapter: TreeRecyclerAdapter
    private var dataList = arrayListOf<DateAndRecordDataBean>()
    private var selectAll = false
    private var messageDialog: MessageDialog? = null
    private val ACTION_USB_PERMISSION = "com.demo.otgusb.USB_PERMISSION"
    private var mUsbManager: UsbManager? = null
    private var mPermissionIntent: PendingIntent? = null

    override fun initView() {
        setTitle(R.string.data_management)
        initRecycleView()
        initClicks()
        init()
        if ("host" != CmdControl.readOtgState()) {
            CmdControl.setHost()
        }
    }

    private fun initRecycleView() {
        treeRecyclerAdapter = TreeRecyclerAdapter(TreeRecyclerType.SHOW_EXPAND)
        mVb.rcvTestData.layoutManager = GridLayoutManager(mContext, 3)
        mVb.rcvTestData.itemAnimator = DefaultItemAnimator()
        mVb.rcvTestData.adapter = treeRecyclerAdapter
    }

    override fun initData() {
        super.initData()
        launch(Dispatchers.Main) {
            val testDataList = vm.getDataList(mContext)
            if (testDataList.isNullOrEmpty()) {
                return@launch
            }
            val list = organizeData(testDataList)
            if (list.isNullOrEmpty()) dataList.clear() else dataList = list
            val parentItems =
                ItemHelperFactory.createItems(dataList, DataDateItemParent::class.java)
            treeRecyclerAdapter.itemManager?.replaceAllItem(parentItems)
            notifySelect()
        }
    }

    private fun organizeData(testDataList: MutableList<TestData>?): ArrayList<DateAndRecordDataBean>? {
        if (testDataList.isNullOrEmpty()) {
            return null
        }
        testDataList.sortByDescending { it.testTime }
        val dataAndRecordList = arrayListOf<DateAndRecordDataBean>()
        val tempDateList = mutableMapOf<String, DateAndRecordDataBean>()
        for ((index, value) in testDataList.withIndex()) {
            if (value.testTime.isNullOrEmpty()) {
                continue
            }
            val dateTime = DateUtils.getDateToString(
                DateUtils.getStringToDateTime(
                    value.testTime!!,
                    DateUtils.DATE_FORMAT2
                ), "yyyy.MM"
            )
            if (!tempDateList.containsKey(dateTime)) {
                tempDateList[dateTime] = DateAndRecordDataBean(dateTime)
            }
            val bean = tempDateList[dateTime]
            bean?.dataRecordList?.add(value)
        }
        for (key in tempDateList.keys) {
            if (tempDateList[key] != null) {
                dataAndRecordList.add(tempDateList[key]!!)
            }
        }
        return dataAndRecordList
    }

    private fun initClicks() {
        setOnBackClick {
            findNavController().popBackStack()
        }
        mVb.tvSelectAll clicks {
            allSelectOrNot()
        }
        mVb.btnDelete clicks {
            deleteSelectedDate()
        }
        mVb.btnSaveUsb clicks {
            saveDate()
        }
        treeRecyclerAdapter.setOnItemClickListener { viewHolder, position ->
            val treeItem = treeRecyclerAdapter.getData(position)
            if (treeItem != null) {
                treeItem.onClick(viewHolder)
                if (treeItem is DataItem) {
                    val action =
                        DataFragmentDirections.actionDataFragmentToDataDetailFragment(treeItem.data as TestData)
                    findNavController().navigate(action)
                }
                notifySelect()
            }
        }
    }

    override fun createObserver() {
        LiveEventBus.get(LiveEventBusConstants.REFRESH_ON_ITEM_CLICK_VIEW).observe(this) {
            notifySelect()
        }
    }

    override fun lazyLoadData() {
    }

    private fun notifySelect() {
        selectAll = true
        for (item in treeRecyclerAdapter.data) {
            if (item is DataDateItemParent) {
                if (!item.isSelect) {
                    selectAll = false
                    continue
                }
                if (!item.isSelectAll) {
                    selectAll = false
                }
            }
        }
        treeRecyclerAdapter.notifyDataSetChanged()
        mVb.tvSelectAll.isSelected = selectAll
    }

    private fun getSelectData(): MutableList<TestData> {
        val dataList = mutableListOf<TestData>()
        for (item in treeRecyclerAdapter.data) {
            if (item is DataDateItemParent) {
                for (childItem in item.selectItems) {
                    val data = childItem.data as TestData
                    dataList.add(data)
                }
            }
        }
        return dataList
    }

    private fun allSelectOrNot() {
        selectAll = !selectAll
        for (item in treeRecyclerAdapter.data) {
            if (item is DataDateItemParent) {
                item.selectAll(selectAll, true)
            }
        }
        treeRecyclerAdapter.notifyDataSetChanged()
        mVb.tvSelectAll.isSelected = selectAll
    }

    override fun needLoaddialog(): Boolean {
        return true
    }

    private fun deleteData(beanList: MutableList<TestData>) {
        launch(Dispatchers.Main) {
            showCenterLoading(R.string.loading_delete)
            val result = vm.deleteData(mContext, beanList)

            val testDataList = vm.getDataList(mContext)
            hideCenterLoading()
            if (result > 0) NToast.shortToast(
                mContext,
                R.string.delete_success
            ) else NToast.shortToast(
                mContext,
                R.string.delete_failed
            )
            val list = organizeData(testDataList)
            if (list.isNullOrEmpty()) dataList.clear() else dataList = list
            treeRecyclerAdapter.itemManager.replaceAllItem(
                ItemHelperFactory.createItems(
                    dataList,
                    DataDateItemParent::class.java
                )
            )
        }
    }

    private fun deleteSelectedDate() {
        val beanList = mutableListOf<TestData>()
        for (item in treeRecyclerAdapter.data) {
            if (item is DataDateItemParent) {
                for (childItem in item.selectItems) {
                    val data = childItem.data as TestData
                    beanList.add(data)
                }
            }
        }
        if (beanList.isNullOrEmpty()) {
            showMessage(getString(R.string.select_delete_data)) {}
            return
        }
        showMessage(getString(R.string.confirm_delete_data, beanList.size), cancelClick = {}) {
            deleteData(beanList)
        }
    }

    fun saveDate() {
        launch {
            try {
                val storageDevices = UsbMassStorageDevice.getMassStorageDevices(mContext)
                if (storageDevices.isNullOrEmpty()) {
                    NToast.shortToast(mContext, R.string.usb_cannot)
                    return@launch
                }
                for (storageDevice in storageDevices) { //一般手机只有一个USB设备
                    // 申请USB权限
                    if (!mUsbManager!!.hasPermission(storageDevice.usbDevice)) {
                        mUsbManager!!.requestPermission(storageDevice.usbDevice, mPermissionIntent)
                        break
                    }
                    // 初始化
                    storageDevice.init()
                    // 获取分区
                    val partitions: List<Partition> = storageDevice.partitions
                    if (partitions.isEmpty()) {
                        NToast.shortToast(mContext, "U盘获取失败，请检查U盘状态")
                        return@launch
                    }
                    val dataList = getSelectData()
                    if (dataList.isNullOrEmpty()) {
                        showMessage(getString(R.string.select_save_data)) {}
                        return@launch
                    }
                    showCenterLoading(R.string.loading_save)
                    val dirPath = PathUtils.getDeviceDataPath(
                        DateUtils.getDateToString(
                            System.currentTimeMillis(),
                            DateUtils.DATE_FORMAT_FILENAME
                        )
                    )
                    FileUtils.checkPathIsExists(dirPath)

                    val result = withContext(Dispatchers.IO) {
                        saveDateList(dirPath, dataList)
                        // 仅使用第一分区
                        val fileSystem: FileSystem = partitions[0].fileSystem
                        LogUtil.i("kevin", "分区名称: " + fileSystem.volumeLabel)
                        val root: UsbFile = fileSystem.rootDirectory

                        val files = root.listFiles()
                        var needCreateLogcat = true
                        var fileName: UsbFile? = null
                        for (file in files) {
                            LogUtil.i("kevin", "U盘文件列表: " + file.name)
                            if (file.name == ConstantsUtils.USB_DATA_DIRECTORY) {
                                needCreateLogcat = false
                                fileName =
                                    file.createFile("Data_" + System.currentTimeMillis() + ".zip")
                                break
                            }
                        }
                        if (needCreateLogcat) {
                            val newDir = root.createDirectory(ConstantsUtils.USB_DATA_DIRECTORY)
                            fileName =
                                newDir.createFile("Data_" + System.currentTimeMillis() + ".zip")
                        }
                        FileUtils.ZipUsbFolder(dirPath, fileName!!, fileSystem)
                    }

                    hideCenterLoading()
                    storageDevice.close()
                    if (result) {
                        showMessage(getString(R.string.save_usb_success)) {}
                        FileUtils.deleteDirectory(dirPath)
                    } else showMessage(getString(R.string.save_usb_failed)) {}
                }
            } catch (e: Exception) {
                LogUtil.i("kevin", "错误: $e")
            }
        }

    }

    private suspend fun saveDateList(dirPath: String, dataList: MutableList<TestData>) {
        val gson = Gson()
        for ((index, data) in dataList.withIndex()) {
            val dPath = PathUtils.getPath(dirPath, data.workpieceNo + "_" + index)
            FileUtils.checkPathIsExists(dPath)
            //设置Excel第一行表头
            val title = arrayOf("时间", "状态", "测试压力kpa", "泄漏量pa")
            //设置Excel的Sheet名
            val sheetName = "数据列表"
            val filePaths = dPath + "DataList_$index.xls"
            initExcel(filePaths, sheetName, title)
            val pointerJson = data.pointerList
            if (!pointerJson.isNullOrEmpty()) {
                val tempDataList: ArrayList<PointerTempDate> = gson.fromJson(
                    pointerJson,
                    object : TypeToken<List<PointerTempDate>>() {}.type
                )
                writeObjListToExcel(tempDataList, filePaths)
            }
            if (!data.imagePath.isNullOrEmpty()) {
                FileUtils.copyFile(data.imagePath!!, dPath + "image_$index.jpg")
            }
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

    private fun init() {
        //USB管理器
        mUsbManager = mContext.getSystemService(Context.USB_SERVICE) as UsbManager?
        mPermissionIntent =
            PendingIntent.getBroadcast(mContext, 0, Intent(ACTION_USB_PERMISSION), 0)

        //注册广播,监听USB插入和拔出
        val intentFilter = IntentFilter()
        intentFilter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
        intentFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
        intentFilter.addAction(ACTION_USB_PERMISSION)
        mContext.registerReceiver(mUsbReceiver, intentFilter)
    }

    private val mUsbReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            LogUtil.d("kevin", "onReceive: $intent")
            val action = intent.action ?: return
            when (action) {
                ACTION_USB_PERMISSION -> synchronized(this) {
                    if (intent.getBooleanExtra(
                            UsbManager.EXTRA_PERMISSION_GRANTED,
                            false
                        )
                    ) { //允许权限申请
                        saveDate()
                    } else {
                        NToast.shortToast(mContext, "访问USB设备失败")
                    }
                }
                UsbManager.ACTION_USB_DEVICE_ATTACHED -> NToast.shortToast(mContext, R.string.usb_device_insert)
                UsbManager.ACTION_USB_DEVICE_DETACHED -> NToast.shortToast(mContext, R.string.usb_device_extract)
            }
        }
    }

    override fun onDestroyView() {
        mContext.unregisterReceiver(mUsbReceiver)
        super.onDestroyView()
    }
}