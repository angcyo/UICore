package com.angcyo.doodle.element

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import com.angcyo.doodle.brush.BaseBrush
import com.angcyo.doodle.core.DoodleTouchManager
import com.angcyo.doodle.data.BrushElementData
import com.angcyo.doodle.data.TouchPoint
import com.angcyo.doodle.layer.BaseLayer
import com.angcyo.library.ex.before
import com.angcyo.library.ex.bezier
import com.angcyo.library.ex.eachPath
import com.angcyo.library.ex.size

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/08/02
 */
class ZenPathBrushElement(brushElementData: BrushElementData) : BaseBrushElement(brushElementData) {

    /**路径采样率, 值越小画出来的线越细腻*/
    var pathSampleStep = 0.5f

    override fun onCreateElement(manager: DoodleTouchManager, pointList: List<TouchPoint>) {
        super.onCreateElement(manager, pointList)
    }

    var _lastMidX = 0f
    var _lastMidY = 0f

    val _tempPath = Path()

    override fun onUpdateElement(
        manager: DoodleTouchManager,
        pointList: List<TouchPoint>,
        point: TouchPoint
    ) {
        super.onUpdateElement(manager, pointList, point)
        BaseBrush.computeLastPointSpeed(pointList)
        if (!point.isFirst) {
            //
            val prevPoint = pointList.before(point)!!
            _tempPath.rewind()

            //
            val midX = (prevPoint.eventX + point.eventX) / 2
            val midY = (prevPoint.eventY + point.eventY) / 2

            if (pointList.size() == 2) {
                _lastMidX = midX
                _lastMidY = midY
            }

            //
            _tempPath.moveTo(_lastMidX, _lastMidY)
            _tempPath.bezier(prevPoint.eventX, prevPoint.eventY, midX, midY)
            val startWidth = selectPaintWidth(prevPoint.speed)
            val endWidth = if (point.isLast) {
                selectPaintWidth(prevPoint.speed)
            } else {
                selectPaintWidth(point.speed)
            }

            brushPath?.apply {
                _tempPath.eachPath(pathSampleStep) { index, ratio, posArray ->
                    val width = startWidth + (endWidth - startWidth) * ratio
                    addCircle(posArray[0], posArray[1], width, Path.Direction.CW)
                }
            }

            //
            _lastMidX = midX
            _lastMidY = midY
        }
    }

    override fun onDraw(layer: BaseLayer, canvas: Canvas) {
        brushPath?.apply {
            paint.color = brushElementData.paintColor
            paint.style = Paint.Style.FILL
            canvas.drawPath(this, paint)
        }
    }
}