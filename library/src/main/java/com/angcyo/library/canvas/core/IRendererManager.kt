package com.angcyo.library.canvas.core

import android.graphics.PointF
import com.angcyo.library.canvas.annotation.CanvasInsideCoordinate

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/06/28
 */
interface IRendererManager {

    /**通过相对于画板原点的点[point], 查找画板内部符合条件的渲染器
     * [reverse] 是否要反序元素, true:最上层的元素优先, false:最下层的元素优先*/
    fun findRendererList(@CanvasInsideCoordinate point: PointF): List<IRenderElement>

    /**添加一个渲染器*/
    fun addRenderer(renderer: IRenderInside) {
    }

    /**移除一个渲染器*/
    fun removeRenderer(renderer: IRenderInside) {
    }

}