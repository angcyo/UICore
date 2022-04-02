package com.angcyo.canvas

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.angcyo.canvas.core.*
import com.angcyo.canvas.core.component.CanvasTouchHandler
import com.angcyo.canvas.core.component.XAxis
import com.angcyo.canvas.core.component.YAxis
import com.angcyo.canvas.core.renderer.MonitorRenderer
import com.angcyo.canvas.core.renderer.XAxisRenderer
import com.angcyo.canvas.core.renderer.YAxisRenderer

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/03/29
 */
class CanvasView(context: Context, attributeSet: AttributeSet? = null) :
    View(context, attributeSet), ICanvasView {

    //<editor-fold desc="成员变量">

    /**视图控制*/
    val canvasViewBox = CanvasViewBox(this)

    /**手势控制*/
    val canvasTouchHandler = CanvasTouchHandler(this)

    /**事件回调*/
    val canvasListenerList = mutableSetOf<ICanvasListener>()

    /**额外的渲染器*/
    val rendererList = mutableSetOf<IRenderer>()

    //</editor-fold desc="成员变量">

    //<editor-fold desc="横纵坐标轴">

    /**绘制在顶上的x轴*/
    val xAxisRender = XAxisRenderer(XAxis(), canvasViewBox, Transformer(canvasViewBox))

    /**绘制在左边的y轴*/
    val yAxisRender = YAxisRenderer(YAxis(), canvasViewBox, Transformer(canvasViewBox))

    //</editor-fold desc="横纵坐标轴">

    //<editor-fold desc="渲染组件">

    //</editor-fold desc="渲染组件">

    init {
        rendererList.add(MonitorRenderer(canvasViewBox, Transformer(canvasViewBox)))
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (xAxisRender.axis.enable) {
            xAxisRender.updateRenderBounds(this)
        }
        if (yAxisRender.axis.enable) {
            yAxisRender.updateRenderBounds(this)
        }

        rendererList.forEach {
            it.updateRenderBounds(this)
        }

        canvasViewBox.updateContentBox()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (xAxisRender.axis.enable) {
            xAxisRender.render(canvas)
        }

        if (yAxisRender.axis.enable) {
            yAxisRender.render(canvas)
        }

        rendererList.forEach {
            it.render(canvas)
        }

        //canvas.drawColor(Color)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        canvasListenerList.forEach {
            it.onCanvasTouchEvent(event)
        }
        if (canvasTouchHandler.enable) {
            return canvasTouchHandler.onTouch(this, event)
        }
        return super.onTouchEvent(event)
    }

}