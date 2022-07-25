package com.angcyo.doodle.brush

import com.angcyo.doodle.brush.element.PenElement
import com.angcyo.doodle.element.BaseElement

/**
 * 钢笔画刷
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2022/07/25
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
class PenBrush : BaseBrush() {

    override fun onCreateBrushElement(): BaseElement? {
        return collectPointList?.run {
            PenElement(this)
        }
    }
}