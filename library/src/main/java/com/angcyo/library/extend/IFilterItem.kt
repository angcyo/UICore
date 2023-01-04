package com.angcyo.library.extend

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/01/04
 */
interface IFilterItem {

    /**当前数据是否包含*/
    fun containsFilterText(text: CharSequence): Boolean
}