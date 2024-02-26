package com.newchip.tool.leaktest.ui.setting.adapter

import android.content.Context
import android.graphics.Typeface
import android.view.View
import com.newchip.tool.leaktest.R
import com.newchip.tool.leaktest.databinding.LanguageItemLayoutBinding
import com.power.baseproject.ktbase.ui.BaseAdapter
import com.power.baseproject.utils.ConstantsUtils
import com.power.baseproject.utils.EasyPreferences
import com.power.baseproject.utils.Tools

class LanguageAdapter(context: Context, languageList: MutableList<String>) :
    BaseAdapter<LanguageItemLayoutBinding, String>(context, languageList) {
    private var curSelectItem = -1
    var isEvt501 = false
    init {
        val productName = EasyPreferences.instance[ConstantsUtils.PRODUCT_NAME]
        isEvt501 = !productName.isNullOrEmpty() && productName == "EVT501"
    }
    override fun convert(v: LanguageItemLayoutBinding, t: String, position: Int) {
        v.tvLanguageName.text = t
        updateSelect(v, position)
    }

    private fun updateSelect(v: LanguageItemLayoutBinding, position: Int) {
        if (curSelectItem == position) {
            v.imgSelect.visibility = View.VISIBLE
            if (isEvt501) {
                v.tvLanguageName.typeface = Typeface.DEFAULT_BOLD
            }
            return
        }
        v.imgSelect.visibility = View.GONE
        if (isEvt501) {
            v.tvLanguageName.typeface = Typeface.DEFAULT
        }
    }

    fun setSelectedId(pos: Int) {
        curSelectItem = pos
        notifyDataSetChanged()
    }
}