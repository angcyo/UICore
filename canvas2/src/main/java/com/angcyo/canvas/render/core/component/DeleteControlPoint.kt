package com.angcyo.canvas.render.core.component

import com.angcyo.canvas.render.R
import com.angcyo.canvas.render.core.CanvasControlManager
import com.angcyo.library.ex._drawable

/**
 * 删除控制点
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/02/23
 */
class DeleteControlPoint(controlManager: CanvasControlManager) : BaseControlPoint(controlManager) {

    init {
        drawable = _drawable(R.drawable.canvas_render_control_point_delete)
        controlType = CONTROL_TYPE_DELETE
    }

}