package com.angcyo.canvas.render.core.component

import android.graphics.Canvas
import android.graphics.Paint
import android.view.MotionEvent
import com.angcyo.canvas.render.R
import com.angcyo.canvas.render.core.CanvasSelectorManager
import com.angcyo.canvas.render.core.IRenderer
import com.angcyo.canvas.render.data.RenderParams
import com.angcyo.canvas.render.util.createRenderPaint
import com.angcyo.library.canvas.core.Reason
import com.angcyo.library.ex._color
import com.angcyo.library.ex.alpha
import com.angcyo.library.ex.dp
import com.angcyo.library.ex.emptyRectF
import kotlin.math.absoluteValue
import kotlin.math.max
import kotlin.math.min

/**
 * 滑动多选组件
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/02/22
 */
class CanvasMoveSelectorComponent(val selectorManager: CanvasSelectorManager) :
    BaseTouchComponent(), IRenderer {

    /**当手指移动的距离大于此值, 是否有效的移动*/
    var moveThreshold = 3 * dp

    /**绘制的画笔属性*/
    var paint = createRenderPaint(width = 1 * dp)

    /**选择框的颜色*/
    var borderColor: Int = _color(R.color.canvas_render_select)

    /**选择框填充的颜色*/
    val fillColor: Int
        get() = borderColor.alpha(32)

    override var isEnableComponent: Boolean = true

    override var renderFlags: Int = 0xf

    private val selectRect = emptyRectF()
    private val _tempRect = emptyRectF()

    override fun renderOnOutside(canvas: Canvas, params: RenderParams) {
        if (isHandleTouch) {
            paint.style = Paint.Style.FILL
            paint.color = fillColor
            canvas.drawRect(selectRect, paint)

            paint.style = Paint.Style.STROKE
            paint.color = borderColor
            canvas.drawRect(selectRect, paint)
        }
    }

    override fun handleTouchEvent(event: MotionEvent) {
        when (event.actionMasked) {
            MotionEvent.ACTION_POINTER_DOWN -> {
                isHandleTouch = false
                ignoreHandle = true
            }

            MotionEvent.ACTION_MOVE -> {
                val dx = _movePointList[0].x - _downPointList[0].x
                val dy = _movePointList[0].y - _downPointList[0].y

                if (dx.absoluteValue >= moveThreshold || dy.absoluteValue >= moveThreshold) {
                    //有移动
                    isHandleTouch = true
                }

                selectRect.set(
                    min(_downPointList[0].x, _movePointList[0].x),
                    min(_downPointList[0].y, _movePointList[0].y),
                    max(_downPointList[0].x, _movePointList[0].x),
                    max(_downPointList[0].y, _movePointList[0].y),
                )

                if (isHandleTouch) {
                    selectorManager.removeDelayCancelSelectRenderer()
                    selectorManager.delegate.refresh()
                }
            }

            MotionEvent.ACTION_UP -> {
                if (isHandleTouch) {
                    selectorManager.delegate.renderViewBox.transformToInside(selectRect, _tempRect)

                    val list = selectorManager.findRendererList(_tempRect)
                    selectorManager.selectorComponent.resetSelectorRenderer(list, Reason.preview)
                    selectorManager.delegate.refresh()
                }
            }
        }
    }

}