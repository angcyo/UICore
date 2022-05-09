package com.angcyo.canvas.core.renderer

import android.graphics.Matrix
import android.graphics.RectF
import com.angcyo.canvas.CanvasDelegate
import com.angcyo.canvas.core.CanvasViewBox
import com.angcyo.canvas.core.ICanvasView
import com.angcyo.canvas.core.IRenderer

/**
 * 渲染器
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/01
 */
abstract class BaseRenderer(val canvasView: ICanvasView) : IRenderer {

    /**是否可见, 决定是否绘制*/
    var _visible: Boolean = true

    /**距离坐标系原点的像素坐标*/
    val _bounds = RectF()

    /**在坐标系中的坐标*/
    val _renderBounds = RectF()

    /**相对于视图左上角的坐标*/
    val _visualBounds = RectF()

    val canvasViewBox: CanvasViewBox
        get() = canvasView.getCanvasViewBox()

    /**获取图层描述的名字*/
    override fun getName(): String {
        return "Default"
    }

    override fun isVisible(): Boolean = _visible

    /**此[_bounds]是相对于坐标原点的坐标*/
    override fun getBounds(): RectF = _bounds

    /**此[_renderBounds]是相对于坐标原点的可绘制像素坐标*/
    override fun getRenderBounds(): RectF = _renderBounds

    /**此[_visualBounds]是相对于视图左上角原点的坐标*/
    override fun getVisualBounds(): RectF = _visualBounds

    override fun onCanvasSizeChanged(canvasView: CanvasDelegate) {
        //no op
    }

    /**当[CanvasViewBox]坐标系发生改变时, 实时更新[_visualBounds]*/
    override fun onCanvasBoxMatrixUpdate(
        canvasView: CanvasDelegate,
        matrix: Matrix,
        oldValue: Matrix
    ) {
        canvasViewBox.calcItemRenderBounds(getBounds(), getRenderBounds())
        canvasViewBox.calcItemVisualBounds(getRenderBounds(), getVisualBounds())
    }

    /**调用此方法用来更新[getBounds]
     * 同时需要更新[getRenderBounds],[getVisualBounds]等信息*/
    open fun changeBounds(block: RectF.() -> Unit) {
        getBounds().block()
        //canvasViewBox.canvasView.dispatchItemBoundsChanged()
    }

    /**触发刷新*/
    fun refresh() {
        canvasView.refresh()
    }
}