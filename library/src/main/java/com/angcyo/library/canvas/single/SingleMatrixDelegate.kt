package com.angcyo.library.canvas.single

import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.RectF
import android.view.MotionEvent
import android.view.View
import com.angcyo.library.canvas.annotation.CanvasInsideCoordinate
import com.angcyo.library.canvas.core.CanvasTouchManager
import com.angcyo.library.canvas.core.CanvasViewBox
import com.angcyo.library.canvas.core.ICanvasView
import com.angcyo.library.canvas.core.IRendererManager
import com.angcyo.library.canvas.core.Reason
import com.angcyo.library.ex.disableParentInterceptTouchEvent
import com.angcyo.library.ex.dp
import com.angcyo.library.ex.getScaleX
import com.angcyo.library.ex.getScaleY
import kotlin.math.min

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

    /**显示指定矩形的范围*/
    fun showRectBounds(
        @CanvasInsideCoordinate
        rect: RectF? = null,
        margin: Float = 20 * dp,
        zoomIn: Boolean = true /*自动放大*/,
        zoomOut: Boolean = true /*自动缩小*/,
        lockScale: Boolean = true,
        anim: Boolean = true,
        offsetRectTop: Boolean = false,
        offsetX: Float = 0f,
        offsetY: Float = 0f,
        finish: (isCancel: Boolean) -> Unit = {}
    ) {
        val rect = rect ?: renderManager.getRendererBounds()
        val renderViewBox = renderViewBox
        if (!renderViewBox.isCanvasInit) {
            view.post {
                showRectBounds(
                    rect,
                    margin,
                    zoomIn,
                    zoomOut,
                    lockScale,
                    anim,
                    offsetRectTop,
                    offsetX,
                    offsetY,
                    finish
                )
            }
            return
        }

        val contentWidth = renderViewBox.renderBounds.width()
        val contentHeight = renderViewBox.renderBounds.height()

        val centerX = contentWidth / 2
        val centerY = contentHeight / 2
        val originPoint = renderViewBox.getOriginPoint()

        //先将坐标系移动到view的中心
        val coordinateTranslateX = centerX - originPoint.x
        val coordinateTranslateY = centerY - originPoint.y

        //再计算目标中心需要偏移的距离量
        val translateX = coordinateTranslateX - rect.centerX()
        val translateY = coordinateTranslateY - rect.centerY()

        val matrix = Matrix()
        //平移
        matrix.setTranslate(translateX, translateY)

        val width = rect.width() + margin * 2
        val height = rect.height() + margin * 2

        var scaleX = renderViewBox.renderMatrix.getScaleX()
        var scaleY = renderViewBox.renderMatrix.getScaleY()

        val visibleWidth = renderViewBox.visibleBoundsInside.width()
        val visibleHeight = renderViewBox.visibleBoundsInside.height()

        if (zoomOut) {
            //需要自动缩小
            if (width > visibleWidth || height > visibleHeight) {
                //目标的宽高, 大于画布当前可见的宽高
                scaleX = (contentWidth - margin * 2) / rect.width()
                scaleY = (contentHeight - margin * 2) / rect.height()
            }
        }

        if (zoomIn) {
            //需要自动放大
            if (width < visibleWidth || height < visibleHeight) {
                //目标的宽高, 小于画布当前可见的宽高
                scaleX = (contentWidth - margin * 2) / rect.width()
                scaleY = (contentHeight - margin * 2) / rect.height()
            }
        }

        if (lockScale) {
            scaleX = min(scaleX, scaleY)
            scaleY = scaleX
        }

        //自动缩小
        //自动放大
        matrix.postScale(
            scaleX,
            scaleY,
            centerX,
            centerY
        )

        //偏移量的平移
        matrix.postTranslate(offsetX, offsetY)

        if (offsetRectTop) {
            val offset = (renderViewBox.renderBounds.height() - rect.height() * scaleY) / 2 - margin
            matrix.postTranslate(0f, -offset)
        }

        //更新
        renderViewBox.changeRenderMatrix(matrix, anim, Reason.user, finish)
    }

}