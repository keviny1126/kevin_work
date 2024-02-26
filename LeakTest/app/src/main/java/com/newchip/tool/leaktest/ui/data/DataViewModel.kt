package com.newchip.tool.leaktest.ui.data

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.power.baseproject.db.DataRepository
import com.power.baseproject.db.entity.TestData
import com.power.baseproject.ktbase.model.BaseViewModel
import com.power.baseproject.utils.FileUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async

class DataViewModel() : BaseViewModel() {
    suspend fun getDataList(context: Context): MutableList<TestData>? {
        val job = viewModelScope.async(Dispatchers.IO) {
            val dataList = DataRepository.instance.getAllTestData(context)
            dataList
        }
        return job.await()
    }

    suspend fun getSearchKeyDataList(context: Context, key: String): MutableList<TestData>? {
        val job = viewModelScope.async(Dispatchers.IO) {
            val dataList = DataRepository.instance.getSearchKeyData(context, key)
            dataList
        }
        return job.await()
    }

    suspend fun deleteData(context: Context, beanList: MutableList<TestData>): Int {
        val job = viewModelScope.async(Dispatchers.IO) {
            for (bean in beanList) {
                if (bean.imagePath != null) {
                    FileUtils.deleteFile(bean.imagePath!!)
                }
            }
            val result = DataRepository.instance.deleteData(context, beanList)
            result
        }
        return job.await()
    }

}