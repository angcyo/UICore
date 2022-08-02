package com.angcyo.doodle.brush

import android.graphics.Path
import com.angcyo.doodle.core.DoodleTouchManager
import com.angcyo.doodle.data.BrushElementData
import com.angcyo.doodle.data.TouchPoint
import com.angcyo.doodle.element.BaseBrushElement
import com.angcyo.doodle.element.ZenPathBrushElement
import com.angcyo.library.ex.before
import com.angcyo.library.ex.bezier
import com.angcyo.library.ex.eachPath
import com.angcyo.library.ex.size

/** 在曲线路径上, 绘制无数个半径不等的圆, 达到笔锋效果
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/08/02
 */
class ZenPathBrush : BaseBrush() {

    /**路径采样率, 值越小画出来的线越细腻*/
    var pathSampleStep = 0.8f

    override fun onCreateBrushElement(
        manager: DoodleTouchManager,
        pointList: List<TouchPoint>
    ): BaseBrushElement {
        return ZenPathBrushElement(BrushElementData())
    }

    var _lastMidX = 0f
    var _lastMidY = 0f

    val _tempPath = Path()

    override fun onUpdateBrushElement(
        manager: DoodleTouchManager,
        pointList: List<TouchPoint>,
        point: TouchPoint
    ) {
        computeLastPointSpeed(pointList)
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

            brushElement?.brushElementData?.brushPath?.apply {
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

}