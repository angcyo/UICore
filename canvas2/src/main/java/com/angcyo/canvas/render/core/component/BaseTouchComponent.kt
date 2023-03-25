package com.angcyo.canvas.render.core.component

import android.graphics.PointF
import android.view.MotionEvent
import com.angcyo.canvas.render.core.CanvasRenderDelegate
import com.angcyo.canvas.render.core.ICanvasTouchListener
import com.angcyo.canvas.render.core.IComponent

/**
 * 基础的手势组件
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/02/16
 */
abstract class BaseTouchComponent : ICanvasTouchListener, IComponent {

    companion object {

        /**获取事件[event]中所有手指的点位信息*/
        fun obtainPointList(
            event: MotionEvent,
            list: MutableList<PointF>
        ): List<PointF> {
            list.clear()
            for (i in 0 until event.pointerCount) {
                list.add(PointF(event.getX(i), event.getY(i)))
            }
            return list
        }
    }

    /**数据缓存*/
    protected val _downPointList = mutableListOf<PointF>()
    protected val _movePointList = mutableListOf<PointF>()

    /**是否处理了手势*/
    var isHandleTouch = false

    /**是否临时忽略处理手势*/
    var ignoreHandle = false

    //region---继承---

    /**是否激活组件*/
    override var isEnableComponent: Boolean = true

    override fun dispatchTouchEvent(event: MotionEvent) {
        if (isEnableComponent && !ignoreHandle) {
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> obtainPointList(
                    event,
                    _downPointList
                )
                MotionEvent.ACTION_MOVE -> obtainPointList(event, _movePointList)
            }
            handleTouchEvent(event)//---
            when (event.actionMasked) {
                MotionEvent.ACTION_POINTER_UP -> obtainPointList(event, _downPointList)
            }
        }
        when (event.actionMasked) {
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                isHandleTouch = false
                ignoreHandle = false

                _downPointList.clear()
                _movePointList.clear()
            }
        }
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean = false

    override fun onTouchEvent(event: MotionEvent): Boolean = false

    abstract fun handleTouchEvent(event: MotionEvent)

    //endregion---继承---

    //region---辅助---

    protected val _tempPointList = mutableListOf<PointF>()

    /**获取事件[event]中所有手指的点位信息*/
    fun obtainPointList(
        event: MotionEvent,
        list: MutableList<PointF> = _tempPointList
    ): List<PointF> {
        return BaseTouchComponent.obtainPointList(event, list)
    }

    /**消耗完[_movePointList]之后, 应该将[_downPointList]更新*/
    fun updateDownPointList(list: List<PointF> = _movePointList) {
        _downPointList.clear()
        _downPointList.addAll(list)
    }

    /**禁止元素的控制手势*/
    fun disableControlHandle(delegate: CanvasRenderDelegate) {
        val target = delegate.controlManager._interceptTarget
        if (target is BaseControl) {
            target.handleControl = false
        }
    }

    //endregion---辅助---

}