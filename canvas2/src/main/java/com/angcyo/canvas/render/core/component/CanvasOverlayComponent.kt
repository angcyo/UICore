package com.angcyo.canvas.render.core.component

import android.graphics.Canvas
import android.graphics.PointF
import android.view.MotionEvent
import com.angcyo.canvas.render.core.CanvasRenderDelegate
import com.angcyo.canvas.render.data.RenderParams
import com.angcyo.canvas.render.renderer.BaseRenderer
import com.angcyo.library.BuildConfig
import com.angcyo.library.L
import com.angcyo.library.annotation.Pixel
import com.angcyo.library.canvas.annotation.CanvasInsideCoordinate
import com.angcyo.library.canvas.core.ICanvasComponent
import com.angcyo.library.canvas.core.ICanvasTouchListener
import com.angcyo.library.ex.dp
import kotlin.math.absoluteValue

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @date 2025/05/08
 */
open class CanvasOverlayComponent : BaseRenderer(), ICanvasTouchListener, ICanvasComponent {

    /**核心变量*/
    var canvasDelegate: CanvasRenderDelegate? = null

    //--

    override var isEnableComponent: Boolean = true

    open fun attachToCanvasDelegate(canvasDelegate: CanvasRenderDelegate) {
        this.canvasDelegate = canvasDelegate
        canvasDelegate.touchManager.touchListenerList.add(this)
        canvasDelegate.renderManager.addAfterRendererList(this)
    }


    open fun detachFromCanvasDelegate(canvasDelegate: CanvasRenderDelegate) {
        this.canvasDelegate = null
        canvasDelegate.touchManager.touchListenerList.remove(this)
        canvasDelegate.renderManager.removeAfterRendererList(this)
    }

    override fun isVisibleInRender(
        delegate: CanvasRenderDelegate?,
        fullIn: Boolean,
        def: Boolean
    ): Boolean {
        //return super.isVisibleInRender(delegate, fullIn, def)
        return true
    }

    //--

    override fun renderOnInside(canvas: Canvas, params: RenderParams) {
        super.renderOnInside(canvas, params)
    }

    //--

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        return true
    }

    /**当手指移动的距离大于此值, 是否有效的移动*/
    var translateThreshold = 3 * dp

    private val _tempPoint = PointF()

    @Pixel
    @CanvasInsideCoordinate
    private var _downPoint: PointF? = null

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val eventX = event.getX(event.actionIndex)
        val eventY = event.getY(event.actionIndex)
        _tempPoint.set(eventX, eventY)
        canvasDelegate?.getCanvasViewBox()?.transformToInside(_tempPoint)

        if (BuildConfig.DEBUG) {
            L.d("按下:[${_tempPoint}]")
        }

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                _downPoint = PointF()
                _downPoint?.set(_tempPoint)
            }

            MotionEvent.ACTION_POINTER_DOWN -> {
                _downPoint = null
            }

            MotionEvent.ACTION_UP -> {
                if (_downPoint != null) {
                    val dx1 = _tempPoint.x - _downPoint!!.x
                    val dy1 = _tempPoint.y - _downPoint!!.y

                    if (dx1.absoluteValue < translateThreshold &&
                        dy1.absoluteValue < translateThreshold
                    ) {
                        onClickPointEvent(event, _downPoint!!)
                    }
                }
            }
        }
        return true
    }

    /**点击事件*/
    open fun onClickPointEvent(
        event: MotionEvent,
        @Pixel
        @CanvasInsideCoordinate
        point: PointF
    ) {
        if (BuildConfig.DEBUG) {
            L.d("点击:[${point}]")
        }
    }
}