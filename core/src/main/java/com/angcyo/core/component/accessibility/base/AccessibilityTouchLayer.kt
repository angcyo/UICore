package com.angcyo.core.component.accessibility.base

import android.graphics.RectF
import android.view.Gravity
import com.angcyo.core.R
import com.angcyo.core.component.accessibility.BaseAccessibilityService
import com.angcyo.core.component.accessibility.action.toPointF
import com.angcyo.core.component.accessibility.click
import com.angcyo.ilayer.ILayer
import com.angcyo.ilayer.container.DragRectFConstraint
import com.angcyo.ilayer.container.OffsetPosition
import com.angcyo.ilayer.container.WindowContainer
import com.angcyo.library.*
import com.angcyo.library.ex._color
import com.angcyo.library.ex.isDebugType
import com.angcyo.widget.base.setInputText
import com.angcyo.widget.base.string
import com.angcyo.widget.checkEmpty
import com.angcyo.widget.seek
import com.angcyo.widget.span.span
import kotlin.math.min

/**
 * 手势操作测试
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/08/10
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
object AccessibilityTouchLayer : ILayer() {

    val _windowContainer = WindowContainer(app()).apply {
        defaultOffsetPosition = OffsetPosition(Gravity.TOP or Gravity.RIGHT)
    }

    init {
        iLayerLayoutId = R.layout.lib_layout_accessibility_touch
        enableDrag = false
        showCancelLayer = enableDrag
        dragContainer = DragRectFConstraint(
            RectF(0f, _satusBarHeight * 1f / _screenHeight, 0f, 0.0000001f)
        )
    }

    fun show() {
        renderLayer = {

            fun x() = (ev(R.id.x_point_view).string().toFloatOrNull() ?: 0f).toPointF(
                _screenWidth
            )

            fun y() = (ev(R.id.y_point_view).string().toFloatOrNull() ?: 0f).toPointF(
                _screenHeight
            )

            fun updateTip() {
                tv(R.id.lib_text_view)?.text = span {
                    append("w:$_screenWidth")
                    append(" h:$_screenHeight")
                    appendln()
                    append("x:${x().toInt()}") {
                        foregroundColor = _color(R.color.bg_sub_color)
                    }
                    append(" y:${y().toInt()}") {
                        foregroundColor = _color(R.color.bg_sub_color)
                    }
                }
            }

            seek(R.id.x_seek_bar) { value, fraction, fromUser ->
                ev(R.id.x_point_view)?.setInputText(fraction.toFraction().toString())
                updateTip()
            }

            seek(R.id.y_seek_bar) { value, fraction, fromUser ->
                ev(R.id.y_point_view)?.setInputText(fraction.toFraction().toString())
                updateTip()
            }

            updateTip()

            //touch
            throttleClick(R.id.touch_button) {
                if (!checkEmpty(R.id.x_point_view, R.id.y_point_view)) {
                    //"touch:0.9192,0.9842"
                    val x = x()
                    val y = y()

                    postDelay(3_00) {
                        BaseAccessibilityService.lastService?.gesture?.click(x, y)
                        L.w("touch:", x, ",", y)
                    }
                }
            }

            //close
            throttleClick(R.id.lib_close_view) {
                hide()
            }
        }
        show(_windowContainer)
    }

    fun hide() {
        hide(_windowContainer)
    }

    /**比例从0.5开始到0.9999*/
    fun Float.toFraction() = if (isDebugType()) this else min(0.9999f, 0.5f + 0.5f * this)
}