package com.angcyo.canvas.render.renderer

import android.graphics.PointF
import android.graphics.RectF
import android.graphics.drawable.Drawable
import com.angcyo.canvas.render.core.CanvasRenderDelegate
import com.angcyo.canvas.render.core.Reason
import com.angcyo.canvas.render.core.component.CanvasRenderProperty
import com.angcyo.canvas.render.data.RenderParams
import com.angcyo.canvas.render.element.IElement
import com.angcyo.library.ex.have
import com.angcyo.library.ex.remove

/**用来绘制具体元素的类
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/02/11
 */
class CanvasElementRenderer : BaseRenderer() {

    /**需要绘制的元素*/
    var renderElement: IElement? = null

    //region---core---

    override fun requestRenderDrawable(renderParams: RenderParams?): Drawable? {
        return renderElement?.requestElementRenderDrawable(renderParams)
    }

    override fun isSupportControlPoint(type: Int): Boolean {
        return renderElement?.isElementSupportControlPoint(type)
            ?: super.isSupportControlPoint(type)
    }

    /**更新渲染时, 需要的一些数据*/
    override fun readyRenderIfNeed(params: RenderParams?) {
        super.readyRenderIfNeed(params)
        val element = renderElement ?: return

        val requestProperty = renderFlags.have(RENDERER_FLAG_REQUEST_PROPERTY)
        if (renderProperty == null || requestProperty) {
            renderProperty = element.requestElementRenderProperty()
            renderFlags = renderFlags.remove(RENDERER_FLAG_REQUEST_PROPERTY)
        }

        val requestDrawable = renderFlags.have(RENDERER_FLAG_REQUEST_DRAWABLE)
        if (renderDrawable == null || requestProperty || requestDrawable) {
            renderDrawable = element.requestElementRenderDrawable(params)
            renderFlags = renderFlags.remove(RENDERER_FLAG_REQUEST_DRAWABLE)
        }
    }

    /**[com.angcyo.canvas.render.element.IElement.elementContainsPoint]*/
    override fun rendererContainsPoint(delegate: CanvasRenderDelegate?, point: PointF): Boolean =
        renderElement?.elementHitComponent?.elementContainsPoint(delegate, point) == true

    /**[com.angcyo.canvas.render.element.IElement.elementContainsRect]*/
    override fun rendererContainsRect(delegate: CanvasRenderDelegate?, rect: RectF): Boolean =
        renderElement?.elementHitComponent?.elementContainsRect(delegate, rect) == true

    /**[com.angcyo.canvas.render.element.IElement.elementIntersectRect]*/
    override fun rendererIntersectRect(delegate: CanvasRenderDelegate?, rect: RectF): Boolean =
        renderElement?.elementHitComponent?.elementIntersectRect(delegate, rect) == true

    override fun getElementList(): List<IElement> {
        val result = mutableListOf<IElement>()
        renderElement?.let { result.add(it) }
        return result
    }

    override fun updateRenderProperty(
        target: CanvasRenderProperty?,
        reason: Reason,
        delegate: CanvasRenderDelegate?
    ) {
        super.updateRenderProperty(target, reason, delegate)
        target?.let { renderElement?.updateElementRenderProperty(it) }
    }

    //endregion---core---

}