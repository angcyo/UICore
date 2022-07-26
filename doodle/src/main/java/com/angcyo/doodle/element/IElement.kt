package com.angcyo.doodle.element

import android.graphics.Canvas
import com.angcyo.doodle.core.IDoodleItem
import com.angcyo.doodle.layer.BaseLayer
import com.angcyo.library.annotation.CallPoint

/**
 * 绘制的最小单位
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2022/07/25
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
interface IElement : IDoodleItem {

    /**核心的元素绘制方法*/
    @CallPoint
    fun onDraw(layer: BaseLayer, canvas: Canvas)

    //region ---layer---

    /**被添加到[layer]*/
    fun onAddToLayer(layer: BaseLayer) {

    }

    /**被[layer]移除*/
    fun onRemoveFromLayer(layer: BaseLayer) {

    }

    //endregion ---layer---

}