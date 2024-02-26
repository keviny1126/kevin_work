package com.newchip.tool.leaktest.ui.setting.fragment

import android.hardware.usb.UsbDevice
import android.view.LayoutInflater
import android.view.ViewGroup
import com.github.mjdev.libaums.fs.UsbFile
import com.jeremyliao.liveeventbus.LiveEventBus
import com.newchip.tool.leaktest.R
import com.newchip.tool.leaktest.databinding.FragmentLogBinding
import com.newchip.tool.leaktest.utils.UsbHelper
import com.power.baseproject.common.UsbListener
import com.power.baseproject.ktbase.ui.BaseAppFragment
import com.power.baseproject.utils.*
import com.power.baseproject.utils.log.LogUtil
import com.power.baseproject.widget.NToast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class SettingLogManagementFragment : BaseAppFragment<FragmentLogBinding>() {
    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentLogBinding.inflate(inflater, container, false)

    private var usbHelper: UsbHelper? = null
    override fun initView() {
        LiveEventBus.get(LiveEventBusConstants.SETTING_TITLE_NAME).post(3)
        showOrHideHeadView(false)
        mVb.cbPutlog.isChecked = EasyPreferences.instance[ConstantsUtils.LOG_SHOW_FLAG, false]
        initClick()
        if ("host" != CmdControl.readOtgState()) {
            CmdControl.setHost()
        }

        initUsb()
    }

    private fun initClick() {
        mVb.cbPutlog.setOnCheckedChangeListener { buttonView, isChecked ->
            LogUtil.i("kevin", "------是否保存日志，勾选:$isChecked")
            EasyPreferences.instance.put(ConstantsUtils.LOG_SHOW_FLAG, isChecked)
        }
        mVb.copyLogClick clicks {
            saveLog()
        }
        mVb.deleteLogClick clicks {
            showCenterLoading(R.string.loading_save)
            val result = FileUtils.deleteDirectory(PathUtils.getLogcatPath())
            hideCenterLoading()
            if (result) NToast.shortToast(mContext, R.string.delete_success) else NToast.shortToast(
                mContext,
                R.string.delete_failed
            )
        }
        mVb.sendLogClick clicks {

        }
    }

    override fun needLoaddialog(): Boolean {
        return true
    }

    override fun createObserver() {
    }

    override fun lazyLoadData() {
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
                saveLog()
            }

            override fun failedReadUsb(usbDevice: UsbDevice?) {
            }
        })
    }

    override fun onDestroyView() {
        usbHelper?.finishUsbHelper()
        super.onDestroyView()
    }

    private fun saveLog() {
        launch {
            try {
                val storageDevices = usbHelper?.getDeviceList()
                if (storageDevices.isNullOrEmpty()) {
                    NToast.shortToast(mContext, R.string.usb_cannot)
                    return@launch
                }
                showCenterLoading(R.string.loading_save)
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
                            if (file.name == ConstantsUtils.USB_LOG_DIRECTORY) {
                                needCreateLogcat = false
                                fileName =
                                    file.createFile("Logcat_$time.zip")
                                break
                            }
                        }
                        if (needCreateLogcat) {
                            val newDir = usbHelper?.getCurrentFolder()
                                ?.createDirectory(ConstantsUtils.USB_LOG_DIRECTORY)
                            fileName = newDir?.createFile("Logcat_$time.zip")
                        }
                        val fileSystem = usbHelper!!.fileSystem ?: return@withContext false
                        if (fileName == null) {
                            return@withContext false
                        }
                        FileUtils.ZipUsbFolder(PathUtils.getLogcatPath(), fileName, fileSystem)
                    }
                    storageDevice.close()
                }
                hideCenterLoading()
                if (result) NToast.shortToast(
                    mContext,
                    R.string.save_usb_success
                ) else NToast.shortToast(mContext, R.string.save_usb_failed)
            } catch (e: Exception) {
                LogUtil.i("kevin", "USB 错误: $e")
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = SettingLogManagementFragment()
    }
}