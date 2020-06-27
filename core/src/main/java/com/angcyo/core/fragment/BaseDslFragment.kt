package com.angcyo.core.fragment

import android.os.Bundle
import androidx.annotation.CallSuper
import androidx.recyclerview.widget.RecyclerView
import com.angcyo.behavior.refresh.IRefreshContentBehavior
import com.angcyo.core.R
import com.angcyo.dsladapter.*
import com.angcyo.dsladapter.data.loadDataEnd
import com.angcyo.library.L
import com.angcyo.library.model.Page
import com.angcyo.widget.recycler.DslRecyclerView
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

    /**实时获取[DslRecyclerView]*/
    val _recycler: DslRecyclerView
        get() = (_vh.rv(R.id.lib_recycler_view) as? DslRecyclerView)
            ?: DslRecyclerView(fContext()).apply {
                L.e("注意:访问目标[DslRecyclerView]不存在!")
            }

    /**实时获取[DslAdapter]*/
    val _adapter: DslAdapter
        get() = (_recycler.adapter as? DslAdapter) ?: DslAdapter().apply {
            L.e("注意:访问目标[DslAdapter]不存在!")
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

        dslAdapter.onRefreshOrLoadMore { itemHolder, loadMore ->
            if (loadMore) {
                onLoadMore()
            } else {
                onRefresh(null)
            }
        }
    }

    /**调用此方法, 渲染界面
     * [reset] 是否需要重置界面*/
    open fun renderDslAdapter(reset: Boolean = false, config: DslAdapter.() -> Unit) {
        if (reset) {
            _adapter.dataItems.clear()
        }
        _adapter.config()
    }

    //<editor-fold desc="数据加载">

    /**页面请求相关辅助操作参数*/
    var page = Page()

    override fun onFragmentFirstShow(bundle: Bundle?) {
        super.onFragmentFirstShow(bundle)
        //触发加载中
        if (enableRefresh) {
            _adapter.toLoading()
        }
    }

    override fun onRefresh(refreshContentBehavior: IRefreshContentBehavior?) {
        page.pageRefresh()
        onLoadData()
    }

    open fun onLoadMore() {
        page.pageLoadMore()
        onLoadData()
    }

    /**重写此方法, 拉取数据*/
    @CallSuper
    open fun onLoadData() {
        //因为使用的behavior实现的刷新, 所以fling操作之后, scroll并不一定就会停止
        _recycler.stopScroll()
    }

    /**数据加载完成后, 调用此方法*/
    fun <Item : DslAdapterItem, Bean> loadDataEnd(
        itemClass: Class<Item>,
        dataList: List<Bean>?,
        error: Throwable? = null,
        initItem: Item.(data: Bean) -> Unit = {}
    ) {
        finishRefresh()
        _adapter.loadDataEnd(itemClass, dataList, error, page, initItem)
    }

    //</editor-fold desc="数据加载">

}