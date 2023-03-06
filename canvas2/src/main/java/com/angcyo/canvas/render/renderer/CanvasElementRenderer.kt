package com.angcyo.canvas.render.renderer

import android.graphics.Canvas
import android.graphics.PointF
import android.graphics.RectF
import android.graphics.drawable.Drawable
import androidx.core.graphics.withSave
import com.angcyo.canvas.render.core.CanvasRenderDelegate
import com.angcyo.canvas.render.core.Reason
import com.angcyo.canvas.render.core.component.CanvasRenderProperty
import com.angcyo.canvas.render.data.RendererParams
import com.angcyo.canvas.render.element.IElement
import com.angcyo.library.ex.have
import com.angcyo.library.ex.remove

/**用来绘制具体元素的类
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/02/11
 */
class CanvasElementRenderer : BaseRenderer() {

    companion object {
        /**请求需要重新获取[IElement]的绘制属性*/
        const val RENDERER_FLAG_REQUEST_PROPERTY = 0x100

        /**请求需要重新获取[IElement]的绘制Drawable*/
        const val RENDERER_FLAG_REQUEST_DRAWABLE = RENDERER_FLAG_REQUEST_PROPERTY shl 1
    }

    /**需要绘制的元素*/
    var element: IElement? = null
        set(value) {
            field = value
            updateRenderIfNeed()
        }

    //region---缓存---

    var _elementRenderDrawable: Drawable? = null

    //endregion---缓存---

    //region---core---

    override fun renderOnInside(canvas: Canvas, params: RendererParams) {
        element ?: return
        updateRenderIfNeed()
        renderProperty?.let { property ->
            _elementRenderDrawable?.let { drawable ->
                val renderBounds = property.getRenderBounds()
                canvas.withSave {
                    translate(renderBounds.left, renderBounds.top)
                    drawable.draw(canvas)
                }
            }
        }
    }

    /**更新渲染时, 需要的一些数据*/
    fun updateRenderIfNeed() {
        val element = element ?: return
        if (renderProperty == null || renderFlags.have(RENDERER_FLAG_REQUEST_PROPERTY)) {
            renderProperty = element.requestElementRenderProperty()
            renderFlags.remove(RENDERER_FLAG_REQUEST_PROPERTY)
        }

        if (_elementRenderDrawable == null || renderFlags.have(RENDERER_FLAG_REQUEST_DRAWABLE)) {
            _elementRenderDrawable = element.requestElementRenderDrawable()
            renderFlags.remove(RENDERER_FLAG_REQUEST_DRAWABLE)
        }
    }

    override fun updateRenderFlag(newFlag: Int, reason: Reason, delegate: CanvasRenderDelegate?) {
        val old = renderFlags
        super.updateRenderFlag(newFlag, reason, delegate)
        if (old != renderFlags) {
            updateRenderIfNeed()
        }
    }

    /**[com.angcyo.canvas.render.element.IElement.elementContainsPoint]*/
    override fun rendererContainsPoint(point: PointF): Boolean =
        element?.elementContainsPoint(point) == true

    /**[com.angcyo.canvas.render.element.IElement.elementContainsRect]*/
    override fun rendererContainsRect(rect: RectF): Boolean =
        element?.elementContainsRect(rect) == true

    /**[com.angcyo.canvas.render.element.IElement.elementIntersectRect]*/
    override fun rendererIntersectRect(rect: RectF): Boolean =
        element?.elementIntersectRect(rect) == true

    override fun getElementList(): List<IElement> {
        val result = mutableListOf<IElement>()
        element?.let { result.add(it) }
        return result
    }

    override fun updateRenderProperty(
        target: CanvasRenderProperty?,
        reason: Reason,
        delegate: CanvasRenderDelegate?
    ) {
        super.updateRenderProperty(target, reason, delegate)
        target?.let { element?.updateElementRenderProperty(it) }
    }

    //endregion---core---

}