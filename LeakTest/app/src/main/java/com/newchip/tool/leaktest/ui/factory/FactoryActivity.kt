package com.newchip.tool.leaktest.ui.factory

import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbDevice
import android.os.Bundle
import com.cnlaunch.physics.serialport.SerialPortManager
import com.github.mjdev.libaums.fs.UsbFile
import com.jeremyliao.liveeventbus.LiveEventBus
import com.newchip.tool.leaktest.R
import com.newchip.tool.leaktest.databinding.ActivityFactoryBinding
import com.newchip.tool.leaktest.utils.DeviceConnectUtils
import com.newchip.tool.leaktest.utils.LeakSerialOrderUtils
import com.newchip.tool.leaktest.utils.UsbHelper
import com.power.baseproject.common.UsbListener
import com.power.baseproject.ktbase.ui.BaseActivity
import com.power.baseproject.utils.CmdControl
import com.power.baseproject.utils.ConstantsUtils
import com.power.baseproject.utils.FileUtils
import com.power.baseproject.utils.LiveEventBusConstants
import com.power.baseproject.utils.PathUtils
import com.power.baseproject.utils.Tools
import com.power.baseproject.utils.clicks
import com.power.baseproject.utils.log.LogUtil
import com.power.baseproject.widget.NToast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FactoryActivity : BaseActivity() {
    private lateinit var binding: ActivityFactoryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFactoryBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
    }

    override fun onResume() {
        super.onResume()
        launch(Dispatchers.IO) {
            CmdControl.showOrHideStatusBar(true)
        }
    }

    companion object {
        fun actionStart(context: Context) {
            val intent = Intent(context, FactoryActivity::class.java)
            context.startActivity(intent)
        }
    }
}