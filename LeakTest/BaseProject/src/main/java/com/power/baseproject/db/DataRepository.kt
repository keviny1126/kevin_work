package com.power.baseproject.db

import android.content.Context
import android.database.Cursor
import com.power.baseproject.db.entity.TestData

class DataRepository {
    companion object {
        val instance: DataRepository by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            DataRepository()
        }
    }

    /**
     * 查询所有数据
     */
    suspend fun getAllTestData(context: Context): MutableList<TestData>? {
        var dataList: MutableList<TestData>? = null
        try {
            dataList = AppDataBase.getDatabase(context).getTestDataDao().getAll()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (dataList.isNullOrEmpty()) {
            return null
        }
        return dataList
    }

    /**
     * 查询搜索数据
     */
    suspend fun getSearchKeyData(context: Context, key: String): MutableList<TestData>? {
        var dataList: MutableList<TestData>? = null
        try {
            dataList = AppDataBase.getDatabase(context).getTestDataDao().findBySearchKey(key)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (dataList.isNullOrEmpty()) {
            return null
        }
        return dataList
    }


    /**
     * 储存数据
     */
    suspend fun saveData(context: Context, bean: TestData) {
        val dao = AppDataBase.getDatabase(context).getTestDataDao()
        dao.insertAll(bean)
    }

    suspend fun deleteData(context: Context, beanList: MutableList<TestData>): Int {
        val dao = AppDataBase.getDatabase(context).getTestDataDao()
        return dao.delete(beanList)
    }

    /**
     * 查询所有数据
     */
    fun getAllTestDataForCursor(context: Context): Cursor? {
        var cursor: Cursor? = null
        try {
            cursor = AppDataBase.getDatabase(context).getTestDataDao().getAllForCursor()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return cursor
    }

    /**
     * 查询搜索数据
     */
    fun getDataByIdForCursor(context: Context, id: Int): Cursor? {
        var cursor: Cursor? = null
        try {
            cursor = AppDataBase.getDatabase(context).getTestDataDao().findByIdForCursor(id)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return cursor
    }

    suspend fun updateTestData(context: Context, bean: TestData) {
        AppDataBase.getDatabase(context).getTestDataDao().update(bean)
    }
}