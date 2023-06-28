package com.angcyo.library.canvas.core

import android.view.MotionEvent
import com.angcyo.library.canvas.annotation.CanvasOutsideCoordinate

/**
 * 手势回调监听, 事件坐标均是相对于画板左上角的坐标,
 * 可能需要调用[transformToInside]方法将坐标映射到画板内部
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/02/16
 */
@CanvasOutsideCoordinate
interface ICanvasTouchListener {

    /**派发手势的回调, 此方法一定会触发*/
    @CanvasOutsideCoordinate
    fun dispatchTouchEvent(event: MotionEvent) {
    }

    /**是否要拦截手势处理, 拦截后. 其他对象将不接收事件回调, 但是[dispatchTouchEvent]还是会触发*/
    @CanvasOutsideCoordinate
    fun onInterceptTouchEvent(event: MotionEvent): Boolean = false

    /**当[onInterceptTouchEvent]返回true时, 则之后的事件会走此方法*/
    @CanvasOutsideCoordinate
    fun onTouchEvent(event: MotionEvent): Boolean = false

}