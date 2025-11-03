package com.angcyo.core.fragment

import android.os.Bundle
import androidx.annotation.AnyThread
import androidx.annotation.CallSuper
import androidx.annotation.UiThread
import androidx.recyclerview.widget.RecyclerView
import com.angcyo.behavior.refresh.IRefreshContentBehavior
import com.angcyo.core.R
import com.angcyo.dsladapter.DslAdapter
import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.dsladapter.DslAdapterStatusItem
import com.angcyo.dsladapter.DslItemDecoration
import com.angcyo.dsladapter.HoverItemDecoration
import com.angcyo.dsladapter.PlaceholderDslAdapter
import com.angcyo.dsladapter.annotation.UpdateByDiff
import com.angcyo.dsladapter.data.SingleDataUpdate
import com.angcyo.dsladapter.data.loadDataEndIndex
import com.angcyo.dsladapter.data.resetRender
import com.angcyo.dsladapter.data.updateAdapter
import com.angcyo.dsladapter.item.IFragmentItem
import com.angcyo.dsladapter.loadingStatus
import com.angcyo.dsladapter.onRefreshOrLoadMore
import com.angcyo.dsladapter.toLoading
import com.angcyo.library.model.Page
import com.angcyo.widget.recycler.noItemChangeAnim
import kotlin.reflect.KClass

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
        get() = (_recycler.adapter as? DslAdapter) ?: PlaceholderDslAdapter()

    /**[initBaseView]*/
    override fun onInitFragment(savedInstanceState: Bundle?) {
        super.onInitFragment(savedInstanceState)

        _vh.rv(R.id.lib_recycler_view)?.apply {
            val dslAdapter = DslAdapter()
            //监听[IFragmentItem]
            dslAdapter.hookUpdateDepend()
            onInitDslLayout(this, dslAdapter)
            adapter = dslAdapter
        }
    }

    /**监听, 并赋值[IFragmentItem]*/
    fun DslAdapter.hookUpdateDepend() {
        observeItemUpdateDepend {
            adapterItems.forEach {
                if (it is IFragmentItem) {
                    it.itemFragment = this@BaseDslFragment
                }
            }
        }
    }

    /**初始化布局
     * [onInitFragment]*/
    open fun onInitDslLayout(recyclerView: RecyclerView, dslAdapter: DslAdapter) {
        recyclerView.noItemChangeAnim()
        baseDslItemDecoration?.attachToRecyclerView(recyclerView)
        hoverItemDecoration?.attachToRecyclerView(recyclerView)

        dslAdapter.onRefreshOrLoadMore { _, loadMore ->
            if (loadMore) {
                onLoadMore()
            } else {
                _delayRefresh {
                    onRefresh(null)
                }
            }
        }
    }

    /**调用此方法, 渲染界面
     * [reset] 是否需要重置界面*/
    open fun renderDslAdapter(
        clear: Boolean = false,
        reset: Boolean = enableAdapterRefresh,
        updateState: Boolean = true,
        action: DslAdapter.() -> Unit
    ) {
        finishRefresh()
        _adapter.render(updateState) {
            if (clear) {
                clearAllItems()
            } else if (reset) {
                dataItems.clear()
            }
            action()
        }
    }

    /**调用此方法, 更新界面*/
    open fun updateDslAdapter(update: SingleDataUpdate.() -> Unit) {
        _adapter.updateAdapter(update)
    }

    /**调用此方法, 更新所有item数据
     * 在静态界面中, 可能需要重新调用[renderDslAdapter]才能更新界面
     * */
    open fun updateAllDslAdapterItems(payload: Any? = DslAdapterItem.PAYLOAD_UPDATE_PART) {
        _adapter.updateAllItem(payload)
    }

    /**
     * 开始适配器刷新状态
     * [startRefresh]*/
    @UpdateByDiff
    fun startAdapterLoading() {
        _adapter.toLoading()
    }

    //<editor-fold desc="数据加载">

    /**页面请求相关辅助操作参数*/
    var page = Page()

    override fun onFragmentFirstShow(bundle: Bundle?) {
        super.onFragmentFirstShow(bundle)
        //触发加载中
        if (enableAdapterRefresh) {
            if (_adapter.dslAdapterStatusItem.itemState == DslAdapterStatusItem.ADAPTER_STATUS_NONE ||
                _adapter.dataItems.isEmpty()
            ) {
                _adapter.render(false) {
                    loadingStatus()
                }
            }
        }
    }

    override fun onRefresh(refreshContentBehavior: IRefreshContentBehavior?) {
        super.onRefresh(refreshContentBehavior)
        page.pageRefresh()
        onLoadData()
    }

    open fun onLoadMore() {
        page.pageLoadMore()
        onLoadData()
    }

    /**重写此方法, 拉取数据
     * [com.angcyo.core.fragment.BaseTitleFragment.finishRefresh]
     * [com.angcyo.dsladapter.toNone]*/
    @CallSuper
    @UiThread
    open fun onLoadData() {
        //因为使用的behavior实现的刷新, 所以fling操作之后, scroll并不一定就会停止
        _recycler.stopScroll()
        //finishRefresh()
        //_adapter.toNone()
    }

    /**数据加载完成后, 调用此方法*/
    @AnyThread
    fun <Item : DslAdapterItem, Bean> loadDataEnd(
        itemClass: Class<Item>,
        dataList: List<Bean>?,
        error: Throwable? = null,
        page: Page? = null,
        initItem: Item.(data: Bean) -> Unit = {}
    ) {
        loadDataEndIndex(itemClass, dataList, error, page) { data, _ ->
            initItem(data)
        }
    }

    @AnyThread
    fun <Item : DslAdapterItem, Bean> loadDataEnd(
        itemClass: KClass<Item>,
        dataList: List<Bean>?,
        error: Throwable? = null,
        page: Page? = null,
        initItem: Item.(data: Bean) -> Unit = {}
    ) {
        loadDataEnd(itemClass.java, dataList, error, page, initItem)
    }

    @AnyThread
    fun <Item : DslAdapterItem, Bean> loadDataEndIndex(
        itemClass: Class<Item>,
        dataList: List<Bean>?,
        error: Throwable? = null,
        page: Page? = null,
        initItem: Item.(data: Bean, index: Int) -> Unit = { _, _ -> }
    ) {
        finishRefresh()
        _adapter.loadDataEndIndex(itemClass, dataList, error, page ?: this.page) { data, index ->
            initItem(data, index)
        }
    }

    @AnyThread
    fun <Item : DslAdapterItem, Bean> loadDataEndIndex(
        itemClass: KClass<Item>,
        dataList: List<Bean>?,
        error: Throwable? = null,
        page: Page? = null,
        initItem: Item.(data: Bean, index: Int) -> Unit = { _, _ -> }
    ) {
        loadDataEndIndex(itemClass.java, dataList, error, page, initItem)
    }

    /**简单的加载多类型的item*/
    @AnyThread
    fun <T> resetRender(data: T?, error: Throwable? = null, render: DslAdapter.(data: T) -> Unit) {
        finishRefresh()
        _adapter.resetRender(data, error, page, render)
    }

    //</editor-fold desc="数据加载">

}