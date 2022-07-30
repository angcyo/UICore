package com.angcyo.doodle.brush

import com.angcyo.doodle.core.DoodleTouchManager
import com.angcyo.doodle.data.BrushElementData
import com.angcyo.doodle.data.TouchPoint
import com.angcyo.doodle.element.BaseBrushElement
import com.angcyo.doodle.element.DebugBrushElement

/**
 * 调试画笔
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2022/07/30
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
class DebugBrush : BaseBrush() {

    override fun onCreateBrushElement(
        manager: DoodleTouchManager,
        pointList: List<TouchPoint>
    ): BaseBrushElement? {
        return DebugBrushElement(BrushElementData(pointList))
    }

}