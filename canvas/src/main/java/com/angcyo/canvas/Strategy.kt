package com.angcyo.canvas

/**
 * 操作策略, 比如当前的操作需要加入回退栈/或者不需要加入
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/05/06
 */
data class Strategy(
    val type: Int = STRATEGY_TYPE_NORMAL
) {

    companion object {
        /**正常操作, 需要加入撤销栈*/
        const val STRATEGY_TYPE_NORMAL = 1

        /**撤销操作, 需要加入恢复栈*/
        const val STRATEGY_TYPE_UNDO = 2

        /**恢复操作, 需要加入撤销栈*/
        const val STRATEGY_TYPE_REDO = 2
    }
}