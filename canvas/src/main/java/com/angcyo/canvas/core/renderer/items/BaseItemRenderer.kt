package com.angcyo.canvas.core.renderer.items

import com.angcyo.canvas.core.CanvasViewBox
import com.angcyo.canvas.core.Transformer
import com.angcyo.canvas.core.renderer.BaseRenderer

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2022/04/03
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
abstract class BaseItemRenderer(canvasViewBox: CanvasViewBox) :
    BaseRenderer(canvasViewBox), IItemRenderer {

    override val transformer: Transformer = Transformer(canvasViewBox)

}