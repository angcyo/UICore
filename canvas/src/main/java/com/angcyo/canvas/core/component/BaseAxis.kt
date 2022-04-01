package com.angcyo.canvas.core.component

import com.angcyo.canvas.core.ViewBox
import com.angcyo.library.ex.dp

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/01
 */
abstract class BaseAxis : BaseComponent() {

    /**轴的宽度*/
    var axisSize = 20 * dp

    /**刻度的大小*/
    var lineSize: Float = 4 * dp

    /**关键刻度的大小*/
    var lineProtrudeSize: Float = axisSize

    /**次要刻度的大小*/
    var lineSecondarySize: Float = axisSize / 2

    /**刻度文本和刻度之间的偏移距离*/
    var labelXOffset = 2 * dp

    var labelYOffset = 2 * dp

    /**获取轴上需要绘制线段的点位坐标, px*/
    abstract fun getLinePointList(viewBox: ViewBox): List<Float>

}