package com.power.baseproject.db.dao

import android.database.Cursor
import androidx.room.*
import com.power.baseproject.db.entity.TestData

@Dao
interface TestDataDao {
    @Query("SELECT * FROM TestData")
    fun getAll(): MutableList<TestData>

    @Query("SELECT * FROM TestData WHERE id LIKE :id LIMIT 1")
    fun findById(id: Int): TestData?

    @Query("SELECT * FROM TestData WHERE workpiece_no LIKE '%' || :key || '%' ")
    fun findBySearchKey(key:String):MutableList<TestData>

    @Insert
    fun insertAll(vararg data: TestData)

    // 更新操作，根据参数对象的主键更新指定 row 的数据
    // onConflict 设置当事务中遇到冲突时的策略
    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(vararg data: TestData)

    @Delete
    fun delete(dataList: MutableList<TestData>):Int

    @Query("SELECT * FROM TestData")
    fun getAllForCursor(): Cursor

    @Query("SELECT * FROM TestData WHERE id LIKE :id LIMIT 1")
    fun findByIdForCursor(id: Int): Cursor
}