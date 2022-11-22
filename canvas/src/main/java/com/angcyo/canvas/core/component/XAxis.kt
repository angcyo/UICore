package com.angcyo.canvas.core.component

import com.angcyo.canvas.core.CanvasViewBox
import com.angcyo.library.ex.getScaleX
import com.angcyo.library.ex.have

/**
 * X轴组件
 * [XAxisRenderer]
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/01
 */
class XAxis : BaseAxis() {

    override fun getPlusPixelList(canvasViewBox: CanvasViewBox): List<AxisPoint> {
        plusList.clear()
        val coordinateSystemX = canvasViewBox.getCoordinateSystemX()
        var pixel = coordinateSystemX //获取刻度开始的像素位置
        val factor = canvasViewBox.invertMatrix.getScaleX() //如果放大了, 需要扩大的因子
        val end =
            (coordinateSystemX + canvasViewBox.getContentWidth() - canvasViewBox.getTranslateX()) * factor //获取刻度结束的像素位置
        val step = canvasViewBox.valueUnit.getGraduatedScaleGap().toFloat() //刻度的间隔

        val scaleX = canvasViewBox.getScaleX()
        var index = 0
        while (pixel < end) {
            val type = getAxisLineType(canvasViewBox, index, scaleX)
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
        val coordinateSystemX = canvasViewBox.getCoordinateSystemX()
        var pixel = coordinateSystemX
        val factor = canvasViewBox.invertMatrix.getScaleX()
        val end = (0 - canvasViewBox.getTranslateX()) * factor
        val step = canvasViewBox.valueUnit.getGraduatedScaleGap().toFloat()

        val scaleX = canvasViewBox.getScaleX()
        var index = 0
        while (pixel > end) {
            val type = getAxisLineType(canvasViewBox, index, scaleX)
            if (type.have(LINE_TYPE_DRAW_GRID)) {
                minusList.add(AxisPoint(pixel, index, type))
            }
            pixel -= step
            index++
        }
        return minusList
    }
}