package com.angcyo.canvas.utils

import android.graphics.Path
import com.angcyo.canvas.LinePath
import com.angcyo.canvas.LovePath
import com.angcyo.library.unit.MmValueUnit
import com.angcyo.canvas.items.PictureShapeItem
import com.angcyo.library.ex.toRadians
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin


/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/29
 */

// region ===========================================

object ShapesHelper {

    val defaultWidth: Float
        get() = MmValueUnit().convertValueToPixel(PictureShapeItem.SHAPE_DEFAULT_WIDTH)

    val defaultHeight: Float
        get() = MmValueUnit().convertValueToPixel(PictureShapeItem.SHAPE_DEFAULT_HEIGHT)

    /**多边形的Path*/
    fun polygonPath(
        number: Int,
        width: Float = defaultWidth,
        height: Float = defaultHeight
    ): Path {
        if (number < 3) {
            error("至少需要是3正三边形")
        }
        val originX = width / 2
        val originY = height / 2
        val angleSum = (number - 2) * 180 //内角和
        val angleOne = angleSum / number //每条边的角度
        val a = (angleOne / 2f).toRadians()  //半条边的弧度

        /*val minR = min(originX, originY) //内圆半径
        val maxR = minR / sin(a) //外圆半径*/

        //val c = 2 * minR / tan(a) //每条边的边长

        val maxR = min(originX, originY)

        val path = Path()

        //底部左边为起点
        val startRadians = Math.PI - a    //开始绘制的弧度

        for (i in 0..number) {
            val radians = startRadians + Math.PI * 2 / number * i  //弧度
            val nextX = originX + maxR * cos(radians)
            val nextY = originY + maxR * sin(radians)
            if (i == 0) {
                path.moveTo(nextX.toFloat(), nextY.toFloat())
            } else {
                path.lineTo(nextX.toFloat(), nextY.toFloat())
            }
        }
        path.close()

        /*for (i in 0..number) {
            // - 0.5 : Turn 90 ° counterclockwise
            val radians = (2f / number * i - 0.5) * Math.PI //弧度
            val nextX = originX + r * cos(radians)
            val nextY = originY + r * sin(radians)
            if (i == 0) {
                path.moveTo(nextX.toFloat(), nextY.toFloat())
            } else {
                path.lineTo(nextX.toFloat(), nextY.toFloat())
            }
        }*/

        return path
    }

    /**画线*/
    fun linePath(length: Float = defaultWidth): Path = LinePath().apply {
        initPath(length)
    }

    /**圆形Path
     * [diameter] 直径*/
    fun circlePath(diameter: Float = defaultWidth): Path = Path().apply {
        val x = diameter / 2
        val y = x
        val radius = x
        addCircle(x, y, radius, Path.Direction.CW)
    }

    /**三角形Path*/
    fun trianglePath(width: Float = defaultWidth, height: Float = defaultHeight): Path =
        polygonPath(3, width, height)
    /*Path().apply {
        moveTo(0f, height)
        lineTo(width / 2, 0f)
        lineTo(width, height)
        lineTo(0f, height)
        close()
    }*/

    /**正方形Path*/
    fun squarePath(width: Float = defaultWidth, height: Float = defaultHeight): Path =
        Path().apply {
            moveTo(0f, 0f)
            lineTo(width, 0f)
            lineTo(width, height)
            lineTo(0f, height)
            close()
        }

    /**五角形Path*/
    fun pentagonPath(width: Float = defaultWidth, height: Float = defaultHeight): Path =
        polygonPath(5, width, height)

    /**六角形Path*/
    fun hexagonPath(width: Float = defaultWidth, height: Float = defaultHeight): Path =
        polygonPath(6, width, height)

    /**八角形Path*/
    fun octagonPath(width: Float = defaultWidth, height: Float = defaultHeight): Path =
        polygonPath(8, width, height)

    /**菱形Path*/
    fun rhombusPath(width: Float = defaultWidth * 3 / 4, height: Float = defaultHeight): Path =
        Path().apply {
            moveTo(width / 2f, height)
            lineTo(0f, height / 2f)
            lineTo(width / 2f, 0f)
            lineTo(width, height / 2f)
            lineTo(width / 2f, height)
            close()
        }

    /**星星Path
     * [number] 角的数量*/
    fun pentagramPath(
        number: Int = 5,
        width: Float = defaultWidth,
        height: Float = defaultHeight
    ): Path =
        Path().apply {

            val r = min(width, height) / 4 //五角星内圆的半径
            val R = min(width, height) / 2 //五角星外圆的半径

            val originX = width / 2
            val originY = height / 2

            val startRadians = Math.PI / 2 //开始绘制的弧度

            //从底部中心开始的角度
            val step = 360f / (number * 2)
            for (i in 0..(number * 2)) {
                val radians = startRadians + (step * i).toRadians()
                val nextX: Double
                val nextY: Double
                if (i % 2 == 0) {
                    //内圆
                    nextX = originX + r * cos(radians)
                    nextY = originY + r * sin(radians)
                } else {
                    //外圆
                    nextX = originX + R * cos(radians)
                    nextY = originY + R * sin(radians)
                }
                if (i == 0) {
                    moveTo(nextX.toFloat(), nextY.toFloat())
                } else {
                    lineTo(nextX.toFloat(), nextY.toFloat())
                }
            }
            close()
        }

    /**心形Path*/
    fun lovePath(width: Float = defaultWidth, height: Float = defaultHeight): Path =
        LovePath().apply {

            /*val r = width / 4
            val R = width * 3 / 4

            val x1 = r
            val y1 = r

            val x2 = R
            val y2 = r

            val rect = emptyRectF()
            moveTo(0f, r)

            val offsetAngle = 10f
            rect.set(0f, 0f, r * 2, r * 2)
            arcTo(rect, 180f, 180f - offsetAngle)
            rect.set(r * 2, 0f, r * 4, r * 2)
            arcTo(rect, 180f + offsetAngle, 180f)

            val sweepAngle = acos(r / R).toDegrees()
            rect.set(x1 - R, y1 - R, x1 + R, y1 + R)
            arcTo(rect, 0f, sweepAngle)

            rect.set(x2 - R, y2 - R, x2 + R, y2 + R)
            arcTo(rect, 180f - sweepAngle, sweepAngle)
            close()*/

            /*val w2 = width / 2f
            val h2 = height / 2f

            val c = sqrt(w2 * w2 + h2 * h2)
            val r = c / 2

            val a = atan(h2 / w2)

            val x1 = cos(a) * r
            val y1 = sin(a) * r //h2 - sqrt(r * r + x1 * x1)

            val x2 = w2 * 3 / 2
            val y2 = y1

            moveTo(w2, height)
            lineTo(0f, h2)
            val rect = emptyRectF()
            rect.set(x1 - r, y1 - r, x1 + r, y1 + r)
            val angle = 90f + 90f - atan(h2 / w2).toDegrees()
            arcTo(rect, angle, 180f)

            rect.set(x2 - r, y2 - r, x2 + r, y2 + r)
            arcTo(rect, -angle, 180f)

            lineTo(w2, height)
            close()*/

            initPath(width, height)
        }
}

// region ===========================================
