package com.angcyo.canvas.render.element

import android.graphics.Path
import android.graphics.PointF
import android.graphics.RectF
import android.graphics.drawable.Drawable
import com.angcyo.canvas.render.core.component.CanvasRenderProperty
import com.angcyo.library.ex.contains
import com.angcyo.library.ex.intersect

/**
 * 元素的基类
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/02/11
 */
abstract class BaseElement : IElement {

    /**描述的渲染属性, 包含坐标/缩放/倾斜信息*/
    var renderProperty = CanvasRenderProperty()

    /**需要在界面上渲染的[Drawable]*/
    var renderDrawable: Drawable? = null

    //region---core---

    override fun requestElementRenderProperty(): CanvasRenderProperty = renderProperty

    override fun requestElementRenderDrawable(): Drawable? = renderDrawable

    override fun elementContainsPoint(point: PointF): Boolean {
        var result = getElementBoundsPath().contains(point)
        if (!result) {
            val tempRect = RectF()
            tempRect.set(point.x, point.y, point.x + 1, point.y + 1)
            result = elementIntersectRect(tempRect) //此时使用1像素的矩形,进行碰撞
        }
        return result
    }

    override fun elementContainsRect(rect: RectF): Boolean = getElementBoundsPath().contains(rect)

    override fun elementIntersectRect(rect: RectF): Boolean = getElementBoundsPath().intersect(rect)

    override fun updateElementRenderProperty(property: CanvasRenderProperty) {
        property.copyTo(renderProperty)
    }

    //endregion---core---

    //region---方法---

    /**获取元素用来碰撞检测的[Path]范围*/
    fun getElementBoundsPath(result: Path = Path()): Path {
        val property = renderProperty
        val renderMatrix = property.getRenderMatrix(includeRotate = true)
        val rect = RectF(0f, 0f, property.width, property.height)
        result.rewind()
        result.addRect(rect, Path.Direction.CW)
        result.transform(renderMatrix)
        return result
    }

    //endregion---方法---

}