package com.angcyo.canvas.core.renderer

import android.graphics.Matrix
import android.graphics.RectF
import com.angcyo.canvas.CanvasView
import com.angcyo.canvas.core.CanvasViewBox
import com.angcyo.canvas.core.IRenderer

/**
 * 渲染器
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/01
 */
abstract class BaseRenderer(val canvasViewBox: CanvasViewBox) : IRenderer {

    /**是否可见, 决定是否绘制*/
    var _visible: Boolean = true

    /**在坐标系中的坐标*/
    val _bounds = RectF()

    /**相对于视图左上角的坐标*/
    val _visualBounds = RectF()

    override fun isVisible(): Boolean = _visible

    /**此[_bounds]是相对于坐标原点的坐标*/
    override fun getRendererBounds(): RectF = _bounds

    /**此[_visualBounds]是相对于视图左上角原点的坐标*/
    override fun getVisualBounds(): RectF = _visualBounds

    override fun onCanvasSizeChanged(canvasView: CanvasView) {
        //no op
    }

    /**当[CanvasViewBox]坐标系发生改变时, 实时更新[_visualBounds]*/
    override fun onCanvasMatrixUpdate(canvasView: CanvasView, matrix: Matrix, oldValue: Matrix) {
        canvasViewBox.calcItemVisibleBounds(this, _visualBounds)
    }

    /**调用地方用来更新[getRendererBounds]*/
    open fun changeBounds(block: RectF.() -> Unit) {
        getRendererBounds().block()
    }
}