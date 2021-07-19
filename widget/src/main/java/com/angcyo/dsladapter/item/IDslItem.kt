package com.angcyo.dsladapter.item

import androidx.recyclerview.widget.LinearLayoutManager
import com.angcyo.dsladapter.DslAdapterItem

/**
 * 空实现, 方便查找实现类
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/07/16
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
interface IDslItem {

    /**是否是占满宽度的item*/
    fun isFullWidthItem(item: DslAdapterItem): Boolean {
        if (item.itemSpanCount == DslAdapterItem.FULL_ITEM) {
            return true
        }
        if (item.itemDslAdapter?._recyclerView?.layoutManager is LinearLayoutManager) {
            return true
        }
        return false
    }

}