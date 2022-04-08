package com.angcyo.canvas.core.component

import com.angcyo.canvas.core.CanvasViewBox
import com.angcyo.canvas.utils._tempMatrix
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
        val result = mutableListOf<Float>()
        var start = canvasViewBox.getCoordinateSystemX()
        canvasViewBox.matrix.invert(_tempMatrix)
        val factor = max(1f, _tempMatrix.getScaleX())
        val end =
            (start + canvasViewBox.getContentWidth() * factor - canvasViewBox._translateX) * factor
        val step = canvasViewBox.valueUnit.convertValueToPixel(1f)

        while (start < end) {
            result.add(start)
            start += step
        }
        return result
    }

    override fun getMinusPixelList(canvasViewBox: CanvasViewBox): List<Float> {
        val result = mutableListOf<Float>()
        var start = canvasViewBox.getCoordinateSystemX()
        canvasViewBox.matrix.invert(_tempMatrix)
        val factor = max(1f, _tempMatrix.getScaleX())
        val end =
            (start - canvasViewBox.getContentWidth() - canvasViewBox._translateX) * factor
        val step = canvasViewBox.valueUnit.convertValueToPixel(1f)

        while (start > end) {
            result.add(start)
            start -= step
        }
        return result
    }
}