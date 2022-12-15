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

        /**来自初始化的操作*/
        const val STRATEGY_TYPE_INIT = 0

        /**正常操作, 需要加入撤销栈*/
        const val STRATEGY_TYPE_NORMAL = 1

        /**来自撤销的操作*/
        const val STRATEGY_TYPE_UNDO = 2

        /**来自恢复的操作*/
        const val STRATEGY_TYPE_REDO = 2

        /**来自预览的操作*/
        const val STRATEGY_TYPE_PREVIEW = 3

        val normal: Strategy
            get() = Strategy(STRATEGY_TYPE_NORMAL)

        val undo: Strategy
            get() = Strategy(STRATEGY_TYPE_UNDO)

        val redo: Strategy
            get() = Strategy(STRATEGY_TYPE_REDO)

        val preview: Strategy
            get() = Strategy(STRATEGY_TYPE_PREVIEW)

        val init: Strategy
            get() = Strategy(STRATEGY_TYPE_INIT)
    }
}