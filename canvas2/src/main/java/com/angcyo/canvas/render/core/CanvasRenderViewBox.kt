package com.angcyo.canvas.render.core

import com.angcyo.canvas.render.core.component.BaseControlPoint
import com.angcyo.library.canvas.core.CanvasViewBox
import com.angcyo.library.canvas.core.Reason
import com.angcyo.library.ex.add

/**
 * 渲染器的坐标转换, 和可视范围的配置/限制
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/02/11
 */
class CanvasRenderViewBox(val delegate: CanvasRenderDelegate) : CanvasViewBox(delegate) {

    override fun translateBy(dx: Float, dy: Float, anim: Boolean, reason: Reason) {
        reason.apply {
            controlType = (controlType ?: 0).add(BaseControlPoint.CONTROL_TYPE_TRANSLATE)
        }
        super.translateBy(dx, dy, anim, reason)
    }

    override fun translateTo(tx: Float, ty: Float, anim: Boolean, reason: Reason) {
        reason.apply {
            controlType = (controlType ?: 0).add(BaseControlPoint.CONTROL_TYPE_TRANSLATE)
        }
        super.translateTo(tx, ty, anim, reason)
    }

    override fun scaleBy(
        sx: Float,
        sy: Float,
        px: Float,
        py: Float,
        anim: Boolean,
        reason: Reason
    ) {
        reason.apply {
            controlType = (controlType ?: 0).add(BaseControlPoint.CONTROL_TYPE_SCALE)
        }
        super.scaleBy(sx, sy, px, py, anim, reason)
    }

    override fun scaleTo(sx: Float, sy: Float, anim: Boolean, reason: Reason) {
        reason.apply {
            controlType = (controlType ?: 0).add(BaseControlPoint.CONTROL_TYPE_SCALE)
        }
        super.scaleTo(sx, sy, anim, reason)
    }

}