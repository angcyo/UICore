package com.angcyo.canvas.core.component

import android.graphics.PointF
import androidx.core.graphics.contains
import com.angcyo.canvas.core.CanvasViewBox
import com.angcyo.canvas.core.renderer.items.IItemsRenderer
import com.angcyo.library.ex.dp

/**
 * 控制渲染的数据组件
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/08
 */
class ControlHandler : BaseComponent() {

    /**当前选中的[IItemsRenderer]*/
    var selectedItemRender: IItemsRenderer? = null

    /**绘制宽高时的偏移量*/
    var sizeOffset = 4 * dp

    /**通过坐标, 找到对应的元素*/
    fun findItemRenderer(canvasViewBox: CanvasViewBox, touchPoint: PointF): IItemsRenderer? {
        val point = canvasViewBox.mapTouchPoint(touchPoint)
        canvasViewBox.canvasView.itemsRendererList.reversed().forEach {
            if (it.getRenderBounds().contains(point)) {
                return it
            }
        }
        return null
    }

}