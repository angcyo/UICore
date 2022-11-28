package com.angcyo.canvas.core.component

import com.angcyo.canvas.core.CanvasViewBox
import com.angcyo.library.ex.getScaleY
import com.angcyo.library.ex.have

/**
 * Y轴组件
 * [YAxisRenderer]
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/01
 */
class YAxis : BaseAxis() {

    override fun getPlusPixelList(canvasViewBox: CanvasViewBox): List<AxisPoint> {
        plusList.clear()
        val scaleY = canvasViewBox.getScaleY()
        val coordinateSystemY = canvasViewBox.getCoordinateSystemY()
        var pixel = coordinateSystemY
        val factor = canvasViewBox.invertMatrix.getScaleY()
        val end =
            (coordinateSystemY + canvasViewBox.getContentHeight() - canvasViewBox.getTranslateY()) * factor
        val step = canvasViewBox.valueUnit.getGraduatedScaleGap(scaleY)

        var index = 0
        while (pixel < end) {
            val type = getAxisLineType(canvasViewBox, index, scaleY)
            if (type.have(LINE_TYPE_DRAW_GRID)) {
                plusList.add(AxisPoint(pixel, step, index, type))
            }
            pixel += step.toFloat()
            index++
        }
        return plusList
    }

    override fun getMinusPixelList(canvasViewBox: CanvasViewBox): List<AxisPoint> {
        minusList.clear()
        val scaleY = canvasViewBox.getScaleY()
        val coordinateSystemY = canvasViewBox.getCoordinateSystemY()
        var pixel = coordinateSystemY
        val factor = canvasViewBox.invertMatrix.getScaleY()
        val end = (0 - canvasViewBox.getTranslateY()) * factor
        val step = canvasViewBox.valueUnit.getGraduatedScaleGap(scaleY)

        var index = 0
        while (pixel > end) {
            val type = getAxisLineType(canvasViewBox, index, scaleY)
            if (type.have(LINE_TYPE_DRAW_GRID)) {
                minusList.add(AxisPoint(pixel, step, index, type))
            }
            pixel -= step.toFloat()
            index++
        }
        return minusList
    }
}