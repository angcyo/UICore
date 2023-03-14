package com.angcyo.canvas.render.core

import com.angcyo.canvas.render.data.ControlRendererInfo
import com.angcyo.canvas.render.state.IStateStack
import com.angcyo.canvas.render.renderer.BaseRenderer
import java.util.*

/**
 * 撤销重做管理
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023-2-24
 */
class CanvasUndoManager(val delegate: CanvasRenderDelegate) {

    val undoStack = Stack<ICanvasStep>()
    val redoStack = Stack<ICanvasStep>()

    /**添加一个撤销操作*/
    fun addUndoAction(step: ICanvasStep) {
        clearRedo()
        _addUndoAction(step)
    }

    fun _addUndoAction(step: ICanvasStep) {
        undoStack.add(step)
        delegate.dispatchRenderUndoChange()
    }

    /**添加一个恢复操作*/
    fun addRedoAction(step: ICanvasStep) {
        redoStack.add(step)
        delegate.dispatchRenderUndoChange()
    }

    /**清空恢复栈, 在添加新的撤销栈时, 需要清空恢复栈*/
    fun clearRedo() {
        redoStack.clear()
        delegate.dispatchRenderUndoChange()
    }

    /**清空所有栈*/
    fun clear() {
        undoStack.clear()
        clearRedo()
    }

    /**执行撤销*/
    fun undo() {
        if (undoStack.isNotEmpty()) {
            val step = undoStack.pop()
            step.runUndo()
            addRedoAction(step)
        }
    }

    /**执行重做*/
    fun redo() {
        if (redoStack.isNotEmpty()) {
            val step = redoStack.pop()
            step.runRedo()
            _addUndoAction(step)
        }
    }

    /**是否有撤销操作可以执行*/
    fun canUndo(): Boolean = undoStack.isNotEmpty()

    /**是否有重做操作可以执行*/
    fun canRedo(): Boolean = redoStack.isNotEmpty()

    /**添加一个可以被撤销和重做的操作, 并且立即执行重做
     * [redoIt] 是否要立即执行[redo]*/
    fun addAndRedo(
        strategy: Strategy,
        redoIt: Boolean,
        undo: (strategy: Strategy) -> Unit,
        redo: (strategy: Strategy) -> Unit
    ): ICanvasStep? {
        val step = object : ICanvasStep {
            override fun runUndo() {
                undo(Strategy.undo)
            }

            override fun runRedo() {
                redo(Strategy.redo)
            }
        }
        if (redoIt) {
            step.runRedo()
        }
        if (strategy.type == Strategy.STRATEGY_TYPE_NORMAL) {
            addUndoAction(step)
            return step
        }
        return null
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
            undoState.restoreState(reason, it, delegate)
        }) {
            redoState.restoreState(reason, it, delegate)
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
            undoState.restoreState(reason, it, delegate)
        }) {
            redoState.restoreState(reason, it, delegate)
        }
    }

    /**自动进行状态保存和恢复
     * [undoState] 用来撤销的状态存储
     * [redoState] 用来重做的状态存储*/
    fun addToStack(
        undoState: IStateStack,
        redoState: IStateStack,
        redoIt: Boolean,
        reason: Reason,
        strategy: Strategy
    ) {
        addAndRedo(strategy, redoIt, {
            undoState.restoreState(reason, it, delegate)
        }) {
            redoState.restoreState(reason, it, delegate)
        }
    }

}