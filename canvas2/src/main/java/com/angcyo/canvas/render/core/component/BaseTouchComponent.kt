package com.angcyo.canvas.render.core.component

import com.angcyo.canvas.render.core.CanvasRenderDelegate
import com.angcyo.library.canvas.core.BaseCanvasTouchComponent

/**
 * 基础的手势组件
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/02/16
 */
abstract class BaseTouchComponent : BaseCanvasTouchComponent() {

    /**禁止元素的控制手势*/
    fun disableControlHandle(delegate: CanvasRenderDelegate) {
        val target = delegate.controlManager._interceptTarget
        if (target is BaseControl) {
            target.handleControl = false
        }
    }

    //endregion---辅助---

}