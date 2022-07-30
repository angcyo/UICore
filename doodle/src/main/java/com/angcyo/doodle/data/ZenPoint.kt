package com.angcyo.doodle.data

/**
 * 毛笔元素需要绘制的点
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2022/07/26
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
data class ZenPoint(
    /**手势的点[TouchPoint]*/
    val point: TouchPoint,
    /**当前点应该绘制的半径
     * [com.angcyo.doodle.data.BaseElementData.paintWidth]*/
    var paintWidth: Float = 0f,

    /**上行点的坐标, 中心点[point]半径[paintWidth]的圆上的点*/
    var upPointX: Float = 0f,
    var upPointY: Float = 0f,

    /**下行点的坐标, 中心点[point]半径[paintWidth]的圆上的点*/
    var downPointX: Float = 0f,
    var downPointY: Float = 0f,
)
