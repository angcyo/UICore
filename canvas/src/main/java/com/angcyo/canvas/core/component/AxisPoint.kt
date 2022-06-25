package com.angcyo.canvas.core.component

/**
 * 坐标轴上的点信息
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2022/06/25
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
data class AxisPoint(

    /**坐标值, 像素*/
    var pixel: Float,

    /**[pixel]从0开始的索引*/
    var index: Int,

    /**点位类型
     * [LINE_TYPE_NONE]
     * [LINE_TYPE_NORMAL]
     * [LINE_TYPE_PROTRUDE]
     * [LINE_TYPE_SECONDARY]
     * [LINE_TYPE_DRAW_LABEL]
     * [LINE_TYPE_DRAW_GRID]
     * */
    var type: Int,
)
