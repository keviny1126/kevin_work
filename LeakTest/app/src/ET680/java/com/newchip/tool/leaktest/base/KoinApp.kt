package com.newchip.tool.leaktest.base

import com.newchip.tool.leaktest.ui.MainViewModel
import com.newchip.tool.leaktest.ui.data.DataViewModel
import com.newchip.tool.leaktest.ui.factory.AgingViewModel
import com.newchip.tool.leaktest.ui.setting.SettingViewModel
import com.power.baseproject.ktbase.api.HttpApi
import com.power.baseproject.ktbase.api.HttpOpApi
import com.power.baseproject.ktbase.api.HttpSmartSafeApi
import com.power.baseproject.ktbase.api.RetrofitManager
import com.power.baseproject.ktbase.api.RetrofitOpApiManager
import com.power.baseproject.ktbase.api.RetrofitSmartSafeManager
import com.power.baseproject.ktbase.model.MainRepo
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appKtModule = module {
    single { RetrofitManager.instance.getService(HttpApi::class.java) }
    single { RetrofitSmartSafeManager.instance.getService(HttpSmartSafeApi::class.java) }
    single { RetrofitOpApiManager.instance.getService(HttpOpApi::class.java) }
    single { MainRepo(get(), get(), get()) }
    viewModel { SettingViewModel(get()) }
    viewModel { DataViewModel() }
    viewModel { MainViewModel(get()) }
    viewModel { AgingViewModel(get()) }
}