package com.angcyo.doodle.core

import com.angcyo.doodle.DoodleDelegate
import java.util.*

/**
 * 撤销重做管理
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022-07-25
 */
class DoodleUndoManager(val doodleDelegate: DoodleDelegate) {

    /**可以撤销的栈*/
    val undoStack = Stack<IDoodleStep>()

    /**可以重做的栈*/
    val redoStack = Stack<IDoodleStep>()

    /**添加一个可以被撤销操作*/
    fun addUndoAction(step: IDoodleStep) {
        clearRedo()
        _addUndoAction(step)
    }

    fun _addUndoAction(step: IDoodleStep) {
        undoStack.add(step)
        doodleDelegate.dispatchDoodleUndoChanged()
    }

    /**添加一个可以被恢复操作*/
    fun addRedoAction(step: IDoodleStep) {
        redoStack.add(step)
        doodleDelegate.dispatchDoodleUndoChanged()
    }

    /**清空恢复栈, 在添加新的撤销栈时, 需要清空恢复栈*/
    fun clearRedo() {
        redoStack.clear()
        doodleDelegate.dispatchDoodleUndoChanged()
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
     *
     * [undo] 撤销的操作
     * [redo] 当前需要指定的操作
     * */
    fun addAndRedo(
        strategy: Strategy,
        undo: (strategy: Strategy) -> Unit,
        redo: (strategy: Strategy) -> Unit
    ): IDoodleStep? {
        val step = object : IDoodleStep {
            override fun runUndo() {
                undo(Strategy.Undo())
            }

            override fun runRedo() {
                redo(Strategy.Redo())
            }
        }
        step.runRedo()
        if (strategy is Strategy.Normal) {
            addUndoAction(step)
            return step
        }
        return null
    }

}