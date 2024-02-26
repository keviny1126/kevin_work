package com.newchip.tool.leaktest.ui.detection

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.cnlaunch.physics.serialport.util.AnalysisLeakData
import com.cnlaunch.physics.utils.ByteHexHelper
import com.google.gson.Gson
import com.jeremyliao.liveeventbus.LiveEventBus
import com.newchip.tool.leaktest.utils.AnalysisTools
import com.newchip.tool.leaktest.utils.DeviceConnectUtils
import com.newchip.tool.leaktest.utils.LeakSerialOrderUtils
import com.power.baseproject.bean.ConfigLeakInfoBean
import com.power.baseproject.bean.LeakDataBean
import com.power.baseproject.bean.TestOnOrOffResult
import com.power.baseproject.db.DataRepository
import com.power.baseproject.db.entity.TestData
import com.power.baseproject.ktbase.model.BaseViewModel
import com.power.baseproject.utils.ConstantsUtils
import com.power.baseproject.utils.EasyPreferences
import com.power.baseproject.utils.LiveEventBusConstants
import com.power.baseproject.utils.log.LogUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DetectionViewModel : BaseViewModel() {
    val leakDataLivedata = MutableLiveData<LeakDataBean>()//实时气压值回调
    val testSwitchLivedata = MutableLiveData<TestOnOrOffResult>()//测试启停结果回调
    val exhaustLivedata = MutableLiveData<TestOnOrOffResult>()//测试启停结果回调

    fun getDeviceInfo() {
        val mCurrentDevice = DeviceConnectUtils.instance.getCurrentDevice() ?: return
        LeakSerialOrderUtils.getDeviceInfoOrder(mCurrentDevice)
    }

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
     fun sendExhaustOrder(data:String) {
        val mCurrentDevice = DeviceConnectUtils.instance.getCurrentDevice() ?: return
        LeakSerialOrderUtils.sendExhaustOrder(mCurrentDevice,data)
    }

    fun getConfigData(): ConfigLeakInfoBean {
        val json = EasyPreferences.instance[ConstantsUtils.LEAK_CONFIG_KEY]
        if (json != null && json.isNotEmpty()) {
            return Gson().fromJson(json, ConfigLeakInfoBean::class.java)
        }
        return ConfigLeakInfoBean()
    }

    fun setConfigData(bean: ConfigLeakInfoBean): Boolean {
        return EasyPreferences.instance.putToJson(ConstantsUtils.LEAK_CONFIG_KEY, bean)
    }

    fun setData(context: Context, bean: TestData) {
        viewModelScope.launch(Dispatchers.IO) {
            DataRepository.instance.saveData(context, bean)
        }
    }

}