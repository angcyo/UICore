package com.angcyo.canvas.core.renderer

/**
 * 用来实现撤销和重做
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/09
 */
interface ICanvasStep {

    /**执行撤销操作*/
    fun runUndo()

    /**执行重做操作*/
    fun runRedo()

}