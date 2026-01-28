package com.angcyo.doodle.ui.dslitem

import com.angcyo.doodle.R

/**
 * 涂鸦功能item, 用来互斥
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/08/19
 */
class DoodleFunItem : DoodleIconItem() {
    init {
        itemSingleSelectMutex = true
        itemLayoutId = R.layout.item_doodle_icon_layout2
    }
}