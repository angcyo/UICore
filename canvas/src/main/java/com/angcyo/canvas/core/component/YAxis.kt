package com.angcyo.canvas.core.component

import com.angcyo.canvas.core.CanvasViewBox
import com.angcyo.canvas.utils.getScaleY
import kotlin.math.max

/**
 * Y轴组件
 * [YAxisRenderer]
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/01
 */
class YAxis : BaseAxis() {

    override fun getPlusPixelList(canvasViewBox: CanvasViewBox): List<Float> {
        plusList.clear()
        var start = canvasViewBox.getCoordinateSystemY()
        canvasViewBox.matrix.invert(_tempMatrix)
        val factor = max(1f, _tempMatrix.getScaleY())
        val end =
            (start + canvasViewBox.getContentHeight() * factor - canvasViewBox._translateY) * factor
        val step = canvasViewBox.valueUnit.convertValueToPixel(1f)

        while (start < end) {
            plusList.add(start)
            start += step
        }
        return plusList
    }

    override fun getMinusPixelList(canvasViewBox: CanvasViewBox): List<Float> {
        minusList.clear()
        var start = canvasViewBox.getCoordinateSystemY()
        canvasViewBox.matrix.invert(_tempMatrix)
        val factor = max(1f, _tempMatrix.getScaleY())
        val end =
            (start - canvasViewBox.getContentHeight() - canvasViewBox._translateY) * factor
        val step = canvasViewBox.valueUnit.convertValueToPixel(1f)

        while (start > end) {
            minusList.add(start)
            start -= step
        }
        return minusList
    }
}