package com.angcyo.canvas.core.component

import com.angcyo.canvas.core.CanvasViewBox
import com.angcyo.canvas.utils.getTranslateX

/**
 * X轴组件
 *
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/01
 */
class XAxis : BaseAxis() {

    override fun getLinePointList(canvasViewBox: CanvasViewBox): List<Float> {
        val result = mutableListOf<Float>()

        //可视区域移动的距离
        val translateX = canvasViewBox.matrix.getTranslateX()

        //线的坐标绘制范围
        val minLeft = canvasViewBox.getContentLeft() - translateX
        val maxRight = canvasViewBox.getContentRight() - translateX

        //开始绘制的起始坐标
        var startLeft = minLeft

        //默认, 每隔1mm绘制一个刻度
        val step = canvasViewBox.convertValueToPixel(1f)

        //反方向的点/正方向的点
        while (startLeft < maxRight) {
            result.add(startLeft)
            startLeft += step
        }

        return result
    }
}