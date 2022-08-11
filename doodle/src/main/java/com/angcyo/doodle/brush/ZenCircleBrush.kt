package com.angcyo.doodle.brush

import android.graphics.Path
import com.angcyo.doodle.core.DoodleTouchManager
import com.angcyo.doodle.data.BrushElementData
import com.angcyo.doodle.data.TouchPoint
import com.angcyo.doodle.element.BaseBrushElement
import com.angcyo.doodle.element.ZenCircleBrushElement
import com.angcyo.library.ex.before
import com.angcyo.library.ex.bezier
import com.angcyo.library.ex.eachPath
import com.angcyo.library.ex.size

/** 在曲线路径上, 绘制无数个半径不等的圆, 达到笔锋效果
 *
 * 通过无限多的[addCircle]实现的笔锋效果, 性能可能差一点
 *
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/08/02
 */
class ZenCircleBrush : BaseBrush() {

    /**路径采样率, 值越小画出来的线越细腻
     * 高端手机5f效果就很好
     * 中端手机3f
     * 低端手机1f
     * */
    var pathSampleStep = 3f

    override fun onCreateBrushElement(
        manager: DoodleTouchManager,
        pointList: List<TouchPoint>
    ): BaseBrushElement {
        return ZenCircleBrushElement(BrushElementData())
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
                    addCircle(posArray[0], posArray[1], width / 2, Path.Direction.CW)
                }
            }

            //
            _lastMidX = midX
            _lastMidY = midY
        }
    }

}