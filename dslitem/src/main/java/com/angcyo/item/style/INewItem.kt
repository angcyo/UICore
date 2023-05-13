package com.angcyo.item.style

import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.dsladapter.annotation.ItemInitEntryPoint
import com.angcyo.dsladapter.item.IDslItemConfig
import com.angcyo.item.R
import com.angcyo.library.ex.hawkGetBoolean
import com.angcyo.library.ex.hawkPut
import com.angcyo.widget.DslViewHolder

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/04/22
 */
interface INewItem : IAutoInitItem {

    /**配置类 */
    var newItemConfig: NewItemConfig

    /**初始化*/
    @ItemInitEntryPoint
    fun initNewItem(
        itemHolder: DslViewHolder,
        itemPosition: Int,
        adapterItem: DslAdapterItem,
        payloads: List<Any>
    ) {
        itemHolder.visible(newItemConfig.itemNewViewId, itemHaveNew)
    }
}

/**设置Key*/
var INewItem.itemNewHawkKeyStr: String?
    get() = newItemConfig.itemNewHawkKeyStr
    set(value) {
        newItemConfig.itemNewHawkKeyStr = value
    }

/**是否有new*/
var INewItem.itemHaveNew: Boolean
    get() = itemNewHawkKeyStr.hawkGetBoolean(newItemConfig.itemDefaultNew && !itemNewHawkKeyStr.isNullOrEmpty())
    set(value) {
        itemNewHawkKeyStr.hawkPut(value)
    }

open class NewItemConfig : IDslItemConfig {

    /**默认情况下, 是否有new*/
    var itemDefaultNew: Boolean = true

    /**[R.id.lib_new_view]*/
    var itemNewViewId: Int = R.id.lib_new_view

    /**用来判断是否有new的hawk key*/
    var itemNewHawkKeyStr: String? = null
}