package com.newchip.tool.leaktest.ui.setting

import androidx.lifecycle.MutableLiveData
import com.power.baseproject.ktbase.model.BaseViewModel
import com.power.baseproject.ktbase.model.MainRepo

class SettingViewModel(val mainRepo: MainRepo) : BaseViewModel() {
    private val pageLiveData = MutableLiveData<Int>()

    fun setPage(page: Int) {
        pageLiveData.value = page
    }

    fun getPage(): Int? {
        return pageLiveData.value
    }

    companion object {
        const val TAG = "SettingViewModel"
    }
}