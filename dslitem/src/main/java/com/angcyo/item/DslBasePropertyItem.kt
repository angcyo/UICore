package com.angcyo.item

import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.item.style.DesItemConfig
import com.angcyo.item.style.IDesItem
import com.angcyo.item.style.ILabelItem
import com.angcyo.item.style.LabelItemConfig

/**
 * 属性item 基类
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/06/08
 */
open class DslBasePropertyItem : DslAdapterItem(), ILabelItem, IDesItem {

    /**属性标签
     * [itemLabelText]*/
    override var labelItemConfig: LabelItemConfig = LabelItemConfig()

    /**属性描述
     * [itemDes]*/
    override var desItemConfig: DesItemConfig = DesItemConfig()

    init {
        itemLayoutId = R.layout.dsl_base_property_item

        configDesStyle {
            goneOnTextEmpty = true
        }
    }

}