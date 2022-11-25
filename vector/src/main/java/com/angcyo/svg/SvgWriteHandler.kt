package com.angcyo.svg

import android.graphics.Path
import com.angcyo.library.ex.toLossyFloat
import com.angcyo.vector.VectorHelper
import com.angcyo.vector.VectorWriteHandler
import kotlin.math.absoluteValue

/**
 * 将[Path]输出成[svg]path数据
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2022/10/13
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
class SvgWriteHandler : VectorWriteHandler() {

    override fun onPathStart() {
        super.onPathStart()
        //viewBox ?
    }

    override fun onPathEnd() {
        super.onPathEnd()
        //closeSvg() //need?
    }

    override fun onNewPoint(x: Double, y: Double) {
        writer?.append("M${x} $y")
    }

    override fun onLineToPoint(point: VectorPoint) {
        if (point.pointType == POINT_TYPE_CIRCLE) {
            //原
            val first = _pointList.firstOrNull()
            val circle = first?.circle
            if (first == null || circle == null) {
                super.onLineToPoint(point)
            } else {
                //A 支持
                val r = VectorHelper.spacing(circle.x, circle.y, first.x, first.y).absoluteValue

                val xValue = point.x
                val yValue = point.y

                writer?.append(buildString {
                    val a1 = VectorHelper.angle3(circle.x, circle.y, first.x, first.y)
                    val a2 = VectorHelper.angle3(circle.x, circle.y, point.x, point.y)

                    //弧线的方向，0 表示从起点到终点沿逆时针画弧，1 表示从起点到终点沿顺时针画弧。
                    val sweepFlag = if (first.circleDir == Path.Direction.CW) 1 else 0

                    var a3 = 0
                    //决定弧线是大于还是小于 180 度, 0 表示小角度弧，1 表示大角度弧
                    val largeArcFlag = if (first.circleDir == Path.Direction.CW) {
                        //顺时针枚举点
                        a3 = if (a2 <= a1) {
                            a2 - a1 + 360
                        } else {
                            a2 - a1
                        }
                        if (a3 > 180) 1 else 0
                    } else {
                        //逆时针枚举点
                        a3 = if (a2 <= a1) {
                            a1 - a2 + 360
                        } else {
                            a1 - a2
                        }
                        if (a3 > 180) 1 else 0
                    }
                    append("A ${r.toLossyFloat()} ${r.toLossyFloat()} 0 $largeArcFlag $sweepFlag ${xValue.toLossyFloat()} ${yValue.toLossyFloat()}")
                })
            }
        } else {
            super.onLineToPoint(point)
        }
    }

    override fun onLineToPoint(x: Double, y: Double) {
        //super.onLineToPoint(x, y)
        writer?.append("L${x} $y")
    }

    /**关闭路径*/
    fun closeSvg() {
        writer?.append("Z")
    }
}