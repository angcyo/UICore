package com.angcyo.canvas.core.component

import android.graphics.RectF
import android.view.MotionEvent
import com.angcyo.canvas.CanvasDelegate

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/11
 */
class InitialPointHandler : BaseComponent() {

    /**左上角初始点位坐标*/
    val initialPointRect: RectF = RectF()
    var isTouchDownInInitial = false

    fun onTouch(view: CanvasDelegate, event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                initialPointRect.set(
                    0f,
                    0f,
                    view.getCanvasViewBox().getContentLeft(),
                    view.getCanvasViewBox().getContentTop()
                )
                isTouchDownInInitial = initialPointRect.contains(event.x, event.y)
            }
            MotionEvent.ACTION_UP -> {
                if (isTouchDownInInitial) {
                    if (initialPointRect.contains(event.x, event.y)) {
                        view.getCanvasViewBox().updateTo()
                        return true
                    }
                }
            }
        }
        return false
    }
}