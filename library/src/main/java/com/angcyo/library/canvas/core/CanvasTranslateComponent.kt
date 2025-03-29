package com.angcyo.library.canvas.core

import android.view.MotionEvent
import com.angcyo.library.L
import com.angcyo.library.ex.dp
import com.angcyo.library.ex.size
import kotlin.math.absoluteValue
import kotlin.math.max
import kotlin.math.min

/**
 * 画板平移组件
 * [com.angcyo.canvas.render.core.CanvasRenderViewBox]
 *
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/02/16
 */
class CanvasTranslateComponent(val iCanvasView: ICanvasView) : BaseCanvasTouchComponent() {

    /**当手指移动的距离大于此值, 是否有效的移动*/
    var translateThreshold = 3 * dp

    override fun handleTouchEvent(event: MotionEvent) {
        if (event.actionMasked == MotionEvent.ACTION_MOVE) {
            handleTranslateIntent()
        }
    }

    /**处理平移意图*/
    fun handleTranslateIntent() {
        if (_movePointList.size() < 2) {
            return
        }

        val dx1 = _movePointList[0].x - _downPointList[0].x
        val dy1 = _movePointList[0].y - _downPointList[0].y

        val dx2 = _movePointList[1].x - _downPointList[1].x
        val dy2 = _movePointList[1].y - _downPointList[1].y

        if (dx1.absoluteValue >= translateThreshold && dx2.absoluteValue >= translateThreshold) {
            if (dx1 > 0 && dx2 > 0) {
                //双指向右移动
                translateBy(max(dx1, dx2), 0f)
            } else if (dx1 < 0 && dx2 < 0) {
                //双指向左移动
                translateBy(min(dx1, dx2), 0f)
            }
        } else if (dy1.absoluteValue >= translateThreshold && dy2.absoluteValue >= translateThreshold) {
            if (dy1 > 0 && dy2 > 0) {
                //双指向下移动
                translateBy(0f, max(dy1, dy2))
            } else if (dy1 < 0 && dy2 < 0) {
                //双指向上移动
                translateBy(0f, min(dy1, dy2))
            }
        }
    }

    private fun translateBy(dx: Float, dy: Float, anim: Boolean = false) {
        L.d("平移手势:dx:${dx} dy:${dy}")
        iCanvasView.getCanvasViewBox().translateBy(dx, dy, anim, Reason.user)
        isHandleTouch = true
        updateDownPointList()
    }

}