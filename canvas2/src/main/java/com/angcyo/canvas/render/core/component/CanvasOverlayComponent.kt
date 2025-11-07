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

    @CanvasInsideCoordinate
    private val _tempPoint = PointF()
    private var _lastEventX = 0f
    private var _lastEventY = 0f

    @Pixel
    @CanvasInsideCoordinate
    protected var _downPoint: PointF? = null

    @Pixel
    @CanvasInsideCoordinate
    protected var _movePoint: PointF? = null

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val eventX = event.getX(event.actionIndex)
        val eventY = event.getY(event.actionIndex)
        _tempPoint.set(eventX, eventY)
        canvasDelegate?.getCanvasViewBox()?.transformToInside(_tempPoint)

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                if (BuildConfig.DEBUG) {
                    L.d("按下:[${_tempPoint}]")
                }
                _lastEventX = eventX
                _lastEventY = eventY
                _downPoint = PointF()
                _downPoint?.set(_tempPoint)
            }

            MotionEvent.ACTION_POINTER_DOWN -> {
                _downPoint = null
                _movePoint = null
            }

            MotionEvent.ACTION_MOVE -> {
                if (_movePoint == null) {
                    _movePoint = PointF()
                    _movePoint?.set(_tempPoint)
                } else {
                    val dx1 = _lastEventX - eventX
                    val dy1 = _lastEventY - eventY
                    if (dx1.absoluteValue >= translateThreshold ||
                        dy1.absoluteValue >= translateThreshold
                    ) {
                        _movePoint?.set(_tempPoint)
                        _lastEventX = eventX
                        _lastEventY = eventY
                    }
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (_downPoint != null) {
                    val dx1 = _lastEventX - eventX
                    val dy1 = _lastEventY - eventY

                    if (dx1.absoluteValue < translateThreshold &&
                        dy1.absoluteValue < translateThreshold
                    ) {
                        onClickPointEvent(event, _downPoint!!)
                    }
                }
                _downPoint = null
                _movePoint = null
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