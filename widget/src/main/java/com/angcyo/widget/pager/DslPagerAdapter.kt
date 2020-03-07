package com.angcyo.widget.pager

import android.view.ViewGroup
import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget.base.each
import com.angcyo.widget.base.setDslAdapterItem
import com.angcyo.widget.base.tagDslViewHolder

/**
 * 支持[DslAdapterItem]部分功能
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/01/22
 */

open class DslPagerAdapter(var adapterItems: List<DslAdapterItem> = emptyList()) : RPagerAdapter() {

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

    override fun onBindViewHolder(holder: DslViewHolder, position: Int, payload: List<Any>) {
        super.onBindViewHolder(holder, position, payload)
        getAdapterItem(position)?.run {
            holder.itemView.setDslAdapterItem(this)
            itemBind(holder, position, this, payload)
        }
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        return super.instantiateItem(container, position).apply {
            if (this is DslViewHolder) {
                getAdapterItem(position)?.itemViewAttachedToWindow?.invoke(this, position)
            }
        }
    }

    override fun destroyItem(container: ViewGroup, position: Int, item: Any) {
        super.destroyItem(container, position, item)
        if (item is DslViewHolder) {
            getAdapterItem(position)?.itemViewRecycled?.invoke(item, position)
        }
    }

    override fun setPrimaryItem(container: ViewGroup, position: Int, item: Any) {
        super.setPrimaryItem(container, position, item)
        container.each {
            val dslViewHolder = it.tagDslViewHolder()
            dslViewHolder?.apply {
                val adapterPosition =
                    (it.layoutParams as? DslViewPager.LayoutParams)?.adapterPosition ?: -1
                if (adapterPosition != -1 && adapterPosition != position) {
                    getAdapterItem(adapterPosition)?.itemViewDetachedToWindow?.invoke(
                        dslViewHolder,
                        position
                    )
                }
            }
        }
    }

    //<editor-fold desc="数据操作">

    fun resetItems(items: List<DslAdapterItem>) {
        this.adapterItems = items
        notifyDataSetChanged()
    }

    fun notifyItemChanged(
        payload: List<Any> = emptyList(),
        predicate: (DslAdapterItem) -> Boolean
    ) {
        adapterItems.forEachIndexed { index, dslAdapterItem ->
            if (predicate(dslAdapterItem)) {
                notifyItemChanged(index, payload)
            }
        }
    }

    //</editor-fold desc="数据操作">

}