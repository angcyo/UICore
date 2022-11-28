package com.angcyo.vector

import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import com.angcyo.library.ex.abs
import com.angcyo.library.model.PointD
import com.angcyo.svg.StylePath
import com.pixplicity.sharp.Sharp
import kotlin.math.*

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
    fun spacing(x1: Double, y1: Double, x2: Double, y2: Double): Double {
        val x = x2 - x1
        val y = y2 - y1
        return sqrt(x * x + y * y)
    }

    fun spacing(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        val x = x2 - x1
        val y = y2 - y1
        return sqrt(x * x + y * y)
    }

    fun spacing(p1: PointF, p2: PointF): Float {
        return spacing(p1.x, p1.y, p2.x, p2.y)
    }

    fun spacing(p1: PointD, p2: PointD): Double {
        return spacing(p1.x, p1.y, p2.x, p2.y)
    }

    /**获取2个点之间的角度, 非弧度
     * [0~180°] [0~-180°]
     * https://blog.csdn.net/weixin_38351681/article/details/115512792
     *
     * 返回的是安卓绘制坐标系
     * [0~-90]    点2 在第一象限
     * [-90~-180] 点2 在第二象限
     * [90~180]   点2 在第三象限
     * [0~90]     点2 在第四象限
     * */
    fun angle(x1: Double, y1: Double, x2: Double, y2: Double): Double {
        return 180.0 / Math.PI * atan2((y2 - y1), (x2 - x1))
    }
    /*fun angle(y1: Float, x1: Float, y2: Float, x2: Float): Float {
        return Math.toDegrees(
            atan2(y1.toDouble(), x1.toDouble()) -
                    atan2(y2.toDouble(), x2.toDouble())
        ).toFloat() % 360
    }*/

    /**视图坐标系中的角度
     * [0~360°]*/
    fun angle2(x1: Double, y1: Double, x2: Double, y2: Double): Double {
        val degrees = angle(x1, y1, x2, y2)
        if (degrees < 0) {
            return 360 + degrees
        }
        return degrees
    }

    /**
     * 获取两条线的夹角, 笛卡尔坐标系. x右+, y上+
     * @param centerX
     * @param centerY
     * @param x
     * @param y
     * @return [0~360]]
     */
    fun angle3(centerX: Double, centerY: Double, x: Double, y: Double): Double {
        var rotation = 0.0
        val k1 = (centerY - centerY) / (centerX * 2 - centerX)
        val k2 = (y - centerY) / (x - centerX)
        val tmpDegree = atan(abs(k1 - k2) / (1 + k1 * k2)) / Math.PI * 180
        if (x > centerX && y < centerY) {
            //第一象限
            rotation = 90 - tmpDegree
        } else if (x > centerX && y > centerY) {
            //第二象限
            rotation = 90 + tmpDegree
        } else if (x < centerX && y > centerY) {
            //第三象限
            rotation = 270 - tmpDegree
        } else if (x < centerX && y < centerY) {
            //第四象限
            rotation = 270 + tmpDegree
        } else if (x == centerX && y < centerY) {
            rotation = 0.0
        } else if (x == centerX && y > centerY) {
            rotation = 180.0
        }
        return rotation
    }

    fun angle3(centerX: Float, centerY: Float, x: Float, y: Float): Double =
        angle3(centerX.toDouble(), centerY.toDouble(), x.toDouble(), y.toDouble())

    fun angle(p1: PointF, p2: PointF): Double {
        return angle(p1.x.toDouble(), p1.y.toDouble(), p2.x.toDouble(), p2.y.toDouble())
    }

    /**判断2个点是否是想要横向平移
     * 否则就是纵向平移*/
    fun isHorizontalIntent(x1: Float, y1: Float, x2: Float, y2: Float): Boolean {
        return (x2 - x1).abs() < (y2 - y1).abs()
    }

    fun isHorizontalIntent(p1: PointF, p2: PointF): Boolean {
        return isHorizontalIntent(p1.x, p1.y, p2.x, p2.y)
    }

    /**3个点, 求圆心
     * https://www.cnblogs.com/jason-star/archive/2013/04/22/3036130.html
     * https://stackoverflow.com/questions/4103405/what-is-the-algorithm-for-finding-the-center-of-a-circle-from-three-points
     * */
    fun centerOfCircle(
        x1: Double,
        y1: Double,
        x2: Double,
        y2: Double,
        x3: Double,
        y3: Double
    ): PointD? {
        val tempA1 = x1 - x2
        val tempA2 = x3 - x2
        val tempB1 = y1 - y2
        val tempB2 = y3 - y2
        val tempC1 = (x1.pow(2.0) - x2.pow(2.0) + y1.pow(2.0) - y2.pow(2.0)) / 2
        val tempC2 = (x3.pow(2.0) - x2.pow(2.0) + y3.pow(2.0) - y2.pow(2.0)) / 2
        val temp = tempA1 * tempB2 - tempA2 * tempB1
        return if (temp == 0.0) {
            null
        } else {
            PointD(
                (tempC1 * tempB2 - tempC2 * tempB1) / temp,
                (tempA1 * tempC2 - tempA2 * tempC1) / temp
            )
        }
    }
}

/**[android.graphics.Paint.Style]*/
fun Path.pathStyle() = if (this is StylePath) {
    this.pathStyle
} else {
    Paint.Style.STROKE
}

/**SVG 字符数据 转 Path
 * M250,150L150,350L350,350Z,*/
fun String.toPath(): Path = Sharp.loadPath(this)