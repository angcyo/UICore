package com.angcyo.library.canvas.single

import android.graphics.Canvas
import android.graphics.PointF
import androidx.core.graphics.withMatrix
import androidx.core.graphics.withTranslation
import com.angcyo.library.annotation.CallPoint
import com.angcyo.library.canvas.annotation.CanvasInsideCoordinate
import com.angcyo.library.canvas.core.IRenderElement
import com.angcyo.library.canvas.core.IRenderInside
import com.angcyo.library.canvas.core.IRendererManager
import java.util.concurrent.CopyOnWriteArrayList

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/06/28
 */
class SingleMatrixRenderManager(val delegate: SingleMatrixDelegate) : IRendererManager {

    /**所有的元素渲染器集合*/
    val rendererList = CopyOnWriteArrayList<IRenderInside>()

    @CallPoint
    fun render(canvas: Canvas) {
        val renderViewBox = delegate.renderViewBox

        val renderBounds = renderViewBox.renderBounds
        val originPoint = renderViewBox.getOriginPoint()

        //偏移到画布bounds
        canvas.withTranslation(renderBounds.left, renderBounds.top) {
            clipRect(0f, 0f, renderBounds.width(), renderBounds.height())//剪切画布
            //平移到画布原点
            translate(originPoint.x, originPoint.y)
            //---
            for (renderer in rendererList) {
                canvas.withMatrix(renderViewBox.renderMatrix) {
                    renderer.renderOnInside(canvas)
                }
            }
        }
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