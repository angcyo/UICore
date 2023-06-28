package com.angcyo.library.canvas.single

import android.graphics.Canvas
import android.view.MotionEvent
import android.view.View
import com.angcyo.library.canvas.core.CanvasTouchManager
import com.angcyo.library.canvas.core.CanvasViewBox
import com.angcyo.library.canvas.core.ICanvasView
import com.angcyo.library.canvas.core.IRendererManager
import com.angcyo.library.ex.disableParentInterceptTouchEvent

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/06/28
 */
class SingleMatrixDelegate(val view: View) : ICanvasView {

    var renderViewBox = CanvasViewBox(this)

    /**渲染管理*/
    var renderManager = SingleMatrixRenderManager(this)

    /**手势管理*/
    var touchManager = CanvasTouchManager(this)

    override fun getRawView(): View = view

    override fun computeScroll() {
        touchManager.flingComponent.onComputeScroll()
    }

    override fun getCanvasViewBox(): CanvasViewBox = renderViewBox

    override fun getRenderManager(): IRendererManager = renderManager

    override fun onDraw(canvas: Canvas) {
        renderManager.render(canvas)
    }

    /**是否在画板区域按下*/
    var _isTouchDownInCanvas = false

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        //inner
        val renderBounds = getCanvasViewBox().renderBounds

        val eventX = event.x
        val eventY = event.y

        //事件的坐标是相对于画板左上角的坐标
        val offsetX = -renderBounds.left
        val offsetY = -renderBounds.top
        event.offsetLocation(offsetX, offsetY)

        val x = event.x
        val y = event.y

        //L.d("${action.actionToString()}:${eventX},${eventY} inside:$x,$y $_isTouchDownInCanvas")

        //init
        val action = event.actionMasked
        if (action == MotionEvent.ACTION_DOWN) {
            _isTouchDownInCanvas = renderBounds.contains(eventX, eventY)
        }

        if (!_isTouchDownInCanvas) {
            //按下的时候, 没有在画板区域, 则后续不处理事件
            return false
        }

        //view
        if (action == MotionEvent.ACTION_DOWN) {
            view.disableParentInterceptTouchEvent()
        } else if (action == MotionEvent.ACTION_UP ||
            action == MotionEvent.ACTION_CANCEL
        ) {
            view.disableParentInterceptTouchEvent(false)
        }

        //dispatch
        val handle = touchManager.dispatchTouchEventDelegate(event)
        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
            _isTouchDownInCanvas = false
        }
        //dispatchCanvasTouchEvent(event)
        return handle
    }

    override fun refresh(just: Boolean) {
        if (just) {
            view.invalidate()
        } else {
            view.postInvalidate()
        }
    }

}