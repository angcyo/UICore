package com.angcyo.canvas.core.component

import android.graphics.RectF
import android.view.MotionEvent
import com.angcyo.canvas.CanvasDelegate
import com.angcyo.library.ex.emptyRectF
import com.angcyo.library.ex.isNoSize

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/11
 */
class InitialPointHandler : BaseComponent() {

    /**左上角初始点位坐标*/
    val initialPointRect: RectF = emptyRectF()
    var isTouchDownInInitial = false

    fun onTouch(view: CanvasDelegate, event: MotionEvent): Boolean {
        val canvasViewBox = view.getCanvasViewBox()
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                initialPointRect.set(
                    0f,
                    0f,
                    canvasViewBox.getContentLeft(),
                    canvasViewBox.getContentTop()
                )
                isTouchDownInInitial = initialPointRect.contains(event.x, event.y)
            }
            MotionEvent.ACTION_UP -> {
                if (isTouchDownInInitial) {
                    if (initialPointRect.contains(event.x, event.y)) {
                        var def = true
                        val path = view.limitRenderer.limitPath
                        if (path.isEmpty) {
                            //def
                        } else {
                            path.computeBounds(_tempRect, true)
                            if (_tempRect.isNoSize()) {
                                //def
                            } else {
                                def = false
                            }
                        }
                        if (!def) {
                            view.showRectBounds(_tempRect)
                        } else {
                            canvasViewBox.updateTo {
                                setTranslate(0f, 0f)
                                postScale(
                                    canvasViewBox.getScaleX(),
                                    canvasViewBox.getScaleY(),
                                    canvasViewBox.getCoordinateSystemX(),
                                    canvasViewBox.getCoordinateSystemY()
                                )
                            }
                        }
                        return true
                    }
                }
            }
        }
        return false
    }
}