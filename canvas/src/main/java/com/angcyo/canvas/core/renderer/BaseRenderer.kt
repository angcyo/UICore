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
import com.angcyo.library.annotation.Pixel
import com.angcyo.library.component.ScalePictureDrawable
import com.angcyo.library.ex.emptyRectF
import com.angcyo.library.ex.isInitialize
import com.angcyo.library.ex.isOutOf
import com.angcyo.library.ex.withPicture

/**
 * 渲染器
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/01
 */
abstract class BaseRenderer(val canvasView: ICanvasView) : IRenderer {

    /**是否可见, 决定是否绘制*/
    var _visible: Boolean = true

    /**是否锁定, 决定是否可以操作*/
    var _isLock: Boolean = false

    /**距离坐标系原点的像素坐标*/
    val _bounds = emptyRectF(Float.MIN_VALUE)

    /**在坐标系中的坐标*/
    val _renderBounds = emptyRectF(Float.MIN_VALUE)

    /**相对于视图左上角的坐标*/
    val _visualBounds = emptyRectF(Float.MIN_VALUE)

    //---

    val canvasDelegate: CanvasDelegate
        get() = canvasView as CanvasDelegate

    val canvasViewBox: CanvasViewBox
        get() = canvasView.getCanvasViewBox()

    var _name: CharSequence? = null

    /**当前的渲染器绘制的时候需要[Canvas]剪切内容*/
    var needCanvasClipContent: Boolean = true

    /**当前的渲染器绘制的时候是否需要旋转
     * [getDrawRotate]*/
    var needCanvasRotation: Boolean = true

    /**当前的渲染器绘制的时候需要收到[CanvasViewBox]的[Matrix]影响*/
    var needCanvasContentMatrix: Boolean = true

    /**当前的渲染器绘制的时候是否需要偏移到坐标系原点*/
    var needCanvasTranslateCoordinateOrigin: Boolean = false

    //---

    /**覆盖渲染*/
    fun overlayRender() {
        needCanvasClipContent = false
        needCanvasContentMatrix = false
        needCanvasTranslateCoordinateOrigin = false
    }

    //---

    /**获取图层描述的名字*/
    override fun getName(): CharSequence? = _name ?: "Default"

    override fun isVisible(renderParams: RenderParams?): Boolean =
        if (renderParams?.isPreview == true) true else _visible

    override fun isLock(): Boolean = _isLock

    /**设置可见性*/
    open fun setVisible(visible: Boolean, strategy: Strategy = Strategy.normal) {
        val oldValue = isVisible(null)
        if (visible == oldValue) {
            return
        }
        _visible = visible

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

        onRendererVisibleChanged(oldValue, visible, strategy)
        canvasView.dispatchItemVisibleChanged(this, visible, strategy)
        refresh()
    }

    /**设置锁定状态, 锁定后的图层不能进行控制操作, 只能渲染*/
    open fun setLockLayer(lock: Boolean, strategy: Strategy = Strategy.normal) {
        val oldValue = isLock()
        if (lock == oldValue) {
            return
        }
        _isLock = lock

        if (strategy.type == Strategy.STRATEGY_TYPE_NORMAL) {
            canvasView.getCanvasUndoManager().addUndoAction(object : ICanvasStep {
                override fun runUndo() {
                    setLockLayer(oldValue, Strategy.undo)
                }

                override fun runRedo() {
                    setLockLayer(lock, Strategy.redo)
                }
            })
        }

        onRendererLockChanged(oldValue, lock, strategy)
        canvasView.dispatchItemLockChanged(this, lock, strategy)
        refresh()
    }

    /**自身的回调, 可见性改变了*/
    open fun onRendererVisibleChanged(from: Boolean, to: Boolean, strategy: Strategy) {}

    /**自身的锁定状态改变回调*/
    open fun onRendererLockChanged(from: Boolean, to: Boolean, strategy: Strategy) {}

    /**此[_bounds]是相对于坐标原点的坐标, 不带旋转属性
     *
     * [com.angcyo.canvas.items.renderer.IItemRenderer.getRotateBounds]*/
    override fun getBounds(): RectF = _bounds

    /**此[_renderBounds]是相对于[View]左上角原点的可绘制像素坐标, 不带旋转属性
     * [com.angcyo.canvas.items.renderer.IItemRenderer.getRotateBounds]
     * */
    override fun getRenderBounds(): RectF = _renderBounds

    /**此[_visualBounds]是相对于[View]左上角原点的坐标, 不带旋转属性, 映射了坐标系的[Matrix]
     *
     * [com.angcyo.canvas.items.renderer.IItemRenderer.getVisualRotateBounds]
     * */
    override fun getVisualBounds(): RectF = _visualBounds

    override fun onCanvasSizeChanged(canvasView: CanvasDelegate) {
        //no op
    }

    /**[needCanvasRotation]*/
    open fun getDrawRotate(): Float = 0f

    /**当前的渲染器是否超过可视化渲染区域, 超过区域的渲染器不会被渲染 */
    open fun isOutOfVisualRect(@Pixel visualRect: RectF): Boolean {
        return if (getBounds().isInitialize(Float.MIN_VALUE)) {
            getRenderBounds().isOutOf(visualRect)
        } else false
    }

    /**当[CanvasViewBox]坐标系发生改变时, 实时更新[_visualBounds]*/
    override fun onCanvasBoxMatrixUpdate(
        canvasView: CanvasDelegate,
        matrix: Matrix,
        oldMatrix: Matrix,
        isEnd: Boolean
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