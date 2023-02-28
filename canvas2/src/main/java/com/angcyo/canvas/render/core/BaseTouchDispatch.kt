package com.angcyo.canvas.render.core

import android.view.MotionEvent
import com.angcyo.canvas.render.annotation.CanvasOutsideCoordinate
import com.angcyo.library.annotation.CallPoint
import java.util.concurrent.CopyOnWriteArrayList

/**
 * 手势分发基类
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/02/23
 */
abstract class BaseTouchDispatch {

    /**事件监听列表*/
    val touchListenerList = CopyOnWriteArrayList<ICanvasTouchListener>()

    /**手势是否按下*/
    var _isTouchDown = false

    /**被目标拦截*/
    var _interceptTarget: ICanvasTouchListener? = null

    /**事件是否有拦截的目标*/
    val haveInterceptTarget: Boolean
        get() = _interceptTarget != null

    @CanvasOutsideCoordinate
    @CallPoint
    open fun dispatchTouchEventDelegate(event: MotionEvent): Boolean {
        val action = event.actionMasked

        //init
        if (action == MotionEvent.ACTION_DOWN) {
            _isTouchDown = true
        } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
            _isTouchDown = false
        }

        //1:dispatchTouchEvent
        val size = touchListenerList.size
        for (i in size - 1 downTo 0) {
            val listener = touchListenerList[i]
            if (listener is IComponent && !listener.isEnable) {
                continue
            }
            listener.dispatchTouchEvent(event)
        }

        var handle = false
        val target = _interceptTarget
        if (target == null) {
            //当前事件, 没有被特定的目标处理

            //2:onInterceptTouchEvent
            for (i in size - 1 downTo 0) {
                val listener = touchListenerList[i]
                if (listener is IComponent && !listener.isEnable) {
                    continue
                }

                val intercept = listener.onInterceptTouchEvent(event)
                if (intercept) {
                    //被拦截
                    _interceptTarget = listener
                    onTouchEventIntercept(listener)

                    //3:onTouchEvent
                    handle = listener.onTouchEvent(event)
                    break
                }
            }
        } else {
            //3:onTouchEvent

            if (target is IComponent && target.isEnable) {
                handle = target.onTouchEvent(event)
            }
        }

        //收尾
        if (action == MotionEvent.ACTION_UP ||
            action == MotionEvent.ACTION_CANCEL
        ) {
            _interceptTarget = null
        }

        return handle
    }

    /**当事件被[target]拦截*/
    open fun onTouchEventIntercept(target: ICanvasTouchListener) {

    }

}