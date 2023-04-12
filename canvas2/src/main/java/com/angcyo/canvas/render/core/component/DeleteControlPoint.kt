package com.angcyo.canvas.render.core.component

import com.angcyo.canvas.render.R
import com.angcyo.canvas.render.core.CanvasControlManager
import com.angcyo.canvas.render.core.Reason
import com.angcyo.canvas.render.core.Strategy
import com.angcyo.library.ex._drawable

/**
 * 删除控制点
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/02/23
 */
class DeleteControlPoint(controlManager: CanvasControlManager) : BaseControlPoint(controlManager) {

    init {
        drawable = _drawable(R.drawable.canvas_render_control_point_delete)
        controlPointType = CONTROL_TYPE_DELETE
    }

    override fun onClickControlPoint() {
        super.onClickControlPoint()
        val delegate = controlManager.delegate
        val list = delegate.selectorManager.getSelectorRendererList(false)
        delegate.renderManager.removeElementRenderer(list, Reason.user, Strategy.normal)
    }
}