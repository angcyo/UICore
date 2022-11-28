package com.angcyo.canvas.core.component

import com.angcyo.library.annotation.Pixel
import com.angcyo.library.ex.have

/**
 * 坐标轴上的点信息
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2022/06/25
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
data class AxisPoint(

    /**坐标值, 像素. 相对于View的坐标*/
    @Pixel
    val pixel: Float,

    /**每个点之间的间隙*/
    @Pixel
    val gap: Double,

    /**[pixel]从0开始的索引*/
    val index: Int,

    /**点位类型
     * [LINE_TYPE_NONE]
     * [LINE_TYPE_NORMAL]
     * [LINE_TYPE_PROTRUDE]
     * [LINE_TYPE_SECONDARY]
     * [LINE_TYPE_DRAW_LABEL]
     * [LINE_TYPE_DRAW_GRID]
     * */
    val type: Int,
)

/**当前的点位是否有刻度*/
fun AxisPoint.haveRule(): Boolean {
    return type.have(BaseAxis.LINE_TYPE_NORMAL) ||
            type.have(BaseAxis.LINE_TYPE_SECONDARY) ||
            type.have(BaseAxis.LINE_TYPE_PROTRUDE)
}

/**是否是关键的刻度*/
fun AxisPoint.isMasterRule(): Boolean {
    return type.have(BaseAxis.LINE_TYPE_SECONDARY) ||
            type.have(BaseAxis.LINE_TYPE_PROTRUDE)
}
