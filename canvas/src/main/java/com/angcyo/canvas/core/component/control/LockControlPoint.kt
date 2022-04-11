package com.angcyo.canvas.core.component.control

import android.view.MotionEvent
import com.angcyo.canvas.CanvasView
import com.angcyo.canvas.core.component.ControlPoint
import com.angcyo.canvas.items.renderer.IItemRenderer

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/09
 */
class LockControlPoint : ControlPoint() {

    override fun onTouch(
        view: CanvasView,
        itemRenderer: IItemRenderer<*>,
        event: MotionEvent
    ): Boolean {
        return false
    }

}