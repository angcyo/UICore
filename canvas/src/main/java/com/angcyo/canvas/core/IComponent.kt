package com.angcyo.canvas.core

import com.angcyo.canvas.CanvasDelegate

/**
 * Canvas组件
 *
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/01
 */
interface IComponent {

    /**可以在此方法中执行动画的帧切换*/
    fun onComputeScroll(canvasDelegate: CanvasDelegate)

}