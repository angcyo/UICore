package com.angcyo.canvas.render.element

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import androidx.core.graphics.withMatrix
import androidx.core.graphics.withTranslation
import com.angcyo.canvas.render.BuildConfig
import com.angcyo.canvas.render.data.CharDrawInfo
import com.angcyo.canvas.render.data.getOutlineRect
import com.angcyo.library.annotation.CallPoint
import com.angcyo.library.component.pool.acquireTempMatrix
import com.angcyo.library.component.pool.acquireTempRectF
import com.angcyo.library.component.pool.release
import com.angcyo.library.ex.ensure
import com.angcyo.library.ex.getOutlineRect
import com.angcyo.library.ex.isDebugType
import kotlin.math.absoluteValue

/**
 * 曲线文本绘制信息
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/06/02
 */
data class CurveTextDraw(
    /**需要绘制的文本*以及对应的绘制信息*/
    var charList: List<CharDrawInfo>,
    /**曲度[-360~360], 角度*/
    var curvature: Float = 0f,
    /**文本默认的原始宽度*/
    var textWidth: Float = 0f,
    /**内圈半径*/
    var innerRadius: Float = 0f,
    /**外圈半径*/
    var outerRadius: Float = 0f,

    //---计算结果↓---
    /**曲线文本outline的边界*/
    var curveOutlineBounds: RectF = RectF(),

    /**在绘制中文的时候, 无法全部包裹住, 此时需要额外撑一点高度*/
    var offsetWidth: Float = 0f,
    var offsetHeight: Float = 0f,
    /**中心点需要的偏移量*/
    var offsetX: Float = 0f,
    var offsetY: Float = 0f,
) {

    companion object {

        /**创建一个曲线绘制对象*/
        fun create(charList: List<CharDrawInfo>, curvature: Float): CurveTextDraw {
            val outlineRect = charList.getOutlineRect()
            val textWidth = outlineRect.width()
            val textHeight = outlineRect.height()
            return create(charList, curvature, textWidth, textHeight)
        }

        /**创建一个曲线绘制对象*/
        fun create(
            charList: List<CharDrawInfo>,
            curvature: Float,
            textWidth: Float,
            textHeight: Float,
        ): CurveTextDraw {
            if (curvature == 0f) {
                return CurveTextDraw(
                    charList,
                    curvature,
                    textWidth,
                    0f,
                    textHeight,
                ).apply {
                    measureCurveText()
                }
            }
            val fraction = curvature / 360f
            //圆的周长
            val circleLength = (textWidth * 1 / fraction).absoluteValue
            //圆的半径
            val radius = circleLength / 2 / Math.PI
            //外圆的半径
            val outRadius = radius + textHeight
            return CurveTextDraw(
                charList,
                curvature,
                textWidth,
                radius.toFloat(),
                outRadius.toFloat()
            ).apply {
                measureCurveText()
            }
        }
    }

    /**基准角度*/
    val baseAngleDegrees: Float
        get() = if (curvature > 0) 270f else 90f

    /**第一个字符的角度*/
    val startAngle: Float
        get() = baseAngleDegrees - curvature / 2 //charInfoList.firstOrNull()?.angle ?: baseAngleDegrees

    /**字符的偏移角度.
     * 从当前位置, 顺时针移动到0°的角度
     * */
    val offsetRotate: Float
        get() = if (curvature > 0) 90f else -90f

    /**左边角度*/
    val leftAngleDegrees: Float
        get() = startAngle

    /**文本的高度*/
    val textHeight: Float
        get() = outerRadius - innerRadius

    /**每个像素对应的角度*/
    val pixelAngle: Float
        get() = (curvature.absoluteValue / textWidth).ensure(0f)

    /**曲线文本的宽高*/
    val curveTextWidth: Float
        get() = curveOutlineBounds.width() + offsetWidth

    val curveTextHeight: Float
        get() = curveOutlineBounds.height() + offsetHeight

    /**曲线对外提示线绘制中心坐标*/
    val curveCx: Float
        get() = curveDrawCx - curveOutlineBounds.left

    val curveCy: Float
        get() = if (curvature >= 0) curveDrawCy else curveTextHeight - textHeight - innerRadius

    /**计算outline bounds时的曲线中心点坐标
     * 这个坐标会影响outline的计算, 所以不能一直变
     * */
    private val curveDrawCx: Float
        get() = textWidth / 2
    private val curveDrawCy: Float
        get() = if (curvature == 0f) {
            textHeight / 2
        } else if (curvature > 0) {
            //下弧
            0 + textHeight + innerRadius
        } else {
            //上弧
            0 - innerRadius
        }

    /**是否开启调试*/
    private val isDebug = BuildConfig.BUILD_TYPE.isDebugType()

    /**测量曲线文本的宽高大小*/
    @CallPoint
    fun measureCurveText() {
        updateCurveCenter(RectF(0f, 0f, textWidth, textHeight))
        innerMeasureCureText()
    }

    private fun innerMeasureCureText() {
        val outline = charList.getOutlineRect()
        //开始绘制的角度
        val startAngle = leftAngleDegrees
        //每个像素对应的角度
        val pixelAngle = pixelAngle

        if (pixelAngle == 0f) {
            //直线文本
            updateCurveCenter(outline)
            return
        }

        val rectList = mutableListOf<RectF>()
        val textMatrix = acquireTempMatrix()

        charList.forEach { charInfo ->
            val charAngle = pixelAngle * (charInfo.bounds.centerX() - outline.left)
            charInfo._curveAngle = if (curvature > 0) {
                startAngle + charAngle
            } else {
                startAngle - charAngle
            }//计算每个字符需要旋转到的目标角度
            charInfo.initDrawMatrix(textMatrix)
            val textRect = RectF(charInfo.bounds)
            textMatrix.mapRect(textRect)
            rectList.add(textRect)
        }

        textMatrix.release()
        updateCurveCenter(rectList.getOutlineRect())
    }

    /**使用曲线文本的宽高, 更新曲线文本的中心点坐标*/
    private fun updateCurveCenter(rect: RectF = curveOutlineBounds) {
        curveOutlineBounds.set(rect)
    }

    /**绘制曲线文本,按照角度变化, 一个一个字符绘制 */
    fun draw(canvas: Canvas, paint: Paint) {
        val matrix = acquireTempMatrix()
        val textRect = acquireTempRectF()
        charList.forEach { charInfo ->
            if (curvature == 0f) {
                canvas.drawCharInfo(charInfo, paint)
                if (isDebug) {
                    paint.color = Color.GREEN
                    canvas.drawRect(charInfo.bounds, paint)
                }
            } else {
                textRect.set(charInfo.bounds)
                charInfo.initDrawMatrix(matrix)
                //偏移到0,0的位置
                val offsetX = -curveOutlineBounds.left
                val offsetY = if (curvature >= 0) -curveOutlineBounds.top else 0f
                canvas.withTranslation(offsetX, offsetY) {
                    canvas.withMatrix(matrix) {
                        drawCharInfo(charInfo, paint)
                    }
                    if (isDebug) {
                        paint.color = Color.GREEN
                        matrix.mapRect(textRect)
                        canvas.drawRect(textRect, paint)
                    }
                }
            }
        }
        matrix.release()
        textRect.release()
    }

    private fun CharDrawInfo.initDrawMatrix(matrix: Matrix): Matrix {
        matrix.setRotate(_curveAngle + offsetRotate, bounds.centerX(), curveDrawCy)
        val ty = if (curvature > 0) {
            0f
        } else {
            curveTextHeight - textHeight
        }
        matrix.postTranslate(curveDrawCx - bounds.centerX(), ty)
        return matrix
    }

    private fun Canvas.drawCharInfo(charInfo: CharDrawInfo, paint: Paint) {
        if (isDebug) {
            paint.color = Color.BLACK
        }
        val x = charInfo.bounds.left + charInfo.charDrawOffsetX
        val y = charInfo.bounds.bottom - charInfo.lineDescent + charInfo.charDrawOffsetY
        drawText(charInfo.char, x, y, paint)
        if (isDebug) {
            paint.color = Color.RED
            drawRect(charInfo.bounds, paint)
        }
    }
}