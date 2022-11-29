package com.angcyo.canvas.core.renderer

import android.graphics.Matrix
import android.graphics.RectF
import android.graphics.drawable.Drawable
import com.angcyo.canvas.CanvasDelegate
import com.angcyo.canvas.Reason
import com.angcyo.canvas.Strategy
import com.angcyo.canvas.core.CanvasViewBox
import com.angcyo.canvas.core.ICanvasView
import com.angcyo.canvas.core.IRenderer
import com.angcyo.canvas.core.RenderParams
import com.angcyo.library.component.ScalePictureDrawable
import com.angcyo.library.ex.emptyRectF
import com.angcyo.library.ex.withPicture

/**
 * 渲染器
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/01
 */
abstract class BaseRenderer(val canvasView: ICanvasView) : IRenderer {

    /**是否可见, 决定是否绘制*/
    var _visible: Boolean = true

    /**距离坐标系原点的像素坐标*/
    val _bounds = emptyRectF()

    /**在坐标系中的坐标*/
    val _renderBounds = emptyRectF()

    /**相对于视图左上角的坐标*/
    val _visualBounds = emptyRectF()

    val canvasViewBox: CanvasViewBox
        get() = canvasView.getCanvasViewBox()

    var _name: CharSequence? = null

    /**获取图层描述的名字*/
    override fun getName(): CharSequence? = _name ?: "Default"

    override fun isVisible(): Boolean = _visible

    /**设置可见性*/
    fun setVisible(visible: Boolean, strategy: Strategy = Strategy.normal) {
        val oldValue = isVisible()
        if (visible == oldValue) {
            return
        }
        _visible = visible
        canvasView.dispatchItemVisibleChanged(this, visible)
        refresh()

        if (strategy.type == Strategy.STRATEGY_TYPE_NORMAL) {
            canvasView.getCanvasUndoManager().addUndoAction(object : ICanvasStep {
                override fun runUndo() {
                    setVisible(oldValue, Strategy.undo)
                }

                override fun runRedo() {
                    setVisible(visible, Strategy.redo)
                }
            })
        }
    }

    /**此[_bounds]是相对于坐标原点的坐标, 不带旋转属性*/
    override fun getBounds(): RectF = _bounds

    /**此[_renderBounds]是相对于[View]左上角原点的可绘制像素坐标, 不带旋转属性*/
    override fun getRenderBounds(): RectF = _renderBounds

    /**此[_visualBounds]是相对于[View]左上角原点的坐标, 不带旋转属性, 映射了坐标系的[Matrix]*/
    override fun getVisualBounds(): RectF = _visualBounds

    override fun onCanvasSizeChanged(canvasView: CanvasDelegate) {
        //no op
    }

    /**当[CanvasViewBox]坐标系发生改变时, 实时更新[_visualBounds]*/
    override fun onCanvasBoxMatrixUpdate(
        canvasView: CanvasDelegate,
        matrix: Matrix,
        oldMatrix: Matrix
    ) {
        canvasViewBox.calcItemRenderBounds(getBounds(), getRenderBounds())
        canvasViewBox.calcItemVisualBounds(getRenderBounds(), getVisualBounds())
    }

    /**调用此方法用来更新[getBounds]
     * 同时需要更新[getRenderBounds],[getVisualBounds]等信息
     * @return 修改是否成功*/
    open fun changeBoundsAction(
        reason: Reason = Reason(Reason.REASON_USER, true, Reason.REASON_FLAG_BOUNDS),
        block: RectF.() -> Unit
    ): Boolean {
        getBounds().block()
        return true
    }

    /**触发刷新*/
    fun refresh() {
        canvasView.refresh()
    }

    override fun preview(renderParams: RenderParams): Drawable? {
        val renderBounds = getRenderBounds()
        val oldRenderRect = RectF(renderBounds)

        renderBounds.set(0f, 0f, renderBounds.width(), renderBounds.height())
        val result = ScalePictureDrawable(
            withPicture(
                renderBounds.width().toInt(),
                renderBounds.height().toInt()
            ) {
                render(this, renderParams)
            })
        renderBounds.set(oldRenderRect)
        return result
    }
}