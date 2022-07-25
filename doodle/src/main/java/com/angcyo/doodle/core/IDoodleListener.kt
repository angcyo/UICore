package com.angcyo.doodle.core

import com.angcyo.doodle.layer.BaseLayer

/**
 * 事件
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2022/07/25
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
interface IDoodleListener {

    //region ---Layer---

    /**操作图层改变回调*/
    fun onOperateLayerChanged(from: BaseLayer?, to: BaseLayer?) {}

    /**笔刷改变回调*/
    fun onTouchRecognizeChanged(from: ITouchRecognize?, to: ITouchRecognize?) {}

    /**回退/恢复栈发生改变后的回调
     * [DoodleUndoManager]*/
    fun onDoodleUndoChanged(undoManager: DoodleUndoManager) {}

    //endregion ---Layer---

}