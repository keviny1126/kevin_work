package com.newchip.tool.leaktest.widget

import android.content.Context
import android.util.AttributeSet
import com.newchip.tool.leaktest.R
import com.scwang.smartrefresh.layout.header.ClassicsHeader

class RefreshClassicsHeader @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ClassicsHeader(context, attrs, defStyleAttr) {

    init {
        REFRESH_HEADER_PULLING = context.getString(R.string.refresh_header_pulling)
        REFRESH_HEADER_REFRESHING = context.getString(R.string.refresh_header_refreshing)
        REFRESH_HEADER_RELEASE = context.getString(R.string.refresh_header_release)
        REFRESH_HEADER_FINISH = context.getString(R.string.refresh_header_finish)
        REFRESH_HEADER_FAILED = context.getString(R.string.refresh_header_failed)
    }
}