package com.angcyo.canvas.core.renderer.items

import android.graphics.RectF
import com.angcyo.canvas.core.CanvasViewBox
import com.angcyo.canvas.core.Transformer

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2022/04/03
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
abstract class BaseItemRenderer(val canvasViewBox: CanvasViewBox, val transformer: Transformer) :
    IItemsRenderer {

    override var visible: Boolean = true

    val bounds = RectF()

    override fun getRenderBounds(): RectF = bounds

}