package com.angcyo.doodle.core

import com.angcyo.doodle.element.BaseElement
import com.angcyo.doodle.layer.BaseLayer

/**
 * 事件回调
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2022/07/25
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
interface IDoodleListener {

    //region ---Layer/Element---

    /**有涂层添加*/
    fun onLayerAdd(layer: BaseLayer) {}

    /**有涂层移除*/
    fun onLayerRemove(layer: BaseLayer) {}

    /**操作图层改变回调*/
    fun onOperateLayerChanged(from: BaseLayer?, to: BaseLayer?) {}

    /**笔刷改变回调*/
    fun onTouchRecognizeChanged(from: ITouchRecognize?, to: ITouchRecognize?) {}

    /**回退/恢复栈发生改变后的回调
     * [DoodleUndoManager]*/
    fun onDoodleUndoChanged(undoManager: DoodleUndoManager) {}

    /**当有元素创建时, 回调*/
    fun onCreateElement(element: BaseElement, brush: ITouchRecognize?) {}

    /**派发元素[elementList]追加到[layer]图层中*/
    fun onElementAttach(elementList: List<BaseElement>, layer: BaseLayer) {}

    /**派发元素[elementList]从[layer]图层中移除*/
    fun onElementDetach(elementList: List<BaseElement>, layer: BaseLayer) {}

    //endregion ---Layer/Element---

}