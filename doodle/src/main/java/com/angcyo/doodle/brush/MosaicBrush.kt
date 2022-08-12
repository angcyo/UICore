package com.angcyo.doodle.brush

import com.angcyo.doodle.core.DoodleTouchManager
import com.angcyo.doodle.data.BrushElementData
import com.angcyo.doodle.data.TouchPoint
import com.angcyo.doodle.element.BaseBrushElement
import com.angcyo.doodle.element.MosaicBrushElement

/**
 * 马赛克路径收集
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/08/12
 */
class MosaicBrush : PenBrush() {

    init {
        enableBezier = true
    }

    override fun onCreateBrushElement(
        manager: DoodleTouchManager,
        pointList: List<TouchPoint>
    ): BaseBrushElement {
        return MosaicBrushElement(BrushElementData())
    }
}