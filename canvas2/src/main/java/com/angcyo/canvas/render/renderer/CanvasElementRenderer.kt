package com.angcyo.canvas.render.renderer

import android.graphics.Canvas
import android.graphics.PointF
import android.graphics.RectF
import android.graphics.drawable.Drawable
import androidx.core.graphics.withSave
import com.angcyo.canvas.render.core.CanvasRenderDelegate
import com.angcyo.canvas.render.core.Reason
import com.angcyo.canvas.render.core.component.CanvasRenderProperty
import com.angcyo.canvas.render.data.RenderParams
import com.angcyo.canvas.render.element.IElement

/**用来绘制具体元素的类
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/02/11
 */
class CanvasElementRenderer : BaseRenderer() {

    /**需要绘制的元素*/
    var renderElement: IElement? = null
        set(value) {
            field = value
            updateRenderProperty()
        }

    //region---core---

    override fun renderOnInside(canvas: Canvas, params: RenderParams) {
        super.renderOnInside(canvas, params)
        renderProperty?.let {
            val element = renderElement
            if (element == null) {
                renderNoDrawable(canvas, params)
            } else {
                val renderBounds = _renderBounds
                canvas.withSave {
                    translate(renderBounds.left, renderBounds.top)//平移到指定位置
                    element.onRenderInside(this@CanvasElementRenderer, canvas, params)
                }
            }
        }
    }

    override fun requestRenderDrawable(overrideSize: Float?): Drawable? {
        return renderElement?.requestElementDrawable(this, RenderParams().apply {
            this.overrideSize = overrideSize
            if (overrideSize != null) {
                getRendererBounds()?.width()?.let {
                    this.renderDst = overrideSize / it //缩放比例, 对画笔进行反向放大
                }
            }
        })
    }

    override fun isSupportControlPoint(type: Int): Boolean {
        return renderElement?.isElementSupportControlPoint(type)
            ?: super.isSupportControlPoint(type)
    }

    override fun updateRenderProperty() {
        val element = renderElement ?: return
        renderProperty = element.requestElementRenderProperty()
        super.updateRenderProperty()
    }

    /**[com.angcyo.canvas.render.core.component.ElementHitComponent.elementContainsPoint]*/
    override fun rendererContainsPoint(delegate: CanvasRenderDelegate?, point: PointF): Boolean =
        renderElement?.elementHitComponent?.elementContainsPoint(delegate, point) == true

    /**[com.angcyo.canvas.render.core.component.ElementHitComponent.elementContainsRect]*/
    override fun rendererContainsRect(delegate: CanvasRenderDelegate?, rect: RectF): Boolean =
        renderElement?.elementHitComponent?.elementContainsRect(delegate, rect) == true

    /**[com.angcyo.canvas.render.core.component.ElementHitComponent.elementIntersectRect]*/
    override fun rendererIntersectRect(delegate: CanvasRenderDelegate?, rect: RectF): Boolean =
        renderElement?.elementHitComponent?.elementIntersectRect(delegate, rect) == true

    override fun getSingleElementList(): List<IElement> {
        val result = mutableListOf<IElement>()
        renderElement?.let { result.add(it) }
        return result
    }

    /**将渲染属性, 同步更新到[IElement]*/
    override fun updateRenderPropertyTo(
        target: CanvasRenderProperty?,
        reason: Reason,
        delegate: CanvasRenderDelegate?
    ) {
        super.updateRenderPropertyTo(target, reason, delegate)
        updateElementRenderProperty()
    }

    /**更新元素的渲染属性*/
    fun updateElementRenderProperty() {
        renderProperty?.let { renderElement?.updateElementRenderProperty(it) }
    }

    //endregion---core---

}