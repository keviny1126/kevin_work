package com.newchip.tool.leaktest.ui.setting.fragment

import android.hardware.usb.UsbDevice
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import com.jeremyliao.liveeventbus.LiveEventBus
import com.newchip.tool.leaktest.R
import com.newchip.tool.leaktest.databinding.FragmentDevelopMaintenanceBinding
import com.newchip.tool.leaktest.ui.setting.SeniorSettingActivity
import com.newchip.tool.leaktest.ui.setting.SettingViewModel
import com.newchip.tool.leaktest.utils.DeviceConnectUtils
import com.newchip.tool.leaktest.utils.LeakSerialOrderUtils
import com.newchip.tool.leaktest.utils.UsbHelper
import com.power.baseproject.common.UsbListener
import com.power.baseproject.ktbase.dialog.DateTimePickerDialog
import com.power.baseproject.ktbase.dialog.MessageDialog
import com.power.baseproject.ktbase.ui.BaseAppFragment
import com.power.baseproject.utils.CmdControl
import com.power.baseproject.utils.ConstantsUtils
import com.power.baseproject.utils.DateUtils
import com.power.baseproject.utils.EasyPreferences
import com.power.baseproject.utils.FileUtils
import com.power.baseproject.utils.LiveEventBusConstants
import com.power.baseproject.utils.PathUtils
import com.power.baseproject.utils.ProductConstants
import com.power.baseproject.utils.Tools
import com.power.baseproject.utils.clicks
import com.power.baseproject.utils.log.LogUtil
import com.power.baseproject.widget.NToast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class SettingDevelopMaintenanceFragment : BaseAppFragment<FragmentDevelopMaintenanceBinding>() {
    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentDevelopMaintenanceBinding.inflate(inflater, container, false)

    private var messageDialog: MessageDialog? = null
    private var dateDialog: DateTimePickerDialog? = null
    val vm: SettingViewModel by viewModel()
    private var showTime = true
    private var mYear: Int? = null
    private var mMonth: Int? = null
    private var mDay: Int? = null
    private var hour: Int? = null
    private var minute: Int? = null
    private var curAppVersion: String? = null
    private var usbHelper: UsbHelper? = null
    private var updateType = 0

    override fun initView() {
        showOrHideHeadView(false)
        LiveEventBus.get(LiveEventBusConstants.SETTING_TITLE_NAME).post(4)
        mVb.cbFactoryService.isChecked =
            EasyPreferences.instance[ConstantsUtils.FACTORY_SERVICE_START_FLAG, true]
        initClick()
        curAppVersion = Tools.getAppVersionName(mContext)
        mVb.tvAppVersion.text = "V$curAppVersion"
        val firmwareVersion = EasyPreferences.instance[ConstantsUtils.FIRMWARE_VERSION]
        mVb.tvFirmwareVersion.text =
            if (firmwareVersion.isNullOrEmpty()) "" else "V$firmwareVersion"

        val product = EasyPreferences.instance[ConstantsUtils.PRODUCT_NAME]
        if (product == ProductConstants.ET500) {
            mVb.btnCalibration.visibility = View.VISIBLE
            mVb.tvCalibration.visibility = View.VISIBLE
            mVb.tvLine3.visibility = View.VISIBLE
        }
        if ("host" != CmdControl.readOtgState()) {
            CmdControl.setHost()
        }
        initUsb()
    }

    private fun initClick() {
        mVb.btnInputPassword.setOnClickListener {
            val psw = mVb.edittextInputPassword.text.toString()
            if (psw == ConstantsUtils.DEVELOP_PASSWORD_WORD) {
                mVb.clDevelopManage.visibility = View.VISIBLE
                mVb.clInputPassword.visibility = View.GONE
                return@setOnClickListener
            }
            NToast.shortToast(context, R.string.re_input_password)
        }

        mVb.imgDetails clicks {
            showMessage(getString(R.string.local_update_message)) {}
        }

        mVb.imgDetailsFirmware clicks {
            showMessage(getString(R.string.firmware_local_update_message)) {}
        }

        mVb.appLocalUpdate clicks {
            updateType = 1
            readUsb()
        }

        mVb.firmwareLocalUpdate clicks {
            updateType = 2
            readUsb()
        }

        mVb.datePicker clicks {
            dateDialog?.dismiss()
            dateDialog = null

            dateDialog = DateTimePickerDialog(mContext)
            dateDialog?.showDialog(title = R.string.system_time_setting, mConfirmClick = {
                try {
                    DateUtils.setDateTime(mYear!!, mMonth!!, mDay!!, hour!!, minute!!)
                } catch (e: IOException) {
                    NToast.shortToast(mContext, e.toString())
                }
            }, timeListener = { view, hourOfDay, minute ->
                hour = hourOfDay
                this.minute = minute
                LogUtil.i("kevin", "time is :$hourOfDay - $minute")
            }) { view, year, monthOfYear, dayOfMonth ->
                mYear = year
                mMonth = monthOfYear + 1
                mDay = dayOfMonth
                LogUtil.i("kevin", "date is :$year - $monthOfYear - $dayOfMonth")
            }
        }
        mVb.seniorSetting clicks {
            SeniorSettingActivity.actionStart(mContext)
        }
        mVb.btnCalibration clicks {
            val mCurrentDevice = DeviceConnectUtils.instance.getCurrentDevice()
            if (mCurrentDevice != null)
                LeakSerialOrderUtils.clearAdcOrder(mCurrentDevice)
        }
        LiveEventBus.get(LiveEventBusConstants.CLEAR_ADC_RESULT, Boolean::class.java)
            .observe(this) {
                if (lifecycle.currentState != Lifecycle.State.RESUMED) {
                    return@observe
                }
                showToast(if (it) R.string.calibration_success else R.string.calibration_failed)
            }
        mVb.cbFactoryService.setOnCheckedChangeListener { buttonView, isChecked ->
            LogUtil.i("kevin", "------是否开启工厂测试服务，勾选:$isChecked")
            EasyPreferences.instance.put(ConstantsUtils.FACTORY_SERVICE_START_FLAG, isChecked)
        }
    }

    override fun initData() {
        super.initData()
        val ca = Calendar.getInstance()
        mYear = ca.get(Calendar.YEAR)
        mMonth = ca.get(Calendar.MONTH) + 1
        mDay = ca.get(Calendar.DAY_OF_MONTH)
        hour = ca.get(Calendar.HOUR_OF_DAY)
        minute = ca.get(Calendar.MINUTE)
        LogUtil.d(
            "kevin",
            "initData mYear:$mYear -- mMonth:$mMonth -- mDay:$mDay -- mHour:$hour -- minute:$minute"
        )
        showDateTime()
    }

    override fun createObserver() {
    }

    override fun lazyLoadData() {
    }

    private fun showDateTime() {
        showTime = true

        val format = SimpleDateFormat(DateUtils.DATE_FORMAT2, Locale.getDefault())
        launch(Dispatchers.IO) {
            while (showTime) {
                withContext(Dispatchers.Main) {
                    mVb.tvShowDate.text = format.format(Date(System.currentTimeMillis()))
                }
                delay(1000)
            }
        }
    }

    private fun showMessage(msg: String, onClickListener: View.OnClickListener) {
        messageDialog?.dismiss()
        messageDialog = null

        messageDialog = MessageDialog(mContext)
        messageDialog?.showMessage(
            msg = msg,
            confirmBg = R.drawable.selector_common_button,
            mConfirmClick = onClickListener
        )
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
                readUsb()
            }

            override fun failedReadUsb(usbDevice: UsbDevice?) {
                NToast.shortToast(mContext, "访问USB设备失败")
            }
        })
    }

    override fun needLoaddialog(): Boolean {
        return true
    }

    private fun readUsb() {
        launch(Dispatchers.Main) {
            try {
                val storageDevices = usbHelper?.getDeviceList()
                if (storageDevices.isNullOrEmpty()) {
                    NToast.shortToast(mContext, R.string.usb_cannot)
                    return@launch
                }
                var hasApk = false
                for (storageDevice in storageDevices) {
                    val files = usbHelper?.readDevice(storageDevice)
                    if (files.isNullOrEmpty()) {
                        continue
                    }
                    for (file in files) {
                        LogUtil.i("kevin", "U盘文件列表: ${file.name}")
                        when (file.name) {
                            ConstantsUtils.UPDATE_APK -> {
                                if (updateType == 1) {
                                    hasApk = true
//                                    showToast(R.string.app_installing)
                                    showCenterLoading(R.string.app_installing)
                                    val toFilePath = withContext(Dispatchers.IO) {
                                        val path = PathUtils.getAppSoftPath("usbApk")
                                        FileUtils.checkPathIsExists(path)
                                        path + ConstantsUtils.UPDATE_APK
                                    }
                                    val result = withContext(Dispatchers.IO) {
                                        val fileSystem =
                                            usbHelper!!.fileSystem ?: return@withContext false
                                        FileUtils.saveUsbFolderToLocal(
                                            file,
                                            toFilePath,
                                            fileSystem
                                        )
                                    }
                                    //app安装
                                    if (result) {
                                        if (!Tools.installApk(toFilePath)) {
                                            NToast.shortToast(mContext, R.string.install_failed)
                                            hideCenterLoading()
                                        }
                                    } else {
                                        hideCenterLoading()
                                    }

                                    break
                                }
                            }

                            ConstantsUtils.UPDATE_FIRMWARE -> {
                                if (updateType == 2) {
                                    hasApk = true
                                    showToast(R.string.firmware_updating)
                                    val toFilePath = withContext(Dispatchers.IO) {
                                        val path = PathUtils.getAppSoftPath("usbBin")
                                        FileUtils.checkPathIsExists(path)
                                        path + ConstantsUtils.UPDATE_FIRMWARE
                                    }
                                    val result = withContext(Dispatchers.IO) {
                                        val fileSystem =
                                            usbHelper!!.fileSystem ?: return@withContext false
                                        FileUtils.saveUsbFolderToLocal(
                                            file,
                                            toFilePath,
                                            fileSystem
                                        )
                                    }
                                    if (result) {
                                        //固件升级
                                        LiveEventBus.get(LiveEventBusConstants.UPDATE_FIRMWARE)
                                            .post(toFilePath)
                                    }
                                    break
                                }
                            }
                        }
                    }
                    storageDevice.close()
                }
                if (!hasApk) {
                    showToast(R.string.has_not_update_app)
                }
            } catch (e: Exception) {
                LogUtil.i("kevin", "USB 错误: $e")
            }
        }
    }

    override fun onDestroyView() {
        usbHelper?.finishUsbHelper()
        super.onDestroyView()
        showTime = false
    }

    private fun showToast(msg: Int) {
        //在前台才提示
        if (lifecycle.currentState == Lifecycle.State.RESUMED) {
            launch(Dispatchers.Main) {
                NToast.shortToast(mContext, msg)
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = SettingDevelopMaintenanceFragment()
    }
}