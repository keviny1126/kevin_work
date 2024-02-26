package com.newchip.tool.leaktest.ui.factory

import android.hardware.usb.UsbDevice
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.cnlaunch.physics.serialport.SerialPortManager
import com.github.mjdev.libaums.fs.UsbFile
import com.jeremyliao.liveeventbus.LiveEventBus
import com.newchip.tool.leaktest.R
import com.newchip.tool.leaktest.databinding.FragmentFactoryBinding
import com.newchip.tool.leaktest.utils.DeviceConnectUtils
import com.newchip.tool.leaktest.utils.LeakSerialOrderUtils
import com.newchip.tool.leaktest.utils.UsbHelper
import com.power.baseproject.common.UsbListener
import com.power.baseproject.ktbase.ui.BaseAppFragment
import com.power.baseproject.utils.CmdControl
import com.power.baseproject.utils.ConstantsUtils
import com.power.baseproject.utils.FileUtils
import com.power.baseproject.utils.LiveEventBusConstants
import com.power.baseproject.utils.PathUtils
import com.power.baseproject.utils.Tools
import com.power.baseproject.utils.log.LogUtil
import com.power.baseproject.widget.NToast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FactoryFragment : BaseAppFragment<FragmentFactoryBinding>() {
    private var mCurrentDevice: SerialPortManager? = null
    private var usbHelper: UsbHelper? = null
    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentFactoryBinding.inflate(inflater, container, false)

    override fun initView() {
        binding.tvTitleShow.text = "工厂测试界面"
        mCurrentDevice = DeviceConnectUtils.instance.getCurrentDevice() as SerialPortManager
        if ("host" != CmdControl.readOtgState()) {
            CmdControl.setHost()
        }
        initUsb()
        initObserver()
        initClick()
    }

    override fun createObserver() {
    }

    override fun lazyLoadData() {
    }

    private fun startFishingJoy() {
        if (Tools.checkApkExist(mContext, "com.qqtap.fishingjoy")) {
            launch(Dispatchers.IO) {
                CmdControl.showOrHideStatusBar(false)
            }
            LogUtil.i("kevin", "-------存在捕鱼达人app-----")
            Tools.startAPK(
                mContext,
                "com.qqtap.fishingjoy",
                "com.unity3d.player.UnityPlayerNativeActivity"
            )
            return
        }
        LogUtil.e("kevin", "-------不存在捕鱼达人app--------")
        readUsb()
    }

    private fun initClick() {
        setOnBackClick {
            activity?.finish()
        }

        mVb.btnAgingTest.setOnClickListener {
            findNavController().navigate(FactoryFragmentDirections.actionFactoryFragmentToAgingLeakTestFragment())
//            startFishingJoy()
        }
        mVb.btnStartLauncher.setOnClickListener {
            CmdControl.startLaunch()
        }
        mVb.btnSn.setOnClickListener {
            val serialNo = mVb.edtSerialNum.text.toString()
            if (serialNo.isNotEmpty()) {
                LogUtil.d("kevin", "序列号:$serialNo")
                mCurrentDevice ?: return@setOnClickListener
                LeakSerialOrderUtils.sendSerialNoOrder(mCurrentDevice!!, serialNo)
            }
        }
    }

    private fun initObserver() {
        LiveEventBus.get(LiveEventBusConstants.SET_SERIAL_NUM_RESULT, Boolean::class.java)
            .observe(this) {
                NToast.shortToast(mContext, "序列号写入${if (it) "成功" else "失败"}")
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
                readUsb()
            }

            override fun failedReadUsb(usbDevice: UsbDevice?) {
            }
        })

    }

    private suspend fun installApp(file: UsbFile) {
        val path = PathUtils.getAppSoftPath("agingApk")
        FileUtils.checkPathIsExists(path)
        val toFilePath = path + ConstantsUtils.AGING_APK

        val fileSystem = usbHelper!!.fileSystem
        if (fileSystem != null) {
            val result = FileUtils.saveUsbFolderToLocal(
                file,
                toFilePath,
                fileSystem
            )
            if (result) {
                Tools.installOtherApk(mContext, toFilePath)
            }
        }
    }

    private fun readUsb() {
        launch {
            try {
                val storageDevices = usbHelper?.getDeviceList()
                if (storageDevices.isNullOrEmpty()) {
                    NToast.shortToast(mContext, R.string.usb_cannot)
                    return@launch
                }
                for (storageDevice in storageDevices) {
                    val files = usbHelper?.readDevice(storageDevice)
                    if (files.isNullOrEmpty()) {
                        continue
                    }
                    for (file in files) {
                        LogUtil.i("kevin", "U盘文件列表: ${file.name}")
                        if (file.name == ConstantsUtils.AGING_APK) {
                            withContext(Dispatchers.IO) {
                                installApp(file)
                            }
                            break
                        }
                    }
                    storageDevice.close()
                }
            } catch (e: Exception) {
                LogUtil.i("kevin", "USB 错误: $e")
            }
        }
    }

    override fun onDestroy() {
        usbHelper?.finishUsbHelper()
        super.onDestroy()
    }
}