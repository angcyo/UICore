package com.angcyo.item

import com.angcyo.item.style.ISwitchItem
import com.angcyo.item.style.SwitchItemConfig

/**
 * 开关属性控制item
 *
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/06/08
 */
open class DslPropertySwitchItem : DslBasePropertyItem(), ISwitchItem {

    override var switchItemConfig: SwitchItemConfig = SwitchItemConfig()

    init {
        itemLayoutId = R.layout.dsl_property_switch_item

        switchItemConfig.itemSwitchChangedAction = {
            onItemSwitchChanged(it)
        }
    }

    /**提供一个可以被重写的子类方法*/
    open fun onItemSwitchChanged(checked: Boolean) {

    }
}