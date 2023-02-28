package com.angcyo.canvas.render.core.component

import android.graphics.PointF
import android.view.MotionEvent
import com.angcyo.canvas.render.R
import com.angcyo.canvas.render.annotation.CanvasInsideCoordinate
import com.angcyo.canvas.render.core.CanvasControlManager
import com.angcyo.canvas.render.core.Reason
import com.angcyo.library.L
import com.angcyo.library.ex._drawable
import kotlin.math.atan2

/**
 * 旋转控制点
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/02/23
 */
class RotateControlPoint(controlManager: CanvasControlManager) : BaseControlPoint(controlManager) {

    /**操作目标的中点坐标*/
    @CanvasInsideCoordinate
    private val centerPointInside = PointF()

    init {
        drawable = _drawable(R.drawable.canvas_render_control_point_rotate)
        controlType = CONTROL_TYPE_ROTATE
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        super.onTouchEvent(event)
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                val selectorComponent = controlManager.delegate.selectorManager.selectorComponent
                startControl(selectorComponent)
                selectorComponent.showRotateRender(Reason.preview, null)

                selectorComponent.renderProperty?.let {
                    it.getRenderCenter(centerPointInside)
                }
            }
            MotionEvent.ACTION_MOVE -> {
                val angle = calculateAngleBetweenLines(
                    centerPointInside.x,
                    centerPointInside.y,
                    touchDownPointInside.x,
                    touchDownPointInside.y,
                    centerPointInside.x,
                    centerPointInside.y,
                    touchMovePointInside.x,
                    touchMovePointInside.y,
                )
                if (angle != 0f) {
                    rotate(angle)
                }
            }
        }
        return true
    }

    /**直接从原始位置, 旋转多少角度*/
    private fun rotate(angle: Float) {
        L.i("旋转元素:angle:$angle")
        controlRendererInfo?.let {
            isControlHappen = true
            controlMatrix.setRotate(angle, centerPointInside.x, centerPointInside.y)

            applyRotate(Reason.preview, controlManager.delegate)
        }
    }

    override fun endControl() {
        if (isControlHappen) {
            controlRendererInfo?.let {
                applyRotate(Reason.user, controlManager.delegate)
            }
        }
        super.endControl()
    }

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
        var angle = angleTo % 360.0f - angleFrom % 360.0f
        if (angle < -180.0f) {
            angle += 360.0f
        } else if (angle > 180.0f) {
            angle -= 360.0f
        }
        return angle
    }

}