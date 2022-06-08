package com.angcyo.item

import com.angcyo.item.style.ISwitchItem
import com.angcyo.item.style.SwitchItemConfig

/**
 * [com.angcyo.github.SwitchButton] 开关item
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/08/09
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
open class DslSwitchInfoItem : DslBaseInfoItem(), ISwitchItem {

    override var switchItemConfig: SwitchItemConfig = SwitchItemConfig()

    init {
        itemExtendLayoutId = R.layout.dsl_extent_switch_item
        switchItemConfig.itemSwitchChangedAction = {
            onItemSwitchChanged(it)
        }
    }

    /**提供一个可以被重写的子类方法*/
    open fun onItemSwitchChanged(checked: Boolean) {

    }

}