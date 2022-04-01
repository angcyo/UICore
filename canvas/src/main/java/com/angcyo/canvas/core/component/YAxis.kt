package com.angcyo.canvas.core.component

import com.angcyo.canvas.core.ViewBox

/**
 * Y轴组件
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/01
 */
class YAxis : BaseAxis() {
    override fun getLinePointList(viewBox: ViewBox): List<Float> {
        val result = mutableListOf<Float>()

        //开始绘制的起始坐标
        var startTop = viewBox.getContentTop()

        //默认, 每隔1mm绘制一个刻度
        val step = viewBox.convertValueToPixel(1f)

        while (startTop < viewBox.getContentBottom()) {
            result.add(startTop)
            startTop += step
        }

        return result
    }
}