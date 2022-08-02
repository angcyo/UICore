package com.angcyo.doodle.brush

import com.angcyo.doodle.core.DoodleTouchManager
import com.angcyo.doodle.data.BrushElementData
import com.angcyo.doodle.data.TouchPoint
import com.angcyo.doodle.element.BaseBrushElement
import com.angcyo.doodle.element.ZenPathBrushElement

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/08/02
 */
class ZenPathBrush : BaseBrush() {

    override fun onCreateBrushElement(
        manager: DoodleTouchManager,
        pointList: List<TouchPoint>
    ): BaseBrushElement {
        return ZenPathBrushElement(BrushElementData(pointList))
    }

}