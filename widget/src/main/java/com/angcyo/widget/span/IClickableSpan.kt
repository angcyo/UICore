package com.angcyo.widget.span

import android.view.MotionEvent
import android.view.View
import com.angcyo.library.L
import com.angcyo.widget.base.actionToString

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/01/11
 */
interface IClickableSpan {

    fun isCanClick(): Boolean {
        return true
    }

    fun onClickSpan(view: View, span: IClickableSpan) {
        L.i(span)
    }

    fun onTouchEvent(view: View, span: IClickableSpan, event: MotionEvent) {
        L.v("$span ${event.actionToString()}")
    }
}