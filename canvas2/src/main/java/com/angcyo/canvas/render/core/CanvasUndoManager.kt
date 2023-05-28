package com.angcyo.canvas.render.core

import com.angcyo.canvas.render.data.ControlRendererInfo
import com.angcyo.canvas.render.renderer.BaseRenderer
import com.angcyo.canvas.render.state.IStateStack
import com.angcyo.library.component.Strategy
import com.angcyo.library.component.UndoManager

/**
 * 撤销重做管理
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023-2-24
 */
class CanvasUndoManager(val delegate: CanvasRenderDelegate) : UndoManager() {

    init {
        onUndoRedoChangeAction = {
            delegate.dispatchRenderUndoChange()

        }
    }

    /**自动进行状态保存和恢复
     * [renderer] 当前操作的渲染器
     * [action] 在操作之前/之后各保存一份状态用来撤销/恢复*/
    fun addToStack(
        renderer: BaseRenderer,
        redoIt: Boolean,
        reason: Reason,
        strategy: Strategy,
        action: () -> Unit
    ) {
        val undoState = ControlRendererInfo(renderer)
        action()//run
        val redoState = ControlRendererInfo(renderer)
        addAndRedo(strategy, redoIt, {
            undoState.restoreState(renderer, reason, it, delegate)
        }) {
            redoState.restoreState(renderer, reason, it, delegate)
        }
    }

    /**添加一个状态到回退栈
     * [oldControlInfo] 旧状态, 并且会使用[ControlRendererInfo.controlRenderer]生成新状态
     * [redoIt] 是否要立即触发重做
     * */
    fun addToStack(
        oldControlInfo: ControlRendererInfo,
        redoIt: Boolean,
        reason: Reason,
        strategy: Strategy
    ) {
        //撤销的状态
        val undoState = oldControlInfo
        //重做的状态
        val redoState = ControlRendererInfo(oldControlInfo.controlRenderer)
        addAndRedo(strategy, redoIt, {
            undoState.restoreState(oldControlInfo.controlRenderer, reason, it, delegate)
        }) {
            redoState.restoreState(oldControlInfo.controlRenderer, reason, it, delegate)
        }
    }

    /**自动进行状态保存和恢复
     * [undoState] 用来撤销的状态存储
     * [redoState] 用来重做的状态存储*/
    fun addToStack(
        renderer: BaseRenderer,
        undoState: IStateStack,
        redoState: IStateStack,
        redoIt: Boolean,
        reason: Reason,
        strategy: Strategy
    ) {
        addAndRedo(strategy, redoIt, {
            undoState.restoreState(renderer, reason, it, delegate)
        }) {
            redoState.restoreState(renderer, reason, it, delegate)
        }
    }

}