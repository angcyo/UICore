package com.angcyo.canvas.core.component.control

import com.angcyo.canvas.CanvasDelegate
import com.angcyo.canvas.R
import com.angcyo.canvas.Strategy
import com.angcyo.canvas.core.component.ControlPoint
import com.angcyo.canvas.items.renderer.BaseItemRenderer
import com.angcyo.library.ex._drawable

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/09
 */
class DeleteControlPoint : ControlPoint() {

    init {
        drawable = _drawable(R.drawable.canvas_control_point_delete)
    }

    override fun onClickControlPoint(
        canvasDelegate: CanvasDelegate,
        itemRenderer: BaseItemRenderer<*>
    ) {
        canvasDelegate.removeItemRenderer(itemRenderer, Strategy(Strategy.STRATEGY_TYPE_NORMAL))
    }
}