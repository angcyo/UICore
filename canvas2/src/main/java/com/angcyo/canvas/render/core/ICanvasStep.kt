package com.angcyo.canvas.render.core

/**
 * 用来实现撤销和重做
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023-2-24
 */
interface ICanvasStep {

    /**执行撤销操作*/
    fun runUndo()

    /**执行重做操作*/
    fun runRedo()

}