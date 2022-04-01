package com.angcyo.canvas.core.component

import com.angcyo.canvas.core.CanvasViewBox

/**
 * Y轴组件
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/01
 */
class YAxis : BaseAxis() {
    override fun getLinePointList(canvasViewBox: CanvasViewBox): List<Float> {
        val result = mutableListOf<Float>()

        //开始绘制的起始坐标
        var startTop = canvasViewBox.getContentTop()

        //默认, 每隔1mm绘制一个刻度
        val step = canvasViewBox.convertValueToPixel(1f)

        while (startTop < canvasViewBox.getContentBottom()) {
            result.add(startTop)
            startTop += step
        }

        return result
    }
}