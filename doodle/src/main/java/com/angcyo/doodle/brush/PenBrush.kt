package com.angcyo.doodle.brush

import com.angcyo.doodle.core.DoodleTouchManager
import com.angcyo.doodle.data.BrushElementData
import com.angcyo.doodle.data.TouchPoint
import com.angcyo.doodle.element.BaseBrushElement
import com.angcyo.doodle.element.PenBrushElement
import com.angcyo.library.ex.bezier

/**
 * 钢笔画刷, 手势收集
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2022/07/25
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
open class PenBrush : BaseBrush() {

    /**激活曲线*/
    var enableBezier: Boolean = true

    override fun onCreateBrushElement(
        manager: DoodleTouchManager,
        pointList: List<TouchPoint>
    ): BaseBrushElement {
        return PenBrushElement(BrushElementData())
    }

    override fun onUpdateBrushElement(
        manager: DoodleTouchManager,
        pointList: List<TouchPoint>,
        point: TouchPoint
    ) {
        brushElement?.brushElementData?.brushPath?.apply {
            if (point.isFirst) {
                moveTo(point.eventX, point.eventY)
            } else {
                if (enableBezier) {
                    val prevPoint = pointList[pointList.lastIndex - 1]
                    val midX = (prevPoint.eventX + point.eventX) / 2
                    val midY = (prevPoint.eventY + point.eventY) / 2
                    //bezier(c1x, c1y, point.eventX, point.eventY)
                    bezier(prevPoint.eventX, prevPoint.eventY, midX, midY)
                } else {
                    lineTo(point.eventX, point.eventY)
                }
            }
        }
    }
}