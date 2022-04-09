package com.angcyo.canvas.core

import com.angcyo.canvas.CanvasView
import com.angcyo.canvas.core.renderer.ICanvasStep
import java.util.*

/**
 * 撤销重做管理
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/09
 */
class CanvasUndoManager(val canvasView: CanvasView) {

    val undoStack = Stack<ICanvasStep>()
    val redoStack = Stack<ICanvasStep>()

    /**撤销*/
    fun undo() {

    }

    /**重做*/
    fun redo() {

    }

    /**是否有撤销操作可以执行*/
    fun canUndo(): Boolean = undoStack.isNotEmpty()

    /**是否有重做操作可以执行*/
    fun canRedo(): Boolean = redoStack.isNotEmpty()

}