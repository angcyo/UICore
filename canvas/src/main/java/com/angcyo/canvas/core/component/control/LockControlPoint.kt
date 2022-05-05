package com.angcyo.canvas.core.component.control

import com.angcyo.canvas.CanvasView
import com.angcyo.canvas.R
import com.angcyo.canvas.core.component.ControlPoint
import com.angcyo.canvas.items.renderer.BaseItemRenderer
import com.angcyo.library.ex._drawable

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/09
 */
class LockControlPoint : ControlPoint() {

    /**是否锁定了缩放比例, 如果解锁之后, 宽高可以任意比例缩放. 否则就是等比缩放*/
    var isLockScaleRatio: Boolean = true
        set(value) {
            field = value
            drawable = if (value) {
                _drawable(R.drawable.canvas_control_point_lock)
            } else {
                _drawable(R.drawable.canvas_control_point_unlock)
            }
        }

    init {
        drawable = _drawable(R.drawable.canvas_control_point_lock)
    }

    override fun onClickControlPoint(view: CanvasView, itemRenderer: BaseItemRenderer<*>) {
        super.onClickControlPoint(view, itemRenderer)
        view.controlHandler.setLockScaleRatio(!isLockScaleRatio)
    }

}