package com.angcyo.item

import com.angcyo.item.style.itemSelectIndexChangeAction

/**
 * 块状颜色分段的[DslSegmentTabItem]
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/09/27
 */
open class DslSegmentSolidTabItem : DslSegmentTabItem() {

    init {
        itemLayoutId = R.layout.dsl_segment_solid_tab_item

        //选项列表
        itemSegmentList

        //回调监听
        itemSelectIndexChangeAction
    }

}