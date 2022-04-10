package com.angcyo.canvas.core.renderer

import android.graphics.RectF
import com.angcyo.canvas.core.CanvasViewBox
import com.angcyo.canvas.core.IRenderer

/**
 * 渲染器
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/01
 */
abstract class BaseRenderer(val canvasViewBox: CanvasViewBox) :
    IRenderer {

    override var visible: Boolean = true

    val bounds = RectF()

    /**此[bounds]是相对于坐标原点的坐标*/
    override fun getRendererBounds(): RectF = bounds

    /**获取当前[bounds]相对于[view]左上角的坐标
     * [com.angcyo.canvas.core.CanvasViewBox.calcItemVisibleBounds]*/
    fun getRendererVisibleBounds(result: RectF): RectF {
        return canvasViewBox.calcItemVisibleBounds(this, result)
    }
}