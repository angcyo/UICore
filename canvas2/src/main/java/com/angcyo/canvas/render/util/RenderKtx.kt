package com.angcyo.canvas.render.util

import android.graphics.*
import android.text.TextPaint
import android.view.Gravity
import com.angcyo.canvas.render.core.CanvasRenderDelegate
import com.angcyo.canvas.render.core.Reason
import com.angcyo.canvas.render.core.Strategy
import com.angcyo.canvas.render.core.component.CanvasSelectorComponent
import com.angcyo.canvas.render.element.IElement
import com.angcyo.canvas.render.element.TextElement
import com.angcyo.canvas.render.renderer.BaseRenderer
import com.angcyo.canvas.render.renderer.CanvasElementRenderer
import com.angcyo.canvas.render.renderer.CanvasGroupRenderer
import com.angcyo.library.annotation.Pixel
import com.angcyo.library.ex.*
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

/**一些工具扩展
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/02/11
 */

//region---Canvas---

private val _overrideMatrix = Matrix()

/**创建一个输出等比指定大小的矩阵
 * [overrideWidth] 输出的宽度
 * [overrideHeight] 输出的高度
 * 宽高同时指定时, 则任意比例缩放. 只指定1个时, 等比缩放
 * */
fun createOverrideMatrix(
    originWidth: Float,
    originHeight: Float,
    overrideWidth: Float?,
    overrideHeight: Float? = null,
    result: Matrix = _overrideMatrix
): Matrix {
    var sx = 1f
    var sy = 1f
    //覆盖大小需要进行的缩放

    if (overrideWidth != null && overrideHeight != null) {
        //任意比例
        if (originWidth > 0) {
            sx = overrideWidth / originWidth
        }
        if (originHeight > 0) {
            sy = overrideHeight / originHeight
        }
    } else if (overrideWidth != null || overrideHeight != null) {
        //等比
        val overrideSize = overrideWidth ?: overrideHeight
        if (overrideSize != null) {
            if (originWidth > 0) {
                sx = overrideSize / originWidth
            }
            if (originHeight > 0) {
                sy = overrideSize / originHeight
            }

            if (originWidth > 0 && originHeight > 0) {
                sx = min(sx, sy)
                sy = sx//等比
            }
        }
    } else {
        //no op
    }
    result.setScale(sx, sy)
    return result
}

/**创建一个输出指定大小的[Canvas] [Picture]
 * [createOverrideMatrix]
 * */
fun createOverridePictureCanvas(
    @Pixel
    originWidth: Float,
    @Pixel
    originHeight: Float,
    @Pixel
    overrideWidth: Float?, /*要缩放到的宽度*/
    @Pixel
    overrideHeight: Float? = null,
    @Pixel
    minWidth: Float = 1f, /*最小宽度*/
    @Pixel
    minHeight: Float = 1f,
    block: Canvas.() -> Unit
): Picture {
    val matrix = createOverrideMatrix(originWidth, originHeight, overrideWidth, overrideHeight)
    //目标输出的大小
    val width = originWidth * matrix.getScaleX()
    val height = originHeight * matrix.getScaleY()

    val w = max(width, minWidth).ceilInt()
    val h = max(height, minHeight).ceilInt()

    return withPicture(w, h) {
        concat(matrix)
        block()
    }
}

/**创建一个输出指定大小的[Canvas] [Bitmap]
 * [createOverrideMatrix]
 */
fun createOverrideBitmapCanvas(
    originWidth: Float,
    originHeight: Float,
    overrideWidth: Float?,
    overrideHeight: Float? = null,
    block: Canvas.() -> Unit
): Bitmap {
    val matrix = createOverrideMatrix(originWidth, originHeight, overrideWidth, overrideHeight)
    //目标输出的大小
    val width = originWidth * matrix.getScaleX()
    val height = originHeight * matrix.getScaleY()
    return withBitmap(width.ceilInt(), height.ceilInt()) {
        concat(matrix)
        block()
    }
}

/**创建一个[Picture]对象*/
fun withPicture(width: Int, height: Int, block: Canvas.() -> Unit): Picture {
    return Picture().apply {
        val canvas = beginRecording(width, height)
        canvas.block()
        //结束
        endRecording()
    }
}

/**创建一个[Bitmap]对象*/
fun withBitmap(
    width: Int,
    height: Int,
    config: Bitmap.Config = Bitmap.Config.ARGB_8888,
    block: Canvas.() -> Unit
): Bitmap {
    return Bitmap.createBitmap(width, height, config).apply {
        val canvas = Canvas(this)
        canvas.block()
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
    strokeWidth = 1f
    strokeJoin = Paint.Join.ROUND
    strokeCap = Paint.Cap.ROUND
}

//endregion---Canvas---

//region---render---

/**渲染元素*/
val BaseRenderer.renderElement: IElement?
    get() = if (this is CanvasElementRenderer) renderElement else null

val BaseRenderer.textElement: TextElement?
    get() = element()

/**获取指定类型的元素[IElement]*/
inline fun <reified T : IElement> BaseRenderer.element(): T? {
    val element = renderElement
    if (element is T) {
        return element
    }
    return null
}

//endregion---render---

//region---util---

/**保留小数点后几位*/
fun Float.canvasDecimal(
    digit: Int = 2,
    fadedUp: Boolean = true,
    ensureInt: Boolean = true
): String {
    return decimal(digit, ensureInt, fadedUp)
}

fun Double.canvasDecimal(
    digit: Int = 2,
    fadedUp: Boolean = true,
    ensureInt: Boolean = true
): String {
    return decimal(digit, ensureInt, fadedUp)
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

/**判断当前的渲染器是否是选择组件*/
fun BaseRenderer?.isSelectorGroupRenderer() = this is CanvasSelectorComponent

/**判断当前的渲染器是否是群组渲染器, 但是不是选择群组渲染器*/
fun BaseRenderer?.isOnlyGroupRenderer() = !isSelectorGroupRenderer() && this is CanvasGroupRenderer

//endregion---operate---