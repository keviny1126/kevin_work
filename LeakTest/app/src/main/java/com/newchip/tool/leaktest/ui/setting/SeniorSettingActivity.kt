package com.newchip.tool.leaktest.ui.setting

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.newchip.tool.leaktest.databinding.ActivitySeniorSettingBinding
import com.power.baseproject.ktbase.ui.BaseActivity

class SeniorSettingActivity : BaseActivity() {
    private lateinit var binding: ActivitySeniorSettingBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySeniorSettingBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
    }

    companion object {
        fun actionStart(context: Context) {
            val intent = Intent(context, SeniorSettingActivity::class.java)
            context.startActivity(intent)
        }
    }
}