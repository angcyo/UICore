package com.angcyo.canvas.core.renderer

import android.graphics.RectF
import com.angcyo.canvas.core.IRenderer
import com.angcyo.canvas.core.Transformer
import com.angcyo.canvas.core.ViewBox

/**
 * 渲染器
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/01
 */
abstract class BaseRenderer(val viewBox: ViewBox, val transformer: Transformer) : IRenderer {

    val bounds = RectF()

    override fun getRenderBounds(): RectF = bounds

}