package com.angcyo.item.component

import android.text.style.ForegroundColorSpan
import android.widget.EditText
import androidx.core.widget.doOnTextChanged
import com.angcyo.core.fragment.BaseTitleFragment
import com.angcyo.dsladapter.DslAdapter
import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.dsladapter.filter.BaseFilterInterceptor
import com.angcyo.dsladapter.filter.FilterChain
import com.angcyo.dsladapter.filter.IFilterInterceptor
import com.angcyo.dsladapter.noneStatus
import com.angcyo.item.R
import com.angcyo.item.style.IDesItem
import com.angcyo.item.style.ITextItem
import com.angcyo.item.style.itemDes
import com.angcyo.item.style.itemText
import com.angcyo.library.ex.have
import com.angcyo.library.ex.highlight
import com.angcyo.library.ex.removeSpan
import com.angcyo.library.ex.resetAll
import com.angcyo.library.extend.IFilterItem
import com.angcyo.library.extend.IToText
import com.angcyo.widget.base.onTextChange

/**
 * 搜索适配器关键字过滤item
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/11/30
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */

/**是否需要过滤, true表示item不被过滤*/
typealias FilterItemAction = (DslAdapterItem) -> Boolean

class SearchAdapterFilter {

    companion object {

        /**过滤[list]
         * [withHide] 是否使用隐藏的方式进行过滤处理*/
        fun filterItemList(
            list: List<DslAdapterItem>,
            filterText: String,
            filterItemAction: FilterItemAction? = null, /*返回true, 表示需要当前的item*/
            withHide: Boolean = false
        ): List<DslAdapterItem> {
            return list.filter { item ->
                var need = false
                when (item) {
                    is ITextItem -> {
                        //ITextItem
                        item.itemUpdateFlag = true
                        item.itemText?.removeSpan(ForegroundColorSpan::class.java)
                        if (item.itemText?.have(filterText) == true) {
                            item.itemText = item.itemText?.highlight(filterText)
                            need = true
                        }
                    }

                    is IDesItem -> {
                        //IDesItem
                        item.itemUpdateFlag = true
                        item.itemDes?.removeSpan(ForegroundColorSpan::class.java)
                        if (item.itemDes?.have(filterText) == true) {
                            item.itemDes = item.itemDes?.highlight(filterText)
                            need = true
                        }
                    }

                    is IToText -> {
                        //IToText
                        val itemText = item.toText()
                        itemText?.removeSpan(ForegroundColorSpan::class.java)
                        if (itemText?.have(filterText) == true) {
                            need = true
                        }
                    }

                    is IFilterItem -> {
                        //IFilterItem
                        if (item.containsFilterText(filterText)) {
                            need = true
                        }
                    }
                }
                //result
                need = if (need) true else filterItemAction?.invoke(item) ?: false
                if (withHide) {
                    item.itemHidden = !need
                    true
                } else {
                    need
                }
            }
        }
    }

    /**额外的过滤条件判断回调, 用来过滤[DslAdapterItem]
     * 返回true, 表示需要显示当前的Item
     * */
    var filterItemAction: FilterItemAction? = null

    var _adapter: DslAdapter? = null

    /**过滤拦截器, 满足过滤条件的[DslAdapterItem]会被过滤, 不会出现在列表中*/
    val filterInterceptor: IFilterInterceptor = object : BaseFilterInterceptor() {
        override fun intercept(chain: FilterChain): List<DslAdapterItem> {
            val text = filterText
            if (text.isNullOrEmpty()) {
                //无需要过滤的文本
                return chain.requestList
            }
            return filterItemList(chain.requestList, text, filterItemAction)
        }
    }

    /**普通初始化, 需要过滤时, 请主动调用[filter]方法
     * [com.angcyo.item.component.SearchAdapterFilter.filter]
     * */
    fun init(adapter: DslAdapter?, filterItemAction: FilterItemAction? = null) {
        init(null, adapter, filterItemAction)
    }

    /**使用[EditText]初始化
     * [editText] 输入框, 会自动监听文本框的改变
     * [filterItemAction]额外的过滤回调, true表示需要显示对应的item
     * */
    fun init(
        editText: EditText?,
        adapter: DslAdapter?,
        filterItemAction: FilterItemAction? = null
    ) {
        _adapter?.dslDataFilter?.filterInterceptorList?.remove(filterInterceptor)

        _adapter = adapter
        this.filterItemAction = filterItemAction

        //监听
        editText?.apply {
            onTextChange(shakeDelay = 160) {
                filter(it)
            }
        }

        //过滤
        _adapter?.dslDataFilter?.filterInterceptorList?.apply {
            if (!contains(filterInterceptor)) {
                add(filterInterceptor)
            }
        }
    }

    /**当前过滤的文本*/
    var filterText: String? = null

    /**开始过滤文本*/
    fun filter(text: CharSequence?) {
        filterText = text?.toString()

        _adapter?.apply {
            if (adapterItems.isNotEmpty()) {
                //toNone()
                noneStatus()
            }
            //触发流程
            updateItemDepend()
        }
    }
}

/**直接过滤现有的[DslAdapterItem]
 * [useFilterList] 是否使用过滤后的数据, true:多次过滤会无效
 * */
fun DslAdapter.filterItem(
    filterText: String,
    useFilterList: Boolean = false,
    filterItemAction: FilterItemAction? = null,
    withHide: Boolean = true
) {
    val list = SearchAdapterFilter.filterItemList(
        if (withHide) dataItems else getDataList(useFilterList),
        filterText,
        filterItemAction,
        withHide
    )
    changeDataItems {
        it.resetAll(list)
    }
}

/**快速获取一个[SearchAdapterFilter]*/
fun searchAdapterFilter(
    adapter: DslAdapter?,
    onFilterItemAction: FilterItemAction? = null
) = searchAdapterFilter(null, adapter, onFilterItemAction)

/**搜索过滤
 * [SearchAdapterFilter]*/
fun searchAdapterFilter(
    editText: EditText?,
    adapter: DslAdapter?,
    onFilterItemAction: FilterItemAction? = null
): SearchAdapterFilter {
    val filter = SearchAdapterFilter()
    filter.init(editText, adapter, onFilterItemAction)
    return filter
}

fun BaseTitleFragment.goneSearchLayout(gone: Boolean = true) {
    _vh.gone(R.id.lib_search_root_layout, gone)
}

/**全部一次性初始化*/
fun BaseTitleFragment.initSearchAdapterFilter(
    searchLabel: CharSequence? = "请输入关键字",
    onFilterItemAction: FilterItemAction? = null
) {
    _vh.tv(R.id.lib_search_label_view)?.text = searchLabel
    _vh.ev(R.id.lib_search_edit_view)?.apply {
        doOnTextChanged { text, start, before, count ->
            _vh.visible(R.id.lib_search_wrap_layout, text.isNullOrEmpty())
        }
        searchAdapterFilter(this, _recycler.adapter as? DslAdapter, onFilterItemAction)
    }
}