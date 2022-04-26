package com.angcyo.canvas.core.component.control

import android.graphics.PointF
import android.view.MotionEvent
import com.angcyo.canvas.CanvasView
import com.angcyo.canvas.R
import com.angcyo.canvas.core.component.ControlPoint
import com.angcyo.canvas.items.renderer.BaseItemRenderer
import com.angcyo.library.L
import com.angcyo.library.ex._drawable
import kotlin.math.atan2

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/09
 */
class RotateControlPoint : ControlPoint() {

    //按下的坐标
    val _touchPoint = PointF()

    val _movePoint = PointF()

    //中点坐标
    val _centerPoint = PointF()

    /**每次移动旋转的角度*/
    var angle = 0f

    init {
        drawable = _drawable(R.drawable.control_point_rotate)
    }

    override fun onTouch(
        view: CanvasView,
        itemRenderer: BaseItemRenderer<*>,
        event: MotionEvent
    ): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                _touchPoint.set(event.x, event.y)
                val bounds = itemRenderer.getVisualBounds()
                _centerPoint.set(bounds.centerX(), bounds.centerY())
            }
            MotionEvent.ACTION_MOVE -> {
                _movePoint.set(event.x, event.y)
                calculateAngleBetweenLines(
                    _centerPoint.x,
                    _centerPoint.y,
                    _touchPoint.x,
                    _touchPoint.y,
                    _centerPoint.x,
                    _centerPoint.y,
                    _movePoint.x,
                    _movePoint.y,
                )
                if (angle != 0f) {
                    L.i("即将旋转:$angle °")
                    val assistant = view.smartAssistant.smartRotateBy(itemRenderer, angle)
                    _touchPoint.set(_movePoint)
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                angle = 0f
            }
        }
        return true
    }

    //<editor-fold desc="RotationGestureDetector">

    /**计算两个线之间的角度*/
    private fun calculateAngleBetweenLines(
        fx1: Float, fy1: Float, fx2: Float, fy2: Float, //第一根线的2个坐标
        sx1: Float, sy1: Float, sx2: Float, sy2: Float //第二根线的2个坐标
    ): Float {
        return calculateAngleDelta(
            Math.toDegrees(
                atan2(
                    (fy1 - fy2).toDouble(),
                    (fx1 - fx2).toDouble()
                )
            ).toFloat(),
            Math.toDegrees(
                atan2(
                    (sy1 - sy2).toDouble(),
                    (sx1 - sx2).toDouble()
                )
            ).toFloat()
        )
    }

    private fun calculateAngleDelta(angleFrom: Float, angleTo: Float): Float {
        angle = angleTo % 360.0f - angleFrom % 360.0f
        if (angle < -180.0f) {
            angle += 360.0f
        } else if (angle > 180.0f) {
            angle -= 360.0f
        }
        return angle
    }

    //</editor-fold desc="RotationGestureDetector">
}