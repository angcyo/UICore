package com.angcyo.vector

import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Point
import android.graphics.PointF
import android.graphics.RectF
import android.os.Build
import com.angcyo.library.annotation.Pixel
import com.angcyo.library.component.pool.acquireTempPath
import com.angcyo.library.component.pool.acquireTempRectF
import com.angcyo.library.component.pool.release
import com.angcyo.library.ex.abs
import com.angcyo.library.ex.c
import com.angcyo.library.ex.computePathBounds
import com.angcyo.library.ex.isRotated
import com.angcyo.library.model.PointD
import com.angcyo.svg.StylePath
import com.pixplicity.sharp.Sharp
import java.lang.Math.pow
import kotlin.math.abs
import kotlin.math.atan
import kotlin.math.atan2
import kotlin.math.pow
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

    /**[angle]*/
    fun angle(y1: Float, x1: Float, y2: Float, x2: Float): Float {
        //2个点求角度
        val angle = atan2((y2 - y1).toDouble(), (x2 - x1).toDouble()) * 180 / Math.PI
        return angle.toFloat()
    }

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

    /**获取指定的点在那个象限
     * [origin] 原点
     * [point] 指定的点
     * https://zh.wikipedia.org/wiki/%E8%B1%A1%E9%99%90%E8%A7%92 */
    fun quadrant(origin: PointF, point: PointF): Int {
        if (point.x > origin.x) {
            return if (point.y > origin.y) {
                //第四象限
                4
            } else {
                //第一象限
                1
            }
        } else {
            return if (point.y > origin.y) {
                //第三象限
                3
            } else {
                //第二象限
                2
            }
        }
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

    /**2个点, 1个半径, 求圆心*/
    fun centerOfCircle(
        x1: Double,
        y1: Double,
        x2: Double,
        y2: Double,
        r: Double
    ): PointD? {
        // 求出中点坐标
        val centerX = (x1 + x2) / 2
        val centerY = (y1 + y2) / 2

        // 求出向量d1和d2
        val dx1 = x1 - x2
        val dy1 = y1 - y2

        val dx2 = -y1 + y2
        val dy2 = x1 - x2

        if (dx1 == 0.0 && dy1 == 0.0) {
            return null
        }

        // 求出向量d1和d2的长度
        val d1 = sqrt(dx1 * dx1 + dy1 * dy1)
        val d2 = r * d1 / sqrt(dx1 * dx1 + dy1 * dy1)
        // 求出单位向量U
        val u1 = dx2 / d2
        val u2 = dy2 / d2
        // 求出圆心坐标
        val originX = centerX + r * u1
        val originY = centerY + r * u2
        return PointD(originX, originY)
    }

    /**[centerOfCircle]*/
    fun centerOfCircle2(x1: Float, y1: Float, x2: Float, y2: Float, r: Float): Array<Point>? {
        val midX = (x1 + x2) / 2
        val midY = (y1 + y2) / 2
        val distance = sqrt((x2 - x1).toDouble().pow(2.0) + (y2 - y1).toDouble().pow(2.0)).toFloat()
        if (distance > r * 2) {
            // 两点之间的距离大于直径，不存在圆心
            return null
        }
        val offsetX = (y2 - y1) * sqrt(
            pow(r.toDouble(), 2.0) - pow(
                (distance / 2).toDouble(),
                2.0
            )
        ).toFloat() / distance
        val offsetY = (x2 - x1) * sqrt(
            pow(r.toDouble(), 2.0) - pow(
                (distance / 2).toDouble(),
                2.0
            )
        ).toFloat() / distance
        val centerX1 = midX + offsetX
        val centerY1 = midY + offsetY
        val centerX2 = midX - offsetX
        val centerY2 = midY - offsetY
        val d1 = sqrt(
            pow((centerX1 - x1).toDouble(), 2.0) + pow(
                (centerY1 - y1).toDouble(),
                2.0
            )
        ).toFloat()
        val d2 = sqrt(
            pow((centerX1 - x2).toDouble(), 2.0) + pow(
                (centerY1 - y2).toDouble(),
                2.0
            )
        ).toFloat()
        return if (abs(d1 - r) < 0.01 && abs(d2 - r) < 0.01) {
            arrayOf(Point(centerX1.toInt(), centerY1.toInt()))
        } else {
            arrayOf(
                Point(centerX1.toInt(), centerY1.toInt()),
                Point(centerX2.toInt(), centerY2.toInt())
            )
        }
    }

    /**将[pathList]使用扫描碰撞的方式,生成用来填充的新的[pathList]
     *
     * [fillPathStep] 填充间距
     * [fillAngle] 填充线的旋转角度
     * [pathFillType] 填充类型
     *
     * [VectorWriteHandler.PATH_FILL_TYPE_RECT]
     * [VectorWriteHandler.PATH_FILL_TYPE_CIRCLE]
     * */
    fun pathFill(
        pathList: List<Path>?,
        @Pixel fillPathStep: Float = 1f,
        fillAngle: Float = 0f,
        pathFillType: Int = VectorWriteHandler.PATH_FILL_TYPE_RECT
    ): List<Path>? {
        pathList ?: return null
        val result = mutableListOf<Path>()
        val targetPathList = mutableListOf<Path>()

        //能够完全包含path的矩形
        val pathBounds = acquireTempRectF()
        //先将目标反向旋转
        var targetMatrix: Matrix? = null
        //再将结果正向旋转
        var resultMatrix: Matrix? = null

        if (fillAngle.isRotated()) {
            targetMatrix = Matrix()
            resultMatrix = Matrix()
            pathList.computePathBounds(pathBounds)
            targetMatrix.setRotate(-fillAngle, pathBounds.centerX(), pathBounds.centerY())
            resultMatrix.setRotate(fillAngle, pathBounds.centerX(), pathBounds.centerY())

            //先将目标反向旋转
            for (path in pathList) {
                val targetPath = Path(path)
                targetPath.transform(targetMatrix)
                targetPathList.add(targetPath)
            }
        } else {
            targetPathList.addAll(pathList)
        }
        targetPathList.computePathBounds(pathBounds)

        //---开始扫描---

        for (path in targetPathList) {
            val fillPathList = pathFill(path, pathBounds, resultMatrix, fillPathStep, pathFillType)
            fillPathList?.let {
                result.addAll(it)
            }
        }

        return result
    }

    /**扫描填充一个[path]路径*/
    private fun pathFill(
        path: Path?,
        pathBounds: RectF,
        resultMatrix: Matrix?,
        @Pixel fillPathStep: Float = 1f,
        pathFillType: Int = VectorWriteHandler.PATH_FILL_TYPE_RECT
    ): List<Path>? {
        val targetPath = path ?: return null
        //矩形由上往下扫描, 取与path的交集
        val scanStep = fillPathStep //扫描步长

        var y = pathBounds.top + scanStep
        var endY = pathBounds.bottom

        val centerX = pathBounds.centerX()
        val centerY = pathBounds.centerY()

        val scanPath = acquireTempPath() //扫描形状

        if (pathFillType == VectorWriteHandler.PATH_FILL_TYPE_CIRCLE) {
            //使用圆的方式填充, 则y是当前扫描的半径, endY是最大扫描半径
            y = scanStep
            endY = c(pathBounds.width() / 2, pathBounds.height() / 2).toFloat()
        }

        var isFirst = true
        val result = mutableListOf<Path>()

        while (y <= endY) {
            //逐行扫描
            scanPath.rewind()
            val resultPath = Path() //碰撞结果

            //一行
            //这里用CCW出来的就是顺时针
            if (pathFillType == VectorWriteHandler.PATH_FILL_TYPE_CIRCLE) {
                //圆形碰撞扫描
                scanPath.addCircle(centerX, centerY, y, Path.Direction.CCW)
            } else {
                scanPath.addRect(
                    pathBounds.left,
                    y,
                    pathBounds.right,
                    y + scanStep,
                    Path.Direction.CCW //ccw无效?
                )
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT &&
                resultPath.op(scanPath, targetPath, Path.Op.INTERSECT)
            ) {
                //操作成功
                if (!resultPath.isEmpty) {
                    //有交集数据, 写入GCode数据
                    resultMatrix?.let {
                        resultPath.transform(it)
                    }
                    result.add(resultPath)
                    isFirst = false
                }
            }

            if (y == endY) {
                break
            }

            //
            y += if (pathFillType == VectorWriteHandler.PATH_FILL_TYPE_CIRCLE) {
                scanStep * 2
            } else {
                scanStep * 2
            }

            //
            if (y > endY) {
                //y = endY //填充下, 丢弃. 防止重复雕刻
                break
            }
        }

        scanPath.rewind()
        scanPath.release()

        return result
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