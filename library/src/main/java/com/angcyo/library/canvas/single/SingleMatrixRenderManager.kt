package com.angcyo.library.canvas.single

import android.graphics.Canvas
import android.graphics.PointF
import android.graphics.RectF
import androidx.core.graphics.withMatrix
import androidx.core.graphics.withTranslation
import com.angcyo.library.annotation.CallPoint
import com.angcyo.library.canvas.annotation.CanvasInsideCoordinate
import com.angcyo.library.canvas.core.IRender
import com.angcyo.library.canvas.core.IRenderElement
import com.angcyo.library.canvas.core.IRenderInside
import com.angcyo.library.canvas.core.IRenderOutside
import com.angcyo.library.canvas.core.IRendererManager
import com.angcyo.library.canvas.element.MonitorRenderElement
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.math.max
import kotlin.math.min

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/06/28
 */
class SingleMatrixRenderManager(val delegate: SingleMatrixDelegate) : IRendererManager {

    val beforeRendererList = CopyOnWriteArrayList<IRender>()

    /**所有的元素渲染器集合*/
    val rendererList = CopyOnWriteArrayList<IRender>()

    val afterRendererList = CopyOnWriteArrayList<IRender>()

    init {
        afterRendererList.add(MonitorRenderElement())
    }

    @CallPoint
    fun render(canvas: Canvas) {
        //before
        for (renderer in beforeRendererList) {
            if (renderer is IRenderOutside) {
                renderer.renderOnOutside(delegate, canvas)
            }
        }

        //inside
        val renderViewBox = delegate.renderViewBox

        val renderBounds = renderViewBox.renderBounds
        val originPoint = renderViewBox.getOriginPoint()

        //偏移到画布bounds
        canvas.withTranslation(renderBounds.left, renderBounds.top) {
            clipRect(0f, 0f, renderBounds.width(), renderBounds.height())//剪切画布
            //平移到画布原点
            translate(originPoint.x, originPoint.y)
            //before
            for (renderer in beforeRendererList) {
                if (renderer is IRenderInside) {
                    canvas.withMatrix(renderViewBox.renderMatrix) {
                        renderer.renderOnInside(delegate, canvas)
                    }
                }
            }
            //---
            for (renderer in rendererList) {
                if (renderer is IRenderInside) {
                    canvas.withMatrix(renderViewBox.renderMatrix) {
                        renderer.renderOnInside(delegate, canvas)
                    }
                }
            }
            //after
            for (renderer in afterRendererList) {
                if (renderer is IRenderInside) {
                    canvas.withMatrix(renderViewBox.renderMatrix) {
                        renderer.renderOnInside(delegate, canvas)
                    }
                }
            }
        }

        //after
        for (renderer in afterRendererList) {
            if (renderer is IRenderOutside) {
                renderer.renderOnOutside(delegate, canvas)
            }
        }
    }

    /**所有元素的边界包裹矩形*/
    fun getRendererBounds(onlySelect: Boolean = true): RectF {
        var left: Float? = null
        var top: Float? = null
        var right: Float? = null
        var bottom: Float? = null
        for (renderer in rendererList) {
            if (renderer is IRenderElement) {
                if (onlySelect && !renderer.canSelectElement()) {
                    continue
                }
                val bounds = renderer.getRenderBounds()
                left = if (left == null) bounds.left else min(left, bounds.left)
                top = if (top == null) bounds.top else min(top, bounds.top)
                right = if (right == null) bounds.right else max(right, bounds.right)
                bottom = if (bottom == null) bounds.bottom else max(bottom, bounds.bottom)
            }
        }
        return RectF(left ?: 0f, top ?: 0f, right ?: 0f, bottom ?: 0f)
    }

    /**通过相对于画板原点的点[point], 查找画板内部符合条件的渲染器
     * [reverse] 是否要反序元素, true:最上层的元素优先, false:最下层的元素优先*/
    override fun findRendererList(@CanvasInsideCoordinate point: PointF): List<IRenderElement> {
        val result = mutableListOf<IRenderElement>()
        val elementRendererList = rendererList
        for (element in elementRendererList) {
            if (element is IRenderElement) {
                if (!element.canSelectElement()) {
                    continue
                }
                if (element.elementContainsPoint(delegate, point)) {
                    result.add(element)
                }
            }
        }
        return result
    }

    override fun addRenderer(renderer: IRenderInside) {
        if (rendererList.contains(renderer)) {
            return
        }
        rendererList.add(renderer)
        delegate.refresh()
    }

    override fun removeRenderer(renderer: IRenderInside) {
        if (rendererList.contains(renderer)) {
            rendererList.remove(renderer)
            delegate.refresh()
        }
    }

    override fun addRendererList(list: List<IRenderInside>) {
        for (renderer in list) {
            if (rendererList.contains(renderer)) {
                continue
            }
            rendererList.add(renderer)
        }
        delegate.refresh()
    }

    override fun removeRendererList(list: List<IRenderInside>) {
        rendererList.removeAll(list)
        delegate.refresh()
    }
}