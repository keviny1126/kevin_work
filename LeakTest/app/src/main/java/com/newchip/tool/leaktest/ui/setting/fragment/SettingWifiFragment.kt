package com.newchip.tool.leaktest.ui.setting.fragment

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.NetworkInfo
import android.net.NetworkInfo.DetailedState
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Message
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.jeremyliao.liveeventbus.LiveEventBus
import com.newchip.tool.leaktest.R
import com.newchip.tool.leaktest.databinding.FragmentWifiBinding
import com.newchip.tool.leaktest.ui.setting.adapter.WifiAdapter
import com.newchip.tool.leaktest.ui.setting.manager.WifiConnect
import com.newchip.tool.leaktest.ui.setting.manager.WifiDataManager
import com.newchip.tool.leaktest.widget.InputPasswordDialog
import com.power.baseproject.bean.DeviceWifiBean
import com.power.baseproject.bean.PermissionListBean
import com.power.baseproject.bean.WifiData
import com.power.baseproject.ktbase.application.BaseApplication
import com.power.baseproject.ktbase.dialog.MessageDialog
import com.power.baseproject.ktbase.ui.BaseAppFragment
import com.power.baseproject.utils.HandlerUtils
import com.power.baseproject.utils.LiveEventBusConstants
import com.power.baseproject.utils.Tools
import com.power.baseproject.utils.log.LogUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class SettingWifiFragment : BaseAppFragment<FragmentWifiBinding>(),
    HandlerUtils.OnReceiveMessageListener {

    private var wifiReceiver: WifiReceiver? = null
    private var mWifiHandler: HandlerUtils.HandlerHolder? = null
    private var wifiList = arrayListOf<WifiData>()
    private lateinit var mAdapter: WifiAdapter
    private var messageDialog: MessageDialog? = null
    private var inputDialog: InputPasswordDialog? = null
    private var mPermissionAllList = arrayListOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentWifiBinding.inflate(inflater, container, false)

    override fun initView() {
        LiveEventBus.get(LiveEventBusConstants.SETTING_TITLE_NAME).post(2)
        showOrHideHeadView(false)
        initReceiver()
        initRecycleView()
        initClick()
    }

    override fun initData() {
        super.initData()
        mWifiHandler = HandlerUtils.HandlerHolder(this)
        WifiConnect.instance.setHandler(mWifiHandler)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val mPermissionList = Tools.requestPermission(mContext, mPermissionAllList)
            if (mPermissionList.isNotEmpty()) {
                val bean = PermissionListBean()
                bean.permissionList = mPermissionList
                LiveEventBus.get(LiveEventBusConstants.SEND_REQUEST_PERMISSION).post(bean)
                return
            }
        }
        WifiConnect.instance.openWifi()
        mVb.refreshLayout.autoRefresh()
    }

    private fun initRecycleView() {
        mAdapter = WifiAdapter(mContext, wifiList)
        mVb.rcvWifiList.layoutManager = LinearLayoutManager(context)
        mVb.rcvWifiList.adapter = mAdapter
    }

    private fun initClick() {
        mVb.refreshLayout.setOnRefreshListener {
            WifiConnect.instance.scanWifi()
            mAdapter.connectWifiName = getCurrentWifiName()
        }
        mAdapter.itemLongClick {
            val bean = wifiList[it]
            if (mAdapter.connectWifiName != null && mAdapter.connectWifiName == bean.name) {
                showForgetPasswordOrDisconnectWifiDialog(mAdapter.connectWifiName!!)
            }
        }
        mAdapter.itemClick { itemWifiLayoutBinding, i ->
            val bean = wifiList[i]
            if (mAdapter.connectWifiName != null && mAdapter.connectWifiName == bean.name) {
                return@itemClick
            }
            val wifiName = bean.name
            val wifiCapabilities = bean.capabilities
            val password = bean.password
            if (wifiCapabilities != null && (wifiCapabilities.lowercase(Locale.getDefault())
                    .contains("wep") || wifiCapabilities.lowercase(Locale.getDefault())
                    .contains("wpa"))
            ) {
                val wifiList: List<DeviceWifiBean> =
                    WifiDataManager.queryWifiByKey(wifiName, mContext)
                if (wifiList.isNotEmpty() && wifiList[0].password != null) {
                    mAdapter.connectWifiName = ""
                    mAdapter.updateWifiList(i)
                    mAdapter.currentNetName = wifiName
                    showCenterLoading(getString(R.string.connecting, wifiName))
                    WifiConnect.instance.connectByManager(
                        wifiName!!,
                        wifiList[0].password,
                        wifiCapabilities
                    )
                } else {
                    showInputWifiPasswordDialog(i, wifiName!!, wifiCapabilities)
                }
                return@itemClick
            }
            if (WifiConnect.instance.addWifi(
                    wifiName!!,
                    password,
                    wifiCapabilities!!
                )
            ) {
                mAdapter.connectWifiName = ""
                showCenterLoading(getString(R.string.connecting, wifiName))
                WifiConnect.instance.connectByManager(wifiName, null, null)
            }
            mAdapter.currentNetName = wifiName
            mAdapter.updateWifiList(i)
        }
    }

    private fun initReceiver() {
        wifiReceiver = WifiReceiver()
        val intentFilter = IntentFilter()
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION)
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION)
        activity?.registerReceiver(wifiReceiver, intentFilter)
    }

    override fun createObserver() {
        LiveEventBus.get(LiveEventBusConstants.REQUEST_PERMISSION_RESULT, Boolean::class.java)
            .observe(this) {
                if (it) {
                    WifiConnect.instance.openWifi()
                    mVb.refreshLayout.autoRefresh()
                }
            }
    }

    override fun lazyLoadData() {
    }

    private fun showForgetPasswordOrDisconnectWifiDialog(name: String) {
        messageDialog?.dismiss()
        messageDialog = null

        messageDialog = MessageDialog(mContext)
        messageDialog?.showMessage(
            R.string.wifi_forget,
            name,
            confirm = R.string.wifi_forget,
            cancel = R.string.wifi_disconnect,
            mCancelClick = {
                WifiConnect.instance.disconnect(name)
                mAdapter.connectWifiName = ""
                mAdapter.currentNetName = ""
                mAdapter.flag = 0
                mAdapter.notifyDataSetChanged()
            }
        ) {
            mAdapter.forgetNetWork(name, true)
            mAdapter.notifyDataSetChanged()
        }
    }

    private fun showInputWifiPasswordDialog(
        position: Int,
        wifi_name: String,
        wifi_capabilities: String
    ) {
        inputDialog?.dismiss()
        inputDialog = null
        inputDialog = InputPasswordDialog(mContext)
        inputDialog?.setTitle(wifi_name)
        inputDialog?.showDialog(mCancelClick = {
            mAdapter.hideKeyboard(inputDialog!!.getPswView())
        }) {
            mAdapter.hideKeyboard(inputDialog!!.getPswView())
            val psw = inputDialog!!.getPswText().trim { it <= ' ' }
            mAdapter.currentNetName = wifi_name
            mAdapter.connectWifiName = ""
            mAdapter.updateWifiList(position)
            val result: Boolean = WifiConnect.instance.addWifi(
                wifi_name,
                psw,
                wifi_capabilities
            )
            if (result) {
                mAdapter.connectWifiName = ""
                showCenterLoading(getString(R.string.connecting, wifi_name))
                WifiConnect.instance.connectByManager(
                    wifi_name,
                    psw,
                    wifi_capabilities
                )
                mAdapter.currentNetPassword = psw
                return@showDialog
            }
            connectFailed()
        }
    }

    private fun getCurrentWifiName(): String? {
        val mWifiManager =
            mContext.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val wifiInfo = mWifiManager.connectionInfo
        //判断当前是否链接
        if (wifiInfo!!.ipAddress == 0) {
            return null
        }
        if (wifiInfo.ssid != null) {
            return stringReplace(wifiInfo.ssid)
        }
        return null
    }

    private fun stringReplace(str: String): String {
        //去掉" "号
        return str.replace("\"", "")
    }

    internal inner class WifiReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent?.action
            when (action) {
                //扫描已完成，结果可用
                WifiManager.SCAN_RESULTS_AVAILABLE_ACTION -> {
                    wifiList.clear()
                    wifiList.addAll(WifiConnect.instance.changeToWifiData(WifiConnect.instance.scanResultsWifi()))
                    mAdapter.listDatas = wifiList
                    mAdapter.notifyDataSetChanged()
                    mVb.refreshLayout.finishRefresh()
                }
                //Wi-Fi连接状态
                WifiManager.NETWORK_STATE_CHANGED_ACTION -> {
                    val parcelable = intent
                        .getParcelableExtra<Parcelable>(WifiManager.EXTRA_NETWORK_INFO)
                    if (parcelable is NetworkInfo) {
                        LogUtil.e("kevin", "wifi 连接状态改变：${parcelable.detailedState}")
                        when (parcelable.detailedState) {
                            DetailedState.CONNECTED -> {
                                mWifiHandler!!.removeCallbacksAndMessages(null)
                                hideCenterLoading()
                                mAdapter.flag = 2
                                mAdapter.notifyItemChanged(0)
                            }
                            DetailedState.CONNECTING -> {
                                mAdapter.flag = 1
                                mAdapter.notifyItemChanged(0)
                            }
                            DetailedState.DISCONNECTED -> mAdapter.setConnectWifiName(
                                false
                            )
                            else -> {
                            }
                        }
                    }
                }
                //指示Wi-Fi已启用、禁用
                WifiManager.WIFI_STATE_CHANGED_ACTION -> {

                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (wifiReceiver == null) {
            wifiReceiver = WifiReceiver()
            val intentFilter = IntentFilter()
            intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
            intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION)
            activity?.registerReceiver(wifiReceiver, intentFilter)
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

    override fun onStop() {
        super.onStop()
        if (wifiReceiver != null) {
            activity?.unregisterReceiver(wifiReceiver)
            wifiReceiver = null
        }
    }

    override fun onDestroyView() {
        if (wifiReceiver != null) {
            activity?.unregisterReceiver(wifiReceiver)
            wifiReceiver = null
        }
        mWifiHandler?.removeCallbacksAndMessages(null)
        WifiConnect.instance.setHandler(null)
        super.onDestroyView()
    }

    override fun needLoaddialog(): Boolean {
        return true
    }

    companion object {
        @JvmStatic
        fun newInstance() = SettingWifiFragment()
    }

    private fun connectFailed() {
        hideCenterLoading()
        //刷新失败信息
        mAdapter.flag = 3
        mAdapter.notifyItemChanged(0)
        //连接失败后刷新一下WIFI列表
        WifiConnect.instance.openWifi()
        mVb.refreshLayout.autoRefresh()
        //连接失败后删除可能保存的错误的WIFI密码，确保下一次能正确连接
        val wifiList: List<DeviceWifiBean> =
            WifiDataManager.queryWifiByKey(mAdapter.currentNetName, mContext)
        for (wifi in wifiList) {
            WifiDataManager.deleteWifi(wifi, mContext)
        }
        //连接失败后不再显示连接状态
        launch(Dispatchers.Main) {
            withContext(Dispatchers.IO) {
                delay(2000)
            }
            mAdapter.currentNetName = ""
            mAdapter.currentNetPassword = ""
        }
        showMessage(getString(R.string.wifi_connect_failed)) {}
    }

    override fun handlerMessage(msg: Message) {
        when (msg.what) {
            1 -> {
                connectFailed()
            }
        }
    }
}