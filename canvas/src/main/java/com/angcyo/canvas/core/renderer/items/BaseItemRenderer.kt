package com.angcyo.canvas.core.renderer.items

import com.angcyo.canvas.core.CanvasViewBox
import com.angcyo.canvas.core.items.BaseItem
import com.angcyo.canvas.core.renderer.BaseRenderer

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2022/04/03
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
abstract class BaseItemRenderer<T : BaseItem>(canvasViewBox: CanvasViewBox) :
    BaseRenderer(canvasViewBox), IItemRenderer<T> {

    /**需要渲染的数据*/
    override var rendererItem: T? = null
        set(value) {
            val old = field
            field = value
            if (old != value && value != null) {
                onUpdateRendererItem(value)
            }
        }
}