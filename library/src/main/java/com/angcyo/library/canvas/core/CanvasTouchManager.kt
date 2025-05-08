package com.angcyo.library.canvas.core

/**
 * 手势管理类
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/02/14
 */
class CanvasTouchManager(val iCanvasView: ICanvasView) : BaseCanvasTouchDispatch() {

    /**画板平移组件*/
    var translateComponent = CanvasTranslateComponent(iCanvasView)

    /**画板缩放组件*/
    var scaleComponent = CanvasScaleComponent(iCanvasView)

    /**画板快滑组件*/
    var flingComponent = CanvasFlingComponent(iCanvasView)

    /**是否要忽略指定监听器的事件回调*/
    var onIgnoreTouchListener: ((listener: ICanvasTouchListener) -> Boolean)? = null

    init {
        touchListenerList.add(translateComponent)
        touchListenerList.add(scaleComponent)
        touchListenerList.add(flingComponent)
    }

    override fun ignoreTouchListener(listener: ICanvasTouchListener): Boolean {
        if (onIgnoreTouchListener != null) {
            return onIgnoreTouchListener!!.invoke(listener)
        }
        return super.ignoreTouchListener(listener)
    }
}