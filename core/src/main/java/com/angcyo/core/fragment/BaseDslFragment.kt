package com.angcyo.core.fragment

import androidx.recyclerview.widget.RecyclerView
import com.angcyo.core.R
import com.angcyo.dsladapter.DslAdapter
import com.angcyo.dsladapter.DslItemDecoration
import com.angcyo.dsladapter.HoverItemDecoration
import com.angcyo.widget.recycler.resetLayoutManager

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/12/23
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
open class BaseDslFragment : BaseTitleFragment() {

    var hoverItemDecoration: HoverItemDecoration? = HoverItemDecoration()
    var baseDslItemDecoration: RecyclerView.ItemDecoration? = DslItemDecoration()

    override fun getContentLayoutId(): Int = R.layout.lib_recycler_layout

    override fun initTitleFragment() {
        super.initTitleFragment()
        initDslLayout()
    }

    open fun initDslLayout() {
        baseViewHolder.rv(R.id.lib_recycler_view)?.apply {
            baseDslItemDecoration?.let { addItemDecoration(it) }
            hoverItemDecoration?.attachToRecyclerView(this)
            adapter = DslAdapter()

            resetLayoutManager("V")
        }
    }

    /**调用此方法, 渲染界面*/
    open fun renderDslAdapter(config: DslAdapter.() -> Unit) {
        baseViewHolder.rv(R.id.lib_recycler_view)?.let {
            if (it.adapter is DslAdapter) {
                (it.adapter as DslAdapter).config()
            }
        }
    }
}

/**[DslAdapter]必备的组件*/
fun RecyclerView.initDsl() {
    addItemDecoration(DslItemDecoration())
    HoverItemDecoration().attachToRecyclerView(this)
}

/**快速初始化[DslAdapter]*/
fun RecyclerView.initDslAdapter(action: DslAdapter.() -> Unit) {
    initDsl()
    resetLayoutManager("v")
    adapter = DslAdapter().apply {
        this.action()
    }
}