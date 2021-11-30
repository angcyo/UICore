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
import com.angcyo.dsladapter.toNone
import com.angcyo.item.R
import com.angcyo.item.style.ITextItem
import com.angcyo.item.style.itemText
import com.angcyo.library.ex.have
import com.angcyo.library.ex.highlight
import com.angcyo.library.ex.removeSpan
import com.angcyo.widget.base.onTextChange

/**
 * 搜索适配器关键字过滤item
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/11/30
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */

/**是否需要true, 表示item不被过滤*/
typealias FilterItem = (DslAdapterItem) -> Boolean

class SearchAdapterFilter {

    var filterItem: FilterItem? = null

    var _adapter: DslAdapter? = null

    val filterInterceptor: IFilterInterceptor = object : BaseFilterInterceptor() {
        override fun intercept(chain: FilterChain): List<DslAdapterItem> {
            return chain.requestList.filter { item ->
                if (item is ITextItem) {
                    item.itemUpdateFlag = true
                    item.itemText?.removeSpan(ForegroundColorSpan::class.java)
                    when {
                        filterText.isEmpty() -> true
                        item.itemText?.have(filterText) == true -> {
                            item.itemText = item.itemText?.highlight(filterText)
                            true
                        }
                        else -> filterItem?.invoke(item) ?: false
                    }
                } else {
                    filterItem?.invoke(item) ?: true
                }
            }
        }
    }

    /**初始化*/
    fun init(editText: EditText?, adapter: DslAdapter?, onFilterItem: FilterItem? = null) {
        _adapter?.dslDataFilter?.filterInterceptorList?.remove(filterInterceptor)

        _adapter = adapter
        filterItem = onFilterItem

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
    var filterText: String = ""

    /**开始过滤文本*/
    fun filter(text: CharSequence) {
        filterText = text.toString()

        _adapter?.apply {
            if (adapterItems.isNotEmpty()) {
                toNone()
            }
            updateItemDepend()
        }
    }
}

/**搜索过滤*/
fun searchAdapterFilter(
    editText: EditText?,
    adapter: DslAdapter?,
    onFilterItem: FilterItem? = null
): SearchAdapterFilter {
    val filter = SearchAdapterFilter()
    filter.init(editText, adapter, onFilterItem)
    return filter
}

fun BaseTitleFragment.goneSearchLayout(gone: Boolean = true) {
    _vh.gone(R.id.lib_search_root_layout, gone)
}

/**全部一次性初始化*/
fun BaseTitleFragment.initSearchAdapterFilter(
    searchLabel: CharSequence? = "请输入关键字",
    onFilterItem: FilterItem? = null
) {
    _vh.tv(R.id.lib_search_label_view)?.text = searchLabel
    _vh.ev(R.id.lib_search_edit_view)?.apply {
        doOnTextChanged { text, start, before, count ->
            _vh.visible(R.id.lib_search_wrap_layout, text.isNullOrEmpty())
        }
        searchAdapterFilter(this, _recycler.adapter as? DslAdapter, onFilterItem)
    }
}