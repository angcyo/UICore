package com.angcyo.canvas.render.state

import com.angcyo.canvas.render.core.CanvasRenderDelegate
import com.angcyo.canvas.render.core.Reason
import com.angcyo.canvas.render.core.Strategy
import com.angcyo.canvas.render.renderer.BaseRenderer

/**
 * 用来存储状态, 恢复状态的接口
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/03/09
 */
interface IStateStack {

    /**保存状态当前的状态*/
    fun saveState(renderer: BaseRenderer, delegate: CanvasRenderDelegate?)

    /**恢复状态
     * [reason] 恢复的原因, 用户操作/代码操作/预览操作等
     * [strategy] 操作的策略, 正常/重做/恢复/预览等
     * */
    fun restoreState(
        renderer: BaseRenderer,
        reason: Reason,
        strategy: Strategy,
        delegate: CanvasRenderDelegate?
    )
}