package com.angcyo.canvas.render.core

import com.angcyo.canvas.render.core.component.CanvasFlingComponent
import com.angcyo.canvas.render.core.component.CanvasScaleComponent
import com.angcyo.canvas.render.core.component.CanvasTranslateComponent

/**
 * 手势管理类
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/02/14
 */
class CanvasTouchManager(val delegate: CanvasRenderDelegate) : BaseTouchDispatch() {

    /**画板平移组件*/
    var translateComponent = CanvasTranslateComponent(delegate)

    /**画板缩放组件*/
    var scaleComponent = CanvasScaleComponent(delegate)

    /**画板快滑组件*/
    var flingComponent = CanvasFlingComponent(delegate)

    init {
        touchListenerList.add(translateComponent)
        touchListenerList.add(scaleComponent)
        touchListenerList.add(flingComponent)
    }

}