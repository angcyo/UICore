package com.angcyo.canvas.render.core.component

import android.graphics.Path
import android.graphics.PointF
import android.graphics.RectF
import com.angcyo.canvas.render.core.CanvasRenderDelegate
import com.angcyo.canvas.render.core.IComponent
import com.angcyo.canvas.render.element.BaseElement
import com.angcyo.canvas.render.element.IElement
import com.angcyo.library.annotation.Pixel
import com.angcyo.library.component.pool.acquireTempRectF
import com.angcyo.library.component.pool.release
import com.angcyo.library.ex.contains
import com.angcyo.library.ex.dp
import com.angcyo.library.ex.intersect

/** 元素的碰撞检测
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/03/20
 */
class ElementHitComponent(val element: IElement) : IComponent {

    companion object {
        val elementBoundsPath = Path()
    }

    override var isEnableComponent: Boolean = true

    /**是否激活碰撞增益, 在元素很小的时候, 自动放大处理*/
    var enableHitGain = true

    /**增益的大小*/
    var hitGainSize = 10 * dp

    /**增益的阈值, 宽/高小于这个值时, 才增益*/
    var hitGainThreshold = 10 * dp

    /**元素的bounds是否完全包含point
     * [com.angcyo.canvas.render.renderer.BaseRenderer.rendererContainsPoint]*/
    @Pixel
    fun elementContainsPoint(delegate: CanvasRenderDelegate?, point: PointF): Boolean {
        var result = getElementBoundsPath(delegate, elementBoundsPath).contains(point)
        if (!result) {
            val tempRect = acquireTempRectF()
            tempRect.set(point.x, point.y, point.x + 1, point.y + 1)
            result = elementIntersectRect(delegate, tempRect) //此时使用1像素的矩形,进行碰撞
            tempRect.release()
        }
        return result
    }

    /**元素的bounds是否完全包含rect
     * [com.angcyo.canvas.render.renderer.BaseRenderer.rendererContainsRect]*/
    @Pixel
    fun elementContainsRect(delegate: CanvasRenderDelegate?, rect: RectF): Boolean =
        getElementBoundsPath(delegate, elementBoundsPath).contains(rect)

    /**元素的bounds是否相交rect
     * [com.angcyo.canvas.render.renderer.BaseRenderer.rendererIntersectRect]*/
    @Pixel
    fun elementIntersectRect(delegate: CanvasRenderDelegate?, rect: RectF): Boolean =
        getElementBoundsPath(delegate, elementBoundsPath).intersect(rect)

    /**获取元素用来碰撞检测的[Path]范围*/
    @Pixel
    fun getElementBoundsPath(delegate: CanvasRenderDelegate?, result: Path): Path {
        val property = if (element is BaseElement) element.renderProperty else null
        val renderMatrix = property?.getRenderMatrix(includeRotate = true)
        val rect = getElementBounds(delegate, acquireTempRectF())
        result.rewind()
        result.addRect(rect, Path.Direction.CW)
        if (renderMatrix != null) {
            result.transform(renderMatrix)
        }
        rect.release()
        return result
    }

    /**获取元素的矩形, 宽高大小*/
    @Pixel
    fun getElementBounds(delegate: CanvasRenderDelegate?, result: RectF): RectF {
        val renderProperty = if (element is BaseElement) element.renderProperty else null
        val gainSize = hitGainSize / (delegate?.renderViewBox?.getScale() ?: 1f) //上下增益10dp
        result.set(0f, 0f, renderProperty?.width ?: 1f, renderProperty?.height ?: 1f)//线的高度
        if (enableHitGain) {
            if (result.width() <= hitGainThreshold) {
                result.inset(-gainSize, 0f)
            }
            if (result.height() <= hitGainThreshold) {
                result.inset(0f, -gainSize)
            }
        }
        return result
    }

}