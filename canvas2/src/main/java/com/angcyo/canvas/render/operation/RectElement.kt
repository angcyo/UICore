package com.angcyo.canvas.render.operation

import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.drawable.Drawable
import com.angcyo.canvas.render.element.BaseElement
import com.angcyo.canvas.render.util.PictureRenderDrawable
import com.angcyo.canvas.render.util.withPicture
import com.angcyo.library.ex.ceilInt
import com.angcyo.library.unit.toPixel

/**
 * 矩形元素
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/02/11
 */
class RectElement : BaseElement() {

    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = Color.RED
        this.style = Paint.Style.STROKE
        strokeWidth = 1f
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
    }

    init {
        renderProperty.apply {
            /*left = 10.098119f.toPixel()
            top = 7.090558f.toPixel()
            width = 26.933332f.toPixel()
            height = 9.2f.toPixel()

            scaleX = 0.5651549f
            scaleY = 0.4007788f
            angle = 69.81055f

            skewX = 51.981655f
            skewY = 0f*/

            anchorX = 2.8f.toPixel()
            anchorY = 0.26666668f.toPixel()
            width = 12.933333f.toPixel()
            height = 10.8f.toPixel()

            scaleX = 1.7841423f
            scaleY = 0.8901337f
            angle = 14.873828f

            skewX = -37.52056f
            skewY = 0f
        }
    }

    override fun requestElementRenderDrawable(): Drawable? {
        val bounds = RectF()
        val property = requestElementRenderProperty()
        property.getRenderBounds(bounds, false)

        val width = bounds.width()
        val height = bounds.height()

        return PictureRenderDrawable(withPicture(width.ceilInt(), height.ceilInt()) {
            val renderMatrix = property.getRenderMatrix(includeRotate = true)
            val path = Path()
            val rect = RectF(0f, 0f, property.width, property.height)
            //path.addRect(rect, Path.Direction.CW)
            path.addOval(rect, Path.Direction.CW)
            path.transform(renderMatrix)

            translate(-bounds.left, -bounds.top)
            paint.color = Color.RED

            //直接作用数据, 绘制出来的矩形就不会出现粗细不一致的情况
            drawPath(path, paint)

            //以下方式渲染出来的矩形, 会有边框粗细不一样的情况
            /*withMatrix(renderMatrix) {
                drawRect(rect, paint)
            }*/
        })
    }
}