package com.angcyo.doodle.brush

import com.angcyo.doodle.core.DoodleTouchManager
import com.angcyo.doodle.data.BrushElementData
import com.angcyo.doodle.data.TouchPoint
import com.angcyo.doodle.element.BaseBrushElement
import com.angcyo.doodle.element.EraserBrushElement

/**
 * 橡皮擦路径收集
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2022/08/02
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
class EraserBrush : PenBrush() {

    init {
        enableBezier = true
    }

    override fun onCreateBrushElement(
        manager: DoodleTouchManager,
        pointList: List<TouchPoint>
    ): BaseBrushElement {
        return EraserBrushElement(BrushElementData())
    }
}