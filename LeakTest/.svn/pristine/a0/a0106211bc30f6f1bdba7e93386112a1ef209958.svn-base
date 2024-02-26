package com.power.baseproject.widget

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import com.power.baseproject.R

class FontView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {
    companion object {
        const val SS = 1
        const val XK = 2
        const val ZY = 3
    }

    init {
        initAttrs(context, attrs)
    }

    private fun initAttrs(context: Context, attrs: AttributeSet?) {
        val attributes = context.obtainStyledAttributes(attrs, R.styleable.FontView)
        val fontType = attributes.getInt(R.styleable.FontView_fontType, 1)
        var fontPath: String? = null
        when (fontType) {
            SS -> fontPath = "ss_font.otf"
            XK -> {
            }
            ZY -> {
            }
            else -> fontPath = "yj_font.ttf"
        }
        //设置字体
        if (!fontPath.isNullOrEmpty()) {
            val typeFace = Typeface.createFromAsset(getContext().assets, fontPath)
            typeface = typeFace
        }
        attributes.recycle()
    }
}