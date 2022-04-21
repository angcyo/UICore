package com.angcyo.canvas.core.component

import com.angcyo.canvas.core.CanvasViewBox
import com.angcyo.canvas.utils.getScaleX
import kotlin.math.max

/**
 * X轴组件
 * [XAxisRenderer]
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/01
 */
class XAxis : BaseAxis() {

    override fun getPlusPixelList(canvasViewBox: CanvasViewBox): List<Float> {
        plusList.clear()
        var start = canvasViewBox.getCoordinateSystemX()
        val factor = max(1f, canvasViewBox.invertMatrix.getScaleX())
        val end =
            (start + canvasViewBox.getContentWidth() * factor - canvasViewBox.getTranslateX()) * factor
        val step = canvasViewBox.valueUnit.convertValueToPixel(1f)

        while (start < end) {
            plusList.add(start)
            start += step
        }
        return plusList
    }

    override fun getMinusPixelList(canvasViewBox: CanvasViewBox): List<Float> {
        minusList.clear()
        var start = canvasViewBox.getCoordinateSystemX()
        val factor = max(1f, canvasViewBox.invertMatrix.getScaleX())
        val end =
            (start - canvasViewBox.getContentWidth() - canvasViewBox.getTranslateX()) * factor
        val step = canvasViewBox.valueUnit.convertValueToPixel(1f)

        while (start > end) {
            minusList.add(start)
            start -= step
        }
        return minusList
    }
}