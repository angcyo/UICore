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

    /**是否需要再路径结束之后, 添加Z
     * [closeSvg]*/
    var needClosePath: Boolean = false

    init {
        unit = null
    }

    override fun onPathStart() {
        super.onPathStart()
        //viewBox ?
    }

    override fun onPathEnd() {
        super.onPathEnd()
        if (needClosePath) {
            closeSvg() //need?
        }
    }

    override fun onNewPoint(x: Double, y: Double) {
        writer?.append("M${x},$y")
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
                    val a1 = VectorHelper.angle(circle.x, circle.y, first.x, first.y)
                    val a2 = VectorHelper.angle(circle.x, circle.y, point.x, point.y)

                    //L.w("a1:${a1} a2:${a2} 1:${first} 2:${point}")

                    //弧线的方向，0 表示从起点到终点沿逆时针画弧，1 表示从起点到终点沿顺时针画弧。
                    val sweepFlag = if (first.circleDir == Path.Direction.CW) 1 else 0

                    var a3 = a2 - a1
                    if (a3 < 0) {
                        a3 += 360
                    }
                    //决定弧线是大于还是小于 180 度, 0 表示小角度弧，1 表示大角度弧
                    val largeArcFlag = if (first.circleDir == Path.Direction.CW) {
                        //顺时针枚举点
                        if (a3 > 180) 1 else 0
                    } else {
                        //逆时针枚举点
                        if (a3 > 180) 0 else 1
                    }
                    append(buildString {
                        append("A${r.toLossyFloat().toValueString()},")
                        append("${r.toLossyFloat().toValueString()},")
                        append("0,$largeArcFlag,$sweepFlag,")
                        append("${xValue.toLossyFloat().toValueString()},")
                        append(yValue.toLossyFloat().toValueString())
                    })
                })
            }
        } else {
            super.onLineToPoint(point)
        }
    }

    override fun onLineToPoint(x: Double, y: Double) {
        //super.onLineToPoint(x, y)
        writer?.append("L${x.toValueString()},${y.toValueString()}")
    }

    /**关闭路径*/
    fun closeSvg() {
        writer?.append("Z")
    }
}