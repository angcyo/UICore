package com.angcyo.library.gesture

import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import androidx.core.view.GestureDetectorCompat
import com.angcyo.library.app

/**
 * 双击手势识别
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/05/06
 */
class DoubleGestureDetector(context: Context = app(), action: (event: MotionEvent) -> Unit) {

    val gestureDetector = GestureDetectorCompat(context,
        object : GestureDetector.SimpleOnGestureListener() {
            override fun onDoubleTap(e: MotionEvent): Boolean {
                action(e)
                return true
            }
        })

    /**入口*/
    fun onTouchEvent(event: MotionEvent): Boolean = gestureDetector.onTouchEvent(event)

}