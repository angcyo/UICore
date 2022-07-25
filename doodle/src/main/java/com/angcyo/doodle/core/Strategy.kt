package com.angcyo.doodle.core

/**
 * 操作策略, 比如当前的操作需要加入回退栈/或者不需要加入
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022-07-25
 */

sealed class Strategy {

    companion object {
        /**正常操作, 需要加入撤销栈*/
        const val STRATEGY_TYPE_NORMAL = 1

        /**来自撤销的操作*/
        const val STRATEGY_TYPE_UNDO = 2

        /**来自恢复的操作*/
        const val STRATEGY_TYPE_REDO = 2

        /**来自预览的操作*/
        const val STRATEGY_TYPE_PREVIEW = 3
    }

    data class Normal(val type: Int = STRATEGY_TYPE_NORMAL) : Strategy()
    data class Undo(val type: Int = STRATEGY_TYPE_UNDO) : Strategy()
    data class Redo(val type: Int = STRATEGY_TYPE_REDO) : Strategy()
    data class Preview(val type: Int = STRATEGY_TYPE_PREVIEW) : Strategy()

    /*object Normal : Strategy()
    object Undo : Strategy()
    object Redo : Strategy()
    object Preview : Strategy()*/
}