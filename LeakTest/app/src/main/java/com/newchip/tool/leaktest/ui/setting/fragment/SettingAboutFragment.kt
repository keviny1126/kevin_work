package com.newchip.tool.leaktest.ui.setting.fragment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import com.cnlaunch.physics.impl.IPhysics
import com.newchip.tool.leaktest.R
import com.newchip.tool.leaktest.databinding.FragmentAboutBinding
import com.newchip.tool.leaktest.ui.MainViewModel
import com.newchip.tool.leaktest.utils.DeviceConnectUtils
import com.newchip.tool.leaktest.utils.NetworkUtil
import com.power.baseproject.bean.ClientAppInfo
import com.power.baseproject.bean.FirmwareInfo
import com.power.baseproject.ktbase.dialog.MessageDialog
import com.power.baseproject.ktbase.model.BaseResponse
import com.power.baseproject.ktbase.model.DataState
import com.power.baseproject.ktbase.ui.BaseAppFragment
import com.power.baseproject.utils.ConstantsUtils
import com.power.baseproject.utils.EasyPreferences
import com.power.baseproject.utils.FileUtils
import com.power.baseproject.utils.PathUtils
import com.power.baseproject.utils.Tools
import com.power.baseproject.utils.log.LogConfig
import com.power.baseproject.utils.log.LogUtil
import com.power.baseproject.widget.NToast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SettingAboutFragment : BaseAppFragment<FragmentAboutBinding>() {
    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentAboutBinding.inflate(inflater, container, false)

    val vm: MainViewModel by activityViewModels()

    var mDeviceIphysics: IPhysics? = null

    //    private var firmDialog: ProgressDialog? = null
//    private var awaitTimeout = false
    private var curFirmwareVersion: String? = null

    private var lastFirmwareVersion: String? = null
    private var lastAppVersion: String? = null
    private lateinit var curAppVersion: String
    var msgDialog: MessageDialog? = null
    var deviceNum: String? = null

    //var deviceSn: String? = null
    override fun initView() {
        showOrHideHeadView(false)
        initClick()
        curAppVersion = Tools.getAppVersionName(mContext)
        mVb.tvShowAppVersion.text = "V$curAppVersion"
        val productName = EasyPreferences.instance[ConstantsUtils.PRODUCT_NAME]
        if (!productName.isNullOrEmpty() && productName == "EVT501") {
            mVb.imgLogo.setBackgroundResource(if (Tools.isChineseLanguage()) R.drawable.about_logo_cn else R.drawable.about_logo_hw)
        }
        mDeviceIphysics = DeviceConnectUtils.instance.getCurrentDevice()
        if (mDeviceIphysics == null) {
            vm.connectSerialPort(mContext)
            return
        }
        vm.getDeviceInfo()
    }

    override fun needLoaddialog(): Boolean {
        return true
    }

    private fun initClick() {
        mVb.checkAppUpdate.setOnClickListener {
            if (!NetworkUtil.isConnected(mContext)) {
                showToast(R.string.common_network_unavailable)
                return@setOnClickListener
            }
            if (deviceNum == null) {
                vm.getDeviceInfo()
                return@setOnClickListener
            }
            if (LogConfig.instance.isDebug) {
                vm.getAppVersion()
                return@setOnClickListener
            }
            vm.checkAppVersionForSS(curAppVersion, deviceNum!!)
        }
        mVb.checkFirmwareUpdate.setOnClickListener {
            if (!NetworkUtil.isConnected(mContext)) {
                showToast(R.string.common_network_unavailable)
                return@setOnClickListener
            }
            if (deviceNum == null) {
                vm.getDeviceInfo()
                return@setOnClickListener
            }
            if (ConstantsUtils.IS_TEST) {
                vm.getFirmwareVersion()
                return@setOnClickListener
            }
            vm.checkFirmwareVersionForSS(curAppVersion, curFirmwareVersion!!, deviceNum!!)
        }

//        mVb.imgLogo.setOnClickListener {
//            val intent = Intent()
//            intent.component = ComponentName(
//                "com.newchip.pxpcbfactoryservice",
//                "com.newchip.pxpcbfactoryservice.FactoryService"
//            )
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                mContext.startForegroundService(intent)
//            } else {
//                mContext.startService(intent)
//            }
//        }
    }

    override fun createObserver() {
        //串口连接结果
        vm.deviceSerialLiveData.observe(this) {
            if (lifecycle.currentState != Lifecycle.State.RESUMED) {
                return@observe
            }
            when (it.dataState) {
                DataState.STATE_LOADING -> {
                }

                DataState.STATE_SUCCESS -> {
                    if (it.data!!) {
                        mDeviceIphysics = DeviceConnectUtils.instance.getCurrentDevice()
                        vm.getDeviceInfo()
                    }
                }

                else -> {
                    showToast(R.string.connecting_serial_failed)
                }
            }
        }
        vm.deviceInfoLiveData.observe(this) {
            if (lifecycle.currentState != Lifecycle.State.RESUMED) {
                return@observe
            }
            val productName = EasyPreferences.instance[ConstantsUtils.PRODUCT_NAME]
            mVb.tvShowType.text = if (productName.isNullOrEmpty()) it.deviceName else productName
            deviceNum = it.deviceNum
            if (deviceNum != null && deviceNum == "988300000001") {
                deviceNum = "806030000100"
            }
            EasyPreferences.instance.put(ConstantsUtils.DEVICE_NUMBER, deviceNum)
//            deviceSn = it.deviceSn
//            if (deviceSn == null || deviceSn!!.isEmpty()) {
//                deviceSn = deviceNum
//            }
            mVb.tvShowNum.text = deviceNum
            //mVb.tvShowSn.text = deviceSn
            curFirmwareVersion = it.softVersion
            mVb.tvShowFirmwareVersion.text = "V$curFirmwareVersion"
            EasyPreferences.instance.put(ConstantsUtils.FIRMWARE_VERSION, curFirmwareVersion)
        }
        vm.appLastVersionForSS.observe(this) {
            if (lifecycle.currentState != Lifecycle.State.RESUMED) {
                return@observe
            }
            checkUpdateSSApp(it)
        }

        vm.appLastVersion.observe(this) {
            if (lifecycle.currentState != Lifecycle.State.RESUMED) {
                return@observe
            }
            checkUpdateApp(it)
        }

        vm.firmwareLastVersionForSS.observe(this) {
            if (lifecycle.currentState != Lifecycle.State.RESUMED) {
                return@observe
            }
            checkUpdateSSFirmware(it)
        }
//        vm.firmwareLastVersion.observe(this) {
//            if (lifecycle.currentState != Lifecycle.State.RESUMED) {
//                return@observe
//            }
//            checkUpdateFirmware(it)
//        }
    }

    private fun checkUpdateFirmware(it: BaseResponse<List<FirmwareInfo>>) {
        LogUtil.d("ykw", "获取固件版本:$it")
        when (it.dataState) {
            DataState.STATE_LOADING -> {
            }

            DataState.STATE_SUCCESS -> {
                val firmwareInfoList = it.data as List<FirmwareInfo>
                for (bean in firmwareInfoList) {
                    if ((if (ConstantsUtils.IS_TEST) ConstantsUtils.TEST_DOWNLOADBIN else ConstantsUtils.SS_FIRMWARE_NAME) == bean.fileTitle) {
                        lastFirmwareVersion = bean.fileCode
                        val downloadPath = bean.filePath
                        val needDownload =
                            FileUtils.checkVersion(lastFirmwareVersion, curFirmwareVersion)
                        LogUtil.d(
                            "kevin",
                            "是否需要升级固件:$needDownload ，downloadPath:$downloadPath ,服务器固件版本：$lastFirmwareVersion ,下位机当前固件版本：$curFirmwareVersion"
                        )
                        if (needDownload) {
                            vm.startDownload(
                                downloadPath,
                                lastFirmwareVersion!!
                            ) {
                                LogUtil.i("kevin", "<------------文件下载进度:$it------------>")
                            }
                            return
                        }
                    }
                }
            }

            else -> {
            }
        }

    }

    private fun checkUpdateSSFirmware(it: BaseResponse<ClientAppInfo>) {
        when (it.dataState) {
            DataState.STATE_LOADING -> {
                showCenterLoading(R.string.checking)
            }

            DataState.STATE_SUCCESS -> {
                hideCenterLoading()
                val info = it.data as ClientAppInfo
                val bean = info.clientInfo
                if (bean?.url == null || bean.version == null) {
                    showMessageDialog(getString(R.string.current_is_last_version),
                        confirmClickListener = {})
                    return
                }
                lastFirmwareVersion = bean.version
                val downloadPath = bean.url
                showMessageDialog(getString(
                    R.string.new_version_update,
                    getString(R.string.firmware),
                    lastFirmwareVersion
                ),
                    confirm = R.string.download,
                    cancelClickListener = {},
                    confirmClickListener = {
                        vm.startDownload(
                            downloadPath,
                            lastFirmwareVersion!!
                        ) {
                            LogUtil.i("kevin", "<------------文件下载进度:$it------------>")
                        }
                    })
            }

            else -> {
                hideCenterLoading()
            }
        }
    }

    private fun checkUpdateSSApp(it: BaseResponse<ClientAppInfo>) {
        when (it.dataState) {
            DataState.STATE_LOADING -> {
                showCenterLoading(R.string.checking)
            }

            DataState.STATE_SUCCESS -> {
                hideCenterLoading()
                val info = it.data as ClientAppInfo
                val bean = info.clientInfo
                if (bean?.url == null || bean.version == null) {
                    showMessageDialog(getString(R.string.current_is_last_version),
                        confirmClickListener = {})
                    return
                }

                lastAppVersion = bean.version
                val downloadPath = bean.url
                showMessageDialog(getString(
                    R.string.new_version_update,
                    "",
                    lastAppVersion
                ),
                    confirm = R.string.download,
                    cancelClickListener = {},
                    confirmClickListener = {
                        vm.startDownloadApp(
                            downloadPath,
                            PathUtils.getAppSoftPath(lastAppVersion!!) + "LeakTest_V${lastAppVersion}.apk"
                        ) {
                            LogUtil.i("kevin", "<------------文件下载进度:$it------------>")
                        }
                    })
                return
            }

            else -> {
                hideCenterLoading()
            }
        }
    }

    private fun checkUpdateApp(it: BaseResponse<List<FirmwareInfo>>) {
        when (it.dataState) {
            DataState.STATE_LOADING -> {
                showCenterLoading(R.string.checking)
            }

            DataState.STATE_SUCCESS -> {
                hideCenterLoading()
                val infoList = it.data as List<FirmwareInfo>
                for (bean in infoList) {
                    if (ConstantsUtils.SS_APP_NAME == bean.fileTitle) {
                        lastAppVersion = bean.fileCode
                        val downloadPath = bean.filePath
                        val needDownload =
                            FileUtils.checkVersion(lastAppVersion, curAppVersion)
                        LogUtil.d(
                            "kevin",
                            "是否需要升级固件:$needDownload ，downloadPath:$downloadPath ,服务器APP版本：$lastAppVersion ,下位机当前固件版本：$curAppVersion"
                        )
                        if (needDownload) {
                            showMessageDialog(getString(
                                R.string.new_version_update,
                                "",
                                lastAppVersion
                            ),
                                confirm = R.string.download,
                                cancelClickListener = {},
                                confirmClickListener = {
                                    vm.startDownloadApp(
                                        downloadPath,
                                        PathUtils.getAppSoftPath(lastAppVersion!!) + "LeakTest_V${lastAppVersion}.apk"
                                    ) {
                                        LogUtil.i(
                                            "kevin",
                                            "<------------文件下载进度:$it------------>"
                                        )
                                    }
                                })
                            return
                        }
                        showMessageDialog(getString(R.string.current_is_last_version),
                            confirmClickListener = {})
                    }
                }
            }

            else -> {
                hideCenterLoading()
            }
        }
    }

    private fun showMessageDialog(
        msg: String,
        confirm: Int = R.string.btn_confirm,
        cancelClickListener: View.OnClickListener? = null,
        confirmClickListener: View.OnClickListener
    ) {
        if (msgDialog != null && msgDialog!!.isShowing) {
            msgDialog?.dismiss()
            msgDialog = null
        }
        msgDialog = MessageDialog(mContext)
        msgDialog?.showMessage(
            msg = msg,
            confirm = confirm,
            mCancelClick = cancelClickListener,
            mConfirmClick = confirmClickListener
        )
    }

    private fun showToast(msg: Int) {
        //在前台才提示
        if (lifecycle.currentState == Lifecycle.State.RESUMED) {
            launch(Dispatchers.Main) {
                NToast.shortToast(mContext, msg)
            }
        }
    }

    override fun lazyLoadData() {
    }

    companion object {
        @JvmStatic
        fun newInstance() = SettingAboutFragment()
    }
}