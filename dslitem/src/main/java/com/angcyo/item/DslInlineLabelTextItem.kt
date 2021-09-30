package com.angcyo.item

import com.angcyo.item.style.ITextItem
import com.angcyo.item.style.TextItemConfig

/**
 * 内嵌的标签+文本的item
 * 左标签, 又文本
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/09/30
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
class DslInlineLabelTextItem : DslBaseLabelItem(), ITextItem {

    override var textItemConfig: TextItemConfig = TextItemConfig()

    init {
        itemLayoutId = R.layout.dsl_inline_label_text
    }
}