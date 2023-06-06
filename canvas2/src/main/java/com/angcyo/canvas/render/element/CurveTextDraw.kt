package com.angcyo.canvas.render.element

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import androidx.core.graphics.withRotation
import com.angcyo.canvas.render.BuildConfig
import com.angcyo.library.ex.ensure
import com.angcyo.library.ex.getPointOnCircle
import com.angcyo.library.ex.textWidth
import kotlin.math.absoluteValue

/**
 * 曲线文本绘制信息
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/06/02
 */
data class CurveTextDraw(
    /**文本*/
    var text: String,
    /**曲度[-360~360], 角度*/
    var curvature: Float = 0f,
    /**文本默认的原始宽度*/
    var textWidth: Float = 0f,
    /**内圈半径*/
    var innerRadius: Float = 0f,
    /**外圈半径*/
    var outerRadius: Float = 0f,

    //---计算结果↓---

    /**曲线绘制中心坐标*/
    var curveCx: Float = 0f,
    var curveCy: Float = 0f,
    /**曲线文本的宽高*/
    var curveTextWidth: Float = 0f,
    var curveTextHeight: Float = 0f,
) {

    companion object {
        /**创建一个曲线绘制对象*/
        fun create(
            text: String,
            curvature: Float,
            textWidth: Float,
            textHeight: Float
        ): CurveTextDraw {
            val fraction = curvature / 360f
            //圆的周长
            val circleLength = (textWidth * 1 / fraction).absoluteValue
            //圆的半径
            val radius = circleLength / 2 / Math.PI
            //外圆的半径
            val outRadius = radius + textHeight
            return CurveTextDraw(text, curvature, textWidth, radius.toFloat(), outRadius.toFloat())
        }
    }

    /**基准角度*/
    val baseAngleDegrees: Float
        get() = if (curvature > 0) 270f else 90f

    /**左边角度*/
    val leftAngleDegrees: Float
        get() = baseAngleDegrees - curvature / 2

    /**右边角度*/
    val rightAngleDegrees: Float
        get() = baseAngleDegrees + curvature / 2

    /**文本的高度*/
    val textHeight: Float
        get() = outerRadius - innerRadius

    init {
        measureCurveText()
    }

    /**测量曲线文本的宽高大小*/
    fun measureCurveText() {
        //计算宽高
        if (curvature.absoluteValue == 360f) {
            curveTextWidth = outerRadius * 2
            curveTextHeight = outerRadius * 2
        } else if (curvature.absoluteValue >= 180) {
            curveTextWidth = outerRadius * 2

            val top = getPointOnCircle(0f, 0f, outerRadius, baseAngleDegrees)
            val bottom = getPointOnCircle(0f, 0f, outerRadius, leftAngleDegrees)
            curveTextHeight = (top.y - bottom.y).absoluteValue
        } else {
            val left = getPointOnCircle(0f, 0f, outerRadius, leftAngleDegrees)
            val right = getPointOnCircle(0f, 0f, outerRadius, rightAngleDegrees)
            curveTextWidth = (left.x - right.x).absoluteValue

            val top = getPointOnCircle(0f, 0f, outerRadius, baseAngleDegrees)
            val bottom = getPointOnCircle(0f, 0f, innerRadius, leftAngleDegrees)
            curveTextHeight = (top.y - bottom.y).absoluteValue
        }

        //计算相对于0,0位置应该绘制的中心位置
        val textHeight = outerRadius - innerRadius
        val cx = curveTextWidth / 2
        val cy = if (curvature > 0) {
            textHeight + innerRadius
        } else {
            curveTextHeight - textHeight - innerRadius
        }
        curveCx = cx
        curveCy = cy
    }

    /**获取文本绘制的路径
     * 这种方式在正向曲线绘制时没有问题,
     * 但是反向绘制时, 大圈内绘制文本无法闭合
     * 小圈外绘制文本会叠加
     * 解决方案: 一个字符一个字符绘制
     * [draw]*/
    fun getTextDrawPath(): Path {
        val path = Path()
        val cx = curveCx
        val cy = curveCy
        val oval = if (curvature > 0) {
            RectF(
                cx - innerRadius,
                cy - innerRadius,
                cx + innerRadius,
                cy + innerRadius,
            )
        } else {
            RectF(
                cx - outerRadius,
                cy - outerRadius,
                cx + outerRadius,
                cy + outerRadius,
            )
            /*RectF(
                cx - innerRadius,
                cy - innerRadius,
                cx + innerRadius,
                cy + innerRadius,
            )*/
        }
        path.addArc(oval, leftAngleDegrees, curvature)
        //path.addOval(oval, Path.Direction.CW)
        return path
    }

    /**提示圆圈的path*/
    fun getTextDrawInnerCirclePath(): Path {
        val path = Path()
        val cx = curveCx
        val cy = curveCy
        path.addCircle(cx, cy, innerRadius, Path.Direction.CW)
        return path
    }

    fun getTextDrawOuterCirclePath(): Path {
        val path = Path()
        val cx = curveCx
        val cy = curveCy
        path.addCircle(cx, cy, outerRadius, Path.Direction.CW)
        return path
    }

    /**在指定位置绘制的最佳偏移量*/
    fun getTranslateMatrix(x: Float, y: Float, result: Matrix = Matrix()): Matrix {
        val tx = x - curveTextWidth / 2
        val ty = if (curvature > 0) {
            y - curveTextHeight / 2 + curveTextHeight / 2 - textHeight
        } else {
            y - curveTextHeight
        }
        result.setTranslate(tx, ty)
        return result
    }

    /**绘制曲线文本,按照角度变化, 一个一个字符绘制 */
    fun draw(canvas: Canvas, paint: Paint) {
        //开始绘制的角度
        val startAngle = leftAngleDegrees
        //每个像素对应的角度
        val pixelAngle = (curvature.absoluteValue / textWidth).ensure(0f)

        var textSumWidth = 0f
        for (char in text) {
            val charStr = char.toString()
            val charWidth = paint.textWidth(charStr)
            textSumWidth += charWidth
        }
        //间隙角度
        val gapAngle: Float =
            (curvature.absoluteValue - pixelAngle * textSumWidth) / (text.length - 1)

        //自旋角度
        val charRotate = if (curvature > 0) 90f else -90f
        var charStartAngle = startAngle
        for (char in text) {
            val charStr = char.toString()
            val charWidth = paint.textWidth(charStr)
            val charHeight = textHeight
            val textRotateCenterX = curveCx + innerRadius + charHeight / 2
            val textRotateCenterY = curveCy /*+ charWidth / 2*/
            val drawX = textRotateCenterX - charWidth / 2
            var drawY = textRotateCenterY + charHeight / 2 - paint.descent() + paint.strokeWidth

            val charAngle = pixelAngle * charWidth
            val rotate =
                if (curvature > 0) charStartAngle + charAngle / 2 else charStartAngle - charAngle / 2
            canvas.withRotation(rotate, curveCx, curveCy) {//旋转到曲线上
                canvas.withRotation(charRotate, textRotateCenterX, textRotateCenterY) { //自旋角度
                    //正常绘制文本
                    canvas.drawText(charStr, drawX, drawY, paint)
                }
            }
            if (BuildConfig.DEBUG && charStartAngle == startAngle) {
                //调试, 在未自旋的位置绘制
                canvas.drawText(charStr, drawX, drawY, paint)
                paint.color = Color.MAGENTA
                canvas.withRotation(charRotate, textRotateCenterX, textRotateCenterY) {
                    canvas.drawText(charStr, drawX, drawY, paint)
                }
            }
            if (curvature > 0) {
                charStartAngle += gapAngle + charAngle
            } else {
                charStartAngle -= gapAngle + charAngle
            }
        }
    }
}