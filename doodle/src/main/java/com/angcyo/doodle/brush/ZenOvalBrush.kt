package com.angcyo.doodle.brush

import com.angcyo.doodle.core.DoodleTouchManager
import com.angcyo.doodle.data.BrushElementData
import com.angcyo.doodle.data.TouchPoint
import com.angcyo.doodle.element.BaseBrushElement
import com.angcyo.doodle.element.ZenOvalBrushElement

/**
 * 毛笔笔刷手势数据收集
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2022-8-1
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
class ZenOvalBrush : BaseBrush() {

    override fun onCreateBrushElement(
        manager: DoodleTouchManager,
        pointList: List<TouchPoint>
    ): BaseBrushElement {
        return ZenOvalBrushElement(BrushElementData(pointList))
    }

}