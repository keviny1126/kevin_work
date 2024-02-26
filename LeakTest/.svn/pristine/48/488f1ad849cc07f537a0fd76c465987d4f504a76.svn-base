package com.newchip.tool.leaktest

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri
import com.power.baseproject.db.DataRepository
import com.power.baseproject.utils.log.LogUtil

class ReportContentProvider : ContentProvider() {

    private val AUTHORITY = "com.newchip.tool.et500.provider"
    private val CONTENT_URL = Uri.parse("content://$AUTHORITY/reportData")

    private val TEST_DATA = 1
    private val TEST_DATA_ID = 2
    private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH)

    init {
        uriMatcher.addURI(AUTHORITY, "reportData", TEST_DATA)
        uriMatcher.addURI(AUTHORITY, "reportData/#", TEST_DATA_ID)
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        return 0
    }

    override fun getType(uri: Uri): String? {
        return null
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        return null
    }

    override fun onCreate(): Boolean {
        return true
    }

    override fun query(
        uri: Uri, projection: Array<String>?, selection: String?,
        selectionArgs: Array<String>?, sortOrder: String?
    ): Cursor? {
        return when (uriMatcher.match(uri)) {
            TEST_DATA -> {
                // 查询所有数据
                val cursor = context?.let { DataRepository.instance.getAllTestDataForCursor(it) }
                cursor?.setNotificationUri(context?.contentResolver, uri)
                cursor
            }

            TEST_DATA_ID -> {
                // 查询指定数据
                val id = uri.lastPathSegment?.toIntOrNull()
                val cursor = if (id != null) {
                    context?.let { DataRepository.instance.getDataByIdForCursor(it, id) }
                } else {
                    null
                }
                cursor?.setNotificationUri(context?.contentResolver, uri)
                cursor
            }

            else -> null
        }
    }

    override fun update(
        uri: Uri, values: ContentValues?, selection: String?,
        selectionArgs: Array<String>?
    ): Int {
        return 0
    }
}