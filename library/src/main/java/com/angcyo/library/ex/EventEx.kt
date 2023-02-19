package com.angcyo.library.ex

import android.view.MotionEvent
import android.view.View

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/12/20
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */

fun MotionEvent.isTouchDown(): Boolean {
    return actionMasked == MotionEvent.ACTION_DOWN
}

fun MotionEvent.isTouchFinish(): Boolean {
    return actionMasked == MotionEvent.ACTION_UP || actionMasked == MotionEvent.ACTION_CANCEL
}

fun MotionEvent.isTouchMove(): Boolean {
    return actionMasked == MotionEvent.ACTION_MOVE
}

/**是否在当前view中touch*/
fun MotionEvent.isTouchIn(view: View?): Boolean {
    if (view == null) {
        return false
    }
    return view.viewFrameF().contains(x, y)
}

/**是否在当前view中touch*/
fun MotionEvent.isScreenTouchIn(view: View?): Boolean {
    if (view == null) {
        return false
    }
    return view.viewScreenFrameF().contains(rawX, rawY)
}

fun MotionEvent.actionToString(): String = this.actionMasked.actionToString()

fun Int.toActionToStr() = actionToString()

fun Int.actionToString(): String {
    val action = this
    when (action) {
        MotionEvent.ACTION_DOWN -> return "ACTION_DOWN"
        MotionEvent.ACTION_UP -> return "ACTION_UP"
        MotionEvent.ACTION_CANCEL -> return "ACTION_CANCEL"
        MotionEvent.ACTION_OUTSIDE -> return "ACTION_OUTSIDE"
        MotionEvent.ACTION_MOVE -> return "ACTION_MOVE"
        MotionEvent.ACTION_HOVER_MOVE -> return "ACTION_HOVER_MOVE"
        MotionEvent.ACTION_SCROLL -> return "ACTION_SCROLL"
        MotionEvent.ACTION_HOVER_ENTER -> return "ACTION_HOVER_ENTER"
        MotionEvent.ACTION_HOVER_EXIT -> return "ACTION_HOVER_EXIT"
        11 -> return "ACTION_BUTTON_PRESS"
        12 -> return "ACTION_BUTTON_RELEASE"
    }
    val index =
        action and MotionEvent.ACTION_POINTER_INDEX_MASK shr MotionEvent.ACTION_POINTER_INDEX_SHIFT
    return when (action and MotionEvent.ACTION_MASK) {
        MotionEvent.ACTION_POINTER_DOWN -> "ACTION_POINTER_DOWN($index)"
        MotionEvent.ACTION_POINTER_UP -> "ACTION_POINTER_UP($index)"
        else -> action.toString()
    }
}

/**
 * 创建一个[MotionEvent]事件, 请主动调用 [android.view.MotionEvent.recycle]方法*/
fun motionEvent(action: Int = MotionEvent.ACTION_UP, x: Float = 0f, y: Float = 0f): MotionEvent {
    return MotionEvent.obtain(
        nowTime(),
        nowTime(),
        action,
        x,
        y,
        0
    )
}

/**创建一个取消事件*/
fun cancelEvent() = MotionEvent.obtain(
    nowTime(),
    nowTime(),
    MotionEvent.ACTION_CANCEL,
    0f,
    0f,
    0
)

/**发送一个取消事件*/
fun View.sendCancelEvent() {
    val cancelEvent = cancelEvent()
    dispatchTouchEvent(cancelEvent)
    cancelEvent.recycle()
}