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
        var pixel = canvasViewBox.getCoordinateSystemY()
        val factor = canvasViewBox.invertMatrix.getScaleY()
        val end = (canvasViewBox.getContentHeight() - canvasViewBox.getTranslateY()) * factor
        val step = canvasViewBox.valueUnit.getGraduatedScaleGap().toFloat()

        val scaleY = canvasViewBox.getScaleY()
        var index = 0
        while (pixel < end) {
            val type = getAxisLineType(canvasViewBox, index, scaleY)
            if (type.have(LINE_TYPE_DRAW_GRID)) {
                plusList.add(AxisPoint(pixel, index, type))
            }
            pixel += step
            index++
        }
        return plusList
    }

    override fun getMinusPixelList(canvasViewBox: CanvasViewBox): List<AxisPoint> {
        minusList.clear()
        var pixel = canvasViewBox.getCoordinateSystemY()
        val factor = canvasViewBox.invertMatrix.getScaleY()
        val end = (0 - canvasViewBox.getTranslateY()) * factor
        val step = canvasViewBox.valueUnit.getGraduatedScaleGap().toFloat()

        val scaleY = canvasViewBox.getScaleY()
        var index = 0
        while (pixel > end) {
            val type = getAxisLineType(canvasViewBox, index, scaleY)
            if (type.have(LINE_TYPE_DRAW_GRID)) {
                minusList.add(AxisPoint(pixel, index, type))
            }
            pixel -= step
            index++
        }
        return minusList
    }
}