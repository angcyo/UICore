package com.angcyo.doodle.brush

import android.graphics.Path
import com.angcyo.doodle.core.DoodleTouchManager
import com.angcyo.doodle.data.BrushPath
import com.angcyo.doodle.data.PathBrushElementData
import com.angcyo.doodle.data.TouchPoint
import com.angcyo.doodle.element.BaseBrushElement
import com.angcyo.doodle.element.ZenPathBrushElement
import com.angcyo.library.ex.before
import com.angcyo.library.ex.bezier
import com.angcyo.library.ex.eachSegment
import com.angcyo.library.ex.size

/**
 * [ZenCircleBrush]
 *
 * 通过在[Path]上, 无限取宽度不一样的小[Path]实现笔锋
 *
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/08/11
 */

@Deprecated("效果可以, 但是性能较差")
class ZenPathBrush : BaseBrush() {

    /**路径采样率, 值越小画出来的线越细腻*/
    var pathSampleStep = 10f

    override fun onCreateBrushElement(
        manager: DoodleTouchManager,
        pointList: List<TouchPoint>
    ): BaseBrushElement {
        return ZenPathBrushElement(PathBrushElementData())
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

            _tempPath.eachSegment(pathSampleStep) { index, ratio, path ->
                val width = startWidth + (endWidth - startWidth) * ratio
                (brushElement as? ZenPathBrushElement)?.pathBrushElementData?.listPath?.add(BrushPath().apply {
                    set(path)
                    strokeWidth = width
                })
            }

            //
            _lastMidX = midX
            _lastMidY = midY
        }
    }

}