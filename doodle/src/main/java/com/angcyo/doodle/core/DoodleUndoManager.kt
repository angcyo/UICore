package com.angcyo.doodle.core

import com.angcyo.doodle.DoodleDelegate
import com.angcyo.library.component.IDoStep
import com.angcyo.library.component.Strategy
import com.angcyo.library.component.UndoManager

/**
 * 撤销重做管理
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022-07-25
 */
class DoodleUndoManager(val doodleDelegate: DoodleDelegate) : UndoManager() {

    init {
        onUndoRedoChangeAction = {
            doodleDelegate.dispatchDoodleUndoChanged()
        }
    }

    /**添加一个可以被撤销和重做的操作, 并且立即执行重做
     *
     * [undo] 撤销的操作
     * [redo] 当前需要指定的操作
     * */
    fun addAndRedo(
        strategy: Strategy,
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
        step.runRedo()
        if (strategy.type == Strategy.STRATEGY_TYPE_NORMAL) {
            addUndoAction(step)
            return step
        }
        return null
    }
}