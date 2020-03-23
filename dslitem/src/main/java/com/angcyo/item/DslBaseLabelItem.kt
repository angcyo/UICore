package com.angcyo.item

import android.view.Gravity
import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.widget.DslViewHolder

/**
 * 带有Label的item
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/03/23
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */

open class DslBaseLabelItem : DslAdapterItem() {

    /**左边的Label文本*/
    var itemLabelText: CharSequence? = null

    var itemLabelGravity: Int = Gravity.LEFT or Gravity.CENTER_VERTICAL

    init {
        itemLayoutId = R.layout.dsl_label_item
    }

    override fun onItemBind(
        itemHolder: DslViewHolder,
        itemPosition: Int,
        adapterItem: DslAdapterItem,
        payloads: List<Any>
    ) {
        super.onItemBind(itemHolder, itemPosition, adapterItem, payloads)

        itemHolder.tv(R.id.lib_label_view)?.apply {
            text = itemLabelText
            gravity = itemLabelGravity
        }
    }
}