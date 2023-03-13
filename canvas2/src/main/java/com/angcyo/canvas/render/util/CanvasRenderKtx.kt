package com.angcyo.canvas.render.util

import android.graphics.*
import android.text.TextPaint
import android.view.Gravity
import com.angcyo.canvas.render.core.CanvasRenderDelegate
import com.angcyo.canvas.render.core.Reason
import com.angcyo.canvas.render.core.Strategy
import com.angcyo.canvas.render.element.IElement
import com.angcyo.canvas.render.element.TextElement
import com.angcyo.canvas.render.renderer.BaseRenderer
import com.angcyo.canvas.render.renderer.CanvasElementRenderer
import com.angcyo.library.ex.decimal
import com.angcyo.library.ex.dp
import kotlin.math.sqrt

/**一些工具扩展
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/02/11
 */

//region---1---

/**创建一个[Picture]对象*/
fun withPicture(width: Int, height: Int, block: Canvas.() -> Unit): Picture {
    return Picture().apply {
        val canvas = beginRecording(width, height)
        canvas.block()
        //结束
        endRecording()
    }
}

/**创建一个画笔对象*/
fun createRenderPaint(
    color: Int = Color.BLACK,
    width: Float = 1f,
    style: Paint.Style = Paint.Style.STROKE
) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
    this.color = color
    this.style = style
    strokeWidth = width
    strokeJoin = Paint.Join.ROUND
    strokeCap = Paint.Cap.ROUND
}

fun createRenderTextPaint(
    color: Int = Color.BLACK,
    style: Paint.Style = Paint.Style.FILL
) = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
    this.color = color
    this.style = style
    textSize = 9 * dp
    strokeJoin = Paint.Join.ROUND
    strokeCap = Paint.Cap.ROUND
}

//endregion---1---

//region---render---

val BaseRenderer.renderElement: IElement?
    get() = if (this is CanvasElementRenderer) renderElement else null

val BaseRenderer.textElement: TextElement?
    get() {
        val element = renderElement
        if (element is TextElement) {
            return element
        }
        return null
    }

//endregion---render---

//region---util---

/**保留小数点后几位*/
fun Float.canvasDecimal(digit: Int = 2, fadedUp: Boolean = true): String {
    return this.toDouble().decimal(digit, fadedUp)
}

fun Double.canvasDecimal(digit: Int = 2, fadedUp: Boolean = true): String {
    return decimal(digit, fadedUp)
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

/**获取2个点的中点坐标*/
fun midPoint(x1: Float, y1: Float, x2: Float, y2: Float, result: PointF) {
    result.x = (x1 + x2) / 2f
    result.y = (y1 + y2) / 2f
}

fun midPoint(p1: PointF, p2: PointF, result: PointF) {
    midPoint(p1.x, p1.y, p2.x, p2.y, result)
}

//endregion---util---

//region---operate---

/**将渲染器在[bounds]内对齐*/
fun BaseRenderer.alignInBounds(
    delegate: CanvasRenderDelegate?,
    bounds: RectF,
    align: Int = Gravity.CENTER,
    strategy: Strategy = Strategy.normal
) {
    renderProperty?.let {
        val itemBounds = it.getRenderBounds()
        val dx = when (align) {
            Gravity.CENTER -> bounds.centerX() - itemBounds.centerX()
            Gravity.LEFT -> bounds.left - itemBounds.left
            Gravity.RIGHT -> bounds.right - itemBounds.right
            else -> 0f
        }
        val dy = when (align) {
            Gravity.CENTER -> bounds.centerY() - itemBounds.centerY()
            Gravity.TOP -> bounds.top - itemBounds.top
            Gravity.BOTTOM -> bounds.bottom - itemBounds.bottom
            else -> 0f
        }
        translate(dx, dy, Reason.user, strategy, delegate)
    }
}

//endregion---operate---