package com.angcyo.canvas.core.component.control

import android.view.MotionEvent
import com.angcyo.canvas.CanvasView
import com.angcyo.canvas.core.component.ControlPoint
import com.angcyo.canvas.core.renderer.items.IItemRenderer

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/09
 */
class CloseControlPoint : ControlPoint() {

    var isTouchDownIn: Boolean = false

    override fun onTouch(
        view: CanvasView,
        itemRenderer: IItemRenderer<*>,
        event: MotionEvent
    ): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                isTouchDownIn = true
            }
            MotionEvent.ACTION_MOVE -> {
                isTouchDownIn = bounds.contains(event.x, event.y)
            }
            MotionEvent.ACTION_UP -> {
                if (isTouchDownIn) {
                    view.removeItemRenderer(itemRenderer)
                }
            }
        }
        return true
    }
}