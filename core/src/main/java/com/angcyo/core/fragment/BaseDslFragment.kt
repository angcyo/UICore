package com.angcyo.core.fragment

import android.os.Bundle
import androidx.recyclerview.widget.RecyclerView
import com.angcyo.core.R
import com.angcyo.dsladapter.DslAdapter
import com.angcyo.dsladapter.DslItemDecoration
import com.angcyo.dsladapter.HoverItemDecoration
import com.angcyo.fragment.AbsLifecycleFragment
import com.angcyo.widget.DslViewHolder

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/12/23
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
open class BaseDslFragment : AbsLifecycleFragment() {

    var hoverItemDecoration: HoverItemDecoration? = HoverItemDecoration()
    var baseDslItemDecoration: RecyclerView.ItemDecoration? = DslItemDecoration()

    override fun getFragmentLayoutId(): Int = R.layout.lib_recycler_layout

    override fun initBaseView(
        viewHolder: DslViewHolder,
        arguments: Bundle?,
        savedInstanceState: Bundle?
    ) {
        super.initBaseView(viewHolder, arguments, savedInstanceState)
        initDslLayout(viewHolder, arguments, savedInstanceState)
    }

    open fun initDslLayout(
        viewHolder: DslViewHolder,
        arguments: Bundle?,
        savedInstanceState: Bundle?
    ) {
        viewHolder.rv(R.id.base_recycler_view)?.apply {
            baseDslItemDecoration?.let { addItemDecoration(it) }
            hoverItemDecoration?.attachToRecyclerView(this)
            adapter = DslAdapter()
        }
    }

    /**调用此方法, 渲染界面*/
    open fun renderDslAdapter(config: DslAdapter.() -> Unit) {
        baseViewHolder.rv(R.id.base_recycler_view)?.let {
            if (it.adapter is DslAdapter) {
                (it.adapter as DslAdapter).config()
            }
        }
    }
}