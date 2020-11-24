package com.angcyo.dsladapter.internal

import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.dsladapter.filter.BaseFilterInterceptor
import com.angcyo.dsladapter.filter.FilterChain

/**
 *
 * 加载更多数据拦截器
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/02/05
 */
class LoadMoreFilterInterceptor : BaseFilterInterceptor() {

    /**总是激活加载更多, 会覆盖[dslLoadMoreItem]的状态*/
    var alwaysEnable = false

    override fun intercept(chain: FilterChain): List<DslAdapterItem> {
        if (chain.dslAdapter.isAdapterStatus()) {
            return chain.requestList
        }

        val dslLoadMoreItem = chain.dslAdapter.dslLoadMoreItem
        if (alwaysEnable) {
            val oldState = dslLoadMoreItem.itemState
            dslLoadMoreItem.itemStateEnable = true
            dslLoadMoreItem.itemState = oldState
        }

        if (!dslLoadMoreItem.itemStateEnable) {
            return chain.requestList
        }

        val result = mutableListOf<DslAdapterItem>()
        result.addAll(chain.requestList)
        result.add(dslLoadMoreItem)
        return result
    }
}