package com.angcyo.canvas.core.renderer

import android.graphics.RectF
import com.angcyo.canvas.core.CanvasViewBox
import com.angcyo.canvas.core.IRenderer
import com.angcyo.canvas.core.Transformer

/**
 * 渲染器
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/01
 */
abstract class BaseRenderer(val canvasViewBox: CanvasViewBox, val transformer: Transformer) :
    IRenderer {

    override var visible: Boolean = true

    val bounds = RectF()

    override fun getRenderBounds(): RectF = bounds

}