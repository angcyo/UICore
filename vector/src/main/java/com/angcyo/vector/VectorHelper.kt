package com.angcyo.vector

import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import com.angcyo.library.ex.abs
import com.angcyo.svg.StylePath
import kotlin.math.atan2
import kotlin.math.sqrt

/**
 * 矢量助手工具类
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2022/09/05
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
object VectorHelper {

    /**获取2个点的中点坐标*/
    fun midPoint(x1: Float, y1: Float, x2: Float, y2: Float, result: PointF) {
        result.x = (x1 + x2) / 2f
        result.y = (y1 + y2) / 2f
    }

    fun midPoint(p1: PointF, p2: PointF, result: PointF) {
        midPoint(p1.x, p1.y, p2.x, p2.y, result)
    }

    /**获取2个点之间的距离, 勾股定律*/
    fun spacing(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        val x: Float = x2 - x1
        val y: Float = y2 - y1
        return sqrt((x * x + y * y).toDouble()).toFloat()
    }

    fun spacing(p1: PointF, p2: PointF): Float {
        return spacing(p1.x, p1.y, p2.x, p2.y)
    }

    /**获取2个点之间的角度, 非弧度
     * [0~180°] [0~-180°]
     * https://blog.csdn.net/weixin_38351681/article/details/115512792
     *
     * [0~-90]    点2 在第一象限
     * [-90~-180] 点2 在第二象限
     * [90~180]   点2 在第三象限
     * [0~90]     点2 在第四象限
     * */
    fun angle(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        val degrees = 180.0 / Math.PI * atan2((y2 - y1), (x2 - x1))
        return degrees.toFloat()
    }

    /*fun angle(y1: Float, x1: Float, y2: Float, x2: Float): Float {
        return Math.toDegrees(
            atan2(y1.toDouble(), x1.toDouble()) -
                    atan2(y2.toDouble(), x2.toDouble())
        ).toFloat() % 360
    }*/

    /**视图坐标系中的角度
     * [0~360°]*/
    fun angle2(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        val degrees = angle(x1, y1, x2, y2)
        if (degrees < 0) {
            return 360 + degrees
        }
        return degrees
    }

    fun angle(p1: PointF, p2: PointF): Float {
        return angle(p1.x, p1.y, p2.x, p2.y)
    }

    /**判断2个点是否是想要横向平移
     * 否则就是纵向平移*/
    fun isHorizontalIntent(x1: Float, y1: Float, x2: Float, y2: Float): Boolean {
        return (x2 - x1).abs() < (y2 - y1).abs()
    }

    fun isHorizontalIntent(p1: PointF, p2: PointF): Boolean {
        return isHorizontalIntent(p1.x, p1.y, p2.x, p2.y)
    }

}

/**[android.graphics.Paint.Style]*/
fun Path.pathStyle() = if (this is StylePath) {
    this.pathStyle
} else {
    Paint.Style.STROKE
}