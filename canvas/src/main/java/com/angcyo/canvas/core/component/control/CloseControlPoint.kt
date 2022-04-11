package com.angcyo.canvas.core.component.control

import com.angcyo.canvas.CanvasView
import com.angcyo.canvas.R
import com.angcyo.canvas.core.component.ControlPoint
import com.angcyo.canvas.items.renderer.IItemRenderer
import com.angcyo.library.ex._drawable

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/09
 */
class CloseControlPoint : ControlPoint() {

    init {
        drawable = _drawable(R.drawable.control_point_close)
    }

    override fun onClickControlPoint(view: CanvasView, itemRenderer: IItemRenderer<*>) {
        view.removeItemRenderer(itemRenderer)
    }
}