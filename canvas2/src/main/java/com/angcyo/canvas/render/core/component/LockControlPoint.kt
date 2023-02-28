package com.angcyo.canvas.render.core.component

import com.angcyo.canvas.render.R
import com.angcyo.canvas.render.core.CanvasControlManager
import com.angcyo.canvas.render.core.Reason
import com.angcyo.library.ex._drawable

/**
 * 锁定宽高比控制点
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/02/23
 */
class LockControlPoint(controlManager: CanvasControlManager) : BaseControlPoint(controlManager) {

    /**是否锁定了宽高比*/
    var isLockScaleRatio: Boolean = true
        set(value) {
            field = value
            drawable = if (value) {
                _drawable(R.drawable.canvas_render_control_point_lock)
            } else {
                _drawable(R.drawable.canvas_render_control_point_unlock)
            }
        }

    init {
        controlType = CONTROL_TYPE_LOCK
    }

    override fun onClickControlPoint() {
        super.onClickControlPoint()
        isLockScaleRatio = !isLockScaleRatio
        //---
        val delegate = controlManager.delegate
        controlManager.scaleControlPoint.isLockScaleRatio = isLockScaleRatio
        delegate.selectorManager.selectorComponent.updateLockScaleRatio(
            isLockScaleRatio,
            Reason.user,
            delegate
        )
    }
}