package com.angcyo.item

import com.angcyo.item.style.DesItemConfig
import com.angcyo.item.style.IDesItem

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @date 2025/04/07
 *
 * 垂直排列的[DslLabelTextItem]
 */
open class DslLabelTextVerticalItem : DslLabelTextItem(), IDesItem {

    /**属性描述
     * [itemDes]*/
    override var desItemConfig: DesItemConfig = DesItemConfig().apply {
        itemDesStyle.goneOnTextEmpty = true
    }

    init {
        itemLayoutId = R.layout.dsl_label_text_vertical_item
    }
}