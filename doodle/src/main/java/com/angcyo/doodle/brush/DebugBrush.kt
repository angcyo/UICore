package com.angcyo.doodle.brush

import android.graphics.Path
import com.angcyo.doodle.core.DoodleTouchManager
import com.angcyo.doodle.data.TouchPoint
import com.angcyo.doodle.element.BaseBrushElement
import com.angcyo.doodle.element.DebugBrushElement
import com.angcyo.library.ex.textHeight
import com.angcyo.library.ex.textWidth

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
        return DebugBrushElement(DebugBrushElement.DebugBrushElementData())
    }

    override fun onUpdateBrushElement(
        manager: DoodleTouchManager,
        pointList: List<TouchPoint>,
        point: TouchPoint
    ) {
        computeLastPointSpeed(pointList)
        (brushElement as? DebugBrushElement)?.apply {
            brushElementData.brushPath?.apply {
                addCircle(
                    point.eventX,
                    point.eventY,
                    brushElementData.paintWidth,
                    Path.Direction.CW
                )
            }
            brushData.debugPointList.add(DebugBrushElement.DebugPoint(point).apply {
                text = "${point.angle}/${point.speed}"
                textDrawX = point.eventX - paint.textWidth(text) / 2
                textDrawY = point.eventY - paint.textHeight()
            })
        }
    }

}