package com.power.baseproject.ktbase.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.power.baseproject.utils.clicks
import java.lang.reflect.ParameterizedType

/**
 * 通过传入ViewBinding，不再需要写具体xml资源，省略onBindViewHolder中findviewById
 * 注意点：item的最外层布局高度要设为wrap_content，
 * 如果item有需求要设置为固定宽高，可以在子类的convert方法里，通过代码设置
 */
abstract class BaseAdapter<VB : ViewBinding, T>(
    var mContext: Context,
    var listDatas: MutableList<T>
) : RecyclerView.Adapter<BaseViewHolder>() {
    var clickPos: Int = -1//当前点击的item

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val type = javaClass.genericSuperclass as ParameterizedType
        val clazz = type.actualTypeArguments[0] as Class<VB>
        val method = clazz.getMethod("inflate", LayoutInflater::class.java)
        var vb = method.invoke(null, LayoutInflater.from(mContext)) as VB
        vb.root.layoutParams = RecyclerView.LayoutParams(
            RecyclerView.LayoutParams.MATCH_PARENT,
            RecyclerView.LayoutParams.WRAP_CONTENT
        )
        return BaseViewHolder(vb, vb.root)
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        holder.itemView.clicks {
            itemClick?.let {
                clickPos = position
                it(holder.v as VB, position)
            }
        }
        holder.itemView.setOnLongClickListener {
            itemLongClick?.let { it1 -> it1(position) }
            true
        }
        convert(holder.v as VB, listDatas[position], position)
    }

    override fun getItemCount(): Int {
        return listDatas.size
    }

    abstract fun convert(v: VB, t: T, position: Int)
    private var itemClick: ((VB, Int) -> Unit)? = null
    private var itemLongClick: ((Int) -> Unit)? = null

    fun itemClick(itemClick: (VB, Int) -> Unit) {
        this.itemClick = itemClick
    }

    fun itemLongClick(itemLongClick: (Int) -> Unit) {
        this.itemLongClick = itemLongClick
    }
}