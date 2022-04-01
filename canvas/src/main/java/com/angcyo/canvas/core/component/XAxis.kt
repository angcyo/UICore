package com.angcyo.canvas.core.component

import com.angcyo.canvas.core.ViewBox

/**
 * X轴组件
 *
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/01
 */
class XAxis : BaseAxis() {

    override fun getLinePointList(viewBox: ViewBox): List<Float> {
        val result = mutableListOf<Float>()

        //开始绘制的起始坐标
        var startLeft = viewBox.getContentLeft()

        //默认, 每隔1mm绘制一个刻度
        val step = viewBox.convertValueToPixel(1f)

        while (startLeft < viewBox.getContentRight()) {
            result.add(startLeft)
            startLeft += step
        }

        return result
    }
}