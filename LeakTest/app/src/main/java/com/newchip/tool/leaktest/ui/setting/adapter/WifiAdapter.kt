package com.newchip.tool.leaktest.ui.setting.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.newchip.tool.leaktest.R
import com.newchip.tool.leaktest.databinding.ItemWifiLayoutBinding
import com.newchip.tool.leaktest.ui.setting.manager.WifiConnect
import com.newchip.tool.leaktest.ui.setting.manager.WifiDataManager
import com.power.baseproject.bean.DeviceWifiBean
import com.power.baseproject.bean.WifiData
import com.power.baseproject.ktbase.dialog.MessageDialog
import com.power.baseproject.ktbase.ui.BaseAdapter
import com.power.baseproject.utils.ConstantsUtils
import com.power.baseproject.utils.EasyPreferences
import com.power.baseproject.utils.log.LogUtil
import java.net.InetAddress
import java.nio.ByteBuffer
import java.nio.ByteOrder

class WifiAdapter(context: Context, wifiList: MutableList<WifiData>) :
    BaseAdapter<ItemWifiLayoutBinding, WifiData>(context, wifiList) {
    var flag = 0 //0:表示未连接  1：正在连接  2：连接成功   3：连接失败
    var connectWifiName: String? = null
    var currentNetName: String? = null
    var currentNetPassword: String? = null
    var mWifiManager: WifiManager? = null
    var messageDialog: MessageDialog? = null
    var isEvt501 = false

    init {
        mWifiManager =
            context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        connectWifiName =
            getCurrentWifiName()
        val productName = EasyPreferences.instance[ConstantsUtils.PRODUCT_NAME]
        isEvt501 = !productName.isNullOrEmpty() && productName == "EVT501"
    }

    override fun convert(v: ItemWifiLayoutBinding, t: WifiData, position: Int) {
        if (connectWifiName != null && connectWifiName == t.name && position == 0) {
            LogUtil.e("kevin", "已连接wifi:$currentNetName")
            v.tvWifiName.text = t.name
            if (isEvt501) {
                v.tvWifiName.typeface = Typeface.DEFAULT_BOLD
            }
            v.tvWifiStatus.isSelected = true
            v.btnDeviceInfo.visibility = View.VISIBLE
            v.btnDeviceInfo.setOnClickListener {
                val ipInt =
                    WifiConnect.instance.connectWifiInfo?.ipAddress ?: return@setOnClickListener
                val ip = InetAddress.getByAddress(
                    ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(ipInt).array()
                ).hostAddress
                showMessage(
                    R.string.device_ip_title,
                    mContext.getString(R.string.ip_address) + "\n" + ip
                ) {}
            }
        } else {
            v.tvWifiName.text = t.name
            if (isEvt501) {
                v.tvWifiName.typeface = Typeface.DEFAULT
            }
            v.tvWifiStatus.isSelected = false
            v.btnDeviceInfo.visibility = View.GONE
        }

        if (currentNetName != null && currentNetName == t.name) {
            LogUtil.e("kevin", "currentNetName:$currentNetName ==== flag:$flag")
            when (flag) {
                1 -> {
                    v.tvWifiName.text = t.name
                }

                2 -> {
                    connectWifiName = getCurrentWifiName()
                    if (connectWifiName != null && connectWifiName == t.name) {
                        insertConnectWifi(t)

                        v.tvWifiName.text = t.name
                        v.tvWifiStatus.isSelected = true
                        v.btnDeviceInfo.visibility = View.VISIBLE
                        v.btnDeviceInfo.setOnClickListener {
                            val ipInt =
                                WifiConnect.instance.connectWifiInfo?.ipAddress
                                    ?: return@setOnClickListener
                            val ip = InetAddress.getByAddress(
                                ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(ipInt)
                                    .array()
                            ).hostAddress
                            showMessage(
                                R.string.device_ip_title,
                                mContext.getString(R.string.ip_address) + "\n" + ip
                            ) {}
                        }
                    }
                }

                3 -> {
                    v.tvWifiName.text = t.name
                    v.tvWifiStatus.isSelected = false
                }

                else -> {

                }
            }
            flag = 0
        }

    }

    private fun showMessage(title: Int, msg: String, onClickListener: View.OnClickListener) {
        messageDialog?.dismiss()
        messageDialog = null

        messageDialog = MessageDialog(mContext)
        messageDialog?.showMessage(
            title = title,
            msg = msg,
            confirmBg = R.drawable.selector_common_button,
            mConfirmClick = onClickListener
        )
    }

    fun updateWifiList(position: Int) {
        val dataBean: WifiData = listDatas[position]
        val wifiData = WifiData()
        wifiData.name = dataBean.name
        wifiData.password = dataBean.password
        wifiData.capabilities = dataBean.capabilities
        wifiData.signal = dataBean.signal
        listDatas.removeAt(position)
        listDatas.add(0, wifiData)
        notifyDataSetChanged()
    }

    fun setConnectWifiName(isNull: Boolean) {
        connectWifiName = if (isNull) {
            ""
        } else {
            getCurrentWifiName()
        }
    }

    private fun getCurrentWifiName(): String? {
        val wifiInfo: WifiInfo = mWifiManager!!.connectionInfo
        /*判断当前是否链接*/
        if (wifiInfo.ipAddress == 0) {
            return null
        }
        if (wifiInfo.ssid != null) {
            return stringReplace(wifiInfo.ssid)
        }
        return null
    }

    private fun insertConnectWifi(scanResult: WifiData) {
        val wifiBean = DeviceWifiBean()
        wifiBean.name = scanResult.name
        if (currentNetPassword != null) {
            wifiBean.password = currentNetPassword
        } else {
            return
        }
        val wifiList: List<DeviceWifiBean> =
            WifiDataManager.queryWifiByKey(scanResult.name, mContext)
        if (wifiList.isNotEmpty()) {
            var i = 0
            while (i < wifiList.size) {
                if (scanResult.name.equals(wifiList[i].name)) {
                    if (scanResult.password == null && wifiList[i].password == null) break else if (scanResult.password != null && wifiList[i].password != null && scanResult.password
                            .equals(
                                wifiList[i].password
                            )
                    ) break
                }
                i++
            }
            if (i == wifiList.size) {
                WifiDataManager.insertWifi(wifiBean, mContext)
            }
        } else {
            WifiDataManager.insertWifi(wifiBean, mContext)
        }
    }

    @SuppressLint("MissingPermission")
    fun forgetNetWork(name: String, isConnectedItem: Boolean) {
        val wifiList: List<DeviceWifiBean> = WifiDataManager.queryWifiByKey(name, mContext)
        for (wifi in wifiList) {
            WifiDataManager.deleteWifi(wifi, mContext)
        }
        val wifiConfigs = mWifiManager!!.configuredNetworks
        for (wifiConfig in wifiConfigs) {
            if (name == wifiConfig.SSID || "\"" + name + "\"" == wifiConfig.SSID) {
                mWifiManager?.disableNetwork(wifiConfig.networkId)
                mWifiManager?.removeNetwork(wifiConfig.networkId)
                mWifiManager?.saveConfiguration()
                if (isConnectedItem) {
                    mWifiManager!!.disconnect()
                    connectWifiName = ""
                    currentNetName = ""
                    flag = 0
                }
                break
            }
        }
    }

    private fun stringReplace(str: String): String {
        //去掉" "号
        return str.replace("\"", "")
    }

    fun hideKeyboard(v: View) {
        val imm = v.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if (imm.isActive) {
            imm.hideSoftInputFromWindow(v.applicationWindowToken, 0)
        }
    }
}