package com.angcyo.library.canvas.component

import android.graphics.PointF
import android.view.MotionEvent
import com.angcyo.library.BuildConfig
import com.angcyo.library.L
import com.angcyo.library.canvas.core.ICanvasComponent
import com.angcyo.library.canvas.core.ICanvasTouchListener
import com.angcyo.library.canvas.core.ICanvasView
import com.angcyo.library.canvas.core.IRenderElement

/**
 * 元素点击事件回调
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/06/28
 */
open class ElementClickComponent(
    protected val iCanvasView: ICanvasView,
    /**回调*/
    val onClickElementAction: (IRenderElement) -> Unit
) : ICanvasTouchListener, ICanvasComponent {

    override var isEnableComponent: Boolean = true

    private val _tempPoint = PointF()
    private var _touchRenderer: IRenderElement? = null

    override fun dispatchTouchEvent(event: MotionEvent) {
        val eventX = event.getX(event.actionIndex)
        val eventY = event.getY(event.actionIndex)
        _tempPoint.set(eventX, eventY)
        iCanvasView.getCanvasViewBox().transformToInside(_tempPoint)

        if (BuildConfig.DEBUG) {
            L.d("按下:[${_tempPoint}]")
        }

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                val list = iCanvasView.getRenderManager().findRendererList(_tempPoint)
                _touchRenderer = list.lastOrNull()
            }

            MotionEvent.ACTION_UP -> {
                val list = iCanvasView.getRenderManager().findRendererList(_tempPoint)
                if (list.contains(_touchRenderer)) {
                    _touchRenderer?.let {
                        onClickElement(it)
                    }
                }
            }
        }
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        return false
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return true
    }

    protected open fun onClickElement(element: IRenderElement) {
        onClickElementAction(element)
    }
}