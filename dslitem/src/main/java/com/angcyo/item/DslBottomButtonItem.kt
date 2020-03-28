package com.angcyo.item

import com.angcyo.widget.DslViewHolder

/**
 * 显示的[RecyclerView]底部的item
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/03/26
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
open class DslBottomButtonItem : DslButtonItem() {
    init {
        itemLayoutId = R.layout.dsl_bottom_button_item
        itemButtonStyle.themeStyle()
    }

    override fun _initItemSize(itemHolder: DslViewHolder) {
        //super._initItemSize(itemHolder)
        //RecyclerBottomLayout不支持调整item height
    }
}