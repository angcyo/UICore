package com.angcyo.library.component

import com.angcyo.library.ex.Action
import java.util.Stack

/**
 * 回退栈管理
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2023/05/28
 */
open class UndoManager {

    /**撤销栈*/
    val undoStack = Stack<IDoStep>()

    /**重做栈*/
    val redoStack = Stack<IDoStep>()

    /**栈改变通知*/
    var onUndoRedoChangeAction: Action? = null

    /**添加一个撤销操作*/
    fun addUndoAction(step: IDoStep) {
        clearRedo()
        addUndoActionInner(step)
    }

    protected open fun addUndoActionInner(step: IDoStep) {
        undoStack.add(step)
        onUndoRedoChangeAction?.invoke()
    }

    /**添加一个撤销操作*/
    fun addRedoAction(step: IDoStep) {
        addRedoActionInner(step)
    }

    protected open fun addRedoActionInner(step: IDoStep) {
        redoStack.add(step)
        onUndoRedoChangeAction?.invoke()
    }

    /**清空恢复栈, 在添加新的撤销栈时, 需要清空恢复栈*/
    open fun clearRedo() {
        redoStack.clear()
        onUndoRedoChangeAction?.invoke()
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
            addUndoActionInner(step)
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
    ): IDoStep? {
        val step = object : IDoStep {
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
}

/**用来实现撤销和重做*/
interface IDoStep {

    /**执行撤销操作*/
    fun runUndo()

    /**执行重做操作*/
    fun runRedo()
}