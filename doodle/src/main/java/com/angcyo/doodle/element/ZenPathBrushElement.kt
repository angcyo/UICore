package com.angcyo.doodle.element

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PathMeasure
import com.angcyo.doodle.brush.BaseBrush
import com.angcyo.doodle.core.DoodleTouchManager
import com.angcyo.doodle.data.BrushElementData
import com.angcyo.doodle.data.TouchPoint
import com.angcyo.doodle.layer.BaseLayer
import com.angcyo.library.ex.before
import com.angcyo.library.ex.bezier
import com.angcyo.library.ex.size
import kotlin.math.absoluteValue

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/08/02
 */
class ZenPathBrushElement(brushElementData: BrushElementData) : BaseBrushElement(brushElementData) {

    val zenPathList = mutableListOf<ZenPath>()

    var lastMidX = 0f
    var lastMidY = 0f

    override fun onCreateElement(manager: DoodleTouchManager, pointList: List<TouchPoint>) {
        super.onCreateElement(manager, pointList)
        zenPathList.clear()
    }

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
            val zenPath = ZenPath()

            //
            val midX = (prevPoint.eventX + point.eventX) / 2
            val midY = (prevPoint.eventY + point.eventY) / 2

            if (pointList.size() == 2) {
                lastMidX = midX
                lastMidY = midY
            }

            //
            zenPath.path.moveTo(lastMidX, lastMidY)
            zenPath.path.bezier(prevPoint.eventX, prevPoint.eventY, midX, midY)
            zenPath.startWidth = selectPaintWidth(prevPoint.speed)
            if (point.isLast) {
                zenPath.endWidth = selectPaintWidth(prevPoint.speed)
            } else {
                zenPath.endWidth = selectPaintWidth(point.speed)
            }

            zenPathList.add(zenPath)

            //
            lastMidX = midX
            lastMidY = midY
        }
    }

    val dstPath = Path()

    override fun onDraw(layer: BaseLayer, canvas: Canvas) {
        zenPathList.forEach { zenPath ->
            paint.color = brushElementData.paintColor
            paint.style = Paint.Style.STROKE

            val pathMeasure = PathMeasure(zenPath.path, false)
            val length = pathMeasure.length
            var start = 0f
            var step = (zenPath.endWidth - zenPath.startWidth).absoluteValue
            var end = start + step

            while (end <= length) {
                dstPath.rewind()
                pathMeasure.getSegment(start, end, dstPath, true)
                val ratio = end / length
                paint.strokeWidth =
                    zenPath.startWidth + (zenPath.endWidth - zenPath.startWidth) * ratio

                canvas.drawPath(dstPath, paint)

                if (end + 0.1f > length) {
                    break
                }

                start = end
                end = start + step

                if (end > length) {
                    end = length
                }
            }
        }
    }

    data class ZenPath(
        //路径
        val path: Path = Path(),
        //路径开始的宽度和结束的宽度
        var startWidth: Float = 0f,
        var endWidth: Float = 0f
    )

}