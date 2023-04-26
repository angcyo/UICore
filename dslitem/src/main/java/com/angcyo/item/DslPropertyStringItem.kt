package com.angcyo.item

import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.item.style.EditItemConfig
import com.angcyo.item.style.IEditItem
import com.angcyo.library.ex._string

/**
 * 文本属性
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/11/10
 */
open class DslPropertyStringItem : DslBasePropertyItem(), IEditItem {

    override var editItemConfig: EditItemConfig = EditItemConfig().apply {
        itemEditTextStyle.hint = _string(R.string.dialog_input_hint)
        itemEditTextStyle.editMaxInputLength = 100//最大字符
    }

    init {
        itemLayoutId = R.layout.dsl_property_string_item
    }

    override fun onItemChangeListener(item: DslAdapterItem) {
        //super.onItemChangeListener(item)
        //updateItemOnHaveDepend()
    }
}