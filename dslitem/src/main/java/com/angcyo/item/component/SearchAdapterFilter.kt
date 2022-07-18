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
import com.angcyo.item.style.IDesItem
import com.angcyo.item.style.ITextItem
import com.angcyo.item.style.itemDes
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

/**是否需要过滤, true表示item不被过滤*/
typealias FilterItemAction = (DslAdapterItem) -> Boolean

class SearchAdapterFilter {

    /**额外的过滤条件判断回调
     * 返回true, 表示需要显示当前的Item
     * */
    var filterItemAction: FilterItemAction? = null

    var _adapter: DslAdapter? = null

    val filterInterceptor: IFilterInterceptor = object : BaseFilterInterceptor() {
        override fun intercept(chain: FilterChain): List<DslAdapterItem> {
            if (filterText.isEmpty()) {
                //无需要过滤的文本
                return chain.requestList
            }
            return chain.requestList.filter { item ->
                when (item) {
                    is ITextItem -> {
                        //ITextItem
                        item.itemUpdateFlag = true
                        item.itemText?.removeSpan(ForegroundColorSpan::class.java)
                        when {
                            item.itemText?.have(filterText) == true -> {
                                item.itemText = item.itemText?.highlight(filterText)
                                true
                            }
                            else -> filterItemAction?.invoke(item) ?: false
                        }
                    }
                    is IDesItem -> {
                        //IDesItem
                        item.itemUpdateFlag = true
                        item.itemDes?.removeSpan(ForegroundColorSpan::class.java)
                        when {
                            item.itemDes?.have(filterText) == true -> {
                                item.itemDes = item.itemDes?.highlight(filterText)
                                true
                            }
                            else -> filterItemAction?.invoke(item) ?: false
                        }
                    }
                    else -> {
                        filterItemAction?.invoke(item) ?: true
                    }
                }
            }
        }
    }

    /**初始化
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
    var filterText: String = ""

    /**开始过滤文本*/
    fun filter(text: CharSequence) {
        filterText = text.toString()

        _adapter?.apply {
            if (adapterItems.isNotEmpty()) {
                toNone()
            }
            //触发流程
            updateItemDepend()
        }
    }
}

/**搜索过滤*/
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