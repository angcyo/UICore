package com.angcyo.dsladapter.filter

import com.angcyo.dsladapter.DslAdapter
import com.angcyo.dsladapter.DslAdapterItem

/**
 * 排序过滤器
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/11/14
 */
class SortAfterFilterInterceptor<R : Comparable<R>>(
    /**正序/逆序*/
    val desc: Boolean = false,
    /**是否激活组件*/
    val enable: Boolean = true,
    /**排序依据字段*/
    val selector: (DslAdapterItem) -> R?
) : BaseFilterInterceptor() {

    override var isEnable: Boolean = enable

    /**安装排序过滤器*/
    fun install(adapter: DslAdapter?) {
        uninstall(adapter)
        adapter?.dslDataFilter?.afterFilterInterceptorList?.apply {
            add(this@SortAfterFilterInterceptor)
        }
    }

    /**卸载排序过滤器*/
    fun uninstall(adapter: DslAdapter?) {
        adapter?.dslDataFilter?.afterFilterInterceptorList?.apply {
            remove(this@SortAfterFilterInterceptor)
        }
    }

    override fun intercept(chain: FilterChain): List<DslAdapterItem> {
        val resultList = chain.requestList.toMutableList()
        if (desc) {
            //正序, 从大到小
            resultList.sortByDescending(selector)
        } else {
            //逆序, 从小到大
            resultList.sortBy(selector)
        }
        return resultList
    }
}