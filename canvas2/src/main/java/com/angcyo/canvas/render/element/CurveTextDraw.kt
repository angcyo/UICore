package com.angcyo.canvas.render.element

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import androidx.core.graphics.withRotation
import com.angcyo.library.annotation.CallPoint
import com.angcyo.library.ex.ensure
import com.angcyo.library.ex.getPointOnCircle
import com.angcyo.library.ex.textWidth
import com.angcyo.library.ex.toRadians
import kotlin.math.absoluteValue
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.tan

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
    /**用来测量单字符的高度/宽度*/
    var measureTextWidthAction: ((text: String) -> Float)? = null,
    var measureTextHeightAction: ((text: String) -> Float)? = null,

    //---计算结果↓---
    /**每个字符绘制的中垂线角度*/
    /*val charInfoList: MutableList<CharInfo> = mutableListOf(),*/
    /**曲线绘制中心坐标*/
    var curveCx: Float = 0f,
    var curveCy: Float = 0f,
    /**曲线内部最大空洞的高度和宽度*/
    var curveInnerWidth: Float = 0f,
    var curveInnerHeight: Float = 0f,
    /**曲线文本的宽高*/
    var curveTextWidth: Float = 0f,
    var curveTextHeight: Float = 0f,

    /**在绘制中文的时候, 无法全部包裹住, 此时需要额外撑一点高度*/
    var offsetWidth: Float = 0f,
    var offsetHeight: Float = 0f
) {

    companion object {
        /**创建一个曲线绘制对象*/
        fun create(
            text: String,
            curvature: Float,
            textWidth: Float,
            textHeight: Float,
            paint: Paint
        ): CurveTextDraw {
            val fraction = curvature / 360f
            //圆的周长
            val circleLength = (textWidth * 1 / fraction).absoluteValue
            //圆的半径
            val radius = circleLength / 2 / Math.PI
            //外圆的半径
            val outRadius = radius + textHeight
            return CurveTextDraw(
                text,
                curvature,
                textWidth,
                radius.toFloat(),
                outRadius.toFloat()
            ).apply {
                measureCurveText(paint)
            }
        }
    }

    /**基准角度*/
    val baseAngleDegrees: Float
        get() = if (curvature > 0) 270f else 90f

    /**第一个字符的角度*/
    val startAngle: Float
        get() = baseAngleDegrees - curvature / 2 //charInfoList.firstOrNull()?.angle ?: baseAngleDegrees

    /**最后一个字符的角度*/
    val endAngle: Float
        get() = baseAngleDegrees + curvature / 2 //charInfoList.lastOrNull()?.angle ?: baseAngleDegrees

    /**字符的自旋角度*/
    val charRotate: Float
        get() = if (curvature > 0) 90f else -90f

    /**左边角度*/
    val leftAngleDegrees: Float
        get() = startAngle

    /**右边角度*/
    val rightAngleDegrees: Float
        get() = endAngle

    /**文本的高度*/
    val textHeight: Float
        get() = outerRadius - innerRadius

    /**每个像素对应的角度*/
    val pixelAngle: Float
        get() = (curvature.absoluteValue / textWidth).ensure(0f)

    /**测量曲线文本的宽高大小*/
    @CallPoint
    fun measureCurveText(paint: Paint) {
        //measureCharAngle(paint)
        //计算宽高
        if (curvature.absoluteValue == 360f) {
            curveTextWidth = outerRadius * 2
            curveTextHeight = curveTextWidth
            curveInnerWidth = innerRadius * 2
            curveInnerHeight = curveInnerWidth
        } else {
            val offset = 0f //pixelAngle * charWidth() / 2
            val leftAngle = leftAngleDegrees - offset
            val rightAngle = rightAngleDegrees + offset
            val textDrawHeight = textHeight //- paint.descent()
            if (curvature.absoluteValue >= 180) {
                curveTextWidth = outerRadius * 2
                val top = getPointOnCircle(0f, 0f, outerRadius, baseAngleDegrees)
                val bottom = getPointOnCircle(0f, 0f, outerRadius, leftAngle)
                curveTextHeight = (top.y - bottom.y).absoluteValue

                curveInnerWidth = innerRadius * 2
                val innerTop = getPointOnCircle(0f, 0f, innerRadius, baseAngleDegrees)
                val innerBottom = getPointOnCircle(0f, 0f, innerRadius, leftAngle)
                curveInnerHeight = (innerTop.y - innerBottom.y).absoluteValue
            } else {
                val left = getPointOnCircle(0f, 0f, outerRadius, leftAngle)
                val right = getPointOnCircle(0f, 0f, outerRadius, rightAngle)
                curveTextWidth = (left.x - right.x).absoluteValue

                val top = getPointOnCircle(0f, 0f, outerRadius, baseAngleDegrees)
                val bottom = getPointOnCircle(0f, 0f, innerRadius, leftAngle)
                curveTextHeight = (top.y - bottom.y).absoluteValue

                val innerLeft = getPointOnCircle(0f, 0f, innerRadius, leftAngle)
                val innerRight = getPointOnCircle(0f, 0f, innerRadius, rightAngle)
                curveInnerWidth = (innerLeft.x - innerRight.x).absoluteValue

                val innerTop = getPointOnCircle(0f, 0f, innerRadius, baseAngleDegrees)
                val innerBottom = getPointOnCircle(0f, 0f, innerRadius, leftAngle)
                curveInnerHeight = (innerTop.y - innerBottom.y).absoluteValue
            }
        }

        //
        val metrics = paint.fontMetrics
        val offset = (metrics.bottom - metrics.descent)
        curveTextWidth += offset
        curveTextHeight += offset

        //高度补偿
        offsetHeight = measureTextWidth("${text[(text.length / 2f).roundToInt()]}", paint) / 4
        /*if (text.length % 2 == 0) {
            offsetHeight /= 2
        }*/
        //offsetHeight *= tan(min(curvature.absoluteValue, 45f).toRadians())
        offsetWidth = offsetHeight * tan(min(curvature.absoluteValue, 45f).toRadians())
        curveTextWidth += offsetWidth
        curveTextHeight += offsetHeight

        //计算相对于0,0位置应该绘制的中心位置
        val textHeight = outerRadius - innerRadius
        val cx = curveTextWidth / 2
        val cy = if (curvature > 0) {
            textHeight + innerRadius + offsetHeight
        } else {
            curveTextHeight - textHeight - innerRadius
        }
        curveCx = cx
        curveCy = cy
    }

    /**测量单字符的高度*/
    fun measureTextHeight(text: String): Float {
        return measureTextHeightAction?.invoke(text) ?: textHeight
    }

    /**补偿文本的高度
     * [measureTextHeight]*/
    fun wrapTextHeight(text: String): Float {
        return measureTextHeight(text) + offsetHeight
    }

    /**测量单字符的宽度*/
    fun measureTextWidth(text: String, paint: Paint): Float {
        return measureTextWidthAction?.invoke(text) ?: paint.textWidth(text)
    }

    /**单字符的平均宽度*/
    /*fun charWidth(): Float {
        var textSumWidth = 0f
        if (measureTextWidthAction == null) {
            textSumWidth = textWidth
        } else {
            for (char in text) {
                val charStr = char.toString()
                val charWidth = measureTextWidthAction!!.invoke(charStr)
                textSumWidth += charWidth
            }
        }
        return textSumWidth / text.length
    }*/

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

    /**绘制曲线文本,按照角度变化, 一个一个字符绘制 */
    fun draw(canvas: Canvas, paint: Paint) {
        //开始绘制的角度
        val startAngle = leftAngleDegrees
        //每个像素对应的角度
        val pixelAngle = pixelAngle

        var textSumWidth = 0f
        for (char in text) {
            val charStr = char.toString()
            val charWidth = measureTextWidth(charStr, paint)
            textSumWidth += charWidth
        }
        //间隙角度
        val gapAngle: Float =
            (curvature.absoluteValue - pixelAngle * textSumWidth) / (text.length - 1)

        //在中心绘制
        val drawCenter = true //中心点绘制误差会小一点
        val metrics = paint.fontMetrics
        val offset = if (curvature > 0) metrics.bottom - metrics.descent else 0f
        val descent = paint.descent()

        //自旋角度
        var charStartAngle = startAngle
        for (char in text) {
            val charStr = char.toString()
            val charWidth = measureTextWidth(charStr, paint)
            val charHeight = if (curvature > 0) measureTextHeight(charStr) else textHeight
            val textRotateCenterX = curveCx + innerRadius + charHeight / 2 - offset
            val textRotateCenterY = if (drawCenter) curveCy else curveCy + charWidth / 2
            //val textRotateCenterY = curveCy + charWidth / 2 //在下面绘制
            val drawX = textRotateCenterX - charWidth / 2
            val drawY = textRotateCenterY + charHeight / 2 - descent

            val charAngle = pixelAngle * (charWidth - offset)
            val offsetAngle = if (drawCenter) charAngle / 2 else 0f
            val rotate =
                if (curvature > 0) charStartAngle + offsetAngle else charStartAngle - offsetAngle
            canvas.withRotation(rotate, curveCx, curveCy) {//旋转到曲线上
                canvas.withRotation(charRotate, textRotateCenterX, textRotateCenterY) { //自旋角度
                    //正常绘制文本, 不应该绘制在起始线的中间, 而是应该绘制在起始线的下方
                    canvas.drawText(charStr, drawX, drawY, paint)
                }
            }
            /*if (BuildConfig.DEBUG && charStartAngle == startAngle) {
                //调试, 在未自旋的位置绘制
                canvas.drawText(charStr, drawX, drawY, paint)
                canvas.withRotation(charRotate, textRotateCenterX, textRotateCenterY) {
                    canvas.drawText(charStr, drawX, drawY, paint)
                }
                paint.color = Color.MAGENTA//后面绘制的都是紫色
            }*/
            if (curvature > 0) {
                charStartAngle += gapAngle + charAngle
            } else {
                charStartAngle -= gapAngle + charAngle
            }
        }
    }
}