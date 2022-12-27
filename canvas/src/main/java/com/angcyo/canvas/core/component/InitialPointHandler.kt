package com.angcyo.canvas.core.component

import android.graphics.RectF
import android.view.MotionEvent
import android.view.ViewConfiguration
import com.angcyo.canvas.CanvasDelegate
import com.angcyo.canvas.R
import com.angcyo.library.annotation.CallPoint
import com.angcyo.library.component.MainExecutor
import com.angcyo.library.ex.emptyRectF
import com.angcyo.library.ex.longFeedback
import com.angcyo.library.ex.mH
import com.angcyo.library.ex.nowTime
import com.angcyo.widget.base.showPopupMenu

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/11
 */
class InitialPointHandler : BaseComponent() {

    /**左上角初始点位坐标*/
    val initialPointRect: RectF = emptyRectF()

    /**是否在目标区域按下*/
    var isTouchDownInInitial = false

    val _tempRect: RectF = emptyRectF()

    /**按下的时间, 用来计算是否长按了*/
    var _touchDownTime: Long = -1

    /**长按检测时间*/
    val longPressTimeout = ViewConfiguration.getLongPressTimeout().toLong()//400

    /**长按事件*/
    var _longRunnable: Runnable? = null

    @CallPoint
    fun onTouch(delegate: CanvasDelegate, event: MotionEvent): Boolean {
        val canvasViewBox = delegate.getCanvasViewBox()
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                initialPointRect.set(
                    0f,
                    0f,
                    canvasViewBox.getContentLeft(),
                    canvasViewBox.getContentTop()
                )
                isTouchDownInInitial = initialPointRect.contains(event.x, event.y)
                _touchDownTime = if (isTouchDownInInitial) {
                    _longRunnable = Runnable {
                        if (isTouchDownInInitial) {
                            //长按
                            isTouchDownInInitial = false
                            onLongPress(delegate)
                        }
                    }
                    MainExecutor.delay(_longRunnable!!, longPressTimeout)
                    nowTime()
                } else {
                    -1
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (isTouchDownInInitial) {
                    isTouchDownInInitial = initialPointRect.contains(event.x, event.y)
                }
            }
            MotionEvent.ACTION_UP -> {
                _longRunnable?.let {
                    MainExecutor.remove(it)
                    _longRunnable = null
                }
                if (isTouchDownInInitial) {
                    if (initialPointRect.contains(event.x, event.y)) {
                        return onClick(delegate)
                    }
                }
            }
        }
        return false
    }

    /**点击事件*/
    fun onClick(delegate: CanvasDelegate): Boolean {
        val canvasViewBox = delegate.getCanvasViewBox()
        var def = true
        val primaryLimitBounds = delegate.limitRenderer.getPrimaryLimitBounds()
        if (primaryLimitBounds != null) {
            _tempRect.set(primaryLimitBounds)
            def = false
        }
        if (!def) {
            delegate.showRectBounds(
                _tempRect,
                offsetRectTop = delegate.limitRenderer.getPrimaryLimitInfo()?.offsetRectTop == true
            )
        } else {
            canvasViewBox.updateToMatrix {
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

    /**长按事件*/
    fun onLongPress(delegate: CanvasDelegate): Boolean {
        val canvasViewBox = delegate.viewBox
        delegate.view.longFeedback()
        delegate.view.showPopupMenu(R.menu.initial_menu) {
            offsetX = canvasViewBox.getContentLeft().toInt()
            offsetY = -(delegate.view.mH() - canvasViewBox.getContentTop().toInt())
            menuItemClickAction = {
                when (it.itemId) {
                    R.id.menu_reset -> canvasViewBox.reset()
                    R.id.menu_best -> onClick(delegate)
                    R.id.menu_ratio_1 -> canvasViewBox.scaleTo(1f, 1f)
                    R.id.menu_origin -> canvasViewBox.translateTo(0f, 0f)
                }
                true
            }
        }
        return false
    }
}