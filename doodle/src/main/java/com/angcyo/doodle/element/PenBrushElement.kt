package com.angcyo.doodle.element

import android.graphics.Canvas
import android.graphics.Paint
import com.angcyo.doodle.core.DoodleTouchManager
import com.angcyo.doodle.data.BrushElementData
import com.angcyo.doodle.data.TouchPoint
import com.angcyo.doodle.layer.BaseLayer
import com.angcyo.library.ex.bezier

/**
 * 钢笔绘制元素
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2022/07/25
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
class PenBrushElement(brushElementData: BrushElementData) : BaseBrushElement(brushElementData) {

    /**激活曲线*/
    var enableBezier: Boolean = true

    /**更新绘制元素*/
    override fun onUpdateElement(
        manager: DoodleTouchManager,
        pointList: List<TouchPoint>,
        point: TouchPoint
    ) {
        brushPath?.apply {
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

    override fun onDraw(layer: BaseLayer, canvas: Canvas) {
        brushPath?.let {
            paint.color = brushElementData.paintColor
            paint.strokeWidth = brushElementData.paintWidth
            paint.style = Paint.Style.STROKE
            canvas.drawPath(it, paint)
        }
    }
}