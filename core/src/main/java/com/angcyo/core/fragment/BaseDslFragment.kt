package com.angcyo.core.fragment

import androidx.recyclerview.widget.RecyclerView
import com.angcyo.core.R
import com.angcyo.dsladapter.DslAdapter
import com.angcyo.dsladapter.DslItemDecoration
import com.angcyo.dsladapter.HoverItemDecoration
import com.angcyo.library.L
import com.angcyo.widget.recycler.noItemChangeAnim

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/12/23
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
open class BaseDslFragment : BaseTitleFragment() {

    /**为[DslAdapterItem]提供悬停功能*/
    var hoverItemDecoration: HoverItemDecoration? = HoverItemDecoration()
    /**为[DslAdapterItem]提供基础的分割线功能*/
    var baseDslItemDecoration: DslItemDecoration? = DslItemDecoration()

    /**实时获取[DslAdapter]*/
    val _adapter: DslAdapter
        get() = (_vh.rv(R.id.lib_recycler_view)?.adapter as? DslAdapter) ?: DslAdapter().apply {
            L.e("注意:访问目标[DslAdapter]不存在")
        }

    override fun onInitFragment() {
        super.onInitFragment()

        _vh.rv(R.id.lib_recycler_view)?.apply {
            val dslAdapter = DslAdapter()
            onInitDslLayout(this, dslAdapter)
            adapter = dslAdapter
        }
    }

    /**初始化布局*/
    open fun onInitDslLayout(recyclerView: RecyclerView, dslAdapter: DslAdapter) {
        recyclerView.noItemChangeAnim()
        baseDslItemDecoration?.attachToRecyclerView(recyclerView)
        hoverItemDecoration?.attachToRecyclerView(recyclerView)
    }


    /**调用此方法, 渲染界面*/
    open fun renderDslAdapter(config: DslAdapter.() -> Unit) {
        _adapter.config()
    }
}