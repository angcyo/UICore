package com.angcyo.canvas.render.core.component

import android.graphics.PointF
import android.view.MotionEvent
import com.angcyo.canvas.render.core.CanvasRenderDelegate
import com.angcyo.canvas.render.core.Reason
import com.angcyo.canvas.render.util.midPoint
import com.angcyo.canvas.render.util.spacing
import com.angcyo.library.L
import com.angcyo.library.ex.dp
import com.angcyo.library.ex.size
import com.angcyo.library.gesture.DoubleGestureDetector2
import kotlin.math.absoluteValue

/**
 * 画板缩放组件
 * [com.angcyo.canvas.render.core.CanvasRenderViewBox]
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/02/16
 */
class CanvasScaleComponent(val delegate: CanvasRenderDelegate) : BaseTouchComponent() {

    /**当手指缩放/放大的距离大于此值, 是否有效的缩放*/
    var scaleThreshold = 10 * dp

    /**双击时, 需要放大的比例*/
    var doubleScaleValue = 1.5f

    /**双击检测*/
    private val doubleGestureDetector = DoubleGestureDetector2(delegate.view.context) { event ->
        if (isEnable && !ignoreHandle) {
            isHandleTouch = true
            //双击
            _tempPoint.set(event.x, event.y)
            //delegate.renderViewBox.transformToInside(_tempPoint)
            delegate.renderViewBox.offsetToOrigin(_tempPoint)
            delegate.renderViewBox.scaleBy(
                doubleScaleValue,
                doubleScaleValue,
                _tempPoint.x,
                _tempPoint.y,
                true,
                Reason.user
            )
            L.d("双击缩放手势:sx:${doubleScaleValue} sy:${doubleScaleValue} px:${_tempPoint.x} py:${_tempPoint.y}")
        }
    }

    private val _tempPoint = PointF()

    override fun handleTouchEvent(event: MotionEvent) {
        doubleGestureDetector.onTouchEvent(event)
        if (event.actionMasked == MotionEvent.ACTION_MOVE) {
            handleScaleIntent()
        }
    }

    /**处理缩放意图*/
    private fun handleScaleIntent() {
        if (_movePointList.size() < 2) {
            return
        }

        val c1 = spacing(
            _downPointList[0].x,
            _downPointList[0].y,
            _downPointList[1].x,
            _downPointList[1].y
        )
        val c2 = spacing(
            _movePointList[0].x,
            _movePointList[0].y,
            _movePointList[1].x,
            _movePointList[1].y
        )

        if ((c2 - c1).absoluteValue >= scaleThreshold) {
            //需要处理缩放
            val sx = c2 / c1
            val sy = sx
            midPoint(_movePointList[0], _movePointList[1], _tempPoint)
            //delegate.renderViewBox.transformToInside(_tempPoint)
            delegate.renderViewBox.offsetToOrigin(_tempPoint)
            scaleBy(sx, sy, _tempPoint.x, _tempPoint.y)
        }
    }

    private fun scaleBy(sx: Float, sy: Float, px: Float, py: Float, anim: Boolean = false) {
        L.d("缩放手势:sx:${sx} sy:${sy} px:${px} py:${py}")
        delegate.renderViewBox.scaleBy(sx, sy, px, py, anim, Reason.user)
        isHandleTouch = true
        updateDownPointList()
    }
}