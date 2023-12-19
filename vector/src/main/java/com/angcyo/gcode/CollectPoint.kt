package com.angcyo.gcode

import android.graphics.PointF

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/10/23
 */
data class CollectPoint(
    /**
     * 这条线段上的关键点集合
     * 首 + 折点 + 折点 + 折点 + ... + 尾
     * */
    val pointList: MutableList<PointF> = mutableListOf(),
)