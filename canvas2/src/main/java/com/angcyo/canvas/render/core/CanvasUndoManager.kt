package com.angcyo.canvas.render.core

import com.angcyo.canvas.render.data.ControlRendererInfo
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
        redoIt: Boolean = true,
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

    /**添加一个状态到回退栈*/
    fun addToStack(
        controlInfo: ControlRendererInfo,
        redoIt: Boolean = false,
        strategy: Strategy = Strategy.normal
    ) {
        //撤销的状态
        val undoState = controlInfo
        //重做的状态
        val redoState = ControlRendererInfo(controlInfo.controlRenderer)
        addAndRedo(strategy, redoIt, {
            undoState.restoreState(Reason.code, delegate)
        }) {
            redoState.restoreState(Reason.code, delegate)
        }
    }

}