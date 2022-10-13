package com.angcyo.doodle.brush

import android.graphics.Path
import com.angcyo.doodle.core.DoodleTouchManager
import com.angcyo.doodle.data.TouchPoint
import com.angcyo.doodle.info.PathSampleInfo
import com.angcyo.library.ex.before
import com.angcyo.library.ex.bezier
import com.angcyo.library.ex.eachPath
import com.angcyo.library.ex.size
import kotlin.math.min

/**
 * 二阶贝塞尔曲线路径采样数据收集器
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/08/12
 */
abstract class BasePathSampleBrush : BaseBrush() {

    /**路径采样率, 值越小画出来的线越细腻
     * 高端手机5f效果就很好
     * 中端手机3f
     * 低端手机1f
     * */
    var pathSampleStep = 3f

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

            //开始采样
            //L.i(_tempPath.length())
            val step = min(pathSampleStep, manager.doodleDelegate.doodleConfig.paintWidth)
            _tempPath.eachPath(step) { index, ratio, contourIndex, posArray ->
                val width = startWidth + (endWidth - startWidth) * ratio
                val pathSampleInfo = PathSampleInfo(posArray[0], posArray[1], width)
                onPathSample(pathSampleInfo)
                //L.i(posArray)
            }

            //
            _lastMidX = midX
            _lastMidY = midY
        }
    }

    /**采样到的数据点*/
    open fun onPathSample(pathSampleInfo: PathSampleInfo) {

    }
}