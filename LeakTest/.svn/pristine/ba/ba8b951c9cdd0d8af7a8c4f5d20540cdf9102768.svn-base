package com.power.baseproject.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.power.baseproject.db.dao.TestDataDao
import com.power.baseproject.db.entity.TestData

@Database(
    // 指定该数据库有哪些表，若需建立多张表，以逗号相隔开
    entities = [TestData::class],
    // 指定数据库版本号，后续数据库的升级正是依据版本号来判断的
    version = 2
)
abstract class AppDataBase : RoomDatabase() {

    abstract fun getTestDataDao(): TestDataDao

    companion object {
        private const val DB_NAME = "app_data.db"

        @Volatile
        private var INSTANCE: AppDataBase? = null
        /**
         * 自定义一个合并的方法
         * 新增表
         */
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE TestData ADD COLUMN deviceSN TEXT")
                database.execSQL("ALTER TABLE TestData ADD COLUMN reportNo TEXT")
            }
        }

        fun getDatabase(context: Context): AppDataBase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext, AppDataBase::class.java, DB_NAME
                ).allowMainThreadQueries()// 允许在主线程中操作，如果不加的话，只能在子线程中调用
                    .addMigrations(MIGRATION_1_2)
                    //     .fallbackToDestructiveMigration() // 等于删除数据库后重建表，这只是升级数据库的方法之一，并不推荐（原有数据都被清空了)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}