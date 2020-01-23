package com.angcyo.widget.pager

import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.widget.DslViewHolder

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
            itemBind(holder, position, this)
        }
    }
}