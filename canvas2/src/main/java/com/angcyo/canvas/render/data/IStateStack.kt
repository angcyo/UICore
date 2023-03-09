package com.angcyo.canvas.render.data

import com.angcyo.canvas.render.core.CanvasRenderDelegate
import com.angcyo.canvas.render.core.Reason

/**
 * 用来存储状态, 恢复状态的接口
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/03/09
 */
interface IStateStack {
    /**恢复状态*/
    fun restoreState(reason: Reason, delegate: CanvasRenderDelegate?)
}