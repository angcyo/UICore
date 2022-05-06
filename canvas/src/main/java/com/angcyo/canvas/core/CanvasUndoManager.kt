package com.angcyo.canvas.core

import com.angcyo.canvas.core.renderer.ICanvasStep
import java.util.*

/**
 * 撤销重做管理
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/09
 */
class CanvasUndoManager(val canvasView: ICanvasView) {

    val undoStack = Stack<ICanvasStep>()
    val redoStack = Stack<ICanvasStep>()

    /**添加一个撤销操作*/
    fun addUndoAction(step: ICanvasStep) {
        clearRedo()
        _addUndoAction(step)
    }

    fun _addUndoAction(step: ICanvasStep) {
        undoStack.add(step)
        canvasView.dispatchCanvasUndoChanged()
    }

    /**添加一个恢复操作*/
    fun addRedoAction(step: ICanvasStep) {
        redoStack.add(step)
        canvasView.dispatchCanvasUndoChanged()
    }

    /**清空恢复栈, 在添加新的撤销栈时, 需要清空恢复栈*/
    fun clearRedo() {
        redoStack.clear()
        canvasView.dispatchCanvasUndoChanged()
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

}