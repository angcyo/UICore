package com.angcyo.canvas.items.renderer

import android.graphics.Path
import android.graphics.RectF
import android.text.TextPaint
import com.angcyo.library.component.ScalePictureDrawable
import com.angcyo.canvas.core.ICanvasView
import com.angcyo.canvas.items.ShapeItem
import com.angcyo.library.ex.ceil
import com.angcyo.library.ex.emptyRectF
import com.angcyo.library.ex.withPicture
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/22
 */
@Deprecated("请使用[PictureItemRenderer]")
class ShapeItemRenderer(canvasView: ICanvasView) :
    DrawableItemRenderer<ShapeItem>(canvasView) {

    val shapeBounds = emptyRectF()

    /**添加一个shape
     * [Path] 请注意需要从0,0的位置开始绘制*/
    fun addShape(path: Path, paint: TextPaint? = null): ShapeItem {
        val bounds = getBounds()
        path.computeBounds(bounds, true)
        shapeBounds.set(bounds)
        _rendererItem = ShapeItem().apply {
            this.path = path
            this.paint = paint ?: this.paint
            drawable = ScalePictureDrawable(
                withPicture(
                    bounds.width().absoluteValue.ceil().roundToInt(),
                    bounds.height().absoluteValue.ceil().roundToInt()
                ) {
                    drawPath(path, this@apply.paint)
                })
        }
        refresh()
        return _rendererItem!!
    }

    /**添加一个矩形, 坐标是相对于坐标系的坐标*/
    fun addRect(rect: RectF, paint: TextPaint? = null) {
        val path = Path().apply {
            addRect(
                0f,
                0f,
                rect.width().absoluteValue,
                rect.height().absoluteValue,
                Path.Direction.CW
            )
        }
        addShape(path, paint)
        getBounds().offsetTo(rect.left, rect.top)
        refresh()
    }
}