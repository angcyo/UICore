package com.angcyo.widget.pager

import android.view.ViewGroup
import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget.base.each

/**
 * 支持[DslAdapterItem]部分功能
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/01/22
 */

open class DslPagerAdapter(val adapterItems: List<DslAdapterItem>) : RPagerAdapter() {
    override fun getCount(): Int {
        return adapterItems.size
    }

    fun getAdapterItem(position: Int): DslAdapterItem? {
        return adapterItems.getOrNull(position)
    }

    override fun getItemViewType(position: Int): Int {
        return getAdapterItem(position)!!.itemLayoutId
    }

    override fun getItemLayoutId(viewType: Int): Int {
        return viewType
    }

    override fun onBindViewHolder(holder: DslViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        getAdapterItem(position)?.run {
            itemBind(holder, position, this, emptyList())
        }
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        return super.instantiateItem(container, position).apply {
            if (this is DslViewHolder) {
                getAdapterItem(position)?.onItemViewAttachedToWindow?.invoke(this)
            }
        }
    }

    override fun destroyItem(container: ViewGroup, position: Int, item: Any) {
        super.destroyItem(container, position, item)
        if (item is DslViewHolder) {
            getAdapterItem(position)?.onItemViewRecycled?.invoke(item)
        }
    }

    override fun setPrimaryItem(container: ViewGroup, position: Int, item: Any) {
        super.setPrimaryItem(container, position, item)
        container.each {
            if (it.tag is DslViewHolder) {
                val dslViewHolder = it.tag as DslViewHolder
                val adapterPosition =
                    (it.layoutParams as? DslViewPager.LayoutParams)?.adapterPosition ?: -1
                if (adapterPosition != -1 && adapterPosition != position) {
                    getAdapterItem(position)?.onItemViewDetachedToWindow?.invoke(dslViewHolder)
                }
            }
        }
    }
}