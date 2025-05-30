package com.angcyo.canvas.render.util

import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.text.TextPaint
import android.view.Gravity
import com.angcyo.canvas.render.core.CanvasRenderDelegate
import com.angcyo.canvas.render.core.component.CanvasSelectorComponent
import com.angcyo.canvas.render.element.IElement
import com.angcyo.canvas.render.element.TextElement
import com.angcyo.canvas.render.renderer.BaseRenderer
import com.angcyo.canvas.render.renderer.CanvasElementRenderer
import com.angcyo.canvas.render.renderer.CanvasGroupRenderer
import com.angcyo.library.annotation.Pixel
import com.angcyo.library.canvas.core.Reason
import com.angcyo.library.component.Strategy
import com.angcyo.library.ex.decimal
import com.angcyo.library.ex.dp

/**一些工具扩展
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/02/11
 */

//region---Canvas---

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

//--

fun Iterable<BaseRenderer>?.toDrawable(
    overrideSize: Float? = null,
    @Pixel bounds: RectF? = null,
    ignoreVisible: Boolean = false
) = CanvasGroupRenderer.createRenderDrawable(this, overrideSize, bounds, ignoreVisible)

fun Iterable<BaseRenderer>?.toBitmap(
    overrideSize: Float? = null,
    @Pixel bounds: RectF? = null,
    ignoreVisible: Boolean = false
) = CanvasGroupRenderer.createRenderBitmap(this, overrideSize, bounds, ignoreVisible)

@Pixel
val Iterable<BaseRenderer>?.allRendererBounds: RectF? get() = CanvasGroupRenderer.computeBounds(this)

//endregion---operate---